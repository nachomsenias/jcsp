package jcsp.robust;

import java.util.Arrays;

import org.jamesframework.core.problems.Solution;

import jcsp.CSPProblem;
import jcsp.CSPSolution;

public class RobustCSPSolution extends CSPSolution {

	public RobustCSPSolution(CSPProblem csp, int[] sequence) {
		super(new int[0][0], csp, sequence);
		fitness = csp.evaluateRestrictions(sequence, sequence.length-1);
	}

	public void swap(int i, int j) {
		int tmp = sequence[i];
		sequence[i]=sequence[j];
		sequence[j]=tmp;
		
		fitness = csp.evaluateRestrictions(sequence, sequence.length-1);
	}
	
	@Override
	public Solution copy() {
		return new RobustCSPSolution(
				csp, Arrays.copyOf(sequence, sequence.length));
	}
}
