package jcsp.robust;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gnu.trove.list.array.TIntArrayList;
import jcsp.CSPProblem;
import jcsp.CSPSolution;
import jcsp.util.functions.ArrayFunctions;
import jcsp.util.random.Randomizer;
import jcsp.util.random.RandomizerFactory;
import jcsp.util.random.RandomizerFactory.RandomizerAlgorithm;
import jcsp.util.random.RandomizerUtils;

public class RobustnessEvaluator {
	
	public final static int BASE_MC = 30;
	
	private int numMC;
	
	private int numSpecial;
	
	private int[][] productionPlans;
	
	private CSPProblem csp;
	
	private class SequenceBean {
		int classId;
		// > 0 => Reference Plan > Custom Plan => Remove Car
		// < 0 => Reference Plan < Custom Plan => Add Car
		int diff;
		public SequenceBean(int classId, int diff) {
			super();
			this.classId = classId;
			this.diff = diff;
		}
		@Override
		public String toString() {
			return "SequenceBean [classId=" + classId + ", diff=" + diff + "]";
		}
	}

	public RobustnessEvaluator(CSPProblem csp,
				int numSpecial, int[][] productionPlans) {
		//Robustness
		numMC = BASE_MC;
		this.numSpecial = numSpecial;
		this.productionPlans = productionPlans;
		this.csp=csp;
		
		if(productionPlans[0].length!=numSpecial)
			throw new IllegalArgumentException("Mismatch with special classes.");
	}

	/**
	 * Evaluates the robustness of the given solution using the alternate
	 * production plans from the given special classes.
	 * 
	 * @param solution solution to be evaluated.
	 * @return the robustness of the given solution.
	 */
	public RobustnessResult evaluateRobustness(CSPSolution solution) {
		int numPlans = productionPlans.length;
		
		//Variables for computing robustness
		double totalRobustness = 0;
		double minRobustness = Double.MAX_VALUE;
		
		double[] avgRobustnessByPlan = new double [numPlans];
		double[] minRobustnessByPlan = new double [numPlans];
		Arrays.fill(minRobustnessByPlan, Double.MAX_VALUE);
		
		//Random sequences based on alternate plans
		int[][][] alternateSequences = generateAlternateSequences(solution.getSequence());
		
		for (int p=0; p<numPlans; p++) {
			for (int mc = 0; mc<numMC; mc++) {
				int[] sequence = alternateSequences[p][mc];
				
				final double robustness=csp.evaluateRestrictions(sequence, sequence.length);
				totalRobustness+=robustness;
				avgRobustnessByPlan[p]+=robustness;
				
				if(robustness<minRobustnessByPlan[p]) {
					minRobustnessByPlan[p]=robustness;
					
					if(robustness<minRobustness) {
						minRobustness=robustness;
					}
				}
				
			}
		}
		
		for (int p=0; p<numPlans; p++) {
			avgRobustnessByPlan[p]/=numMC;
		}
		
		totalRobustness/= (numPlans*numMC);
		
		return new RobustnessResult(totalRobustness, minRobustness, 
				avgRobustnessByPlan, minRobustnessByPlan, solution.getSequence());
	}
	
	private int[][][] generateAlternateSequences(int[] initialSequence) {
		int numPlans = productionPlans.length;
//		int totalAlternateSequences = numMC * numPlans;
		
		int[][][] alternateSequences = new int [numPlans][numMC][];
		
//		int sequenceIndex = 0;
		
		for (int mc = 0; mc<numMC; mc++) {
			Randomizer random = RandomizerFactory.createRandomizer(
					RandomizerAlgorithm.XOR_SHIFT_128_PLUS_FAST,
					RandomizerUtils.PRIME_SEEDS[mc]);
			
			for (int p=0; p<numPlans; p++) {
				List<SequenceBean> beanList = getSequenceBeans(p);
				alternateSequences[p][mc] 
						= getNewSequence(beanList, initialSequence, random);
			}
		}
		
		return alternateSequences;
	}
	
	private List<SequenceBean> getSequenceBeans(int planId) {
		List<SequenceBean> beanList = new ArrayList<SequenceBean>();
		
		int totalClasses = csp.getNumClasses();
		int firstSpecialClass = totalClasses-numSpecial;
		
		int[] referenceDemand=csp.getDemandByClasses();
		
		for (int s=0; s<numSpecial; s++) {
			int initialDemand = referenceDemand[s+firstSpecialClass];
			int planDemand = productionPlans[planId][s];
			
			if(initialDemand!=planDemand) {
				beanList.add(new SequenceBean(s+firstSpecialClass,initialDemand-planDemand));
			}
		}
		
		return beanList;
	}
	
	private int[] getNewSequence(List<SequenceBean> beanList, int[] originalSequence, Randomizer random) {
		TIntArrayList gainers = new TIntArrayList(numSpecial);
		TIntArrayList loosers = new TIntArrayList(numSpecial);
		TIntArrayList toInclude = new TIntArrayList(numSpecial);
		TIntArrayList toRemove = new TIntArrayList(numSpecial);
		
		for (SequenceBean bean : beanList) {
			if(bean.diff>0) {
				loosers.add(bean.classId);
				toRemove.add(bean.diff);
			} else {
				gainers.add(bean.classId);
				toInclude.add(-bean.diff);
			}
		}
		
		int[] newSequence = Arrays.copyOf(originalSequence,originalSequence.length);
		
		while(!gainers.isEmpty() && !loosers.isEmpty()) {
			
			int gainerIndex = random.nextInt(gainers.size());
			int classToInclude = gainers.get(gainerIndex);
			
			int looserIndex = random.nextInt(loosers.size());
			int classToRemove = loosers.get(looserIndex);
			
			int[] appereancesOfLooser = ArrayFunctions.getAllIndexesOf(newSequence, classToRemove);
			int randomAppereance = random.nextInt(appereancesOfLooser.length);
			int position=appereancesOfLooser[randomAppereance];
			
			newSequence[position] = classToInclude;
			
			//Check remaining
			int remainingToInclude = toInclude.get(gainerIndex);
			if(remainingToInclude>1) {
				toInclude.set(gainerIndex, remainingToInclude-1);
			} else {
				toInclude.removeAt(gainerIndex);
				gainers.removeAt(gainerIndex);
			}
			
			int remainingToRemove = toRemove.get(looserIndex);
			if(remainingToRemove>1) {
				toRemove.set(looserIndex, remainingToRemove-1);
			} else {
				toRemove.removeAt(looserIndex);
				loosers.removeAt(looserIndex);
			}
		}
		
		return newSequence;
	}
	
	// GETTERS & SETTERS 

	public CSPProblem getCsp() {
		return csp;
	}
	
}
