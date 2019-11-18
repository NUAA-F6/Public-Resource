package org.uma.jmetal.algorithm.impl;

import java.util.ArrayList;
import java.util.List;

import org.uma.jmetal.operator.NeighborSearchOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;

@SuppressWarnings("serial")
public abstract class AbstractLocalSearchSingle<S extends Solution<?>, Result> extends AbstractEvolutionarySingle<S, Result> {
	
	protected NeighborSearchOperator<S> neighborSearchOperator ;
	
	protected int currentIterationSearchPos ;
	

	
	public AbstractLocalSearchSingle(Problem<S> problem) {
		setProblem(problem);
	}
	
	@Override
	protected List<S> selection(List<S> population) {
		List<S> solutionList = new ArrayList<>();
		solutionList.add(population.get(currentIterationSearchPos++));
		return solutionList ;
	}
	
	@Override
	protected boolean isGeneAllOffspring() {
		return currentIterationSearchPos >= getPopulation().size();
	}
	
	@Override
	protected List<S> reproduction(List<S> matingPopulation) {
		S solution = matingPopulation.get(0);
		List<S> offSpring = neighborSearchOperator.execute(solution);
		return offSpring;
	}
	
}
