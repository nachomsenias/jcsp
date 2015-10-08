package jcsp.algo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jcsp.CSPProblem;
import jcsp.CSPSolution;
import jcsp.experiment.beans.GRASPBean;
import jcsp.localsearch.FirstImprovement;
import jcsp.localsearch.LocalSearch;
import jcsp.util.ProgressSearchListener;

import org.jamesframework.core.search.SingleNeighbourhoodSearch;
import org.jamesframework.core.search.neigh.Neighbourhood;
import org.jamesframework.core.search.stopcriteria.MaxSteps;

import util.Functions;
import util.random.Randomizer;

public class GRASP {

	//Problem
	private CSPProblem csp;
	
	//GRASP parameters
	private int iterations;
	private double alpha;
	private long maxSteps;

	//Neighbourhoods
	private List<Neighbourhood<CSPSolution>> neighbourhoods;
	private boolean random = false;
	private boolean once = false;
	
	//Local search
	private LocalSearch localSearch;
	
	//Output
	private CSPSolution bestFound = null;
	private double bestFitness = Double.MAX_VALUE;
	
	//Time printing
	private boolean verbose;
	
	
	private class GRASPResult {
		final CSPSolution solution;
		final double fitness;
		
		public GRASPResult(CSPSolution solution, double fitness) {
			this.solution = solution;
			this.fitness = fitness;
		}
	}
	
	public GRASP() {
		
	}
	
	public GRASP(CSPProblem csp, int iterations, double alpha, long maxSteps,
			List<Neighbourhood<CSPSolution>> neighbourhoods, boolean verbose) {
		super();
		this.csp = csp;
		this.iterations = iterations;
		this.alpha = alpha;
		this.maxSteps = maxSteps;
		
		this.neighbourhoods = neighbourhoods;
		localSearch = new FirstImprovement(null);
		
		this.verbose = verbose;
	}
	
	public GRASP(CSPProblem csp, GRASPBean bean, boolean verbose) {
		super();
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
		    	
		    GRASPResult result=null;
		    CSPSolution solutionCopy=(CSPSolution)best.copy();
		    Neighbourhood<CSPSolution> neighbourhood;
		    
		    //If only one random operator is
		    if(once) {
		    	neighbourhood = neighbourhoods.get(
		    			random.nextInt(neighbourhoods.size()));
		    	
		    	result = runLocalSearch(neighbourhood, solutionCopy);
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
		    			result = runLocalSearch(neighbourhood, solutionCopy);
		    			
		    			solutionCopy = result.solution;
		    		}
		    	} else {
		    		for (Neighbourhood<CSPSolution> n: neighbourhoods) {
		    			result = runLocalSearch(n, solutionCopy);
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
	
	private GRASPResult runLocalSearch(Neighbourhood<CSPSolution> neighbourhood, CSPSolution solutionCopy) {
    	
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
	    
	    GRASPResult result = new GRASPResult(improved, improvedFitness);

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
}
