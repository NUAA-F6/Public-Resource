package org.uma.jmetal.operator.impl.neighborsearch;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.uma.jmetal.operator.NeighborSearchOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.PermutationSolution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.comparator.DominanceComparator;

@SuppressWarnings("serial")
public class TwoOpt4QAP implements NeighborSearchOperator<PermutationSolution<Integer>> {

	private double searchProbility = 1.0;
	private Problem<PermutationSolution<Integer>> problem;

	private Comparator<PermutationSolution<Integer>> comparator;

	public TwoOpt4QAP(double searchProbility, Problem<PermutationSolution<Integer>> problem) {
		if ((searchProbility < 0) || (searchProbility > 1)) {
			throw new JMetalException("Search probability value invalid: " + searchProbility);
		}
		this.searchProbility = searchProbility;
		this.problem = problem;
		comparator = new DominanceComparator<>();
	}

	@Override
	public List<PermutationSolution<Integer>> execute(PermutationSolution<Integer> source) {
		return doSearch(source);
	}

	private List<PermutationSolution<Integer>> doSearch(PermutationSolution<Integer> source) {
		List<PermutationSolution<Integer>> neighbors = new ArrayList<>();
		int numOfVar = source.getNumberOfVariables();
		for (int startIndex = 0; startIndex < numOfVar - 1; startIndex++) {
			for (int endIndex = startIndex + 2; endIndex < numOfVar; endIndex++) {
				if (Math.random() < searchProbility) {
					PermutationSolution<Integer> neighbor = (PermutationSolution<Integer>) source.copy();
					neighbor.setVariableValue(startIndex, source.getVariableValue(endIndex));
					neighbor.setVariableValue(endIndex, source.getVariableValue(startIndex));
//					for (int subPos = startIndex + 1, mainPos = endIndex; subPos <= endIndex; subPos++, mainPos--) {
//						neighbor.setVariableValue(subPos, source.getVariableValue(mainPos));
//					}
					problem.evaluate(neighbor);
					if (comparator != null) {
						if (comparator.compare(source, neighbor) >= 0)
							neighbors.add(neighbor);
					} else
						neighbors.add(neighbor);
				}
			}
		}
		return neighbors;
	}

	@Override
	public void setCandidate(List<PermutationSolution<Integer>> population) {
	}

	@Override
	public void setComparator(Comparator<PermutationSolution<Integer>> comparator) {
		this.comparator = comparator;
	}

}
