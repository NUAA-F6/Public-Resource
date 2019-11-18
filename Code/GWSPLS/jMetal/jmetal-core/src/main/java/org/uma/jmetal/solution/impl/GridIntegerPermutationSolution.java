package org.uma.jmetal.solution.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.uma.jmetal.problem.GridPermutationProblem;
import org.uma.jmetal.solution.GridPermutationSolution;
import org.uma.jmetal.solution.Solution;

@SuppressWarnings("serial")
public class GridIntegerPermutationSolution extends 
	AbstractGenericSolution<Integer, GridPermutationProblem<?>>
		implements GridPermutationSolution<Integer>{

	private int[] gridCoordinates; 
	
	private int[] constraints;
	
	public GridIntegerPermutationSolution(GridPermutationProblem<?> problem) {
		super(problem);
		gridCoordinates = new int [problem.getNumberOfObjectives()];
	    List<Integer> randomSequence = new ArrayList<>(problem.getPermutationLength());

	    for (int j = 0; j < problem.getPermutationLength(); j++) {
	      randomSequence.add(j);
	    }

	    java.util.Collections.shuffle(randomSequence);

	    for (int i = 0; i < getNumberOfVariables(); i++) {
	      setVariableValue(i, randomSequence.get(i)) ;
	    }
	}

	
	  /** Copy Constructor */
	  public GridIntegerPermutationSolution(GridIntegerPermutationSolution solution) {
	    super(solution.problem) ;
	    gridCoordinates = new int [problem.getNumberOfObjectives()];
	    for (int i = 0; i < problem.getNumberOfObjectives(); i++) {
	      setObjective(i, solution.getObjective(i)) ;
	    }

	    for (int i = 0; i < problem.getNumberOfVariables(); i++) {
	      setVariableValue(i, solution.getVariableValue(i));
	    }
	    
	    for(int i = 0;i<problem.getNumberOfObjectives();i++){
	    	setGridCoordinate(i, solution.getGridCoordinate(i));
	    }
	    //overallConstraintViolationDegree = solution.overallConstraintViolationDegree ;
	    //numberOfViolatedConstraints = solution.numberOfViolatedConstraints ;

	    attributes = new HashMap<Object, Object>(solution.attributes) ;
	  }



	@Override
	public String getVariableValueString(int index) {
		return getVariableValue(index).toString();
	}

	@Override
	public Solution<Integer> copy() {
		return new GridIntegerPermutationSolution(this);
	}

	@Override
	public void setGridCoordinate(int index, int value) {
		gridCoordinates[index] = value;
		
	}

	@Override
	public int getGridCoordinate(int index) {
		return gridCoordinates[index];
	}
	
	@Override
	public void setConstraint(int index, int value) {
		constraints[index] = value;
		
	}
	@Override
	public int getConstraint(int index) {
		return constraints[index];
	}

}
