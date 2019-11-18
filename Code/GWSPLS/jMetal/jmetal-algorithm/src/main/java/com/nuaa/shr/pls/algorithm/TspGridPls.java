package com.nuaa.shr.pls.algorithm;

import java.util.List;

import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.GridPermutationSolution;
import org.uma.jmetal.util.comparator.DominanceComparator;

import com.nuaa.shr.pls.algorithm.abst.GrParetoLocalSearch;

@SuppressWarnings("serial")
public class TspGridPls extends GrParetoLocalSearch{

	private int[][] candidateEdge;
	
	public TspGridPls(Problem<GridPermutationSolution<Integer>> problem, int maxIteration, int populationSize, int numberOfGrid) {
		super(problem, maxIteration, populationSize, numberOfGrid);
		candidateEdge = new int[problem.getNumberOfVariables()][problem.getNumberOfVariables()];
	}

	/**
	 * 更新边的候选集
	 */
	protected void updateCandidateEdges() {
		clearCandidateEdges();// 先清除候选边集

		// 将所有在外部集中的边加入到候选边集
		for (int i = 0; i < workPopulation.size(); i++) {
			GridPermutationSolution<Integer> solution = workPopulation.get(i);
			for (int j = 0; j < solution.getNumberOfVariables() - 1; j++) {
				int frontNode = (int) solution.getVariableValue(j);
				int nextNode = (int) solution.getVariableValue(j + 1);
				candidateEdge[frontNode][nextNode] = 1;
				candidateEdge[nextNode][frontNode] = 1;
			}
		}
	}

	private void clearCandidateEdges() {
		for (int i = 0; i < problem.getNumberOfVariables(); i++) {
			for (int j = 0; j < problem.getNumberOfVariables(); j++) {
				candidateEdge[i][j] = 0;
			}
		}
	}
	
	@Override
	public String getName() {
		return "tsp grid pls";
	}

	@Override
	public String getDescription() {
		return "tsp grid pls";
	}

	
	
	@Override
	protected void searchNeighborhood(GridPermutationSolution<Integer> solution,
			List<GridPermutationSolution<Integer>> tempList) {
		for (int i = 0; i < problem.getNumberOfVariables() - 2; i++) {
			for (int j = i + 2; j < problem.getNumberOfVariables(); j++) {
				int subLastNode = (int) solution.getVariableValue(j);// 子串的最后一个节点
				int firstNode = (int) solution.getVariableValue(i); // 主串断开开始的节点
				int subFirstNode = (int) solution.getVariableValue(i + 1);// 子串的第一个节点
				int lastNode;
				if (j < problem.getNumberOfVariables() - 1)
					lastNode = (int) solution.getVariableValue(j + 1);// 主串断开结束的节点
				else
					lastNode = (int) solution.getVariableValue(0);

				if (candidateEdge[firstNode][subLastNode] == 1 || candidateEdge[subFirstNode][lastNode] == 1) {
					GridPermutationSolution<Integer> neighborSolution = getANeighborSolution(solution, i, j);
					setGridCoordinate(neighborSolution);
					DominanceComparator<GridPermutationSolution<Integer>> dominateComparator = new DominanceComparator<>();
					int dominateFlag = dominateComparator.compare(solution, neighborSolution);
					if(dominateFlag >= 0){
						if(addSolutionToExternal(neighborSolution)){
							reduceDominatedInList(neighborSolution, externalPopulation);
							addToTempPopulation(neighborSolution, tempList);
						}
					}
				}
			}
		}
		
	}

	public GridPermutationSolution<Integer> getANeighborSolution(GridPermutationSolution<Integer> solution,
			int startIndex, int endIndex) {
		GridPermutationSolution<Integer> neighborSolution = this.problem.createSolution();
		int pos;
		for (pos = 0; pos < startIndex + 1; pos++) {
			neighborSolution.setVariableValue(pos, solution.getVariableValue(pos));
		}
		// 将子串倒序复制给neighborSolution
		int k = endIndex;
		for (pos = startIndex + 1; pos < endIndex + 1; pos++) {
			neighborSolution.setVariableValue(pos, solution.getVariableValue(k--));
		}
		for (pos = endIndex + 1; pos < solution.getNumberOfVariables(); pos++) {
			neighborSolution.setVariableValue(pos, solution.getVariableValue(pos));
		}
		this.problem.evaluate(neighborSolution);
		return neighborSolution;
	}
	
	@Override
	protected void updateSomething() {
		updateCandidateEdges();
		
	}

}
