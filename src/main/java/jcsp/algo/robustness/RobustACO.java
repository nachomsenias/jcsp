package jcsp.algo.robustness;

import java.util.Arrays;
import java.util.Date;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;

import jcsp.CSPProblem;
import jcsp.CSPSolution;
import jcsp.algo.ACO;
import jcsp.experiment.beans.AlgorithmBean;

public class RobustACO extends ACO {

	public RobustACO(CSPProblem csp, AlgorithmBean algBean, boolean verbose) {
		super(csp, algBean, verbose);
	}

	public void optimize() {
		initializeTrail();
		int step = 0;
		while (bestFitness>CSPProblem.FEASIBLE_FITNESS && step<maxCycles) {
			//Initialize Ants
			CSPSolution[] ants = createAnts();

			double[] colissionsByAnt = growAnts(ants);
			
			//Best of the cycle
			double bestFitness = NumberUtils.min(colissionsByAnt);
			int bestAnt = ArrayUtils.indexOf(colissionsByAnt, bestFitness);

			int[] bestSequence = ants[bestAnt].getSequence();

			if(verbose) {
				System.out.println("Best ant before LS: "+bestFitness);
			}

			evapore(bestSequence, bestFitness);

			if(localSearch!=null) {
				Result localResult = iterateLocalSearch(
						localSearch, 
						new CSPSolution(null, csp, bestSequence)
					);
				if(localResult.solution!=null && localResult.fitness<bestFitness) {
					bestFitness = (int)localResult.fitness;
					bestSequence = localResult.solution.getSequence();
				}
				if(verbose) {
					System.out.println("Best ant after LS: "+bestFitness);
				}
			}
			
			
			
			CSPSolution robustAnt = new CSPSolution(null, csp, bestSequence);
			
			double robustFitness = evaluator.evaluateRobustness(robustAnt).averagedMinRobustness;
			
			double computedFitness = omega*bestFitness + (1-omega)*robustFitness;
			
			if(computedFitness<this.bestFitness) {
				this.bestFitness = computedFitness;
				this.bestFound = robustAnt;
			}
			
			//Next cycle
			step++;
		}

		//Over All Local Search
		if(overAllSearch!=null && this.bestFitness>CSPProblem.FEASIBLE_FITNESS) {
			if(verbose) {
				System.out.println("Best ant before local search : "+bestFitness);
				System.out.println("Final iteration sequence: " + Arrays.toString(
						bestFound.getSequence()));
			}
			//Final solution is searched more deeply.
			maxSteps = 2000 * maxCycles;
			
			Result finalResult = iterateLocalSearch(
					localSearch, 
					bestFound
				);
			
			double robustFitness = evaluator.evaluateRobustness(finalResult.solution).averagedMinRobustness;
			
			double computedFitness = omega*bestFitness + (1-omega)*robustFitness;
			
			
			
			if(computedFitness<this.bestFitness) {
				this.bestFitness = computedFitness;
				this.bestFound = finalResult.solution;
			}
		}
		
		if(verbose) {
			System.out.println("Final ant : "+bestFitness);
			System.out.println("Final sequence: " + Arrays.toString(
					bestFound.getSequence()));
			System.out.println(new Date().toString());
		}
	}
}
