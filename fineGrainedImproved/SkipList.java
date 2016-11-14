package fineGrainedImproved;

/*
 * Copied from: http://www.mathcs.emory.edu/~cheung/Courses/323/Syllabus/Map/skip-list-impl.html
 * */

import java.util.*;


public class SkipList
{

	public static final int MAXHEIGHT = 32;
  public SkipListEntry head;    // First element of the top level
  public SkipListEntry tail;    // Last element of the top level


  public int n; 		// number of entries in the Skip list

  public int h;       // Height
  public Random r;    // Coin toss

  /* ----------------------------------------------
     Constructor: empty skiplist

                          null        null
                           ^           ^
                           |           |
     head --->  null <-- -inf <----> +inf --> null
                           |           |
                           v           v
                          null        null
     ---------------------------------------------- */
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
  
  /////////////////////////////////////////////////////////////////////////////
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
  
  Integer get(String k) {
  	SkipListEntry[] preds = new SkipListEntry[maxHeight()];
		SkipListEntry[] succs = new SkipListEntry[maxHeight()];
		int lFound = findNode(k, preds, succs);
		
		if (lFound == -1)
			return null;
			
		if(lFound != -1) {
			SkipListEntry nodeFound = succs[lFound];
			if (!nodeFound.marked) {
				while(!nodeFound.fullyLinked){}
				
				if(r.nextDouble() < 0.1) {
					int topLayer = nodeFound.topLayer + 1;
					if(topLayer < MAXHEIGHT) {
						preds[topLayer].nexts[topLayer] = nodeFound;
						nodeFound.nexts[topLayer] = succs[topLayer];
						nodeFound.topLayer = topLayer;
					}
				}			
				return nodeFound.value;
			} else {
				return null;
			}
		}
		
		return null;
	}
  
  boolean add(String k, Integer value) {
  	int topLayer = randomLevel(maxHeight());
		SkipListEntry[] preds = new SkipListEntry[maxHeight()];
		SkipListEntry[] succs = new SkipListEntry[maxHeight()];
		
		while(true) {
			int lFound = findNode(k, preds, succs);
			if(lFound != -1) {
				SkipListEntry nodeFound = succs[lFound];
				if (!nodeFound.marked) {
					while(!nodeFound.fullyLinked){}
					nodeFound.value = value; return true;
				}
				continue;
			}
			int highestLocked = -1;
			try {
				SkipListEntry pred, succ, prevPred;
				prevPred = null;
				boolean valid = true;
				for(int layer = 0; valid && (layer <= topLayer); layer++){
					//if(preds[0].topLayer < layer) break;
					pred = preds[layer];
					succ = succs[layer];
					if(pred != prevPred){
						pred.lock.lock();
						highestLocked = layer;
						prevPred = pred;
					}
					valid = !pred.marked && !succ.marked && pred.nexts[layer] == succ;
				}
				if(!valid) continue;
				
				SkipListEntry newNode = new SkipListEntry(k, value, topLayer);
				for(int layer = 0; layer <= topLayer; layer++){
					newNode.nexts[layer] = succs[layer];
					preds[layer].nexts[layer] = newNode;
				}
				
				newNode.fullyLinked = true;
				return true;
			}
			finally{
				if(highestLocked != -1){
					SkipListEntry pred, prevPred = null;
					for(int layer = highestLocked; layer >= 0; layer--){
						pred = preds[layer];
						if(pred != prevPred){
							pred.lock.unlock();
							prevPred = pred;
						}
					}
				}
			}
		}
	}
	
	boolean okToDelete(SkipListEntry candidate, int lFound) {
		return (candidate.fullyLinked
			&& candidate.topLayer == lFound
			&& !candidate.marked);
	}
	
	boolean remove(String k) {
		SkipListEntry nodeToDelete = null;
		boolean isMarked = false;
		int topLayer = -1;
		SkipListEntry[] preds = new SkipListEntry[maxHeight()];
		SkipListEntry[] succs = new SkipListEntry[maxHeight()];
		
		while(true) {
			int lFound = findNode(k, preds, succs);
			if (isMarked ||
				 (lFound != -1 && okToDelete(succs[lFound], lFound))) {
				
				if (!isMarked) {
					nodeToDelete = succs[lFound];
					topLayer = nodeToDelete.topLayer;
					nodeToDelete.lock.lock();
					if (nodeToDelete.marked) {
						nodeToDelete.lock.unlock();
						return false;
					}
					nodeToDelete.marked = true;
					isMarked = true;
				}
				int highestLocked = -1;
				try {
					SkipListEntry pred, succ, prevPred = null;
					boolean valid = true;
					for(int layer = 0; valid && (layer <= topLayer); layer ++){
						pred = preds[layer];
						succ = succs[layer];
						if (pred != prevPred) {
							pred.lock.lock();
							highestLocked = layer;
							prevPred = pred;
						}
						valid = !pred.marked && pred.nexts[layer] == succ;
					}
					if(!valid) continue;
					
					for(int layer = topLayer; layer >= 0; layer--){
						preds[layer].nexts[layer] = nodeToDelete.nexts[layer];
					}
					nodeToDelete.lock.unlock();
					return true;
				}
				finally{
					if(highestLocked != -1){
						SkipListEntry pred, prevPred = null;
						for(int layer = highestLocked; layer >= 0; layer--){
							pred = preds[layer];
							if(pred != prevPred){
								pred.lock.unlock();
								prevPred = pred;
							}
						}
					}
				}
			} else {
				return false;
			}
		}
	}
					
} 
