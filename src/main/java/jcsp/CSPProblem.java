package jcsp;

import gnu.trove.list.array.TIntArrayList;

import java.util.Arrays;

import jcsp.util.HeapBean;

import org.apache.commons.collections.BinaryHeap;
import org.apache.commons.lang3.math.NumberUtils;
import org.jamesframework.core.problems.Problem;
import org.jamesframework.core.problems.constraints.validations.SimpleValidation;
import org.jamesframework.core.problems.constraints.validations.Validation;
import org.jamesframework.core.problems.objectives.evaluations.Evaluation;
import org.jamesframework.core.problems.objectives.evaluations.SimpleEvaluation;

import util.Functions;
import util.random.Randomizer;

@SuppressWarnings("deprecation")
public class CSPProblem implements Problem<CSPSolution>{

	public final static int EMPTY_CAR = -1;
	
	public final static double FEASIBLE_FITNESS = 0.0;
	
	public static final byte POSSIBLE_INDEX = 0;
	public static final byte TOTAL_INDEX = 1;

	//First Line
	protected int carsDemand; 
	protected int numOptions; 
	protected int numClasses;
	//Second/Third Line
	protected int[][] options;
	//Other lines
	protected int[][] requirements;
	protected int[] demandByClasses;
	
	//Auxiliar structs
	
	/**
	 * For each option.
	 */
	protected double [] ratioPossibleTotal;
	protected int [] carsRequiring;
	
	//Randomizer
	public Randomizer random;
	
	
	public CSPProblem(
			int carsDemand, int numOptions, int numClasses, 
			int[][] options,
			int[][] requirements,
			int[] demandByClasses
		) {
		super();
		this.carsDemand = carsDemand;
		this.numOptions = numOptions;
		this.numClasses = numClasses;
		this.options = options;
		this.requirements = requirements;
		this.demandByClasses = demandByClasses;
		
		carsRequiring = new int [numOptions];
		
		//Create datastructs
		ratioPossibleTotal = new double[numOptions];
		for (int i=0; i<numOptions; i++) {
			ratioPossibleTotal[i] = (double) options[TOTAL_INDEX][i] 
					/ (double) options[POSSIBLE_INDEX][i];
			
			for (int j=0; j<numClasses; j++) {
				if(requirements[j][i]>0) {
					carsRequiring[i]+=demandByClasses[j];
				}
			}
		}
	}
	
	public CSPSolution createRandomSolution() {
		int [] sequence = new int [carsDemand];
		
		int classByDemand = 0;
		int ocurrences = 0;

		for (int i=0; i<carsDemand; i++) {
			sequence[i] = classByDemand;
			ocurrences++;
			
			if(ocurrences>=demandByClasses[classByDemand]) {
				classByDemand++;
				ocurrences=0;
			}
		}
		
		Functions.shuffleArrayFast(sequence, random);
		
		return new CSPSolution(null,this,sequence);
	}
	
	public CSPSolution createEmptySolution() {
		int [] sequence = new int [carsDemand];
		//Initialize invalid indexes.
		Arrays.fill(sequence, EMPTY_CAR);
		
		return CSPSolution.createEmpty(
				sequence,
					Arrays.copyOf(demandByClasses, numClasses),
						this);
	}
	
	public int[][] createExcessMatrix(int[] sequence) {
		int[][] excesses = new int [numOptions][carsDemand];
		
		for (int car=0; car<carsDemand; car++) {
			for (int option=0; option<numOptions; option++) {
				
				int total = this.options[TOTAL_INDEX][option];
				int possible = this.options[POSSIBLE_INDEX][option];
				
//				int occurrences = 0;

				if(requirements[sequence[car]][option] == 0) {
					continue;
				}
				
//				int nextCar = 0;
//				while(nextCar<total && car+nextCar < carsDemand) {
//					occurrences+=requirements[sequence[car+nextCar]][option];
//					nextCar++;
//				}
//				
//				//P+
//				if (occurrences>possible) {
//					excesses[option][car]=occurrences-possible;
//				}
				
				excesses[option][car]= calculateColissions(
						car, option, sequence, total, possible);

			}
		}
		
		return excesses;
	}
	
	public double evaluateRestrictions(int[] sequence, int lastIndex) {
		double fitness =0;
		
		for (int car=0; car<lastIndex; car++) {
			for (int option=0; option<numOptions; option++) {
				
				int total = this.options[TOTAL_INDEX][option];
				int possible = this.options[POSSIBLE_INDEX][option];
				
//				int occurrences = 0;

				if(requirements[sequence[car]][option] == 0) {
					continue;
				}
				
//				int nextCar = 0;
//				while(nextCar<total && car+nextCar < carsDemand) {
//					occurrences+=requirements[sequence[car+nextCar]][option];
//					nextCar++;
//				}
//				
//				//P+
//				if (occurrences>possible) {
//					fitness+=occurrences-possible;
//				}
				
				fitness+= calculateColissions(car, option, sequence, 
						total, possible);

			}
		}
		
		return fitness;
	}
	
	public int evaluateRestrictionsPartialSequence(int[] sequence, int lastIndex) {
		int fitness = 0;
		
		for (int car=0; car<lastIndex; car++) {
			for (int option=0; option<numOptions; option++) {
				
				int total = this.options[TOTAL_INDEX][option];
				int possible = this.options[POSSIBLE_INDEX][option];
				
				if(requirements[sequence[car]][option] == 0) {
					continue;
				}
				
//				int occurrences = 0;
//				int nextCar = 0;
//				while(nextCar<total && car+nextCar<lastIndex 
//						&& sequence[car+nextCar]!=EMPTY_CAR) {
//					occurrences+=requirements[sequence[car+nextCar]][option];
//					nextCar++;
//				}
//				
//				//P+
//				if (occurrences>possible) {
//					fitness+=occurrences-possible;
//				}
				fitness+= calculateColissions(car, option, sequence, 
						total, possible);
				
			}
		}
		
		return fitness;
	}
	
	protected int calculateColissions(int car, int option, 
			int[] sequence, int total, int possible){
		int nextCar = 1;
		int occurrences = 1;
		
		while(nextCar<total && car+nextCar < carsDemand) {
			occurrences+=requirements[sequence[car+nextCar]][option];
			nextCar++;
		}
		
		//P+
		if (occurrences>possible) {
			return occurrences-possible;
		} else return 0;
	}
	
	private double dynamicUtilizationRate(int option, int dynamicRequiring, int dynamicDemand) {
		double dur = (dynamicRequiring * ratioPossibleTotal[option]) / dynamicDemand;
		
		return dur;
	}

	public double dynamicUtilizationRateSum(CSPSolution sol) {
		int last = sol.getLastIndex()+1;

		return dynamicUtilizationRateSum(sol.getRequiring(), carsDemand-last, sol.getLastType());
	}
	public double dynamicUtilizationRateSum(int[] carsRequiring, 
			int dynamicDemand, int targetClass) {
		double durSumByClass = 0;

		for (int o =0; o<numOptions; o++) {
				if(requirements[targetClass][o]>0) {
					durSumByClass += dynamicUtilizationRate(
							o, carsRequiring[o], dynamicDemand);
				}
		}
		return durSumByClass;
	}

	public CSPSolution createHeuristic(double alpha) {
		CSPSolution initial = createEmptySolution();
		
		int pos = 0;
		
		while(pos<carsDemand) {
			int[] fitness = initial.checkPosition(pos);
			
			int minimum = NumberUtils.min(fitness);
			
			TIntArrayList lowest = new TIntArrayList();
			for (int carClass = 0; carClass<numClasses; carClass++) {
				int colissions = fitness[carClass];
				if(colissions == minimum) {
					lowest.add(carClass);
				}
			}
			
			int[] filteredClasses = lowest.toArray();
			
			double[] heuristicValues = 
					initial.checkHeuristicValues(filteredClasses, pos);
			int next = getBestRandomized(filteredClasses, heuristicValues, alpha);
			initial.addCar(next);
			
			pos++;
		}
		
		return initial;
	}
	
	private int getBestRandomized(int[] filteredClasses, 
			double[] heuristicValues, double alpha
		) {
		
		int numValues = filteredClasses.length;
		
		int numCandidates = (int)(numValues * alpha)+1;
		
		BinaryHeap maxHeap = new BinaryHeap(false, HeapBean.beanComparator());
		for (int c = 0; c<numValues; c++) {
			maxHeap.add(new HeapBean(heuristicValues[c], filteredClasses[c]));
		}
		
		HeapBean[] candidates = new HeapBean[numCandidates];
		for (int i = 0; i<numCandidates; i++) {
			candidates[i] = (HeapBean)maxHeap.pop();
		}
		int selectedClass = candidates[random.nextInt(numCandidates)].carClass;
		
		return selectedClass;
	}
	
	public Evaluation evaluate(CSPSolution sol) {
		
		int lastIndex = sol.getLastIndex();
		
		if(lastIndex!=carsDemand-1) {
			int[] sequence = sol.getSequence();
			return new SimpleEvaluation(
					evaluateRestrictions(sequence, lastIndex+1));
		} else {
			return new SimpleEvaluation(sol.getFitness());
		}
	}

	/**
	 * CSP Problem is modeled as a minimization problem.
	 */
	public boolean isMinimizing() {
		return true;
	}

	/**
	 * By default, any sequence is valid.
	 */
	public Validation validate(CSPSolution sol) {
		
		int[] sequence = sol.getSequence();
		int lastIndex = sol.getLastIndex();
		
		if(sequence == null || lastIndex == EMPTY_CAR) {
			return SimpleValidation.FAILED;
		}
		
		if(lastIndex<carsDemand-1) {
			return SimpleValidation.PASSED;
		}
		
		
		int[] ocurrences = new int [numClasses];
		
		for (int ocurrence : sequence) {
			ocurrences[ocurrence]++;
		}
		
		if(Arrays.equals(ocurrences, demandByClasses)) {
			return SimpleValidation.PASSED;
		} else {
			return SimpleValidation.FAILED;
		}

	}
	
	// GETTERS & SETTERS

	public int getCarsDemand() {
		return carsDemand;
	}
	
	public int[] getDemandByClasses() {
		return demandByClasses;
	}
	
	public int[] getCarsRequiring() {
		return carsRequiring;
	}
	
	public int getNumClasses() {
		return numClasses;
	}
	
	public int getNumOptions() {
		return numOptions;
	}
	
	public int getMaxQ() {
		return NumberUtils.max(options[TOTAL_INDEX]);
	}
	
	public int getP(int option) {
		return options[POSSIBLE_INDEX][option];
	}
	
	public int getQ(int option) {
		return options[TOTAL_INDEX][option];
	}
	
	public int[][] getRequirements() {
		return requirements;
	}
}
