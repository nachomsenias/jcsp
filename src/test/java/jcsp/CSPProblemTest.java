package jcsp;

import java.io.IOException;

import jcsp.util.CSPParser;
import junit.framework.TestCase;

public class CSPProblemTest extends TestCase {

	private CSPProblem csp;
	private CSPSolution sol;
	
	public CSPProblemTest() throws IOException {
		String exampleFile="../xCSP/instances/test_10_cars.txt";
		csp = CSPParser.load(exampleFile);
		sol = new CSPSolution(new int[]{0,1,5,2,4,3,3,4,2,5}, 6);
	}

	public void testEvaluation() {
		
		double evaluation = csp.evaluate(sol).getValue();
		
		boolean assertion = evaluation == CSPProblem.FEASIBLE_FITNESS;
		assertTrue( assertion );
	}
}
