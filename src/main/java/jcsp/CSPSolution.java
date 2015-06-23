package jcsp;

import java.util.Arrays;
import java.util.Objects;

import org.jamesframework.core.problems.Solution;

public class CSPSolution extends Solution{

	private final int[] sequence;
	private final int numClasses;
	
	private int[] availableClasses;
	
	private int lastIndex =-1;
	private int lastType = -1;
	private int prevType = -1;
	
	private boolean building = true;
	
	public CSPSolution(
			int[] sequence,
			int numClasses
		) {
		super();
		this.sequence=sequence;
		this.numClasses=numClasses;
		availableClasses = null;
		lastIndex =sequence.length-1;
	}
	
	public CSPSolution(
			int[] sequence,
			int numClasses,
			int[] availableClasses
		) {
		super();
		this.sequence=sequence;
		this.numClasses=numClasses;
		this.availableClasses = availableClasses;
	}

	@Override
	public Solution copy() {
		return new CSPSolution(
				Arrays.copyOf(sequence, sequence.length), numClasses);
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
	
	// MOVE FUNCTIONALITY
	
	public void swap(int i, int j) {
		int tmp = sequence[i];
		sequence[i]=sequence[j];
		sequence[j]=tmp;
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
	
	public void setImproving() {
		building = false;
	}
	
	public boolean isBuilding() {
		return building;
	}
	
	public int getNumClasses() {
		return numClasses;
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
	
	public int[] getRemainingClasses() {
		return availableClasses;
	}

	@Override
	public String toString() {
		return "CSPSolution [sequence=" + Arrays.toString(sequence) + "]";
	}
}
