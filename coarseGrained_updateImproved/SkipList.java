
import java.util.*;


public class SkipList
{

	public static final int MAXHEIGHT = 32;
  public SkipListEntry head;    // First element of the top level
  public SkipListEntry tail;    // Last element of the top level


  public int n; 		// number of entries in the Skip list

  public int h;       // Height
  public Random r;    // Coin toss

  public SkipList()     // Default constructor...
  { 
     SkipListEntry p1, p2;

     p1 = new SkipListEntry(SkipListEntry.negInf, null, 0);
     p2 = new SkipListEntry(SkipListEntry.posInf, null, 0);

     head = p1;
     tail = p2;
     
     for(int layer = 0; layer < maxHeight(); layer ++){
     	 p1.nexts[layer] = p2;
     }

     n = 0;
     h = 0;

     r = new Random();
  }
  
  int maxHeight(){
	  return MAXHEIGHT;
	}
	
	int randomLevel(int maxHeight){
		int i = 0;
		while(r.nextDouble() < 0.5){
			i ++;
		}
		return i;
	}
  
  int findNode(String k, SkipListEntry[] preds, SkipListEntry[] succs) {
  	int lFound = -1;
  	SkipListEntry pred = head;
  	for (int layer = maxHeight() - 1; layer >= 0; layer --){
  	  SkipListEntry curr = pred.nexts[layer];
  	  while(k.compareTo(curr.key) > 0) {
  	  	pred = curr;
  	  	curr = pred.nexts[layer];
  	  }
  	  if (lFound == -1 && k.equals(curr.key)){
  	  	lFound = layer;
  	  }
  	  preds[layer] = pred;
  	  succs[layer] = curr;
  	}
  	return lFound;
  }
  
  synchronized Integer get(String k) {
  	SkipListEntry[] preds = new SkipListEntry[maxHeight()];
		SkipListEntry[] succs = new SkipListEntry[maxHeight()];
		int lFound = findNode(k, preds, succs);
		
		if (lFound == -1)
			return null;
			
		if(lFound != -1) {
			SkipListEntry nodeFound = succs[lFound];
			if(r.nextDouble() < 0.1) {
				int topLayer = nodeFound.topLayer + 1;
				if(topLayer < MAXHEIGHT) {
					preds[topLayer].nexts[topLayer] = nodeFound;
					nodeFound.nexts[topLayer] = succs[topLayer];
					nodeFound.topLayer = topLayer;
				}
			}
			return nodeFound.value;
		}
		
		return null;
	}
  
  synchronized boolean add(String k, Integer value) {
  		int topLayer = randomLevel(maxHeight());
		SkipListEntry[] preds = new SkipListEntry[maxHeight()];
		SkipListEntry[] succs = new SkipListEntry[maxHeight()];
		
		int lFound = findNode(k, preds, succs);
		if(lFound != -1) {
			SkipListEntry nodeFound = succs[lFound];
			nodeFound.value = value;
			return true;
		}
		SkipListEntry pred, succ;
		boolean valid = true;
		for(int layer = 0; valid && (layer <= topLayer); layer++){
			pred = preds[layer];
			succ = succs[layer];
			valid = pred.nexts[layer] == succ;
		}
		
		SkipListEntry newNode = new SkipListEntry(k, value, topLayer);
		for(int layer = 0; layer <= topLayer; layer++){
			newNode.nexts[layer] = succs[layer];
			preds[layer].nexts[layer] = newNode;
		}
		return true;
	}
	
	synchronized boolean remove(String k) {
		SkipListEntry nodeToDelete = null;
		int topLayer = -1;
		SkipListEntry[] preds = new SkipListEntry[maxHeight()];
		SkipListEntry[] succs = new SkipListEntry[maxHeight()];
		
		int lFound = findNode(k, preds, succs);
		if (lFound != -1) {
			nodeToDelete = succs[lFound];
			topLayer = nodeToDelete.topLayer;				
			SkipListEntry pred, succ;
			boolean valid = true;
			for(int layer = 0; valid && (layer <= topLayer); layer ++){
				pred = preds[layer];
				succ = succs[layer];
				valid = pred.nexts[layer] == succ;
			}					
			for(int layer = topLayer; layer >= 0; layer--){
				preds[layer].nexts[layer] = nodeToDelete.nexts[layer];
			}
			return true;
		} else {
			return false;
		}
	}
					
} 
