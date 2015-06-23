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
		sol.insert(newPos, oldPos);
	}

	@Override
	public String toString() {
		return "SingleInsertion [newPos=" + newPos + ", oldPos=" + oldPos + "]";
	}
}
