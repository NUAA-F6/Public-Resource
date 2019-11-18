package org.uma.jmetal.algorithm.impl;

import java.util.List;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;

@SuppressWarnings("serial")
public abstract class AbstractEvolutionarySingle<S extends Solution<?>, R> implements Algorithm<R> {
	private List<S> population;
	private int maxPopulationSize;
	private Problem<S> problem;

	  public List<S> getPopulation() {
		    return population;
	  }
	  public void setPopulation(List<S> population) {
	    this.population = population;
	  }

	  public void setMaxPopulationSize(int maxPopulationSize) {
	    this.maxPopulationSize = maxPopulationSize ;
	  }
	  public int getMaxPopulationSize() {
	    return maxPopulationSize ;
	  }

	  public void setProblem(Problem<S> problem) {
	    this.problem = problem ;
	  }
	  public Problem<S> getProblem() {
	    return problem ;
	  }
	
	@Override
	public void run() {
	    List<S> offspringPopulation;
	    List<S> matingPopulation;
		
	    population = createInitialPopulation();
	    population = evaluatePopulation(population);
	    initProcess();
		while(!isStopConditionReached()){
			while(!isGeneAllOffspring()){
				matingPopulation = selection(population);
				offspringPopulation = reproduction(matingPopulation);
//				offspringPopulation = evaluatePopulation(offspringPopulation);
				replacement(offspringPopulation);
			}
			updateProcess();
			
		}
		
	}
	protected abstract void updateProcess() ;
	protected abstract void replacement(List<S> offspringPopulation);
	protected abstract List<S> reproduction(List<S> matingPopulation);
	protected abstract List<S> selection(List<S> population2) ;
	protected abstract boolean isGeneAllOffspring();
	protected abstract boolean isStopConditionReached();
	protected abstract void initProcess();
	protected abstract List<S> evaluatePopulation(List<S> population2);
	protected abstract List<S> createInitialPopulation();

}
