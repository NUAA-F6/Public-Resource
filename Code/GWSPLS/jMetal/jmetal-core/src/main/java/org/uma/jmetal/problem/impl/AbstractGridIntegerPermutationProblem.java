package org.uma.jmetal.problem.impl;

import org.uma.jmetal.problem.GridPermutationProblem;
import org.uma.jmetal.solution.GridPermutationSolution;
import org.uma.jmetal.solution.impl.GridIntegerPermutationSolution;

@SuppressWarnings("serial")
public abstract class AbstractGridIntegerPermutationProblem
    extends AbstractGenericProblem<GridPermutationSolution<Integer>> implements
    GridPermutationProblem<GridPermutationSolution<Integer>> {

  /* Getters */

  /* Setters */

  @Override
  public GridPermutationSolution<Integer> createSolution() {
    return new GridIntegerPermutationSolution(this) ;
  }
}
