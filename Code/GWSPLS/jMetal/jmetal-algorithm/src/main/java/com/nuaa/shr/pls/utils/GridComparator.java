package com.nuaa.shr.pls.utils;

import java.util.Comparator;

import org.uma.jmetal.solution.GridPermutationSolution;

public class GridComparator implements Comparator<GridPermutationSolution<Integer>> {

	
	/**
	 * -1 solution1 dominate
	 * 1 solution2 dominate
	 * 0 non-dominate
	 */
	@Override
	public int compare(GridPermutationSolution<Integer> solution1, GridPermutationSolution<Integer> solution2) {
		int result;
		boolean solution1Dominates = false;
		boolean solution2Dominates = false;

		int flag;
		double value1, value2;
		
//		boolean isEqual = false;
		for (int i = 0; i < solution1.getNumberOfObjectives(); i++) {
			value1 = solution1.getGridCoordinate(i);
			value2 = solution2.getGridCoordinate(i);
			if (value1 < value2) {
				flag = -1;
			} else if (value2 < value1) {
				flag = 1;
			} else {
//				flag = 0;
//				isEqual = true;
				return 0;
			}

			if (flag == -1) {
				solution1Dominates = true;
			}

			if (flag == 1) {
				solution2Dominates = true;
			}
		}

			if (solution1Dominates == solution2Dominates) {
				// non-dominated solutions
				result = 0;
			} else if (solution1Dominates) {
				// solution1 dominates
				result = -1;
			} else {
				// solution2 dominates
				result = 1;
			}
		
		/*strong dominant*/
//		if (solution1Dominates == solution2Dominates) {
//			// non-dominated solutions
//			result = 0;
//		} else if (solution1Dominates) {
//			// solution1 dominates
//			result = -1;
//		} else {
//			// solution2 dominates
//			result = 1;
//		}
		return result;
	}
}
