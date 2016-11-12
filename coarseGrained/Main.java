import java.util.*;

public class Main {

	public static void main(String[] args) throws InterruptedException {
		
		SkipList sl = new SkipList();
		
		Thread t1 = new Thread(new SkipListThread(sl));
		Thread t2 = new Thread(new SkipListThread(sl));
		Thread t3 = new Thread(new SkipListThread(sl));
		
		t1.start();
		t2.start();
		t3.start();
		
		t1.join();
		t2.join();
		t3.join();
		
		sl.printHorizontal();
	}

}

class SkipListThread implements Runnable {

	SkipList sl;
	
	public SkipListThread(SkipList sl){
		this.sl = sl;
	}
	
	@Override
	public void run() { // need to change
		sl.put("A", 1);
		sl.put("B", 2);
		sl.put("C", 3);
	}
}
