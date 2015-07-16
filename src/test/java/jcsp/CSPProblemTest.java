package jcsp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jcsp.util.CSPParser;
import junit.framework.TestCase;

public class CSPProblemTest extends TestCase {

	private CSPProblem csp;
	private List<CSPSolution> sols;
	
	public CSPProblemTest() throws IOException {
		String exampleFile="../xCSP/instances/test_10_cars.txt";
		csp = CSPParser.load(exampleFile);
		sols = new ArrayList<CSPSolution>();
		sols.add(new CSPSolution(null, new int[]{0,1,5,2,4,3,3,4,2,5}, csp));
		sols.add(new CSPSolution(null,new int[]{5,3,4,2,3,4,1,5,2,0}, csp));
		sols.add(new CSPSolution(null,new int[]{5,2,4,3,3,4,2,5,1,0}, csp));
		sols.add(new CSPSolution(null,new int[]{4,3,2,4,3,5,1,5,2,0}, csp));
	}

	public void testEvaluation() {
		
		boolean assertion = true;
		
		for (CSPSolution sol: sols ) {
			double evaluation = csp.evaluate(sol).getValue();
			
			assertion &= evaluation == CSPProblem.FEASIBLE_FITNESS;
		}
		
		assertTrue( assertion );
	}
}
