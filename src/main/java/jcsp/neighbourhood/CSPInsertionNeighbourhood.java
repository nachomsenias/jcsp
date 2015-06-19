package jcsp.neighbourhood;

import java.util.ArrayList;
import java.util.List;

import jcsp.CSPProblem;
import jcsp.CSPSolution;
import jcsp.move.SingleInsertion;

import org.jamesframework.core.search.neigh.Neighbourhood;

import util.random.Randomizer;

public class CSPInsertionNeighbourhood implements Neighbourhood<CSPSolution>{

	public List<SingleInsertion> getAllMoves(CSPSolution sol) {
		List<SingleInsertion> allInsertions = new ArrayList<SingleInsertion>();
		
		int demand = sol.getSecuenceLength();
		
		for (int pos = 0; pos<demand; pos++) {
			allInsertions.add(new SingleInsertion(pos));
		}
		
		return allInsertions;
	}

	public SingleInsertion getRandomMove(CSPSolution sol) {
		Randomizer random = CSPProblem.random;
		
		int randomPos = random.nextInt(sol.getSecuenceLength()-1);
		
		return new SingleInsertion(randomPos);
	}
}
