package jcsp.algo;

import org.jamesframework.core.search.neigh.Neighbourhood;

import jcsp.CSPProblem;
import jcsp.CSPSolution;
import jcsp.experiment.beans.AlgorithmBean;
import jcsp.experiment.beans.VNSBean;
import jcsp.localsearch.LocalSearch;
import jcsp.move.MultipleMoves;

public class CustomVNS extends VNS {
	
	protected int maxShakings;
	
	protected LocalSearch ls;

	public CustomVNS(CSPProblem csp, AlgorithmBean algBean, boolean verbose) {
		super(csp, algBean, verbose);
		VNSBean bean = (VNSBean) algBean;
		maxShakings = bean.maxShakings;
		
		ls = bean.localSearch;
		
	}

	@Override
	public void optimize() {
		
		int iteration = 0;
		CSPSolution initial=getInitialSolution();
		
		bestFound=(CSPSolution)initial.copy();
		bestFitness = initial.getFitness();
		
		while (iteration < maxShakings
				&& bestFitness!=CSPProblem.FEASIBLE_FITNESS) {
			
			Neighbourhood<CSPSolution> neighbour = shakers.get(iteration);
			
			MultipleMoves move = (MultipleMoves) neighbour.getRandomMove(initial);
			
			move.apply(initial);
			
			Result result = iterateLocalSearch(ls, initial);
			
			initial = result.solution;
			double improvedFitness = result.fitness;
			
			if(improvedFitness<bestFitness) {
	    		bestFitness=improvedFitness;
	    		bestFound=(CSPSolution) initial.copy();
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
			
			//Next neighbourhod
			iteration++;
		}
	}
}
