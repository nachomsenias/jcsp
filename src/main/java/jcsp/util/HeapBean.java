package jcsp.util;

import java.util.Comparator;

public class HeapBean implements Comparable<HeapBean>{

	public final double heuristicValue;
	
	public final int carClass;

	public HeapBean(double heuristic, int carClass) {
		super();
		this.heuristicValue = heuristic;
		this.carClass = carClass;
	}

	@Override
	public int compareTo(HeapBean bean) {
		if (bean.heuristicValue==heuristicValue) {
			return 0;
		} else if (bean.heuristicValue>heuristicValue) {
			return -1;
		} else return 1;
	}
	
	public static Comparator<HeapBean> beanComparator() {
		return new Comparator<HeapBean>() {
			@Override
			public int compare(HeapBean b1, HeapBean b2) {
				return b1.compareTo(b2);
			}
		};
	}

	@Override
	public String toString() {
		return "HeapBean [heuristic=" + heuristicValue + ", car=" + carClass + "]";
	}
}
