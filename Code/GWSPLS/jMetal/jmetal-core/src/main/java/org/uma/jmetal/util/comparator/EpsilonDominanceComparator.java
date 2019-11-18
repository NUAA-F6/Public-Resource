package org.uma.jmetal.util.comparator;

import java.io.Serializable;
import java.util.Comparator;

import org.uma.jmetal.solution.Solution;

@SuppressWarnings("serial")
public class EpsilonDominanceComparator<S extends Solution<?>> implements Comparator<S>, Serializable {

	protected boolean isSameBox;
	protected double[] epsilons;
	
	private double[] idealPoint ;
	
	public EpsilonDominanceComparator(double epsilon) {
		epsilons = new double[] {epsilon} ;
		idealPoint = new double[] {0} ;
	}
	
	public EpsilonDominanceComparator(double[] epsilons) {
		this.epsilons = epsilons ;
		idealPoint = new double[] {0} ;
	}
	
	public double getEpsilon(int objective) {
		return epsilons[objective < epsilons.length ? objective
				: epsilons.length - 1];
	}
	
	public double getIdealValue(int objective){
		return idealPoint[objective < idealPoint.length ? objective
				: idealPoint.length - 1];
	}
	
	public void setIdealValue(double ideal){
		idealPoint = new double[]{ideal};
	}
	
	public void setIdealValue(double[] ideals){
		idealPoint = ideals;
	}
	
	public boolean isSameBox() {
		return isSameBox;
	}
	
	protected void setSameBox(boolean isSameBox) {
		this.isSameBox = isSameBox;
	}
	
	
	/**
	 * compare to solution according to &epsilon;-dominance
	 * @return -1 if solution1 dominates solution2, 1 if solution2 dominates solution1, 
	 * and 0 if the solutions are non-dominated
	 */
	@Override
	public int compare(S solution1, S solution2) {
		setSameBox(false);

		boolean dominate1 = false;
		boolean dominate2 = false;

		for (int i = 0; i < solution1.getNumberOfObjectives(); i++) {
			double epsilon = getEpsilon(i);
			double index1 = Math.floor((solution1.getObjective(i) - getIdealValue(i)) / epsilon);
			double index2 = Math.floor((solution2.getObjective(i) - getIdealValue(i)) / epsilon);

			if (index1 < index2) {
				dominate1 = true;

				if (dominate2) {
					return 0;
				}
			} else if (index1 > index2) {
				dominate2 = true;

				if (dominate1) {
					return 0;
				}
			}
		}

		if (!dominate1 && !dominate2) {
			setSameBox(true);

			double dist1 = 0.0;
			double dist2 = 0.0;

			for (int i = 0; i < solution1.getNumberOfObjectives(); i++) {
				double epsilon = getEpsilon(i);
				double index1 = Math.floor((solution1.getObjective(i) - getIdealValue(i)) / epsilon);
				double index2 = Math.floor((solution2.getObjective(i) - getIdealValue(i)) / epsilon);

				dist1 += Math.pow(solution1.getObjective(i) - (index1 * epsilon + getIdealValue(i)),
						2.0);
				dist2 += Math.pow(solution2.getObjective(i) - (index2 * epsilon + getIdealValue(i)),
						2.0);
			}

			if (dist1 < dist2) {
				return -1;
			} else {
				return 1;
			}
		} else if (dominate1) {
			return -1;
		} else {
			return 1;
		}
	}

}
