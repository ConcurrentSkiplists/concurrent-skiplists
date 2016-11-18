package StandardLibrary;

import java.util.*;
import java.util.concurrent.*;

public final class SkipList implements SkipListInterface.SkipListInterface {
	ConcurrentSkipListMap<String, Integer> map = new ConcurrentSkipListMap<>();
	public SkipList() {
	}

	public boolean add(String key, Integer value) {
		if(map.put(key, value) == null) {
			return false;
		} else {
			return true;
		}
	}

	public boolean remove(String key) {
		if(map.remove(key) == null) {
			return false;
		} else {
			return true;
		}
	}

	public Integer get(String key) {
		return map.get(key);
	}
}


