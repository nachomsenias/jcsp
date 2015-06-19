package jcsp.move;

import jcsp.CSPSolution;

import org.jamesframework.core.search.neigh.Move;

public class AddCar implements Move<CSPSolution>{

	public final int classType;
	
	public AddCar(int classType) {
		this.classType = classType;
	}
	
	@Override
	public void apply(CSPSolution sol) {
		sol.addCar(classType);
	}

	@Override
	public void undo(CSPSolution sol) {
		sol.removeLast();
	}

	@Override
	public String toString() {
		return "AddCar [classType=" + classType + "]";
	}

}
