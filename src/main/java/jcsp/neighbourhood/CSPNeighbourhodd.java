package jcsp.neighbourhood;

import java.util.ArrayList;
import java.util.List;

public class CSPNeighbourhodd {
	/**
	 * Notice package visibility.
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
}
