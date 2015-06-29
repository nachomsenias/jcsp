package jcsp.neighbourhood;

import java.util.ArrayList;
import java.util.List;

import jcsp.CSPProblem;
import jcsp.CSPSolution;
import jcsp.move.AddCar;
import jcsp.util.FitnessBean;

import org.apache.commons.collections.BinaryHeap;
import org.jamesframework.core.search.neigh.Neighbourhood;

@SuppressWarnings("deprecation")
public class CSPGreedyNeighbourhood implements Neighbourhood<CSPSolution>{

	private final double alpha;
	private final CSPProblem problem;
	
	public CSPGreedyNeighbourhood(CSPProblem problem, double alpha) {
		this.alpha=alpha;
		this.problem=problem;
	}
	
	@Override
	public List<AddCar> getAllMoves(CSPSolution sol) {
		
		List<AddCar> allMoves = getEveryMove(sol);
		
		if (allMoves.isEmpty()) {
			return allMoves; 
		}
		
		int totalMoves = allMoves.size();
		
		BinaryHeap bh = new BinaryHeap(false, FitnessBean.beanComparator());
		
		for (int i=0; i<totalMoves; i++) {
			AddCar move = allMoves.get(i);
			move.apply(sol);
			double fitness = problem.evaluate(sol).getValue();
			move.undo(sol);
			bh.add(new FitnessBean(fitness, move));
		}
		
		return selectBesties(bh, totalMoves);
	}
	
	public List<AddCar> selectBesties(BinaryHeap bh, int total) {
		//At least one move should be applied when the sequence is not complete.
		int selectableMoves = (int) (alpha * total)+1;
		
		List<AddCar> selectedMoves = new ArrayList<AddCar>();
		
		for (int i=0; i<selectableMoves; i++) {
			FitnessBean selected = (FitnessBean) bh.pop();
			
			selectedMoves.add(
					selected.move
				);
		}
		
		return selectedMoves;
	}

	public List<AddCar> getEveryMove(CSPSolution sol) {
		List<AddCar> allMoves = new ArrayList<AddCar>();
		int[] remaining = sol.getRemainingClasses();
		for(int i=0; i<sol.getNumClasses(); i++) {
			if(remaining[i]>0) {
				allMoves.add(new AddCar(i));
			}
		}
		return allMoves;
	}
	
	@Override
	public AddCar getRandomMove(CSPSolution sol) {
		List<AddCar> allMoves = getAllMoves(sol);
		if(allMoves.isEmpty()) {
			return null;
		} else {
			int moves = allMoves.size();
			return allMoves.get(CSPProblem.random.nextInt(moves));
		}
	}
	
	public double getAlpha() {
		return alpha;
	}

}
