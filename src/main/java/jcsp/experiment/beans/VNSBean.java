package jcsp.experiment.beans;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jcsp.CSPSolution;
import jcsp.localsearch.LocalSearch;
import jcsp.neighbourhood.CSPRandomShakingInsert;
import jcsp.neighbourhood.CSPRandomShakingSwap;

import org.jamesframework.core.search.neigh.Neighbourhood;

import util.io.ConfigFileReader;

public class VNSBean {

	//VNS parameters
	final public int maxSteps;
	final public int maxSeconds;
	
	final public boolean greedyInitialSolution;
	
	final public List<Neighbourhood<CSPSolution>> improvers;
	
	final public List<Neighbourhood<CSPSolution>> shakers;

	public VNSBean(boolean greedy, int maxSteps, int maxSeconds, 
			List<Neighbourhood<CSPSolution>> localSearch, 
			List<Neighbourhood<CSPSolution>> shakers) {
		this.greedyInitialSolution = greedy;
		this.maxSteps = maxSteps;
		this.maxSeconds = maxSeconds;
		this.improvers = localSearch;
		this.shakers = shakers;
	}
	
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
	
	public static VNSBean readConfigFile(File configFile) throws FileNotFoundException, IOException {

		ConfigFileReader reader = new ConfigFileReader();
		
		reader.readConfigFile(configFile);
		
		boolean greedy = reader.getParameterBoolean("greedyInitialSolution");
		
		int maxSteps = reader.getParameterInteger("maxSteps");
		int maxSeconds = reader.getParameterInteger("maxSeconds");
	
		String[] neighbourhood = reader.getParameterStringArray("neighbourhood");
		List<Neighbourhood<CSPSolution>> neighbours = LocalSearch.createNeighbourhoods(neighbourhood);
		
		int maxShaking = reader.getParameterInteger("maxShaking");
		String[] shakers = reader.getParameterStringArray("shakers");
		
		List<Neighbourhood<CSPSolution>> shaking = readShakers(shakers, maxShaking);

		return new VNSBean(greedy,maxSteps, maxSeconds,neighbours, shaking);
	}
}
