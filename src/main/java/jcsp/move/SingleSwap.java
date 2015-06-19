package jcsp.move;

import jcsp.CSPSolution;

import org.jamesframework.core.search.neigh.Move;

public class SingleSwap implements Move<CSPSolution>{

	public final int firstIndex, secondIndex;
	
	public SingleSwap(int i, int j) {
		
		if (i==j) throw new IllegalArgumentException(
				"SWAP ERROR: Swapping positions need to be different"
			);
		
		firstIndex = i;
		secondIndex = j;
	}
	
	public void apply(CSPSolution sol) {
		sol.swap(firstIndex, secondIndex);
	}

	/**
	 * Undoing a swap consist in applying swap again.
	 */
	public void undo(CSPSolution sol) {
		apply(sol);
	}

	@Override
	public String toString() {
		return "SingleSwap [firstIndex=" + firstIndex + ", secondIndex="
				+ secondIndex + "]";
	}
}
