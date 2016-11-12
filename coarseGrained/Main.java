
public class Main {

	public static void main(String[] args) throws InterruptedException {
		
		SkipList sl = new SkipList();
		
		Thread t1 = new Thread(sl);
		Thread t2 = new Thread(sl);
		Thread t3 = new Thread(sl);
		
		t1.start();
		t2.start();
		t3.start();
		
		t1.join();
		t2.join();
		t3.join();
		
		sl.printHorizontal();
	}

}
