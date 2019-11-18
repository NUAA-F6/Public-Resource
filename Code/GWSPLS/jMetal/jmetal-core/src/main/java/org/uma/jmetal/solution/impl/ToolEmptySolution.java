package org.uma.jmetal.solution.impl;

import org.uma.jmetal.problem.DoubleProblem;

@SuppressWarnings("serial")
public class ToolEmptySolution extends DefaultDoubleSolution{

	private int index;
	
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public ToolEmptySolution(DoubleProblem emptyProblem, int index) {
		super(emptyProblem);
		this.index = index;
	}

}
