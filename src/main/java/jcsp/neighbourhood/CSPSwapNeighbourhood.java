package jcsp.neighbourhood;

import java.util.ArrayList;
import java.util.List;

import jcsp.CSPProblem;
import jcsp.CSPSolution;
import jcsp.move.SingleSwap;

import org.jamesframework.core.search.neigh.Neighbourhood;

import util.random.Randomizer;

public class CSPSwapNeighbourhood implements Neighbourhood<CSPSolution>{

	public List<SingleSwap> getAllMoves(CSPSolution sol) {

		List<SingleSwap> allSwaps = new ArrayList<SingleSwap>();
		
		int[] sequence = sol.getSequence();
		int demand = sequence.length;

		for (int firstIndex=0; firstIndex<demand; firstIndex++) {
			List<Integer> indexes = CSPNeighbourhodd.getValues(firstIndex, sequence);
			for (int secondIndex: indexes) {
				allSwaps.add(new SingleSwap(firstIndex, secondIndex));
			}
		}
		
		return allSwaps;
	}

	public SingleSwap getRandomMove(CSPSolution sol) {
		Randomizer random = CSPProblem.random;
		int sequenceLenght = sol.getSecuenceLength();
		
		int firstIndex = -1;
		int secondIndex = -1;
		
		do {
			firstIndex = random.nextInt(sequenceLenght);
			secondIndex = random.nextInt(sequenceLenght);
		} while(firstIndex == secondIndex);
		
		return new SingleSwap(firstIndex, secondIndex);
	}
}
