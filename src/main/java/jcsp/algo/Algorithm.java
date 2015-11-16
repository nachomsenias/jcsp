package jcsp.algo;

import java.util.Arrays;

import jcsp.CSPProblem;
import jcsp.CSPSolution;
import jcsp.experiment.beans.AlgorithmBean;
import jcsp.localsearch.LocalSearch;
import jcsp.util.ProgressSearchListener;

import org.jamesframework.core.search.SingleNeighbourhoodSearch;
import org.jamesframework.core.search.neigh.Neighbourhood;
import org.jamesframework.core.search.stopcriteria.MaxSteps;

public abstract class Algorithm {
	
	//Problem
	protected CSPProblem csp;
	
	//Constants
	public static final boolean PRINT_TIMES = true;
	
	//Local Search
	protected long maxSteps;
	
	//Output
	protected CSPSolution bestFound = null;
	protected double bestFitness = Double.MAX_VALUE;
	
	//Time printing
	protected boolean verbose;
	
	protected class Result {
		final CSPSolution solution;
		final double fitness;
		
		public Result(CSPSolution solution, double fitness) {
			this.solution = solution;
			this.fitness = fitness;
		}
	}
	
	public Algorithm(CSPProblem csp, AlgorithmBean bean, boolean verbose) {
		this.csp = csp;
		this.verbose = verbose;
	}
	
	public abstract void optimize();
	
	protected Result runLocalSearch(LocalSearch localSearch, 
			Neighbourhood<CSPSolution> neighbourhood, CSPSolution solutionCopy) {
    	
    	SingleNeighbourhoodSearch<CSPSolution> searchAlgo = localSearch.createLocalSearch(csp, neighbourhood);
    	
	    searchAlgo.addStopCriterion(new MaxSteps(maxSteps));
	    if(verbose) {
	    	searchAlgo.addSearchListener(new ProgressSearchListener());
	    }
	    
	    //Try to improve constructed solution.
	    searchAlgo.setCurrentSolution(solutionCopy);
	    
	    //Improvement phase
	    searchAlgo.start();
	    
	    CSPSolution improved = searchAlgo.getBestSolution();
	    
	    double improvedFitness = 
    			searchAlgo.getBestSolutionEvaluation().getValue();

	    Result result = new Result(improved, improvedFitness);

		// print results
	    if(verbose) {
	    	if(improved!=null) {
				System.out.println("Improved sequence: " + Arrays.toString(
						improved.getSequence()));
		        System.out.println("Improved sequence fitness: " + 
		        		improvedFitness);
	    	} else {
	    		System.out.println("No improving solution found...");
	    	}
	    }
	    // dispose
	    searchAlgo.dispose();
	    
	    return result;
	}

	public CSPSolution getBest() {
		return bestFound;
	}
	
	public double getFinalFitness() {
		return bestFitness;
	}
	
	public void setVerbose(boolean verbose) {
		this.verbose=verbose;
	}
}
