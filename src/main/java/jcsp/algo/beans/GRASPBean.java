package jcsp.algo.beans;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

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
	
	final public LocalSearch localSearch;

	public GRASPBean(int iterations, double alpha, long maxSteps,
			LocalSearch localSearch) {
		super();
		this.iterations = iterations;
		this.alpha = alpha;
		this.maxSteps = maxSteps;
		this.localSearch = localSearch;
	}
	
	public GRASPBean() {
		alpha = 0.4;
        iterations = 50;
        maxSteps = 100000;
        
        localSearch = new BestImprovement(null);
	}
	
	public static GRASPBean readConfigFile(File configFile) throws FileNotFoundException, IOException {

		ConfigFileReader reader = new ConfigFileReader();
		
		reader.readConfigFile(configFile);
		
		int iterations = reader.getParameterInteger("iterations");
		double alpha = reader.getParameterDouble("alpha");
		int maxSteps = reader.getParameterInteger("maxSteps");
				
		String localSearh = reader.getParameterString("localSearch");
		String neighbourhood = reader.getParameterString("neighbourhood");
		
		LocalSearch ls = createLocalSearch(localSearh, neighbourhood);

		return new GRASPBean(iterations, alpha, maxSteps, ls);
	}
	
	private static LocalSearch createLocalSearch(String localSearh, String neighbourhood) {
		
		Neighbourhood<CSPSolution> nh = null;
		
		if(neighbourhood!=null) {
			switch (neighbourhood) {
			case "swap":
				nh = new CSPSwapNeighbourhood();
				break;
			case "insertion":
				nh = new CSPInsertionNeighbourhood();
				break;
			case "inversion":
				nh = new CSPInvertionNeighbourhood();
				break;
			case "shuffle":
				nh = new CSPShuffleNeighbourhood();
				break;
			default:
				throw new IllegalArgumentException(
						"Illegal neighbourhood value: "+neighbourhood);
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
