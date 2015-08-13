package jcsp.localsearch;

import java.util.List;

import jcsp.CSPProblem;
import jcsp.CSPSolution;

import org.jamesframework.core.search.SingleNeighbourhoodSearch;
import org.jamesframework.core.search.neigh.Neighbourhood;

public abstract class LocalSearch {
	
	protected List<Neighbourhood<CSPSolution>> neighbourhood = null;

	public LocalSearch(List<Neighbourhood<CSPSolution>> neighbourhood) {
		this.neighbourhood = neighbourhood;
	}
	
	protected void checkNeighbourhood() {
		if(neighbourhood==null) {
			throw new IllegalStateException("Neighbourhood is null.");
		}
	}

	public abstract SingleNeighbourhoodSearch<CSPSolution> createLocalSearch(CSPProblem csp, Neighbourhood<CSPSolution> neighbourhood);
	public abstract SingleNeighbourhoodSearch<CSPSolution> createLocalSearch(CSPProblem csp);

	public List<Neighbourhood<CSPSolution>> getNeighbourhoods() {
		return neighbourhood;
	}

	public void setNeighbourhood(List<Neighbourhood<CSPSolution>> neighbourhood) {
		this.neighbourhood = neighbourhood;
	}
}
