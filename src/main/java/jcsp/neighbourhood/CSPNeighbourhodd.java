package jcsp.neighbourhood;

import java.util.ArrayList;
import java.util.List;

public class CSPNeighbourhodd {
	/**
	 * Returns indexes compatible with given pivot for using in 
	 * swaps and insertions.
	 * 
	 * (Notice package visibility.)
	 * @param pivot
	 * @param sequence
	 * @return
	 */
	static List<Integer> getValues(int pivot, int[] sequence) {
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
	
	static List<Integer> getIntervals(int begin, int[] sequence) {
		final int type = sequence[begin];
		List<Integer> indexes = new ArrayList<Integer>();
		
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
}
