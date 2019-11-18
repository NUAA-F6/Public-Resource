package com.nuaa.shr.pls.algorithm.gplsd;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.uma.jmetal.algorithm.impl.AbstractLocalSearchSingle;
import org.uma.jmetal.operator.NeighborSearchOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.problem.multiobjective.ManyObjectiveKnapsack;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.comparator.GwsComparator;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.fileoutput.SolutionListOutput;

import com.nuaa.shr.pls.utils.InitializeMethod;
import com.nuaa.shr.pls.utils.SolutionCompareUtils;

@SuppressWarnings("serial")
public class GPLSD <S extends Solution<?>> extends AbstractLocalSearchSingle<S, List<S>>{

	private final int maxIteration ;
	private final int maxLocalSearchTime ;
	private final int numberOfGrid;
	
	private int iteration ;
	private int localSearchTime ;
	private boolean isUpdate ;
	private SolutionListEvaluator<S> evaluator;
	
	private List<S> externalPopulation ;
	private List<S> tempPopulation ;
	private double[] idealPoint ;
	private double[] nadPoint;
	private GwsComparator<S> gwsComparator ;
	
	protected InitialSolutionMethod initialSolutionMethod ;
	
	public enum InitialSolutionMethod{
		GREEDY_METHOD, RANDOM_METHOD, USER_DEFINR_METHOD
	}
	
	
	private SolutionCompareUtils<S> solutionUtils = new SolutionCompareUtils<>();
	
	public GPLSD(Problem<S> problem,int maxPopulationSize, int maxLocalSearchTime, int maxIteration,
			int numberOfGrid, SolutionListEvaluator<S> evaluator, 
			NeighborSearchOperator<S> neighborSearchOperator,
			InitialSolutionMethod initialSolutionMethod) {
		super(problem);
		setMaxPopulationSize(maxPopulationSize);
		this.maxIteration = maxIteration;
		this.maxLocalSearchTime = maxLocalSearchTime;
		this.numberOfGrid = numberOfGrid ;
		this.evaluator = evaluator;
		this.neighborSearchOperator = neighborSearchOperator;
		this.initialSolutionMethod = initialSolutionMethod;
	}

	@Override
	public List<S> getResult() {
		return SolutionListUtils.getNondominatedSolutions(externalPopulation);
	}

	@Override
	public String getName() {
		return "GPLSD";
	}

	@Override
	public String getDescription() {
		return "Grid Pareto Local Search Based on Decopsition";
	}

	@Override
	protected void updateProcess() {
		getPopulation().clear();
		getPopulation().addAll(tempPopulation);
		tempPopulation.clear();
		
		this.currentIterationSearchPos = 0;
		
		initialIdealPoint();
		initialNadPoint();
		gwsComparator.setIdealAndNad(idealPoint, nadPoint);
		
		reduceInTheSameGrid(externalPopulation);
		reduceInTheSameGrid(getPopulation());
		neighborSearchOperator.setCandidate(getPopulation());
		
		iteration++;
		try {
			new SolutionListOutput(externalPopulation).printObjectivesToFile("results/media/FUN" + iteration + ".tsv");
		} catch (IOException e) {
			e.printStackTrace();
		}
		JMetalLogger.logger.info(iteration + " iteration...");	
		JMetalLogger.logger.info("archive size: "+externalPopulation.size()+", population size: "+getPopulation().size());
	}

	private void reduceInTheSameGrid(List<S> population) {
		List<S> removeList = new ArrayList<>();
		for(int i = 0;i<population.size();i++){
			for(int j = 0;j<population.size();j++){
				int flag = gwsComparator.compare(population.get(i), population.get(j));
				if(flag>0){
					removeList.add(population.get(i));
					break;
				}
			}
		}
		population.removeAll(removeList);
	}

	@Override
	protected void replacement(List<S> offspringPopulation) {
		for(S solution : offspringPopulation){
			if(addToArchive(solution)){
				updateTempPopulation(solution);
				isUpdate = true;
			}
		}
		localSearchTime ++;
	}

	private void updateTempPopulation(S solution) {
		Iterator<S> iterator = tempPopulation.iterator();
		while(iterator.hasNext()){
			S oldSolution = iterator.next();
			int flag = gwsComparator.compare(solution, oldSolution);
			if(flag < 0)
				iterator.remove();
		}
		tempPopulation.add(solution);
	}

	private boolean addToArchive(S solution) {
		Iterator<S> iterator = externalPopulation.iterator();
		while(iterator.hasNext()){
			S oldSolution = iterator.next();
			int flag = gwsComparator.compare(solution, oldSolution);
			if (flag < 0) {
				iterator.remove();
			} else if (flag > 0) {
				return false;
			} else if (solutionUtils.isObjectiveEqual(solution, oldSolution)) {
				return false;
			}
		}
		return externalPopulation.add(solution);
	}

	@Override
	protected boolean isStopConditionReached() {
		boolean flag =  iteration > maxIteration || 
				localSearchTime > maxLocalSearchTime || 
				!isUpdate;
		isUpdate = false ;
		return flag ;
	}

	@Override
	protected void initProcess() {
		iteration = 0;
		localSearchTime = 0;
		isUpdate = true ;
		
		//initialize 3 population: WP,TP,and EP
		List<S> nonDomiPop = SolutionListUtils.getNondominatedSolutions(getPopulation());
		getPopulation().clear();
		getPopulation().addAll(nonDomiPop);
		
		externalPopulation = new ArrayList<S>();
		externalPopulation.addAll(SolutionListUtils.getNondominatedSolutions(getPopulation()));
		tempPopulation = new ArrayList<>();
		tempPopulation.addAll(getPopulation());
		
		//initialize ideal and nad point
		idealPoint = new double[getProblem().getNumberOfObjectives()];
		initialIdealPoint();
		
		nadPoint = new double[getProblem().getNumberOfObjectives()];
		initialNadPoint();
		
		//initialize comparator and operator
		gwsComparator = new GwsComparator<>(numberOfGrid);
		gwsComparator.setIdealAndNad(idealPoint, nadPoint);
		
		neighborSearchOperator.setCandidate(getPopulation());
		neighborSearchOperator.setComparator(gwsComparator);
		
		//initialize current search solution index
		this.currentIterationSearchPos = 0 ;
		
		
		
	}
	
	
	private void initialIdealPoint() {
		for(int i = 0;i<idealPoint.length;i++){
			idealPoint[i] = Double.MAX_VALUE ;
		}
		for(S solution : externalPopulation){
			updateIdealPoint(solution);
		}
	}

	private void initialNadPoint(){
		for(int i = 0;i<nadPoint.length;i++){
			nadPoint[i] = Double.NEGATIVE_INFINITY ;
		}
		for(S solution : externalPopulation){
			updateNadPoint(solution);
		}
	}
	
	private void updateNadPoint(S solution) {
		for(int i = 0;i<solution.getNumberOfObjectives();i++){
			if(solution.getObjective(i)>nadPoint[i])
				nadPoint[i] = solution.getObjective(i);
		}
	}

	private void updateIdealPoint(S solution) {
		for(int i = 0;i<solution.getNumberOfObjectives();i++){
			if(solution.getObjective(i)<idealPoint[i])
				idealPoint[i] = solution.getObjective(i);
		}
		
	}

	@Override
	protected List<S> evaluatePopulation(List<S> population) {
		if(population.size() > 0)//prevent the situation the task is null
			evaluator.evaluate(population, getProblem());
		return population;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected List<S> createInitialPopulation() {
	    List<S> population = new ArrayList<>(getMaxPopulationSize());
	    if(InitialSolutionMethod.RANDOM_METHOD.equals(initialSolutionMethod)){
		    for (int i = 0; i < getMaxPopulationSize(); i++) {
		      S newIndividual = getProblem().createSolution();
		      population.add(newIndividual);
		    }
	    }else if(InitialSolutionMethod.GREEDY_METHOD.equals(initialSolutionMethod)){
	    	double[][] weightVectors = new InitializeMethod().
	    			initializeUniformWeight(getProblem().getNumberOfObjectives(),getMaxPopulationSize());
		    for (int i = 0; i < weightVectors.length; i++) {
			      S newIndividual = (S) ((ManyObjectiveKnapsack)getProblem()).createSolution(weightVectors[i]);
			      population.add(newIndividual);
		    }
	    }else if(InitialSolutionMethod.USER_DEFINR_METHOD.equals(initialSolutionMethod)){
	    	population.addAll(userPopulation);
	    	userPopulation = null;
	    }
	    return population;
	}
	
	private List<S> userPopulation; 
	
	public void setUserPopulation(List<S> userPopulation){
		this.userPopulation = userPopulation ;
	}

}
