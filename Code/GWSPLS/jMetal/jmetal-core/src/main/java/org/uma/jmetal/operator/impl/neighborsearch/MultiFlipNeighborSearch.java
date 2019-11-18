package org.uma.jmetal.operator.impl.neighborsearch;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.uma.jmetal.operator.NeighborSearchOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.PermutationSolution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.PermutationUtility;
import org.uma.jmetal.util.comparator.DominanceComparator;

@SuppressWarnings("serial")
public class MultiFlipNeighborSearch implements NeighborSearchOperator<PermutationSolution<Integer>>{

	private Problem<PermutationSolution<Integer>> problem;
	private int flipNum;
	
	private Comparator<PermutationSolution<Integer>> comparator ;
	public MultiFlipNeighborSearch(Problem<PermutationSolution<Integer>> problem,
			int flipNum) {
		if ((flipNum < 0) || (flipNum > problem.getNumberOfVariables())) {
			throw new JMetalException("Flip num value invalid: " + flipNum);
		}
		this.flipNum = flipNum;
		this.problem = problem ;
		comparator = new DominanceComparator<>();
	}
	
	@Override
	public List<PermutationSolution<Integer>> execute(PermutationSolution<Integer> solution) {
		List<PermutationSolution<Integer>> neighbors = new ArrayList<PermutationSolution<Integer>>();
		
		Integer[] perm = new PermutationUtility().initPermutation(solution.getNumberOfVariables());
		int i = 0;
		while(flipNum*(i+1) <= perm.length){
			PermutationSolution<Integer> neighborSolution = getNeighborhood(solution, perm, i, flipNum);
			if (comparator != null) {
				int dominateFlag = comparator.compare(solution, neighborSolution);
				if (dominateFlag >= 0) {
					neighbors.add(neighborSolution);
				}
			} else
				neighbors.add(neighborSolution);
			i++;
		}
		return neighbors ;
	}

	private PermutationSolution<Integer> getNeighborhood(PermutationSolution<Integer> solution,
			Integer[] perm, int i, int flipNum) {
		PermutationSolution<Integer> child = (PermutationSolution<Integer>) solution.copy();
		int bit = Math.random()<0.5?0:1;
		for(int k = i;k < i+flipNum;k++){
			child.setVariableValue(perm[k], bit);
		}
		this.problem.evaluate(child);
		return child;
	}
	
	@Override
	public void setCandidate(List<PermutationSolution<Integer>> population) {
	}

	@Override
	public void setComparator(Comparator<PermutationSolution<Integer>> comparator){
		this.comparator = comparator ;
	}
}
