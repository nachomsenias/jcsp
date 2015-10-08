package jcsp.move;

import java.util.Arrays;

import jcsp.CSPSolution;

import org.apache.commons.lang3.ArrayUtils;
import org.jamesframework.core.search.neigh.Move;


public class MultipleMoves implements Move<CSPSolution>{

	private Move<CSPSolution>[] moves;
	
	private Move<CSPSolution>[] reversemoves;
	
	public MultipleMoves(Move<CSPSolution>[] moves) {
		this.moves = moves;
		reversemoves = Arrays.copyOf(moves, moves.length);
		ArrayUtils.reverse(reversemoves);
	}
	
	@Override
	public void apply(CSPSolution solution) {
		for (Move<CSPSolution> move : moves) {
			move.apply(solution);
		}
	}

	@Override
	public void undo(CSPSolution solution) {
		for (Move<CSPSolution> move : reversemoves) {
			move.undo(solution);
		}
	}

}
