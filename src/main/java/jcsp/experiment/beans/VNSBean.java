package jcsp.experiment.beans;

import java.util.ArrayList;
import java.util.List;

import jcsp.CSPProblem;
import jcsp.CSPSolution;
import jcsp.algo.Algorithm;
import jcsp.algo.VNS;
import jcsp.localsearch.LocalSearch;
import jcsp.neighbourhood.CSPRandomShakingInsert;
import jcsp.neighbourhood.CSPRandomShakingSwap;

import org.jamesframework.core.search.neigh.Neighbourhood;

import util.io.ConfigFileReader;

public class VNSBean extends AlgorithmBean{

	//VNS parameters
	public int maxSteps;
	public int maxSeconds;
	
	public boolean greedyInitialSolution;
	
	public List<Neighbourhood<CSPSolution>> improvers;
	
	public List<Neighbourhood<CSPSolution>> shakers;
	
	private static List<Neighbourhood<CSPSolution>> readShakers(
			String[] shakers, int max) {
		List<Neighbourhood<CSPSolution>> sh = new ArrayList<Neighbourhood<CSPSolution>>();

		for (String shaking:shakers) {
			switch (shaking) {
			case "randomSwap":
				for (int i=1; i<=max; i++) {
					sh.add(new CSPRandomShakingSwap(i));
				}
				break;
			case "randomInsert":
				for (int i=1; i<=max; i++) {
					sh.add(new CSPRandomShakingInsert(i));
				}
				break;
			default:
				throw new IllegalArgumentException(
						"Illegal neighbourhood value: "+shaking);
			}
		}
		
		return sh;
	}
	
	public void readConfigFile(ConfigFileReader reader) {
		
		greedyInitialSolution = reader.getParameterBoolean("greedyInitialSolution");
		
		maxSteps = reader.getParameterInteger("maxSteps");
		maxSeconds = reader.getParameterInteger("maxSeconds");
	
		String[] neighbourhood = reader.getParameterStringArray("neighbourhood");
		improvers = LocalSearch.createNeighbourhoods(neighbourhood);
		
		int maxShaking = reader.getParameterInteger("maxShaking");
		String[] shaking = reader.getParameterStringArray("shakers");
		
		shakers = readShakers(shaking, maxShaking);
	}

	@Override
	public Algorithm createAlgorithmInstance(CSPProblem csp, boolean verbose) {
		return new VNS(csp, this, verbose);
	}
}
