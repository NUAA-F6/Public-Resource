package com.nuaa.shr.pls.algorithm;

import java.util.ArrayList;
import java.util.List;

import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.problem.multiobjective.GridManyObjectiveKnapsack;
import org.uma.jmetal.solution.GridPermutationSolution;

import com.nuaa.shr.pls.algorithm.abst.MoeadParetoLocalSearch;

@SuppressWarnings("serial")
public class KnapsackMoead extends MoeadParetoLocalSearch {

	private double neighborRatio = 0.2;
	private int[] tempFlag = new int[populationSize];
	private int[] updateFlag = new int[populationSize];

	public KnapsackMoead(Problem<GridPermutationSolution<Integer>> problem, int maxIteration, int populationSize) {
		super(problem, maxIteration, populationSize);
	}

	@Override
	protected void searchAndUpdateSubProblem(int index, List<GridPermutationSolution<Integer>> population) {

		if (updateFlag[index] == 1 || iteration == 0) {
			searchCount++;
			GridPermutationSolution<Integer> solution = workPopulation.get(index);
			List<Integer> unSelectedIndex = ((GridManyObjectiveKnapsack) problem).getWorstIndex(solution,
					neighborRatio);
			List<Integer> selectedBestIndex = ((GridManyObjectiveKnapsack) problem).getBestIndex(solution,
					neighborRatio);

			// List<Integer> unSelectedIndex =
			// ((GridManyObjectiveKnapsack)problem).getUnselectedIndex(solution);
			// List<Integer> selectedBestIndex =
			// ((GridManyObjectiveKnapsack)problem).getSelectedIndex(solution);
			for (int i = 0; i < selectedBestIndex.size(); i++) {
				int aSelectedIndex = selectedBestIndex.get(i);
				for (int j = 0; j < unSelectedIndex.size(); j++) {
					int aUnSelectedIndex = unSelectedIndex.get(j);
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
								tempFlag[k] = 1;
								isUpdate = true;
							}
						}
					}
				}
			}
		}

	}

	public GridPermutationSolution<Integer> getNeighborhood(GridPermutationSolution<Integer> solution,
			int aSelectedIndex, int aUnSelectedIndex) {
		GridPermutationSolution<Integer> neighborSolution = (GridPermutationSolution<Integer>) solution.copy();
		// System.out.println(((GridManyObjectiveKnapsack)problem).isValide(neighborSolution));
		// System.out.println(neighborSolution.getVariableValue(aSelectedIndex));
		// System.out.println(neighborSolution.getVariableValue(aUnSelectedIndex));

		neighborSolution.setVariableValue(aSelectedIndex, 0);
		neighborSolution.setVariableValue(aUnSelectedIndex, 1);
		if (((GridManyObjectiveKnapsack) problem).isValide(neighborSolution)) {
			((GridManyObjectiveKnapsack) problem).greedyRepair(neighborSolution);
			return neighborSolution;
		} else
			return null;
	}

	@Override
	protected void updateSomething() {
		for (int i = 0; i < tempFlag.length; i++) {
			if (tempFlag[i] == 1) {
				updateFlag[i] = 1;
				tempFlag[i] = 0;
			} else
				updateFlag[i] = 0;
		}
	}

	@Override
	protected List<GridPermutationSolution<Integer>> createInitialPopulation() {
		List<GridPermutationSolution<Integer>> population = new ArrayList<>();
		for (int i = 0; i < populationSize; i++) {
			GridPermutationSolution<Integer> solution = ((GridManyObjectiveKnapsack) this.problem)
					.createSolution(lambda[i]);
			// GridPermutationSolution<Integer> solution =
			// ((GridManyObjectiveKnapsack)this.problem).createValidSolution();
			// ((GridManyObjectiveKnapsack)this.problem).repair(solution);

			population.add(solution);
		}
		return population;
	}

	@Override
	protected void globalSearch() {
	}

	public void setNeighborRatio(double neighborRatio) {
		this.neighborRatio = neighborRatio;
	}
}
