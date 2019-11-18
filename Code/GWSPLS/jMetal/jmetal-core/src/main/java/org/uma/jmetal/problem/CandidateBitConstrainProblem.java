package org.uma.jmetal.problem;

import java.util.List;

import org.uma.jmetal.solution.Solution;

public interface CandidateBitConstrainProblem<S extends Solution<Integer>> extends Problem<S> {
	public List<Integer> getUnselectedIndex(S solution, double probility);
	public List<Integer> getSelectedIndex(S solution, double probility);
	public void repair(S solution);
	public boolean isValid(S solution) ;

}
