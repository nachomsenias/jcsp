package jcsp.neighbourhood;

import java.util.ArrayList;
import java.util.List;

import jcsp.CSPSolution;
import jcsp.move.ShuffleSequence;

import org.jamesframework.core.search.neigh.Move;

public class CSPShuffleNeighbourhood extends CSPNeighbourhood{

	@Override
	public List<Move<CSPSolution>> getAllMoves(CSPSolution solution) {
		List<Move<CSPSolution>> allShuffles = new ArrayList<Move<CSPSolution>>();
		
		int[] sequence = solution.getSequence();
		int demand = sequence.length;

		for (int firstIndex=0; firstIndex<demand; firstIndex++) {
			List<Integer> indexes = getIntervals(firstIndex, sequence);
			for (int secondIndex: indexes) {
				if(firstIndex<secondIndex) {
					allShuffles.add(new ShuffleSequence(firstIndex, secondIndex));
				} else {
					allShuffles.add(new ShuffleSequence(secondIndex, firstIndex));
				}
			}
		}
		
		return allShuffles;
	}
}
