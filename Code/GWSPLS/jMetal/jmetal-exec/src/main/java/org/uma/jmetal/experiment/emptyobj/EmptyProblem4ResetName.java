package org.uma.jmetal.experiment.emptyobj;

import org.uma.jmetal.problem.impl.AbstractDoubleProblem;
import org.uma.jmetal.solution.DoubleSolution;

@SuppressWarnings("serial")
public class EmptyProblem4ResetName extends AbstractDoubleProblem{

	
	public void setName(String name){
		super.setName(name);
	}

	@Override
	public void evaluate(DoubleSolution solution) {		
	}
	
}
