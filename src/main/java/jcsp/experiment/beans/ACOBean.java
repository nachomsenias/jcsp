package jcsp.experiment.beans;

import jcsp.CSPProblem;
import jcsp.algo.ACO;
import jcsp.algo.Algorithm;
import jcsp.localsearch.LocalSearch;
import util.io.ConfigFileReader;

public class ACOBean extends AlgorithmBean{
	
	public int ants;
	public int maxCycles;
	
	public double alpha;
	public double beta;
	public double delta;
	
	public double q0;
	
	public double tau0;
	public double localRho;
	public double globalRho;

	public LocalSearch localSearch;

	public LocalSearch overAllSearch;
	
	public ACOBean() {}

	public ACOBean(int ants, int maxCycles, double alpha, double beta,
			double delta, double q0, double tau0, double localRho,
			double globalRho, LocalSearch localSearch, LocalSearch overAllSearch) {
		super();
		this.ants = ants;
		this.maxCycles = maxCycles;
		this.alpha = alpha;
		this.beta = beta;
		this.delta = delta;
		this.q0 = q0;
		this.tau0 = tau0;
		this.localRho = localRho;
		this.globalRho = globalRho;
		this.localSearch = localSearch;
		this.overAllSearch = overAllSearch;
	}

	@Override
	public void readConfigFile(ConfigFileReader reader){
		ants = reader.getParameterInteger("ants");
		maxCycles = reader.getParameterInteger("maxCycles");
		
		alpha = reader.getParameterDouble("alpha");
		beta = reader.getParameterDouble("beta");
		delta = reader.getParameterDouble("delta");
		
		q0 = reader.getParameterDouble("q0");
		tau0 = reader.getParameterDouble("tau0");
		localRho = reader.getParameterDouble("localRho");
		globalRho = reader.getParameterDouble("globalRho");
		
		try {
			String localSearh = reader.getParameterString("localSearch");
			String[] neighbourhood = reader.getParameterStringArray("neighbourhood");
			
			localSearch = LocalSearch.createLocalSearch(localSearh, neighbourhood);
		}catch (NullPointerException e) {
			//If any LS parameter remains unspecified, this value is set to null.
			localSearch = null;
		}
		
		try {
			String localSearh = reader.getParameterString("overAllSearch");
			String[] neighbourhood = reader.getParameterStringArray("overAllNeighbourhood");
			
			overAllSearch = LocalSearch.createLocalSearch(localSearh, neighbourhood);
		}catch (NullPointerException e) {
			//If any LS parameter remains unspecified for final search, this value is set to null.
			overAllSearch = null;
		}
	}

	@Override
	public Algorithm createAlgorithmInstance(CSPProblem csp, boolean verbose) {
		return new ACO(csp, this, verbose);
	}

}
