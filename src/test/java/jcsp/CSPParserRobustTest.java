package jcsp;

import java.io.IOException;
import java.util.Arrays;

import org.jamesframework.core.problems.constraints.validations.Validation;
import org.jamesframework.core.search.SingleNeighbourhoodSearch;
import org.jamesframework.core.search.algo.RandomDescent;
import org.jamesframework.core.search.neigh.Neighbourhood;
import org.jamesframework.core.search.stopcriteria.MaxSteps;

import jcsp.neighbourhood.CSPSwapNeighbourhood;
import jcsp.robust.RobustCSPProblem;
import jcsp.util.CSPParser;
import util.random.RandomizerFactory;
import util.random.RandomizerFactory.RandomizerAlgorithm;
import util.random.RandomizerUtils;

public class CSPParserRobustTest {
	
	public static void main(String[] args) {
//		 String file = "instances\\200\\robust_200_07.txt";
//		 String file = "instances\\robust_test_16_cars.txt";
		 String file = "instances\\robust_test_10_cars.txt";
		 try {
			RobustCSPProblem problem = (RobustCSPProblem)CSPParser.load(file);
			
			for (int mc =0; mc<30; mc++) {
				problem.random = RandomizerFactory.createRandomizer(
						RandomizerAlgorithm.XOR_SHIFT_128_PLUS_FAST,
						RandomizerUtils.PRIME_SEEDS[mc]);
				//Random Solution
				CSPSolution sol = problem.createRandomSolution();
				Validation validation=problem.validate(sol);
				System.out.println(validation.toString());
				//CountRestrictions
				System.out.println(
						"Fitness:: "+sol.getFitness());
				System.out.println(Arrays.toString(sol.getSequence()));
				
				//Optimization
				Neighbourhood<CSPSolution> neighbourhood 
	        		= new CSPSwapNeighbourhood();
				SingleNeighbourhoodSearch<CSPSolution> search 
					= new RandomDescent<CSPSolution>(problem, neighbourhood);
				
				search.addStopCriterion(new MaxSteps(100000));
			    
			    //Try to improve constructed solution.
				search.setCurrentSolution(sol);
			    
			    //Improvement phase
				search.start();
				
				CSPSolution improved = search.getBestSolution();
				System.out.println(
						"Improved:: "+improved.getFitness());
				System.out.println(Arrays.toString(improved.getSequence()));
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
