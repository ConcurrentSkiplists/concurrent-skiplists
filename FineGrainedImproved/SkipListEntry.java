package FineGrainedImproved;

/*
 * Copied from: http://www.mathcs.emory.edu/~cheung/Courses/323/Syllabus/Map/skip-list-impl.html
 * */

import java.util.concurrent.locks.ReentrantLock;

public class SkipListEntry {
	public String key;
	public Integer value;

	public int topLayer;

	public int pos;      // I added this to print the skiplist "nicely"

	public SkipListEntry[] nexts;
	public boolean marked;
	public boolean fullyLinked;
	public ReentrantLock lock = new ReentrantLock();

	public static final String negInf = "! -oo";  // -inf key value
	public static final String posInf = "~ +oo";  // +inf key value

	public SkipListEntry(String k, Integer v) {
		key = k;
		value = v;
	}

	public SkipListEntry(String k, Integer v, int topLayer) {
		key = k;
		value = v;
		nexts = new SkipListEntry[SkipList.MAXHEIGHT];
		this.topLayer = topLayer;
	}

	public Integer getValue() {
		return value;
	}

	public String getKey() {
		return key;
	}

	public Integer setValue(Integer val) {
		Integer oldValue = value;
		value = val;
		return oldValue;
	}

	public boolean equals(Object o) {
		SkipListEntry ent;

		try {
			ent = (SkipListEntry) o;    // Test if o is a SkipListEntry...
		} catch (ClassCastException ex) {
			return false;
		}

		return (ent.getKey() == key) && (ent.getValue() == value);
	}

	public String toString() {
		return "(" + key + "," + value + ")";
	}
}
