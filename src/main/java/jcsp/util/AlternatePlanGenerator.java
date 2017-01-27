package jcsp.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;

import jcsp.CSPProblem;
import util.functions.ArrayFunctions;
import util.random.Randomizer;
import util.random.RandomizerFactory;
import util.random.RandomizerFactory.RandomizerAlgorithm;
import util.random.RandomizerUtils;

public class AlternatePlanGenerator {

	public final static String ROBUST_APPENDIX= "_alternate_plans.txt";
	
	public static void generateAlternatePlansFile(String baseCSP, int numPlans, 
			double specialPercentage) throws IOException{
		CSPProblem csp = CSPParser.load(baseCSP);
		
		int totalClasses= csp.getNumClasses();
		int numSpecial = (int)(totalClasses*specialPercentage);
		
		//Compute special demand
		int[] demandByClasses = csp.getDemandByClasses();
		int specialDemand = 0;
		
		for (int car = totalClasses; car > totalClasses-numSpecial; car--) {
			specialDemand+=demandByClasses[car-1];
		}
		
		int[][] alternatePlans = new int [numPlans][numSpecial];
		
		Randomizer random = RandomizerFactory.createRandomizer(
				RandomizerAlgorithm.XOR_SHIFT_128_PLUS_FAST,
				RandomizerUtils.PRIME_SEEDS[0]);
		
		for (int p =0; p<numPlans; p++) {
			int demand = specialDemand;
			for (int car =0; car<numSpecial-1; car++) {
				int assignedCars = random.nextInt(demand);
				alternatePlans[p][car]=assignedCars;
				
				demand-=assignedCars;
			}
			alternatePlans[p][numSpecial-1]=demand;
		}
		
		File base = new File(baseCSP);
		
		File alternate = new File(base.getParent()+File.separator+FilenameUtils.getBaseName(base.getName())+ROBUST_APPENDIX);
		
//		FileWriter fw = new FileWriter(alternate);
		BufferedWriter fw = new BufferedWriter(new FileWriter(alternate, false));
		
		fw.write(String.valueOf(numSpecial));
		fw.newLine();
		fw.write(String.valueOf(numPlans));
		fw.newLine();
		
		for (int p =0; p<numPlans; p++) {
//			fw.write(alternatePlans[p][0]);
//			for (int car =1; car<numSpecial; car++) {
//				fw.write(" "+alternatePlans[p][car]);
//			}
//			fw.write("\n");
			fw.write(ArrayFunctions.arrayToString(alternatePlans[p], " "));
			fw.newLine();
		}
		
		fw.flush();
		fw.close();
	}
	
	public static void main(String[] args) {
		String instancesDir = args[0];
		int numPlans = Integer.parseInt(args[1]);
		double specialPercentage = Double.parseDouble(args[2]);
		
		File baseDir = new File(instancesDir);
		
		if(baseDir.isDirectory()) {
			File[] instanceDirs=baseDir.listFiles();
			
			for (File dir : instanceDirs) {
				if(!dir.getName().equals(".") && !dir.getName().equals("..") 
						&& dir.isDirectory()) {
					File[] cspInstances = dir.listFiles();
					
					for (File instance: cspInstances) {
						try {
							if(!instance.getName().endsWith(ROBUST_APPENDIX))
								generateAlternatePlansFile(instance.getPath(), numPlans, specialPercentage);
						} catch (IOException e) {
							System.out.println("Failed at file: "+instance.getName());
							e.printStackTrace();
						}
					}
				}
			}
		}
		
	}
}
