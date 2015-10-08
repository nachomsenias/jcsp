package jcsp.neighbourhood;

import java.util.ArrayList;
import java.util.List;

import jcsp.CSPSolution;
import jcsp.move.MultipleMoves;
import jcsp.move.SingleSwap;

import org.jamesframework.core.search.neigh.Move;

public class CSPRandomShakingSwap extends CSPSwapNeighbourhood{
	
	private int numShakes;

	public CSPRandomShakingSwap(int numShakes) {
		super();
		this.numShakes = numShakes;
	}

	@Override
	public List<Move<CSPSolution>> getAllMoves(CSPSolution solution) {
		return null;
	}
	
	@Override
	public Move<CSPSolution> getRandomMove(CSPSolution sol) {
		
		List<Move<CSPSolution>> swaps = new ArrayList<Move<CSPSolution>>();
		SingleSwap[] emptySwap = {};
		
		for (int shakes =0; shakes<numShakes; shakes++) {
			Move<CSPSolution> swap;
			do {
				swap = super.getRandomMove(sol);
			} while(swaps.contains(swap));
			swaps.add(swap);
		}
		return new MultipleMoves(swaps.toArray(emptySwap));
	}

}
