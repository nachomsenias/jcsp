package jcsp.experiment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import jcsp.CSPProblem;
import jcsp.CSPSolution;
import jcsp.algo.GRASP;
import jcsp.algo.beans.GRASPBean;
import jcsp.util.CSPParser;
import util.Functions;
import util.random.RandomizerFactory;
import util.random.RandomizerFactory.RandomizerAlgorithm;
import util.random.RandomizerUtils;

public class GRASPExperiment {

	private static BufferedWriter csvWriter = null;

	public static void main(String[] args) throws IOException {
		if (args.length != 3) {
			throw new IllegalArgumentException(
				"This application requires a directory with sequence files, "
					+ "a folder for writing the experiment log and a "
						+ "experiment config file.");
		}

		String sourceDir = args[0];

		File dir = new File(sourceDir);

		if (!dir.isDirectory()) {
			throw new IllegalArgumentException(
					"Supplied path is intended to be a directory");
		}

		// Create result files and folders.
		String logFolder = args[1];
		new File(logFolder).mkdirs();

		String experimentFile = args[2];

		String timestamp = String.valueOf(System.currentTimeMillis());

		File experimentConfig = new File(experimentFile);

		String resulPath = logFolder + "/" + experimentConfig.getName()
				+ "_" + timestamp + ".csv";

		csvWriter = new BufferedWriter(new FileWriter(resulPath, false));

		// PrintHeaders
		String csvHeader = "Instance,"
				+ Functions.arrayToString(RandomizerUtils.PRIME_SEEDS, ",");
		csvWriter.write(csvHeader);
		csvWriter.newLine();

		String message = "Experiment Signature: " + timestamp + "\n";
		System.out.print(message);
		// fw.write(message);

		message = "Result file: " + resulPath + "\n";
		System.out.print(message);

		for (File config : dir.listFiles()) {

			String name = config.getName();

			if (name.equals(".") || name.equals("..") || name.equals("results"))
				continue;

			csvWriter.write(name);

			runGrasp(config.getAbsolutePath(), timestamp, experimentConfig);
			csvWriter.newLine();
			csvWriter.flush();
		}

		csvWriter.close();
	}

	private static void runGrasp(String sequenceFile, String signature,
			File experimentFile) throws IOException {
		String message = "Starting experiment with file: " + sequenceFile
				+ "\n";
		System.out.print(message);

		CSPProblem csp = CSPParser.load(sequenceFile);

		GRASPBean bean = GRASPBean.readConfigFile(experimentFile);
		boolean verbose = false;

		for (int seedIndex = 0; seedIndex < 30; seedIndex++) {

			message = "Seed: " + seedIndex + "\n";
			System.out.print(message);
			// fw.write(message);

			CSPProblem.random = RandomizerFactory.createRandomizer(
					RandomizerAlgorithm.XOR_SHIFT_128_PLUS_FAST,
					RandomizerUtils.PRIME_SEEDS[seedIndex]);

			GRASP grasp = new GRASP(csp, bean, verbose);
			grasp.optimize();

			CSPSolution best = grasp.getBest();
			// Best solution should never be null because at least one solution
			// must be returned.
			message = "GRASP SOLUTION::\n";
			System.out.print(message);
			// fw.write(message);

			message = best.toString() + "\n";
			System.out.print(message);
			// fw.write(message);

			message = "GRASP FINAL FITNESS::\n";
			System.out.print(message);
			// fw.write(message);

			double fitness = grasp.getFinalFitness();

			csvWriter.write("," + fitness);

			message = fitness + "\n";
			System.out.print(message);
			// fw.write(message);
		}
	}
}
