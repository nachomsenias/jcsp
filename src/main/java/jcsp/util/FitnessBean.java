package jcsp.util;

import java.util.Comparator;

import jcsp.move.AddCar;

public class FitnessBean implements Comparable<FitnessBean>{

	public final double fitness;
	
	public final AddCar move;
	
	public FitnessBean(double fitness, AddCar move) {
		super();
		this.fitness = fitness;
		this.move = move;
	}
	
	@Override
	public int compareTo(FitnessBean bean) {
		if (bean.fitness==fitness) {
			return 0;
		} else if (bean.fitness>fitness) {
			return -1;
		} else return 1;
	}
	
	public static Comparator<FitnessBean> beanComparator() {
		return new Comparator<FitnessBean>() {
			@Override
			public int compare(FitnessBean b1, FitnessBean b2) {
				return b1.compareTo(b2);
			}
		};
	}

	@Override
	public String toString() {
		return "FitnessBean [fitness=" + fitness + ", move=" + move + "]";
	}
}
