package com.nuaa.shr.pls.algorithm.momad;

import org.uma.jmetal.operator.NeighborSearchOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.AlgorithmBuilder;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;

import com.nuaa.shr.pls.algorithm.momad.MOMAD.InitialSolutionMethod;


public class MOMADBuilder <S extends Solution<?>> implements AlgorithmBuilder<MOMAD<S>>{

	protected NeighborSearchOperator<S> neighborSearchOperator;
	private int maxPopulationSize;
	private Problem<S> problem;
	private int maxIteration;
	private int maxLocalSearchTime;
	private SolutionListEvaluator<S> evaluator;
	private InitialSolutionMethod initialSolutionMethod;
	
	public MOMADBuilder(Problem<S> problem) {
		this.problem = problem ;
		this.maxPopulationSize = 300;
		this.maxLocalSearchTime = Integer.MAX_VALUE ;
		this.maxIteration = 200;
		this.evaluator = new SequentialSolutionListEvaluator<>();
		this.initialSolutionMethod = InitialSolutionMethod.RANDOM_METHOD ;
		
	}
	
	public NeighborSearchOperator<S> getNeighborSearchOperator() {
		return neighborSearchOperator;
	}

	public MOMADBuilder<S> setNeighborSearchOperator(NeighborSearchOperator<S> neighborSearchOperator) {
		this.neighborSearchOperator = neighborSearchOperator;
		return this ;
	}

	public int getMaxPopulationSize() {
		return maxPopulationSize;
	}

	public MOMADBuilder<S> setMaxPopulationSize(int maxPopulationSize) {
		this.maxPopulationSize = maxPopulationSize;
		return this ;
	}

	public int getMaxIteration() {
		return maxIteration;
	}

	public MOMADBuilder<S> setMaxIteration(int maxIteration) {
		this.maxIteration = maxIteration;
		return this ;
	}

	public int getMaxLocalSearchTime() {
		return maxLocalSearchTime;
	}

	public MOMADBuilder<S> setMaxLocalSearchTime(int maxLocalSearchTime) {
		this.maxLocalSearchTime = maxLocalSearchTime;
		return this ;
	}

	public SolutionListEvaluator<S> getEvaluator() {
		return evaluator;
	}

	public MOMADBuilder<S> setEvaluator(SolutionListEvaluator<S> evaluator) {
		this.evaluator = evaluator;
		return this ;
	}

	public InitialSolutionMethod getInitialSolutionMethod() {
		return initialSolutionMethod;
	}

	public MOMADBuilder<S> setInitialSolutionMethod(InitialSolutionMethod initialSolutionMethod) {
		this.initialSolutionMethod = initialSolutionMethod;
		return this ;
	}

	public Problem<S> getProblem() {
		return problem;
	}

	@Override
	public MOMAD<S> build() {
		if(neighborSearchOperator == null)
			throw new JMetalException("search operator must be defined!");
		return new MOMAD<S>(problem, maxPopulationSize, maxIteration, 
				maxLocalSearchTime, neighborSearchOperator, evaluator, initialSolutionMethod);
	}

}
