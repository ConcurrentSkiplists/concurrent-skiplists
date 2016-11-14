package coarseGrained;

import java.util.*;

public class Main {

	public static void main(String[] args) throws InterruptedException {
		
		SkipList sl = new SkipList();
		
//		Thread t1 = new Thread(new SkipListBenchmarkGet(sl));
		Thread t1 = new Thread(new SkipListTest(sl));
		Thread t2 = new Thread(new SkipListTest(sl));
		Thread t3 = new Thread(new SkipListTest(sl));
		
		t1.start();
		t2.start();
		t3.start();
		
		t1.join();
		t2.join();
		t3.join();
		
//		sl.printHorizontal();
	}

}

class SkipListTest implements Runnable {

	SkipList sl;
	
	public SkipListTest(SkipList sl){
		this.sl = sl;
	}
	
	@Override
	public void run() {
		Random r = new Random();
		
		int putN = r.nextInt(1000);
		int getN = r.nextInt(1000);
		
		ArrayList<String> names = new ArrayList<>();
		for(int i = 0; i < putN; i ++) {
			String name = Integer.toString(i);
			names.add(name);
			sl.put(name, i);
		}
		
		for(int i = 0; i < getN; i ++) {
			int rand = r.nextInt(putN);
			sl.get(names.get(rand));
		}
	}
}

class SkipListBenchmarkGet implements Runnable {

	SkipList sl;
	
	public SkipListBenchmarkGet(SkipList sl){
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
			sl.put(name, i);
		}
		
		int numberOfTimes = 200;
		long totalTime = 0;
		for(int k = 0; k < numberOfTimes; k ++){
			Collections.shuffle(names, new Random(System.nanoTime()));
		
			int commonIndex = r.nextInt(N);
		
			long time1 = System.nanoTime();
			for(int i = 0; i < N; i ++) {
				int rand = r.nextInt(2 * N);
				if(rand >= N){
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
