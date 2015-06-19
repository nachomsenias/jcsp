package jcsp.apps;

import java.io.IOException;

import org.jamesframework.core.search.neigh.Neighbourhood;

import util.random.RandomizerFactory;
import util.random.RandomizerUtils;
import util.random.RandomizerFactory.RandomizerAlgorithm;
import jcsp.CSPProblem;
import jcsp.CSPSolution;
import jcsp.algo.GRASP;
import jcsp.neighbourhood.CSPSwapNeighbourhood;
import jcsp.util.CSPParser;

public class SimpleGRASPApp {

	public static void main(String[] args) throws IOException {
		String exampleFile="../xCSP/instances/pb_400_05.txt";
        
        CSPProblem csp = CSPParser.load(exampleFile);
        
        System.out.println("Starting experiment with file: " + exampleFile);
        
        double alpha = 0.8;
        int iterations = 50;
        long maxSteps = 100000;
        
        Neighbourhood<CSPSolution> neighbourhood = new CSPSwapNeighbourhood();
        boolean verbose = true;
        
        for (int seedIndex = 0; seedIndex<30; seedIndex++) { 
        
        	System.out.println("Seed: " + seedIndex);
        	
        	CSPProblem.random = RandomizerFactory.createRandomizer(
		    		RandomizerAlgorithm.XOR_SHIFT_128_PLUS_FAST, RandomizerUtils.PRIME_SEEDS[seedIndex]
			); 
        	
        	GRASP grasp = new GRASP(csp, iterations, alpha, maxSteps, neighbourhood, verbose);
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
