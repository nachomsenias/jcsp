package jcsp.neighbourhood;

import java.util.ArrayList;
import java.util.List;

import jcsp.CSPProblem;
import jcsp.CSPSolution;
import jcsp.move.InvertSequence;

import org.jamesframework.core.search.neigh.Move;

import util.random.Randomizer;

public class CSPInvertionNeighbourhood extends CSPNeighbourhood{

	@Override
	public List<Move<CSPSolution>> getAllMoves(CSPSolution solution) {
		List<Move<CSPSolution>> allInvertions = new ArrayList<Move<CSPSolution>>();
		
		int[] sequence = solution.getSequence();
		int demand = sequence.length;

		for (int firstIndex=0; firstIndex<demand; firstIndex++) {
			List<Integer> indexes = getIntervals(firstIndex, sequence);
			for (int secondIndex: indexes) {
				allInvertions.add(new InvertSequence(firstIndex, secondIndex));
			}
		}
		
		return allInvertions;
	}
	
	public Move<CSPSolution> getRandomMove(CSPSolution sol) {
		Randomizer random = CSPProblem.random;
		int sequenceLenght = sol.getSecuenceLength();
		
		int[] sequence = sol.getSequence();
		
		int firstIndex = random.nextInt(sequenceLenght);

		List<Integer> indexes = getIntervals(firstIndex, sequence);
		
		if(indexes.isEmpty()) {
			return null;
		}
		
		int numIndexes = indexes.size();
		int lastIndex = indexes.get(random.nextInt(numIndexes));
		
		return new InvertSequence(firstIndex, lastIndex);
	}
}
