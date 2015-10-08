package jcsp.neighbourhood;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jcsp.CSPProblem;
import jcsp.CSPSolution;
import jcsp.move.MultipleMoves;
import jcsp.move.SingleInsertion;

import org.jamesframework.core.search.neigh.Move;

import util.random.Randomizer;

public class CSPRandomShakingInsert extends CSPNeighbourhood{

	private int maxInserts;
	
	public CSPRandomShakingInsert(int numInserts) {
		this.maxInserts=numInserts;
	}
	
	@Override
	public List<Move<CSPSolution>> getAllMoves(CSPSolution solution) {
		return null;
	}
	
	private Set<Integer> getColissions(int [][] colissions) {
		Set<Integer> colissioningIndexes = new HashSet<Integer>();
		
		for (int i=0; i<colissions.length; i++) {
			for (int j=0; j<colissions[i].length; j++) {
				if(colissions[i][j]!=0) {
					colissioningIndexes.add(j);
				}
			}
		}
		
		return colissioningIndexes;
	}

	@Override
	public Move<CSPSolution> getRandomMove(CSPSolution sol) {
		
		Set<Integer> conflictingIndexes = getColissions(sol.getColissionMatrix());
		Integer[] indexes = {};
		indexes = conflictingIndexes.toArray(indexes);
		int numIndexes = indexes.length;

		int numInserts = Math.min(numIndexes, this.maxInserts);
		
		Move<CSPSolution>[] moves = new SingleInsertion[numInserts];
		
		Randomizer random = CSPProblem.random;
		int[] sequence = sol.getSequence();
		int sequenceLenght = sequence.length;
		
		for (int i=0; i<numInserts; i++) {
			int oldPos;
			int newPos;
			do {
				oldPos = indexes[random.nextInt(numIndexes)];
				newPos = random.nextInt(sequenceLenght);
			}while(Math.abs(oldPos-newPos)>1
					|| sequence[oldPos]==sequence[newPos]);
			moves[i] = new SingleInsertion(oldPos, newPos);
		}
		
		return new MultipleMoves(moves);
	}
}
