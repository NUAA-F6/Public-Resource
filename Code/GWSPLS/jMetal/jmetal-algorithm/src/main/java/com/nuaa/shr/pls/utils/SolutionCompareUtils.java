package com.nuaa.shr.pls.utils;

import org.uma.jmetal.solution.Solution;

public class SolutionCompareUtils<S extends Solution<?>> {

	/**
	 * 
	 * @param s1
	 * @param s2
	 * @return {@code true} if s1 equals to s2
	 */
	public boolean isDuplicate(S s1, S s2) {
		for (int i = 0; i < s1.getNumberOfVariables(); i++) {
			if (s1.getVariableValue(i) != s2.getVariableValue(i)) {
				return false;
			}
		}
		
		return true;
	}

	public boolean isObjectiveEqual(S solution1,
			S solution2) {
		for (int i = 0; i < solution1.getNumberOfObjectives(); i++) {
			if (solution1.getObjective(i) != solution2.getObjective(i)) {
				return false;
			}
		}
		
		return true;
	}

}
