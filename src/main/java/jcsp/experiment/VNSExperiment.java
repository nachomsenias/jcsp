package jcsp.experiment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import jcsp.CSPProblem;
import jcsp.CSPSolution;
import jcsp.experiment.beans.AlgorithmBean;
import jcsp.experiment.beans.VNSBean;
import jcsp.util.CSPParser;
import jcsp.util.ProgressSearchListener;

import org.jamesframework.core.search.algo.vns.VariableNeighbourhoodSearch;
import org.jamesframework.core.search.neigh.Neighbourhood;
import org.jamesframework.core.search.stopcriteria.MaxRuntime;
import org.jamesframework.core.search.stopcriteria.MaxSteps;

import util.Functions;
import util.random.RandomizerFactory;
import util.random.RandomizerFactory.RandomizerAlgorithm;
import util.random.RandomizerUtils;

public class VNSExperiment {
	private static BufferedWriter csvWriter = null;

	public static void main(String[] args) throws IOException {
		if (args.length != 3) {
			throw new IllegalArgumentException(
				"This application requires a directory with sequence files, "
					+ "a folder for writing the experiment log and a experiment "
					+ "config file.");
		}

		String sourceDir = args[0];
		
		// Create result files and folders.
		String logFolder = args[1];
		new File(logFolder).mkdirs();
		
		String experimentFile = args[2];
		
		runExperiment(logFolder,experimentFile,sourceDir);
	}
	
	private static void runExperiment(String logFolder, String experimentFile, 
			String sourceDir) throws IOException {
		
		File dir = new File(sourceDir);

		if (!dir.isDirectory()) {
			throw new IllegalArgumentException(
					"Supplied path is intended to be a directory");
		}
		
		String timestamp = String.valueOf(System.currentTimeMillis());
		
		File experimentConfig = new File(experimentFile);

		String resulPath = logFolder + "/"+ experimentConfig.getName() 
				+ "_" + timestamp + ".csv";

		csvWriter = new BufferedWriter(new FileWriter(resulPath, false));

		// PrintHeaders
		String csvHeader = "Instance,"
				+ Functions.arrayToString(RandomizerUtils.PRIME_SEEDS, ",");
		csvWriter.write(csvHeader);
		csvWriter.newLine();

		String message = "Experiment Signature: " + timestamp + "\n";
		System.out.print(message);

		message = "Result file: " + resulPath + "\n";
		System.out.print(message);

		for (File config : dir.listFiles()) {

			String name = config.getName();

			if (name.equals(".") || name.equals("..") || name.equals("results"))
				continue;

			csvWriter.write(name);

			runVNS(config.getAbsolutePath(), timestamp, experimentConfig);
			csvWriter.newLine();
			csvWriter.flush();
		}

		csvWriter.close();
	}

	private static void runVNS(String sequenceFile, String signature, 
			File experimentConfig ) throws IOException {
		String message = "Starting experiment with file: " + sequenceFile
				+ "\n";
		System.out.print(message);

		CSPProblem csp = CSPParser.load(sequenceFile);
		
		VNSBean bean = null;
		try {
			bean = (VNSBean) AlgorithmBean.getBean(experimentConfig);
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		//Greedy solution is deterministic, so
		CSPSolution greedySolution = csp.createHeuristic(0.0);
		

		for (int seedIndex = 0; seedIndex < 30; seedIndex++) {

			message = "Seed: " + seedIndex + "\n";
			System.out.print(message);

			csp.random = RandomizerFactory.createRandomizer(
					RandomizerAlgorithm.XOR_SHIFT_128_PLUS_FAST,
					RandomizerUtils.PRIME_SEEDS[seedIndex]
							);

			List<Neighbourhood<CSPSolution>> shakers = bean.shakers;
        	List<Neighbourhood<CSPSolution>> improvers = bean.improvers;
        	
        	VariableNeighbourhoodSearch<CSPSolution> vns = 
        			new VariableNeighbourhoodSearch<CSPSolution>(csp, shakers, improvers);
        	
        	vns.addStopCriterion(new MaxSteps(bean.maxSteps));
        	vns.addStopCriterion(new MaxRuntime(bean.maxSeconds, TimeUnit.SECONDS));
        	if(bean.greedyInitialSolution) {
        		vns.setCurrentSolution((CSPSolution)greedySolution.copy());
        	} else {
        		vns.setCurrentSolution(csp.createRandomSolution());
        	}
        	
        	vns.addSearchListener(new ProgressSearchListener());
        	vns.start();

			CSPSolution best = vns.getBestSolution();
			// Best solution should never be null because at least one solution
			// must be returned.
			message = "VNS SOLUTION::\n";
			System.out.print(message);
			// fw.write(message);

			message = best.toString() + "\n";
			System.out.print(message);
			// fw.write(message);

			message = "VNS FITNESS::\n";
			System.out.print(message);
			// fw.write(message);

			double fitness = vns.getBestSolutionEvaluation().getValue();

			csvWriter.write("," + fitness);

			message = fitness + "\n";
			System.out.print(message);
			// fw.write(message);
		}
	}
}
