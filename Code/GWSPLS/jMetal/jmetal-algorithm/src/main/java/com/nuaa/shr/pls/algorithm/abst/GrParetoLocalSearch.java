package com.nuaa.shr.pls.algorithm.abst;

import java.util.ArrayList;
import java.util.List;

import org.uma.jmetal.algorithm.multiobjective.moead.util.MOEADUtils;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.GridPermutationSolution;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.comparator.DominanceComparator;

import com.nuaa.shr.pls.utils.PlsSolutionUtils;

@SuppressWarnings("serial")
public abstract class GrParetoLocalSearch extends AbstractParetoLocalSearch<GridPermutationSolution<Integer>>{
	private int division;
	private double[] unitLen;
	
	
	public GrParetoLocalSearch(Problem<GridPermutationSolution<Integer>> problem, 
			int maxIteration, int populationSize, int numberOfGrid){
		super(problem,maxIteration,populationSize);
		this.division = numberOfGrid;
		unitLen = new double[problem.getNumberOfObjectives()];
	}

	@Override
	public void run() {
		List<GridPermutationSolution<Integer>> initPopulation = createInitialPopulation();
		evaluator.evaluate(initPopulation, problem);
		workPopulation = SolutionListUtils.getNondominatedSolutions(initPopulation);
		externalPopulation.addAll(workPopulation);
		updateSomething();
		initializeIdealPoint();
		initialNadirPoint();
		updateGridEnvironment();
		
		int iteration = 0;
		while(iteration++ < maxIteration && workPopulation.size() > 0){
			List<GridPermutationSolution<Integer>> tempList = new ArrayList<>();
			int[] permutation = new int[workPopulation.size()];
			MOEADUtils.randomPermutation(permutation, workPopulation.size());
			for(int i = 0;i<permutation.length;i++){
				searchNeighborhood(workPopulation.get(permutation[i]),tempList);
			}
			workPopulation.clear();
			workPopulation.addAll(tempList);
			updateIdealPoint(externalPopulation);
//			if(iteration % 10 == 0)
				initialNadirPoint();
			updateGridEnvironment();
			reduceSameGridSolution(externalPopulation);
			reduceSameGridSolution(workPopulation);
			updateSomething();
			System.out.println(iteration+" iteration...");
			System.out.println("externalPopulation:"+externalPopulation.size()+
					"|| workPopulation:"+workPopulation.size()+"\n");
//			new SolutionListOutput(externalPopulation).setSeparator("\t")
//				.setFunFileOutputContext(new DefaultFileOutputContext("results/"+getName()+"/FUN" + iteration)).print();
		}
		

	}
	
	
	protected abstract void updateSomething();

	protected boolean addSolutionToExternal(GridPermutationSolution<Integer> neighborSolution) {
		if(!PlsSolutionUtils.isDominatedByOrEqualToList(neighborSolution, externalPopulation)){
			if(isOutOfBounds(neighborSolution)){
				externalPopulation.add(neighborSolution);
				return true;
			}else {
				if(addToExternalAccordingToGrid(neighborSolution))
					return true;
				else return false;
			}
		}else return false;
	}
	
	private boolean addToExternalAccordingToGrid(GridPermutationSolution<Integer> neighborSolution) {
		for(int i = 0;i<externalPopulation.size();i++){
			GridPermutationSolution<Integer> solution = externalPopulation.get(i);
			if(isEqualByGrid(solution, neighborSolution)){
				if(replaceByDistance(neighborSolution,i,externalPopulation,1.0))
					return true;
				else return false;
			}
		}
		externalPopulation.add(neighborSolution);
		return true;
	}
	
	private boolean isOutOfBounds(GridPermutationSolution<Integer> neighborSolution) {
		for(int i = 0;i<neighborSolution.getNumberOfObjectives();i++){
			if(neighborSolution.getObjective(i) < idealPoint[i] || 
					neighborSolution.getObjective(i)>nadirPoint[i]){
				return true;
			}
		}
		return false;
	}
	
	private void reduceSameGridSolution(List<GridPermutationSolution<Integer>> population) {
		for(int i = 0;i<population.size();i++){
			for(int j = population.size()-1;j>i;j--){
				if(isEqualByGrid(population.get(i), population.get(j))){
					replaceByDistance(population.get(j), i, population,1.0);
					population.remove(j);
				}
			}
		}
	}
	
	
	public boolean replaceByDistance(GridPermutationSolution<Integer> gridPermutationSolution, int i,
			List<GridPermutationSolution<Integer>> population, double norm) {
		GridPermutationSolution<Integer> solution = population.get(i);
		double solutionDistance  = 0.0;
		double neighborSolutionDistance = 0.0;
//		double sum = 0.0;
//		for(int k = 0;k<solution.getNumberOfObjectives();k++){
//			sum += solution.getObjective(k);
//		}
		for(int index = 0;index<solution.getNumberOfObjectives();index++){
			int coordinate = solution.getGridCoordinate(index);
			double coef = 1.0 / ((double) coordinate + 0.0000001);
//			double coef = 1.0-((double)coordinate)/sum;
			
			double gridIdealValue = idealPoint[index]+coordinate * unitLen[index];
			solutionDistance += Math.pow((solution.getObjective(index)-gridIdealValue)*coef, norm);
			neighborSolutionDistance += Math.pow((gridPermutationSolution.getObjective(index)-gridIdealValue)*coef, norm);
		}
		solutionDistance = Math.pow(solutionDistance, 1.0/norm);
		neighborSolutionDistance = Math.pow(neighborSolutionDistance, 1.0/norm);
		if(neighborSolutionDistance < solutionDistance){
			population.set(i, gridPermutationSolution);
			return true;
		}else return false;
		
	}
	
	private boolean isEqualByGrid(GridPermutationSolution<Integer> solution,
			GridPermutationSolution<Integer> neighborSolution) {
		for(int i = 0;i<solution.getNumberOfObjectives();i++){
			if(solution.getGridCoordinate(i) != neighborSolution.getGridCoordinate(i))
				return false;
		}
		return true;
	}

	protected abstract void searchNeighborhood(GridPermutationSolution<Integer> solution,
			List<GridPermutationSolution<Integer>> tempList);

	
	public boolean addToTempPopulation(GridPermutationSolution<Integer> neighborSolution,
			List<GridPermutationSolution<Integer>> tempList) {
		if (!plsUtils.isEqualsToList(neighborSolution, workPopulation)) {
			for(int i = tempList.size()-1; i>=0; i--){
				GridPermutationSolution<Integer> solution = tempList.get(i);
	 			DominanceComparator<GridPermutationSolution<Integer>> comparator = new DominanceComparator<>();
	 			int tempFlag = comparator.compare(neighborSolution,solution);
	 			if(tempFlag==-1||isEqualByGrid(solution, neighborSolution)){
	 				tempList.remove(i);
	 			}
			}
			tempList.add(neighborSolution);
			return true;
		}else return false;
	}
	

	private void updateGridEnvironment() {
		for(int i = 0;i<unitLen.length;i++){
			unitLen[i] = (nadirPoint[i] - idealPoint[i])/(double) division;
		}
		for(GridPermutationSolution<?> solution:externalPopulation){
			setGridCoordinate(solution);
		}
		for(GridPermutationSolution<?> solution:workPopulation){
			setGridCoordinate(solution);
		}
	}
	
	protected void setGridCoordinate(GridPermutationSolution<?> solution) {
		for(int i = 0;i<solution.getNumberOfObjectives();i++){
			/**
			 * 如果范围小于idealpoint，坐标为负值
			 */
			int gridCoordinate = (int) ((solution.getObjective(i) - idealPoint[i])/unitLen[i]);
			solution.setGridCoordinate(i, gridCoordinate);
		}
	}
}
