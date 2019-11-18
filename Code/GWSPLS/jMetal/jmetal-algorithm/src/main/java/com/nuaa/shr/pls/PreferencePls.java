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
import com.nuaa.shr.pls.utils.PlsUtils;

@SuppressWarnings("serial")
public class PreferencePls extends AbstractParetoLocalSearch<PermutationSolution<Integer>>{

	private double[] preferenceVector ;
	private static double PREFERENCE_SIN = 1.0;
	private PlsUtils<PermutationSolution<Integer>> utils = new PlsUtils<>();
	
	public PreferencePls(Problem<PermutationSolution<Integer>> problem, int maxIteration, int populationSize, double[] preferenceVector) {
		super(problem, maxIteration, populationSize);
		this.preferenceVector = preferenceVector;
	}

	@Override
	public void run() {
		List<PermutationSolution<Integer>> initPopulation = createInitialPopulation();
		evaluator.evaluate(initPopulation, problem);
		workPopulation = SolutionListUtils.getNondominatedSolutions(initPopulation);
		updateCandidateEdges();
		while(iteration++ < maxIteration){
			if(workPopulation.size() > populationSize){
				PREFERENCE_SIN = getFeasibleAngle();
			}
			
			List<PermutationSolution<Integer>> tempList = new ArrayList<>();
			
			System.out.println("workPopulationSize: " + workPopulation.size());
			System.out.println("SIN: " + PREFERENCE_SIN);
			long time = System.currentTimeMillis();
			for (int index = 0; index < workPopulation.size(); index++) {
				PermutationSolution<Integer> solution = workPopulation.get(index);
				searchNeighborhood(solution, tempList);
			}
			System.out.println("search time: " + (System.currentTimeMillis() - time) + "ms");
			
			workPopulation.clear();
			workPopulation.addAll(tempList);
			
			System.out.println(iteration + " iteration\n");
			updateCandidateEdges();
			
			new SolutionListOutput(workPopulation).setSeparator("\t")
				.setFunFileOutputContext(new DefaultFileOutputContext("results/" + getName() + "/FUN" + iteration))
					.print();
		}
	}

	private double getFeasibleAngle() {
		List<Double> angles = new ArrayList<>();
		for(int i = 0;i<workPopulation.size();i++){
			angles.add(utils.sin(workPopulation.get(i), preferenceVector));
		}
		Collections.sort(angles);
		return angles.get(populationSize);
	}

	private void searchNeighborhood(PermutationSolution<Integer> solution,
			List<PermutationSolution<Integer>> tempList) {
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
					
					DominanceComparator<Solution<?>> comparator = new DominanceComparator<>();
					int dominateFlag = comparator.compare(solution, neighborSolution);

					if (dominateFlag >= 0) {
						if (utils.sin(neighborSolution, preferenceVector) < PREFERENCE_SIN) {
							addToTempPopulation(neighborSolution, tempList);
						}
					}
				}
			}
		}
		
	}

	/**
	 * 添加到临时population中，临时population的解将在循环的末尾赋值给workPopulation
	 * @param neighborSolution
	 * @param tempList
	 * @return
	 */
	private boolean addToTempPopulation(PermutationSolution<Integer> neighborSolution,
			List<PermutationSolution<Integer>> tempList) {
		if (!PlsSolutionUtils.isDominatedByList(neighborSolution, tempList)&&
				!PlsSolutionUtils.isEqualsToList(neighborSolution, tempList)
				&&!PlsSolutionUtils.isEqualsToList(neighborSolution, workPopulation)) {
			tempList.add(neighborSolution);
			reduceDominatedInList(neighborSolution, tempList);
			return true;
		} else
			return false;
	}

	@Override
	public String getName() {
		return "preference-pls";
	}

	@Override
	public String getDescription() {
		return "Preference Pareto Local Search";
	}
	
	public List<PermutationSolution<Integer>> getResult(){
		return workPopulation;
	}
	
	@Override
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
	
}
