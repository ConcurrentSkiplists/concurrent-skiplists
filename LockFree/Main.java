package LockFree;

import java.util.*;
import java.util.concurrent.*;

public class Main {

	public static void main(String[] args) throws InterruptedException {

		SkipList sl = new SkipList();

//		Thread t1 = new Thread(new SkipListBenchmarkGet(sl));
		Thread t1 = new Thread(new RandPutGetRemove(sl));
		Thread t2 = new Thread(new RandPutGetRemove(sl));
		Thread t3 = new Thread(new RandPutGetRemove(sl));

		t1.start();
		t2.start();
		t3.start();

		t1.join();
		t2.join();
		t3.join();

//		sl.printHorizontal();
	}

}

class SkipListTest2 implements Runnable {

	SkipList sl;

	public SkipListTest2(SkipList sl) {
		this.sl = sl;
	}

	@Override
	public void run() {
		sl.add("A", 2);
		sl.add("B", 12);
		sl.add("C", 42);

		sl.remove("B");

		System.out.println(sl.get("B"));
	}
}

class SkipListBenchmarkGet implements Runnable {

	SkipList sl;

	public SkipListBenchmarkGet(SkipList sl) {
		this.sl = sl;
	}

	@Override
	public void run() {
		Random r = new Random();

		int N = 1000;

		ArrayList<String> names = new ArrayList<>();
		for(int i = 0; i < N; i ++) {
			String name = Integer.toString(i);
			names.add(name);
			sl.add(name, i);
		}

		int numberOfTimes = 200;
		long totalTime = 0;
		for(int k = 0; k < numberOfTimes; k ++) {
			Collections.shuffle(names, new Random(System.nanoTime()));

			int commonIndex = r.nextInt(N);

			long time1 = System.nanoTime();
			for(int i = 0; i < N; i ++) {
				int rand = r.nextInt(2 * N);
				if(rand >= N) {
					sl.get(names.get(commonIndex));
				} else {
					sl.get(names.get(rand));
				}
			}
			long time2 = System.nanoTime();

			totalTime += (time2 - time1);
		}

		System.out.println("Average time taken: " + ((double)totalTime / numberOfTimes) + " ns");
	}
}

class RandPutGetRemove implements Runnable {

	SkipList sl;
	public boolean passed;

	public RandPutGetRemove(SkipList sl) {
		this.sl = sl;
		this.passed = true;
	}

	@Override
	public void run() {
		Random r = new Random();
		ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();

		int putN = r.nextInt(1000);
		int getN = r.nextInt(1000);
		int removeN = r.nextInt(1000);

		ArrayList<String> names = new ArrayList<>();
		for(int i = 0; i < putN; i ++) {
			String name = Integer.toString(i);
			names.add(name);
			sl.add(name, i);
			map.put(name, i);
		}

		for(int i = 0; i < removeN; i ++) {
			int rand = r.nextInt(putN);
			sl.remove(names.get(rand));
			map.remove(names.get(rand));
		}

		for(int i = 0; i < getN; i ++) {
			int rand = r.nextInt(putN);
			Integer a = sl.get(names.get(rand));
			Integer b = map.get(names.get(rand));
		}
	}
}
