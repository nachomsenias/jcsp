package jcsp.algo.beans;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jcsp.CSPSolution;
import jcsp.localsearch.BestImprovement;
import jcsp.localsearch.FirstImprovement;
import jcsp.localsearch.LocalSearch;
import jcsp.neighbourhood.CSPInsertionNeighbourhood;
import jcsp.neighbourhood.CSPInvertionNeighbourhood;
import jcsp.neighbourhood.CSPShuffleNeighbourhood;
import jcsp.neighbourhood.CSPSwapNeighbourhood;

import org.jamesframework.core.search.neigh.Neighbourhood;

import util.io.ConfigFileReader;

public class GRASPBean {

	//GRASP parameters
	final public int iterations;
	final public double alpha;
	final public long maxSteps;
	final public boolean random;
	final public boolean once;
	
	final public LocalSearch localSearch;

	public GRASPBean(int iterations, double alpha, long maxSteps,
			LocalSearch localSearch, boolean random, boolean once) {
		super();
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
		
		LocalSearch ls = createLocalSearch(localSearh, neighbourhood);

		return new GRASPBean(iterations, alpha, maxSteps, ls, random, once);
	}
	
	private static LocalSearch createLocalSearch(String localSearh, String[] neighbourhoods) {
		
		List<Neighbourhood<CSPSolution>> nh = new ArrayList<Neighbourhood<CSPSolution>>();
		
		if(neighbourhoods!=null) {
			for (String neighbourhood:neighbourhoods) {
				switch (neighbourhood) {
				case "swap":
					nh.add(new CSPSwapNeighbourhood());
					break;
				case "insertion":
					nh.add(new CSPInsertionNeighbourhood());
					break;
				case "inversion":
					nh.add(new CSPInvertionNeighbourhood());
					break;
				case "shuffle":
					nh.add(new CSPShuffleNeighbourhood());
					break;
				default:
					throw new IllegalArgumentException(
							"Illegal neighbourhood value: "+neighbourhood);
				}
			}
		}
		
		switch (localSearh) {
		case "first":
			return new FirstImprovement(nh);
			
		case "best":
			return new BestImprovement(nh);

		default:
			throw new IllegalArgumentException(
					"Illegal local search value: "+localSearh);
		}
	}
}
