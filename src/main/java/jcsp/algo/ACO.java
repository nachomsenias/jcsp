package jcsp.algo;

import java.util.Arrays;

import jcsp.CSPProblem;
import jcsp.CSPSolution;
import jcsp.localsearch.LocalSearch;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.math3.stat.StatUtils;

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
	
	public ACO(CSPProblem csp, int ants, int maxCycles, double alpha,
			double beta, double delta, double q0, double tau0, double localRho,
			double globalRho, LocalSearch localSearch, LocalSearch overAllSearch, long maxSteps) {
		this.csp = csp;
		this.ants = ants;
		this.maxCycles = maxCycles;
		this.alpha = alpha;
		this.beta = beta;
		this.delta = delta;
		this.q0 = q0;
		this.tau0 = tau0;
		this.localRho = localRho;
		this.globalRho = globalRho;
		this.localSearch = localSearch;
		this.overAllSearch = overAllSearch;
		this.maxSteps = maxSteps;
	}
	
	private void initializeTrail() {
		int numClasses = csp.getNumClasses(); 
		int maxQ = csp.getMaxQ();
		
		trail = new double [numClasses][numClasses][maxQ];
		
		for (int i=0; i<numClasses; i++) {
			for (int j=0; j<numClasses; j++) {
				Arrays.fill(this.trail[i][j],tau0);
			}
		}
	}
	
	private int[][] createAnts() {
		int[][] ants = new int [this.ants][csp.getCarsDemand()];
		
		for (int a = 0; a<this.ants; a++) {
			//Randomly chooses first class
			Arrays.fill(ants[a],-1);
			ants[a][0] = CSPProblem.random.nextInt(csp.getNumClasses());
		}
		
		return ants;
	}
	
	private void evapore(int[] sequence, double fitness) {
		
		//Global evaporation
		int numClasses = csp.getNumClasses();
		int maxQ = csp.getMaxQ();
		
		for (int i=0; i<numClasses; i++) {
			for (int j=0; j<numClasses; j++) {
				for (int y=0; y<maxQ; y++) {
					trail[i][j][y]*= globalRho;
				}
			}
		}
		
		//Best solution update
		int qMax = csp.getMaxQ();
		
		for (int i=0; i<sequence.length; i++) {
			int classI = sequence[i];
			int q = 1;
			int j = i+1;
			while (j<sequence.length && q<=qMax) {
				int classJ = sequence[j];
				
				trail[classI][classJ][q-1] += (1-globalRho) * (1/fitness);
				
				q++;
				j++;
			}
		}
	}
	
	private double[] calculateValues(int position, int[] insertedVehicules, 
			int[] sequence,int previousColissions, int[] demand) {
		int numClasses = csp.getNumClasses();
		int qMax = csp.getMaxQ();
		
		double [] trailValues = new double [numClasses];
		
		for (int i=0; i<numClasses; i++) {
			if(demand[i]==insertedVehicules[i]) {
				continue;
			}
			int j =1;
			while(j<=qMax && position-j>=0){
				trailValues[i]+=trail[i][sequence[position-j]][j-1];
				j++;
			}
			trailValues[i] = Math.pow(trailValues[i], alpha);
		}
		
		double[] heuristicValues = new double [numClasses];
		
		for (int c = 0; c < numClasses; c++) {
			if(insertedVehicules[c]<demand[c]) {
				heuristicValues[c]=Math.pow(csp.staticUtilizationRateSum(c),delta);
			}
		}
		
		double[] colissionsValues = new double [numClasses];
		
		for (int c = 0; c < numClasses; c++) {
			if(insertedVehicules[c]<demand[c]) {
				sequence[position]=c;
				int newColissions = csp.evaluateRestrictionsPartialSequence(sequence, position) - previousColissions;
				colissionsValues[c]=Math.pow(1.0/(double)(1+newColissions),beta);
			}
		}
		
		double [] values = new double [numClasses];
		
		for (int c = 0; c<values.length; c++) {
			values[c] = trailValues[c] * colissionsValues[c] * heuristicValues[c];
		}
		
		return values;
	}
	
	private int choose(int position, int[] insertedVehicules, 
			int[] sequence, int previousColissions) {
		
		int[] demand = csp.getDemandByClasses();
		
		double [] values = calculateValues(position, insertedVehicules, 
				sequence, previousColissions, demand);
		
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
				if(insertedVehicules[chosenClass]==demand[chosenClass]) {
					continue;
				}
			} while (probAccumulada<roulette && chosenClass<csp.getNumClasses());

		}
		//Local trail update after selecting one class
		insertedVehicules[chosenClass]++;
		sequence[position]=chosenClass;
		
		int qMax = csp.getMaxQ();
		int i=1;
		while(position-i>=0 && i<=qMax) {
			int prevClass = sequence[position-i];
			
//			trail[prevClass][chosenClass][i-1] *= localRho;
//			trail[prevClass][chosenClass][i-1] += (1.0-localRho)*tau0;
			
			trail[prevClass][chosenClass][i-1] =
					(trail[prevClass][chosenClass][i-1] * localRho)
						+ ((1.0-localRho)*tau0);
//			trail[chosenClass][sequence[position-i]][i-1] =
//					(trail[chosenClass][sequence[position-i]][i-1] * localRho)
//						+ (1-localRho)*tau0;
			i++;
		}
		
		return chosenClass;
	}
	
	public void optimize() {
		initializeTrail();
		int step = 0;
		while (bestFitness>0 && step<maxCycles) {
			//Initialize Ants
			int[][] ants = createAnts();
			int[][] insertedVehicules = new int [this.ants][csp.getNumClasses()];
			for (int a =0; a<this.ants; a++) {
				insertedVehicules[a][ants[a][0]]++;
			}
			int[] colissionsByAnt = new int[this.ants];
			
			//Fill sequences for every ant
			for (int p=1; p<csp.getCarsDemand(); p++) {
				for (int a = 0; a<this.ants; a++) {
					ants[a][p] = choose(p, insertedVehicules[a], ants[a], colissionsByAnt[a]);
					
					colissionsByAnt[a] = csp.evaluateRestrictionsPartialSequence(ants[a], p);
				}
			}
			
			//Best of the cycle
			int bestFitness = NumberUtils.min(colissionsByAnt);
			int bestAnt = ArrayUtils.indexOf(colissionsByAnt, bestFitness);
			
			if(verbose) {
				System.out.println("Best ant before LS: "+bestFitness);
			}
			
			Result localResult = runLocalSearch(localSearch, 
					localSearch.getNeighbourhoods().get(0), 
						new CSPSolution(null, csp, ants[bestAnt]));
//			Result localResult = new Result(new CSPSolution(null, csp, ants[bestAnt]), bestFitness);
			
//			System.out.println(localResult.fitness);
			
			if(verbose) {
				System.out.println("Best ant after LS: "+localResult.fitness);
				System.out.println("Best ant sequence: "
						+Arrays.toString(localResult.solution.getSequence()));
			}
			
			evapore(localResult.solution.getSequence(), localResult.fitness);
			
			//Best overall
			if(localResult.fitness<this.bestFitness) {
				this.bestFitness = localResult.fitness;
				this.bestFound = localResult.solution;
			}
			
			//Next cycle
			step++;
		}
		
		//Over All Local Search
		if(this.bestFitness>0.0) {
			Result finalResult = runLocalSearch(overAllSearch, 
					overAllSearch.getNeighbourhoods().get(0), 
						bestFound);
			
			if(finalResult.fitness<this.bestFitness) {
				this.bestFitness = finalResult.fitness;
				this.bestFound = finalResult.solution;
			}
		}		
	}
}
