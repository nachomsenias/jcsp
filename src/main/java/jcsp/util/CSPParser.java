package jcsp.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import jcsp.CSPProblem;
import jcsp.robust.RobustCSPProblem;
import jcsp.robust.RobustnessEvaluator;

public class CSPParser {
	
	public static final int DEMAND_INDEX = 0;
	public static final int NUM_OPTIONS_INDEX = 1;
	public static final int NUM_CLASSES_INDEX = 2;
	public static final int NUM_SPECIAL_INDEX = 3;
	
	public static final int POSSIBLE_INDEX = 0;
	public static final int TOTAL_POSSIBLE_INDEX = 1;
	
	public static final int CLASS_DEMAND_INDEX = 1;
	
	public static RobustnessEvaluator loadRobustnessEvaluator(String pathProblem, 
			String pathPlans) throws IOException{
		//Load Problem
		CSPProblem csp = load(pathProblem);
		if(pathPlans==null || pathPlans.equals("")) {
			throw new IllegalArgumentException(
					"Invalid file pathPlans:: Cant be null or empty.");
		}
		//Load Plans
		BufferedReader fr = new BufferedReader(new FileReader(pathPlans));
		
		String line = fr.readLine();
		int numSpecial = Integer.parseInt(line);
		
		line = fr.readLine();
		int numPlans = Integer.parseInt(line);
		
		int[][] productionPlans = new int [numPlans][numSpecial];
		
		for (int p=0; p<numPlans; p++) {
			line = fr.readLine();
			String[] chunked = line.split(" ");
			for (int special=0; special<numSpecial; special++) {
				productionPlans[p][special] = Integer.parseInt(chunked[special]);
			}
		}
		fr.close();
		
		return new RobustnessEvaluator(csp, numSpecial, productionPlans);
	}
	
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
		
		//Robust CSP
		int specialClasses = 0;
		if(firstLineChunked.length>3) {
			specialClasses=Integer.parseInt(firstLineChunked[NUM_SPECIAL_INDEX]);
		}
		
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
		
		//Skip non-regular classes
		int regularClasses = classes-specialClasses;
		
		//Then for each class: index no.; no. of cars in this class; for each 
		//option, whether or not this class requires it (1 or 0).
		classDemand = new int[regularClasses];
		requirements = new int [regularClasses][numOptions];
		
		int currentClass = 0;
		line = fr.readLine();
		
		
		
		while(line!=null && currentClass<regularClasses) {
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
		
		if(specialClasses==0) {
			fr.close();
			return new CSPProblem(
					demand, numOptions, classes, options, requirements, classDemand
				);
		} else {
			
			int specialCarDemanded = Integer.parseInt(line);
//			int specialCarDemanded = classDemand[currentClass-1];
			
			int[][] specialRequirements = new int [specialClasses][numOptions];
			int specialClassIndex = 0;
			
			line = fr.readLine();
			
			while(currentClass<classes) {
				String [] randomLineSplitted = line.split(" ");
				for(int i=0; i<numOptions; i++) {
					specialRequirements[specialClassIndex][i] = Integer.parseInt(
							randomLineSplitted[i+1]);
				}
				line = fr.readLine();
				currentClass++;
				specialClassIndex++;
			}
			
			int numPlans = Integer.parseInt(line);
			int[][] demandSpecial = new int[numPlans][specialClasses];
			
			for (int p = 0; p<numPlans; p++) {
				line = fr.readLine();
				String [] randomLineSplitted = line.split(" ");
				for (int s=0; s<specialClasses; s++) {
					demandSpecial[p][s] = Integer.parseInt(randomLineSplitted[s]);
				}
			}
			
			fr.close();
			return new RobustCSPProblem(
					demand, numOptions, regularClasses, options, requirements, classDemand,
					specialClasses,specialCarDemanded,specialRequirements,numPlans,demandSpecial
				);
		}
	}
}
