package jcsp.experiment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.io.FilenameUtils;

import jcsp.CSPProblem;
import jcsp.algo.Algorithm;
import jcsp.experiment.beans.AlgorithmBean;
import jcsp.robust.RobustnessEvaluator;
import jcsp.util.AlternatePlanGenerator;
import jcsp.util.CSPParser;
import util.functions.ArrayFunctions;
import util.random.RandomizerFactory;
import util.random.RandomizerFactory.RandomizerAlgorithm;
import util.random.RandomizerUtils;

public class Experiment {
	private static BufferedWriter csvWriter = null;
	
	private static boolean computeRobustness = false;
	private static String robustnessFlag = "-ROBUST";
	
	private static BufferedWriter logWriter = null;

	public static void main(String[] args) throws IOException {
		if (args.length < 3 || args.length> 4) {
			throw new IllegalArgumentException(
				"Running experiments requires a directory with sequence files, "
					+ "a folder for writing the experiment log and a experiment "
					+ "config file.");
		}

		String sourceDir = args[0];
		
		// Create result files and folders.
		String logFolder = args[1];
		new File(logFolder).mkdirs();
		
		String experimentFile = args[2];
		
		if(args.length==4) {
			computeRobustness = args[3].equals(robustnessFlag);
		}
		
		runExperiment(logFolder,experimentFile,sourceDir);
	}
	
	private static void runExperiment(String logFolder, 
			String experimentFile, String sourceDir) throws IOException {
		
		File dir = new File(sourceDir);

		if (!dir.isDirectory()) {
			throw new IllegalArgumentException(
					"Supplied path needs to be a directory");
		}
		
		String timestamp = String.valueOf(System.currentTimeMillis());
		
		File experimentConfig = new File(experimentFile);

		String resulPath = logFolder + "/"+ experimentConfig.getName() 
				+ "_" + timestamp + ".csv";

		csvWriter = new BufferedWriter(new FileWriter(resulPath, false));
		logWriter = new BufferedWriter(new FileWriter(resulPath+".info", false));

		// PrintHeaders
		String csvHeader = "Instance,"
				+ ArrayFunctions.arrayToString(RandomizerUtils.PRIME_SEEDS, ",");
		csvWriter.write(csvHeader);
		csvWriter.newLine();

		String message = "Experiment Signature: " + timestamp + "\n";
		System.out.print(message);

		message = "Result file: " + resulPath + "\n";
		System.out.print(message);

		for (File config : dir.listFiles()) {

			String name = config.getName();

			if (name.equals(".") || name.equals("..") || name.equals("results")
					|| name.endsWith(AlternatePlanGenerator.ROBUST_APPENDIX))
				continue;

			csvWriter.write(name);

			runAlgorithm(config.getAbsolutePath(), timestamp, experimentConfig);
			csvWriter.newLine();
			csvWriter.flush();
		}

		csvWriter.close();
		logWriter.close();
	}
	
	private static void runAlgorithm(String sequenceFile, String signature, 
			File experimentConfig ) throws IOException {
		String message = "Starting experiment with file: " + sequenceFile
				+ "\n";
		System.out.print(message);

		CSPProblem csp = CSPParser.load(sequenceFile);
		
		AlgorithmBean bean;
		try {
			bean = AlgorithmBean.getBean(experimentConfig);
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException e) {
			throw new IllegalArgumentException(
					"Error when loading algorithm configuration: " 
							+ e.getMessage());
		}
		boolean verbose = false;

		for (int seedIndex = 0; seedIndex < 30; seedIndex++) {

			message = "Seed: " + seedIndex + "\n";
			System.out.print(message);

			csp.random = RandomizerFactory.createRandomizer(
					RandomizerAlgorithm.XOR_SHIFT_128_PLUS_FAST,
					RandomizerUtils.PRIME_SEEDS[seedIndex]
							);
			
			Algorithm alg = bean.createAlgorithmInstance(csp, verbose);
			
			if(computeRobustness) {
				File base = new File(sequenceFile);
				String alternatePlanFile = base.getParent()+File.separator
						+FilenameUtils.getBaseName(base.getName())
							+AlternatePlanGenerator.ROBUST_APPENDIX;
				
				RobustnessEvaluator evaluator = CSPParser.loadRobustnessEvaluator(
						sequenceFile, alternatePlanFile);
				
				alg.setRobustnessEvaluator(evaluator);
			}
			
			alg.optimize();
			double fitness = alg.getFinalFitness();
			
			logWriter.write(message);
			logWriter.newLine();
			logWriter.write("Computed fitness: "+fitness);
			logWriter.newLine();

//			if(!computeRobustness) {
				logWriter.write(Arrays.toString(alg.getBest().getSequence()));
				logWriter.newLine();
				logWriter.newLine();
				
				csvWriter.write("," + fitness);
//			} else {
//				File base = new File(sequenceFile);
//				String alternatePlanFile = base.getParent()+File.separator
//						+FilenameUtils.getBaseName(base.getName())
//							+AlternatePlanGenerator.ROBUST_APPENDIX;
//				
//				RobustnessEvaluator evaluator = CSPParser.loadRobustnessEvaluator(
//						sequenceFile, alternatePlanFile);
//				
//				RobustnessResult evaluation = evaluator.evaluateRobustness(alg.getBest());
//				
//				
//				logWriter.write(evaluation.toString());
//				logWriter.newLine();
//				logWriter.newLine();
//				
//				csvWriter.write("," + evaluation.averagedMinRobustness);
//				
//			}
		}
	}
}
