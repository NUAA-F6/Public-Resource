package com.nuaa.shr.pls.algorithm.abst;

import java.util.ArrayList;
import java.util.List;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.comparator.DominanceComparator;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;

import com.nuaa.shr.pls.utils.PlsUtils;

@SuppressWarnings("serial")
public abstract class AbstractParetoLocalSearch<S extends Solution<?>> implements Algorithm<List<S>> {


	protected int populationSize;
	protected Problem<S> problem;
	protected PlsUtils<S> plsUtils;
	protected List<S> workPopulation;
	protected List<S> externalPopulation;
	protected final SolutionListEvaluator<S> evaluator;
	protected int maxIteration;
	
	protected double[] idealPoint;
	protected double[] nadirPoint;

	protected int searchCount = 0;
	protected int runId = 0;
	
	public AbstractParetoLocalSearch(Problem<S> problem, int maxIteration, int populationSize) {
		this.problem = problem;
		idealPoint = new double[problem.getNumberOfObjectives()];
		nadirPoint = new double[problem.getNumberOfObjectives()];
		this.maxIteration = maxIteration;
		this.populationSize = populationSize;
		plsUtils = new PlsUtils<>();
		evaluator = new SequentialSolutionListEvaluator<S>();
		externalPopulation = new ArrayList<>();
	}

	/**
	 * initialize the ideal point
	 */
	protected void initializeIdealPoint() {
		for (int i = 0; i < problem.getNumberOfObjectives(); i++) {
			idealPoint[i] = 1.0e+30;
		}

		for (int i = 0; i < workPopulation.size(); i++) {
			updateIdealPoint(workPopulation.get(i));
		}
	}

	/**
	 * update ideal point according to individual
	 * 
	 * @param individual
	 */
	protected void updateIdealPoint(S individual) {
		for (int n = 0; n < problem.getNumberOfObjectives(); n++) {
			if (individual.getObjective(n) < idealPoint[n]) {
				idealPoint[n] = individual.getObjective(n);
			}
		}
	}
	
	protected void updateIdealPoint(List<S> population){
		for(int i = 0;i<population.size();i++){
			updateIdealPoint(population.get(i));
		}
	}
	@Override
	public List<S> getResult() {
		return externalPopulation;
	}



	public void initialNadirPoint() {
		for (int i = 0; i < problem.getNumberOfObjectives(); i++) {
			nadirPoint[i] = Double.NEGATIVE_INFINITY;
		}
		for (int i = 0; i < externalPopulation.size(); i++) {
			updateNadirPoint(externalPopulation.get(i));
		}
	}

	protected boolean addToExternalPopulation(S neighborSolution) {
		if (!plsUtils.isDominatedOrEqualToList(neighborSolution, externalPopulation)) {
			externalPopulation.add(neighborSolution);
			reduceDominatedInList(neighborSolution, externalPopulation);
			return true;
		} else
			return false;
	}
	
	protected boolean addToTempPopulation(S neighborSolution,
			List<S> tempList) {
		if (!plsUtils.isDominatedOrEqualToList(neighborSolution, tempList)) {
			tempList.add(neighborSolution);
			reduceDominatedInList(neighborSolution, tempList);
			return true;
		} else
			return false;
	}
	
	protected void reduceDominatedInList(S neighborSolution, List<S> population) {
		for (int i = population.size() - 1; i >= 0; i--) {
			S comparaSolution = population.get(i);
			DominanceComparator<S> comparator = new DominanceComparator<>();
			if (comparator.compare(neighborSolution, comparaSolution) == -1) {
				population.remove(i);
			}
		}
	}
	
	public void updateNadirPoint(S solution) {
		for (int i = 0; i < solution.getNumberOfObjectives(); i++) {
			if (solution.getObjective(i) > nadirPoint[i]) {
				nadirPoint[i] = solution.getObjective(i);
			}
		}

	}

	public int getSearchCount(){
		return searchCount;
	}
	
	/**
	 * This method implements a default scheme create the initial population
	 * 
	 * @return
	 */
	protected List<S> createInitialPopulation() {
		List<S> population = new ArrayList<>(this.populationSize);
		for (int i = 0; i < this.populationSize; i++) {
			S newIndividual = this.problem.createSolution();
			population.add(newIndividual);
		}
		return population;
	}
	
	public void setRunId(int runId){
		this.runId = runId;
	}
	
	public int getRunId(){
		return this.runId;
	}
}
