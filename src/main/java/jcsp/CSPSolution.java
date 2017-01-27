package jcsp;

import java.util.Arrays;
import java.util.Objects;

import org.apache.commons.lang3.ArrayUtils;
import org.jamesframework.core.problems.Solution;

import gnu.trove.list.array.TIntArrayList;
import util.functions.ArrayFunctions;
import util.functions.MatrixFunctions;

public class CSPSolution extends Solution{

	protected final CSPProblem csp;
	protected final int[] sequence;
	
	private int[] demandByClass;
	private int[] requiringByOption;
	private TIntArrayList availableClasses;
	
	private int lastIndex =-1;
	private int lastType = -1;

	//Evaluation
	protected double fitness = Double.MAX_VALUE;
	
	private int[] colissionsByOption;
	private int[][] tempColissionsByClassAndOption;
	
	private int [][] exceedByQ;
	private int [][] debugExceedByQ;
	
	private CSPSolution(
			int[] sequence,
			int[] demandByClass,
			CSPProblem csp
		) {
		super();
		this.sequence=sequence;
		this.demandByClass = demandByClass;
		this.csp=csp;
		
		requiringByOption = Arrays.copyOf(
				csp.getCarsRequiring(),csp.getNumOptions());
		
		int numClasses = csp.getNumClasses();
		availableClasses = new TIntArrayList(numClasses);
		for (int i=0; i<numClasses; i++) {
			availableClasses.add(i);
		}
		
		int numOptions = csp.getNumOptions();
		colissionsByOption = new int[numOptions];
		tempColissionsByClassAndOption = new int[numClasses][numOptions];
		debugExceedByQ = new int[numOptions][csp.getCarsDemand()];
	}
	
	public static CSPSolution createEmpty(
			int[] sequence,
			int[] availableClasses,
			CSPProblem csp
		) {
		return new CSPSolution(sequence, availableClasses, csp);
	}

	public CSPSolution(
			int[][] excess,
			CSPProblem csp,
			int[] sequence
		) {
		super();
		this.sequence=sequence;
		this.csp=csp;
		demandByClass = null;
		lastIndex =sequence.length-1;
		
		if(excess==null) {
			fullEvaluation();
		} else {
			exceedByQ = excess;
			fitness = MatrixFunctions.addMatrix(exceedByQ);
		}
	}
	
	@Override
	public Solution copy() {
		return new CSPSolution(
				MatrixFunctions.copyMatrix(exceedByQ), 
					csp, Arrays.copyOf(sequence, sequence.length));
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CSPSolution other = (CSPSolution) obj;
        return Arrays.equals(this.sequence, other.sequence);
	}

	@Override
	public int hashCode() {
		int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.sequence);
        return hash;
	}
	
	//EVALUATION
	
	public void fullEvaluation() {

		exceedByQ = csp.createExcessMatrix(sequence);
		fitness = MatrixFunctions.addMatrix(exceedByQ);
	}
	
	// MOVE FUNCTIONALITY
	
	public void swap(int i, int j) {
		int tmp = sequence[i];
		sequence[i]=sequence[j];
		sequence[j]=tmp;
		
		fitness = CSPEvaluation.evalSwap(csp, sequence, 
				i, j, fitness, exceedByQ
					);
	}
	
	public void insert(int oldPos, int newPos) {
		int type = sequence[oldPos];
		//Inserting bellow
		if(newPos<oldPos) {
			//Move up
			for (int i=oldPos; i>newPos; i--) {
				sequence[i]=sequence[i-1];
			}
		} else {
			//Inserting over
			//Move down
			for (int i=oldPos; i<newPos; i++) {
				sequence[i]=sequence[i+1];
			}
		}
		sequence[newPos] = type;
		
		fitness = CSPEvaluation.evalInsert(csp, sequence, 
				oldPos, newPos, fitness, exceedByQ
					);
	}
	
	/**
	 * End is inclusive.
	 * @param subsequence
	 * @param begin
	 * @param end
	 */
	public void restore(int [] subsequence, int begin, int end) {
		for (int i=0;  i<subsequence.length; i++) {
			sequence[i+begin] = subsequence[i];
		}
		fullEvaluation();
	}
	
	/**
	 * End is inclusive.
	 * @param begin
	 * @param end
	 */
	public void shuffle(int begin, int end) {
		ArrayFunctions.partialShuffle(sequence, csp.random, begin, end);
		fullEvaluation();
	}
	
	
	public void invert(int begin, int end) {
		//reverse uses the last position as a not inclusive one.
		ArrayUtils.reverse(sequence,begin,end+1);
		
		fitness = CSPEvaluation.evalInvert(csp, sequence, 
				begin, end, fitness, exceedByQ
					);
	}
	
	public void addCar(int typeClass) {
		if(lastIndex==sequence.length-1)
			throw new IllegalStateException(
					"No more cars can be scheduled:Sequence is already full.");
		lastIndex++;
		sequence[lastIndex]=typeClass;
		
		demandByClass[typeClass]--;
		
		if(demandByClass[typeClass]==0) {
			disableCarClass(typeClass);
		} else if(demandByClass[typeClass]<0 || !availableClasses.contains(typeClass)) {
			throw new IllegalArgumentException(
					"Illegal class.");
		}
		
		lastType=typeClass;

		
		fitness = 0;
		
		int[][] requirements = csp.getRequirements();
		for (int r=0; r<csp.getNumOptions(); r++) {
			if(requirements[typeClass][r]>0) {
				requiringByOption[r]--;
			}
			//Update Options
			colissionsByOption[r] = tempColissionsByClassAndOption[typeClass][r];
			//Update fitness
			fitness+=colissionsByOption[r];
			debugExceedByQ[r][lastIndex]=colissionsByOption[r];
		}
	}
	
	public int[] checkPosition(int pos) {
		if(lastIndex+1<pos) {
			throw new IllegalArgumentException(
					"Car checking should skip no position.");
		}
		int[] fitness = new int[csp.getNumClasses()];
		Arrays.fill(fitness, Integer.MAX_VALUE);
		
		int[] classes = availableClasses.toArray();
		
		for (int car : classes) {
			fitness[car] = checkClassAtPosition(car,pos);
		}
		
		return fitness;
	}
	
	private void disableCarClass(int carClass) {
		if(!availableClasses.remove(carClass)) {
			throw new IllegalArgumentException(
					"Given class was already removed: "+carClass);
		}
	}
	
	public int checkClassAtPosition(int carClass, int pos) {
		int colissions = 0;
		
		final int[][] requirements = csp.getRequirements();
		
		for (int o = 0; o<csp.getNumOptions(); o++) {
			int currentColissionsForOption = colissionsByOption[o];
			
			if(requirements[carClass][o]>0) {
				final int p = csp.getP(o);
				final int q = csp.getQ(o);
				
				int occurrences = 1;
				
				int i=1;
				while (i<q && pos-i>=0) {
					occurrences+=requirements[sequence[pos-i]][o];
					i++;
				}
				
				if (occurrences>p) {
					int newColissions = occurrences-p;
					currentColissionsForOption+=newColissions;
				}
			}
			
			tempColissionsByClassAndOption[carClass][o] = currentColissionsForOption;
			colissions += currentColissionsForOption;
		}
		
		return colissions;
	}
	
	public double[] checkHeuristicValues(int [] classes, int position) {
		
		int filteredClasses = classes.length;
		double[] heuristicValues = new double [filteredClasses];
		
		for (int i = 0; i<filteredClasses; i++) {
			heuristicValues[i] = csp.dynamicUtilizationRateSum(
					requiringByOption, csp.getCarsDemand()-position, i);
		}
		
		return heuristicValues;
	}

	// GETTERS & SETTERS

	public CSPProblem getProblem() {
		return csp;
	}
	
	public double getFitness() {
		return fitness;
	}

	public int[] getSequence() {
		return sequence;
	}
	
	public int getSecuenceLength() {
		return sequence.length;
	}
	
	public int getLastIndex() {
		return lastIndex;
	}
	
	public int getLastType() {
		return lastType;
	}
	
	public int[] getRemainingClasses() {
		return demandByClass;
	}
	
	public TIntArrayList getAvailableClasses() {
		return availableClasses;
	}
	
	public int[][] getColissionMatrix() {
		return exceedByQ;
	}
	
	public int[] getRequiring() {
		return requiringByOption;
	}

	@Override
	public String toString() {
		return "CSPSolution [sequence=" + Arrays.toString(sequence) + "]";
	}
}
