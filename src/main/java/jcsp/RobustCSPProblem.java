package jcsp;

public class RobustCSPProblem extends CSPProblem {

	private int numSpecial;
	
	private int demandSpecial;
	
	private int numAlternatePlans;
	
	private int[][] requirementsSpecial;
	
	private int[][] productionPlans;
	
	public RobustCSPProblem(int carsDemand, int numOptions, int numClasses, 
			int[][] options, int[][] requirements, int[] demandByClasses, 
			int numSpecial, int demandSpecial, int[][] requirementsSpecial, 
			int numAlternatePlans, int[][] productionPlans) {
		super(carsDemand, numOptions, numClasses, options, requirements, demandByClasses);
		this.numSpecial = numSpecial;
		this.requirementsSpecial=requirementsSpecial;
		this.numAlternatePlans=numAlternatePlans;
		this.productionPlans = productionPlans;
	}
	
	public int evaluateRestrictions(int[] sequence, int lastIndex) {
		int fitness =0;
		int robustClass = numClasses-1;
		
		for (int car=0; car<lastIndex; car++) {
			for (int option=0; option<numOptions; option++) {
				
				int total = this.options[TOTAL_INDEX][option];
				int possible = this.options[POSSIBLE_INDEX][option];
				
				int occurrences = 0;
				
				int carClass = sequence[car];
				if(carClass==robustClass) {
					//TODO probar las variantes de colisiones en las clases
				} else if(requirements[sequence[car]][option] == 0) {
					continue;
				}
				
				int nextCar = 0;
				while(nextCar<total && car+nextCar < carsDemand) {
					occurrences+=requirements[sequence[car+nextCar]][option];
					nextCar++;
				}
				
				//P+
				if (occurrences>possible) {
					fitness+=occurrences-possible;
				}

			}
		}
		
		return fitness;
	}
	
	//Getters & Setters
	public int getNumSpecial() {
		return numSpecial;
	}

	public void setNumSpecial(int numSpecial) {
		this.numSpecial = numSpecial;
	}

	public int[][] getRequirementsSpecial() {
		return requirementsSpecial;
	}

	public void setRequirementsSpecial(int[][] requirementsSpecial) {
		this.requirementsSpecial = requirementsSpecial;
	}

	public int getNumAlternatePlans() {
		return numAlternatePlans;
	}

	public void setNumAlternatePlans(int numAlternatePlans) {
		this.numAlternatePlans = numAlternatePlans;
	}

	public int getDemandSpecial() {
		return demandSpecial;
	}

	public void setDemandSpecial(int demandSpecial) {
		this.demandSpecial = demandSpecial;
	}

	public int[][] getProductionPlans() {
		return productionPlans;
	}

	public void setProductionPlans(int[][] productionPlans) {
		this.productionPlans = productionPlans;
	}
}
