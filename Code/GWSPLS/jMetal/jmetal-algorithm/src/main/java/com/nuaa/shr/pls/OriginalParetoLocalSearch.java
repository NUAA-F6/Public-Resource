package com.nuaa.shr.pls;

import java.util.ArrayList;
import java.util.List;

import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.PermutationSolution;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.comparator.DominanceComparator;

import com.nuaa.shr.pls.utils.PlsSolutionUtils;

@SuppressWarnings("serial")
public class OriginalParetoLocalSearch extends AbstractParetoLocalSearch<PermutationSolution<Integer>> {

	int count = 0;

	public OriginalParetoLocalSearch(Problem<PermutationSolution<Integer>> problem, int maxIteration,
			int populationSize) {
		super(problem, maxIteration, populationSize);
	}

	@Override
	public void run() {
		List<PermutationSolution<Integer>> initPopulation = createInitialPopulation();
		// 评估目标值
		evaluator.evaluate(initPopulation, problem);

		workPopulation = SolutionListUtils.getNondominatedSolutions(initPopulation);
		externalPopulation = new ArrayList<PermutationSolution<Integer>>();
		externalPopulation.addAll(workPopulation);
		updateCandidateEdges();

		iteration = 0;
		while (workPopulation.size() != 0 && iteration++ < this.maxIteration) {

			List<PermutationSolution<Integer>> tempList = new ArrayList<>();
			long time = System.currentTimeMillis();
			for (int index = 0; index < workPopulation.size(); index++) {
				PermutationSolution<Integer> solution = workPopulation.get(index);
				searchNeighborhood(solution, tempList);
			}
			System.out.println("search time: "+(System.currentTimeMillis()-time));
			workPopulation.clear();
			workPopulation.addAll(tempList);
			System.out.println(iteration + " iteration\n");

			// 更新候选边集
			updateCandidateEdges();

//			new SolutionListOutput(externalPopulation).setSeparator("\t")
//					.setFunFileOutputContext(new DefaultFileOutputContext("results/"+getName()+"/FUN" + iteration)).print();
		}
	}


	/**
	 * Search the neighborhood
	 * @param solution
	 * @param tempList
	 */
	private void searchNeighborhood(PermutationSolution<Integer> solution, List<PermutationSolution<Integer>> tempList){
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
						PermutationSolution<Integer> neighborSolution = getANeighborSolution(solution, i, j);
						DominanceComparator<Solution<?>> comparator = new DominanceComparator<>();
						int dominateFlag = comparator.compare(solution, neighborSolution);
						if (dominateFlag >= 0) {
							if (addToExternalPopulation(neighborSolution)) {
								addToTempPopulation(neighborSolution, tempList);
							}
						}
				}
			}
		}
	}

	
	protected boolean addToExternalPopulation(PermutationSolution<Integer> neighborSolution) {
		if (!PlsSolutionUtils.isDominatedByList(neighborSolution, externalPopulation)
				&& !PlsSolutionUtils.isEqualsToList(neighborSolution, externalPopulation)) {
			externalPopulation.add(neighborSolution);
			reduceDominatedInList(neighborSolution, externalPopulation);
			return true;
		} else
			return false;
	}
	/**
	 * 添加到临时population中，临时population的解将在循环的末尾赋值给workPopulation
	 * @param neighborSolution
	 * @param tempList
	 * @return
	 */
	private boolean addToTempPopulation(PermutationSolution<Integer> neighborSolution,
			List<PermutationSolution<Integer>> tempList) {
		if (!PlsSolutionUtils.isDominatedByList(neighborSolution, tempList)) {
			tempList.add(neighborSolution);
			reduceDominatedInList(neighborSolution, tempList);
			return true;
		} else
			return false;
	}

	/**
	 * 获得邻居解
	 * @param solution
	 * @param startIndex 断开的位置
	 * @param endIndex 断开结束的位置
	 * @return
	 */
	public PermutationSolution<Integer> getANeighborSolution(PermutationSolution<Integer> solution, int startIndex,
			int endIndex) {
		PermutationSolution<Integer> neighborSolution = this.problem.createSolution();
		int pos;
		for(pos = 0;pos<startIndex+1;pos++){
			neighborSolution.setVariableValue(pos, solution.getVariableValue(pos));
		}
		//将子串倒序复制给neighborSolution
		int k = endIndex;
		for(pos = startIndex+1; pos<endIndex+1; pos++){
			neighborSolution.setVariableValue(pos, solution.getVariableValue(k--));
		}
		for(pos = endIndex+1;pos<solution.getNumberOfVariables();pos++){
			neighborSolution.setVariableValue(pos, solution.getVariableValue(pos));
		}
		this.problem.evaluate(neighborSolution);
		return neighborSolution;
	}
	
	@Override
	public List<PermutationSolution<Integer>> getResult() {
		return externalPopulation;
	}

	@Override
	public String getName() {
		return "OriginalPLS";
	}

	@Override
	public String getDescription() {
		return "Original Pareto Local Search";
	}

}
