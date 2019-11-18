package com.nuaa.shr.pls.utils;

import java.util.ArrayList;
import java.util.List;

import org.uma.jmetal.solution.GridPermutationSolution;
import org.uma.jmetal.solution.PermutationSolution;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.comparator.DominanceComparator;

public class PlsSolutionUtils {
	
	public static final String distanceAttr = "com.nuaa.shr.pls.utils.PlsSolutionUtils.Distance";
	
	/**
	 * 从reducingPopulation中获取多样性最好的解
	 * @param reducingPopulation
	 * @param number
	 * @return
	 */
	public static List<PermutationSolution<Integer>> getBestDiversitySolutionsFromList(
			List<PermutationSolution<Integer>> reducingPopulation, int number) {
		
		int numberOfObjectives = reducingPopulation.get(0).getNumberOfObjectives();
		initDistance(reducingPopulation);
		List<PermutationSolution<Integer>> reducedPopu = new ArrayList<>();
		
		List<PermutationSolution<Integer>> extremeSolutions = 
				getExtrmeSolution(reducingPopulation, numberOfObjectives);
		
		for(int i = 0;i<extremeSolutions.size();i++){
			PermutationSolution<Integer> solution = extremeSolutions.get(i);
			reducedPopu.add(solution);
			updateDistance(reducingPopulation, solution);
		}
		
		while(reducedPopu.size() < number){
			PermutationSolution<Integer> solution = getBestSolution(reducingPopulation);
			reducedPopu.add(solution);
			updateDistance(reducingPopulation, solution);
		}
		return reducedPopu;
	}


	private static List<PermutationSolution<Integer>> getExtrmeSolution(
			List<PermutationSolution<Integer>> reducingPopulation, int numberOfObjectives) {
		List<PermutationSolution<Integer>> extremeSolutions = new ArrayList<>();
		for(int i = 0;i<numberOfObjectives;i++){
			double max = Double.MIN_VALUE;
			int index = 0;
			for(int j = 0;j<reducingPopulation.size();j++){
				if(reducingPopulation.get(j).getObjective(i) > max){
					index = j;
					max = reducingPopulation.get(j).getObjective(i);
				}
			}
			extremeSolutions.add(reducingPopulation.get(index));
		}
		return extremeSolutions;
	}
	
	
	public static void setDistance(List<PermutationSolution<Integer>> reducingPopulation, 
			List<PermutationSolution<Integer>> reducedPopulation){
		for(int i = 0;i<reducingPopulation.size();i++){
			double distance = distanceOfSolutionToList(reducingPopulation.get(i), reducedPopulation);
			reducingPopulation.get(i).setAttribute(distanceAttr, distance);
		}
	}
	
	/**
	 * 设置距离初始值
	 * @param reducingPopulation
	 */
	public static void initDistance(List<PermutationSolution<Integer>> reducingPopulation){
		for(int i = 0;i<reducingPopulation.size();i++){
			reducingPopulation.get(i).setAttribute(distanceAttr, Double.MAX_VALUE);
		}
	}
	
	/**
	 * 更新距离属性值，仅与新加入的解比较
	 * @param reducingPopulation
	 * @param solution
	 */
	public static void updateDistance(List<PermutationSolution<Integer>> reducingPopulation, 
			PermutationSolution<Integer> solution){
		for(int i = 0;i<reducingPopulation.size();i++){
			PermutationSolution<Integer> comparaSolution = reducingPopulation.get(i);
			double newDistance = distanceBetweenObjectives(solution, comparaSolution);
			if(newDistance<(double)comparaSolution.getAttribute(distanceAttr))
				reducingPopulation.get(i).setAttribute(distanceAttr, newDistance);
		}
	}

	/**
	 * 获取最小距离最大的解
	 * @param reducingPopulation
	 * @return
	 */
	public static PermutationSolution<Integer> getBestSolution(List<PermutationSolution<Integer>> reducingPopulation){
		double maxDistance = Double.MIN_VALUE;
		int index = 0;
		for(int i = 0;i<reducingPopulation.size();i++){
			Solution<?> comparaSolution = reducingPopulation.get(i);
			if((double)comparaSolution.getAttribute(distanceAttr)>maxDistance){
				maxDistance = (double)comparaSolution.getAttribute(distanceAttr);
				index = i;
			}
		}
		return reducingPopulation.get(index);
	}
	

	/**
	 * 解到集合的最小距离
	 * @param permutationSolution
	 * @param reducedPopu
	 * @return
	 */
	public static double distanceOfSolutionToList(PermutationSolution<Integer> permutationSolution,
			List<PermutationSolution<Integer>> reducedPopu) {
        double distance = Double.MAX_VALUE;

        // found the min distance respect to population
        for (int i = 0; i < reducedPopu.size();i++){
            double aux = distanceBetweenObjectives(permutationSolution,reducedPopu.get(i));
            if (aux < distance)
                distance = aux;
        } // for

        return distance;
	}
	
	
	private static double distanceBetweenObjectives(PermutationSolution<Integer> firstSolution, PermutationSolution<Integer> secondSolution) {
		   
		    double diff;  
		    double distance = 0.0;
		    //euclidean distance
		    for (int nObj = 0; nObj < firstSolution.getNumberOfObjectives();nObj++){
		      diff = firstSolution.getObjective(nObj) - secondSolution.getObjective(nObj);
		      distance += Math.pow(diff,2.0);           
		    } // for   
		        
		    return Math.sqrt(distance);
	  }
	
	/**
	 * 是否被population中任何solution支配
	 * @param neighborSolution
	 * @param population
	 * @return
	 */
	public static boolean isDominatedByList(PermutationSolution<Integer> neighborSolution,
			List<PermutationSolution<Integer>> population) {
		for(int i = 0;i<population.size();i++){
			Solution<?> comparaSolution = population.get(i);
			DominanceComparator<Solution<?>> comparator = new DominanceComparator<>();
			//comparaSolution dominates neighborSolution
			if(comparator.compare(comparaSolution, neighborSolution)==-1)
				return true;
		}
		return false;
	}
	/**
	 * externalPopulation 中是否含有与 neighborSolution相等的解
	 * @param neighborSolution
	 * @param externalPopulation
	 * @return
	 */
	public static boolean isEqualsToList(PermutationSolution<Integer> neighborSolution,
			List<PermutationSolution<Integer>> externalPopulation) {
		for(int i = 0;i<externalPopulation.size();i++){
			Solution<?> comparaSolution = externalPopulation.get(i);
			if(isEqual(comparaSolution, neighborSolution))
				return true;
		}
		return false;
	}

	/**
	 * 两个解是否完全相同
	 * @param comparaSolution
	 * @param neighborSolution
	 * @return
	 */
	public static boolean isEqual(Solution<?> comparaSolution, PermutationSolution<Integer> neighborSolution) {
		for(int i = 0;i<comparaSolution.getNumberOfVariables();i++){
			if(comparaSolution.getVariableValue(i)!=neighborSolution.getVariableValue(i))
				return false;
		}
		return true;
	}
	
	/**
	 * 找到极值点
	 * @param population
	 * @return
	 */
	public static List<PermutationSolution<Integer>> computeExtremePoints(
			List<PermutationSolution<Integer>> population) {
		
		int numberOfObjectives = population.get(0).getNumberOfObjectives();
		List<PermutationSolution<Integer>> extremePoints = new ArrayList<>();
		double[] zidealPoint = getZidealPoint(population);
		for (int j = 0; j < numberOfObjectives; j++) {
			int index = -1;
			double min = Double.MAX_VALUE;

			for (int i = 0; i < population.size(); i++) {
				double asfValue = asfFunction(population.get(i), j, zidealPoint);
				if (asfValue < min) {
					min = asfValue;
					index = i;
				}
			}

			extremePoints.add(population.get(index));
		}
		return extremePoints;
	}
	


	public static double[] getZidealPoint(List<PermutationSolution<Integer>> population) {
		int numberOfObjectives = population.get(0).getNumberOfObjectives();
		double[] zideal = new double[numberOfObjectives];
		for(int i = 0;i<numberOfObjectives;i++){
			double minValue = Double.MAX_VALUE;
			for(int j = 0;j<population.size();j++){
				if(population.get(j).getObjective(i)<minValue){
					minValue = population.get(j).getObjective(i);
				}
			}
			zideal[i] = minValue;
			
		}
		return zideal;
	}


	public static double asfFunction(PermutationSolution<Integer> sol, int j, double[] zideal) {
		
		int numberOfObjectives = sol.getNumberOfObjectives();
		double max = Double.MIN_VALUE;
		double epsilon = 1.0E-10;

		for (int i = 0; i < numberOfObjectives; i++) {

			double val = Math.abs(sol.getObjective(i) - zideal[i]);

			if (j != i)
				val = val / epsilon;

			if (val > max)
				max = val;
		}

		return max;
	}
//	private List<PermutationSolution<Integer>> getTrainData() {
//	List<PermutationSolution<Integer>> trainData = new ArrayList<>();
//	if(externalPopulation.size()<300){
//		trainData.addAll(externalPopulation);
//	}else{
//		List<Integer> indexList = new ArrayList<>();
//		for(int i = 0;i<externalPopulation.size();i++)
//			indexList.add(i);
//		Collections.shuffle(indexList);
//		for(int i = 0;i<300;i++){
//			trainData.add(externalPopulation.get(indexList.get(i)));
//		}
//	}
//	return trainData;
//}


	public static boolean isDominatedByOrEqualToList(Solution<?> neighborSolution,
			List<GridPermutationSolution<Integer>> tempList) {
		for(int i = 0;i<tempList.size();i++){
			Solution<?> comparaSolution = tempList.get(i);
			DominanceComparator<Solution<?>> comparator = new DominanceComparator<>();
			//comparaSolution dominates neighborSolution
			if(comparator.compare(comparaSolution, neighborSolution)==-1||
					isEqual(comparaSolution, neighborSolution))
				return true;
		}
		return false;
	}


	private static boolean isEqual(Solution<?> comparaSolution, Solution<?> neighborSolution) {
		for(int i = 0;i<comparaSolution.getNumberOfVariables();i++){
			if(comparaSolution.getVariableValue(i)!=neighborSolution.getVariableValue(i))
				return false;
		}
		return true;
	}


	public static boolean isEqualsToList(Solution<?> neighborSolution,
			List<GridPermutationSolution<Integer>> population) {
		for(int i = 0;i<population.size();i++){
			Solution<?> comparaSolution = population.get(i);
			if(isEqual(comparaSolution, neighborSolution))
				return true;
		}
		return false;
	}
}
