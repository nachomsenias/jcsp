package jcsp.neighbourhood;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import jcsp.CSPSolution;

import org.jamesframework.core.search.neigh.Move;
import org.jamesframework.core.search.neigh.Neighbourhood;

public abstract class CSPNeighbourhood implements Neighbourhood<CSPSolution>{
	
	protected Queue<Move<CSPSolution>> moveRepository = new LinkedList<Move<CSPSolution>>();
	
	/**
	 * Returns indexes compatible with given pivot for using in 
	 * swaps and insertions.
	 * 
	 * (Notice package visibility.)
	 * @param pivot
	 * @param sequence
	 * @return
	 */
	protected List<Integer> getValues(int pivot, int[] sequence) {
		final int type = sequence[pivot];
		List<Integer> indexes = new ArrayList<Integer>();
		
		int index = pivot-1;
		boolean typeFound = false;
		//Bellow index
		while(index>=0 && !typeFound) {
			int value = sequence[index];
			if(value!=type) {
				indexes.add(index);
				index--;
			} else {
				typeFound=true;
			}
		}
		
		index = pivot+1;
		typeFound = false;
		//Bellow index
		while(index<sequence.length && !typeFound) {
			int value = sequence[index];
			if(value!=type) {
				indexes.add(index);
				index++;
			} else {
				typeFound=true;
			}
		}
		
		return indexes;
	}
	
	protected List<Integer> getIntervals(int begin, int[] sequence) {
		final int type = sequence[begin];
		List<Integer> indexes = new ArrayList<Integer>();
		
		//Invertible intervals should be longer than 2.
		int index = begin+2;
		
		if(begin+1<sequence.length && sequence[begin+1] == type) {
			//If two classes are repeated, empty indexes are return.
			return indexes;
		}
		
		boolean typeFound = false;
		//Bellow index
		while(index<sequence.length && !typeFound) {
			int value = sequence[index];
			if(value!=type) {
				indexes.add(index);
				index++;
			} else {
				typeFound=true;
			}
		}
		
		return indexes;
	}

	@Override
	public Move<CSPSolution> getRandomMove(CSPSolution sol) {
		
		if(moveRepository.isEmpty()) {
			List<Move<CSPSolution>> allSwaps = getAllMoves(sol);
			Collections.shuffle(allSwaps);
			
			moveRepository.addAll(allSwaps);
		}
		
		return moveRepository.remove();
	}

	@Override
	public abstract List<Move<CSPSolution>> getAllMoves(CSPSolution solution);
}
