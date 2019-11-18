package com.nuaa.shr.pls.algorithm;

import java.util.List;

import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.problem.multiobjective.NrpProblem;
import org.uma.jmetal.solution.GridPermutationSolution;

import com.nuaa.shr.pls.algorithm.abst.MoeadParetoLocalSearch;
import com.nuaa.shr.pls.utils.PermutationUtility;

@SuppressWarnings("serial")
public class NrpMoead extends MoeadParetoLocalSearch{

	int[] updateFlag = new int[populationSize];
	int[] tempFlag = new int[populationSize];
	
	public NrpMoead(Problem<GridPermutationSolution<Integer>> problem, int maxIteration, int populationSize) {
		super(problem, maxIteration, populationSize);
	}

	@Override
	protected void searchAndUpdateSubProblem(int index, List<GridPermutationSolution<Integer>> population) {
		if (updateFlag[index] == 1 || iteration == 0) {
			GridPermutationSolution<Integer> solution = workPopulation.get(index);
			Integer[] perm = new PermutationUtility().initPermutation(solution.getNumberOfVariables());
			int i = 0;
			int flipNum = 5;
			while (flipNum * (i + 1) <= perm.length) {
				GridPermutationSolution<Integer> neighborSolution = getNeighborhood(solution, perm, i, flipNum);
				updateIdealPoint(neighborSolution);
				for (int neighborIndex = 0; neighborIndex < neighborhood[index].length; neighborIndex++) {
					int k = neighborhood[index][neighborIndex];

					if (fitnessFunction(neighborSolution, lambda[k]) < fitnessFunction(population.get(k), lambda[k])) {
						population.set(k, neighborSolution);
						isUpdate = true;
						tempFlag[k] = 1;
					}
				}
				i++;
			}
		}
/*		GridPermutationSolution<Integer> solution = workPopulation.get(index);
		List<Integer> selectedBestIndex = ((NrpProblem)problem).getWorstIndex(solution, 0.02);
		List<Integer> unSelectedIndex = ((NrpProblem)problem).getBestIndex(solution, 0.02);
		for(int i = 0;i<selectedBestIndex.size();i++){
			int aSelectedIndex = selectedBestIndex.get(i);
			for(int j = 0;j<unSelectedIndex.size();j++){
				int aUnSelectedIndex = unSelectedIndex.get(j);
				GridPermutationSolution<Integer> neighborSolution = getNeighborhood(solution, aSelectedIndex, aUnSelectedIndex);
				((NrpProblem)problem).repairSolution(neighborSolution);
				this.problem.evaluate(neighborSolution);
				updateIdealPoint(neighborSolution);
				for(int neighborIndex = 0;neighborIndex<neighborhood[index].length;neighborIndex++){
					int k = neighborhood[index][neighborIndex];
					
					if(fitnessFunction(neighborSolution, lambda[k])<
							fitnessFunction(population.get(k),lambda[k])){
						population.set(k, neighborSolution);
						isUpdate = true;
					}
				}
			}
		}*/
		
	}

	private GridPermutationSolution<Integer> getNeighborhood(GridPermutationSolution<Integer> solution,
			Integer[] perm, int i, int flipNum) {
		GridPermutationSolution<Integer> child = (GridPermutationSolution<Integer>) solution.copy();
		int bit = Math.random()<0.5?0:1;
		for(int k = i;k < i+flipNum;k++){
			child.setVariableValue(perm[k], bit);
		}
		((NrpProblem)problem).repairSolution(child);
		this.problem.evaluate(child);
		return child;
	}
	
	public GridPermutationSolution<Integer> getNeighborhood(GridPermutationSolution<Integer> solution, int aSelectedIndex, int aUnSelectedIndex) {
		GridPermutationSolution<Integer> neighborSolution = problem.createSolution();
		for(int k = 0;k<neighborSolution.getNumberOfVariables();k++){
			neighborSolution.setVariableValue(k, solution.getVariableValue(k));
		}
		neighborSolution.setVariableValue(aSelectedIndex, 0);
		neighborSolution.setVariableValue(aUnSelectedIndex, 1);
		return neighborSolution;
	}

	@Override
	protected void updateSomething() {
		for(int i = 0;i<tempFlag.length;i++){
			if(tempFlag[i]==1){
				updateFlag[i] = 1;
				tempFlag[i] = 0;
			}else updateFlag[i] = 0;
		}
	}

	@Override
	protected void globalSearch() {
//		isUpdate = true;
//		int[] permutation = new int[populationSize];
//		MOEADUtils.randomPermutation(permutation, populationSize);
//		
//		for(int i = 0;i<permutation.length;i++){
//			GridPermutationSolution<Integer> child = (GridPermutationSolution<Integer>) workPopulation.get(i).copy();
//			
//			//crossover
//			if(Math.random()<1.0){
//				int[] crossIndex = new int[child.getNumberOfVariables()];
//				int[] crossIndex2 = new int[child.getNumberOfVariables()];
//				for(int idx = 0;idx<crossIndex.length;idx++){
//					crossIndex[idx] = Math.random()<0.5?1:0;
//					crossIndex2[idx] = 1 - crossIndex[idx] ;
//				}
//				GridPermutationSolution<Integer> solution2;
//				if(Math.random()<0.8){
//					int x_i = (int) (Math.random() * neighborSize);
//					solution2 = workPopulation.get(neighborhood[i][x_i]);
//				}else {
//					int x_i = (int) (Math.random() * populationSize);
//					solution2 = workPopulation.get(x_i);
//				}
//				
//				for(int k = 0;k<crossIndex.length;k++){
//					int variableValue = crossIndex[k] * child.getVariableValue(k) + 
//							crossIndex2[k] * solution2.getVariableValue(k);
//					child.setVariableValue(k, variableValue);
//				}
//			}
//			//mutation
//			if(Math.random()<0.1){
//				int mutationIndex = (int) (Math.random() * child.getNumberOfVariables());
//				child.setVariableValue(mutationIndex, 1-child.getVariableValue(mutationIndex));
//			}
//			
//			//evaluate
//			((NrpProblem)problem).repairSolution(child);
//			this.problem.evaluate(child);
//			
//			//update ideal point
//			updateIdealPoint(child);
//			
//			//update
//			for(int neighborIndex = 0;neighborIndex<neighborhood[i].length;neighborIndex++){
//				int neighborIdx = neighborhood[i][neighborIndex];
//				
//				if(fitnessFunction(child, lambda[neighborIdx])<
//						fitnessFunction(workPopulation.get(neighborIdx),lambda[neighborIdx])){
//					workPopulation.set(neighborIdx, child);
//				}
//			}
//		}
		
	}
}
