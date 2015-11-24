package jcsp.algo;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.jamesframework.core.search.algo.vns.VariableNeighbourhoodSearch;
import org.jamesframework.core.search.neigh.Neighbourhood;
import org.jamesframework.core.search.stopcriteria.MaxRuntime;
import org.jamesframework.core.search.stopcriteria.MaxSteps;

import jcsp.CSPProblem;
import jcsp.CSPSolution;
import jcsp.experiment.beans.AlgorithmBean;
import jcsp.experiment.beans.VNSBean;
import jcsp.util.ProgressSearchListener;

public class VNS extends Algorithm{
	
	private final int maxSteps;
	private final int maxSeconds;
	
	private final boolean greedyInitialSolution;
	
	private final List<Neighbourhood<CSPSolution>> improvers;
	
	private final List<Neighbourhood<CSPSolution>> shakers;
	

	public VNS(CSPProblem csp, AlgorithmBean algBean, boolean verbose) {
		super(csp, algBean, verbose);
		VNSBean bean = (VNSBean) algBean;
		
		maxSeconds = bean.maxSeconds;
		maxSteps = bean.maxSteps;
		
		greedyInitialSolution = bean.greedyInitialSolution;
		
		improvers = bean.improvers;
		shakers = bean.shakers;
	}

	@Override
	public void optimize() {
		
		VariableNeighbourhoodSearch<CSPSolution> vns = 
    			new VariableNeighbourhoodSearch<CSPSolution>(csp, shakers, improvers);
		
		vns.addStopCriterion(new MaxSteps(maxSteps));
    	vns.addStopCriterion(new MaxRuntime(maxSeconds, TimeUnit.SECONDS));
    	
    	CSPSolution initial;
    	if(greedyInitialSolution) {
    		initial = csp.createHeuristic(0.0);
    		initial.fullEvaluation();
    	} else {
    		initial = csp.createRandomSolution();
    	}
    	vns.setCurrentSolution(initial);
    	
    	if(verbose) {
    		vns.addSearchListener(new ProgressSearchListener());
    	}
    	
    	vns.start();
    	
    	bestFound = vns.getBestSolution();
    	bestFitness = vns.getBestSolutionEvaluation().getValue();
    	
    	// Best solution should never be null because at least one solution
		// must be returned.
    	if(verbose) {
    		String message = "VNS SOLUTION::\n";
    		System.out.print(message);
    		
    		message = bestFound.toString() + "\n";
			System.out.print(message);
			
			message = "VNS FITNESS::\n";
			System.out.print(message);

			message = bestFitness + "\n";
			System.out.print(message);
    	}
	}
}
