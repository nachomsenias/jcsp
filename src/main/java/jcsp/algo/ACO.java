package jcsp.algo;

import gnu.trove.list.array.TIntArrayList;

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
	
	private class Ant {
		int[] sequence;
		int[] demandByClass;
		TIntArrayList availableClasses;
		int colissions;
		int[] tempColissions;
		
		private Ant(int numClasses, int demand, int[] demandByClass) {
			sequence = new int[demand];
			Arrays.fill(sequence, CSPProblem.EMPTY_CAR);
			
			tempColissions = null;
			this.demandByClass = demandByClass; 
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
	
	public ACO(CSPProblem csp, int ants, int maxCycles, double alpha,
			double beta, double delta, double q0, double tau0, double localRho,
			double globalRho, LocalSearch localSearch, LocalSearch overAllSearch) {
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
		this.maxSteps = 2*maxCycles;
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
	
	private Ant[] createAnts() {
		int numClasses = csp.getNumClasses();
		int demand = csp.getCarsDemand();
		
		Ant[] ants = new Ant [this.ants];
		
		for (int a = 0; a<this.ants; a++) {
			//Create every Ant
			ants[a] = new Ant(numClasses, demand,
							Arrays.copyOf(csp.getDemandByClasses(), numClasses)
								);
			//Randomly chooses first class
			ants[a].addCar(CSPProblem.random.nextInt(numClasses), 0);
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
	
	private double[] calculateValues(Ant z, int position) {
		int numClasses = csp.getNumClasses();
		int qMax = csp.getMaxQ();
		
		int[] classes = z.availableClasses.toArray();
//		int numClasses = classes.length;
		int[] sequence = z.sequence;
		
		double [] trailValues = new double [numClasses];
		
//		for (int i=0; i<numClasses; i++) {
		for (int i : classes) {
//			if(demand[i]==insertedVehicules[i]) {
//				continue;
//			}
			int j =1;
			while(j<=qMax && position-j>=0){
				trailValues[i]+=trail[i][sequence[position-j]][j-1];
				j++;
			}
			trailValues[i] = Math.pow(trailValues[i], alpha);
		}
		
		double[] heuristicValues = new double [numClasses];
		
//		for (int c = 0; c < numClasses; c++) {
		for (int c : classes) {
//			if(insertedVehicules[c]<demand[c]) {
				heuristicValues[c]=Math.pow(csp.staticUtilizationRateSum(c),delta);
//			}
		}
		
		double[] colissionsValues = new double [numClasses];
		int [] newColissions = new int [numClasses];
		int previousColissions = z.colissions;
		
//		for (int c = 0; c < numClasses; c++) {
		for (int c : classes) {
//			if(insertedVehicules[c]<demand[c]) {
				sequence[position]=c;
				newColissions[c] = csp.evaluateRestrictionsPartialSequence(sequence, position) - previousColissions;
				colissionsValues[c]=Math.pow(1.0/(double)(1+newColissions[c]),beta);
//			}
		}
		z.tempColissions = newColissions;
		
		double [] values = new double [numClasses];
		
		for (int c = 0; c<values.length; c++) {
			values[c] = trailValues[c] * colissionsValues[c] * heuristicValues[c];
		}
		
		return values;
	}
	
	private void choose(Ant z, int position) {
		
//		int[] demand = csp.getDemandByClasses();
		
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
//				if(z.demandByClass[chosenClass]<=0) {
//					continue;
//				}
			} while (probAccumulada<roulette && chosenClass<csp.getNumClasses());

		}
		//Local trail update after selecting one class
//		insertedVehicules[chosenClass]++;
//		sequence[position]=chosenClass;
		z.addCar(chosenClass, position);
		z.setFitness(chosenClass);
		
		int[] sequence = z.sequence;
		
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
	}
	
	public void optimize() {
		initializeTrail();
		int step = 0;
		while (bestFitness>0 && step<maxCycles) {
			//Initialize Ants
//			int[][] ants = createAnts();
			Ant[] ants = createAnts();
//			int[][] insertedVehicules = new int [this.ants][csp.getNumClasses()];
//			for (int a =0; a<this.ants; a++) {
//				insertedVehicules[a][ants[a][0]]++;
//			}
			int[] colissionsByAnt = new int[this.ants];
			
			//Fill sequences for every ant
			for (int p=1; p<csp.getCarsDemand(); p++) {
				for (int a = 0; a<this.ants; a++) {
//					ants[a][p] = choose(p, insertedVehicules[a], ants[a], colissionsByAnt[a]);
					choose(ants[a],p);
					colissionsByAnt[a] = ants[a].colissions;
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
						new CSPSolution(null, csp, ants[bestAnt].sequence));
//			Result localResult = new Result(new CSPSolution(null, csp, ants[bestAnt].sequence), bestFitness);
			
//			System.out.println(step);
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
			maxSteps = 2000 * maxCycles;
			
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
