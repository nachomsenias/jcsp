package jcsp.apps;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.jamesframework.core.search.LocalSearch;
import org.jamesframework.core.search.algo.RandomDescent;
import org.jamesframework.core.search.algo.SteepestDescent;
import org.jamesframework.core.search.stopcriteria.MaxRuntime;

import util.random.RandomizerFactory;
import util.random.RandomizerUtils;
import util.random.RandomizerFactory.RandomizerAlgorithm;
import jcsp.CSPProblem;
import jcsp.CSPSolution;
import jcsp.neighbourhood.CSPGreedyNeighbourhood;
import jcsp.neighbourhood.CSPSwapNeighbourhood;
import jcsp.util.CSPParser;
import jcsp.util.ProgressSearchListener;

public class GreedySwapApp {

	public static void main(String[] args) throws IOException {
		String exampleFile="../xCSP/instances/test_10_cars.txt";
        
        CSPProblem csp = CSPParser.load(exampleFile);
        
        System.out.println("Starting experiment with file: " + exampleFile);
        
        double alpha = 0.8;
        
        for (int seedIndex = 0; seedIndex<30; seedIndex++) { 
        
        	System.out.println("Seed: " + seedIndex);
        	
		    csp.random = RandomizerFactory.createRandomizer(
		    		RandomizerAlgorithm.XOR_SHIFT_128_PLUS_FAST, RandomizerUtils.PRIME_SEEDS[seedIndex]
			); 
		    
		    // create random descent search with greedy neighbourhood
		    LocalSearch<CSPSolution> randomDescent = new RandomDescent<CSPSolution>(csp, new CSPGreedyNeighbourhood(csp,alpha));

		    // attach listener 
		    randomDescent.addSearchListener(new ProgressSearchListener());
		    
		    // IMPORTANT: start with empty sequence
		    randomDescent.setCurrentSolution(csp.createEmptySolution());
		
		    // start search
		    randomDescent.start();
		    
		    //Constructive phase: best initial solution
		    CSPSolution best = randomDescent.getBestSolution();
		    
		    System.out.println("Best sequence: " + Arrays.toString(
	        		best.getSequence()));
	        System.out.println("Best sequence fitness: " + 
	        		randomDescent.getBestSolutionEvaluation());
		    
		    LocalSearch<CSPSolution> stocasticDescent = new SteepestDescent<CSPSolution>(csp, new CSPSwapNeighbourhood());
		    // set maximum runtime
		    long timeLimit = 60;
		    stocasticDescent.addStopCriterion(new MaxRuntime(timeLimit, TimeUnit.SECONDS));
		    // attach listener (see example 1A)
		    stocasticDescent.addSearchListener(new ProgressSearchListener());
		    //Constructed solutions is improved.
		    stocasticDescent.setCurrentSolution(best);
		    
		    //Improvement phase
		    stocasticDescent.start();
		    
		    // print results
		    if(stocasticDescent.getBestSolution() != null){
		        System.out.println("Improved sequence: " + Arrays.toString(
		        		stocasticDescent.getBestSolution().getSequence()));
		        System.out.println("Improved sequence fitness: " + 
		        		stocasticDescent.getBestSolutionEvaluation());
		    } else {
		        System.out.println("No improved solution found...");
		    }
		
		    // dispose
		    randomDescent.dispose();
		    stocasticDescent.dispose();
        }
	}

}
