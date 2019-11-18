package com.nuaa.shr.pls;

import java.util.ArrayList;
import java.util.List;

import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.GridPermutationSolution;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.comparator.DominanceComparator;

import com.nuaa.shr.pls.utils.PlsSolutionUtils;


@SuppressWarnings("serial")
public class CdgParetoLocalSearch extends AbstractParetoLocalSearch<GridPermutationSolution<Integer>>{

	private double[] nadirPoint;
	private int division;
	private double[] unitLen;
	
	public CdgParetoLocalSearch(Problem<GridPermutationSolution<Integer>> problem, 
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
		int iteration = 0;
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
					DominanceComparator<GridPermutationSolution<Integer>> dominateComparator = new DominanceComparator<>();
					int dominateFlag = dominateComparator.compare(solution, neighborSolution);
					if(dominateFlag >= 0){
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
		
	}

	private boolean addSolutionToExternal(GridPermutationSolution<Integer> neighborSolution) {
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

	public boolean replaceByDistance(GridPermutationSolution<Integer> gridPermutationSolution, int i,
			List<GridPermutationSolution<Integer>> population, double norm) {
		GridPermutationSolution<Integer> solution = population.get(i);
		double solutionDistance  = 0.0;
		double neighborSolutionDistance = 0.0;
		double sum = 0.0;
		for(int k = 0;k<solution.getNumberOfObjectives();k++){
			sum += solution.getObjective(k);
		}
		for(int index = 0;index<solution.getNumberOfObjectives();index++){
			int coordinate = solution.getGridCoordinate(index);
			double coef = 1.0-((double)coordinate)/sum;
			
			double gridIdealValue = coordinate * unitLen[index];
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
		if (!PlsSolutionUtils.isDominatedByOrEqualToList(neighborSolution, tempList)
				&&!plsUtils.isEqualsToList(neighborSolution, workPopulation)) {
			if(!replaceSameGridSolution(neighborSolution, tempList)){
				tempList.add(neighborSolution);
			}
			reduceDominatedInList(neighborSolution, tempList);
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

}
