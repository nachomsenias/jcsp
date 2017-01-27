package jcsp.robust;

import java.util.Arrays;

import org.jamesframework.core.problems.constraints.validations.SimpleValidation;
import org.jamesframework.core.problems.constraints.validations.Validation;

import jcsp.CSPProblem;
import jcsp.CSPSolution;
import util.functions.ArrayFunctions;
import util.random.Randomizer;
import util.random.RandomizerFactory;
import util.random.RandomizerFactory.RandomizerAlgorithm;
import util.random.RandomizerUtils;

public class RobustCSPProblem extends CSPProblem {
	
	public final static int BASE_MC = 15;
	public final static int ROBUST_CLASS = -1;

	private int numMC;
	
	private int numSpecial;
	
	private int demandSpecial;
	
	private int numAlternatePlans;
	
	private int[][] requirementsSpecial;
	
	private int[][] productionPlans;
	
	// MC Plan Pos
	private int[][][] robustPositionsByPlan;
	
	public RobustCSPProblem(int carsDemand, int numOptions, int numClasses, 
			int[][] options, int[][] requirements, int[] demandByClasses, 
			int numSpecial, int demandSpecial, int[][] requirementsSpecial, 
			int numAlternatePlans, int[][] productionPlans) {
		super(carsDemand, numOptions, numClasses, options, requirements, demandByClasses);
		this.numSpecial = numSpecial;
		this.demandSpecial=demandSpecial;
		this.requirementsSpecial=requirementsSpecial;
		this.numAlternatePlans=numAlternatePlans;
		this.productionPlans = productionPlans;
		
		//Generate robust positions
		numMC=BASE_MC;
		robustPositionsByPlan = createRobustPositions(numMC);
	}
	
	@Override
	public double evaluateRestrictions(int[] sequence, int lastIndex) {
		double fitness =0;
		int robustIndex = 0;
		
		for (int car=0; car<lastIndex; car++) {
			
			int carClass = sequence[car];
			
			if(carClass==ROBUST_CLASS) {
				//Special kar
				
				fitness+=calculateRobustCollisions(car, sequence, robustIndex);
				
				robustIndex++;
			} else {
				//Normal car
				for (int option=0; option<numOptions; option++) {
					
					if(requirements[carClass][option] == 0) {
						continue;
					}
					
					// Q
					int total = this.options[TOTAL_INDEX][option];
					// P
					int possible = this.options[POSSIBLE_INDEX][option];

					//Calculate Normal
					fitness+= calculateNormalCollisions(car, option, sequence, 
							total, possible, robustIndex);
				}
			}
		}
		
		return fitness;
	}
	
	private double calculateRobustCollisions(int carPos, 
			int[] sequence, int robustIndex){
		double robustColissions = 0;
		for (int option=0; option<numOptions; option++) {
			// Q
			int total = this.options[TOTAL_INDEX][option];
			// P
			int possible = this.options[POSSIBLE_INDEX][option];
			/*
			 * When the collisions for a special car are calculated, 
			 * the following special slots are taken from the simulated
			 * schedule.
			 */
			for (int mc = 0; mc< numMC; mc++) {
				for (int plan =0; plan<numAlternatePlans; plan++) {
					
					int initialSpecialCar = robustPositionsByPlan[mc][plan][robustIndex];
					
					if(requirementsSpecial[initialSpecialCar][option] != 0) {
					
						int nextCar = 1;
						double occurrences = 1;
						int nextRobust = robustIndex+1;
						
						while(nextCar<total && carPos+nextCar < carsDemand) {
							
							int carClass = sequence[carPos+nextCar];
							
							if (carClass == ROBUST_CLASS) {
								//Special Kar
										
								int nextSpecialCar = robustPositionsByPlan[mc][plan][nextRobust];
								
								if(requirementsSpecial[nextSpecialCar][option] != 0) {
									occurrences++;
								}

								nextRobust++;
							} else {
								//Normal car
								occurrences+=requirements[sequence[carPos+nextCar]][option];
							}
							
							nextCar++;
						}
						
						//P+
						if (occurrences>possible) {
							robustColissions+=occurrences-possible;
						} 
					}
				}
			}
		}
		
		robustColissions /= (numMC*numAlternatePlans);
		
		return robustColissions;
	}
	
	private double calculateNormalCollisions(int carPos, int option, 
			int[] sequence, int total, int possible, int robustIndex){
		
		int nextCar = 1;
		double occurrences = 1;
		
		while(nextCar<total && carPos+nextCar < carsDemand) {
			
			int carClass = sequence[carPos+nextCar];
			
			/*
			 * When a special car is found, its collisions are simulated.
			 */
			if (carClass == ROBUST_CLASS) {
				//Special Kar
				double specialOcurrences = 0;
				for (int mc = 0; mc< numMC; mc++) {
					for (int plan =0; plan<numAlternatePlans; plan++) {
						
						int specialCar = robustPositionsByPlan[mc][plan][robustIndex];
						
						if(requirementsSpecial[specialCar][option] != 0) {
							specialOcurrences++;
						}
					}
				}
				
				specialOcurrences /= (numMC*numAlternatePlans);
				
				occurrences+=specialOcurrences;
				robustIndex++;
			} else {
				//Normal car
				occurrences+=requirements[sequence[carPos+nextCar]][option];
			}
			
			nextCar++;
		}
		
		//P+
		if (occurrences>possible) {
			return occurrences-possible;
		} else return 0;
	}
	
	private int[][][] createRobustPositions(int numMC) {
		int[][][] robustPositions = new int [numMC][numAlternatePlans][demandSpecial];
		
		for (int mc = 0; mc< numMC; mc++) {
			Randomizer random = RandomizerFactory.createRandomizer(
					RandomizerAlgorithm.XOR_SHIFT_128_PLUS_FAST,
					RandomizerUtils.PRIME_SEEDS[mc]
							);
			for (int plan = 0; plan<numAlternatePlans; plan++) {
				
				int[] assigned = new int[numSpecial];
				for  (int pos = 0; pos<demandSpecial; pos++) {
					
					int robustClass = -1;
					do
					{
						robustClass = random.nextInt(numSpecial);
					}while(assigned[robustClass]>=productionPlans[plan][robustClass]);
					
					assigned[robustClass]++;
					robustPositions[mc][plan][pos] = robustClass;
				}
			}
		}
		
		return robustPositions;
	}
	
	//Solution genration
	
	public CSPSolution createRandomSolution() {
		int [] sequence = new int [carsDemand];
		
		Arrays.fill(sequence, 0, demandSpecial, -1);
		
		int classByDemand = 0;
		int ocurrences = 0;

		for (int i=demandSpecial; i<carsDemand; i++) {
			sequence[i] = classByDemand;
			ocurrences++;
			
			if(ocurrences>=demandByClasses[classByDemand]) {
				classByDemand++;
				ocurrences=0;
			}
		}
		
		ArrayFunctions.shuffleArrayFast(sequence, random);
		
		return new RobustCSPSolution(this,sequence);
	}
	
	//Validation
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
		
		int foundSpecialDemand = 0;
		for (int ocurrence : sequence) {
			if(ocurrence == ROBUST_CLASS) {
				foundSpecialDemand++;
			}else ocurrences[ocurrence]++;
		}
		
		if(Arrays.equals(ocurrences, demandByClasses) 
				&& foundSpecialDemand == demandSpecial) {
			return SimpleValidation.PASSED;
		} else {
			return SimpleValidation.FAILED;
		}

	}
	
	//Getters & Setters
	public int getNumSpecial() {
		return numSpecial;
	}

	public void setNumSpecial(int numSpecial) {
		this.numSpecial = numSpecial;
	}

	public int[][] getRequirementsSpecial() {
		return requirementsSpecial;
	}

	public void setRequirementsSpecial(int[][] requirementsSpecial) {
		this.requirementsSpecial = requirementsSpecial;
	}

	public int getNumAlternatePlans() {
		return numAlternatePlans;
	}

	public void setNumAlternatePlans(int numAlternatePlans) {
		this.numAlternatePlans = numAlternatePlans;
	}

	public int getDemandSpecial() {
		return demandSpecial;
	}

	public void setDemandSpecial(int demandSpecial) {
		this.demandSpecial = demandSpecial;
	}

	public int[][] getProductionPlans() {
		return productionPlans;
	}

	public void setProductionPlans(int[][] productionPlans) {
		this.productionPlans = productionPlans;
	}
}
