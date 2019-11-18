package org.uma.jmetal.problem;

import org.uma.jmetal.solution.GridPermutationSolution;

public interface GridPermutationProblem<S extends GridPermutationSolution<?>> extends Problem<S>{
	public int getPermutationLength() ;
}
