package jcsp.neighbourhood;

import java.util.ArrayList;
import java.util.List;

import jcsp.CSPProblem;
import jcsp.CSPSolution;
import jcsp.move.SingleInsertion;

import org.jamesframework.core.search.neigh.Neighbourhood;

import util.random.Randomizer;

public class CSPInsertionNeighbourhood implements Neighbourhood<CSPSolution>{

	public List<SingleInsertion> getAllMoves(CSPSolution sol) {
		List<SingleInsertion> allInsertions = new ArrayList<SingleInsertion>();
		
		int[] sequence = sol.getSequence();
		int demand = sequence.length;

		for (int oldPos=0; oldPos<demand; oldPos++) {
			List<Integer> indexes = CSPNeighbourhodd.getValues(oldPos, sequence);
			for (int newPos: indexes) {
				allInsertions.add(new SingleInsertion(oldPos, newPos));
			}
		}
		
		return allInsertions;
	}

	public SingleInsertion getRandomMove(CSPSolution sol) {
		Randomizer random = CSPProblem.random;
		
		List<SingleInsertion> moves = getAllMoves(sol);
		
		int numMoves = moves.size();
		
		return moves.get(random.nextInt(numMoves));
	}
}
