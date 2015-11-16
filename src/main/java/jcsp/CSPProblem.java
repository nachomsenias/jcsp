package jcsp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jcsp.move.AddCar;
import jcsp.neighbourhood.CSPGreedyNeighbourhood;
import jcsp.util.FitnessBean;

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

	/**
	 * Default xCSP weights are those used by the CSP, that 
	 * means, only considering Upper Over Assignement (P+).
	 */
//	public final static double [] DEFAULT_WEIGHTS = {
//		1.0, //UOA //P+
//		0.0, //UUA //P-
//		0.0, //LOA //R+
//		0.0  //LUA //R-
//	};
	
	public final static int EMPTY_CAR = -1;
	
	public final static double FEASIBLE_FITNESS = 0.0;
	
	public static final byte POSSIBLE_INDEX = 0;
	public static final byte TOTAL_INDEX = 1;
	
//	private static final byte P_PLUS_INDEX = 0;
//	private static final byte P_MINUS_INDEX = 1;
//	private static final byte R_PLUS_INDEX = 2;
//	private static final byte R_MINUS_INDEX = 3;
	
//	private static final byte NUM_RESTRICTIONS = 2;

	//First Line
	private int carsDemand; 
	private int numOptions; 
	private int numClasses;
	//Second/Third Line
	private int[][] options;
	//Other lines
	private int[][] requirements;
	private int[] demandByClasses;
	
	//Auxiliar structs
	
	/**
	 * For each option.
	 */
	private double [] ratioPossibleTotal;
	private int [] carsRequiring;
	
//	private double baseError;
	
	//xCSP weights
	//0 Upper Over Assignement // P+
	//1 Under Over Assignement // P-
	//2 Lower Over Assignement // R+
	//3 Lower Under Assignement //R-
//	private double [] weights = DEFAULT_WEIGHTS;
	
	//Randomizer
	public static Randomizer random;
	
	
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
//			ratioPossibleTotal[i] = (double) options[POSSIBLE_INDEX][i] 
//					/ (double) options[TOTAL_INDEX][i];
//			
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
				
				int occurrences = 0;
				
				if(car+(total-1)>=carsDemand) {
					continue;
				}
				
				int nextCar = 0;
				while(nextCar<total) {
					occurrences+=requirements[sequence[car+nextCar]][option];
					nextCar++;
				}
				
				//P+
				if (occurrences>possible) {
					excesses[option][car]=occurrences-possible;
				}

			}
		}
		
		return excesses;
	}
	
	public int evaluateRestrictions(int[] sequence, int lastIndex) {
		int fitness =0;
		
		for (int car=0; car<lastIndex; car++) {
			for (int option=0; option<numOptions; option++) {
				
				int total = this.options[TOTAL_INDEX][option];
				int possible = this.options[POSSIBLE_INDEX][option];
				
				int occurrences = 0;
				
				if(car+(total-1)>carsDemand-1) {
					continue;
				}
				
				int nextCar = 0;
				while(nextCar<total) {
					occurrences+=requirements[sequence[car+nextCar]][option];
					nextCar++;
				}
				
				//P+
				if (occurrences>possible) {
					fitness+=occurrences-possible;
				}

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
				
				int occurrences = 0;
				
				int nextCar = 0;
				while(nextCar<total && car+nextCar<lastIndex 
						&& sequence[car+nextCar]!=EMPTY_CAR) {
					occurrences+=requirements[sequence[car+nextCar]][option];
					nextCar++;
				}
				
				//P+
				if (occurrences>possible) {
					fitness+=occurrences-possible;
				}
				
			}
		}
		
		return fitness;
	}
	
	
//	private double staticUtilizationRate(int option) {
//		double sur = (carsRequiring[option] * ratioPossibleTotal[option])/carsDemand;
//		
//		return sur;
//	}
	
	private double dynamicUtilizationRate(int option, int dynamicRequiring, int dynamicDemand) {
		double dur = (dynamicRequiring * ratioPossibleTotal[option]) / dynamicDemand;
		
		return dur;
	}
	
//	public double staticUtilizationRateSum(int givenClass) {
//		double totalDur = 0;
//		
//		for (int i=0; i<numOptions; i++) {
//			if(requirements[givenClass][i]>0) {
//				double dur = staticUtilizationRate(i);
//				totalDur+=dur;
//			}
//		}
//		
//		return totalDur;
//	}
	
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

	public CSPSolution createGreedy(double alpha) {
		CSPSolution initial = createEmptySolution();
		CSPGreedyNeighbourhood neighbourhood = new CSPGreedyNeighbourhood(this,alpha);
		
		while(initial.getLastIndex()<carsDemand-1) {
			initial = applyNext(initial, neighbourhood);
		}
		
		initial.fullEvaluation();
		
		return initial;
	}
	
	private CSPSolution applyNext(CSPSolution sol, CSPGreedyNeighbourhood neighbourhood) {
		BinaryHeap minHeap = new BinaryHeap(true, FitnessBean.beanComparator());
		
		//Every possible new car
		List<AddCar> everyMove = neighbourhood.getEveryMove(sol);
		//Check new violations
		
		for (AddCar move: everyMove) {
			move.apply(sol);
			double violations = evaluateRestrictionsPartialSequence(
					sol.getSequence(), sol.getLastIndex()+1);
			
			move.undo(sol);
			minHeap.add(new FitnessBean(violations, move));
		}
		
		//Retrieve cars with minimum violations (could be more than one)
		
		FitnessBean top = (FitnessBean)minHeap.pop();		
		double topFitness = top.fitness;
		
		List<AddCar> toBeAdded = new ArrayList<AddCar>();
		toBeAdded.add(top.move);
		
		//While fitness is as good as top, add new moves
		if(!minHeap.isEmpty()) {
			top = (FitnessBean)minHeap.pop();
		}
		
		while(top.fitness == topFitness && !minHeap.isEmpty()) {
			toBeAdded.add(top.move);
			top = (FitnessBean)minHeap.pop();
		}
		
		//If various
		if(toBeAdded.size()>1) {
			return getMaxDurSum(sol, toBeAdded, neighbourhood.getAlpha());
		} else {
			AddCar move = toBeAdded.get(0);
			move.apply(sol);
			return sol;
		}
	}
	
	private CSPSolution getMaxDurSum(CSPSolution sol, List<AddCar> moves, double alpha) {
		BinaryHeap maxHeap = new BinaryHeap(false, FitnessBean.beanComparator());
		
		int totalMoves = moves.size();
		
		for (AddCar move: moves) {
			move.apply(sol);
			double dur = dynamicUtilizationRateSum(sol);
			move.undo(sol);
			maxHeap.add(new FitnessBean(dur, move));
		}
		
		if(alpha!=0.0) {
			List<AddCar> besties = CSPGreedyNeighbourhood.selectBesties(maxHeap, totalMoves, alpha);
			int howManyBesties = besties.size();
			
			AddCar move = besties.get(random.nextInt(howManyBesties));
			move.apply(sol);
			
			return sol;
			
		} else {
			FitnessBean fb = (FitnessBean)maxHeap.pop();
			
			fb.move.apply(sol);
			
			return sol;
		}	
		
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
