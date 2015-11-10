package jcsp.apps;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jamesframework.core.search.neigh.Neighbourhood;

import util.random.RandomizerFactory;
import util.random.RandomizerUtils;
import util.random.RandomizerFactory.RandomizerAlgorithm;
import jcsp.CSPProblem;
import jcsp.CSPSolution;
import jcsp.algo.ACO;
import jcsp.localsearch.FirstImprovement;
import jcsp.localsearch.LocalSearch;
import jcsp.neighbourhood.CSPInvertionNeighbourhood;
import jcsp.neighbourhood.CSPSwapNeighbourhood;
import jcsp.util.CSPParser;

public class SimpleACOApp {

	public static void main(String[] args) throws IOException {
//		String exampleFile="../xCSP/instances/test_10_cars.txt";
		String exampleFile="../xCSP/instances/90/pb_90-05.txt";
//		String exampleFile="../xCSP/instances/classical/p41_66.txt";

		CSPProblem csp = CSPParser.load(exampleFile);
		boolean verbose = false;
		
		//ACO Parameters
		int ants = 15;
//		int ants = 3;
		int maxCycles = 1000;
//		int maxCycles = 50;
		
		double alpha = 4;
		double beta = 6;
		double delta = 3;
		
		double q0 = 0.9;
		double tau0 = 0.005;
		double localRho = 0.99;
		double globalRho = 0.99;
		
		long maxSteps = 2000;
		
		List<Neighbourhood<CSPSolution>> localNeighbourhood = new ArrayList<Neighbourhood<CSPSolution>>();
		localNeighbourhood.add(new CSPSwapNeighbourhood());
//		localNeighbourhood.add(new CSPInvertionNeighbourhood());
		
		LocalSearch localSearch = new FirstImprovement(localNeighbourhood);
		
		List<Neighbourhood<CSPSolution>> globalNeighbourhood = new ArrayList<Neighbourhood<CSPSolution>>();
		globalNeighbourhood.add(new CSPInvertionNeighbourhood());
		
		LocalSearch overAllSearch = new FirstImprovement(globalNeighbourhood);
		
		System.out.println("Starting ACO experiment with file: " + exampleFile);

		for (int seedIndex = 0; seedIndex<30; seedIndex++) { 
	        
        	System.out.println("Seed: " + seedIndex);
        	
        	CSPProblem.random = RandomizerFactory.createRandomizer(
		    		RandomizerAlgorithm.XOR_SHIFT_128_PLUS_FAST, 
		    		RandomizerUtils.PRIME_SEEDS[seedIndex]
			);
        	
        	ACO aco = new ACO(csp, ants, maxCycles, alpha, beta, delta, q0, 
        			tau0, localRho, globalRho, localSearch, overAllSearch, 
        			maxSteps);
        	aco.setVerbose(verbose);
        	aco.optimize();
        	
        	System.out.println("Final fitness: "+aco.getFinalFitness());
        	System.out.println("Final sequence: "+Arrays.toString(
        			aco.getBest().getSequence()));
		}
	}

}
