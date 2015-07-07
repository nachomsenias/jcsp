package jcsp.move;

import jcsp.CSPSolution;

import org.apache.commons.lang3.ArrayUtils;
import org.jamesframework.core.search.neigh.Move;

public class ShuffleSequence implements Move<CSPSolution>{

	public final int firstIndex, secondIndex;
	
	private int[] originalSubSequence;
	
	public ShuffleSequence(int i, int j) {
		if (i>=j) throw new IllegalArgumentException(
				"SHUFFLE ERROR: First index should be strictly lower than second index."
			);
		
		firstIndex = i;
		secondIndex = j;
	}
	
	
	@Override
	public void apply(CSPSolution solution) {
		// TODO Auto-generated method stub
		int [] sequence = solution.getSequence();
		
		originalSubSequence = ArrayUtils.subarray(sequence, firstIndex, secondIndex+1);
		
		solution.shuffle(firstIndex, secondIndex);
	}

	@Override
	public void undo(CSPSolution solution) {
		solution.restore(originalSubSequence, firstIndex, secondIndex);		
	}

	@Override
	public String toString() {
		return "ShuffleSequence [firstIndex=" + firstIndex + ", secondIndex="
				+ secondIndex + "]";
	}
}
