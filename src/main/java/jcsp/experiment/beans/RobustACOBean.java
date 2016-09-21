package jcsp.experiment.beans;

import jcsp.CSPProblem;
import jcsp.algo.Algorithm;
import jcsp.algo.robustness.RobustACO;

public class RobustACOBean extends ACOBean {

	@Override
	public Algorithm createAlgorithmInstance(CSPProblem csp, boolean verbose) {
		return new RobustACO(csp, this, verbose);
	}
}
