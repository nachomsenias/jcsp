package jcsp.experiment.beans;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import jcsp.localsearch.BestImprovement;
import jcsp.localsearch.LocalSearch;
import util.io.ConfigFileReader;

public class GRASPBean {

	//GRASP parameters
	final public int iterations;
	public double alpha;
	final public long maxSteps;
	final public boolean random;
	final public boolean once;
	
	final public LocalSearch localSearch;

	public GRASPBean(int iterations, double alpha, long maxSteps,
			LocalSearch localSearch, boolean random, boolean once) {
		this.iterations = iterations;
		this.alpha = alpha;
		this.maxSteps = maxSteps;
		this.localSearch = localSearch;
		
		this.random=random;
		this.once=once;
	}
	
	public GRASPBean() {
		alpha = 0.4;
        iterations = 50;
        maxSteps = 100000;
        
        random = false;
        once = false;
        
        localSearch = new BestImprovement(null);
	}
	
	public static GRASPBean readConfigFile(File configFile) throws FileNotFoundException, IOException {

		ConfigFileReader reader = new ConfigFileReader();
		
		reader.readConfigFile(configFile);
		
		int iterations = reader.getParameterInteger("iterations");
		double alpha = reader.getParameterDouble("alpha");
		int maxSteps = reader.getParameterInteger("maxSteps");
				
		String localSearh = reader.getParameterString("localSearch");		
		String[] neighbourhood = reader.getParameterStringArray("neighbourhood");
		
		boolean random = false;
		try {
			random = reader.getParameterBoolean("random");
		}catch (NullPointerException e) {
			//Nothing is done, this config does not contain a random parameter
		}
		
		boolean once = false;
		try {
			once=reader.getParameterBoolean("once");
		}catch (NullPointerException e) {
			//Nothing is done, this config does not contain a once parameter
		}
		
		LocalSearch ls = LocalSearch.createLocalSearch(localSearh, neighbourhood);

		return new GRASPBean(iterations, alpha, maxSteps, ls, random, once);
	}
}
