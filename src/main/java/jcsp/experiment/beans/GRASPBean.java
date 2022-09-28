package jcsp.experiment.beans;

import jcsp.CSPProblem;
import jcsp.algo.Algorithm;
import jcsp.algo.GRASP;
import jcsp.localsearch.BestImprovement;
import jcsp.localsearch.LocalSearch;
import jcsp.util.io.ConfigFileReader;

public class GRASPBean extends AlgorithmBean{

	//GRASP parameters
	public int iterations;
	public double alpha;
	public long maxSteps;
	public boolean random;
	public boolean once;
	
	public LocalSearch localSearch;
	
	public GRASPBean() {
		alpha = 0.15;
        iterations = 50;
        maxSteps = 100000;
        
        random = false;
        once = false;
        
        localSearch = new BestImprovement(null);
	}
	
	public GRASPBean(int iterations, double alpha, long maxSteps,
			boolean random, boolean once, LocalSearch localSearch) {
		super();
		this.iterations = iterations;
		this.alpha = alpha;
		this.maxSteps = maxSteps;
		this.random = random;
		this.once = once;
		this.localSearch = localSearch;
	}

	public void readConfigFile(ConfigFileReader reader) {

		iterations = reader.getParameterInteger("iterations");
		alpha = reader.getParameterDouble("alpha");
		maxSteps = reader.getParameterInteger("maxSteps");
				
		String localSearh = reader.getParameterString("localSearch");
		String[] neighbourhood = reader.getParameterStringArray("neighbourhood");
		
		random = false;
		try {
			random = reader.getParameterBoolean("random");
		}catch (NullPointerException e) {
			//Nothing is done, this config does not contain a random parameter
		}
		
		once = false;
		try {
			once=reader.getParameterBoolean("once");
		}catch (NullPointerException e) {
			//Nothing is done, this config does not contain a once parameter
		}
		
		localSearch = LocalSearch.createLocalSearch(localSearh, neighbourhood);
	}

	@Override
	public Algorithm createAlgorithmInstance(CSPProblem csp, boolean verbose) {
		return new GRASP(csp, this, verbose);
	}
}
