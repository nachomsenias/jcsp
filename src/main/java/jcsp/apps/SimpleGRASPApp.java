package jcsp.apps;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jcsp.CSPProblem;
import jcsp.CSPSolution;
import jcsp.algo.GRASP;
import jcsp.experiment.beans.GRASPBean;
import jcsp.localsearch.FirstImprovement;
import jcsp.neighbourhood.CSPInsertionNeighbourhood;
import jcsp.neighbourhood.CSPInvertionNeighbourhood;
import jcsp.neighbourhood.CSPShuffleNeighbourhood;
import jcsp.neighbourhood.CSPSwapNeighbourhood;
import jcsp.util.CSPParser;

import org.jamesframework.core.search.neigh.Neighbourhood;

import util.random.RandomizerFactory;
import util.random.RandomizerFactory.RandomizerAlgorithm;
import util.random.RandomizerUtils;

public class SimpleGRASPApp {

//	private final static Logger logger = LoggerFactory.getLogger(SimpleGRASPApp.class);

	public static void main(String[] args) throws IOException {
//		String exampleFile="../xCSP/instances/classical/p4_72.txt";
//		String exampleFile="../xCSP/instances/classical/p26_82.txt";
//		String exampleFile="../xCSP/instances/pb_100_01_4_72_feasible.txt";
//		String exampleFile="../xCSP/instances/pb_60-01.txt";
//		String exampleFile="../xCSP/instances/pb_60-02.txt"; //2.0
//		String exampleFile="../xCSP/instances/pb_90-10.txt";
//		String exampleFile="../xCSP/instances/pb_90-09.txt";
//		String exampleFile="../xCSP/instances/pb_90-08.txt";
//		String exampleFile="../xCSP/instances/pb_90-07.txt";
//		String exampleFile="../xCSP/instances/pb_90-06.txt";
//		String exampleFile="../xCSP/instances/pb_90-05.txt"; //4.0
//		String exampleFile="../xCSP/instances/pb_90-04.txt";
//		String exampleFile="../xCSP/instances/pb_90-03.txt";
//		String exampleFile="../xCSP/instances/pb_90-02.txt";
//		String exampleFile="../xCSP/instances/pb_90-01.txt";
//		String exampleFile="../xCSP/instances/pb_85-10.txt";
//		String exampleFile="../xCSP/instances/pb_85-09.txt";
//		String exampleFile="../xCSP/instances/pb_85-08.txt";
//		String exampleFile="../xCSP/instances/pb_85-07.txt";
//		String exampleFile="../xCSP/instances/pb_85-06.txt";
//		String exampleFile="../xCSP/instances/pb_85-05.txt";
//		String exampleFile="../xCSP/instances/pb_85-04.txt";
//		String exampleFile="../xCSP/instances/pb_85-03.txt";
//		String exampleFile="../xCSP/instances/pb_85-02.txt";
//		String exampleFile="../xCSP/instances/pb_85-01.txt";
//		String exampleFile="../xCSP/instances/pb_80-10.txt";
//		String exampleFile="../xCSP/instances/pb_80-09.txt";
//		String exampleFile="../xCSP/instances/pb_80-08.txt";
//		String exampleFile="../xCSP/instances/pb_80-07.txt";
//		String exampleFile="../xCSP/instances/pb_80-06.txt";
//		String exampleFile="../xCSP/instances/pb_80-05.txt";
//		String exampleFile="../xCSP/instances/pb_80-04.txt";
//		String exampleFile="../xCSP/instances/pb_80-03.txt";
//		String exampleFile="../xCSP/instances/pb_80-02.txt";
//		String exampleFile="../xCSP/instances/pb_80-01.txt";
//		String exampleFile="../xCSP/instances/pb_75-10.txt";
//		String exampleFile="../xCSP/instances/pb_75-09.txt";
//		String exampleFile="../xCSP/instances/pb_75-08.txt";
//		String exampleFile="../xCSP/instances/pb_75-07.txt";
//		String exampleFile="../xCSP/instances/pb_75-06.txt";
//		String exampleFile="../xCSP/instances/pb_200_01.txt";
//		String exampleFile="../xCSP/instances/test_10_cars.txt";
//		String exampleFile="../xCSP/instances/test_12_cars.txt";
//		String exampleFile="../xCSP/instances/pb_400_05.txt";
		
//		if(args.length!=2) {
//			throw new IllegalArgumentException(
//					"This application requires a directory with sequence files "
//					+ "and a folder for writing the experiment log.");
//		}
//		
//		String sourceDir = args[0];
//		
//		File dir = new File(sourceDir);
//		
//		if(!dir.isDirectory()) {
//			throw new IllegalArgumentException(
//					"Supplied path is intended to be a directory");
//		}
//		
//		for (File config :dir.listFiles()) {
//			
//			String name = config.getName();
//			
//			if(name.equals(".") || name.equals("..") || name.equals("results"))
//				continue;
//			
//			String timestamp = String.valueOf(System.currentTimeMillis());
//			
//			runGrasp(config.getAbsolutePath(), name, args[1], timestamp);
//		}
		
		String exampleFile="../xCSP/instances/90/pb_90-05.txt"; //4.0
		
		File config = new File(exampleFile);
		
		String timestamp = String.valueOf(System.currentTimeMillis());
		
		String logFolder = "/home/ECSC/imoya/xcsp/xCSP/instances/90/results";
		
		runGrasp(config.getAbsolutePath(), config.getName(), logFolder, timestamp);
	}
	
	private static void runGrasp(String sequenceFile, String name, 
			String logFolder, String signature) throws IOException {
		new File(logFolder).mkdirs();
		String resulPath = logFolder+"/"+name+"_"+signature+".log";
		FileWriter fw = new FileWriter(resulPath);
		
		CSPProblem csp = CSPParser.load(sequenceFile);
        
		String message = "Starting GRASP experiment with file: " + sequenceFile+"\n";
        System.out.print(message);
        fw.write(message);
        
        message = "Experiment Signature: " + signature+"\n";
        System.out.print(message);
        fw.write(message);

        message = "Result file: " + resulPath+"\n";
        System.out.print(message);
        fw.write(message);
        
        double alpha = 0.4;
        int iterations = 50;
        long maxSteps = 100000;
        boolean verbose = false;

        List<Neighbourhood<CSPSolution>> neighbourhoods 
        	= new ArrayList<Neighbourhood<CSPSolution>>();
        neighbourhoods.add(new CSPSwapNeighbourhood());
        neighbourhoods.add(new CSPInsertionNeighbourhood());
        neighbourhoods.add(new CSPInvertionNeighbourhood());
        neighbourhoods.add(new CSPShuffleNeighbourhood());
        
        final GRASPBean bean = new GRASPBean(iterations, alpha, maxSteps, 
        		false, false, new FirstImprovement(neighbourhoods));
        
        for (int seedIndex = 0; seedIndex<30; seedIndex++) { 
        
        	message = "Seed: " + seedIndex+"\n";
        	System.out.print(message);
            fw.write(message);
        	
        	CSPProblem.random = RandomizerFactory.createRandomizer(
		    		RandomizerAlgorithm.XOR_SHIFT_128_PLUS_FAST, RandomizerUtils.PRIME_SEEDS[seedIndex]
			); 
        	
        	GRASP grasp = new GRASP(csp, bean, verbose);
        	grasp.optimize();
        	
        	CSPSolution best = grasp.getBest();
        	//Best solution should never be null because at least one solution must be returned.
        	message = "GRASP SOLUTION::\n";
        	System.out.print(message);
            fw.write(message);
            
            message = best.toString()+"\n";
            System.out.print(message);
            fw.write(message);
            
            message = "GRASP FINAL FITNESS::\n";
            System.out.print(message);
            fw.write(message);
            
            message = String.valueOf(grasp.getFinalFitness())+"\n";
            System.out.print(message);
            fw.write(message);
        }
        
        fw.close();
	}

}
