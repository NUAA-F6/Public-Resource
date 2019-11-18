package com.nuaa.shr.pls;

import java.util.ArrayList;
import java.util.List;

import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.PermutationSolution;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.comparator.DominanceComparator;
import org.uma.jmetal.util.fileoutput.SolutionListOutput;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;

import com.nuaa.shr.pls.utils.PlsSolutionUtils;

@SuppressWarnings("serial")
public class ParetoLocalSearchStableCav extends AbstractParetoLocalSearch<PermutationSolution<Integer>>{
	
	private boolean trigger;
	public ParetoLocalSearchStableCav(Problem<PermutationSolution<Integer>> problem, int maxIteration, int populationSize){
		super(problem, maxIteration, populationSize);
	}

	@Override
	public void run() {
		List<PermutationSolution<Integer>> initPopulation = createInitialPopulation();
		//评估目标值
		evaluator.evaluate(initPopulation, problem);
		workPopulation = initPopulation;
		updateCandidateEdges();
		initializeUniformWeight();
		initializeIdealPoint();
		associate();

		while(iteration++ < this.maxIteration){
			long time = System.currentTimeMillis();
			trigger = true;
			for(int i = 0;i<workPopulation.size();i++){
				seachNeighbor(workPopulation.get(i),i);
			}
			System.out.println("search neighbor time:"+(System.currentTimeMillis()-time));
			updateCandidateEdges();
			System.out.println(iteration+" iteration\n");
			new SolutionListOutput(workPopulation).setSeparator("\t")
					.setFunFileOutputContext(new DefaultFileOutputContext("results/pls/FUN" + iteration)).print();
			
			if(trigger)
				break;
		}
		workPopulation = SolutionListUtils.getNondominatedSolutions(workPopulation);
		externalPopulation = new ArrayList<>();
		externalPopulation.addAll(workPopulation);
		while(iteration++ < this.maxIteration && workPopulation.size()>0){
			trigger = true;
			long time = System.currentTimeMillis();
			List<PermutationSolution<Integer>> tempList = new ArrayList<>();
			for(int i = 0;i<workPopulation.size();i++){
				searchNeighborhood(workPopulation.get(i), tempList);
			}
			System.out.println("domination search neighbor time:"+(System.currentTimeMillis()-time));
			
			workPopulation.clear();
			workPopulation.addAll(tempList);
			updateCandidateEdges();
			
			System.out.println(iteration+" iteration\n");
			new SolutionListOutput(externalPopulation).setSeparator("\t")
					.setFunFileOutputContext(new DefaultFileOutputContext("results/pls/FUN" + iteration)).print();
			
			if(trigger)
				break;
		}
	}


	/**
	 * 搜索邻居解
	 * @param utils 
	 * @param index 为在index的解做pls
	 * @throws Exception 
	 */
	private void searchNeighborhood(PermutationSolution<Integer> solution, 
			List<PermutationSolution<Integer>> tempList){		
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
						PermutationSolution<Integer> neighborSolution = getANeighborSolution(solution, i, j);
						DominanceComparator<Solution<?>> comparator = new DominanceComparator<>();
						int dominateFlag = comparator.compare(solution, neighborSolution);
						//&& (utils.predict(neighborSolution).equals("positive")||Math.random()>0.9)
						if(dominateFlag >= 0){
							if(addToExternalPopulation(neighborSolution)){
								addToTempPopulation(neighborSolution,tempList);
								trigger = false;
							}
						}
						
				}
			}
		}
		
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
	

	private void searchNeighborByDomination(PermutationSolution<Integer> permutationSolution, int index) {
		for(int i = 0;i<problem.getNumberOfVariables()-2;i++){
			for(int j = i+2;j<problem.getNumberOfVariables();j++){
				int subLastNode = (int) permutationSolution.getVariableValue(j);//子串的最后一个节点
				int firstNode = (int) permutationSolution.getVariableValue(i); //主串断开开始的节点
				int subFirstNode = (int) permutationSolution.getVariableValue(i+1);//子串的第一个节点
				int lastNode;
				if(j<problem.getNumberOfVariables()-1)
					lastNode = (int) permutationSolution.getVariableValue(j+1);//主串断开结束的节点
				else lastNode = (int) permutationSolution.getVariableValue(0);
				
				if(candidateEdge[firstNode][subLastNode]==1||candidateEdge[subFirstNode][lastNode]==1){
						PermutationSolution<Integer> neighborSolution = getANeighborSolution(permutationSolution, i, j);
						updateIdealPoint(neighborSolution);
						DominanceComparator<Solution<?>> comparator = new DominanceComparator<>();
						int flag = comparator.compare(workPopulation.get(index), neighborSolution);
						
						if(flag > 0){
							workPopulation.set(index, neighborSolution);
							trigger = false;
						}
				}
			}
		}
		
	}

	private void seachNeighbor(PermutationSolution<Integer> permutationSolution, int index) {
		for(int i = 0;i<problem.getNumberOfVariables()-2;i++){
			for(int j = i+2;j<problem.getNumberOfVariables();j++){
				int subLastNode = (int) permutationSolution.getVariableValue(j);//子串的最后一个节点
				int firstNode = (int) permutationSolution.getVariableValue(i); //主串断开开始的节点
				int subFirstNode = (int) permutationSolution.getVariableValue(i+1);//子串的第一个节点
				int lastNode;
				if(j<problem.getNumberOfVariables()-1)
					lastNode = (int) permutationSolution.getVariableValue(j+1);//主串断开结束的节点
				else lastNode = (int) permutationSolution.getVariableValue(0);
				
				if(candidateEdge[firstNode][subLastNode]==1||candidateEdge[subFirstNode][lastNode]==1){
						PermutationSolution<Integer> neighborSolution = getANeighborSolution(permutationSolution, i, j);
						updateIdealPoint(neighborSolution);
						double f1 = fitnessFunction(neighborSolution, lambda[index]);
						double f2 = fitnessFunction(workPopulation.get(index), lambda[index]);
						if(f1 < f2){
							workPopulation.set(index, neighborSolution);
							trigger = false;
						}
				}
			}
		}
		
		
	}

	private void associate() {
		List<PermutationSolution<Integer>> tempList = new ArrayList<PermutationSolution<Integer>>();
		tempList.addAll(workPopulation);
		workPopulation.clear();
		for(int i = 0;i<lambda.length;i++){
			double minAngle = Double.MAX_VALUE;
			int index = 0;
			for(int j = 0;j<tempList.size();j++){
				double[] fp = adjustObjectives(tempList.get(j));
				double tempAngle = plsUtils.sin(fp, lambda[i]);
				if(tempAngle<minAngle){
					minAngle = tempAngle;
					index = j;
				}
			}
			workPopulation.add(tempList.get(index));
		}
		
	}

	/**
	 * 获得邻居解
	 * @param solution
	 * @param startIndex 断开的位置
	 * @param endIndex 断开结束的位置
	 * @return
	 */
	public PermutationSolution<Integer> getANeighborSolution(PermutationSolution<Integer> solution, int startIndex,
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


	
}
