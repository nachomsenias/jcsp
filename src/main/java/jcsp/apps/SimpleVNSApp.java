package jcsp.apps;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import jcsp.CSPProblem;
import jcsp.CSPSolution;
import jcsp.neighbourhood.CSPInsertionNeighbourhood;
import jcsp.neighbourhood.CSPSwapNeighbourhood;
import jcsp.neighbourhood.CSPRandomShakingSwap;
import jcsp.util.CSPParser;
import jcsp.util.ProgressSearchListener;

import org.jamesframework.core.search.algo.vns.VariableNeighbourhoodSearch;
import org.jamesframework.core.search.neigh.Neighbourhood;
import org.jamesframework.core.search.stopcriteria.MaxRuntime;
import org.jamesframework.core.search.stopcriteria.MaxSteps;

import util.random.RandomizerFactory;
import util.random.RandomizerFactory.RandomizerAlgorithm;
import util.random.RandomizerUtils;

public class SimpleVNSApp {

	public static void main(String[] args) throws IOException {
//		String exampleFile="../xCSP/instances/test_10_cars.txt";
		String exampleFile="../xCSP/instances/classical/p41_66.txt";
        CSPProblem csp = CSPParser.load(exampleFile);
        long maxSteps = 100000;
        
        System.out.println("Starting VNS experiment with file: " + exampleFile);

        for (int seedIndex = 0; seedIndex<30; seedIndex++) { 
        
        	System.out.println("Seed: " + seedIndex);
        	
        	csp.random = RandomizerFactory.createRandomizer(
		    		RandomizerAlgorithm.XOR_SHIFT_128_PLUS_FAST, 
		    		RandomizerUtils.PRIME_SEEDS[seedIndex]
			); 
        	
        	List<Neighbourhood<CSPSolution>> shakers = 
        			new ArrayList<Neighbourhood<CSPSolution>>();
        	for (int i=1; i<10; i++) {
        		shakers.add(new CSPRandomShakingSwap(i));
        	}
        	List<Neighbourhood<CSPSolution>> improvers = 
        			new ArrayList<Neighbourhood<CSPSolution>>();
        	improvers.add(new CSPSwapNeighbourhood());
        	improvers.add(new CSPInsertionNeighbourhood());
        	
        	VariableNeighbourhoodSearch<CSPSolution> vns = 
        			new VariableNeighbourhoodSearch<CSPSolution>(csp, shakers, improvers);
        	
        	vns.addStopCriterion(new MaxSteps(maxSteps));
        	vns.addStopCriterion(new MaxRuntime(600, TimeUnit.SECONDS));
        	vns.setCurrentSolution(csp.createRandomSolution());
        	vns.addSearchListener(new ProgressSearchListener());
        	vns.start();
        	
        	CSPSolution improved = vns.getBestSolution();
        	
        	double improvedFitness = 
        			vns.getBestSolutionEvaluation().getValue();
        	
        	System.out.println("Improved sequence: " + Arrays.toString(
					improved.getSequence()));
	        System.out.println("Improved sequence fitness: " + 
	        		improvedFitness);
        }
	}

}
