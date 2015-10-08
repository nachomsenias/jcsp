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
		
		int[] sequence = sol.getSequence();
		int sequenceLenght = sequence.length;
		
		int oldPos;
		int newPos;
		
		do {
			oldPos = random.nextInt(sequenceLenght);
			newPos = random.nextInt(sequenceLenght);
		}while(Math.abs(oldPos-newPos)>1
				|| sequence[oldPos]==sequence[newPos]);
		
		return new SingleInsertion(oldPos, newPos);
	}
}
