package jcsp.move;

import jcsp.CSPSolution;

import org.jamesframework.core.search.neigh.Move;

public class SingleInsertion implements Move<CSPSolution>{

	public final int pos;

	public SingleInsertion(int pos) {
		this.pos=pos;
	}
	
	/**
	 * Applying this operator involves overwriting the last cell at the array. 
	 * In order to enable undoing, last element should be stored.
	 */
	public void apply(CSPSolution sol) {
		sol.insert(pos);
	}

	/**
	 * Restoring original values is possible because last element was stored. 
	 * An inverse insertion is performed for restoring original last element.
	 */
	public void undo(CSPSolution sol) {
		int [] sequence = sol.getSequence();
		int last = sequence[pos];
		for (int i=pos; i< sequence.length-1; i++) {
			sequence[i]=sequence[i+1]; 
		}
		sequence[sequence.length-1]=last;
	}
}
