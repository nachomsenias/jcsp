package jcsp.algo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jcsp.CSPProblem;
import jcsp.CSPSolution;
import jcsp.algo.beans.GRASPBean;
import jcsp.localsearch.FirstImprovement;
import jcsp.localsearch.LocalSearch;
import jcsp.util.ProgressSearchListener;

import org.jamesframework.core.search.SingleNeighbourhoodSearch;
import org.jamesframework.core.search.neigh.Neighbourhood;
import org.jamesframework.core.search.stopcriteria.MaxSteps;

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
	
	//Local search
	private LocalSearch localSearch;
	
	//Output
	private CSPSolution bestFound = null;
	private double bestFitness = Double.MAX_VALUE;
	
	//Time printing
	private boolean verbose;
	
	private boolean optimalFound = false;
	
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
		this.neighbourhoods = new ArrayList<Neighbourhood<CSPSolution>>();
		neighbourhoods.add(localSearch.getNeighbourhood());
		
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

		    int numNeighbourhoods = neighbourhoods.size();
		    Randomizer random = CSPProblem.random;
		    
		    int neighbourMax = (int)Math.pow(2, numNeighbourhoods);
		    int neighbourApplied = 0;
		    
		    while(!optimalFound && neighbourApplied<neighbourMax) {

//		    for (Neighbourhood<CSPSolution> neighbourhood: neighbourhoods) {
		    	
		    	Neighbourhood<CSPSolution> neighbourhood = neighbourhoods.get(
		    			random.nextInt(numNeighbourhoods));
		    	
		    	SingleNeighbourhoodSearch<CSPSolution> searchAlgo = localSearch.createLocalSearch(csp, neighbourhood);

//			    stocasticDescent.addStopCriterion(new MaxRuntime(timeLimit, TimeUnit.SECONDS));
			    searchAlgo.addStopCriterion(new MaxSteps(maxSteps));
			    if(verbose) {
			    	searchAlgo.addSearchListener(new ProgressSearchListener());
			    }
			    
			    //Try to improve constructed solution.
			    searchAlgo.setCurrentSolution((CSPSolution)best.copy());
			    
			    //Improvement phase
			    searchAlgo.start();
			    
			    CSPSolution improved = searchAlgo.getBestSolution();
			    
			    if(improved!=null) {
			    	best=improved;
			    	
			    	improvedFitness = 
			    			searchAlgo.getBestSolutionEvaluation().getValue();
		    		// print results
				    if(verbose) {
		    			System.out.println("Improved sequence: " + Arrays.toString(
		    					improved.getSequence()));
				        System.out.println("Improved sequence fitness: " + 
				        		improvedFitness);
				    }
			    } else if(verbose) {
			    	System.out.println("No improving solution found...");
			    }
			    // dispose
			    searchAlgo.dispose();
			    neighbourApplied++;
		    }
		    
	    	if(improvedFitness<bestFitness) {
	    		bestFitness=improvedFitness;
	    		bestFound=best;
	    		 // print results
			    if(verbose) {
	    			System.out.println("Improved sequence: " + Arrays.toString(
	    					best.getSequence()));
			        System.out.println("Improved sequence fitness: " + 
			        		improvedFitness);
			    }
	    	}
	    	if(improvedFitness==CSPProblem.FEASIBLE_FITNESS) {
	    		optimalFound=true;
	    		if(verbose) {
	    			System.out.println("Feasible solution found at iteration "
	    					+i+", ending process.");
	    		}
	    		
//	    		break;
	    	}
		    
		}
	}
	
	public CSPSolution getBest() {
		return bestFound;
	}
	
	public double getFinalFitness() {
		return bestFitness;
	}
}
