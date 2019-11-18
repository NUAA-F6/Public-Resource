package com.nuaa.shr.pls.algorithm.gplsd;

import org.uma.jmetal.operator.NeighborSearchOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.AlgorithmBuilder;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;

import com.nuaa.shr.pls.algorithm.gplsd.GPLSD.InitialSolutionMethod;


public class GPLSDBuillder<S extends Solution<?>> implements AlgorithmBuilder<GPLSD<S>>{
	private final Problem<S> problem;
	private int maxLocalSearchTime;
	private int maxIteration;
	private int numberOfGrid;
	private SolutionListEvaluator<S> evaluator; 
	private int maxPopulationSize;
	private NeighborSearchOperator<S> neighborSearchOperator ; 
	private InitialSolutionMethod initializeSolutionMethod ;
	
	public GPLSDBuillder(Problem<S> problem) {
		this.problem = problem;
		this.maxIteration = 200;
		this.maxLocalSearchTime = Integer.MAX_VALUE;
		this.maxPopulationSize = 100;
		this.numberOfGrid = 200 ;
		initializeSolutionMethod = InitialSolutionMethod.RANDOM_METHOD;
		evaluator = new SequentialSolutionListEvaluator<S>();
	}
	
	@Override
	public GPLSD<S> build() {
		return new GPLSD<S>(problem, maxPopulationSize, 
				maxLocalSearchTime, maxIteration, 
				numberOfGrid, evaluator, neighborSearchOperator, initializeSolutionMethod);
	}

	
	public InitialSolutionMethod getInitializeSolutionMethod() {
		return initializeSolutionMethod;
	}

	public GPLSDBuillder<S> setInitializeSolutionMethod(InitialSolutionMethod initializeSolutionMethod) {
		this.initializeSolutionMethod = initializeSolutionMethod;
		return this ;
	}

	public NeighborSearchOperator<S> getNeighborSearchOperator() {
		return neighborSearchOperator;
	}

	public GPLSDBuillder<S> setNeighborSearchOperator(NeighborSearchOperator<S> neighborSearchOperator) {
		this.neighborSearchOperator = neighborSearchOperator;
		return this ;
	}

	public SolutionListEvaluator<S> getEvaluator() {
		return evaluator;
	}
	public GPLSDBuillder<S> setEvaluator(SolutionListEvaluator<S> evaluator) {
		this.evaluator = evaluator;
		return this ;
	}
	public int getMaxPopulationSize() {
		return maxPopulationSize;
	}
	public GPLSDBuillder<S> setMaxPopulationSize(int maxPopulationSize) {
		this.maxPopulationSize = maxPopulationSize;
		return this;
	}
	public int getMaxLocalSearchTime() {
		return maxLocalSearchTime;
	}
	public GPLSDBuillder<S> setMaxLocalSearchTime(int maxLocalSearchTime) {
		this.maxLocalSearchTime = maxLocalSearchTime;
		return this;
	}
	public int getMaxIteration() {
		return maxIteration;
	}
	public GPLSDBuillder<S> setMaxIteration(int maxIteration) {
		this.maxIteration = maxIteration;
		return this;
	}

	public int getNumberOfGrid() {
		return numberOfGrid;
	}

	public GPLSDBuillder<S> setNumberOfGrid(int numberOfGrid) {
		this.numberOfGrid = numberOfGrid;
		return this ;
	}
	
}
