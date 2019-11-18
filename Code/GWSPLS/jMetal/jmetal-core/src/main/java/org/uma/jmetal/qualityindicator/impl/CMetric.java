package org.uma.jmetal.qualityindicator.impl;

import java.util.List;

import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.comparator.DominanceComparator;

@SuppressWarnings("serial")
public class CMetric<S extends Solution<?>> extends GenericIndicator<S> {

	private List<S> referenceSolutionList;
	/**
	 * C(solutionList, referenceSolutionList)
	 * @param solutionList 用于比较的集合
	 */
	public CMetric(List<S> referenceSolutionList){
		this.referenceSolutionList = referenceSolutionList;
	}
	
	@Override
	public Double evaluate(List<S> solutionList) {
		return getCMetric(solutionList);
	}

	private Double getCMetric(List<S> solutionList) {
		int dominateNum = 0;
		for(S referenceSolution:referenceSolutionList){
			for(S solution:solutionList){
				DominanceComparator<Solution<?>> comparator = new DominanceComparator<>();
				if(comparator.compare(solution, referenceSolution)==-1){
					dominateNum++;
					break;
				}
			}
		}
		
		return (double) dominateNum / (double) referenceSolutionList.size();
	}

	@Override
	public boolean isTheLowerTheIndicatorValueTheBetter() {
		return false;
	}

}
