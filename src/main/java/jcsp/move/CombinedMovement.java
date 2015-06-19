package jcsp.move;

import jcsp.CSPSolution;

import org.jamesframework.core.search.neigh.Move;

public class CombinedMovement implements Move<CSPSolution>{
	
	private final int maxNumberOfMovements;
	
	private Move<CSPSolution>[] moves;

	public CombinedMovement(int maxNumberOfMovements) {
		super();
		this.maxNumberOfMovements = maxNumberOfMovements;
	}

	@Override
	public void apply(CSPSolution solution) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void undo(CSPSolution solution) {
		// TODO Auto-generated method stub
		
	}
}
