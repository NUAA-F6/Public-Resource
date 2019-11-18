package org.uma.jmetal.operator.impl.neighborsearch;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.uma.jmetal.operator.NeighborSearchOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.PermutationSolution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.comparator.DominanceComparator;

@SuppressWarnings("serial")
public class TwoOptNeighborSearch implements NeighborSearchOperator<PermutationSolution<Integer>>{

	private double searchProbility = 1.0;
	private int[][] candidateEdges = null;
	private Problem<PermutationSolution<Integer>> problem;
	
	private Comparator<PermutationSolution<Integer>> comparator ; 
	
	public TwoOptNeighborSearch(double searchProbility, 
			Problem<PermutationSolution<Integer>> problem) {
		if ((searchProbility < 0) || (searchProbility > 1)) {
			throw new JMetalException("Search probability value invalid: " + searchProbility);
		}
		this.searchProbility = searchProbility;
		this.problem = problem ;
		comparator = new DominanceComparator<>();
	}
	
	/**
	 * return all neighborhoods for {@code solution}
	 */
	@Override
	public List<PermutationSolution<Integer>> execute(PermutationSolution<Integer> solution) {
		return doSearch(solution);
	}
	
	private List<PermutationSolution<Integer>> doSearch(PermutationSolution<Integer> solution) {
		List<PermutationSolution<Integer>> neighbors = new ArrayList<>();
		int numOfVar = solution.getNumberOfVariables();
		for (int startIndex = 0; startIndex < numOfVar - 2; startIndex++) {
			for (int endIndex = startIndex + 2; endIndex < numOfVar; endIndex++) {
				if(Math.random()<searchProbility){
					int subLastNode = (int) solution.getVariableValue(endIndex);// 子串的最后一个节点
					int firstNode = (int) solution.getVariableValue(startIndex); // 主串断开开始的节点
					int subFirstNode = (int) solution.getVariableValue(startIndex + 1);// 子串的第一个节点
					int lastNode;
					if (endIndex < solution.getNumberOfVariables() - 1)
						lastNode = (int) solution.getVariableValue(endIndex + 1);// 主串断开结束的节点
					else
						lastNode = (int) solution.getVariableValue(0);
					
					if(candidateEdges!=null && candidateEdges[firstNode][subLastNode] == 0 &&
							candidateEdges[lastNode][subFirstNode]==0){
						continue;
					}
					else{
						PermutationSolution<Integer> neighbor = (PermutationSolution<Integer>) solution.copy();
						for(int subPos = startIndex + 1, mainPos = endIndex;subPos<=endIndex;subPos++,mainPos--){
							neighbor.setVariableValue(subPos, solution.getVariableValue(mainPos));
						}
						problem.evaluate(neighbor);
						if (comparator != null) {
							if (comparator.compare(solution, neighbor) >= 0)
								neighbors.add(neighbor);
						} else
							neighbors.add(neighbor);
					}
				}
			}
		}

		return neighbors;
	}

	/**
	 * set candidate edges for neighborhood search,
	 * if it not be set, all neighborhood will be find 
	 * @param candidateEdges
	 */
	public void setCandidateEdges(int[][] candidateEdges){
		this.candidateEdges = candidateEdges;
	}

	@Override
	public void setCandidate(List<PermutationSolution<Integer>> population) {
		if (population.size() > 0) {
			int numberOfVariable = population.get(0).getNumberOfVariables();
			candidateEdges = new int[numberOfVariable][numberOfVariable];

			// 将所有在工作集中的边加入到候选边集
			for (int i = 0; i < population.size(); i++) {
				PermutationSolution<Integer> solution = population.get(i);
				for (int j = 0; j < solution.getNumberOfVariables() - 1; j++) {
					int frontNode = (int) solution.getVariableValue(j);
					int nextNode = (int) solution.getVariableValue(j + 1);
					candidateEdges[frontNode][nextNode] = 1;
					candidateEdges[nextNode][frontNode] = 1;
				}
			}
		}
	}
	
	@Override
	public void setComparator(Comparator<PermutationSolution<Integer>> comparator){
		this.comparator = comparator ;
	}

}
