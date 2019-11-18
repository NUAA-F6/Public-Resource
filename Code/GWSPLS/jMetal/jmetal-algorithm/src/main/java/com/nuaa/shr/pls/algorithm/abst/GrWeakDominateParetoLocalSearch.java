package com.nuaa.shr.pls.algorithm.abst;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.uma.jmetal.algorithm.multiobjective.moead.util.MOEADUtils;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.GridPermutationSolution;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.fileoutput.SolutionListOutput;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;

import com.nuaa.shr.pls.utils.GridComparator;


@SuppressWarnings("serial")
public abstract class GrWeakDominateParetoLocalSearch extends AbstractParetoLocalSearch<GridPermutationSolution<Integer>>{
	private String baseProcedureDirectory = "results/procedure";
	
	private int division;
	private double[] unitLen;	
	
	private double[] lb;
	private double[] ub;
	protected int iteration;
	public GrWeakDominateParetoLocalSearch(Problem<GridPermutationSolution<Integer>> problem, 
			int maxIteration, int populationSize, int division) {
		super(problem, maxIteration, populationSize);
		this.division = division;
		unitLen = new double[problem.getNumberOfObjectives()];
		lb = new double[problem.getNumberOfObjectives()];
		ub = new double[problem.getNumberOfObjectives()];
	}
	
	@Override
	public void run() {
		List<GridPermutationSolution<Integer>> initPopulation = createInitialPopulation();
		evaluator.evaluate(initPopulation, problem);
		workPopulation = SolutionListUtils.getNondominatedSolutions(initPopulation);
		externalPopulation.addAll(workPopulation);
//		new SolutionListOutput(workPopulation).setSeparator("\t")
//			.setFunFileOutputContext(new DefaultFileOutputContext("results/temp/initialize.tsv")).print();
		printExternal(0);
		updateSomething();
		initializeIdealPoint();
		initialNadirPoint();
		updateGridEnvironment();
		//===========高维添加此处=================
//		reduceSameGridSolution(externalPopulation);
//		reduceSameGridSolution(workPopulation);
		//**************************************************
		iteration =  0;
		while(iteration++ < maxIteration && workPopulation.size()>0){
			List<GridPermutationSolution<Integer>> tempList = new ArrayList<>();
			int[] permutation = new int[workPopulation.size()];
			MOEADUtils.randomPermutation(permutation, workPopulation.size());
			for(int i = 0;i<permutation.length;i++){
				searchCount++;
				searchNeighborhood(workPopulation.get(permutation[i]),tempList);
				
				if(searchCount % 50 == 0)
					printExternal(searchCount / 50);
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
//			System.out.println(iteration+" iteration...");
//			System.out.println("externalPopulation:"+externalPopulation.size()+
//					"|| workPopulation:"+workPopulation.size()+"\n");
//			new SolutionListOutput(externalPopulation).setSeparator("\t")
//			.setFunFileOutputContext(new DefaultFileOutputContext("results/temp/process/FUN"+iteration)).print();
		}
	}

	public void setBaseProcedureDirectory(String baseProcedureDirectory){
		this.baseProcedureDirectory = baseProcedureDirectory;
	}
	
	private void printExternal(int position) {
		StringBuffer folderPath = new StringBuffer(this.baseProcedureDirectory)
				.append("/")
				.append(this.getName())
				.append("/")
				.append(problem.getName())
				.append("/RUN_")
				.append(this.getRunId());
		if(position == 0){
			File folder = new File(folderPath.toString());
			if(!folder.exists()||!folder.isDirectory()){
				folder.mkdirs();
			}
		}
		String filePath = 
				folderPath
				.append("/FUN")
				.append(position)
				.append(".tsv").toString();
		new SolutionListOutput(SolutionListUtils.getNondominatedSolutions(externalPopulation))
			.setSeparator("\t")
			.setFunFileOutputContext(new DefaultFileOutputContext(filePath))
			.print();
	}

	protected abstract void updateSomething();

	/**
	 * 去除在同一个格子里的解
	 * @param population
	 */
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
		for(int index = 0;index<solution.getNumberOfObjectives();index++){
			int coordinate = solution.getGridCoordinate(index);
//			if(coordinate<0)
//				coordinate = 0;
//			double coef = 1.0 / ((double) Math.abs(coordinate) + 0.000001);
			double coef = 1.0;
			
			double gridIdealValue = lb[index] + coordinate * unitLen[index];
			solutionDistance += (solution.getObjective(index)-gridIdealValue)*coef;
			neighborSolutionDistance += (gridPermutationSolution.getObjective(index)-gridIdealValue)*coef;
		}
//		solutionDistance = Math.pow(solutionDistance, 1.0/norm);
//		neighborSolutionDistance = Math.pow(neighborSolutionDistance, 1.0/norm);
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

	protected abstract void searchNeighborhood(GridPermutationSolution<Integer> solution, List<GridPermutationSolution<Integer>> tempList);

	protected boolean addSolutionToExternal(GridPermutationSolution<Integer> neighborSolution) {
		// if (!isOutOfBounds(neighborSolution)) {
		GridComparator gridComara = new GridComparator();
		for (int i = externalPopulation.size() - 1; i >= 0; i--) {
			GridPermutationSolution<Integer> comparaSolution = externalPopulation.get(i);
			int compFlag = gridComara.compare(comparaSolution, neighborSolution);
			if (compFlag == -1) {
				return false;// 可跳出
			} else if (compFlag == 1) {
				externalPopulation.remove(i);// 需要持续进行
			} else {
				if (isEqualByGrid(comparaSolution, neighborSolution)) {
					if (replaceByDistance(neighborSolution, i, externalPopulation, 1.0)) {
						return true;
					} else
						return false;
				} // else 不在同一格子
			}
		}
		// }
		externalPopulation.add(neighborSolution);
		return true;
	}

//	private boolean isOutOfBounds(GridPermutationSolution<Integer> neighborSolution) {
//		for(int i = 0;i<neighborSolution.getNumberOfObjectives();i++){
//			if(neighborSolution.getObjective(i)<lb[i]){
//				return true;
//			}
//		}
//		return false;
//	}
	
	private double pbi(double[] solutionDiff, double[] vector) {
	      double d1, d2, nl;
	      double theta = 5.0;
	      
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

//	private boolean isOutOfBounds(GridPermutationSolution<Integer> neighborSolution) {
//		for(int i = 0;i<neighborSolution.getNumberOfObjectives();i++){
//			if(neighborSolution.getObjective(i) > ub[i] || 
//					neighborSolution.getObjective(i)<lb[i]){
//				return true;
//			}
//		}
//		return false;
//	}

	/**
	 * 添加到临时population中，临时population的解将在循环的末尾赋值给workPopulation
	 * @param neighborSolution
	 * @param tempList
	 * @return
	 */
	protected boolean addToTempPopulation(GridPermutationSolution<Integer> neighborSolution,
			List<GridPermutationSolution<Integer>> tempList) {
		if (!plsUtils.isEqualsToList(neighborSolution, workPopulation)) {
			for(int i = tempList.size()-1; i>=0; i--){
				GridPermutationSolution<Integer> solution = tempList.get(i);
	 			GridComparator comparator = new GridComparator();
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
			
			double sigma = (nadirPoint[i] - idealPoint[i])/(double) (2.0*division);
			ub[i] = nadirPoint[i]+sigma;
			lb[i] = idealPoint[i]-sigma;
			unitLen[i] = ( ub[i]-lb[i] ) / (double) division;
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
			int gridCoordinate = (int) ((solution.getObjective(i) - lb[i])/unitLen[i]);
			if(solution.getObjective(i) < lb[i])
				gridCoordinate--;
			solution.setGridCoordinate(i, gridCoordinate);
		}
	}


	
	@Override
	public String getName() {
		return "GPLS-D";
	}

	@Override
	public String getDescription() {
		return "cdg Pareto Local Search";
	}
	
	@Override
	public List<GridPermutationSolution<Integer>> getResult() {
		return SolutionListUtils.getNondominatedSolutions(externalPopulation);
	}

}
