package jcsp.algo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jcsp.CSPProblem;
import jcsp.CSPSolution;
import jcsp.experiment.beans.GRASPBean;
import jcsp.localsearch.FirstImprovement;
import jcsp.localsearch.LocalSearch;

import org.jamesframework.core.search.neigh.Neighbourhood;

import util.Functions;
import util.random.Randomizer;

public class GRASP extends Algorithm{

	//GRASP parameters
	private int iterations;
	private double alpha;

	//Neighbourhoods
	private List<Neighbourhood<CSPSolution>> neighbourhoods;
	private boolean random = false;
	private boolean once = false;
	
	//Local search
	private LocalSearch localSearch;

	public GRASP(CSPProblem csp, int iterations, double alpha, long maxSteps,
			List<Neighbourhood<CSPSolution>> neighbourhoods, boolean verbose) {
		this.csp = csp;
		this.iterations = iterations;
		this.alpha = alpha;
		this.maxSteps = maxSteps;
		
		this.neighbourhoods = neighbourhoods;
		localSearch = new FirstImprovement(null);
		
		this.verbose = verbose;
	}
	
	public GRASP(CSPProblem csp, GRASPBean bean, boolean verbose) {
		this.csp = csp;
		this.iterations = bean.iterations;
		this.alpha = bean.alpha;
		this.maxSteps = bean.maxSteps;
		
		this.localSearch = bean.localSearch;
		this.random = bean.random;
		this.once=bean.once;
		
		neighbourhoods = new ArrayList<Neighbourhood<CSPSolution>>();
		neighbourhoods.addAll(localSearch.getNeighbourhoods());

		this.verbose = verbose;
	}
	
	private CSPSolution constructivePhase() {
		return csp.createGreedy(alpha);
	}
	
	public void optimize() {
		
		for (int i=0; i<iterations; i++) {
			if(verbose) {
				System.out.println("**Begining GRASP Iteration: "+i);
			}
			//Constructive phase: best initial solution
		    CSPSolution best = constructivePhase();		    
		    double improvedFitness = csp.evaluate(best).getValue();
		    
		    if(verbose) {
		    	System.out.println("Built sequence: " + Arrays.toString(
    					best.getSequence()));
		    	 System.out.println("Built sequence fitness: " + 
			        		improvedFitness);
		    }
		    Randomizer random = CSPProblem.random;
		    	
		    Result result=null;
		    CSPSolution solutionCopy=(CSPSolution)best.copy();
		    Neighbourhood<CSPSolution> neighbourhood;
		    
		    //If only one random operator is
		    if(once) {
		    	neighbourhood = neighbourhoods.get(
		    			random.nextInt(neighbourhoods.size()));
		    	
		    	result = runLocalSearch(localSearch, neighbourhood, solutionCopy);
		    } else {
		    	if(this.random) {
		    		//Neighbourhoods are applied at random.
		    		int numNeigbours = neighbourhoods.size();
		    		int[] indexes = new int[numNeigbours];
		    		for (byte j=0; j<numNeigbours; j++) {
		    			indexes[j]=j;
		    		}
		    		indexes = Functions.shuffleFast(numNeigbours, random);
		    		
		    		for (int index:indexes) {
		    			neighbourhood = neighbourhoods.get(index);
		    			result = runLocalSearch(localSearch, neighbourhood, solutionCopy);
		    			
		    			solutionCopy = result.solution;
		    		}
		    	} else {
		    		for (Neighbourhood<CSPSolution> n: neighbourhoods) {
		    			result = runLocalSearch(localSearch, n, solutionCopy);
		    			solutionCopy = result.solution;
			    	}
		    	}
		    	
		    }
		    
		    best = result.solution;
		    improvedFitness = result.fitness;
		    
	    	if(improvedFitness<bestFitness) {
	    		bestFitness=improvedFitness;
	    		bestFound=best;
	    		 // print results
			    if(verbose) {
	    			System.out.println("Final iteration sequence: " + Arrays.toString(
	    					best.getSequence()));
			        System.out.println("Final iteration fitness: " + 
			        		improvedFitness);
			    }
	    	}
	    	if(improvedFitness==CSPProblem.FEASIBLE_FITNESS) {
	    		if(verbose) {
	    			System.out.println("Feasible solution found at iteration "
	    					+i+", ending process.");
	    		}
	    	}
		    
		}
	}
	
	
}
