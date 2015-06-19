package jcsp.algo;

import java.util.Arrays;

import jcsp.CSPProblem;
import jcsp.CSPSolution;
import jcsp.neighbourhood.CSPGreedyNeighbourhood;
import jcsp.util.ProgressSearchListener;

import org.jamesframework.core.search.LocalSearch;
import org.jamesframework.core.search.algo.RandomDescent;
import org.jamesframework.core.search.algo.SteepestDescent;
import org.jamesframework.core.search.neigh.Neighbourhood;
import org.jamesframework.core.search.stopcriteria.MaxSteps;

public class GRASP {

	//Problem
	private CSPProblem csp;
	
	//GRASP parameters
	private int iterations;
	private double alpha;
	private long maxSteps;

	//Local search
	private Neighbourhood<CSPSolution> neighbourhood;
	
	//Output
	private CSPSolution bestFound = null;
	private double bestFitness = Double.MAX_VALUE;
	
	//Time printing
	private boolean verbose;
	
	public GRASP(CSPProblem csp, int iterations, double alpha, long maxSteps,
			Neighbourhood<CSPSolution> neighbourhood, boolean verbose) {
		super();
		this.csp = csp;
		this.iterations = iterations;
		this.alpha = alpha;
		this.maxSteps = maxSteps;
		this.neighbourhood = neighbourhood;
		this.verbose = verbose;
	}

	private CSPSolution constructivePhase() {
		// create random descent search with greedy neighbourhood
	    LocalSearch<CSPSolution> randomDescent = 
	    		new RandomDescent<CSPSolution>(
	    				csp, 
	    				new CSPGreedyNeighbourhood(csp,alpha)
	    			);

	    // attach listener 
	    if(verbose) {
	    	randomDescent.addSearchListener(new ProgressSearchListener());
	    }
	    
	    // IMPORTANT: start with empty sequence
	    randomDescent.setCurrentSolution(csp.createEmptySolution());
	
	    // start search
	    randomDescent.start();
	    
	    //Constructive phase: best initial solution
	    CSPSolution best = randomDescent.getBestSolution();
	    
	    if(verbose) {
	    	System.out.println("Built sequence: " + Arrays.toString(
	        		best.getSequence()));
	        System.out.println("Built sequence fitness: " + 
	        		randomDescent.getBestSolutionEvaluation());
	    }
	    
	    randomDescent.dispose();
	    
	    return best;
	}
	
	public void optimize() {
		
		for (int i=0; i<iterations; i++) {
		
			//Constructive phase: best initial solution
		    CSPSolution best = constructivePhase();

		    SteepestDescent<CSPSolution> stocasticDescent = 
		    		new SteepestDescent<CSPSolution>(csp, neighbourhood);
		    // set maximum runtime
//		    stocasticDescent.addStopCriterion(new MaxRuntime(timeLimit, TimeUnit.SECONDS));
		    stocasticDescent.addStopCriterion(new MaxSteps(maxSteps));
		    if(verbose) {
		    	stocasticDescent.addSearchListener(new ProgressSearchListener());
		    }
		    
		    //Constructed solutions is improved.
		    stocasticDescent.setCurrentSolution(best);
		    
		    //Improvement phase
		    stocasticDescent.start();
		    
		    CSPSolution improved = stocasticDescent.getBestSolution();
		    
		    if(improved!=null) {
		    	
		    	double improvedFitness = stocasticDescent.getBestSolutionEvaluation().getValue();
		    	
		    	if(improvedFitness<bestFitness) {
		    		bestFitness=improvedFitness;
		    		bestFound=improved;
		    		 // print results
				    if(verbose) {
		    			System.out.println("Improved sequence: " + Arrays.toString(
		    					improved.getSequence()));
				        System.out.println("Improved sequence fitness: " + 
				        		improvedFitness);
				    }
		    	}
		    } else if(verbose) {
		    	System.out.println("No improving solution found...");
		    }
		
		    // dispose		    
		    stocasticDescent.dispose();
		}
	}
	
	public CSPSolution getBest() {
		return bestFound;
	}
	
	public double getFinalFitness() {
		return bestFitness;
	}
}
