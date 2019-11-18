package com.nuaa.shr.pls;

import java.util.ArrayList;
import java.util.List;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.problem.multiobjective.DefaultManyObjectiveTSP;
import org.uma.jmetal.solution.PermutationSolution;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.comparator.DominanceComparator;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;
import org.uma.jmetal.util.fileoutput.SolutionListOutput;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;

import com.nuaa.shr.pls.utils.PlsSolutionUtils;

@SuppressWarnings("serial")
public class LearningParetoLocalSearch implements Algorithm<List<PermutationSolution<Integer>>>{
	
	int count = 0;
	private int iteration;
	private int maxIteration;
	private int populationSize;
	protected final SolutionListEvaluator<PermutationSolution<Integer>> evaluator  = new SequentialSolutionListEvaluator<PermutationSolution<Integer>>();;
	protected int evaluations;
	protected DefaultManyObjectiveTSP problem;
	
	protected List<PermutationSolution<Integer>> workPopulation;
	protected List<PermutationSolution<Integer>> externalPopulation;
	protected int[][] candidateEdge;//候选集邻接矩阵，1代表在候选集中，0代表不在候选集中
	
//	private JMetalRandom randomGenerator = JMetalRandom.getInstance() ;
	
	public LearningParetoLocalSearch(DefaultManyObjectiveTSP problem, int maxIteration, int populationSize){
		this.maxIteration = maxIteration;
		this.populationSize = populationSize;
		this.problem = problem;
		int numberOfVariables = problem.getNumberOfVariables();
		candidateEdge = new int[numberOfVariables][numberOfVariables];
	}
	
	private void clearCandidateEdges() {
		for(int i = 0;i<problem.getNumberOfVariables();i++){
			for(int j = 0;j<problem.getNumberOfVariables();j++){
				candidateEdge[i][j] = 0;
			}
		}
		
	}

	@Override
	public void run() {
		List<PermutationSolution<Integer>> initPopulation = createInitialPopulation();
		//评估目标值
		evaluator.evaluate(initPopulation, problem);
		
		workPopulation = SolutionListUtils.getNondominatedSolutions(initPopulation);
		externalPopulation = new ArrayList<PermutationSolution<Integer>>();
		externalPopulation.addAll(workPopulation);
		updateCandidateEdges();
		
		iteration = 0;
		while (workPopulation.size()!=0 && iteration++<this.maxIteration/2){
//			System.out.println("in");
			System.out.println("work population size:"+workPopulation.size()+" || external population size:"+externalPopulation.size());
			WekaUtils utils = new WekaUtils();
			try {
				utils.svmLearning(externalPopulation, iteration);
				List<PermutationSolution<Integer>> tempList = new ArrayList<>();
				long time = System.currentTimeMillis();
				for (int index = 0; index < workPopulation.size(); index++) {
					PermutationSolution<Integer> solution = workPopulation.get(index);					
					searchNeighborhood(solution, tempList, utils);
				}
				System.out.println("search neighbor time: "+(System.currentTimeMillis()-time)+"ms");
				
				workPopulation.clear();
				workPopulation.addAll(tempList);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			//=======================================
//			reduceExternal();//测试减少外部集数量
//			reduceWorkPopulation();//减少工作集数量
			
			// 更新候选边集
			updateCandidateEdges();
			
			new SolutionListOutput(externalPopulation).setSeparator("\t").
				setFunFileOutputContext(new DefaultFileOutputContext("results/learning-pls/FUN"+iteration)).print();
			System.out.println(iteration+" iteration\n");
		}
		System.out.println("two phase started......\n");
		while (workPopulation.size()!=0 && iteration++<this.maxIteration){
//			System.out.println("in");
			System.out.println("work population size:"+workPopulation.size()+" || external population size:"+externalPopulation.size());
			try {
				List<PermutationSolution<Integer>> tempList = new ArrayList<>();
				long time = System.currentTimeMillis();
				for (int index = 0; index < workPopulation.size(); index++) {
					PermutationSolution<Integer> solution = workPopulation.get(index);					
					searchNeighborhood(solution, tempList, null);
				}
				System.out.println("search neighbor time: "+(System.currentTimeMillis()-time)+"ms");
				
				workPopulation.clear();
				workPopulation.addAll(tempList);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			// 更新候选边集
			updateCandidateEdges();
			
			new SolutionListOutput(externalPopulation).setSeparator("\t").
				setFunFileOutputContext(new DefaultFileOutputContext("results/learning-pls/FUN"+iteration)).print();
			System.out.println(iteration+" iteration\n");
		}
		
	}


	private void reduceWorkPopulation() {
		if(workPopulation.size()>this.populationSize){
			List<PermutationSolution<Integer>> reducedSolution = 
					PlsSolutionUtils.getBestDiversitySolutionsFromList(workPopulation, this.populationSize);
			workPopulation.clear();
			workPopulation.addAll(reducedSolution);
		}
		
	}

	private void reduceExternal() {
		if(externalPopulation.size()>this.populationSize){
			List<PermutationSolution<Integer>> reducedSolution = 
					PlsSolutionUtils.getBestDiversitySolutionsFromList(externalPopulation, this.populationSize);
			externalPopulation.clear();
			externalPopulation.addAll(reducedSolution);
		}
	}

	/**
	 * 搜索邻居解
	 * @param utils 
	 * @param index 为在index的解做pls
	 * @throws Exception 
	 */
	private void searchNeighborhood(PermutationSolution<Integer> solution, 
			List<PermutationSolution<Integer>> tempList, WekaUtils utils) throws Exception{
		for(int i = 0;i<problem.getNumberOfVariables()-2;i++){
			for(int j = i+2;j<problem.getNumberOfVariables();j++){
				int subLastNode = (int) solution.getVariableValue(j);//子串的最后一个节点
				int firstNode = (int) solution.getVariableValue(i); //主串断开开始的节点
				int subFirstNode = (int) solution.getVariableValue(i+1);//子串的第一个节点
				int lastNode;
				if(j<problem.getNumberOfVariables()-1)
					lastNode = (int) solution.getVariableValue(j+1);//主串断开结束的节点
				else lastNode = (int) solution.getVariableValue(0);
				
				if(candidateEdge[firstNode][subLastNode]==1||candidateEdge[subFirstNode][lastNode]==1){
					if(anyObjectiveIsLess(firstNode,lastNode,subFirstNode,subLastNode)){
					
						PermutationSolution<Integer> neighborSolution = getANeighborSolution(solution, i, j);
						DominanceComparator<Solution<?>> comparator = new DominanceComparator<>();
						int dominateFlag = comparator.compare(solution, neighborSolution);
						//&& (utils.predict(neighborSolution).equals("positive")||Math.random()>0.9)
						if(utils!=null){
							if(dominateFlag >= 0 && (utils.predict(neighborSolution).equals("positive")||Math.random()>0.9)){
								if(addToExternalPopulation(neighborSolution)){
									addToTempPopulation(neighborSolution,tempList);
								}
							}
						}else{
							if(dominateFlag >= 0){
								if(addToExternalPopulation(neighborSolution)){
									addToTempPopulation(neighborSolution,tempList);
								}
							}
						}
						
					}
				}
			}
		}
				
//		if(iteration==150){
//			new SolutionListOutput(outList).setSeparator("\t").
//				setFunFileOutputContext(new DefaultFileOutputContext("results/neighbor/neighbor"+
//						count++)).print();
//		}
//		
	}

	/**
	 * 获得邻居解
	 * @param solution
	 * @param startIndex
	 * @param endIndex
	 * @return
	 */
	private PermutationSolution<Integer> getANeighborSolution(PermutationSolution<Integer> solution, int startIndex,
			int endIndex) {
		PermutationSolution<Integer> neighborSolution = this.problem.createSolution();
		int pos;
		for(pos = 0;pos<startIndex+1;pos++){
			neighborSolution.setVariableValue(pos, solution.getVariableValue(pos));
		}
		//将子串倒序复制给neighborSolution
		int k = endIndex;
		for(pos = startIndex+1; pos<endIndex+1; pos++){
			neighborSolution.setVariableValue(pos, solution.getVariableValue(k--));
		}
		for(pos = endIndex+1;pos<solution.getNumberOfVariables();pos++){
			neighborSolution.setVariableValue(pos, solution.getVariableValue(pos));
		}
		this.problem.evaluate(neighborSolution);
		return neighborSolution;
	}

	private boolean anyObjectiveIsLess(int firstNode, int lastNode, int subFirstNode, int subLastNode) {
		for(int i = 0;i<problem.getNumberOfObjectives();i++){
			if(problem.getDistance(firstNode, subLastNode).get(i)+problem.getDistance(subFirstNode, lastNode).get(i)<
					problem.getDistance(firstNode, lastNode).get(i)+problem.getDistance(subFirstNode, subLastNode).get(i))
				return true;
				
		}
		return false;
	}

	/**
	 * 添加到临时population中，临时population的解将在循环的末尾赋值给workPopulation
	 * @param neighborSolution
	 * @param tempList
	 * @return
	 */
	private boolean addToTempPopulation(PermutationSolution<Integer> neighborSolution, List<PermutationSolution<Integer>> tempList) {
		if(!PlsSolutionUtils.isDominatedByList(neighborSolution, tempList)){
			tempList.add(neighborSolution);
			reduceDominatedInList(neighborSolution, tempList);
			return true;
		}else return false;
	}

	/**
	 * 添加到外部集中
	 * @param neighborSolution
	 * @return
	 */
	private boolean addToExternalPopulation(PermutationSolution<Integer> neighborSolution) {
		if(!PlsSolutionUtils.isDominatedByList(neighborSolution,externalPopulation)&&!PlsSolutionUtils.isEqualsToList(neighborSolution,externalPopulation)){
			externalPopulation.add(neighborSolution);
			reduceDominatedInList(neighborSolution,externalPopulation);
			return true;
		}
		else return false;
	}


	private void removeWorst(List<PermutationSolution<Integer>> population) {
		int minMinIndex = 0;
		double minMinDistance = Double.MAX_VALUE;
		for(int i = 0;i<population.size();i++){
			double minDistance = Double.MAX_VALUE;
			for(int j = 0;j<population.size();j++){
				if(j!=i){
					double distance = distance(population.get(i),population.get(j));
					if(distance<minDistance){
						minDistance = distance;
					}
				}
			}
			if(minDistance < minMinDistance){
				minMinDistance = minDistance;
				minMinIndex = i;
			}
		}
		population.remove(minMinIndex);
	}
	
	private double distance(PermutationSolution<Integer> solution1,
			PermutationSolution<Integer> solution2) {
		double distance = 0.0;
		for(int i = 0;i<solution1.getNumberOfObjectives();i++){
			distance += Math.pow(solution1.getObjective(i)-solution2.getObjective(i), 2);
		}
		return Math.sqrt(distance);
	}

	/**
	 * 删除population所有被neighborSolution支配的解
	 * @param neighborSolution
	 * @param population
	 */
	private void reduceDominatedInList(PermutationSolution<Integer> neighborSolution,
			List<PermutationSolution<Integer>> population) {
		for(int i = population.size()-1;i>=0;i--){
			Solution<?> comparaSolution = population.get(i);
			DominanceComparator<Solution<?>> comparator = new DominanceComparator<>();
			if(comparator.compare(neighborSolution, comparaSolution)==-1){
				population.remove(i);
			}
		}
	}

	/**
	 * 更新边的候选集
	 */
	private void updateCandidateEdges() {
		clearCandidateEdges();//先清除候选边集
		
		//将所有在外部集中的边加入到候选边集
		for(int i = 0;i<workPopulation.size();i++){
			PermutationSolution<Integer> solution = workPopulation.get(i);
			for(int j = 0;j<solution.getNumberOfVariables()-1;j++){
				int frontNode = (int) solution.getVariableValue(j);
				int nextNode = (int) solution.getVariableValue(j+1);
				candidateEdge[frontNode][nextNode] = 1;
				candidateEdge[nextNode][frontNode] = 1;
			}
		}
		
	}

	@Override
	public List<PermutationSolution<Integer>> getResult() {
		return externalPopulation;
	}
	
	@Override
	public String getName() {
		return "PLS";
	}

	@Override
	public String getDescription() {
		return "Pareto Local Search";
	}

	  /**
	   * This method implements a default scheme create the initial population
	   * @return
	   */
	  protected List<PermutationSolution<Integer>> createInitialPopulation() {
	    List<PermutationSolution<Integer>> population = new ArrayList<>(this.populationSize);
	    for (int i = 0; i < this.populationSize; i++) {
	    	PermutationSolution<Integer> newIndividual = this.problem.createSolution();
	      population.add(newIndividual);
	    }
	    return population;
	  }
	
}
