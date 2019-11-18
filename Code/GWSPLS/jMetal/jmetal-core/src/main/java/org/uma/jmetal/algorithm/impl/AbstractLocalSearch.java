package org.uma.jmetal.algorithm.impl;

import java.util.ArrayList;
import java.util.List;

import org.uma.jmetal.operator.NeighborSearchOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;

@SuppressWarnings("serial")
public abstract class AbstractLocalSearch<S extends Solution<?>, Result> extends AbstractEvolutionaryAlgorithm<S, Result> {
	protected NeighborSearchOperator<S> neighborSearchOperator ;
	
	
	public AbstractLocalSearch(Problem<S> problem) {
		setProblem(problem);
	}
	
	@Override
	protected List<S> selection(List<S> population) {
		return population;
	}
	
	@Override
	protected List<S> reproduction(List<S> population) {
		List<S> offsprings = new ArrayList<>();

		for(S solution : population){
			List<S> offspring = neighborSearchOperator.execute(solution);
			offsprings.addAll(offspring);
		}
		return offsprings;
	}
	
	
}
