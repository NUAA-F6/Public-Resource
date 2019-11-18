package com.nuaa.shr.pls.algorithm.nepsilonls;

import org.uma.jmetal.operator.NeighborSearchOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.AlgorithmBuilder;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;

import com.nuaa.shr.pls.algorithm.nepsilonls.EpsilonLocalSearch.InitialSolutionMethod;

public class EpsilonLocalSearchBuilder<S extends Solution<?>> implements AlgorithmBuilder<EpsilonLocalSearch<S>> {
	private final Problem<S> problem;
	private int maxLocalSearchTime;
	private int maxIteration;
	private double epsilon;
	private SolutionListEvaluator<S> evaluator; 
	private int maxPopulationSize;
	private NeighborSearchOperator<S> neighborSearchOperator ; 
	private InitialSolutionMethod initializeSolutionMethod ;
	
	public EpsilonLocalSearchBuilder(Problem<S> problem) {
		this.problem = problem ;
		this.maxIteration = 200;
		this.maxLocalSearchTime = Integer.MAX_VALUE;
		this.maxPopulationSize = 100;
		this.epsilon = 0.01 ;
		initializeSolutionMethod = InitialSolutionMethod.RANDOM_METHOD;
		evaluator = new SequentialSolutionListEvaluator<S>();
	}
	
	@Override
	public EpsilonLocalSearch<S> build() {
		if(neighborSearchOperator == null){
			throw new JMetalException("Neighbor search operator must be set!");
		}
		return new EpsilonLocalSearch<S>(problem, maxPopulationSize, maxLocalSearchTime, 
				maxIteration, epsilon, evaluator, neighborSearchOperator,initializeSolutionMethod);
				
	}
	
	
	public InitialSolutionMethod getInitializeSolutionMethod() {
		return initializeSolutionMethod;
	}

	public EpsilonLocalSearchBuilder<S> setInitializeSolutionMethod(InitialSolutionMethod initializeSolutionMethod) {
		this.initializeSolutionMethod = initializeSolutionMethod;
		return this ;
	}

	public NeighborSearchOperator<S> getNeighborSearchOperator() {
		return neighborSearchOperator;
	}

	public EpsilonLocalSearchBuilder<S> setNeighborSearchOperator(NeighborSearchOperator<S> neighborSearchOperator) {
		this.neighborSearchOperator = neighborSearchOperator;
		return this ;
	}

	public SolutionListEvaluator<S> getEvaluator() {
		return evaluator;
	}
	public EpsilonLocalSearchBuilder<S> setEvaluator(SolutionListEvaluator<S> evaluator) {
		this.evaluator = evaluator;
		return this ;
	}
	public int getMaxPopulationSize() {
		return maxPopulationSize;
	}
	public EpsilonLocalSearchBuilder<S> setMaxPopulationSize(int maxPopulationSize) {
		this.maxPopulationSize = maxPopulationSize;
		return this;
	}
	public int getMaxLocalSearchTime() {
		return maxLocalSearchTime;
	}
	public EpsilonLocalSearchBuilder<S> setMaxLocalSearchTime(int maxLocalSearchTime) {
		this.maxLocalSearchTime = maxLocalSearchTime;
		return this;
	}
	public int getMaxIteration() {
		return maxIteration;
	}
	public EpsilonLocalSearchBuilder<S> setMaxIteration(int maxIteration) {
		this.maxIteration = maxIteration;
		return this;
	}
	public double getEpsilon() {
		return epsilon;
	}
	public EpsilonLocalSearchBuilder<S> setEpsilon(double epsilon) {
		this.epsilon = epsilon;
		return this;
	}
	
}
