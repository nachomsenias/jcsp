package jcsp.neighbourhood;

import java.util.ArrayList;
import java.util.List;

import jcsp.CSPProblem;
import jcsp.CSPSolution;
import jcsp.move.SingleInsertion;

import org.jamesframework.core.search.neigh.Move;

import util.random.Randomizer;

public class CSPInsertionNeighbourhood extends CSPNeighbourhood{

	@Override
	public List<Move<CSPSolution>> getAllMoves(CSPSolution sol) {
		List<Move<CSPSolution>> allInsertions = new ArrayList<Move<CSPSolution>>();
		
		int[] sequence = sol.getSequence();
		int demand = sequence.length;

		for (int oldPos=0; oldPos<demand; oldPos++) {
			List<Integer> indexes = getValues(oldPos, sequence,2);
			for (int newPos: indexes) {
				allInsertions.add(new SingleInsertion(oldPos, newPos));
			}
		}
		
		return allInsertions;
	}
	
	//TODO This method is meant to try "Brute" strategies.
	@Override
	public Move<CSPSolution> getRandomMove(CSPSolution sol) {
		Randomizer random = CSPProblem.random;
		int sequenceLenght = sol.getSecuenceLength();
		
		int[] sequence = sol.getSequence();
		
		int oldPos = random.nextInt(sequenceLenght);
		int newPos = random.nextInt(sequenceLenght);
		
		do {
			oldPos = random.nextInt(sequenceLenght);
			newPos = random.nextInt(sequenceLenght);
		}while(Math.abs(oldPos-newPos)>1
				|| sequence[oldPos]==sequence[newPos]);

//		List<Integer> indexes = getValues(oldPos, sequence,2);
		
//		if(indexes.isEmpty()) {
//			return null;
//		}
//		
//		int numPos = indexes.size();
//		int newPos = indexes.get(random.nextInt(numPos));
		
		return new SingleInsertion(oldPos, newPos);
	}
}
