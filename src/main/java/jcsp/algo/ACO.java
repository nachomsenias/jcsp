package jcsp.algo;

import gnu.trove.list.array.TIntArrayList;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import jcsp.CSPProblem;
import jcsp.CSPSolution;
import jcsp.experiment.beans.ACOBean;
import jcsp.experiment.beans.AlgorithmBean;
import jcsp.localsearch.LocalSearch;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.math3.stat.StatUtils;
import org.jamesframework.core.search.neigh.Neighbourhood;

import util.Functions;

public class ACO extends Algorithm{
	
	//ACO parameters
	private int ants;
	private int maxCycles;
	
	private double alpha;
	private double beta;
	private double delta;
	
	private double q0;
	
	private double tau0;
	private double localRho;
	private double globalRho;
	
	/**
	 * Pheromone trail structure.
	 * 	[numClases] [numClases] [maxQ].
	 */
	private double[][][] trail;
	
	//Local search
	private LocalSearch localSearch;
	private LocalSearch overAllSearch;
	
	//CSP values
	private final int numClasses;
	private final int maxQ;
	private final int[][] requirements;
	
	private class Ant {
		private int[] sequence;
		private int[] demandByClass;
		private int [] carsRequiring;
		
		private TIntArrayList availableClasses;
		private int colissions;
		private int[] tempColissions;
		
		private Ant() {
			sequence = new int[csp.getCarsDemand()];
			Arrays.fill(sequence, CSPProblem.EMPTY_CAR);
			
			tempColissions = null;
			demandByClass = Arrays.copyOf(
					csp.getDemandByClasses(), numClasses);
			carsRequiring= Arrays.copyOf(
					csp.getCarsRequiring(), csp.getNumOptions());
			
			colissions = 0;
			tempColissions = null;
			
			availableClasses = new TIntArrayList(numClasses);
			for (int i=0; i<numClasses; i++) {
				availableClasses.add(i);
			}
		}
		
		private void addCar(int carClass, int position) {
			sequence[position] = carClass;
			demandByClass[carClass]--;
			
			for (int i=0; i<csp.getNumOptions(); i++) {
				if(requirements[carClass][i]>0) {
					carsRequiring[i]--;
				}
			}
			
			if(demandByClass[carClass]==0) {
				disableClass(carClass);
			}
		}
		
		private void setFitness(int classChosen) {
			colissions = tempColissions[classChosen];
		}
		
		private void disableClass(int carClass) {
			if(!availableClasses.remove(carClass)) {
				throw new IllegalArgumentException(
						"Given class was already removed: "+carClass);
			}
		}
	}
	
	public ACO(CSPProblem csp, AlgorithmBean algBean, boolean verbose) {
		super(csp,algBean,verbose);
		
		ACOBean bean = (ACOBean)algBean;

		ants = bean.ants;
		maxCycles = bean.maxCycles;
		alpha = bean.alpha;
		beta = bean.beta;
		delta = bean.delta;
		q0 = bean.q0;
		tau0 = bean.tau0;
		localRho = bean.localRho;
		globalRho = bean.globalRho;
		localSearch = bean.localSearch;
		overAllSearch = bean.overAllSearch;
		maxSteps = 2*maxCycles;
		
		maxQ = csp.getMaxQ();
		numClasses = csp.getNumClasses();
		requirements = csp.getRequirements();
	}
	
	private void initializeTrail() {
		trail = new double [numClasses][numClasses][maxQ];
		
		for (int i=0; i<numClasses; i++) {
			for (int j=0; j<numClasses; j++) {
				Arrays.fill(this.trail[i][j],tau0);
			}
		}
	}
	
	private Ant[] createAnts() {
		
		Ant[] ants = new Ant [this.ants];
		
		for (int a = 0; a<this.ants; a++) {
			//Create every Ant
			ants[a] = new Ant();
			//Randomly chooses first class
			ants[a].addCar(CSPProblem.random.nextInt(numClasses), 0);
		}
		
		return ants;
	}
	
	/**
	 * Performs global trail update.
	 * @param sequence
	 * @param fitness
	 */
	private void evapore(int[] sequence, double fitness) {
		
		for (int i=0; i<numClasses; i++) {
			for (int j=0; j<numClasses; j++) {
				for (int y=0; y<maxQ; y++) {
					trail[i][j][y]*= globalRho;
				}
			}
		}
		
		//Best solution update
		
		for (int position=0; position<sequence.length; position++) {
			int classI = sequence[position];
			int y = 1;

			while (position+y<sequence.length && y<=maxQ) {
				int classJ = sequence[position+y];
				
				trail[classI][classJ][y-1] += (1.0-globalRho) * (1.0/fitness);
				
				y++;
			}
		}
	}
	
	private double[] calculateValues(Ant z, int position) {
		int[] classes = z.availableClasses.toArray();
		int[] sequence = z.sequence;
		
		double [] trailValues = new double [numClasses];
		

		for (int i : classes) {
			int j =1;
			while(j<=maxQ && position-j>=0){
				//TRAIL IS CHECKED AT j-1 because 0 position means distance 1.
				trailValues[i]+=trail[sequence[position-j]][i][j-1];
				j++;
			}
			trailValues[i] = Functions.pow(trailValues[i], alpha);
//			trailValues[i] = Math.pow(trailValues[i], alpha);
		}
		
		double[] heuristicValues = new double [numClasses];

		for (int c : classes) {
			double durSumByClass = csp.dynamicUtilizationRateSum(
					z.carsRequiring, csp.getCarsDemand()-position, c);
			heuristicValues[c]=Functions.pow(durSumByClass,delta);
//			heuristicValues[c]=Math.pow(durSumByClass,delta);
		}
		
		double[] colissionsValues = new double [numClasses];
		int [] newColissions = new int [numClasses];
		int previousColissions = z.colissions;
		
		for (int c : classes) {
			sequence[position]=c;
			
			newColissions[c] = csp.evaluateRestrictionsPartialSequence(sequence, position+1);
			
			int colDifference = newColissions[c]- previousColissions;
			double colissionsValue = 1.0/(double)(1+colDifference);
//				colissionsValues[c]=Math.pow(colissionsValue,beta);
			colissionsValues[c]=Functions.pow(colissionsValue,beta);
		}
		z.tempColissions = newColissions;
		
		double [] values = new double [numClasses];
		
		for (int c = 0; c<numClasses; c++) {
			values[c] = trailValues[c] * colissionsValues[c] * heuristicValues[c];
		}
		
		return values;
	}
	
	private void choose(Ant z, int position) {
		
		double [] values = calculateValues(z, position);
		
		int chosenClass = -1;
		
		double random = CSPProblem.random.nextDouble();
		
		if(random<=q0) {
			//Deterministic - Select Max values
			chosenClass = ArrayUtils.indexOf(values, NumberUtils.max(values));
		} else {
			//Probabilistic - Create roulette with max values
			
			double total = StatUtils.sum(values);
			
			double probAccumulada = 0;
			double roulette = CSPProblem.random.nextDouble();
			
			do {
				chosenClass++;
				probAccumulada+=values[chosenClass]/total;
			} while (probAccumulada<roulette && chosenClass<csp.getNumClasses());

		}
		
		z.addCar(chosenClass, position);
		z.setFitness(chosenClass);

		int[] sequence = z.sequence;
		
		//Local trail update
		int i=1;
		while(position-i>=0 && i<=maxQ) {
			int prevClass = sequence[position-i];
			
			trail[prevClass][chosenClass][i-1] =
					(trail[prevClass][chosenClass][i-1] * localRho)
						+ ((1.0-localRho)*tau0);
			i++;
		}
	}
	
	public void optimize() {
		initializeTrail();
		int step = 0;
		while (bestFitness>CSPProblem.FEASIBLE_FITNESS && step<maxCycles) {
			//Initialize Ants
			Ant[] ants = createAnts();
			int[] colissionsByAnt = new int[this.ants];
			
			//Fill sequences for every ant
			for (int p=1; p<csp.getCarsDemand(); p++) {
				for (int a = 0; a<this.ants; a++) {
					choose(ants[a],p);
					//Update fitness
					colissionsByAnt[a] = ants[a].colissions;
				}
			}
			
			//Best of the cycle
			int bestFitness = NumberUtils.min(colissionsByAnt);
			int bestAnt = ArrayUtils.indexOf(colissionsByAnt, bestFitness);
			
			int[] bestSequence = ants[bestAnt].sequence;

			if(verbose) {
				System.out.println("Best ant before LS: "+bestFitness);
			}

			evapore(bestSequence, bestFitness);

			if(localSearch!=null) {
				Result localResult = null;
				List<Neighbourhood<CSPSolution>> neighbourhoods 
					= localSearch.getNeighbourhoods();
				CSPSolution solution = new CSPSolution(null, csp, bestSequence); 
				for (Neighbourhood<CSPSolution> n: neighbourhoods) {
					localResult = runLocalSearch(localSearch, n, solution);
					solution = localResult.solution;
		    	}
				if(localResult.solution!=null && localResult.fitness<bestFitness) {
					bestFitness = (int)localResult.fitness;
					bestSequence = localResult.solution.getSequence();
				}
			}
			
			if(bestFitness<this.bestFitness) {
				this.bestFitness = bestFitness;
				this.bestFound = new CSPSolution(null, csp, bestSequence);
			}
			
			//Next cycle
			step++;
		}

		//Over All Local Search
		if(overAllSearch!=null && this.bestFitness>CSPProblem.FEASIBLE_FITNESS) {
			System.out.println("Best ant before local search : "+bestFitness);
			System.out.println("Final iteration sequence: " + Arrays.toString(
					bestFound.getSequence()));
			
			maxSteps = 2000 * maxCycles;
			
			Result finalResult = null;
			List<Neighbourhood<CSPSolution>> neighbourhoods 
				= overAllSearch.getNeighbourhoods();
					
			for (Neighbourhood<CSPSolution> n: neighbourhoods) {
				finalResult = runLocalSearch(localSearch, n, bestFound);
				bestFound = finalResult.solution;
	    	}
					
					runLocalSearch(overAllSearch, 
					overAllSearch.getNeighbourhoods().get(0), 
						bestFound);
			
			if(finalResult.fitness<this.bestFitness) {
				this.bestFitness = finalResult.fitness;
				this.bestFound = finalResult.solution;
			}
		}
		
		System.out.println("Final ant : "+bestFitness);
		System.out.println("Final sequence: " + Arrays.toString(
				bestFound.getSequence()));
		System.out.println(new Date().toString());
	}
}
