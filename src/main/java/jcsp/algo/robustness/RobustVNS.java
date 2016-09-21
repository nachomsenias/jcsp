package jcsp.algo.robustness;

import org.jamesframework.core.search.neigh.Neighbourhood;

import jcsp.CSPProblem;
import jcsp.CSPSolution;
import jcsp.algo.CustomVNS;
import jcsp.experiment.beans.AlgorithmBean;
import jcsp.move.MultipleMoves;

public class RobustVNS extends CustomVNS {

	public RobustVNS(CSPProblem csp, AlgorithmBean algBean, boolean verbose) {
		super(csp, algBean, verbose);
	}
	
	@Override
	public void optimize() {
		
		int iteration = 0;
		CSPSolution initial=getInitialSolution();
		
		double feaseableFitness = initial.getFitness();
		double robustFitness = evaluator.evaluateRobustness(initial).averagedMinRobustness;
		
		bestFitness = omega*feaseableFitness + (1-omega)*robustFitness;
		bestFound = (CSPSolution) initial.copy();
		
		while (iteration < maxShakings
				&& bestFitness!=CSPProblem.FEASIBLE_FITNESS) {
			
			Neighbourhood<CSPSolution> neighbour = shakers.get(iteration);
			
			MultipleMoves move = (MultipleMoves) neighbour.getRandomMove(initial);
			
			move.apply(initial);
			
			Result result = iterateLocalSearch(ls, initial);
			
			initial = result.solution;
			
			feaseableFitness = result.fitness;
			robustFitness = evaluator.evaluateRobustness(result.solution).averagedMinRobustness;
			
			double improvedFitness = omega*feaseableFitness + (1-omega)*robustFitness;
			
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
