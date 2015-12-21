package jcsp.neighbourhood;

import jcsp.CSPProblem;
import jcsp.CSPSolution;
import jcsp.move.SingleSwap;

import org.jamesframework.core.search.neigh.Move;

public class CSPSmartSwapNeighbourhood extends CSPSmartNeighbourhood{
	
	@Override
	protected Move<CSPSolution> getRandomSmartMove(int firstIndex, CSPProblem csp) {
		int demand = csp.getCarsDemand();
		int lastIndex;
		
		do {
			lastIndex = csp.random.nextInt(demand);
		} while(lastIndex == firstIndex);

		return new SingleSwap(firstIndex, lastIndex);
	}

	@Override
	protected Move<CSPSolution> getMove(int firstIndex, int secondIndex) {
		return new SingleSwap(firstIndex, secondIndex);
	}
}
