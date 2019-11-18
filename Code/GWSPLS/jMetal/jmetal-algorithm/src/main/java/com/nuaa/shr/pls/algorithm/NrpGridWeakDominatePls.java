package com.nuaa.shr.pls.algorithm;

import java.util.List;

import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.problem.multiobjective.NrpProblem;
import org.uma.jmetal.solution.GridPermutationSolution;

import com.nuaa.shr.pls.algorithm.abst.GrWeakDominateParetoLocalSearch;
import com.nuaa.shr.pls.utils.GridComparator;
import com.nuaa.shr.pls.utils.GridUtils;
import com.nuaa.shr.pls.utils.PermutationUtility;

@SuppressWarnings("serial")
public class NrpGridWeakDominatePls extends GrWeakDominateParetoLocalSearch{

	public NrpGridWeakDominatePls(Problem<GridPermutationSolution<Integer>> problem, int maxIteration,
			int populationSize, int division) {
		super(problem, maxIteration, populationSize, division);
	}

	@Override
	protected void searchNeighborhood(GridPermutationSolution<Integer> solution,
			List<GridPermutationSolution<Integer>> tempList) {
//		for(int i = 0;i<workPopulation.size();i++){
//			if(!workPopulation.get(i).equals(solution)){
//				//GridPermutationSolution<Integer> neighborSolution = getNeighborhood(solution, workPopulation.get(i));
//				GridPermutationSolution<Integer> neighborSolution = getNeighborhood(solution);
//				setGridCoordinate(neighborSolution);
//				GridComparator gridComparator = new GridComparator();
//				int dominateFlag = gridComparator.compare(solution, neighborSolution);
//				if(dominateFlag >= 0){
//					if(addSolutionToExternal(neighborSolution)){
//						GridUtils.reduceDominatedInList(neighborSolution, externalPopulation);
//						addToTempPopulation(neighborSolution, tempList);
//					}
//				}
//			}
//		}
		Integer[] perm = new PermutationUtility().initPermutation(solution.getNumberOfVariables());
		int i = 0;
		int flipNum = 5;
		while(flipNum*(i+1) <= perm.length){
			GridPermutationSolution<Integer> neighborSolution = getNeighborhood(solution,perm,i,flipNum);
			setGridCoordinate(neighborSolution);
			GridComparator gridComparator = new GridComparator();
			int dominateFlag = gridComparator.compare(solution, neighborSolution);
			if(dominateFlag >= 0){
				if(addSolutionToExternal(neighborSolution)){
					GridUtils.reduceDominatedInList(neighborSolution, externalPopulation);
					addToTempPopulation(neighborSolution, tempList);
				}
			}
			i++;
		}
		
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
	
	public GridPermutationSolution<Integer> getNeighborhood(GridPermutationSolution<Integer> solution,
			GridPermutationSolution<Integer> solution2) {
		GridPermutationSolution<Integer> child = (GridPermutationSolution<Integer>) solution.copy();
		int[] crossIndex = new int[child.getNumberOfVariables()];
		int[] crossIndex2 = new int[child.getNumberOfVariables()];
		for(int idx = 0;idx<crossIndex.length;idx++){
			crossIndex[idx] = Math.random()<0.5 ? 1 : 0;
			crossIndex2[idx] = 1 - crossIndex[idx] ;
		}
		
		for(int k = 0;k<crossIndex.length;k++){
			int variableValue = crossIndex[k] * child.getVariableValue(k) + 
					crossIndex2[k] * solution2.getVariableValue(k);
			child.setVariableValue(k, variableValue);
		}
		
		//mutation
		if(Math.random()<0.1){
			int mutationIndex = (int) (Math.random() * child.getNumberOfVariables());
			child.setVariableValue(mutationIndex, 1-child.getVariableValue(mutationIndex));
		}
		
		//evaluate
		((NrpProblem)problem).repairSolution(child);
		this.problem.evaluate(child);
		return child;
	}

	@Override
	protected void updateSomething() {
		// TODO Auto-generated method stub
		
	}

}
