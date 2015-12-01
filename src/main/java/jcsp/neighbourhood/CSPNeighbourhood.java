package jcsp.neighbourhood;

import gnu.trove.list.array.TIntArrayList;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import jcsp.CSPProblem;
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
	protected List<Integer> getValues(int pivot, int[] sequence, int jump) {
		final int type = sequence[pivot];
		List<Integer> indexes = new ArrayList<Integer>();
		
		int index = pivot-jump;
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
		
		index = pivot+jump;
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
	
	protected int[] getConflicted(CSPSolution sol) {
		int[][] colissions = sol.getColissionMatrix();
		
		CSPProblem problem = sol.getProblem();
		int demand = problem.getCarsDemand();
		int options = problem.getNumOptions();
		
		TIntArrayList intList = new TIntArrayList(sol.getProblem().getCarsDemand());
		
		for (int pos = 0; pos<demand; pos++) {
			int o = 0;
			while(o<options) {
				if (colissions[pos][o]==0) {
					o++;
				} else {
					intList.add(pos);
					break;
				}
			}
		}
		int[] conflicted = intList.toArray();
		
		return conflicted;
	}

	@Override
	public abstract List<Move<CSPSolution>> getAllMoves(CSPSolution solution);
}
