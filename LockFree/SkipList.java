package LockFree;

import java.util.*;
import java.util.concurrent.atomic.*;

public final class SkipList implements SkipListInterface.SkipListInterface {
	public static final int MAX_LEVEL = 32 - 1;
	final SkipListEntry head = new SkipListEntry("! -oo");
	final SkipListEntry tail = new SkipListEntry("~ +oo");

	Random r = new Random();

	int randomLevel() {
		int i = 0;
		while(r.nextDouble() < 0.5) {
			i ++;
		}

		return i;
	}
	public SkipList() {
		for (int i = 0; i < head.next.length; i++) {
			head.next[i]
			    = new AtomicMarkableReference<SkipListEntry>(tail, false);
		}
	}

	public boolean add(String key, Integer value) {
		int topLevel = randomLevel();
		int bottomLevel = 0;
		SkipListEntry[] preds = (SkipListEntry[]) new SkipListEntry[MAX_LEVEL + 1];
		SkipListEntry[] succs = (SkipListEntry[]) new SkipListEntry[MAX_LEVEL + 1];

		while (true) {
			boolean found = find(key, preds, succs);
			if (found) {
				succs[0].value = value;
				return true;
			} else {
				SkipListEntry newSkipListEntry = new SkipListEntry(key, value, topLevel);
				for (int level = bottomLevel; level <= topLevel; level++) {
					SkipListEntry succ = succs[level];
					newSkipListEntry.next[level].set(succ, false);
				}

				SkipListEntry pred = preds[bottomLevel];
				SkipListEntry succ = succs[bottomLevel];
				newSkipListEntry.next[bottomLevel].set(succ, false);
				if (!pred.next[bottomLevel].compareAndSet(succ, newSkipListEntry, false, false)) {
					continue;
				}

				for (int level = bottomLevel+1; level <= topLevel; level++) {
					while (true) {
						pred = preds[level];
						succ = succs[level];
						if (pred.next[level].compareAndSet(succ, newSkipListEntry, false, false))
							break;
						find(key, preds, succs);
					}
				}
				return true;
			}
		}
	}

	public boolean remove(String key) {
		int bottomLevel = 0;
		SkipListEntry[] preds = (SkipListEntry[]) new SkipListEntry[MAX_LEVEL + 1];
		SkipListEntry[] succs = (SkipListEntry[]) new SkipListEntry[MAX_LEVEL + 1];
		SkipListEntry succ;

		while (true) {
			boolean found = find(key, preds, succs);
			if (!found) {
				return false;
			} else {
				SkipListEntry nodeToRemove = succs[bottomLevel];

				for (int level = nodeToRemove.topLevel; level >= bottomLevel+1; level--) {
					boolean[] marked = {false};
					succ = nodeToRemove.next[level].get(marked);
					while (!marked[0]) {
						nodeToRemove.next[level].attemptMark(succ, true);
						succ = nodeToRemove.next[level].get(marked);
					}
				}

				boolean[] marked = {false};
				succ = nodeToRemove.next[bottomLevel].get(marked);
				while (true) {
					boolean iMarkedIt =
					    nodeToRemove.next[bottomLevel].compareAndSet(succ, succ, false, true);

					succ = succs[bottomLevel].next[bottomLevel].get(marked);
					if (iMarkedIt) {
						find(key, preds, succs);
						return true;
					} else if (marked[0]) return false;
				}
			}
		}
	}

	boolean find(String key, SkipListEntry[] preds, SkipListEntry[] succs) {
		int bottomLevel = 0;
		boolean[] marked = {false};
		boolean snip;
		SkipListEntry pred = null, curr = null, succ = null;
		retry:
		while (true) {
			pred = head;
			for (int level = MAX_LEVEL; level >= bottomLevel; level--) {
				curr = pred.next[level].getReference();
				while (true) {
					succ = curr.next[level].get(marked);
					while (marked[0]) {
						snip = pred.next[level].compareAndSet(curr, succ, false, false);
						if (!snip) continue retry;
						curr = pred.next[level].getReference();
						succ = curr.next[level].get(marked);
					}
					if (curr.key.compareTo(key) < 0) {
						pred = curr;
						curr = succ;
					} else {
						break;
					}
				}
				preds[level] = pred;
				succs[level] = curr;
			}
			return (curr.key.equals(key));
		}
	}

	public Integer get(String key) {
		int bottomLevel = 0;
		boolean[] marked = {false};
		SkipListEntry pred = head, curr = null, succ = null;
		for (int level = MAX_LEVEL; level >= bottomLevel; level--) {
			curr = pred.next[level].getReference();
			while (true) {
				succ = curr.next[level].get(marked);
				while (marked[0]) {
					curr = pred.next[level].getReference();
					succ = curr.next[level].get(marked);
				}
				if (curr.key.compareTo(key) < 0) {
					pred = curr;
					curr = succ;
				} else {
					break;
				}
			}
		}
		if (curr.key.equals(key)) {
			return curr.value;
		} else {
			return null;
		}
	}

}


