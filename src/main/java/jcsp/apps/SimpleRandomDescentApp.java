package jcsp.apps;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import jcsp.CSPProblem;
import jcsp.CSPSolution;
import jcsp.neighbourhood.CSPSwapNeighbourhood;
import jcsp.util.CSPParser;
import jcsp.util.ProgressSearchListener;

import org.jamesframework.core.search.LocalSearch;
import org.jamesframework.core.search.algo.RandomDescent;
import org.jamesframework.core.search.stopcriteria.MaxRuntime;

import util.random.RandomizerFactory;
import util.random.RandomizerFactory.RandomizerAlgorithm;
import util.random.RandomizerUtils;

/**
 * Main running stuff.
 *
 */
public class SimpleRandomDescentApp 
{
    public static void main( String[] args ) throws IOException
    {
        String exampleFile="../xCSP/instances/test_10_cars.txt";
        
        CSPProblem csp = CSPParser.load(exampleFile);
        
        System.out.println("Starting experiment with file: " + exampleFile);
        
        for (int seedIndex = 0; seedIndex<30; seedIndex++) { 
        
        	System.out.println("Iteration: " + seedIndex);
        	
		    csp.random = RandomizerFactory.createRandomizer(
		    		RandomizerAlgorithm.XOR_SHIFT_128_PLUS_FAST, RandomizerUtils.PRIME_SEEDS[seedIndex]
			); 
		    
		    // create random descent search with TSP neighbourhood
		    LocalSearch<CSPSolution> randomDescent = new RandomDescent<CSPSolution>(csp, new CSPSwapNeighbourhood());
		    // set maximum runtime
		    long timeLimit = 600;
		    randomDescent.addStopCriterion(new MaxRuntime(timeLimit, TimeUnit.SECONDS));
		    // attach listener (see example 1A)
		    randomDescent.addSearchListener(new ProgressSearchListener());
		
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
