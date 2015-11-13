package jcsp;

import util.Functions;

/**
 * Contains evaluation functions for CSP instances when using operators.
 * @author imoya
 *
 */
public class CSPEvaluation {

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
	private static int countCollisions(CSPProblem csp, int[] sequence, int beginIndexFirst,
			int endIndexFirst, int option, int[][] excess, int p, int q
		) {
		int currentColissions = 0;

		int[][] requirements = csp.getRequirements();
		
		for (int car=beginIndexFirst; car<=endIndexFirst; car++) {
			
			if(car+q>sequence.length) {
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
	public static double evalSwap(CSPProblem csp, int[] sequence, int first, 
			int second, double prevFitness, int[][] excess
		) {
		int firstClass = sequence[first];
		int secondClass = sequence[second];
		
		double fitness = prevFitness;
		
		int[][] requirements = csp.getRequirements();
		
		int carsDemand = csp.getCarsDemand();
		
		//For each options, the variation in the number of collisions 
		//is counted.
		for (int option=0; option<csp.getNumOptions(); option++) {
			
			//If both swapped cars contain the same option, no 
			//variation is done.
			if(requirements[firstClass][option]
					== requirements[secondClass][option]) {
				continue;
			}
			
			int p = csp.getP(option);
			int q = csp.getQ(option);
			
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
			
			int currentCollisionsFirst = countCollisions(csp, sequence, 
					beginIndexFirst, endIndexFirst, option, excess, p, q);

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

			int currentCollisionsSecond = countCollisions(csp, sequence, 
					beginIndexSecond, endIndexSecond, option, excess, p, q);
			
			fitness-= (prevCollisionsFirst-currentCollisionsFirst);
			
			fitness-= (prevCollisionsSecond-currentCollisionsSecond);
		}
		
		return fitness;
	}
	
	
	
	public static double evalInsert(CSPProblem csp, int[] sequence, int oldPos, 
			int newPos, double prevFitness, int[][] collisions
		) {
		double fitness = prevFitness;
		
		int carsDemand = csp.getCarsDemand();
		
		//For each options, the variation in the number of collisions 
		//is counted.
		for (int option=0; option<csp.getNumOptions(); option++) {
			
			int p = csp.getP(option);
			int q = csp.getQ(option);
			
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

			currentCollisionsNew = countCollisions(csp, sequence, 
					beginIndexNew, endIndexNew, option, collisions, p, q);

			if (oldPos<newPos) {
				if(canRotate) {
					currentCollisionsOld = countCollisions(csp, sequence, 
							beginIndexOld, endIndexOld-1, option, collisions, p, q);
				} else {
					currentCollisionsOld = countCollisions(csp, sequence, 
							beginIndexOld, endIndexOld, option, collisions, p, q);
				}

			} else {
				if(canRotate) {
					currentCollisionsOld = countCollisions(csp, sequence, 
							beginIndexOld+1, endIndexOld, option, collisions, p, q);
				} else {
					currentCollisionsOld = countCollisions(csp, sequence, 
							beginIndexOld, endIndexOld, option, collisions, p, q);
				}

			}

			fitness-= (prevCollisionsOld+prevCollisionsNew-currentCollisionsNew-currentCollisionsOld);
		}
		
		return fitness;
	}
	
	public static double evalInvert(CSPProblem csp, int[] sequence, int firstSequence, 
			int lastSequence, double prevFitness, int[][] collisions
		) {
		double fitness = prevFitness;
		int carsDemand = csp.getCarsDemand();
		
		//For each options, the variation in the number of collisions 
		//is counted.
		for (int option=0; option<csp.getNumOptions(); option++) {
			
			int p = csp.getP(option);
			int q = csp.getQ(option);
			
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
			
			int currentCollisions = countCollisions(csp, sequence, 
					beginSequence, endSequence, option, collisions, p, q);
			
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
	private static void rotateCollisions(int[] collisions, int from, int to, boolean right) {
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
}
