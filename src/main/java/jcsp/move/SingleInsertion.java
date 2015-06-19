package jcsp.move;

import jcsp.CSPSolution;

import org.jamesframework.core.search.neigh.Move;

public class SingleInsertion implements Move<CSPSolution>{

	public final int newPos;
	public final int oldPos;

	public SingleInsertion(int oldPos, int newPos) {
		this.newPos=newPos;
		this.oldPos=oldPos;
	}
	
	/**
	 * Applying this operator involves overwriting the last cell at the array. 
	 * In order to enable undoing, last element should be stored.
	 */
	public void apply(CSPSolution sol) {
		sol.insert(oldPos,newPos);
	}

	/**
	 * Restoring original values is possible because last element was stored. 
	 * An inverse insertion is performed for restoring original last element.
	 */
	public void undo(CSPSolution sol) {
//		int [] sequence = sol.getSequence();
//		
//		int type = sequence[newPos];
//		
//
//		if(newPos<oldPos) {
//			
//			//Move down
//			for (int i=oldPos; i<newPos; i++) {
//				sequence[i]=sequence[i+1];
//			}
//		} else {
//			//Move down
//			for (int i=oldPos; i>newPos; i--) {
//				sequence[i]=sequence[i-1];
//			}
//			
//		}
//		
//		sequence[oldPos]=type;
		sol.insert(newPos, oldPos);
//		int last = sequence[newPos];
//		for (int i=newPos; i< sequence.length-1; i++) {
//			sequence[i]=sequence[i+1]; 
//		}
//		sequence[sequence.length-1]=last;
	}
}
