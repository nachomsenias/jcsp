package jcsp.apps;

import java.io.IOException;
import java.util.Arrays;

import jcsp.CSPProblem;
import jcsp.CSPSolution;
import jcsp.neighbourhood.CSPGreedyNeighbourhood;
import jcsp.util.CSPParser;
import jcsp.util.ProgressSearchListener;

import org.jamesframework.core.search.LocalSearch;
import org.jamesframework.core.search.algo.RandomDescent;

import util.random.RandomizerFactory;
import util.random.RandomizerFactory.RandomizerAlgorithm;
import util.random.RandomizerUtils;

public class SimpleGreedyApp {

	public static void main(String[] args) throws IOException {
		String exampleFile="../xCSP/instances/test_10_cars.txt";
        
        CSPProblem csp = CSPParser.load(exampleFile);
        
        System.out.println("Starting experiment with file: " + exampleFile);
        
        double alpha = 0.8;
        
        for (int seedIndex = 0; seedIndex<30; seedIndex++) { 
        
        	System.out.println("Iteration: " + seedIndex);
        	
		    csp.random = RandomizerFactory.createRandomizer(
		    		RandomizerAlgorithm.XOR_SHIFT_128_PLUS_FAST, RandomizerUtils.PRIME_SEEDS[seedIndex]
			); 
		    
		    // create random descent search with TSP neighbourhood
		    LocalSearch<CSPSolution> randomDescent = new RandomDescent<CSPSolution>(csp, new CSPGreedyNeighbourhood(csp,alpha));

		    // attach listener 
		    randomDescent.addSearchListener(new ProgressSearchListener());
		    
		    // IMPORTANT: start with empty clique
		    randomDescent.setCurrentSolution(csp.createEmptySolution());
		
		    // start search
		    randomDescent.start();
		
		    // print results
		    if(randomDescent.getBestSolution() != null){
		        System.out.println("Best sequence: " + Arrays.toString(
		        		randomDescent.getBestSolution().getSequence()));
		        System.out.println("Best sequence fitness: " + 
		        		randomDescent.getBestSolutionEvaluation());
		    } else {
		        System.out.println("No valid solution found...");
		    }
		
		    // dispose
		    randomDescent.dispose();
        }
	}

}
