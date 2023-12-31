package jcsp.neighbourhood;

import jcsp.CSPProblem;
import jcsp.CSPSolution;
import jcsp.move.InvertSequence;

import org.jamesframework.core.search.neigh.Move;

public class CSPSmartInvertionNeighbourhood extends CSPSmartNeighbourhood{
	
	@Override
	protected Move<CSPSolution> getRandomSmartMove(int firstIndex, CSPProblem csp) {
		int demand = csp.getCarsDemand();
		int lastIndex;
		
		do {
			lastIndex = csp.random.nextInt(demand);
		} while(lastIndex == firstIndex);
		
		if (lastIndex<firstIndex) {
			int aux = firstIndex;			
			firstIndex = lastIndex;
			lastIndex = aux;
		}
		return new InvertSequence(firstIndex, lastIndex);
	}

	@Override
	protected Move<CSPSolution> getMove(int firstIndex, int secondIndex) {
		return new InvertSequence(firstIndex, secondIndex);
	}
}
