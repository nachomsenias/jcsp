package jcsp;

import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;

import jcsp.robust.RobustnessEvaluator;
import jcsp.robust.RobustnessResult;
import jcsp.util.CSPParser;

public class RobustnessEvaluatorTest {
	
	String problemFile;
	String alternatePlansFile;
	
	RobustnessEvaluator rEval;

	public RobustnessEvaluatorTest() {
		problemFile="instances/test_10_cars.txt";
//		alternatePlansFile = "instances/test_10_cars_alternate_plans.txt";
		alternatePlansFile = "instances/test_10_cars_alternate_plans_2.txt";
	}
	
	@Test
	public void testLoader() {
		try {
			rEval=CSPParser.loadRobustnessEvaluator(problemFile, alternatePlansFile);
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void testRobustness() {
		if(rEval==null) {
			testLoader();
		}
		CSPSolution solution = new CSPSolution(
				null, rEval.getCsp(), new int[]{0,1,5,2,4,3,3,4,2,5});
		RobustnessResult rResult = rEval.evaluateRobustness(solution);
		
		System.out.println("Robustness value: "+rResult.toString());
	}
	
}
