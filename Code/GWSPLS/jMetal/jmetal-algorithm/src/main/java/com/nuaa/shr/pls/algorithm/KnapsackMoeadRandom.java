package com.nuaa.shr.pls.algorithm;

import java.util.ArrayList;
import java.util.List;

import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.problem.multiobjective.GridManyObjectiveKnapsack;
import org.uma.jmetal.solution.GridPermutationSolution;

import com.nuaa.shr.pls.algorithm.abst.MoeadParetoLocalSearch;

@SuppressWarnings("serial")
public class KnapsackMoeadRandom extends MoeadParetoLocalSearch{

	double p_ = 0.3;
	
	public KnapsackMoeadRandom(Problem<GridPermutationSolution<Integer>> problem, int maxIteration, int populationSize) {
		super(problem, maxIteration, populationSize);
	}

	@Override
	protected void searchAndUpdateSubProblem(int index, List<GridPermutationSolution<Integer>> population) {
		GridPermutationSolution<Integer> solution = workPopulation.get(index);
		List<Integer> selectedIndexes = ((GridManyObjectiveKnapsack) problem).getSelectedIndex(solution);
		List<Integer> unSelectedIndexes = ((GridManyObjectiveKnapsack) problem).getUnselectedIndex(solution);
		for (int i = 0; i < selectedIndexes.size(); i++) {
			int aSelectedIndex = selectedIndexes.get(i);
			for (int j = 0; j < unSelectedIndexes.size(); j++) {
				if (Math.random() < p_) {
					int aUnSelectedIndex = unSelectedIndexes.get(j);
					GridPermutationSolution<Integer> neighborSolution = getNeighborhood(solution, aSelectedIndex,
							aUnSelectedIndex);
					if (neighborSolution != null) {
						this.problem.evaluate(neighborSolution);
						updateIdealPoint(neighborSolution);
						for (int neighborIndex = 0; neighborIndex < neighborhood[index].length; neighborIndex++) {
							int k = neighborhood[index][neighborIndex];

							if (fitnessFunction(neighborSolution, lambda[k]) < fitnessFunction(population.get(k),
									lambda[k])) {
								population.set(k, neighborSolution);
								isUpdate = true;
							}
						}
					}
				}
			}
		}
		
	}

	public GridPermutationSolution<Integer> getNeighborhood(GridPermutationSolution<Integer> solution, int aSelectedIndex, int aUnSelectedIndex) {
		GridPermutationSolution<Integer> neighborSolution = (GridPermutationSolution<Integer>) solution.copy();
//		System.out.println(((GridManyObjectiveKnapsack)problem).isValide(neighborSolution));
//		System.out.println(neighborSolution.getVariableValue(aSelectedIndex));
//		System.out.println(neighborSolution.getVariableValue(aUnSelectedIndex));
		
		neighborSolution.setVariableValue(aSelectedIndex, 0);
		neighborSolution.setVariableValue(aUnSelectedIndex, 1);
		if(((GridManyObjectiveKnapsack)problem).isValide(neighborSolution)){
			((GridManyObjectiveKnapsack)problem).randomRepair(neighborSolution);
			return neighborSolution;
		}
		else return null;
	}
	
	@Override
	protected void updateSomething() {
	}

	
	@Override
	protected List<GridPermutationSolution<Integer>> createInitialPopulation() {
		List<GridPermutationSolution<Integer>> population = new ArrayList<>();
		for(int i = 0;i<populationSize;i++){
			GridPermutationSolution<Integer> solution = 
					((GridManyObjectiveKnapsack)this.problem).createSolution(lambda[i]);
//			GridPermutationSolution<Integer> solution = ((GridManyObjectiveKnapsack)this.problem).createValidSolution();
//			((GridManyObjectiveKnapsack)this.problem).repair(solution);
			
			population.add(solution);
		}
		return population;
	}

	@Override
	protected void globalSearch() {}
}
