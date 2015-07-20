package jcsp.move;

import jcsp.CSPSolution;

import org.jamesframework.core.search.neigh.Move;

public class InvertSequence implements Move<CSPSolution>{
	
	public final int firstIndex, secondIndex;
	
	public InvertSequence(int i, int j) {
		if (i>=j) throw new IllegalArgumentException(
				"INVERTION ERROR: First index should be strictly lower than second index."  
			);
		
		firstIndex = i;
		secondIndex = j;
	}

	@Override
	public void apply(CSPSolution solution) {
		solution.invert(firstIndex, secondIndex);
	}

	@Override
	public void undo(CSPSolution solution) {
		solution.invert(firstIndex, secondIndex);
	}
	
	@Override
	public String toString() {
		return "InvertSequence [firstIndex=" + firstIndex + ", secondIndex="
				+ secondIndex + "]";
	}
}
