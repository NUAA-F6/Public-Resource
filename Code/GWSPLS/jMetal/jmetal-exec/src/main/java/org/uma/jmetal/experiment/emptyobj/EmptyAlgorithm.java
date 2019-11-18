package org.uma.jmetal.experiment.emptyobj;

import java.util.List;

import org.uma.jmetal.algorithm.impl.AbstractGeneticAlgorithm;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;

@SuppressWarnings("serial")
public class EmptyAlgorithm <S extends Solution<?>> extends AbstractGeneticAlgorithm<S, List<S>>{

	private String name;
	private int runId;
	private String baseProcedureDirectory;
	
	public String getBaseProcedureDirectory() {
		return baseProcedureDirectory;
	}

	public void setBaseProcedureDirectory(String baseProcedureDirectory) {
		this.baseProcedureDirectory = baseProcedureDirectory;
	}

	public EmptyAlgorithm(Problem<S> problem,String name) {
		super(problem);
		this.name = name;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public int getRunId(){
		return runId;
	}
	public void setRunId(int runId){
		this.runId = runId;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	protected void initProgress() {
	}

	@Override
	protected void updateProgress() {
	}

	@Override
	protected boolean isStoppingConditionReached() {
		return false;
	}

	@Override
	protected List<S> evaluatePopulation(List<S> population) {
		return null;
	}

	@Override
	protected List<S> replacement(List<S> population, List<S> offspringPopulation) {
		return null;
	}

	@Override
	public List<S> getResult() {
		return null;
	}
	
}
