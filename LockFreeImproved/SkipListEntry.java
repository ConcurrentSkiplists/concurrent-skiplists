package LockFreeImproved;

import java.util.*;
import java.util.concurrent.atomic.*;

public class SkipListEntry {
	Integer value;
	String key;
	AtomicMarkableReference<SkipListEntry>[] next;

	public int topLevel;

	public static final int MAX_LEVEL = SkipList.MAX_LEVEL;

	// constructor for sentinel SkipListEntrys
	public SkipListEntry(String key) {
		value = null;
		this.key = key;
		next = (AtomicMarkableReference<SkipListEntry>[])
		       new AtomicMarkableReference[MAX_LEVEL + 1];
		for (int i = 0; i < next.length; i++) {
			next[i] = new AtomicMarkableReference<SkipListEntry>(null,false);
		}
		topLevel = MAX_LEVEL;
	}

	// constructor for ordinary SkipListEntrys
	public SkipListEntry(String key, Integer x, int height) {
		value = x;
		this.key = key;
		next = (AtomicMarkableReference<SkipListEntry>[]) new AtomicMarkableReference[MAX_LEVEL + 1];
		for (int i = 0; i < next.length; i++) {
			next[i] = new AtomicMarkableReference<SkipListEntry>(null,false);
		}
		topLevel = height;
	}
}
