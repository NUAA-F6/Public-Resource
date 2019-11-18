package org.uma.jmetal.problem.multiobjective;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.uma.jmetal.problem.impl.AbstractGridIntegerPermutationProblem;
import org.uma.jmetal.solution.GridPermutationSolution;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.solution.impl.ToolEmptySolution;
import org.uma.jmetal.util.solutionattribute.Ranking;
import org.uma.jmetal.util.solutionattribute.impl.DominanceRanking;

@SuppressWarnings("serial")
public class GridManyObjectiveKnapsack extends AbstractGridIntegerPermutationProblem {
	
	private int[][] profitArray;
	private int[][] weightArray;
	private int[] capacity;
	
	private List<List<Integer>> sortedProdIndex;
//	private Ranking<ToolEmptySolution> rank;//存放排好序的item
	
	
	public GridManyObjectiveKnapsack(String profitPath, String weightPath, String capacityPath, int prodCount, int numberOfObjectives) {
		profitArray = new int[prodCount][numberOfObjectives];
		weightArray = new int[prodCount][numberOfObjectives];
		capacity = new int[numberOfObjectives];
		setNumberOfObjectives(numberOfObjectives);
		setNumberOfVariables(prodCount);
		setName("Many-Objective-knapsack");
		
		try{
			readProblem(profitPath, weightPath, capacityPath);
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("Read Problem Error!");
		}
		setSortedIndex();
	}

	
	private void setSortedIndex() {
		List<ToolEmptySolution> selectedList = new ArrayList<ToolEmptySolution>();
		for (int i = 0; i < getNumberOfVariables(); i++) {
			ToolEmptySolution prod = new ToolEmptySolution(new EmptyProblem(getNumberOfObjectives()), i);
			double weightSum = 0;
			for(int j = 0;j<getNumberOfObjectives();j++){
				weightSum += weightArray[i][j];
			}
			for(int j = 0;j<getNumberOfObjectives();j++){
				prod.setObjective(j, (double)profitArray[i][j]/weightSum);
			}
			selectedList.add(prod);
		}
	    Ranking<ToolEmptySolution> ranking = new DominanceRanking<>() ;
	    Ranking<ToolEmptySolution> rank = ranking.computeRanking(selectedList);
	    sortedProdIndex = new ArrayList<>();
	    for(int i = 0;i<rank.getNumberOfSubfronts();i++){
	    	List<Integer> subList = new ArrayList<>();
	    	for(ToolEmptySolution customSolution : rank.getSubfront(i)){
	    		subList.add(customSolution.getIndex());
	    	}

	    	sortedProdIndex.add(subList);
	    }
//		
	}

	/**
	 * get 未被选中的rank最差的
	 * @param solution
	 * @param p
	 * @return
	 */
	public List<Integer> getWorstIndex(GridPermutationSolution<Integer> solution, double p){
	    List<Integer> worstIndexList = new ArrayList<>();
	    List<Integer> unSelectedItem = getUnselectedItem(solution);
	    int size = (int) (p*unSelectedItem.size());
	    
	    for(int i = sortedProdIndex.size()-1;i>=0;i--){
	    	/*
	    	 *打乱每一层，后面局部搜索中，如果当前的层数量大于要加入的数量，则随机加入一部分 
	    	 */
	    	List<Integer> subList = sortedProdIndex.get(i);
	    	Collections.shuffle(subList);
	    	
	    	for(int j = 0;j<subList.size();j++){
		    	if(solution.getVariableValue(subList.get(j)) == 0){
		    		worstIndexList.add(subList.get(j));
		    		if(worstIndexList.size()>=size)
		    			return worstIndexList;
		    	}
	    	}
	    }
	    return worstIndexList;
	}
	
	/**
	 * 返回没选择的Item
	 * @param solution
	 * @return
	 */
	private List<Integer> getUnselectedItem(GridPermutationSolution<Integer> solution) {
		List<Integer> unSelectedItem = new ArrayList<>();
		for(int i = 0;i<solution.getNumberOfVariables();i++){
			if(solution.getVariableValue(i)==0)
				unSelectedItem.add(i);
		}
		return unSelectedItem;
	}

	/**
	 * 已经被选中的最好rank的index
	 * @param solution
	 * @param p
	 * @return
	 */
	public List<Integer> getBestIndex(GridPermutationSolution<Integer> solution, double p){
		List<Integer> bestIndexList = new ArrayList<>();
		int size = (int) (p * (solution.getNumberOfVariables() - getUnselectedItem(solution).size()));
		for (int i = 0; i < sortedProdIndex.size(); i++) {
			List<Integer> subList = sortedProdIndex.get(i);
			Collections.shuffle(subList);
			for (int j = 0; j < subList.size(); j++) {
				if (solution.getVariableValue(subList.get(j)) == 1) {
					bestIndexList.add(subList.get(j));
		    		if(bestIndexList.size()>=size)
		    			return bestIndexList;
				}
			}
		}
	    return bestIndexList;
	}
	
	public List<Integer> getSelectedIndex(GridPermutationSolution<Integer> solution){
		List<Integer> selectedIndex = new ArrayList<>(); 
		for(int i = 0;i<solution.getNumberOfVariables();i++){
			if(solution.getVariableValue(i)==1){
				selectedIndex.add(i);
			}
		}
		return selectedIndex;
	}
	
	public List<Integer> getUnselectedIndex(GridPermutationSolution<Integer> solution){
		List<Integer> unSelectedIndex = new ArrayList<>(); 
		for(int i = 0;i<solution.getNumberOfVariables();i++){
			if(solution.getVariableValue(i)==0){
				unSelectedIndex.add(i);
			}
		}
		return unSelectedIndex;
	}

	private void readProblem(String profitPath, String weightPath, String capacityPath) 
			throws NumberFormatException, IOException {
		InputStream profitIn = getClass().getResourceAsStream(profitPath);
		InputStreamReader profitIsr = new InputStreamReader(profitIn);
		BufferedReader profitBr = new BufferedReader(profitIsr);
		
		InputStream weightIn = getClass().getResourceAsStream(weightPath);
		InputStreamReader weightIsr = new InputStreamReader(weightIn);
		BufferedReader weightBr = new BufferedReader(weightIsr);
		
		
		for(int i = 0;i<getNumberOfObjectives();i++){
			for(int j = 0;j<getNumberOfVariables();j++){
				profitArray[j][i] = Integer.parseInt(profitBr.readLine());
				weightArray[j][i] = Integer.parseInt(weightBr.readLine());
			}
		}
		
		InputStream capacityInput = getClass().getResourceAsStream(capacityPath);
		InputStreamReader capacityReader = new InputStreamReader(capacityInput);
		BufferedReader capacityBr = new BufferedReader(capacityReader);
		for(int i = 0;i<getNumberOfObjectives();i++){
			capacity[i] = Integer.parseInt(capacityBr.readLine());
		}
	}

	/**
	 * 解是否合法
	 * @param solution
	 * @return
	 */
	public boolean isValide(GridPermutationSolution<Integer> solution){
		double[] sumObjectives = new double[solution.getNumberOfObjectives()];
		for(int i = 0;i<solution.getNumberOfObjectives();i++){
			sumObjectives[i] = 0;
		}
		for(int i = 0;i<getNumberOfVariables();i++){
			if(solution.getVariableValue(i) == 1){
				for(int j = 0;j<solution.getNumberOfObjectives();j++){
					sumObjectives[j] += weightArray[i][j];
					if(sumObjectives[j]>capacity[j])
						return false;
				}
			}
		}
		return true;
	}

	/**
	 * 每个解的数量，不需要返回这个参数，重写了createSolution
	 */
	@Override
	public int getPermutationLength() {
		return getNumberOfVariables();
	}

	@Override
	public void evaluate(GridPermutationSolution<Integer> solution) {
		for(int k = 0;k<solution.getNumberOfObjectives();k++){
			int objective = 0;
			for(int i = 0;i<solution.getNumberOfVariables();i++){
				if(solution.getVariableValue(i)==1){
					objective += profitArray[i][k];
				}
			}
			solution.setObjective(k, -objective);
			
		}
	}
	
	@Override
	public GridPermutationSolution<Integer> createSolution() {
		GridPermutationSolution<Integer> solution = super.createSolution();
		for(int i = 0;i<solution.getNumberOfVariables();i++){
			solution.setVariableValue(i, 0);
		}
		return solution;
	}
	
	/**
	 * 随机初始化可行解
	 * @return
	 */
	public GridPermutationSolution<Integer> createValidSolution(){
		GridPermutationSolution<Integer> solution;
		do{
			solution = super.createSolution();
			for(int i = 0;i<solution.getNumberOfVariables();i++){
				int val = Math.random()<0.5?0:1;
				solution.setVariableValue(i, val);
			}
//			System.out.println(isValide(solution));
		}while(!isValide(solution));
		randomRepair(solution);
		return solution;
	}
	
	/**
	 * 根据权重方向创建解
	 * @param vector
	 * @return
	 */
	public GridPermutationSolution<Integer> createSolution(double[] vector){
		GridPermutationSolution<Integer> solution = createSolution();
		int[] remainWeight = new int[capacity.length];
		for(int i = 0;i<capacity.length;i++){
			remainWeight[i] = capacity[i];
		}
		while(unFull(remainWeight)){
			int index = getUnselectedMaxWeight(solution, remainWeight, vector);
			if(index >= 0){
				solution.setVariableValue(index, 1);
				for(int i = 0;i<remainWeight.length;i++){
					remainWeight[i] -= weightArray[index][i];
				}
			}
			else break;
		}
		return solution;
	}


	/**
	 * 是否已经满了
	 * @param remainWeight
	 * @return
	 */
	private boolean unFull(int[] remainWeight) {
		for(int i = 0;i<remainWeight.length;i++){
			if(remainWeight[i]<0)
				return false ;
		}
		return true;
	}

	/**
	 * 根据权重方向选择最大的未选择item
	 * @param solution
	 * @param remainWeight
	 * @param vector
	 * @return
	 */
	public int getUnselectedMaxWeight(GridPermutationSolution<Integer> solution,int[] remainWeight, double[] vector) {
		double max = Double.MIN_VALUE;
		int maxIndex = -1;
		for(int i = 0;i<getNumberOfVariables();i++){
			if(solution.getVariableValue(i)==0 && valide(i,remainWeight)){
				double ratio = calculateRatio(i, vector);
				if(ratio > max){
					maxIndex = i;
					max = ratio;
				}
			}
		}
		return maxIndex;
	}


	/**
	 * 是否合法
	 * @param prodIndex 产品Index
	 * @param remainWeight 剩余重量
	 * @return
	 */
	private boolean valide(int prodIndex, int[] remainWeight) {
		for(int i = 0;i<remainWeight.length;i++){
			if(weightArray[prodIndex][i]>remainWeight[i])
				return false;
		}
		return true;
	}


	/**
	 * 计算比值
	 * @param prodIndex 物品Indx
	 * @param vector 偏好向量
	 * @return
	 */
	private double calculateRatio(int prodIndex, double[] vector) {
		double weightSum = 0;
		double profitSum = 0;
		for(int i = 0;i<weightArray[prodIndex].length;i++){
			weightSum += weightArray[prodIndex][i];
			profitSum += vector[i] * profitArray[prodIndex][i];
		}
		return profitSum / weightSum;
	}


	/**
	 * 获取仍然可以添加的重量
	 * @param solution
	 * @return
	 */
	public int[] getRemainWeight(GridPermutationSolution<Integer> solution) {
		int[] remainWeight = new int[solution.getNumberOfObjectives()];
		for(int i = 0;i<capacity.length;i++){
			remainWeight[i] = capacity[i];
		}
		for(int i = 0;i<solution.getNumberOfVariables();i++){
			if(solution.getVariableValue(i) == 1){
				for(int objIdx = 0;objIdx<solution.getNumberOfObjectives();objIdx++){
					remainWeight[objIdx] -= weightArray[i][objIdx] ;
				}
			}
		}
		return remainWeight;
	}


	/**
	 * 选择未被选择比值最大的item（贪心）
	 * @param solution
	 * @param remainWeight
	 * @return
	 */
	public int getUnselectedMaxWeight(GridPermutationSolution<Integer> solution, int[] remainWeight) {
		double max = Double.MIN_VALUE;
		int maxIndex = -1;
		
		double[] defaultVector = new double[getNumberOfObjectives()];
		for(int i = 0;i<defaultVector.length;i++){
			defaultVector[i] = 0.5;
		}
		for(int i = 0;i<getNumberOfVariables();i++){
			if(solution.getVariableValue(i)==0 && valide(i,remainWeight)){
				double ratio = calculateRatio(i, defaultVector);
				if(ratio > max){
					maxIndex = i; 
					max = ratio;
				}
			}
		}
		return maxIndex;
	}


	/**
	 * 贪心策略修复item
	 * @param solution
	 */
	public void greedyRepair(GridPermutationSolution<Integer> solution) {
		int[] remainWeight = getRemainWeight(solution);
		while(unFull(remainWeight)){
			int index = getUnselectedMaxWeight(solution,remainWeight);
			if(index >= 0){
				solution.setVariableValue(index, 1);
				for(int i = 0;i<remainWeight.length;i++){
					remainWeight[i] -= weightArray[index][i];
				}
			}
			else break;
		}
		
	}
	
	/**
	 * 根据随机选择的item修复solution
	 * @param solution
	 */
	public void randomRepair(GridPermutationSolution<Integer> solution){
		int[] remainWeight = getRemainWeight(solution);
		while(unFull(remainWeight)){
			int index = getUnselIndexRandom(solution, remainWeight);
			if(index >=0 ){
				solution.setVariableValue(index, 1);
				for(int i = 0;i<remainWeight.length;i++){
					remainWeight[i] -= weightArray[index][i];
				}
			}
			else break;
		}
	}


	/**
	 * 随机选择没被选择的item
	 * @param solution
	 * @param remainWeight
	 * @return
	 */
	private int getUnselIndexRandom(GridPermutationSolution<Integer> solution, int[] remainWeight) {
		List<Integer> unSelectedIndexes = getUnselectedIndex(solution);
		List<Integer> tempIndexes = new ArrayList<>();
		tempIndexes.addAll(unSelectedIndexes);
		Collections.shuffle(tempIndexes);
		for(int i = 0;i<tempIndexes.size();i++){
			if(valide(tempIndexes.get(i),remainWeight))
				return tempIndexes.get(i);
		}
		return -1;
	}
	
	public void setName(String name){
		super.setName(name);
		
	}

}