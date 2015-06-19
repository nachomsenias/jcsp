package jcsp.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import jcsp.CSPProblem;

public class CSPParser {
	
	public static final int DEMAND_INDEX = 0;
	public static final int NUM_OPTIONS_INDEX = 1;
	public static final int NUM_CLASSES_INDEX = 2;
	
	public static final int POSSIBLE_INDEX = 0;
	public static final int TOTAL_POSSIBLE_INDEX = 1;
	
	public static final int CLASS_DEMAND_INDEX = 1;
	

	public static CSPProblem load(String path) throws IOException {
		if(path==null || path.equals("")) {
			throw new IllegalArgumentException(
					"Invalid file path:: Cant be null or empty.");
		}
		
		int demand,numOptions,classes;
		int[][] options, requirements;
		int[] classDemand;
		
		BufferedReader fr = new BufferedReader(new FileReader(path));
		
		String line = fr.readLine();
		// First line: number of cars; number of options; number of classes.
		String [] firstLineChunked = line.split(" ");
		demand = Integer.parseInt(firstLineChunked[DEMAND_INDEX]);
		numOptions = Integer.parseInt(firstLineChunked[NUM_OPTIONS_INDEX]);
		classes = Integer.parseInt(firstLineChunked[NUM_CLASSES_INDEX]);
		
		//Second line: for each option, the maximum number of cars with that 
		//option in a block.
		options = new int [2][numOptions];
		line = fr.readLine();
		String [] secondLineChunked = line.split(" ");
		for (int i=0; i<numOptions; i++) {
			options[POSSIBLE_INDEX][i] = Integer.parseInt(secondLineChunked[i]);
		}
		//Third line: for each option, the block size to which the maximum 
		//number refers.
		line = fr.readLine();
		String [] thirdLineChunked = line.split(" ");
		for (int i=0; i<numOptions; i++) {
			options[TOTAL_POSSIBLE_INDEX][i] 
					= Integer.parseInt(thirdLineChunked[i]);
		}
		
		//Then for each class: index no.; no. of cars in this class; for each 
		//option, whether or not this class requires it (1 or 0).
		classDemand = new int[classes];
		requirements = new int [classes][numOptions];
		
		int currentClass = 0;
		line = fr.readLine();
		
		while(line!=null && currentClass<classes) {
			String [] randomLineSplitted = line.split(" ");
			classDemand[currentClass] = Integer.parseInt(
					randomLineSplitted[CLASS_DEMAND_INDEX]);
			for(int i=0; i<numOptions; i++) {
				requirements[currentClass][i] = Integer.parseInt(
						randomLineSplitted[i+CLASS_DEMAND_INDEX+1]);
			}
			line = fr.readLine();
			currentClass++;
		}
		
		fr.close();
		
		return new CSPProblem(
				demand, numOptions, classes, options, requirements, classDemand
			);
	}
}
