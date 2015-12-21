package jcsp.neighbourhood;

import java.util.ArrayList;
import java.util.List;

import jcsp.CSPProblem;
import jcsp.CSPSolution;

import org.jamesframework.core.search.neigh.Move;

public abstract class CSPSmartNeighbourhood extends CSPNeighbourhood{
	
	@Override
	public Move<CSPSolution> getRandomMove(CSPSolution solution) {
		int[] conflicted = getConflicted(solution);
		
		int numConflicts = conflicted.length;
		
		if(numConflicts == 0) {
			return null;
		}
		
		CSPProblem csp = solution.getProblem();
		
		//Fist index is
		int randomConflictedIndex = csp.random.nextInt(numConflicts);
		
		int firstIndex = conflicted[randomConflictedIndex];
		
		Move<CSPSolution> invertion = getRandomSmartMove(firstIndex,csp);
		return invertion;
	}
	
	protected abstract Move<CSPSolution> getRandomSmartMove(int firstIndex, CSPProblem csp);
	
	protected abstract Move<CSPSolution> getMove(int firstIndex, int secondIndex);

	@Override
	public List<Move<CSPSolution>> getAllMoves(CSPSolution solution) {
		int[] conflicted = getConflicted(solution);
		CSPProblem csp = solution.getProblem();
		
		List<Move<CSPSolution>> allInvertions = new ArrayList<Move<CSPSolution>>();
		
		for (int firstIndex : conflicted) {
			
			for (int secondIndex = 0; secondIndex<csp.getCarsDemand(); secondIndex++) {
				if(firstIndex!=secondIndex) {
					Move<CSPSolution> invertion = getMove(firstIndex,secondIndex);
					allInvertions.add(invertion);
				}
			}
		}

		return allInvertions;
	}
}
