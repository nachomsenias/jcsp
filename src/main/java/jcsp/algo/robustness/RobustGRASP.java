package jcsp.algo.robustness;

import java.util.Arrays;

import org.jamesframework.core.search.neigh.Neighbourhood;

import jcsp.CSPProblem;
import jcsp.CSPSolution;
import jcsp.algo.GRASP;
import jcsp.experiment.beans.AlgorithmBean;
import jcsp.util.random.Randomizer;

public class RobustGRASP extends GRASP {

	public RobustGRASP(CSPProblem csp, AlgorithmBean algBean, boolean verbose) {
		super(csp, algBean, verbose);
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
		    double feaseableFitness = result.fitness;
		    
		    double robustFitness = evaluator.evaluateRobustness(best).averagedMinRobustness;
		    
		    improvedFitness = omega*feaseableFitness + (1-omega)*robustFitness;
		    
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
