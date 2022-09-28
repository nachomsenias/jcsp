package jcsp.algo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jcsp.CSPProblem;
import jcsp.CSPSolution;
import jcsp.experiment.beans.AlgorithmBean;
import jcsp.experiment.beans.GRASPBean;
import jcsp.localsearch.LocalSearch;

import org.jamesframework.core.search.neigh.Neighbourhood;

import jcsp.util.random.Randomizer;

public class GRASP extends Algorithm{

	//GRASP parameters
	protected int iterations;
	protected double alpha;

	//Neighbourhoods
	protected List<Neighbourhood<CSPSolution>> neighbourhoods;
	protected boolean random = false;
	protected boolean once = false;
	
	//Local search
	protected LocalSearch localSearch;
	
	public GRASP(CSPProblem csp, AlgorithmBean algBean, boolean verbose) {
		super(csp,algBean,verbose);
		GRASPBean bean = (GRASPBean)algBean;
		
		this.iterations = bean.iterations;
		this.alpha = bean.alpha;
		this.maxSteps = bean.maxSteps;
		
		this.localSearch = bean.localSearch;
		this.random = bean.random;
		this.once=bean.once;
		
		neighbourhoods = new ArrayList<Neighbourhood<CSPSolution>>();
		neighbourhoods.addAll(localSearch.getNeighbourhoods());
	}
	
	protected CSPSolution constructivePhase() {
		CSPSolution greedy = csp.createHeuristic(alpha);
		greedy.fullEvaluation();
		
		return greedy;
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
		    Randomizer random = csp.random;
		    	
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
		    		result = iterateRandomizedLocalSearch(localSearch, solutionCopy);
		    	} else {
		    		result = iterateLocalSearch(localSearch, solutionCopy);
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
