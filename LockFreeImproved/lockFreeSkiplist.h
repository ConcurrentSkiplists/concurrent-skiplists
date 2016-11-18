/*  Concurrent Skip List
 *  Copyright (c) 2010, Dmitry Vyukov
 *  Distributed under the terms of the GNU General Public License
 *  as published by the Free Software Foundation,
 *  either version 3 of the License,
 *  or (at your option) any later version.
 *  See: http://www.gnu.org/licenses
 */ 

#pragma once

#include "base.h"
#include "atomic.h"


template<typename item_t, typename traits_t>
class concurrent_skiplist
{
public:
    concurrent_skiplist()
        : head_(alloc_sentinel())
        , tls_slot_(TlsAlloc())
    {
        if (tls_slot_ == TLS_OUT_OF_INDEXES)
            throw runtime_error("failed to allocate a TLS slot");
        level_.store(1, memory_order_relaxed);
        thread_list_.store(0, memory_order_relaxed);

        SYSTEM_INFO info = {};
        GetSystemInfo(&info);
        allocation_granularity_ = info.dwAllocationGranularity;
    }

    ~concurrent_skiplist()
    {
        thread_t* thr = thread_list_.load(memory_order_relaxed);
        while (thr)
        {
            for (size_t i = 0; i != thr->mem_blocks.size(); i += 1)
                VirtualFree(thr->mem_blocks[i], 0, MEM_RELEASE);
            thread_t* next = thr->next;
            _aligned_free(thr);
            thr = next;
        }
        TlsFree(tls_slot_);
        _aligned_free(head_);
    }

    bool insert(item_t item, item_t*& iter)
    {
        track_t track [max_level_count];

        size_t const max_level = level_.load(memory_order_acquire);
        size_t level = max_level;
        node_t* prev = head_;
        node_t* pos = prev->next[level].load(memory_order_consume);
        for (;;)
        {
            if (pos)
            {
                traits_t::comp_result_t cmp = traits_t::compare(item, pos->item);
                if (cmp > 0)
                {
                    prev = pos;
                    pos = pos->next[level].load(memory_order_consume);
                }
                else if (cmp < 0)
                {
                    track[level].node = prev;
                    track[level].next = pos;
                    if (level == 0)
                        break;
                    level -= 1;
                    pos = prev->next[level].load(memory_order_consume);
                }
                else
                {
                    iter = &pos->item;
                    return false;
                }
            }
            else
            {
                track[level].node = prev;
                track[level].next = pos;
                if (level == 0)
                    break;
                level -= 1;
                pos = prev->next[level].load(memory_order_consume);
            }
        }

        thread_t* thr = get_thread_desc();
        size_t const size = thr->size.load(memory_order_relaxed);
        thr->size.store(size + 1, memory_order_relaxed);
        bool const do_update_level = (size >= thr->next_check);
        level = random_level(thr);
        node_t* node = alloc_node(thr, level, item);

        for (size_t i = 0; i <= level; i += 1)
        {
            for (;;)
            {
                node->next[i].store(track[i].next, memory_order_relaxed);
                if (track[i].node->next[i].compare_exchange_strong(track[i].next, node, memory_order_release))
                    break;
                assert(track[i].next != 0);
                l_retry:
                traits_t::comp_result_t cmp = traits_t::compare(item, track[i].next->item);
                if (cmp > 0)
                {
                    track[i].node = track[i].next;
                    track[i].next = track[i].node->next[i].load(memory_order_consume);
                    if (track[i].next)
                        goto l_retry;
                }
                else if (cmp == 0)
                {
                    assert(i == 0);
                    free_node(thr, level, node);
                    iter = &track[i].next->item;
                    return false;
                }
            }
        }

        if (do_update_level)
            update_level();

        iter = &node->item;
        return true;
    }

    item_t* find(item_t item)
    {
        size_t level = level_.load(memory_order_acquire);
        node_t* prev = head_;
        node_t* pos = prev->next[level].load(memory_order_consume);
        for (;;)
        {
            if (pos)
            {
                traits_t::comp_result_t cmp = traits_t::compare(item, pos->item);
                if (cmp > 0)
                {
                    prev = pos;
                    pos = pos->next[level].load(memory_order_consume);
                }
                else if (cmp < 0)
                {
                    if (level == 0)
                        return 0;
                    level -= 1;
                    pos = prev->next[level].load(memory_order_consume);
                }
                else
                {
                    return &pos->item;
                }
            }
            else
            {
                if (level == 0)
                    return 0;
                level -= 1;
                pos = prev->next[level].load(memory_order_consume);
            }
        }
    }

    template<typename func_t>
    void foreach(func_t& func)
    {
        node_t* pos = head_->next[0].load(memory_order_relaxed);
        while (pos)
        {
            func(pos->item);
            pos = pos->next[0].load(memory_order_relaxed);
        }
    }

private:
    static size_t const     max_level_count = 44;
    static size_t const     density_factor  = 1;

    struct node_t
    {
        item_t              item;
        atomic<node_t*>     next [1];

        node_t(item_t item)
            : item(item)
        {}
    };

    struct track_t
    {
        node_t*             node;
        node_t*             next;
    };

    struct thread_t
    {
        atomic<size_t>      size;
        size_t              next_check;
        uint64_t            rand;
        thread_t*           next;
        void*               cache [max_level_count];
        vector<void*>       mem_blocks;
    };

    unsigned long const     tls_slot_;
    unsigned long           allocation_granularity_;
    node_t* const           head_;
    atomic<size_t>          level_;
    atomic<thread_t*>       thread_list_;

    size_t random_level(thread_t* thr)
    {
        size_t const max_level = level_.load(memory_order_acquire);
        size_t r = thr->rand * 16807;
        thr->rand = r;
        size_t tmp = (size_t)(r % (1ull << (max_level * density_factor - 1)));
        unsigned long idx;
#ifdef _M_X64
        if (_BitScanReverse64(&idx, tmp))
#else
        if (_BitScanReverse(&idx, tmp))
#endif
            idx += 1;
        else
            idx = 0;
        size_t const level = max_level - idx / density_factor - 1;
        assert(level < max_level);
        return level;
    }

    __declspec(noinline)
    void update_level()
    {
        thread_t* thr = get_thread_desc();
        size_t total_size = 0;
        size_t thread_count = 0;
        thread_t* desc = thread_list_.load(memory_order_consume);
        while (desc)
        {
            thread_count += 1;
            total_size += desc->size.load(memory_order_relaxed);
            desc = desc->next;
        }
        size_t new_level = level_.load(memory_order_acquire);
        if (total_size > (1ull << new_level) * density_factor)
        {
            unsigned long idx;
#ifdef _M_X64
            _BitScanReverse64(&idx, total_size);
#else
            _BitScanReverse(&idx, total_size);
#endif
            new_level = idx + 1;
            if (new_level >= max_level_count)
                new_level = max_level_count - 1;
            size_t cmp = level_.load(memory_order_relaxed);
            do
            {
                if (cmp >= new_level)
                {
                    new_level = cmp;
                    break;
                }
            }
            while (false == level_.compare_exchange_strong(cmp, new_level, memory_order_relaxed));
        }
        size_t const remain = (1ull << new_level) * density_factor - total_size;
        size_t const size = thr->size.load(memory_order_relaxed);
        thr->next_check = size + remain / thread_count / 2;
    }

    node_t* alloc_sentinel()
    {
        size_t const sz = sizeof(node_t) + sizeof(atomic<node_t*>) * (max_level_count - 1);
        void* mem = _aligned_malloc(sz, 64);
        if (mem == 0)
            throw bad_alloc();
        node_t* node = new (mem) node_t (item_t());
        for (size_t i = 0; i != max_level_count; i += 1)
            node->next[i].store(0, memory_order_relaxed);
        return node;
    }

    node_t* alloc_node(thread_t* thr, size_t level, item_t item)
    {
        assert(thr && level < max_level_count);
        void* mem = thr->cache[level];
        if (mem)
        {
            thr->cache[level] = *(void**)mem;
            node_t* node = new (mem) node_t (item);
            return node;
        }
        return alloc_node_slow(thr, level, item);
    }

    void free_node(thread_t* thr, size_t level, node_t* node)
    {
        assert(thr && level < max_level_count && node);
        node->~node_t();
        *(void**)node = thr->cache[level];
        thr->cache[level] = node;
    }

    __declspec(noinline)
    node_t* alloc_node_slow(thread_t* thr, size_t level, item_t item)
    {
        assert(thr && level < max_level_count && thr->cache[level] == 0);
        
        void* mem = VirtualAlloc(0, allocation_granularity_, MEM_RESERVE | MEM_COMMIT, PAGE_READWRITE);
        if (mem == 0)
            throw bad_alloc();
        thr->mem_blocks.push_back(mem);
        char* head = 0;
        char* pos = (char*)mem;
        char* end = pos + allocation_granularity_;
        size_t const sz = sizeof(node_t) + sizeof(atomic<node_t*>) * level;
        for (; pos + sz <= end; pos += sz)
        {
            *(char**)pos = head;
            head = pos;
        }
        thr->cache[level] = head;
        return alloc_node(thr, level, item);
    }

    thread_t* get_thread_desc()
    {
        thread_t* thr = (thread_t*)TlsGetValue(tls_slot_);
        if (thr)
            return thr;
        return init_thread_desc();
    }

    __declspec(noinline)
    thread_t* init_thread_desc()
    {
        void* mem = _aligned_malloc(sizeof(thread_t), 128);
        if (mem == 0)
            throw bad_alloc();
        TlsSetValue(tls_slot_, mem);
        thread_t* thr = new (mem) thread_t;
        thr->rand = __rdtsc() + GetCurrentThreadId();
        thr->mem_blocks.reserve(1024);
        thr->size.store(0, memory_order_relaxed);
        thr->next_check = 1;
        for (size_t i = 0; i != max_level_count; i += 1)
            thr->cache[i] = 0;
        thr->next = thread_list_.load(memory_order_acquire);
        while (false == thread_list_.compare_exchange_strong(thr->next, thr, memory_order_acq_rel)) {}
        return thr;
    }

    concurrent_skiplist(concurrent_skiplist const&);
    void operator = (concurrent_skiplist const&);
};




