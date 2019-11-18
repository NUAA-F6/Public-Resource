package com.nuaa.shr.pls;

import java.util.ArrayList;
import java.util.List;

import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.GridPermutationSolution;
import org.uma.jmetal.util.SolutionListUtils;

import com.nuaa.shr.pls.utils.GridComparator;
import com.nuaa.shr.pls.utils.GridUtils;


@SuppressWarnings("serial")
public class CdgGridParetoLocalSearch extends AbstractParetoLocalSearch<GridPermutationSolution<Integer>>{

	private double[] nadirPoint;
	private int division;
	private double[] unitLen;
	
	public CdgGridParetoLocalSearch(Problem<GridPermutationSolution<Integer>> problem, 
			int maxIteration, int populationSize, int division) {
		super(problem, maxIteration, populationSize);
		this.division = division;
		nadirPoint = new double[problem.getNumberOfObjectives()];
		unitLen = new double[problem.getNumberOfObjectives()];
	}

	@Override
	public void run() {
		List<GridPermutationSolution<Integer>> initPopulation = createInitialPopulation();
		evaluator.evaluate(initPopulation, problem);
		workPopulation = SolutionListUtils.getNondominatedSolutions(initPopulation);
		externalPopulation.addAll(workPopulation);
		initializeIdealPoint();
		initialNadirPoint();
		updateCandidateEdges();
		updateGridEnvironment();
		
		int iteration =  0;
		while(iteration++ < maxIteration && workPopulation.size()>0){
			
			List<GridPermutationSolution<Integer>> tempList = new ArrayList<>();
			for(GridPermutationSolution<Integer> solution:workPopulation){
				searchNeighborhood(solution,tempList);
			}
			workPopulation.clear();
			workPopulation.addAll(tempList);	
			
			updateCandidateEdges();
			if(iteration%10 == 0){
				initialNadirPoint();
			}
			updateGridEnvironment();
			reduceSameGridSolution(externalPopulation);
			reduceSameGridSolution(workPopulation);
			
			System.out.println(iteration+" iteration...");
			System.out.println("externalPopulation:"+externalPopulation.size()+
					"|| workPopulation:"+workPopulation.size()+"\n");
		}
		
	}

	/**
	 * 去除在同一个格子里的解
	 * @param population
	 */
	private void reduceSameGridSolution(List<GridPermutationSolution<Integer>> population) {
		for(int i = 0;i<population.size();i++){
			for(int j = population.size()-1;j>i;j--){
				if(isEqualByGrid(population.get(i), population.get(j))){
//					replaceByPBI(population.get(j), i, population);
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
//			double coef = 1.0-((double)coordinate)/sum;
			
			double gridIdealValue = coordinate * unitLen[index];
			solutionDistance += Math.pow((solution.getObjective(index)-gridIdealValue), norm);
			neighborSolutionDistance += Math.pow((gridPermutationSolution.getObjective(index)-gridIdealValue), norm);
		}
		solutionDistance = Math.pow(solutionDistance, 1.0/norm);
		neighborSolutionDistance = Math.pow(neighborSolutionDistance, 1.0/norm);
		if(neighborSolutionDistance < solutionDistance){
			population.set(i, gridPermutationSolution);
			return true;
		}else return false;
		
	}

	public boolean replaceByPBI(GridPermutationSolution<Integer> gridPermutationSolution, int i,
			List<GridPermutationSolution<Integer>> population) {
		GridPermutationSolution<Integer> solution = population.get(i);
		double[] solutionDiff  = new double[problem.getNumberOfObjectives()];
		double[] neighborSolutionDiff= new double[problem.getNumberOfObjectives()];
		
		double[] coordinates = new double[solution.getNumberOfObjectives()];
		double sum = 0.0;
		for(int index = 0;index<solution.getNumberOfObjectives();index++){
			int coordinate = solution.getGridCoordinate(index);
			
			coordinates[index] = coordinate;
			sum += coordinate;
			
			double gridIdealValue = coordinate * unitLen[index];
			solutionDiff[index] = solution.getObjective(index)-gridIdealValue;
			neighborSolutionDiff[index] = gridPermutationSolution.getObjective(index)-gridIdealValue;
		}
		
		for(int k = 0;k<coordinates.length;k++){
			coordinates[k] = coordinates[k] / sum;
		}
		
		if(pbi(solutionDiff, coordinates) > pbi(neighborSolutionDiff, coordinates)){
			population.set(i, gridPermutationSolution);
			return true;
		}else return false;
		
	}

	private void searchNeighborhood(GridPermutationSolution<Integer> solution, List<GridPermutationSolution<Integer>> tempList) {
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
					GridPermutationSolution<Integer> neighborSolution = getANeighborSolution(solution, i, j);
					setGridCoordinate(neighborSolution);
					GridComparator gridComparator = new GridComparator();
					int dominateFlag = gridComparator.compare(solution, neighborSolution);
					if(dominateFlag >= 0){
						if(addSolutionToExternal(neighborSolution)){
							GridUtils.reduceDominatedInList(neighborSolution, externalPopulation);
							
							updateIdealPoint(neighborSolution);
							updateNadirPoint(neighborSolution);
							
							addToTempPopulation(neighborSolution, tempList);
						}
					}
				}
			}
		}
		
	}

	private boolean addSolutionToExternal(GridPermutationSolution<Integer> neighborSolution) {
		if(!GridUtils.isDominatedByOrEqualToList(neighborSolution, externalPopulation)){
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
//				if(replaceByPBI(neighborSolution,i,externalPopulation))
					return true;
				else return false;
			}
		}
		externalPopulation.add(neighborSolution);
		return true;
	}


	private double pbi(double[] solutionDiff, double[] vector) {
	      double d1, d2, nl;
	      double theta = 5.0;
//	      double[] vector = new double[problem.getNumberOfObjectives()];
//	      for(int i = 0;i<vector.length;i++){
//	    	  vector[i] = 0.5;
//	      }
	      
	      d1 = d2 = nl = 0.0;
	      
	      for (int i = 0; i < problem.getNumberOfObjectives(); i++) {
	        d1 += solutionDiff[i] * vector[i];
	        nl += Math.pow(vector[i], 2.0);
	      }
	      nl = Math.sqrt(nl);
	      d1 = Math.abs(d1) / nl;

	      for (int i = 0; i < problem.getNumberOfObjectives(); i++) {
	        d2 += Math.pow( solutionDiff[i] - d1 * (vector[i] / nl), 2.0);
	      }
	      d2 = Math.sqrt(d2);

	      return (d1 + theta * d2);
	}

	private boolean isEqualByGrid(GridPermutationSolution<Integer> solution,
			GridPermutationSolution<Integer> neighborSolution) {
		for(int i = 0;i<solution.getNumberOfObjectives();i++){
			if(solution.getGridCoordinate(i) != neighborSolution.getGridCoordinate(i))
				return false;
		}
		return true;
	}

	private boolean isOutOfBounds(GridPermutationSolution<Integer> neighborSolution) {
		for(int i = 0;i<neighborSolution.getNumberOfObjectives();i++){
			if(neighborSolution.getObjective(i) < idealPoint[i]){
				return true;
			}
		}
		return false;
	}

	/**
	 * 添加到临时population中，临时population的解将在循环的末尾赋值给workPopulation
	 * @param neighborSolution
	 * @param tempList
	 * @return
	 */
	private boolean addToTempPopulation(GridPermutationSolution<Integer> neighborSolution,
			List<GridPermutationSolution<Integer>> tempList) {
		if (!GridUtils.isDominatedByOrEqualToList(neighborSolution, tempList)
				&&!plsUtils.isEqualsToList(neighborSolution, workPopulation)) {
			GridUtils.reduceDominatedInList(neighborSolution, tempList);
			if(!replaceSameGridSolution(neighborSolution, tempList)){
				tempList.add(neighborSolution);
			}
			return true;
		} else
			return false;
	}
	
	
	private boolean replaceSameGridSolution(GridPermutationSolution<Integer> neighborSolution,
			List<GridPermutationSolution<Integer>> tempList) {
		for(int i = 0;i<tempList.size();i++){
			GridPermutationSolution<Integer> solution = tempList.get(i);
			if(isEqualByGrid(solution, neighborSolution)){
				tempList.set(i, neighborSolution);
				return true;
			}
		}
		return false;
		
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

	private void setGridCoordinate(GridPermutationSolution<?> solution) {
		for(int i = 0;i<solution.getNumberOfObjectives();i++){
			/**
			 * 如果范围小于idealpoint，坐标为负值
			 */
			int gridCoordinate = (int) ((solution.getObjective(i) - idealPoint[i])/unitLen[i]);
			solution.setGridCoordinate(i, gridCoordinate);
		}
	}

	private void initialNadirPoint() {
		for (int i = 0;i<problem.getNumberOfObjectives();i++){
			nadirPoint[i] = 0;
		}
		for(int i = 0;i<externalPopulation.size();i++){
			updateNadirPoint(externalPopulation.get(i));
		}
		for(int i = 0;i<problem.getNumberOfObjectives();i++){
			nadirPoint[i] = nadirPoint[i] * 1.1;
		}
		
	}

	private void updateNadirPoint(GridPermutationSolution<Integer> permutationSolution) {
		for(int i = 0;i<permutationSolution.getNumberOfObjectives();i++){
			if(permutationSolution.getObjective(i)>nadirPoint[i]){
				nadirPoint[i] = permutationSolution.getObjective(i);
			}
		}
		
	}

	@Override
	protected void initializeIdealPoint() {
		for (int i = 0; i < problem.getNumberOfObjectives(); i++) {
			idealPoint[i] = 1.0e+30;
		}

		for (int i = 0; i < externalPopulation.size(); i++) {
			updateIdealPoint(externalPopulation.get(i));
		}
	}
	
	@Override
	public String getName() {
		return "cdg-pls";
	}

	@Override
	public String getDescription() {
		return "cdg Pareto Local Search";
	}

	@Override
	public GridPermutationSolution<Integer> getANeighborSolution(GridPermutationSolution<Integer> solution,
			int startIndex, int endIndex) {
		GridPermutationSolution<Integer> neighborSolution = this.problem.createSolution();
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
	public List<GridPermutationSolution<Integer>> getResult() {
		return SolutionListUtils.getNondominatedSolutions(externalPopulation);
	}

}
