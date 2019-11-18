package org.uma.jmetal.problem.multiobjective;

import org.uma.jmetal.problem.impl.AbstractDoubleProblem;
import org.uma.jmetal.solution.DoubleSolution;

@SuppressWarnings("serial")
public class EmptyProblem extends AbstractDoubleProblem {

	public EmptyProblem(int numberOfObjective) {
		setNumberOfObjectives(numberOfObjective);
	}
	
	@Override
	public void evaluate(DoubleSolution solution) {
		
	}

}
