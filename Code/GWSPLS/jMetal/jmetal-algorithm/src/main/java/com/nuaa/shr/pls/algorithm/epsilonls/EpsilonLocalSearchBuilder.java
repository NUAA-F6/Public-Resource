package com.nuaa.shr.pls.algorithm.epsilonls;

import org.uma.jmetal.operator.NeighborSearchOperator;
import org.uma.jmetal.operator.impl.neighborsearch.TwoOptNeighborSearch;
import org.uma.jmetal.problem.multiobjective.ManyObjectiveTSP;
import org.uma.jmetal.solution.PermutationSolution;
import org.uma.jmetal.util.AlgorithmBuilder;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;

public class EpsilonLocalSearchBuilder implements AlgorithmBuilder<EpsilonLocalSearchTSP>{
	private final ManyObjectiveTSP problem;
	private int maxLocalSearchTime;
	private int maxIteration;
	private double epsilon;
	private SolutionListEvaluator<PermutationSolution<Integer>> evaluator; 
	private double searchProbility ;
	private int maxPopulationSize;
	private NeighborSearchOperator<PermutationSolution<Integer>> neighborSearchOperator ; 
	
	public SolutionListEvaluator<PermutationSolution<Integer>> getEvaluator() {
		return evaluator;
	}
	public EpsilonLocalSearchBuilder setEvaluator(SolutionListEvaluator<PermutationSolution<Integer>> evaluator) {
		this.evaluator = evaluator;
		return this ;
	}
	public int getMaxPopulationSize() {
		return maxPopulationSize;
	}
	public EpsilonLocalSearchBuilder setMaxPopulationSize(int maxPopulationSize) {
		this.maxPopulationSize = maxPopulationSize;
		return this;
	}
	public int getMaxLocalSearchTime() {
		return maxLocalSearchTime;
	}
	public EpsilonLocalSearchBuilder setMaxLocalSearchTime(int maxLocalSearchTime) {
		this.maxLocalSearchTime = maxLocalSearchTime;
		return this;
	}
	public int getMaxIteration() {
		return maxIteration;
	}
	public EpsilonLocalSearchBuilder setMaxIteration(int maxIteration) {
		this.maxIteration = maxIteration;
		return this;
	}
	public double getEpsilon() {
		return epsilon;
	}
	public EpsilonLocalSearchBuilder setEpsilon(double epsilon) {
		this.epsilon = epsilon;
		return this;
	}

	public double getSearchProbility() {
		return searchProbility;
	}
	public EpsilonLocalSearchBuilder setSearchProbility(double searchProbility) {
		this.searchProbility = searchProbility;
		//重新设置searchProbility后要重设设置搜索的概率
		this.neighborSearchOperator = new TwoOptNeighborSearch(searchProbility, getProblem());
		return this;
	}
	public ManyObjectiveTSP getProblem() {
		return problem;
	}
	
	public EpsilonLocalSearchBuilder(ManyObjectiveTSP problem) {
		this.problem = problem;
		this.maxIteration = 200;
		this.maxLocalSearchTime = 30000;
		this.maxPopulationSize = 100;
		this.epsilon = 0.01 ;
		this.searchProbility = 1.0;
		neighborSearchOperator = new TwoOptNeighborSearch(searchProbility, getProblem());
		
		evaluator = new SequentialSolutionListEvaluator<PermutationSolution<Integer>>();
		maxLocalSearchTime = Integer.MAX_VALUE;
	}
	@Override
	public EpsilonLocalSearchTSP build() {
		return new EpsilonLocalSearchTSP(problem, maxPopulationSize, maxLocalSearchTime, 
						maxIteration, epsilon, searchProbility, evaluator, neighborSearchOperator);
	}
	
}
