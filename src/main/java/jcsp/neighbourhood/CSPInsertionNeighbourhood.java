package jcsp.neighbourhood;

import java.util.ArrayList;
import java.util.List;

import jcsp.CSPSolution;
import jcsp.move.SingleInsertion;

import org.jamesframework.core.search.neigh.Move;

public class CSPInsertionNeighbourhood extends CSPNeighbourhood{

	@Override
	public List<Move<CSPSolution>> getAllMoves(CSPSolution sol) {
		List<Move<CSPSolution>> allInsertions = new ArrayList<Move<CSPSolution>>();
		
		int[] sequence = sol.getSequence();
		int demand = sequence.length;

		for (int oldPos=0; oldPos<demand; oldPos++) {
			List<Integer> indexes = getValues(oldPos, sequence);
			for (int newPos: indexes) {
				allInsertions.add(new SingleInsertion(oldPos, newPos));
			}
		}
		
		return allInsertions;
	}
}
