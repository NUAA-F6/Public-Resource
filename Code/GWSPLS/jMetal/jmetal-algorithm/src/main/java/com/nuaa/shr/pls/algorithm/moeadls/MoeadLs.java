package com.nuaa.shr.pls.algorithm.moeadls;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.uma.jmetal.algorithm.impl.AbstractLocalSearchSingle;
import org.uma.jmetal.algorithm.multiobjective.moead.util.MOEADUtils;
import org.uma.jmetal.operator.NeighborSearchOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.problem.multiobjective.ManyObjectiveKnapsack;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;

import com.nuaa.shr.pls.utils.InitializeMethod;

@SuppressWarnings("serial")
public class MoeadLs<S extends Solution<?>> extends AbstractLocalSearchSingle<S, List<S>> {

	private int maxIteration ;
	private int maxLocalSearchTime;
	
	private int iteration ;
	private int localSearchTime ;
	private boolean isUpdate ;
	private SolutionListEvaluator<S> evaluator;
	
	private double[] idealPoint ;	
	
	protected InitialSolutionMethod initialSolutionMethod ;
	
	protected enum InitialSolutionMethod{
		GREEDY_METHOD, RANDOM_METHOD
	}
	
	protected FunctionType functionType ;
	public enum FunctionType{
		WS, TCH, PBI
	}
	
	private double[][] lambda ;
	private int neighborSize = 20;
	private int[][] neighborhood ;
	
	public MoeadLs(Problem<S> problem,int maxPopulationSize, int maxLocalSearchTime, int maxIteration,
			SolutionListEvaluator<S> evaluator, NeighborSearchOperator<S> neighborSearchOperator,
			InitialSolutionMethod initialSolutionMethod, FunctionType functionType) {
		super(problem);
		setMaxPopulationSize(maxPopulationSize);
		this.maxIteration = maxIteration;
		this.maxLocalSearchTime = maxLocalSearchTime;
		this.evaluator = evaluator;
		this.neighborSearchOperator = neighborSearchOperator;
		this.initialSolutionMethod = initialSolutionMethod;
		this.functionType = functionType ;
		
	}

	@Override
	protected void initProcess() {
		iteration = 0;
		localSearchTime = 0;
		isUpdate = true;
		
		lambda = new double[getMaxPopulationSize()][getProblem().getNumberOfObjectives()];
		initializeUniformWeight();
		neighborhood = new int[getMaxPopulationSize()][neighborSize];
		initializeNeighborhood();
		
		idealPoint = new double[getProblem().getNumberOfObjectives()];
		initialIdealPoint();
		
		neighborSearchOperator.setCandidate(getPopulation());
		neighborSearchOperator.setComparator(null);//不用支配比较
		
		this.currentIterationSearchPos = 0 ;
	}
	
	private void initialIdealPoint() {
		for(int i = 0;i<idealPoint.length;i++){
			idealPoint[i] = Double.MAX_VALUE ;
		}
		for(S solution : getPopulation()){
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
	protected void updateProcess() {
		this.currentIterationSearchPos = 0;
		neighborSearchOperator.setCandidate(getPopulation());
		
		iteration++;
		
		JMetalLogger.logger.info(iteration + " iteration...");	
		JMetalLogger.logger.info("population size: "+getPopulation().size());
	}

	@Override
	protected void replacement(List<S> offspringPopulation) {
		int currentPos = currentIterationSearchPos - 1;
		int[] neighborIndexs = neighborhood[currentPos] ;
		for(S solution : offspringPopulation){
			updateIdealPoint(solution);
			for(int i = 0;i<neighborIndexs.length;i++){
				int neighborIndex = neighborIndexs[i];
				double function1 = function(solution,lambda[neighborIndex]) ;
				double function2 = function(getPopulation().get(neighborIndex),lambda[neighborIndex]);
				if(function1 < function2){
					getPopulation().set(neighborIndex, solution);
					isUpdate = true;
				}
					
			}
		}
		localSearchTime ++ ;
	}

	private double function(S s, double[] vector) {
		double fitness;

		if (FunctionType.TCH.equals(functionType)) {
			double maxFun = -1.0e+30;

			for (int n = 0; n < getProblem().getNumberOfObjectives(); n++) {
				double diff = Math.abs(s.getObjective(n) - idealPoint[n]);

				double feval;
				if (vector[n] == 0) {
					feval = diff / 0.0001;
				} else {
					feval = diff / vector[n];
				}
				if (feval > maxFun) {
					maxFun = feval;
				}
			}

			fitness = maxFun;
		} else if (FunctionType.WS.equals(functionType)) {
			double sum = 0.0;
			for (int n = 0; n < getProblem().getNumberOfObjectives(); n++) {
				sum += (vector[n]) * s.getObjective(n);
			}

			fitness = sum;

		} else if (FunctionType.PBI.equals(functionType)) {
			double d1, d2, nl;
			double theta = 5.0;

			d1 = d2 = nl = 0.0;

			for (int i = 0; i < getProblem().getNumberOfObjectives(); i++) {
				d1 += (s.getObjective(i) - idealPoint[i]) * vector[i];
				nl += Math.pow(vector[i], 2.0);
			}
			nl = Math.sqrt(nl);
			d1 = Math.abs(d1) / nl;

			for (int i = 0; i < getProblem().getNumberOfObjectives(); i++) {
				d2 += Math.pow((s.getObjective(i) - idealPoint[i]) - d1 * (vector[i] / nl), 2.0);
			}
			d2 = Math.sqrt(d2);

			fitness = (d1 + theta * d2);
		} else {
			throw new JMetalException(" MOEAD.fitnessFunction: unknown type " + functionType);
		}
		return fitness;
	}

	@Override
	protected boolean isStopConditionReached() {
		boolean flag = iteration > maxIteration || 
				localSearchTime > maxLocalSearchTime ||
				!isUpdate;
		isUpdate = false ;
		return flag;
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
	    }
	    return population;
	}
	
	@Override
	public List<S> getResult() {
		return getPopulation() ;
	}

	@Override
	public String getName() {
		return "moead";
	}

	@Override
	public String getDescription() {
		return "MOEAD based local search";
	}
	
	private void initializeUniformWeight() {
		String dataDirectory = "MOEAD_Weights";
		String dataFileName;
		dataFileName = "W" + getProblem().getNumberOfObjectives() + "D_" + getMaxPopulationSize() + ".dat";

		try {
			InputStream in = getClass().getResourceAsStream("/" + dataDirectory + "/" + dataFileName);
			InputStreamReader isr = new InputStreamReader(in);
			BufferedReader br = new BufferedReader(isr);

			int i = 0;
			int j = 0;
			String aux = br.readLine();
			while (aux != null) {
				StringTokenizer st = new StringTokenizer(aux);
				j = 0;
				while (st.hasMoreTokens()) {
					double value = new Double(st.nextToken());
					lambda[i][j] = value;
					j++;
				}
				aux = br.readLine();
				i++;
			}
			br.close();
		} catch (Exception e) {
			throw new JMetalException(
					"initializeUniformWeight: failed when reading for file: " + dataDirectory + "/" + dataFileName, e);
		}
	}
	
	private void initializeNeighborhood() {
		double[] x = new double[getMaxPopulationSize()];
		int[] idx = new int[getMaxPopulationSize()];

		for (int i = 0; i < getMaxPopulationSize(); i++) {
			// calculate the distances based on weight vectors
			for (int j = 0; j < getMaxPopulationSize(); j++) {
				x[j] = MOEADUtils.distVector(lambda[i], lambda[j]);
				idx[j] = j;
			}

			// find 'niche' nearest neighboring subproblems
			MOEADUtils.minFastSort(x, idx, getMaxPopulationSize(), neighborSize);

			System.arraycopy(idx, 0, neighborhood[i], 0, neighborSize);
		}
	}
}
