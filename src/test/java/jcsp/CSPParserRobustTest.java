package jcsp;

import java.io.IOException;

import org.jamesframework.core.problems.constraints.validations.Validation;

import jcsp.util.CSPParser;
import util.random.RandomizerFactory;
import util.random.RandomizerUtils;
import util.random.RandomizerFactory.RandomizerAlgorithm;

public class CSPParserRobustTest {
	
	public static void main(String[] args) {
		 String file = "instances\\200\\robust_200_07.txt";
		 try {
			RobustCSPProblem problem = (RobustCSPProblem)CSPParser.load(file);
			problem.random = RandomizerFactory.createRandomizer(
					RandomizerAlgorithm.XOR_SHIFT_128_PLUS_FAST,
					RandomizerUtils.PRIME_SEEDS[0]);
			//Random Solution
			CSPSolution sol = problem.createRandomSolution();
			Validation validation=problem.validate(sol);
			System.out.println(validation.toString());
			//CountRestrictions
			System.out.println(
					"Fitness:: "+problem.evaluateRestrictions(
							sol.getSequence(),sol.getLastIndex()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
