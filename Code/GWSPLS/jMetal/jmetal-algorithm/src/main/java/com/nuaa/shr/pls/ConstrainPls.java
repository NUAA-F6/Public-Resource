package com.nuaa.shr.pls;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.PermutationSolution;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.comparator.DominanceComparator;
import org.uma.jmetal.util.fileoutput.SolutionListOutput;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;

import com.nuaa.shr.pls.utils.PlsSolutionUtils;

@SuppressWarnings("serial")
public class ConstrainPls extends AbstractParetoLocalSearch<PermutationSolution<Integer>> {
	private static double DELTA_SIN = 0.1;

	public ConstrainPls(Problem<PermutationSolution<Integer>> problem, int maxIteration, int populationSize) {
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
		initializeIdealPoint();
		
		iteration = 0;
		while (iteration++ < this.maxIteration) {
//			double exponent = (double) (workPopulation.size()) / (double) (this.populationSize);
//			DELTA_SIN = Math.pow(Math.E * ((double) (this.populationSize) / 100.0), -exponent);
			double coef = (double) workPopulation.size() / (double) this.populationSize ;
			DELTA_SIN = DELTA_SIN / coef;
			if(DELTA_SIN > 1.0){
				DELTA_SIN = 1.0;
			}
//			if(workPopulation.size() > this.populationSize)
//			{
//				DELTA_SIN = DELTA_SIN / 2;
//			}
//			if(workPopulation.size() < this.populationSize/2)
//				DELTA_SIN = DELTA_SIN * 2;
			if(workPopulation.size() == 0 && externalPopulation.size() > 100)
			{
				DELTA_SIN = 1.0;
				List<Integer> randomIndex = new ArrayList<>();
				for(int i = 0; i<externalPopulation.size();i++){
					randomIndex.add(i);
				}
				Collections.shuffle(randomIndex);
				
				int remainSize = 100-workPopulation.size();
				
				for(int i = 0;i<remainSize;i++){
					workPopulation.add(externalPopulation.get(randomIndex.get(i)));
				}
				// 更新候选边集
				updateCandidateEdges();
			}
//			if(workPopulation.size() > this.populationSize*2){
//				DELTA_SIN = DELTA_SIN / 10;
//			}
			
			List<PermutationSolution<Integer>> tempList = new ArrayList<>();
			
			System.out.println("workPopulationSize: " + workPopulation.size() + " || " + "externalPopulationSize: "+externalPopulation.size());
			System.out.println("DELTA: " + DELTA_SIN);
			long time = System.currentTimeMillis();
			for (int index = 0; index < workPopulation.size(); index++) {
				PermutationSolution<Integer> solution = workPopulation.get(index);
				searchNeighborhood(solution, tempList);
			}
			System.out.println("search time: " + (System.currentTimeMillis() - time) + "ms");
			
			workPopulation.clear();
			workPopulation.addAll(tempList);
//			reduceExternal();
			System.out.println(iteration + " iteration\n");
			// 更新候选边集
			updateCandidateEdges();

			new SolutionListOutput(externalPopulation).setSeparator("\t")
					.setFunFileOutputContext(new DefaultFileOutputContext("results/" + getName() + "/FUN" + iteration))
					.print();
		}
	}

	
	private void reduceExternal() {
		if(externalPopulation.size()>this.populationSize){
			List<PermutationSolution<Integer>> reducedSolution = 
					PlsSolutionUtils.getBestDiversitySolutionsFromList(externalPopulation, this.populationSize);
			externalPopulation.clear();
			externalPopulation.addAll(reducedSolution);
		}
	}
	
	/**
	 * 添加到临时population中，临时population的解将在循环的末尾赋值给workPopulation
	 * 
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
	 * Search the neighborhood
	 * 
	 * @param solution
	 * @param tempList
	 */
	private void searchNeighborhood(PermutationSolution<Integer> solution,
			List<PermutationSolution<Integer>> tempList) {

		List<PermutationSolution<Integer>> outList = new ArrayList<>();
		outList.add(solution);

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
					
					updateIdealPoint(neighborSolution);
					outList.add(neighborSolution);
					
					DominanceComparator<Solution<?>> comparator = new DominanceComparator<>();
					int dominateFlag = comparator.compare(solution, neighborSolution);

					if (dominateFlag >= 0) {
						if (addToExternalPopulation(neighborSolution) && 
								( sin(neighborSolution, solution) < DELTA_SIN)) {
							addToTempPopulation(neighborSolution, tempList);
						}
					}
				}
			}
		}
	}

	private double sin(PermutationSolution<Integer> neighborSolution, PermutationSolution<Integer> solution) {
		double innerProduct = 0.0;
		double sumA = 0.0, sumB = 0.0;
		for (int i = 0; i < solution.getNumberOfObjectives(); i++) {
			double value1 = neighborSolution.getObjective(i) - idealPoint[i];
			double value2 = solution.getObjective(i) - idealPoint[i];
			innerProduct += value1 * value2;
			sumA += Math.pow(value1, 2);
			sumB += Math.pow(value2, 2);
		}
		double cos = innerProduct / (Math.sqrt(sumA) * Math.sqrt(sumB));
		double sin = Math.sqrt(1 - Math.pow(cos, 2));
		if (sin < 0)
			sin = 0;
		return sin;
	}

	/**
	 * 获得邻居解
	 * 
	 * @param solution
	 * @param startIndex
	 *            断开的位置
	 * @param endIndex
	 *            断开结束的位置
	 * @return
	 */
	public PermutationSolution<Integer> getANeighborSolution(PermutationSolution<Integer> solution, int startIndex,
			int endIndex) {
		PermutationSolution<Integer> neighborSolution = this.problem.createSolution();
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
	public String getName() {
		return "ConstrainPls";
	}

	@Override
	public String getDescription() {
		return "constrain Pareto Local Search";
	}

}
