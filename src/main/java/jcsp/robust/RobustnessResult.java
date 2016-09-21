package jcsp.robust;

import java.util.Arrays;

import org.apache.commons.math3.stat.StatUtils;

public class RobustnessResult {

	public final double avgRobustness;
	public final double minRobustness;
	
	public final double averagedMinRobustness;
	
	public final double[] avgRobustnessByPlan;
	public final double[] minRobustnessByPlan;
	
	public final int[] sequence;
	
	public RobustnessResult(double avgRobustness, double minRobustness, 
			double[] avgRobustnessByPlan, double[] minRobustnessByPlan, 
			int[] sequence) {
		super();
		this.avgRobustness = avgRobustness;
		this.minRobustness = minRobustness;
		this.avgRobustnessByPlan = avgRobustnessByPlan;
		this.minRobustnessByPlan = minRobustnessByPlan;
		
		this.sequence=sequence;
		
		averagedMinRobustness = StatUtils.mean(minRobustnessByPlan);
	}

	@Override
	public String toString() {
		return "RobustnessResult [avgRobustness=" + avgRobustness + ", minRobustness=" + minRobustness
				+ ", averagedMinRobustness=" + averagedMinRobustness + ", avgRobustnessByPlan="
				+ Arrays.toString(avgRobustnessByPlan) + ", minRobustnessByPlan=" + Arrays.toString(minRobustnessByPlan)
				+ ", sequence=" + Arrays.toString(sequence) + "]";
	}
}
