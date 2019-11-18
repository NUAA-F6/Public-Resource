package com.nuaa.shr.pls.algorithm;

import java.util.List;

import org.uma.jmetal.problem.multiobjective.NrpProblem;
import org.uma.jmetal.solution.GridPermutationSolution;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.comparator.DominanceComparator;

import com.nuaa.shr.pls.algorithm.abst.GrParetoLocalSearch;

@SuppressWarnings("serial")
public class NrpGridPls extends GrParetoLocalSearch{
	
	
	public NrpGridPls(NrpProblem problem, int maxIteration, int populationSize, int numberOfGrid) {
		super(problem, maxIteration, populationSize, numberOfGrid);
	}

	@Override
	public void searchNeighborhood(GridPermutationSolution<Integer> solution,
			List<GridPermutationSolution<Integer>> tempList) {
		List<Integer> selectedBestIndex = ((NrpProblem)problem).getWorstIndex(solution, 0.05);
		List<Integer> unSelectedIndex = ((NrpProblem)problem).getBestIndex(solution, 0.05);
		for(int i = 0;i<selectedBestIndex.size();i++){
			int aSelectedIndex = selectedBestIndex.get(i);
			for(int j = 0;j<unSelectedIndex.size();j++){
				int aUnSelectedIndex = unSelectedIndex.get(j);
				GridPermutationSolution<Integer> neighborSolution = getNeighborhood(solution, aSelectedIndex, aUnSelectedIndex);
				this.problem.evaluate(neighborSolution);
				setGridCoordinate(neighborSolution);
				
				DominanceComparator<Solution<?>> dominateComparator = new DominanceComparator<>();
				int dominateFlag = dominateComparator.compare(solution, neighborSolution);
				if(dominateFlag > 0){
					if(addSolutionToExternal(neighborSolution)){
						reduceDominatedInList(neighborSolution, externalPopulation);
						updateIdealPoint(neighborSolution);
						updateNadirPoint(neighborSolution);
						addToTempPopulation(neighborSolution, tempList);
					}
				}
			}
		}
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
	public String getName() {
		return "nrp-GridPLS";
	}

	@Override
	public String getDescription() {
		return "nrp-Grid Pareto Local Search";
	}

	@Override
	protected void updateSomething() {		
	}
}
