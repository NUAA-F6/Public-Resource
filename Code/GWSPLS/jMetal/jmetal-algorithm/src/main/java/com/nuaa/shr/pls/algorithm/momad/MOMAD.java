package com.nuaa.shr.pls.algorithm.momad;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.operator.NeighborSearchOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.problem.multiobjective.ManyObjectiveKnapsack;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.PermutationUtility;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.comparator.DominanceComparator;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;

import com.nuaa.shr.pls.utils.InitializeMethod;
import com.nuaa.shr.pls.utils.SolutionCompareUtils;

@SuppressWarnings("serial")
public class MOMAD<S extends Solution<?>> implements Algorithm<List<S>> {
	private List<S> externalPopulation;
	private List<S> subProPopulation;
	private List<S> workPopulation;
	private List<S> tempPopulation;

	protected NeighborSearchOperator<S> neighborSearchOperator;
	private int maxPopulationSize;
	private Problem<S> problem;
	private int maxIteration;
	private int maxLocalSearchTime;
	private SolutionListEvaluator<S> evaluator;
	private InitialSolutionMethod initialSolutionMethod;
	public enum InitialSolutionMethod {
		GREEDY_METHOD, RANDOM_METHOD
	}


	private int iteration;
	private int localSearchTime;
	private double[][] lambda;
	
	private SolutionCompareUtils<S> solutionUtils = new SolutionCompareUtils<>();

	public void setMaxPopulationSize(int maxPopulationSize) {
		this.maxPopulationSize = maxPopulationSize;
	}

	public int getMaxPopulationSize() {
		return maxPopulationSize;
	}

	public void setProblem(Problem<S> problem) {
		this.problem = problem;
	}

	public Problem<S> getProblem() {
		return problem;
	}


	public MOMAD(Problem<S> problem, int maxPopuloationSize, int maxIteration, int maxLocalSearchTime,
			NeighborSearchOperator<S> neighborSearchOperator,SolutionListEvaluator<S> evaluator,
			InitialSolutionMethod initialSolutionMethod) {
		this.problem = problem ;
		this.evaluator = evaluator;
		this.initialSolutionMethod = initialSolutionMethod;
		this.maxIteration = maxIteration;
		this.maxLocalSearchTime = maxLocalSearchTime ;
		this.maxPopulationSize = maxPopuloationSize ;
		this.neighborSearchOperator = neighborSearchOperator;
	}
	
	@Override
	public void run() {
		subProPopulation = createInitialPopulation();
		evaluatePopulation(subProPopulation);
		initProcess();
		while (!isStopConditionReached()) {
			for (int i = 0; i < workPopulation.size(); i++) {
				List<S> neighborhood = getNeighborhood(workPopulation.get(i));
				for (S solution : neighborhood) {
					addToSubPopulation(solution);
					DominanceComparator<S> comparator = new DominanceComparator<>();
					if (comparator.compare(solution, workPopulation.get(i)) == -1) {
						if (addToPopulation(solution, externalPopulation)) {
							addToPopulation(solution, tempPopulation);
						}
					}
				}
				localSearchTime++;
			}
			for (int i = 0; i < subProPopulation.size(); i++) {
				List<S> neighborhood = getNeighborhood(subProPopulation.get(i));
				S solution = getMinFunctionSolution(neighborhood, i);
				addToSubPopulation(solution);
				if (addToPopulation(solution, externalPopulation)) {
					addToPopulation(solution, tempPopulation);
				}
				localSearchTime++;
			}
			updateProgress();
		}
	}

	private S getMinFunctionSolution(List<S> neighborhood, int i) {
		S minSolution = neighborhood.get(0);
		double minFun = function(minSolution, lambda[i]) ;
		for(S solution : neighborhood){
			if(function(solution,lambda[i])<minFun){
				minFun = function(solution, lambda[i]);
				minSolution = solution ;
			}
				
		}
		return minSolution;
	}

	private void evaluatePopulation(List<S> population) {
		evaluator.evaluate(population, problem);
	}

	private boolean isStopConditionReached() {
		return localSearchTime > maxLocalSearchTime || iteration > maxIteration;
	}

	private void addToSubPopulation(S solution) {
		Integer[] permList = new PermutationUtility().initPermutation(subProPopulation.size());
		for (int i = 0; i < permList.length; i++) {
			int k = permList[i];
			S oldSolution = subProPopulation.get(k);
			if (function(solution, lambda[k]) < function(oldSolution, lambda[k])) {
				subProPopulation.set(k, solution);
				break;
			}
		}
	}

	private double function(S solution, double[] ds) {
		double sum = 0.0;
		for (int n = 0; n < getProblem().getNumberOfObjectives(); n++) {
			sum += (ds[n]) * solution.getObjective(n);
		}

		return sum;
	}

	private void updateProgress() {
		workPopulation.clear();
		workPopulation.addAll(tempPopulation);
		tempPopulation.clear();
		neighborSearchOperator.setCandidate(workPopulation);
		iteration++;
		JMetalLogger.logger.info(iteration + " iteration...");	
		
	}

	private void initProcess() {
		lambda = new InitializeMethod().initializeUniformWeight(getProblem().getNumberOfObjectives(),
				getMaxPopulationSize());
		localSearchTime = 0;
		iteration = 0;
		
		workPopulation = new ArrayList<>();
		workPopulation.addAll(SolutionListUtils.getNondominatedSolutions(subProPopulation));
		
		tempPopulation = new ArrayList<>();
		
		externalPopulation = new ArrayList<>();
		externalPopulation.addAll(workPopulation);
		
		neighborSearchOperator.setCandidate(workPopulation);
		neighborSearchOperator.setComparator(null);

	}

	private boolean addToPopulation(S solution, List<S> population) {
		Iterator<S> iterator = population.iterator();
		DominanceComparator<S> comparator = new DominanceComparator<>();
		while (iterator.hasNext()) {
			S oldSolution = iterator.next();
			int flag = comparator.compare(solution, oldSolution);
			if (flag < 0) {
				iterator.remove();
			} else if (flag > 0) {
				return false;
			} else if (solutionUtils.isObjectiveEqual(solution, oldSolution)) {
				return false;
			}
		}
		return population.add(solution);
	}

	private List<S> getNeighborhood(S s) {
		return neighborSearchOperator.execute(s);
	}

	@SuppressWarnings("unchecked")
	private List<S> createInitialPopulation() {
		List<S> population = new ArrayList<>(getMaxPopulationSize());
		if (InitialSolutionMethod.RANDOM_METHOD.equals(initialSolutionMethod)) {
			for (int i = 0; i < getMaxPopulationSize(); i++) {
				S newIndividual = getProblem().createSolution();
				population.add(newIndividual);
			}
		} else if (InitialSolutionMethod.GREEDY_METHOD.equals(initialSolutionMethod)) {
			double[][] weightVectors = new InitializeMethod()
					.initializeUniformWeight(getProblem().getNumberOfObjectives(), getMaxPopulationSize());
			for (int i = 0; i < weightVectors.length; i++) {
				S newIndividual = (S) ((ManyObjectiveKnapsack) getProblem()).createSolution(weightVectors[i]);
				population.add(newIndividual);
			}
		}
		return population;
	}

	@Override
	public String getName() {
		return "MOMAD";
	}

	@Override
	public String getDescription() {
		return "memetic algorithm based on decomposition";
	}

	@Override
	public List<S> getResult() {
		return externalPopulation;
	}

}
