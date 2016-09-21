package jcsp.experiment.beans;

import jcsp.CSPProblem;
import jcsp.algo.Algorithm;
import jcsp.algo.robustness.RobustVNS;

public class RobustVNSBean extends VNSBean {

	@Override
	public Algorithm createAlgorithmInstance(CSPProblem csp, boolean verbose) {
		return new RobustVNS(csp, this, verbose);
	}
}
