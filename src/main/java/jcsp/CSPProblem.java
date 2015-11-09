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
	
	private static final byte POSSIBLE_FROM = 0;
	private static final byte TOTAL_INDEX = 1;
	
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
			ratioPossibleTotal[i] = (double) options[0][i] / (double) options[1][i];
			
			for (int j=0; j<numClasses; j++) {
				if(requirements[j][i]>0) {
					carsRequiring[i]+=demandByClasses[i];
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
		
		return new CSPSolution(
				sequence,
					Arrays.copyOf(demandByClasses, numClasses),
						this);
	}

	/**
	 * This method evaluates a modified sequence using swap at given 
	 * indexes.
	 * 
	 * Given fitness and excess corresponds to the sequence prior to 
	 * the application of the operator.
	 * 
	 * @param sequence
	 * @param first
	 * @param second
	 * @param prevFitness
	 * @param excess
	 * @return
	 */
	public double evalSwap(int[] sequence, int first, int second, 
			double prevFitness, int[][] excess
		) {
		int firstClass = sequence[first];
		int secondClass = sequence[second];
		
		double fitness = prevFitness;
		
		//For each options, the variation in the number of collisions 
		//is counted.
		for (int option=0; option<numOptions; option++) {
			
			//If both swapped cars contain the same option, no 
			//variation is done.
			if(requirements[firstClass][option]
					== requirements[secondClass][option]) {
				continue;
			}
			
			int q = this.options[TOTAL_INDEX][option];
			
			int beginIndexFirst;
			if(first-(q-1)<0) {
				beginIndexFirst = 0;
			} else {
				beginIndexFirst = first-(q-1);
			}
			
			int endIndexFirst;
			if(first+(q-1)<=carsDemand) {
				endIndexFirst = first;
			} else {
				endIndexFirst = carsDemand-q;
			}
			
			int prevCollisionsFirst=Functions.addArraySegment(
					excess[option], beginIndexFirst, endIndexFirst+1);
			
			int currentCollisionsFirst = countCurrentCollisions(sequence, 
					beginIndexFirst, endIndexFirst, option, excess, q);

			int beginIndexSecond;
			if(second-(q-1)<0) {
				beginIndexSecond = 0;
			} else {
				beginIndexSecond = second-(q-1);
			}
			
			int endIndexSecond;
			if(second+(q-1)<carsDemand) {
				endIndexSecond = second;
			} else {
				endIndexSecond = carsDemand-q;
			}
			
			if (beginIndexFirst<beginIndexSecond 
					&& beginIndexSecond<first) {
				beginIndexSecond = endIndexFirst+1;
			} else if(beginIndexSecond<beginIndexFirst 
					&& beginIndexFirst<second) {
				endIndexSecond = beginIndexFirst-1;
			}

			int prevCollisionsSecond=Functions.addArraySegment(
					excess[option], beginIndexSecond, endIndexSecond+1);

			int currentCollisionsSecond = countCurrentCollisions(sequence, 
					beginIndexSecond, endIndexSecond, option, excess, q);
			
			fitness-= (prevCollisionsFirst-currentCollisionsFirst);
			
			fitness-= (prevCollisionsSecond-currentCollisionsSecond);
		}
		
		return fitness;
	}
	
	public double evalInsert(int[] sequence, int oldPos, int newPos, 
			double prevFitness, int[][] collisions
		) {
		
		double fitness = prevFitness;
		
		//For each options, the variation in the number of collisions 
		//is counted.
		for (int option=0; option<numOptions; option++) {
			
			int q = this.options[TOTAL_INDEX][option];
			
			
			boolean canRotate = true;
			
			int beginIndexOld;
			if(oldPos-(q-1)<0) {
				beginIndexOld = 0;
			} else {
				beginIndexOld = oldPos-(q-1);
			}
			
			int endIndexOld;
			if(oldPos+(q-1)<carsDemand) {
				endIndexOld = oldPos;
			} else {
				endIndexOld = carsDemand-q;
			}

			int beginIndexNew;
			if(newPos-(q-1)<0) {
				beginIndexNew = 0;
			} else {
				beginIndexNew = newPos-(q-1);
			}
			
			int endIndexNew;
			if(newPos+(q-1)<carsDemand) {
				endIndexNew = newPos;
			} else {
				endIndexNew = carsDemand-q;
			}
			
			if (endIndexOld<endIndexNew 
					&& beginIndexNew<oldPos) {
				beginIndexNew = endIndexOld+1;
				canRotate=false;
			} else if(endIndexNew<endIndexOld 
					&& beginIndexOld<newPos) {
				endIndexNew = beginIndexOld-1;
				canRotate=false;
			} else if (endIndexNew==endIndexOld) {
				canRotate=false;
				if(beginIndexOld<beginIndexNew) {
					endIndexOld=beginIndexNew-1;
				} else {
					endIndexNew = beginIndexOld-1;
				}
			}

			int prevCollisionsOld,currentCollisionsOld;
			int prevCollisionsNew,currentCollisionsNew;

			prevCollisionsOld=Functions.addArraySegment(
					collisions[option], beginIndexOld, endIndexOld+1);

			if (oldPos<newPos) {

				if(canRotate) {
					
					rotateCollisions(collisions[option], endIndexOld, beginIndexNew, false);
					//b+1 => e
					prevCollisionsNew=Functions.addArraySegment(
							collisions[option], beginIndexNew+1, endIndexNew+1);
				} else {
					prevCollisionsNew=Functions.addArraySegment(
							collisions[option], beginIndexNew, endIndexNew+1);
				}
				
			} else {

				if(canRotate) {

					rotateCollisions(collisions[option], endIndexNew, beginIndexOld, true);
					//nb => e-1
					prevCollisionsNew=Functions.addArraySegment(
							collisions[option], beginIndexNew, endIndexNew);
				}else {
					prevCollisionsNew=Functions.addArraySegment(
							collisions[option], beginIndexNew, endIndexNew+1);
				}

			}

			currentCollisionsNew = countCurrentCollisions(sequence, 
					beginIndexNew, endIndexNew, option, collisions, q);

			if (oldPos<newPos) {
				if(canRotate) {
					currentCollisionsOld = countCurrentCollisions(sequence, 
							beginIndexOld, endIndexOld-1, option, collisions, q);
				} else {
					currentCollisionsOld = countCurrentCollisions(sequence, 
							beginIndexOld, endIndexOld, option, collisions, q);
				}

			} else {
				if(canRotate) {
					currentCollisionsOld = countCurrentCollisions(sequence, 
							beginIndexOld+1, endIndexOld, option, collisions, q);
				} else {
					currentCollisionsOld = countCurrentCollisions(sequence, 
							beginIndexOld, endIndexOld, option, collisions, q);
				}

			}

			fitness-= (prevCollisionsOld+prevCollisionsNew-currentCollisionsNew-currentCollisionsOld);
		}
		
		return fitness;
	}
	
	public double evalInvert(int[] sequence, int firstSequence, int lastSequence, 
			double prevFitness, int[][] collisions
		) {
		double fitness = prevFitness;
		
		//For each options, the variation in the number of collisions 
		//is counted.
		for (int option=0; option<numOptions; option++) {
			
			int q = this.options[TOTAL_INDEX][option];
			
			int beginSequence;
			if(firstSequence-(q-1)<0) {
				beginSequence = 0;
			} else {
				beginSequence = firstSequence-(q-1);
			}

			int endSequence;
			if(lastSequence+(q-1)<carsDemand) {
				endSequence = lastSequence;
			} else {
				endSequence = carsDemand-q;
			}
			
			int prevCollisions = Functions.addArraySegment(
					collisions[option], beginSequence, endSequence+1);
			
			int currentCollisions = countCurrentCollisions(sequence, 
					beginSequence, endSequence, option, collisions, q);
			
			fitness -= (prevCollisions-currentCollisions);
		}
		return fitness;
	}
	
	/**
	 * Both from and to are inclusive!!
	 * @param collisions
	 * @param from
	 * @param to
	 * @param right
	 */
	private void rotateCollisions(int[] collisions, int from, int to, boolean right) {
		if(right) {
			for (int i=to; i>from; i--) {
				collisions[i]=collisions[i-1];
			}
		} else {
			for (int i=from; i<to; i++) {
				collisions[i]=collisions[i+1];
			}
		}
	}
	
	/**
	 * Both indexes are inclusive.
	 * @param sequence
	 * @param beginIndexFirst
	 * @param endIndexFirst
	 * @param option
	 * @param excess
	 * @param q
	 * @return
	 */
	private int countCurrentCollisions(int[] sequence, int beginIndexFirst,
			int endIndexFirst, int option, int[][] excess, int q
		) {
		int currentColissions = 0;
		
		int p = this.options[POSSIBLE_FROM][option];
		
		for (int car=beginIndexFirst; car<=endIndexFirst; car++) {
			
			if(car+q>carsDemand) {
				break;
			}
			
			int occurrences = 0;
			
			int nextCar = 0;
			while(nextCar<q) {
				occurrences+=requirements[sequence[car+nextCar]][option];
				nextCar++;
			}
			
			//P+
			if (occurrences>p) {
				int collisions = occurrences-p;
				excess[option][car]=collisions;
				currentColissions+=collisions;
			} else {
				excess[option][car]=0;
			}
		}
		
		//New fitness is equal to previous fitness minus the different 
		//between previous and the current collisions. 
		
		//If new collisions are fewer than the previous ones, the 
		//fitness is lower (better).
		return currentColissions;
	}
	
	public int[][] createExcessMatrix(int[] sequence) {
		int[][] excesses = new int [numOptions][carsDemand];
		
		for (int car=0; car<carsDemand; car++) {
			for (int option=0; option<numOptions; option++) {
				
				int total = this.options[TOTAL_INDEX][option];
				int possible = this.options[POSSIBLE_FROM][option];
				
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
	
	private double evaluateRestrictions(int[] sequence, int lastIndex) {
		double fitness =0;
		
		for (int car=0; car<lastIndex; car++) {
			for (int option=0; option<numOptions; option++) {
				
				int total = this.options[TOTAL_INDEX][option];
				int possible = this.options[POSSIBLE_FROM][option];
				
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
					fitness+=occurrences-possible;
				}
				
			}
		}
		
		return fitness;
	}
	
	
	private double staticUtilizationRate(int option) {
		double sur = (carsRequiring[option] * ratioPossibleTotal[option])/carsDemand;
		
		return sur;
	}
	
	private double dynamicUtilizationRate(int option, int currentRequiring) {
		double dur = currentRequiring * ratioPossibleTotal[option];
		
		return dur;
	}
	
	public double staticUtilizationRateSum(int lastAssigned) {
		double totalDur = 0;
		
		for (int i=0; i<numOptions; i++) {
			if(requirements[lastAssigned][i]>0) {
				double dur = staticUtilizationRate(i);
				totalDur+=dur;
			}
		}
		
		return totalDur;
	}
	
	@SuppressWarnings("unused")
	private double dynamicUtilizationRateSum(CSPSolution sol) {
		
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
		BinaryHeap bh = new BinaryHeap(false, FitnessBean.beanComparator());
		
		int totalMoves = moves.size();
		
		for (AddCar move: moves) {
			move.apply(sol);
			double dur = staticUtilizationRateSum(sol.getLastCar());
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
	
	public Evaluation evaluate(CSPSolution sol) {
		
		int lastIndex = sol.getLastIndex();
		
		if(lastIndex!=carsDemand-1) {
			int[] sequence = sol.getSequence();
			return new SimpleEvaluation(
					evaluateRestrictions(sequence, lastIndex+1));
		} else {
			return new SimpleEvaluation(
					sol.getFitness());
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
	
	public int[] getDemandByClasses() {
		return demandByClasses;
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
	
	public int[] getQs() {
		return options[TOTAL_INDEX];
	}
}
