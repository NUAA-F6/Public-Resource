package com.nuaa.shr.pls.algorithm.moeadls;

import org.uma.jmetal.operator.NeighborSearchOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.AlgorithmBuilder;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;

import com.nuaa.shr.pls.algorithm.moeadls.MoeadLs.FunctionType;
import com.nuaa.shr.pls.algorithm.moeadls.MoeadLs.InitialSolutionMethod;

public class MoeadLsBuilder<S extends Solution<?>> implements AlgorithmBuilder<MoeadLs<S>> {
	private int maxIteration ;
	private int maxLocalSearchTime;
	private InitialSolutionMethod initialSolutionMethod ;
	private Problem<S> problem ;
	private FunctionType functionType ;
	private int maxPopulationSize ;
	private SolutionListEvaluator<S> evaluator; 
	private NeighborSearchOperator<S> neighborSearchOperator ; 
	
	public MoeadLsBuilder(Problem<S> problem) {
		this.problem = problem ;
		this.maxIteration = 200 ;
		this.maxPopulationSize = 300;
		this.maxLocalSearchTime = Integer.MAX_VALUE ;
		this.functionType = FunctionType.WS ;
		initialSolutionMethod = InitialSolutionMethod.RANDOM_METHOD;
		evaluator = new SequentialSolutionListEvaluator<S>();
	}
	@Override
	public MoeadLs<S> build() {
		if(neighborSearchOperator == null)
			throw new JMetalException("must set operator for local search!");
		return new MoeadLs<>(problem, maxPopulationSize, 
				maxLocalSearchTime, maxIteration, evaluator, 
				neighborSearchOperator, initialSolutionMethod, functionType);
	}
	public int getMaxIteration() {
		return maxIteration;
	}
	public MoeadLsBuilder<S> setMaxIteration(int maxIteration) {
		this.maxIteration = maxIteration;
		return this ;
	}
	public int getMaxLocalSearchTime() {
		return maxLocalSearchTime;
	}
	public MoeadLsBuilder<S> setMaxLocalSearchTime(int maxLocalSearchTime) {
		this.maxLocalSearchTime = maxLocalSearchTime;
		return this ;
	}
	public InitialSolutionMethod getInitialSolutionMethod() {
		return initialSolutionMethod;
	}
	public MoeadLsBuilder<S> setInitialSolutionMethod(InitialSolutionMethod initialSolutionMethod) {
		this.initialSolutionMethod = initialSolutionMethod;
		return this ;
	}
	public Problem<S> getProblem() {
		return problem;
	}
	
	public FunctionType getFunctionType() {
		return functionType;
	}
	public MoeadLsBuilder<S> setFunctionType(FunctionType functionType) {
		this.functionType = functionType;
		return this;
	}
	public int getMaxPopulationSize() {
		return maxPopulationSize;
	}
	public MoeadLsBuilder<S> setMaxPopulationSize(int maxPopulationSize) {
		this.maxPopulationSize = maxPopulationSize;
		return this ;
	}
	public SolutionListEvaluator<S> getEvaluator() {
		return evaluator;
	}
	public MoeadLsBuilder<S> setEvaluator(SolutionListEvaluator<S> evaluator) {
		this.evaluator = evaluator;
		return this ;
	}
	public NeighborSearchOperator<S> getNeighborSearchOperator() {
		return neighborSearchOperator;
	}
	public MoeadLsBuilder<S> setNeighborSearchOperator(NeighborSearchOperator<S> neighborSearchOperator) {
		this.neighborSearchOperator = neighborSearchOperator;
		return this ;
	}
	
	
	
}
