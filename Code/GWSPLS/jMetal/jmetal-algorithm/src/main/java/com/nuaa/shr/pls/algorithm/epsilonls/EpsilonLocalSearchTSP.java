package com.nuaa.shr.pls.algorithm.epsilonls;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.uma.jmetal.operator.NeighborSearchOperator;
import org.uma.jmetal.problem.multiobjective.ManyObjectiveTSP;
import org.uma.jmetal.solution.PermutationSolution;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.comparator.DominanceComparator;
import org.uma.jmetal.util.comparator.EpsilonDominanceComparator;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;

import com.nuaa.shr.pls.utils.SolutionCompareUtils;

@SuppressWarnings("serial")
public class EpsilonLocalSearchTSP extends AbstractLocalSearchTSP{

	private final int maxLocalSearchTime;
	private final int maxIteration;
	private final double epsilon;
	
	private int iteration; // current iteration
	private int localSearchTime; // local search times 
	private boolean isUpdate; // update archive or not 
	
	private List<PermutationSolution<Integer>> externalPopulation ;
	private List<PermutationSolution<Integer>> tempList;
	private double[] idealPoint ;
	private EpsilonDominanceComparator<PermutationSolution<Integer>> epsilonDominance ;
	
	private SolutionCompareUtils<PermutationSolution<Integer>> solutionUtils = new SolutionCompareUtils<>();
	
	public EpsilonLocalSearchTSP(ManyObjectiveTSP problem,int maxPopulationSize, int maxLocalSearchTime, int maxIteration,
			double epsilon,double searchProbility, SolutionListEvaluator<PermutationSolution<Integer>> evaluator, NeighborSearchOperator<PermutationSolution<Integer>> neighborSearchOperator) {
		super(problem);
		setMaxPopulationSize(maxPopulationSize);
		this.maxIteration = maxIteration;
		this.maxLocalSearchTime = maxLocalSearchTime;
		this.epsilon = epsilon ;
		this.searchProbility = searchProbility;
		this.evaluator = evaluator;
		this.neighborSearchOperator = neighborSearchOperator;
	}

	@Override
	protected void initOtherProgress() {
		iteration = 0;
		localSearchTime = 0;
		isUpdate = true ;
		
		externalPopulation = new ArrayList<PermutationSolution<Integer>>();
		externalPopulation.addAll(SolutionListUtils.getNondominatedSolutions(getPopulation()));
		tempList = new ArrayList<>();
		idealPoint = new double[getProblem().getNumberOfObjectives()];
		InitialIdealPoint();
		epsilonDominance = new EpsilonDominanceComparator<>(epsilon);
	}

	private void InitialIdealPoint() {
		for(int i = 0;i<idealPoint.length;i++){
			idealPoint[i] = Double.MAX_VALUE ;
		}
		for(PermutationSolution<Integer> solution : externalPopulation){
			updateIdealPoint(solution);
		}
	}

	private void updateIdealPoint(PermutationSolution<Integer> solution) {
		for(int i = 0;i<solution.getNumberOfObjectives();i++){
			if(solution.getObjective(i)<idealPoint[i])
				idealPoint[i] = solution.getObjective(i);
		}
		
	}

	/**
	 * set population as tempList
	 */
	@Override
	protected void updateOtherProgress() {
		
		tempList.clear();
		iteration++;
		JMetalLogger.logger.info(iteration + " iteration...");
		
//		try {
//			new SolutionListOutput(getPopulation()).printObjectivesToFile("results/media/FUN"+iteration+".tsv");
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}

	@Override
	protected boolean isStoppingConditionReached() {
//		boolean flag =  iteration > maxIteration || 
//				localSearchTime > maxLocalSearchTime
		
		boolean flag =  iteration > maxIteration || 
				localSearchTime > maxLocalSearchTime || 
				!isUpdate;
		isUpdate = false ;
		return flag ;
	}

	
	/**
	 * 更新temp population list and extenal population
	 */
	@Override
	protected List<PermutationSolution<Integer>> replacement(List<PermutationSolution<Integer>> population,
			List<PermutationSolution<Integer>> offspringPopulation) {
		tempList.addAll(getPopulation());
		for(PermutationSolution<Integer> solution : offspringPopulation){
			updateIdealPoint(solution);
			addToTempPopulation(solution);
			if(addToArchive(solution))
				isUpdate = true;
		} 
		
		localSearchTime += getPopulation().size();
		population.clear();
		population.addAll(tempList);
		return population;
	}

	private boolean addToArchive(PermutationSolution<Integer> solution) {
		 epsilonDominance.setIdealValue(idealPoint);
		 Iterator<PermutationSolution<Integer>> iterator = externalPopulation.iterator();
		 while(iterator.hasNext()){
			 PermutationSolution<Integer> oldSolution = iterator.next();
			 int flag = epsilonDominance.compare(solution, oldSolution);
				if (flag < 0) {
					iterator.remove();
				} else if (flag > 0) {
					return false;
				} else if (solutionUtils.isDuplicate(solution, oldSolution)) {
					return false;
				}
		 }
		 return externalPopulation.add(solution);
	}
	
	/**
	 * pop acceptance in Epsilon-MOEA
	 * @param solution
	 */
	private void addToTempPopulation(PermutationSolution<Integer> solution) {
		DominanceComparator<PermutationSolution<Integer>> dominanceComparator = new DominanceComparator<>();
		List<Integer> dominates = new ArrayList<Integer>();
		boolean dominated = false;
		boolean isEqual = false  ;
		for (int i = 0; i < tempList.size(); i++) {
			int flag = dominanceComparator.compare(solution, 
					tempList.get(i));

			if (flag < 0) {
				dominates.add(i);
			} else if (flag > 0) {
				dominated = true;
			}else if(solutionUtils.isObjectiveEqual(solution, tempList.get(i))){
				isEqual = true;
			}
		}
		
		if(!dominates.isEmpty()){
			int index = dominates.get(new Random().nextInt(dominates.size())) ;
			tempList.remove(index);
			tempList.add(solution);
		}else if(!dominated && !isEqual){
			tempList.remove(new Random().nextInt(tempList.size()));
			tempList.add(solution);
		}
		
	}

	@Override
	public List<PermutationSolution<Integer>> getResult() {
		return externalPopulation;
	}
	
	@Override
	public String getName() {
		return "Epsilon-MOEA";
	}

	@Override
	public String getDescription() {
		return "epsilon-dominance MOEA";
	}
}
