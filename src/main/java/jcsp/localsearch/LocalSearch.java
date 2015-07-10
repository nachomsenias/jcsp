package jcsp.localsearch;

import jcsp.CSPProblem;
import jcsp.CSPSolution;

import org.jamesframework.core.search.SingleNeighbourhoodSearch;
import org.jamesframework.core.search.neigh.Neighbourhood;

public abstract class LocalSearch {
	
	protected Neighbourhood<CSPSolution> neighbourhood = null;

	public LocalSearch(Neighbourhood<CSPSolution> neighbourhood) {
		this.neighbourhood = neighbourhood;
	}
	
	protected void checkNeighbourhood() {
		if(neighbourhood==null) {
			throw new IllegalStateException("Neighbourhood is null.");
		}
	}

	public abstract SingleNeighbourhoodSearch<CSPSolution> createLocalSearch(CSPProblem csp, Neighbourhood<CSPSolution> neighbourhood);
	public abstract SingleNeighbourhoodSearch<CSPSolution> createLocalSearch(CSPProblem csp);

	public Neighbourhood<CSPSolution> getNeighbourhood() {
		return neighbourhood;
	}

	public void setNeighbourhood(Neighbourhood<CSPSolution> neighbourhood) {
		this.neighbourhood = neighbourhood;
	}
}
