package jcsp.apps;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jamesframework.core.search.neigh.Neighbourhood;

import util.random.RandomizerFactory;
import util.random.RandomizerUtils;
import util.random.RandomizerFactory.RandomizerAlgorithm;
import jcsp.CSPProblem;
import jcsp.CSPSolution;
import jcsp.algo.GRASP;
import jcsp.neighbourhood.CSPInsertionNeighbourhood;
import jcsp.neighbourhood.CSPSwapNeighbourhood;
import jcsp.util.CSPParser;

public class SimpleGRASPApp {

	public static void main(String[] args) throws IOException {
		String exampleFile="../xCSP/instances/classical/p4_72.txt";
//		String exampleFile="../xCSP/instances/classical/p26_82.txt";
//		String exampleFile="../xCSP/instances/pb_100_01_4_72_feasible.txt";
//		String exampleFile="../xCSP/instances/pb_200_01.txt";
//		String exampleFile="../xCSP/instances/test_10_cars.txt";
//		String exampleFile="../xCSP/instances/test_12_cars.txt";
//		String exampleFile="../xCSP/instances/pb_400_05.txt";
		
        CSPProblem csp = CSPParser.load(exampleFile);
        
        System.out.println("Starting experiment with file: " + exampleFile);
        
        double alpha = 0.3;
        int iterations = 50;
        long maxSteps = 100000;
        boolean verbose = false;

        List<Neighbourhood<CSPSolution>> neighbourhoods = new ArrayList<Neighbourhood<CSPSolution>>();
        
        Neighbourhood<CSPSolution> swapNeighbourhood = new CSPSwapNeighbourhood();
        neighbourhoods.add(swapNeighbourhood);
        
        Neighbourhood<CSPSolution> insertNeighbourhood = new CSPInsertionNeighbourhood();
        neighbourhoods.add(insertNeighbourhood);
        
        for (int seedIndex = 0; seedIndex<30; seedIndex++) { 
        
        	System.out.println("Seed: " + seedIndex);
        	
        	CSPProblem.random = RandomizerFactory.createRandomizer(
		    		RandomizerAlgorithm.XOR_SHIFT_128_PLUS_FAST, RandomizerUtils.PRIME_SEEDS[seedIndex]
			); 
        	
        	GRASP grasp = new GRASP(csp, iterations, alpha, maxSteps, neighbourhoods, verbose);
        	grasp.optimize();
        	
        	CSPSolution best = grasp.getBest();
        	//Best solution should never be null because at least one solution must be returned.
        	System.out.println("GRASP SOLUTION::");
        	System.out.println(best);
        	System.out.println("GRASP FINAL FITNESS::");
        	System.out.println(grasp.getFinalFitness());
        }
	}

}
