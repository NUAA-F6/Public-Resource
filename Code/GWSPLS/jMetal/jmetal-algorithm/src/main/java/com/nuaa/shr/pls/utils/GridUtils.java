package com.nuaa.shr.pls.utils;

import java.util.List;

import org.uma.jmetal.solution.GridPermutationSolution;

public class GridUtils {

	public static boolean isDominatedByOrEqualToList(GridPermutationSolution<Integer> neighborSolution,
			List<GridPermutationSolution<Integer>> population) {
		for(int i = 0;i<population.size();i++){
			GridPermutationSolution<Integer> comparaSolution = population.get(i);
			GridComparator comparator = new GridComparator();
			//comparaSolution dominates neighborSolution
			if(comparator.compare(comparaSolution, neighborSolution)==-1||
					isEqual(comparaSolution, neighborSolution))
				return true;
		}
		return false;
	}

	private static boolean isEqual(GridPermutationSolution<Integer> comparaSolution,
			GridPermutationSolution<Integer> neighborSolution) {
		for(int i = 0;i<comparaSolution.getNumberOfVariables();i++){
			if(comparaSolution.getVariableValue(i)!=neighborSolution.getVariableValue(i))
				return false;
		}
		return true;
	}

	public static void reduceDominatedInList(GridPermutationSolution<Integer> neighborSolution,
			List<GridPermutationSolution<Integer>> population) {
		for (int i = population.size() - 1; i >= 0; i--) {
			GridPermutationSolution<Integer> comparaSolution = population.get(i);
			GridComparator comparator = new GridComparator();
			if (comparator.compare(neighborSolution, comparaSolution) == -1) {
				population.remove(i);
			}
		}
	}

}
