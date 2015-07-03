package jcsp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jcsp.move.AddCar;
import jcsp.neighbourhood.CSPGreedyNeighbourhood;
import jcsp.util.FitnessBean;

import org.apache.commons.collections.BinaryHeap;
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
	private int [] carsRequiring;
	
//	private double baseError;
	
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
		
		carsRequiring = new int [numOptions];
		
		//Create datastructs
		ratioPossibleTotal = new double[numOptions];
		for (int i=0; i<numOptions; i++) {
			ratioPossibleTotal[i] = (double) options[0][i] / (double) options[1][i];
			
			for (int j=0; j<numClasses; j++) {
				if(requirements[j][i]>0) {
					carsRequiring[i]+=demandByClasses[i];
				}
			}
		}
		
//		baseError = numOptions*carsDemand;
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

	
	private double evaluateRestrictions(int[] sequence, int lastIndex) {
		int[] values = new int [NUM_RESTRICTIONS];
		
		for (int car=0; car<lastIndex; car++) {
			for (int option=0; option<numOptions; option++) {
				
				int total = this.options[TOTAL_INDEX][option];
				int possible = this.options[POSSIBLE_FROM][option];
				
				int occurrences = 0;
				
				int nextCar = 0;
				while(nextCar<total && car+nextCar<lastIndex 
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
	
	private double staticUtilizationRate(int option) {
		double sur = (carsRequiring[option] * ratioPossibleTotal[option])/carsDemand;
		
		return sur;
	}
	
	private double dynamicUtilizationRate(int option, int currentRequiring) {
//		double dur = (carsRequiring[option] - currentRequiring) * ratioPossibleTotal[option];
		double dur = currentRequiring * ratioPossibleTotal[option];
		
		return dur;
	}
	
	private double staticUtilizationRateSum(CSPSolution sol) {
		double totalDur = 0;

		int lastAssigned = sol.getLastCar();
		
		for (int i=0; i<numOptions; i++) {
			if(requirements[lastAssigned][i]>0) {
				double dur = staticUtilizationRate(i);
				totalDur+=dur;
			}
		}
		
		return totalDur;
	}
	
	private double dynamicUtilizationRateSum(CSPSolution sol) {
//		double totalDur = 0;
//		int [] availableByClass = sol.getRemainingClasses();
//		
//		int lastAssigned = sol.getLastIndex();
//		
//		int carsLeft = carsDemand-lastAssigned;
//
//		
//		for (int i=0; i<numOptions; i++) {
//			
//			//Ni(PI) - Ni(PIj) => Because Ni(PIj) = Ni(PI) - (Not assigned using i)
//			// This expression equals : Not assigned using i.
//			int notAssigned = 0;
//			for (int j=0; j<numClasses; j++) {
//				if(requirements[j][i]>0) {
//					notAssigned+=availableByClass[j];
//				}
//			}
//			
//			double dur = ratioPossibleTotal[i]*((double)notAssigned/carsLeft);
//			totalDur+=dur;
//		}
//		
//		return totalDur;
		
		double totalDur = 0;

		int lastAssigned = sol.getLastCar();
		
		int carsLeft = carsDemand-lastAssigned;
		
		for (int i=0; i<numOptions; i++) {
			if(requirements[lastAssigned][i]>0) {
				double dur = dynamicUtilizationRate(i, calculateRequiringLeft(sol, i))/carsLeft;
				totalDur+=dur;
			}
		}
		
		return totalDur;
	}
	
	private int calculateRequiringLeft(CSPSolution sol, int option) {
		int [] availableByClass = sol.getRemainingClasses();
		int requiringLeft = 0;
		
		for (int i=0; i<numClasses; i++) {
			if (requirements[i][option]>0) {
				requiringLeft+=availableByClass[option];
			}
		}
		
		return requiringLeft;
	}
	
	private double dynamicUtilizationRateMax(CSPSolution sol) {
		double totalDur = 0;

		int lastAssigned = sol.getLastCar();
		
		for (int i=0; i<numOptions; i++) {
			if(requirements[lastAssigned][i]>0) {
				double dur = staticUtilizationRate(i);
				totalDur+=dur;
			}
		}
		
		return totalDur;
	}
	
	public CSPSolution createGreedy(double alpha) {
		CSPSolution initial = createEmptySolution();
		CSPGreedyNeighbourhood neighbourhood = new CSPGreedyNeighbourhood(this,alpha);
		
		while(initial.getLastIndex()<carsDemand-1) {
//			initial = getMaxDurSum(initial, neighbourhood);
			initial = applyNext(initial, neighbourhood);
		}
		
		return initial;
	}
	
	private CSPSolution applyNext(CSPSolution sol, CSPGreedyNeighbourhood neighbourhood) {
		BinaryHeap minHeap = new BinaryHeap(true, FitnessBean.beanComparator());
		
		//Every possible new car
		List<AddCar> everyMove = neighbourhood.getEveryMove(sol);
//		int totalMoves = everyMove.size();
		
		
		//Check new violations
		
		for (AddCar move: everyMove) {
			move.apply(sol);
			double violations = evaluate(sol).getValue();
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
		
		
//		if(neighbourhood.getAlpha()!=0.0) {
//			List<AddCar> besties = neighbourhood.selectBesties(minHeap, totalMoves);
//			int howManyBesties = besties.size();
//			
//			AddCar move = besties.get(random.nextInt(howManyBesties));
//			move.apply(sol);
//			
//			return sol;
//			
//		} else {
//			FitnessBean fb = (FitnessBean)minHeap.pop();
//			
//			fb.move.apply(sol);
//			
//			return sol;
//		}	
		
	}
	
	private CSPSolution getMaxDurSum(CSPSolution sol, List<AddCar> moves, double alpha) {
		BinaryHeap bh = new BinaryHeap(false, FitnessBean.beanComparator());
		
		int totalMoves = moves.size();
		
		for (AddCar move: moves) {
			move.apply(sol);
//			double dur = dynamicUtilizationRateSum(sol);
//			double dur = dynamicUtilizationRateMax(sol);
			double dur = staticUtilizationRateSum(sol);
			move.undo(sol);
			bh.add(new FitnessBean(dur, move));
		}
		
		if(alpha!=0.0) {
			List<AddCar> besties = CSPGreedyNeighbourhood.selectBesties(bh, totalMoves, alpha);
			int howManyBesties = besties.size();
			
			AddCar move = besties.get(random.nextInt(howManyBesties));
			move.apply(sol);
			
			return sol;
			
		} else {
			FitnessBean fb = (FitnessBean)bh.pop();
			
			fb.move.apply(sol);
			
			return sol;
		}	
		
	}
	
//	private CSPSolution getMaxDurSum(CSPSolution sol, CSPGreedyNeighbourhood neighbourhood) {
//		BinaryHeap bh = new BinaryHeap(false, FitnessBean.beanComparator());
//		
//		List<AddCar> everyMove = neighbourhood.getEveryMove(sol);
//		int totalMoves = everyMove.size();
//		
//		for (AddCar move: everyMove) {
//			move.apply(sol);
//			double dur = dynamicUtilizationRateSum(sol);
//			move.undo(sol);
//			bh.add(new FitnessBean(dur, move));
//		}
//		
//		if(neighbourhood.getAlpha()!=0.0) {
//			List<AddCar> besties = CSPGreedyNeighbourhood.selectBesties(bh, totalMoves, neighbourhood.getAlpha());
//			int howManyBesties = besties.size();
//			
//			AddCar move = besties.get(random.nextInt(howManyBesties));
//			move.apply(sol);
//			
//			return sol;
//			
//		} else {
//			FitnessBean fb = (FitnessBean)bh.pop();
//			
//			fb.move.apply(sol);
//			
//			return sol;
//		}
//	}
	
	public Evaluation evaluate(CSPSolution sol) {		
//		int[] sequence = sol.getSequence();
//		
//		int lastIndex = sol.getLastIndex();
//		
//		int invalidCars = carsDemand-(lastIndex+1);
//		
//		if (invalidCars>0) {
//			double dur = dynamicUtilizationRateSum(sol);
//			
//			double fitness = ((double)carsDemand) / dur;
//			fitness+=baseError;
//			
//			return new SimpleEvaluation(fitness);
//		} else {
//			return new SimpleEvaluation(
//					evaluateRestrictions(sequence, carsDemand));
//		}
		
		int[] sequence = sol.getSequence();
		
		int lastIndex = sol.getLastIndex();

		return new SimpleEvaluation(
				evaluateRestrictions(sequence, lastIndex+1));
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
