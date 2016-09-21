package jcsp.experiment.beans;

import jcsp.CSPProblem;
import jcsp.algo.Algorithm;
import jcsp.algo.robustness.RobustGRASP;

public class RobustGRASPBean extends GRASPBean {

	@Override
	public Algorithm createAlgorithmInstance(CSPProblem csp, boolean verbose) {
		return new RobustGRASP(csp, this, verbose);
	}

}
