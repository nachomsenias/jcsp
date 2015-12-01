package jcsp.neighbourhood;

import java.util.ArrayList;
import java.util.List;

import jcsp.CSPProblem;
import jcsp.CSPSolution;
import jcsp.move.InvertSequence;

import org.jamesframework.core.search.neigh.Move;

public class CSPSmartInvertionNeighbourhood extends CSPNeighbourhood{

	@Override
	public Move<CSPSolution> getRandomMove(CSPSolution sol) {
		
		int[] conflicted = getConflicted(sol);
		CSPProblem csp = sol.getProblem();
		
		//Fist index is
		int randomConflictedIndex = csp.random.nextInt(conflicted.length);
		
		int firstIndex = conflicted[randomConflictedIndex];
		
		Move<CSPSolution> invertion = getNewInvertionMove(firstIndex,csp);
		return invertion;
	}
	
	private InvertSequence getNewInvertionMove(int firstIndex, CSPProblem csp) {
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
	public List<Move<CSPSolution>> getAllMoves(CSPSolution solution) {
		
		int[] conflicted = getConflicted(solution);
		CSPProblem csp = solution.getProblem();
		
		List<Move<CSPSolution>> allInvertions = new ArrayList<Move<CSPSolution>>();
		
		for (int firstIndex : conflicted) {
			
			Move<CSPSolution> invertion = getNewInvertionMove(firstIndex,csp);
			allInvertions.add(invertion);
		}

		return allInvertions;
	}
}
