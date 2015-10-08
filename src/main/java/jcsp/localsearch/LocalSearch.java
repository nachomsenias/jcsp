package jcsp.localsearch;

import java.util.ArrayList;
import java.util.List;

import jcsp.CSPProblem;
import jcsp.CSPSolution;
import jcsp.neighbourhood.CSPInsertionNeighbourhood;
import jcsp.neighbourhood.CSPInvertionNeighbourhood;
import jcsp.neighbourhood.CSPShuffleNeighbourhood;
import jcsp.neighbourhood.CSPSwapNeighbourhood;

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
	
	public static LocalSearch createLocalSearch(String localSearh, String[] neighbourhoods) {
		
		List<Neighbourhood<CSPSolution>> nh = createNeighbourhoods(neighbourhoods);
		
		switch (localSearh) {
		case "first":
			return new FirstImprovement(nh);
			
		case "best":
			return new BestImprovement(nh);

		default:
			throw new IllegalArgumentException(
					"Illegal local search value: "+localSearh);
		}
	}
	
	public static List<Neighbourhood<CSPSolution>> createNeighbourhoods(
			String[] neighbourhoods
			) {
		List<Neighbourhood<CSPSolution>> nh = new ArrayList<Neighbourhood<CSPSolution>>();
		
		for (String neighbourhood:neighbourhoods) {
			switch (neighbourhood) {
			case "swap":
				nh.add(new CSPSwapNeighbourhood());
				break;
			case "insertion":
				nh.add(new CSPInsertionNeighbourhood());
				break;
			case "inversion":
				nh.add(new CSPInvertionNeighbourhood());
				break;
			case "shuffle":
				nh.add(new CSPShuffleNeighbourhood());
				break;
			default:
				throw new IllegalArgumentException(
						"Illegal neighbourhood value: "+neighbourhood);
			}
		}
		
		return nh;
	}
}
