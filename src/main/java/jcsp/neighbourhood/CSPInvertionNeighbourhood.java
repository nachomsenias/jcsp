package jcsp.neighbourhood;

import java.util.ArrayList;
import java.util.List;

import jcsp.CSPSolution;
import jcsp.move.InvertSequence;

import org.jamesframework.core.search.neigh.Move;

public class CSPInvertionNeighbourhood extends CSPNeighbourhood{

	@Override
	public List<Move<CSPSolution>> getAllMoves(CSPSolution solution) {
		List<Move<CSPSolution>> allInvertions = new ArrayList<Move<CSPSolution>>();
		
		int[] sequence = solution.getSequence();
		int demand = sequence.length;

		for (int firstIndex=0; firstIndex<demand; firstIndex++) {
			List<Integer> indexes = getIntervals(firstIndex, sequence);
			for (int secondIndex: indexes) {
				if(firstIndex<secondIndex) {
					allInvertions.add(new InvertSequence(firstIndex, secondIndex));
				} else {
					allInvertions.add(new InvertSequence(secondIndex, firstIndex));
				}
			}
		}
		
		return allInvertions;
	}
}
