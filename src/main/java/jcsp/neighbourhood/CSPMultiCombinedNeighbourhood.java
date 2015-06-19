package jcsp.neighbourhood;

import java.util.ArrayList;
import java.util.List;

import jcsp.CSPProblem;
import jcsp.CSPSolution;
import jcsp.move.CombinedMovement;

import org.jamesframework.core.search.neigh.Move;
import org.jamesframework.core.search.neigh.Neighbourhood;

public class CSPMultiCombinedNeighbourhood implements Neighbourhood<CSPSolution>{

	private final int maxNumberOfMovements;

	public CSPMultiCombinedNeighbourhood(int maxNumberOfMovements) {
		super();
		this.maxNumberOfMovements = maxNumberOfMovements;
	}

	@Override
	public Move<CSPSolution> getRandomMove(CSPSolution solution) {
		List<Move<CSPSolution>> moves = getAllMoves(solution);
		int max = moves.size();
		return moves.get(CSPProblem.random.nextInt(max));
	}

	@Override
	public List<Move<CSPSolution>> getAllMoves(CSPSolution solution) {
		List<Move<CSPSolution>> allMoves = new ArrayList<Move<CSPSolution>>();
		for (int i=0; i<maxNumberOfMovements; i++) {
			allMoves.add(new CombinedMovement(i));
		}
		return allMoves;
	}
}
