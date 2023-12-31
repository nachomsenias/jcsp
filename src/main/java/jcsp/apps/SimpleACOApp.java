package jcsp.apps;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import jcsp.CSPProblem;
import jcsp.CSPSolution;
import jcsp.algo.ACO;
import jcsp.experiment.beans.ACOBean;
import jcsp.localsearch.FirstImprovement;
import jcsp.localsearch.LocalSearch;
import jcsp.neighbourhood.CSPSwapNeighbourhood;
import jcsp.util.CSPParser;

import org.jamesframework.core.search.neigh.Neighbourhood;

import jcsp.util.random.RandomizerFactory;
import jcsp.util.random.RandomizerFactory.RandomizerAlgorithm;
import jcsp.util.random.RandomizerUtils;

public class SimpleACOApp {

	public static void main(String[] args) throws IOException {
//		String exampleFile="../xCSP/instances/test_10_cars.txt";
//		String exampleFile="../xCSP/instances/60/pb_60-05.txt";
//		String exampleFile="../xCSP/instances/90/pb_90-05.txt";
//		String exampleFile="../xCSP/instances/classical/p41_66.txt";
		String exampleFile="./instances/200/pb_200_01.txt";

		CSPProblem csp = CSPParser.load(exampleFile);
		boolean verbose = false;
		
		//ACO Parameters
		int ants = 15;
//		int ants = 3;
//		int ants = 8;
		int maxCycles = 1000;
//		int maxCycles = 50;
//		int maxCycles = 250;
		
//		double alpha = 4;
		double alpha = 4;
		double beta = 6;
		double delta = 3;
//		double beta = 1;
//		double delta = 1;
		
		double q0 = 0.9;
		double tau0 = 0.005;
		double localRho = 0.99;
		double globalRho = 0.99;
		
//		long maxSteps = 2000;
		
		List<Neighbourhood<CSPSolution>> localNeighbourhood = new ArrayList<Neighbourhood<CSPSolution>>();
		localNeighbourhood.add(new CSPSwapNeighbourhood());
//		localNeighbourhood.add(new CSPInvertionNeighbourhood());
		
		LocalSearch localSearch = new FirstImprovement(localNeighbourhood);
		
		List<Neighbourhood<CSPSolution>> globalNeighbourhood = new ArrayList<Neighbourhood<CSPSolution>>();
//		globalNeighbourhood.add(new CSPInvertionNeighbourhood());
		globalNeighbourhood.add(new CSPSwapNeighbourhood());
		
		LocalSearch overAllSearch = new FirstImprovement(globalNeighbourhood);
		
		System.out.println("Starting ACO experiment with file: " + exampleFile);
		
		final ACOBean bean = new ACOBean(ants, maxCycles, alpha, beta, delta, 
				q0, tau0, localRho, globalRho, localSearch, overAllSearch);

		for (int seedIndex = 0; seedIndex<30; seedIndex++) { 
	        System.out.println(new Date().toString());
        	System.out.println("Seed: " + seedIndex);
        	
        	csp.random = RandomizerFactory.createRandomizer(
		    		RandomizerAlgorithm.XOR_SHIFT_128_PLUS_FAST, 
		    		RandomizerUtils.PRIME_SEEDS[seedIndex]
			);
        	
        	ACO aco = new ACO(csp, bean, verbose);
        	aco.optimize();
        	
        	System.out.println("Final fitness: "+aco.getFinalFitness());
        	System.out.println("Final sequence: "+Arrays.toString(
        			aco.getBest().getSequence()));
		}
	}

}
