package jcsp;

import java.util.Arrays;

import org.jamesframework.core.problems.Problem;
import org.jamesframework.core.problems.constraints.validations.SimpleValidation;
import org.jamesframework.core.problems.constraints.validations.Validation;
import org.jamesframework.core.problems.objectives.evaluations.Evaluation;
import org.jamesframework.core.problems.objectives.evaluations.SimpleEvaluation;

import util.Functions;
import util.random.Randomizer;

public class CSPProblem implements Problem<CSPSolution>{

	/**
	 * Default xCSP weights are those used by the CSP, that 
	 * means, only considering Upper Over Assignement (P+).
	 */
	public final static double [] DEFAULT_WEIGHTS = {
		1.0, //UOA //P+
		0.0, //UUA //P-
		0.0, //LOA //R+
		0.0  //LUA //R-
	};
	
	public final static int EMPTY_CAR = -1;
	
	public final static double FEASIBLE_FITNESS = 0.0;
	
	private static final byte POSSIBLE_FROM = 0;
	private static final byte TOTAL_INDEX = 1;
	
	private static final byte P_PLUS_INDEX = 0;
	private static final byte P_MINUS_INDEX = 1;
//	private static final byte R_PLUS_INDEX = 2;
//	private static final byte R_MINUS_INDEX = 3;
	
	private static final byte NUM_RESTRICTIONS = 2;

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
	
	private double baseError;
	
	//xCSP weights
	//0 Upper Over Assignement // P+
	//1 Under Over Assignement // P-
	//2 Lower Over Assignement // R+
	//3 Lower Under Assignement //R-
	private double [] weights = DEFAULT_WEIGHTS;
	
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
		
		//Create datastructs
		ratioPossibleTotal = new double[numOptions];
		for (int i=0; i<numOptions; i++) {
			ratioPossibleTotal[i] = (double) options[0][i] / (double) options[1][i];
		}
		
		baseError = numOptions*carsDemand;
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
		
		return new CSPSolution(sequence,numClasses);
	}
	
	public CSPSolution createEmptySolution() {
		int [] sequence = new int [carsDemand];
		//Initialize invalid indexes.
		Arrays.fill(sequence, EMPTY_CAR);
		
		return new CSPSolution(
				sequence,numClasses,
				Arrays.copyOf(demandByClasses, numClasses)
			);
	}

	
	private double evaluateRestrictions(int[] sequence) {
		int[] values = new int [NUM_RESTRICTIONS];
		
		for (int car=0; car<carsDemand; car++) {
			for (int option=0; option<numOptions; option++) {
				
				int total = this.options[TOTAL_INDEX][option];
				int possible = this.options[POSSIBLE_FROM][option];
				
				int occurrences = 0;
				
				int nextCar = 0;
				while(nextCar<total && car+nextCar<carsDemand 
						&& sequence[car+nextCar]!=EMPTY_CAR) {
					occurrences+=requirements[sequence[car+nextCar]][option];
					nextCar++;
				}
				
				//P+
				if (occurrences>possible) {
					values[P_PLUS_INDEX]+=occurrences-possible;
				}
				
				//P-
				if (occurrences<possible) {
					values[P_MINUS_INDEX]+=possible-occurrences;
				}
				
				//TODO NO values have been supplied for lower assignments
				
//				//R+
//				if (occurrences>min_possible) {
//					loa+=occurrences-min_possible;
//				}
				
//				//R-
//				if (occurrences<min_possible) {
//					lua+=min_possible-occurrences;
//				}
				
			}
		}
		
		double fitness =0;
		
		for (int i=0; i<values.length; i++) {
			fitness+=values[i]*weights[i];
		}
		
		return fitness;
	}
	
	private double dynamicUtilizationRate(CSPSolution sol) {
		double totalDur = 0;
		int [] availableByClass = sol.getRemainingClasses();
		
		int lastAssigned = sol.getLastIndex();

		
		for (int i=0; i<numOptions; i++) {
			
			//Ni(PI) - Ni(PIj) => Because Ni(PIj) = Ni(PI) - (Not assigned using i)
			// This expression equals : Not assigned using i.
			int notAssigned = 0;
			for (int j=0; j<numClasses; j++) {
				if(requirements[j][i]>0) {
					notAssigned+=availableByClass[j];
				}
			}
			
			double dur = ratioPossibleTotal[i]*((double)notAssigned/(carsDemand-(lastAssigned)));
			totalDur+=dur;
		}
		
		return totalDur*carsDemand;
	}
	
	//TODO
	public CSPSolution createGreedy() {
		CSPSolution initial = createEmptySolution();
		return initial;
	}
	
	public Evaluation evaluate(CSPSolution sol) {		
		int[] sequence = sol.getSequence();
		
		int lastIndex = sol.getLastIndex();
		
		int invalidCars = carsDemand-(lastIndex+1);
		
		if (invalidCars>0) {
			double dur = dynamicUtilizationRate(sol);
			
			double fitness = ((double)carsDemand) / dur;
			fitness+=baseError;
			
			return new SimpleEvaluation(fitness);
		} else {
			return new SimpleEvaluation(
					evaluateRestrictions(sequence));
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
		
		if(sol.getLastIndex()<carsDemand-1) {
			return SimpleValidation.PASSED;
		}
		
		int[] sequence = sol.getSequence();
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
	
	public int getNumClasses() {
		return numClasses;
	}
}
