package jcsp;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jcsp.neighbourhood.CSPInsertionNeighbourhood;
import jcsp.neighbourhood.CSPInvertionNeighbourhood;
import jcsp.neighbourhood.CSPSwapNeighbourhood;
import jcsp.util.CSPParser;

import org.jamesframework.core.problems.constraints.validations.Validation;
import org.junit.Test;

import util.Functions;
import util.random.RandomizerFactory;
import util.random.RandomizerUtils;
import util.random.RandomizerFactory.RandomizerAlgorithm;

public class CSPProblemTest {
	
	public final static int numberOfTries = 100;

	private CSPProblem easyCSP;
	private CSPProblem mediumCSP;
	
	private List<CSPSolution> easySols;
	private List<CSPSolution> mediumSols;
	
	public CSPProblemTest() throws IOException {
		String exampleFile="../xCSP/instances/test_10_cars.txt";
		easyCSP = CSPParser.load(exampleFile);
		
		easySols = new ArrayList<CSPSolution>();
		easySols.add(new CSPSolution(null, easyCSP, new int[]{0,1,5,2,4,3,3,4,2,5}));
		easySols.add(new CSPSolution(null,easyCSP, new int[]{5,3,4,2,3,4,1,5,2,0}));
		easySols.add(new CSPSolution(null,easyCSP, new int[]{5,2,4,3,3,4,2,5,1,0}));
		easySols.add(new CSPSolution(null,easyCSP, new int[]{4,3,2,4,3,5,1,5,2,0}));
		
		exampleFile="../xCSP/instances/60/pb_60-05.txt";
		mediumCSP = CSPParser.load(exampleFile);
		
		mediumSols = new ArrayList<CSPSolution>();
		mediumSols.add(new CSPSolution(null, mediumCSP, new int[]{10, 0, 2, 1,
				3, 6, 6, 3, 16, 3, 14, 4, 19, 3, 16, 6, 14, 9, 13, 18, 15, 12, 
				13, 16, 2, 14, 9, 0, 3, 16, 14, 3, 15, 18, 13, 9, 14, 14, 9, 
				13, 5, 8, 6, 2, 16, 11, 6, 7, 6, 2, 16, 0, 2, 8, 2, 13, 9, 11, 
				6, 9, 13, 2,16, 6, 11, 6, 2, 13, 9, 6, 6, 16, 11, 13, 2, 6, 2, 
				11, 2, 13, 2, 6, 2, 6, 2, 11, 2,6, 2, 6, 2, 13, 2, 11, 2, 6, 2, 
				13, 2, 17, 2, 6, 2, 13, 2, 6, 2, 6, 2, 11, 2, 6, 2, 6, 11, 13, 
				2, 13, 2, 6, 2, 11, 2, 6, 2, 6, 2, 13, 2, 6, 2, 6, 2, 13, 2, 6, 
				2, 11, 2,13, 2, 6, 2, 6, 2, 11, 2, 6, 2, 6, 2, 11, 2, 6, 2, 6, 
				2, 13, 2, 6, 2, 6, 5, 13, 2, 6, 2, 6, 2, 13, 2, 13, 2, 6, 2, 
				11, 2, 13, 2, 13, 2, 13, 2, 6, 13, 11, 2, 13, 13, 6, 6, 13, 13, 
				11, 6, 13, 12, 16, 13, 2}));
		
		mediumSols.add(new CSPSolution(null, mediumCSP, new int[]{4, 0, 3, 16, 
				3, 14, 9, 0, 3, 16, 2, 13, 15, 3, 19, 15, 3, 14, 9, 13, 6, 9, 
				14, 3, 11, 12, 13, 9, 14, 18, 16, 14, 2, 6, 18, 16, 14, 2, 8, 
				5, 13, 7,6, 2, 1, 5, 13, 10, 6, 2, 16, 2, 6, 17, 2, 13, 9, 6, 
				11, 9, 13, 2, 16, 2, 6, 9, 13, 2, 16, 6, 2, 11, 2, 13, 2, 6, 
				2, 6, 11, 13, 2, 6, 2, 6, 11, 13, 2, 6, 2, 6, 11, 13, 2, 6, 2, 
				6, 2, 13, 2, 6, 2, 6, 2, 13, 11, 6, 2, 6, 2, 13, 2, 6, 2, 6, 
				11, 13, 2, 6, 2, 6, 11, 13, 2, 6, 2, 6, 2, 13, 2, 0, 2, 6, 2, 
				13, 2, 6, 11, 6, 2, 13, 2, 11, 2, 6, 2, 13, 2, 8, 2, 6, 2, 13,
				2, 6, 2, 6, 11, 13, 2, 6, 2, 6, 2, 13, 2, 6, 2, 6, 2, 13, 2, 6, 
				2, 13, 2, 13, 2, 13, 11, 13, 2, 13, 2, 6, 13, 11, 2, 16, 6, 12, 
				16, 2, 11, 2, 6, 2, 6, 11, 2, 6}));
		
		mediumSols.add(new CSPSolution(null, mediumCSP, new int[]{2, 6, 18, 16, 
				3, 14, 4, 19, 3, 16, 14, 6, 2, 13, 2, 14, 6, 9, 13, 3, 16, 0, 
				12, 16, 2, 13, 2, 6, 5, 16, 2, 6, 15, 5, 13, 9, 14, 6, 16, 2, 
				13, 17, 6, 3, 16, 2, 14, 10, 11, 2, 16, 14, 12, 11, 7, 13, 2, 6, 
				3, 13, 9, 6, 11, 9, 13, 3, 6, 9, 0, 2, 13, 6, 9, 6, 13, 9, 0, 2, 
				6, 2, 13, 2, 6, 2, 15, 2, 13, 13, 2, 6, 2, 13, 2, 6, 2, 13, 2, 
				13, 2, 6, 2, 6, 13, 13, 2, 13, 2, 6, 2, 13, 2, 13, 2, 6, 2, 13, 
				2, 13, 18, 6, 2, 13, 13, 2, 13, 13, 2, 6, 2, 13, 2, 13, 13, 2, 
				6, 2, 6, 2, 11, 6, 2, 6, 2, 11, 6, 2, 6, 2, 11, 6, 2, 6, 2, 11, 
				6, 2, 6, 2, 11, 6, 2, 6, 2, 11, 6, 2, 6, 2, 11, 6, 2, 6, 2, 11, 
				6, 2, 6, 2, 11, 2, 6, 8, 2, 11, 1, 2, 13, 2, 11, 2, 8, 6, 2, 11, 
				2, 6, 2, 16, 2, 11}));
		
		mediumSols.add(new CSPSolution(null, mediumCSP, new int[]{13, 12, 16, 3, 
				13, 15, 12, 19, 9, 13, 14, 4, 13, 18, 16, 14, 14, 9, 13, 18, 15, 
				14, 3, 16, 3, 0, 9, 14, 3, 16, 0, 3, 16, 14, 3, 16, 0, 2, 8, 2, 
				13, 10, 11, 2, 1, 13, 6, 7, 6, 5, 16, 6, 2, 8, 2, 13, 9, 6, 2, 
				13, 9, 11, 2, 6, 6, 9, 13, 2, 16, 11, 6, 9, 13, 2, 6, 2, 6, 2, 
				13, 2, 6, 11, 6, 2, 13, 2, 6, 2, 6, 2, 13, 2, 6, 2, 6, 2, 13, 2, 
				6, 2, 6, 2, 13, 2, 13, 2, 6, 2, 11, 2, 6, 2, 6, 11, 13, 2, 6, 2, 
				6, 2, 13, 2, 6, 2, 6, 2, 11, 2, 6, 2, 6, 2, 13, 11, 6, 2, 6, 2, 
				11, 2, 6, 2, 6, 5, 13, 2, 6, 2, 6, 2, 13, 2, 6, 2, 6, 11, 13, 2, 
				6, 2, 6, 2, 13, 2, 6, 2, 6, 11, 13, 2, 6, 2, 17, 2, 13, 2, 13, 
				11, 13, 2, 13, 2, 11, 2, 16, 2, 6, 2, 13, 11, 6, 2, 6, 2, 11, 6, 
				2, 13, 2, 11}));
		
		mediumSols.add(new CSPSolution(null, mediumCSP, new int[]{6, 0, 2, 1, 3, 
				6, 4, 19, 3, 16, 12, 14, 9, 11, 2, 15, 12, 13, 11, 3, 14, 15, 2, 
				13, 9, 14, 14, 11, 13, 3, 16, 14, 0, 9, 13, 3, 16, 14, 0, 9, 13, 
				2, 16, 2, 6, 7, 13, 5, 8, 6, 3, 16, 5, 6, 8, 2, 13, 9, 6, 2, 16, 
				2, 13, 17, 6, 2, 16, 2, 6, 16, 6, 11, 16, 2, 6, 9, 6, 2, 13, 18, 
				6, 2, 6, 2, 13, 2, 6, 2, 6, 2, 13, 2, 6, 11, 6, 2, 13, 2, 6, 11, 
				6, 2, 13, 2, 6, 2, 11, 2, 13, 2, 6, 2, 6, 2, 13, 2, 6, 2, 6, 2, 
				13, 2, 6, 2, 6, 2, 11, 2, 6, 2, 6, 2, 13, 11, 6, 2, 6, 2, 13, 2, 
				6, 2, 6, 18, 13, 2, 6, 2, 6, 2, 11, 2, 6, 2, 6, 2, 13, 2, 6, 2, 
				11, 2, 13, 2, 6, 2, 11, 2, 13, 2, 6, 2, 11, 2, 13, 2, 13, 2, 13, 
				2, 13, 2, 11, 13, 2, 13, 10, 6, 2, 13, 11, 6, 2, 6, 13, 2, 11, 
				6, 9, 13}));
		
		easyCSP.random = RandomizerFactory.createRandomizer(
				RandomizerAlgorithm.XOR_SHIFT_128_PLUS_FAST,
				RandomizerUtils.PRIME_SEEDS[0]);
		
		mediumCSP.random = RandomizerFactory.createRandomizer(
				RandomizerAlgorithm.XOR_SHIFT_128_PLUS_FAST,
				RandomizerUtils.PRIME_SEEDS[0]);
	}

	@Test
	public void simpleEvaluation() {
		
		boolean assertion = true;
		
		for (CSPSolution sol: easySols ) {
			Validation validation= easyCSP.validate(sol);
			assertion &= validation.passed();
			
			double evaluation = easyCSP.evaluate(sol).getValue();
			
			assertion &= evaluation == CSPProblem.FEASIBLE_FITNESS;
		}
		
		assertTrue( assertion );
	}
	
	@Test
	public void mediumEvaluation() {
		
		boolean assertion = true;
		
		for (CSPSolution sol: mediumSols ) {
			Validation validation= mediumCSP.validate(sol);
			assertion &= validation.passed();
			
			double evaluation = mediumCSP.evaluate(sol).getValue();
			
			assertion &= evaluation == CSPProblem.FEASIBLE_FITNESS;
		}
		
		assertTrue( assertion );
	}
	
	@Test
	public void fitnessEvaluation() {
		
		boolean assertion = true;
		
		for (int t = 0; t<numberOfTries; t++) {
			CSPSolution sol = mediumCSP.createRandomSolution();
			Validation validation= mediumCSP.validate(sol);
			assertion &= validation.passed();
			
			int[] sequence = sol.getSequence();
			
			double debugFitness = mediumCSP.evaluateRestrictions(sequence,sequence.length);
			double otherDebugFitness = Functions.addMatrix(mediumCSP.createExcessMatrix(sequence));
			
			assertion &= debugFitness == sol.getFitness();
			assertion &= otherDebugFitness == debugFitness;
		}
		
		assertTrue( assertion );
	}
	
	@Test
	public void constructiveEvaluation() {
		boolean assertion = true;
		
		final double alphaIncrement = 0.15;
		double alpha = 0;
		
		while(alpha<0.5) {
			
			CSPSolution greedy = mediumCSP.createGreedy(alpha);
			
			Validation validation= mediumCSP.validate(greedy);
			assertion &= validation.passed();
			
			double fitness = greedy.getFitness();
			
			double debugFitness = Functions.addMatrix(mediumCSP.createExcessMatrix(greedy.getSequence()));
			
			assertion &= fitness == debugFitness;
			
			alpha+=alphaIncrement;
		}
		
		assertTrue( assertion );
	}
	
	@Test
	public void swapEvaluation() {
		boolean assertion = true;
		
		CSPSwapNeighbourhood swap = new CSPSwapNeighbourhood();
		
		for (int t = 0; t<numberOfTries; t++) {
			CSPSolution sol = mediumCSP.createRandomSolution();
			
			swap.getRandomMove(sol).apply(sol);
			
			int[] sequence = sol.getSequence();
			
			double debugFitness = mediumCSP.evaluateRestrictions(sequence,sequence.length);
			double otherDebugFitness = Functions.addMatrix(mediumCSP.createExcessMatrix(sequence));
			
			assertion &= debugFitness == sol.getFitness();
			assertion &= otherDebugFitness == debugFitness;
		}
		
		assertTrue( assertion );
	}
	
	@Test
	public void insertEvaluation() {
		boolean assertion = true;
		
		CSPInsertionNeighbourhood insert = new CSPInsertionNeighbourhood();
		
		for (int t = 0; t<numberOfTries; t++) {
			CSPSolution sol = mediumCSP.createRandomSolution();
			
			insert.getRandomMove(sol).apply(sol);
			
			int[] sequence = sol.getSequence();
			
			double debugFitness = mediumCSP.evaluateRestrictions(sequence,sequence.length);
			double otherDebugFitness = Functions.addMatrix(mediumCSP.createExcessMatrix(sequence));
			
			assertion &= debugFitness == sol.getFitness();
			assertion &= otherDebugFitness == debugFitness;
		}
		
		assertTrue( assertion );
	}
	
	@Test
	public void invertEvaluation() {
		boolean assertion = true;
		
		CSPInvertionNeighbourhood invertion = new CSPInvertionNeighbourhood();
		
		for (int t = 0; t<numberOfTries; t++) {
			CSPSolution sol = mediumCSP.createRandomSolution();
			
			invertion.getRandomMove(sol).apply(sol);
			
			int[] sequence = sol.getSequence();
			
			double debugFitness = mediumCSP.evaluateRestrictions(sequence,sequence.length);
			double otherDebugFitness = Functions.addMatrix(mediumCSP.createExcessMatrix(sequence));
			
			assertion &= debugFitness == sol.getFitness();
			assertion &= otherDebugFitness == debugFitness;
		}
		
		assertTrue( assertion );
	}
}
