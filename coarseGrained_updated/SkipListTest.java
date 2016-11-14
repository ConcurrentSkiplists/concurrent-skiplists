//
//import java.util.*;
//import java.util.concurrent.*;
//
//public class SkipListTest {
//    @org.junit.Test
//    public void testSkipListSomething() throws Exception {
//        int got = 1;
//        int expected = 1;
//        assertEquals(expected, got);
//    }
//    
//    @org.junit.Test
//    public void testSingleRandPutAndGet() throws Exception {
//        SkipList sl = new SkipList();
//				
//				RandPutAndGet r1 = new RandPutAndGet(sl);
//				
//				Thread t1 = new Thread(r1);
//	
//				t1.start();
//	
//				t1.join();
//				
//				assertTrue(r1.passed);
//    }
//    
//    @org.junit.Test
//    public void testSingleRandPutGetRemove() throws Exception {
//        SkipList sl = new SkipList();
//				ConcurrentHashMap<String, Integer> removed = new ConcurrentHashMap<>();
//				
//				RandPutGetRemove r1 = new RandPutGetRemove(sl, removed);
//				Thread t1 = new Thread(r1);
//				t1.start();
//				t1.join();
//				
//				assertTrue(r1.passed);
//    }
//    
//    @org.junit.Test
//    public void testRandPutAndGet() throws Exception {
//        SkipList sl = new SkipList();
//				
//				RandPutAndGet r1 = new RandPutAndGet(sl);
//				RandPutAndGet r2 = new RandPutAndGet(sl);
//				RandPutAndGet r3 = new RandPutAndGet(sl);
//				
//				Thread t1 = new Thread(r1);
//				Thread t2 = new Thread(r2);
//				Thread t3 = new Thread(r3);
//	
//				t1.start();
//				t2.start();
//				t3.start();
//	
//				t1.join();
//				t2.join();
//				t3.join();
//				
//				assertTrue(r1.passed);
//				assertTrue(r2.passed);
//				assertTrue(r3.passed);
//    }
//    
//    @org.junit.Test
//    public void testRandPutGetRemove() throws Exception {
//        SkipList sl = new SkipList();
//        ConcurrentHashMap<String, Integer> removed = new ConcurrentHashMap<>();
//				
//				RandPutGetRemove r1 = new RandPutGetRemove(sl, removed);
//				RandPutGetRemove r2 = new RandPutGetRemove(sl, removed);
//				RandPutGetRemove r3 = new RandPutGetRemove(sl, removed);
//				
//				Thread t1 = new Thread(r1);
//				Thread t2 = new Thread(r2);
//				Thread t3 = new Thread(r3);
//	
//				t1.start();
//				t2.start();
//				t3.start();
//	
//				t1.join();
//				t2.join();
//				t3.join();
//				
//				assertTrue(r1.passed);
//				assertTrue(r2.passed);
//				assertTrue(r3.passed);
//    }
//    
//}
//
//class RandPutAndGet implements Runnable {
//
//	SkipList sl;
//	public boolean passed;
//	
//	public RandPutAndGet(SkipList sl){
//		this.sl = sl;
//		this.passed = true;
//	}
//	
//	@Override
//	public void run() {
//		Random r = new Random();
//		ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
//		
//		int putN = r.nextInt(1000);
//		int getN = r.nextInt(1000);
//		
//		ArrayList<String> names = new ArrayList<>();
//		for(int i = 0; i < putN; i ++) {
//			String name = Integer.toString(i);
//			names.add(name);
//			sl.add(name, i);
//			map.put(name, i);
//		}
//		
//		for(int i = 0; i < getN; i ++) {
//			int rand = r.nextInt(putN);
//			int a = sl.get(names.get(rand));
//			int b = map.get(names.get(rand));
//			
//			if(a != b) {
//				passed = false;
//				return;
//			}
//		}
//	}
//}
//
//class RandPutGetRemove implements Runnable {
//
//	SkipList sl;
//	public boolean passed;
//	ConcurrentHashMap<String, Integer> removed;
//	
//	public RandPutGetRemove(SkipList sl, ConcurrentHashMap<String, Integer> removed){
//		this.sl = sl;
//		this.passed = true;
//		this.removed = removed;
//	}
//	
//	@Override
//	public void run() {
//		Random r = new Random();
//		ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
//		
//		int putN = r.nextInt(1000);
//		int getN = r.nextInt(1000);
//		int removeN = r.nextInt(putN);
//		
//		ArrayList<String> names = new ArrayList<>();
//		for(int i = 0; i < putN; i ++) {
//			String name = Integer.toString(i);
//			names.add(name);
//			sl.add(name, i);
//			map.put(name, i);
//		}
//		
//		try{
//			Thread.sleep(1000);
//		} catch (Exception e){
//			e.printStackTrace();
//		}
//		
//		for(int i = 0; i < removeN; i ++) {
//			int rand = r.nextInt(putN);
//			sl.remove(names.get(rand));
//			map.remove(names.get(rand));
//			removed.put(names.get(rand), 0);
//		}
//		
//		for(int i = 0; i < getN; i ++) {
//			int rand = r.nextInt(putN);
//			Integer a = sl.get(names.get(rand));
//			Integer b = map.get(names.get(rand));
//			System.out.println(a + " " + b);
//			
//			
//			
//			if(a == null || b == null){
//				if(a == null && b == null){
//					continue;
//				}
//				else {
//					if(a == null) {
//						if (removed.containsKey(b.toString())) {
//							continue;
//						} else {
//							passed = false;
//							return;
//						}
//					}
//				}
//			}
//			
//			if(!a.equals(b)) {
//				passed = false;
//				return;
//			}
//		}
//	}
//}
