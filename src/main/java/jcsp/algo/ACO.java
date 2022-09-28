package jcsp.algo;

import java.util.Arrays;
import java.util.Date;

import jcsp.CSPProblem;
import jcsp.CSPSolution;
import jcsp.experiment.beans.ACOBean;
import jcsp.experiment.beans.AlgorithmBean;
import jcsp.localsearch.LocalSearch;
import jcsp.util.functions.ArrayFunctions;
import jcsp.util.functions.Functions;
import jcsp.util.functions.MatrixFunctions;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.math3.stat.StatUtils;

import gnu.trove.list.array.TIntArrayList;

public class ACO extends Algorithm{
	
	//ACO parameters
	private int ants;
	protected int maxCycles;
	
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
	protected LocalSearch localSearch;
	protected LocalSearch overAllSearch;
	
	//CSP values
	private final int numClasses;
	private final int maxQ;
	
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
	}
	
	protected void initializeTrail() {
		trail = new double [numClasses][numClasses][maxQ];
		
		for (int i=0; i<numClasses; i++) {
			for (int j=0; j<numClasses; j++) {
				Arrays.fill(this.trail[i][j],tau0);
			}
		}
	}
	
	protected CSPSolution[] createAnts() {
		
		CSPSolution[] ants = new CSPSolution [this.ants];
		
		for (int a = 0; a<this.ants; a++) {
			//Create every Ant
			ants[a] = csp.createEmptySolution();
			//Randomly chooses first class
			ants[a].addCar(csp.random.nextInt(numClasses));
		}
		
		return ants;
	}
	
	/**
	 * Performs global trail update.
	 * @param sequence
	 * @param fitness
	 */
	protected void evapore(int[] sequence, double fitness) {
		
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
	
	private double[] calculateValues(CSPSolution z, int position) {
		int[] classes = z.getAvailableClasses().toArray();
		int[] sequence = z.getSequence();
		
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
					z.getRequiring(), csp.getCarsDemand()-position, c);
			heuristicValues[c]=Functions.pow(durSumByClass,delta);
//			heuristicValues[c]=Math.pow(durSumByClass,delta);
		}
		
		double[] colissionsValues = new double [numClasses];
		int previousColissions = (int)z.getFitness();
		
		int[] colissionDifferences = new int [numClasses];
		Arrays.fill(colissionDifferences, Integer.MAX_VALUE);
		
		for (int c : classes) {
			sequence[position]=c;
			
			int colDifference = z.checkClassAtPosition(c, position) - previousColissions;
			colissionDifferences[c] = colDifference;
			double colissionsValue = 1.0/(double)(1+colDifference);
//			colissionsValues[c]=Math.pow(colissionsValue,beta);
			colissionsValues[c]=Functions.pow(colissionsValue,beta);
		}
		
		//Candidate list checking
		int minColissions = NumberUtils.min(colissionDifferences);
		
		if(minColissions == 0) {
			for (int c = 0; c<numClasses; c++) {
				if(colissionDifferences[c]!=0) {
					//If a minimumColission is considered, candidates are restricted.
					colissionsValues[c] = 0;
				}
			}
		}
		
		double [] values = new double [numClasses];
		
		for (int c = 0; c<numClasses; c++) {
			values[c] = trailValues[c] * colissionsValues[c] * heuristicValues[c];
		}
		
		return values;
	}
	
	private void choose(CSPSolution z, int position) {
		
		double [] values = calculateValues(z, position);
		
		int chosenClass = -1;
		
		double random = csp.random.nextDouble();
		
		if(random<=q0) {
			//Deterministic - Select Max values
//			chosenClass = ArrayUtils.indexOf(values, NumberUtils.max(values));
			chosenClass = ArrayFunctions.getIndexOfMax(values);
			//If there is a double value fail
			if(chosenClass==CSPProblem.EMPTY_CAR) {
				TIntArrayList available = z.getAvailableClasses();
				int remaining=available.size();
				chosenClass = available.get(csp.random.nextInt(remaining));
			}
		} else {
			//Probabilistic - Create roulette with max values
			
			double total = StatUtils.sum(values);
			
			double probAccumulada = 0;
			double roulette = csp.random.nextDouble();
			
			do {
				chosenClass++;
				probAccumulada+=values[chosenClass]/total;
			} while (probAccumulada<=roulette && chosenClass<csp.getNumClasses());

			if(chosenClass==0 && !z.getAvailableClasses().contains(0)) {
				TIntArrayList available = z.getAvailableClasses();
				int remaining=available.size();
				chosenClass = available.get(csp.random.nextInt(remaining));
			}
		}

		z.addCar(chosenClass);
		int[] sequence = z.getSequence();
		
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
	
	protected double[] growAnts(CSPSolution[] ants) {
		
		//Fill sequences for every ant
		for (int p=1; p<csp.getCarsDemand(); p++) {
			for (int a = 0; a<this.ants; a++) {
				choose(ants[a],p);
			}
		}
		
		double[] colissionsByAnt = new double[this.ants];
		//Retrieve fitness
		for (int a = 0; a<this.ants; a++) {
			colissionsByAnt[a] = ants[a].getFitness();
		}

		return colissionsByAnt;
	}
	
	public void optimize() {
		initializeTrail();
		int step = 0;
		while (bestFitness>CSPProblem.FEASIBLE_FITNESS && step<maxCycles) {
			//Initialize Ants
			CSPSolution[] ants = createAnts();

			double[] colissionsByAnt = growAnts(ants);
			
			//Best of the cycle
			double bestFitness = NumberUtils.min(colissionsByAnt);
			int bestAnt = ArrayUtils.indexOf(colissionsByAnt, bestFitness);

			int[] bestSequence = ants[bestAnt].getSequence();

			if(verbose) {
				System.out.println("Best ant before LS: "+bestFitness);
			}

			//evapore(bestSequence, bestFitness);

			if(localSearch!=null) {
				Result localResult = iterateLocalSearch(
						localSearch, 
						new CSPSolution(null, csp, bestSequence)
					);
				if(localResult.solution!=null && localResult.fitness<bestFitness) {
					bestFitness = (int)localResult.fitness;
					bestSequence = localResult.solution.getSequence();
				}
				if(verbose) {
					System.out.println("Best ant after LS: "+bestFitness);
				}
			}

			//Shouldnt we evaporate after LS?
			evapore(bestSequence, bestFitness);
			
			if(bestFitness<this.bestFitness) {
				this.bestFitness = bestFitness;
				this.bestFound = new CSPSolution(null, csp, bestSequence);
			}
			
			//Next cycle
			step++;
		}

		//Over All Local Search
		if(overAllSearch!=null && this.bestFitness>CSPProblem.FEASIBLE_FITNESS) {
			if(verbose) {
				System.out.println("Best ant before local search : "+bestFitness);
				System.out.println("Final iteration sequence: " + Arrays.toString(
						bestFound.getSequence()));
			}
			//Final solution is searched more deeply.
			maxSteps = 2000 * maxCycles;
			
			Result finalResult = iterateLocalSearch(
					localSearch, 
					bestFound
				);
			
			if(finalResult.fitness<this.bestFitness) {
				this.bestFitness = finalResult.fitness;
				this.bestFound = finalResult.solution;
			}
		}
		
		if(verbose) {
			System.out.println("Final ant : "+bestFitness);
			System.out.println("Final sequence: " + Arrays.toString(
					bestFound.getSequence()));
			System.out.println(new Date().toString());
		}
	}
	
	public void checkAnts() throws Exception {
		initializeTrail();
		
		CSPSolution[] ants = createAnts();
		
		growAnts(ants);
		
		for (CSPSolution z: ants) {
			double fitness = z.getFitness();
			int[][] colissions = csp.createExcessMatrix(z.getSequence());
			double debugFitness = MatrixFunctions.addMatrix(colissions);
			if(debugFitness != fitness) {
				throw new Exception("Ants checking failed.");
			}
		}
	}
}
