package jcsp.apps;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jcsp.CSPProblem;
import jcsp.CSPSolution;
import jcsp.neighbourhood.CSPInsertionNeighbourhood;
import jcsp.neighbourhood.CSPSwapNeighbourhood;
import jcsp.util.CSPParser;

import org.jamesframework.core.search.algo.vns.VariableNeighbourhoodSearch;
import org.jamesframework.core.search.neigh.Neighbourhood;

import util.random.RandomizerFactory;
import util.random.RandomizerFactory.RandomizerAlgorithm;
import util.random.RandomizerUtils;

public class SimpleVNSApp {

	public static void main(String[] args) throws IOException {
		String exampleFile="../xCSP/instances/test_10_cars.txt";
        
        CSPProblem csp = CSPParser.load(exampleFile);
        
        System.out.println("Starting experiment with file: " + exampleFile);
        
//        double alpha = 0.8;
//        int iterations = 50;
//        long timeLimit = 60;
//        
//        Neighbourhood<CSPSolution> neighbourhood = new CSPSwapNeighbourhood();
//        boolean verbose = false;
        
        for (int seedIndex = 0; seedIndex<30; seedIndex++) { 
        
        	System.out.println("Seed: " + seedIndex);
        	
        	CSPProblem.random = RandomizerFactory.createRandomizer(
		    		RandomizerAlgorithm.XOR_SHIFT_128_PLUS_FAST, RandomizerUtils.PRIME_SEEDS[seedIndex]
			); 
        	
        	List<Neighbourhood<CSPSolution>> shakers = 
        			new ArrayList<Neighbourhood<CSPSolution>>();
        	List<Neighbourhood<CSPSolution>> improvers = 
        			new ArrayList<Neighbourhood<CSPSolution>>();
        	improvers.add(new CSPSwapNeighbourhood());
        	improvers.add(new CSPInsertionNeighbourhood());
        	
        	VariableNeighbourhoodSearch<CSPSolution> vns = 
        			new VariableNeighbourhoodSearch<CSPSolution>(csp, shakers, improvers);
        	vns.setCurrentSolution(csp.createRandomSolution());
        	
        	
//        	CSPSolution best = grasp.getBest();
//        	//Best solution should never be null because at least one solution must be returned.
//        	System.out.println("GRASP SOLUTION::");
//        	System.out.println(best);
//        	System.out.println("GRASP FINAL FITNESS::");
//        	System.out.println(grasp.getFinalFitness());
        }
	}

}
