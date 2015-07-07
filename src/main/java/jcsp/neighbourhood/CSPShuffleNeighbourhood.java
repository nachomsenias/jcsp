package jcsp.neighbourhood;

import java.util.ArrayList;
import java.util.List;

import jcsp.CSPProblem;
import jcsp.CSPSolution;
import jcsp.move.ShuffleSequence;

import org.jamesframework.core.search.neigh.Move;
import org.jamesframework.core.search.neigh.Neighbourhood;

import util.random.Randomizer;

public class CSPShuffleNeighbourhood implements Neighbourhood<CSPSolution>{

	@Override
	public Move<CSPSolution> getRandomMove(CSPSolution solution) {
		Randomizer random = CSPProblem.random;
		
		List<Move<CSPSolution>> moves = getAllMoves(solution);
		
		int numMoves = moves.size();
		
		return moves.get(random.nextInt(numMoves));
	}

	@Override
	public List<Move<CSPSolution>> getAllMoves(CSPSolution solution) {
		List<Move<CSPSolution>> allShuffles = new ArrayList<Move<CSPSolution>>();
		
		int[] sequence = solution.getSequence();
		int demand = sequence.length;

		for (int firstIndex=0; firstIndex<demand; firstIndex++) {
			List<Integer> indexes = CSPNeighbourhodd.getIntervals(firstIndex, sequence);
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
