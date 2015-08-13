package jcsp.localsearch;

import java.util.List;

import jcsp.CSPProblem;
import jcsp.CSPSolution;

import org.jamesframework.core.search.SingleNeighbourhoodSearch;
import org.jamesframework.core.search.algo.RandomDescent;
import org.jamesframework.core.search.neigh.Neighbourhood;

public class FirstImprovement extends LocalSearch{

	public FirstImprovement(List<Neighbourhood<CSPSolution>> neighbourhood) {
		super(neighbourhood);
	}

	@Override
	public SingleNeighbourhoodSearch<CSPSolution> createLocalSearch(
			CSPProblem csp, Neighbourhood<CSPSolution> neighbourhood) {
		return new RandomDescent<CSPSolution>(csp, neighbourhood);
	}

	@Override
	public SingleNeighbourhoodSearch<CSPSolution> createLocalSearch(
			CSPProblem csp) {
		checkNeighbourhood();
		return new RandomDescent<CSPSolution>(csp, neighbourhood.get(0));
	}

}
