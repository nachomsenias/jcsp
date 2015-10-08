package jcsp;

import java.util.Arrays;
import java.util.Objects;

import org.apache.commons.lang3.ArrayUtils;
import org.jamesframework.core.problems.Solution;

import util.Functions;

public class CSPSolution extends Solution{

	private final CSPProblem csp;
	private final int[] sequence;
	
	private int[] availableClasses;
	
	private int lastIndex =-1;
	private int lastType = -1;
	private int prevType = -1;

	//Evaluation
	private double fitness = Double.MAX_VALUE;
	
	private int [][] exceedByQ;
	
	
	public CSPSolution(
			int[][] excess,
			CSPProblem csp,
			int[] sequence
		) {
		super();
		this.sequence=sequence;
		this.csp=csp;
		availableClasses = null;
		lastIndex =sequence.length-1;
		
		if(excess==null) {
			fullEvaluation();
		} else {
			exceedByQ = excess;
			fitness = Functions.addMatrix(exceedByQ);
		}
	}
	
	public CSPSolution(
			int[] sequence,
			int[] availableClasses,
			CSPProblem csp
		) {
		super();
		this.sequence=sequence;
		this.availableClasses = availableClasses;
		this.csp=csp;
	}

	@Override
	public Solution copy() {
		return new CSPSolution(
				Functions.copyMatrix(exceedByQ), 
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
		fitness = Functions.addMatrix(exceedByQ);
	}
	
	// MOVE FUNCTIONALITY
	
	public void swap(int i, int j) {
		int tmp = sequence[i];
		sequence[i]=sequence[j];
		sequence[j]=tmp;
		
		fitness = csp.evalSwap(
				sequence, i, j, 
				fitness, exceedByQ
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
		
		fitness = csp.evalInsert(
				sequence, oldPos, newPos, 
				fitness, exceedByQ
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
	}
	
	/**
	 * End is inclusive.
	 * @param begin
	 * @param end
	 */
	public void shuffle(int begin, int end) {
		Functions.partialShuffle(sequence, CSPProblem.random, begin, end);
	}
	
	
	public void invert(int begin, int end) {
		//reverse uses the last position as a not inclusive one.
		ArrayUtils.reverse(sequence,begin,end+1);
		
		fitness = csp.evalInvert(sequence, begin, end, fitness, exceedByQ);
	}
	
	public void addCar(int typeClass) {
		if(lastIndex==sequence.length-1)
			throw new IllegalStateException(
					"No more cars can be scheduled:Sequence is already full.");
		lastIndex++;
		prevType = sequence[lastIndex];
		sequence[lastIndex]=typeClass;
		
		availableClasses[typeClass]--;
		lastType=typeClass;
	}
	
	public void removeLast() {
		sequence[lastIndex]=prevType;
		lastIndex--;
		availableClasses[lastType]++;
	}

	// GETTERS & SETTERS

	public int getNumClasses() {
		return csp.getNumClasses();
	}
	
	public double getFitness() {
		return fitness;
	}

	public void setCurrentFitness(double currentFitness) {
		this.fitness = currentFitness;
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
	
	public int getLastCar() {
		return sequence[lastIndex];
	}
	
	public int[] getRemainingClasses() {
		return availableClasses;
	}
	
	public int[][] getColissionMatrix() {
		return exceedByQ;
	}

	@Override
	public String toString() {
		return "CSPSolution [sequence=" + Arrays.toString(sequence) + "]";
	}
}
