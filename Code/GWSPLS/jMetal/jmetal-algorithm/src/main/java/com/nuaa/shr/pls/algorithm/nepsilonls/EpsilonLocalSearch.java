package com.nuaa.shr.pls.algorithm.nepsilonls;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.uma.jmetal.algorithm.impl.AbstractLocalSearchSingle;
import org.uma.jmetal.operator.NeighborSearchOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.problem.multiobjective.ManyObjectiveKnapsack;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.comparator.DominanceComparator;
import org.uma.jmetal.util.comparator.EpsilonDominanceComparator;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.fileoutput.SolutionListOutput;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;

import com.nuaa.shr.pls.utils.InitializeMethod;
import com.nuaa.shr.pls.utils.SolutionCompareUtils;

@SuppressWarnings("serial")
public class EpsilonLocalSearch<S extends Solution<?>> extends AbstractLocalSearchSingle<S, List<S>> {

	private final int maxIteration ;
	private final int maxLocalSearchTime ;
	private final double epsilon;
	
	private int iteration ;
	private int localSearchTime ;
	private boolean isUpdate ;
	private SolutionListEvaluator<S> evaluator;
	
	private List<S> externalPopulation ;
	private List<S> tempPopulation ;
	private double[] idealPoint ;
	private EpsilonDominanceComparator<S> epsilonDominance ;
	
	private SolutionCompareUtils<S> solutionUtils = new SolutionCompareUtils<>();
	
	protected InitialSolutionMethod initialSolutionMethod ;
	
	public enum InitialSolutionMethod{
		GREEDY_METHOD, RANDOM_METHOD
	}
	
	public EpsilonLocalSearch(Problem<S> problem,int maxPopulationSize, int maxLocalSearchTime, int maxIteration,
			double epsilon, SolutionListEvaluator<S> evaluator, 
			NeighborSearchOperator<S> neighborSearchOperator,
			InitialSolutionMethod initialSolutionMethod) {
		super(problem);
		setMaxPopulationSize(maxPopulationSize);
		this.maxIteration = maxIteration;
		this.maxLocalSearchTime = maxLocalSearchTime;
		this.epsilon = epsilon ;
		this.evaluator = evaluator;
		this.neighborSearchOperator = neighborSearchOperator;
		epsilonDominance = new EpsilonDominanceComparator<>(epsilon);
		this.initialSolutionMethod = initialSolutionMethod;
	}

	@Override
	protected void replacement(List<S> offspringPopulation) {
		for(S solution : offspringPopulation){
			updateIdealPoint(solution);
			addToTempPopulation(solution);
			if(addToArchive(solution))
				isUpdate = true;
		} 
		
		localSearchTime ++;
		
		if(localSearchTime % 1000 == 0)
			printExternal(localSearchTime / 1000);
	}
	
	private boolean addToArchive(S solution) {
		 epsilonDominance.setIdealValue(idealPoint);
		 Iterator<S> iterator = externalPopulation.iterator();
		 while(iterator.hasNext()){
			 S oldSolution = iterator.next();
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
	private void addToTempPopulation(S solution) {
		DominanceComparator<S> dominanceComparator = new DominanceComparator<>();
		List<Integer> dominates = new ArrayList<Integer>();
		boolean dominated = false;
		boolean isEqual = false  ;
		for (int i = 0; i < tempPopulation.size(); i++) {
			int flag = dominanceComparator.compare(solution, 
					tempPopulation.get(i));

			if (flag < 0) {
				dominates.add(i);
			} else if (flag > 0) {
				dominated = true;
			}else if(solutionUtils.isObjectiveEqual(solution, tempPopulation.get(i))){
				isEqual = true;
			}
		}
		
		if(!dominates.isEmpty()){
			int index = dominates.get(new Random().nextInt(dominates.size())) ;
			tempPopulation.remove(index);
			tempPopulation.add(solution);
		}else if(!dominated && !isEqual){
			tempPopulation.remove(new Random().nextInt(tempPopulation.size()));
			tempPopulation.add(solution);
		}
		
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
		
		externalPopulation = new ArrayList<S>();
		externalPopulation.addAll(SolutionListUtils.getNondominatedSolutions(getPopulation()));
		tempPopulation = new ArrayList<>();
		tempPopulation.addAll(getPopulation());
		
		printExternal(0);
		
		idealPoint = new double[getProblem().getNumberOfObjectives()];
		InitialIdealPoint();
		epsilonDominance = new EpsilonDominanceComparator<>(epsilon);
		
		neighborSearchOperator.setCandidate(getPopulation());
		this.currentIterationSearchPos = 0 ;
	}

	private void InitialIdealPoint() {
		for(int i = 0;i<idealPoint.length;i++){
			idealPoint[i] = Double.MAX_VALUE ;
		}
		for(S solution : externalPopulation){
			updateIdealPoint(solution);
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
	
	@Override
	protected void updateProcess() {
		
		getPopulation().clear();
		getPopulation().addAll(tempPopulation);
		
		neighborSearchOperator.setCandidate(getPopulation());
		
		this.currentIterationSearchPos = 0;
		iteration++;
		
//		try {
//			new SolutionListOutput(externalPopulation).printObjectivesToFile("results/media/FUN" + iteration + ".tsv");
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		JMetalLogger.logger.info(iteration + " iteration...");		
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
	    }
	    return population;
	}
	
	@Override
	public List<S> getResult() {
		return externalPopulation ;
	}

	@Override
	public String getName() {
		return "EpsilonMOEA";
	}

	@Override
	public String getDescription() {
		return "Epsilon-dominance MOEA";
	}
	
	//for experiments
	private int runId = 0;
	private String baseProcedureDirectory = "results";
	
	public void setRunId(int run){
		this.runId = run;
	}
	public void setBaseProcedureDirectory(String baseDirectory){
		this.baseProcedureDirectory = baseDirectory ;
	}
	
	private void printExternal(int position) {
		StringBuffer folderPath = new StringBuffer(this.baseProcedureDirectory)
				.append("/")
				.append(this.getName())
				.append("/")
				.append(getProblem().getName())
				.append("/RUN_")
				.append(this.runId);
		if(position == 0){
			File folder = new File(folderPath.toString());
			if(!folder.exists()||!folder.isDirectory()){
				folder.mkdirs();
			}
		}
		String filePath = 
				folderPath
				.append("/FUN")
				.append(position)
				.append(".tsv").toString();
		new SolutionListOutput(SolutionListUtils.getNondominatedSolutions(externalPopulation))
			.setSeparator("\t")
			.setFunFileOutputContext(new DefaultFileOutputContext(filePath))
			.print();
	}

	public int getSearchCount() {
		return localSearchTime ;
	}
}
