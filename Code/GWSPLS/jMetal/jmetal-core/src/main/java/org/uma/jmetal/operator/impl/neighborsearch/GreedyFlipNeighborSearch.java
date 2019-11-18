package org.uma.jmetal.operator.impl.neighborsearch;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.uma.jmetal.operator.NeighborSearchOperator;
import org.uma.jmetal.problem.CandidateBitConstrainProblem;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.PermutationSolution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.comparator.DominanceComparator;

@SuppressWarnings("serial")
public class GreedyFlipNeighborSearch implements NeighborSearchOperator<PermutationSolution<Integer>> {

	private Problem<PermutationSolution<Integer>> problem;
	private double bitSelectedProbility;
	
	private Comparator<PermutationSolution<Integer>> comparator ;
	
	public GreedyFlipNeighborSearch(double bitSelectedProbility, 
			Problem<PermutationSolution<Integer>> problem) {
		if ((bitSelectedProbility < 0) || (bitSelectedProbility > 1)) {
			throw new JMetalException("Search probability value invalid: " + bitSelectedProbility);
		}
		this.problem = problem;
		this.bitSelectedProbility = bitSelectedProbility;
		comparator = new DominanceComparator<>();
	}

	@Override
	public List<PermutationSolution<Integer>> execute(PermutationSolution<Integer> solution) {
		List<Integer> unSelectedIndex = ((CandidateBitConstrainProblem<PermutationSolution<Integer>>) problem)
				.getUnselectedIndex(solution, bitSelectedProbility);
		List<Integer> selectedBestIndex = ((CandidateBitConstrainProblem<PermutationSolution<Integer>>) problem)
				.getSelectedIndex(solution, bitSelectedProbility);
		List<PermutationSolution<Integer>> neighbors = new ArrayList<>();
		for (int i = 0; i < selectedBestIndex.size(); i++) {
			int aSelectedIndex = selectedBestIndex.get(i);
			for (int j = 0; j < unSelectedIndex.size(); j++) {
				int aUnSelectedIndex = unSelectedIndex.get(j);
				PermutationSolution<Integer> neighborSolution = getNeighborhood(solution, aSelectedIndex,
						aUnSelectedIndex);
				if (neighborSolution != null) {
					this.problem.evaluate(neighborSolution);
					if (comparator != null) {
						int dmtFlag = comparator.compare(solution, neighborSolution);
						if (dmtFlag >= 0)
							neighbors.add(neighborSolution);
					} else
						neighbors.add(neighborSolution);
						
				}
			}
		}
		return neighbors ;

	}

	public PermutationSolution<Integer> getNeighborhood(PermutationSolution<Integer> solution, int aSelectedIndex,
			int aUnSelectedIndex) {
		PermutationSolution<Integer> neighborSolution = (PermutationSolution<Integer>) solution.copy();

		neighborSolution.setVariableValue(aSelectedIndex, 0);
		neighborSolution.setVariableValue(aUnSelectedIndex, 1);
		if (((CandidateBitConstrainProblem<PermutationSolution<Integer>>) problem).isValid(neighborSolution)) {
			((CandidateBitConstrainProblem<PermutationSolution<Integer>>) problem).repair(neighborSolution);
			return neighborSolution;
		} else
			return null;
	}

	@Override
	public void setCandidate(List<PermutationSolution<Integer>> population) {
	}

	@Override
	public void setComparator(Comparator<PermutationSolution<Integer>> comparator){
		this.comparator = comparator ;
	}
}
