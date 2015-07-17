package jcsp.neighbourhood;

import java.util.ArrayList;
import java.util.List;

import jcsp.CSPProblem;
import jcsp.CSPSolution;
import jcsp.move.SingleSwap;

import org.jamesframework.core.search.neigh.Move;

import util.random.Randomizer;

public class CSPSwapNeighbourhood extends CSPNeighbourhood{

	@Override
	public List<Move<CSPSolution>> getAllMoves(CSPSolution sol) {

		List<Move<CSPSolution>> allSwaps = new ArrayList<Move<CSPSolution>>();
		
		int[] sequence = sol.getSequence();
		int demand = sequence.length;

		for (int firstIndex=0; firstIndex<demand; firstIndex++) {
			List<Integer> indexes = getValues(firstIndex, sequence,1);
			for (int secondIndex: indexes) {
				allSwaps.add(new SingleSwap(firstIndex, secondIndex));
			}
		}
		
		return allSwaps;
	}
	
	//TODO This method is meant to try "Brute" strategies.
	@Override
	public Move<CSPSolution> getRandomMove(CSPSolution sol) {
		Randomizer random = CSPProblem.random;
		int sequenceLenght = sol.getSecuenceLength();
		
		int[] sequence = sol.getSequence();
		
		int firstIndex = -1;
		int secondIndex = -1;
		
		do {
			firstIndex = random.nextInt(sequenceLenght);
			secondIndex = random.nextInt(sequenceLenght);
		} while(firstIndex == secondIndex || 
				sequence[firstIndex] == sequence[secondIndex]);
		
		return new SingleSwap(firstIndex, secondIndex);
	}
}
