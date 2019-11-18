package com.nuaa.shr.pls.algorithm.epsilonls;

import java.util.ArrayList;
import java.util.List;

import org.uma.jmetal.algorithm.impl.AbstractLocalSearch;
import org.uma.jmetal.operator.impl.neighborsearch.TwoOptNeighborSearch;
import org.uma.jmetal.problem.multiobjective.ManyObjectiveTSP;
import org.uma.jmetal.solution.PermutationSolution;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;

@SuppressWarnings("serial")
public abstract class AbstractLocalSearchTSP extends AbstractLocalSearch<PermutationSolution<Integer>, List<PermutationSolution<Integer>>>{
	private int[][] candidateEdges ;
	protected double searchProbility ;
	protected SolutionListEvaluator<PermutationSolution<Integer>> evaluator;
	
	public AbstractLocalSearchTSP(ManyObjectiveTSP problem) {
		super(problem);
		candidateEdges = new int[problem.getNumberOfVariables()][problem.getNumberOfVariables()];
	}
	
	@Override
	protected List<PermutationSolution<Integer>> createInitialPopulation() {
	    List<PermutationSolution<Integer>> population = new ArrayList<>(getMaxPopulationSize());
	    for (int i = 0; i < getMaxPopulationSize(); i++) {
	      PermutationSolution<Integer> newIndividual = getProblem().createSolution();
	      population.add(newIndividual);
	    }
	    return population;
	}
	
	@Override
	protected List<PermutationSolution<Integer>> evaluatePopulation(List<PermutationSolution<Integer>> population) {
		//the population have been evaluated in local search
		
		population = evaluator.evaluate(population, getProblem());
		
	    return population;
	}
	
	@Override
	protected void updateProgress() {
		updateCandidateEdges();
		updateOtherProgress();
	}

	@Override
	protected void initProgress() {
		initOtherProgress();
		updateCandidateEdges();
	}
	
	protected abstract void initOtherProgress();
	protected abstract void updateOtherProgress();

	private void updateCandidateEdges() {
		clearCandidateEdges();// 先清除候选边集

		// 将所有在外部集中的边加入到候选边集
		for (int i = 0; i < getPopulation().size(); i++) {
			PermutationSolution<Integer> solution = getPopulation().get(i);
			for (int j = 0; j < solution.getNumberOfVariables() - 1; j++) {
				int frontNode = (int) solution.getVariableValue(j);
				int nextNode = (int) solution.getVariableValue(j + 1);
				candidateEdges[frontNode][nextNode] = 1;
				candidateEdges[nextNode][frontNode] = 1;
			}
			candidateEdges[solution.getVariableValue(0)][solution.getVariableValue(solution.getNumberOfVariables()-1)] = 1; 
			candidateEdges[solution.getVariableValue(solution.getNumberOfVariables()-1)][solution.getVariableValue(0)] = 1; 
		}
		
		((TwoOptNeighborSearch)neighborSearchOperator).setCandidateEdges(candidateEdges);
	}
	
	private void clearCandidateEdges() {
		for (int i = 0; i < getProblem().getNumberOfVariables(); i++) {
			for (int j = 0; j < getProblem().getNumberOfVariables(); j++) {
				candidateEdges[i][j] = 0;
			}
		}
	}
}
