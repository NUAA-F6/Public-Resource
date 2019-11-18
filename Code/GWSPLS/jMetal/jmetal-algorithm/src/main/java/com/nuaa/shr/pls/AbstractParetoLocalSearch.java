package com.nuaa.shr.pls;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.comparator.DominanceComparator;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;

import com.nuaa.shr.pls.utils.PlsUtils;

@SuppressWarnings("serial")
public abstract class AbstractParetoLocalSearch<S extends Solution<?>> implements Algorithm<List<S>> {
	
	public enum FunctionType {TCHE, PBI, AGG};
	protected int iteration;
	protected int maxIteration;
	protected int populationSize;
	protected final SolutionListEvaluator<S> evaluator;

	protected int evaluations;
	protected Problem<S> problem;
	protected PlsUtils<S> plsUtils;
	protected List<S> workPopulation;
	protected List<S> externalPopulation;
	protected int[][] candidateEdge;// 候选集邻接矩阵，1代表在候选集中，0代表不在候选集中

    
	protected double[] idealPoint;;

	protected double[][] lambda;
	protected FunctionType functionType = FunctionType.TCHE;
	
	public AbstractParetoLocalSearch(Problem<S> problem, int maxIteration, int populationSize) {
		this.problem = problem;
		idealPoint = new double[problem.getNumberOfObjectives()];
		this.maxIteration = maxIteration;
		this.populationSize = populationSize;
		int numberOfVariables = problem.getNumberOfVariables();
		candidateEdge = new int[numberOfVariables][numberOfVariables];
		plsUtils = new PlsUtils<>();
		evaluator = new SequentialSolutionListEvaluator<S>();
		lambda = new double[populationSize][problem.getNumberOfObjectives()];
		externalPopulation = new ArrayList<>();
	}
	
	
	protected double[] getDiff(S solution) {
		double[] vector = new double[solution.getNumberOfObjectives()];
		for(int i = 0;i<solution.getNumberOfObjectives();i++){
			vector[i] = solution.getObjective(i)-idealPoint[i];
		}
		return vector;
	}
	

	/**
	 * Initialize weight vectors
	 */
	protected void initializeUniformWeight() {
		String dataDirectory = "MOEAD_Weights";
		String dataFileName;
		dataFileName = "W" + problem.getNumberOfObjectives() + "D_" + populationSize + ".dat";

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

	@Override
	public List<S> getResult() {
		return externalPopulation;
	}

	/**
	 * 添加到外部集中
	 * 
	 * @param neighborSolution
	 * @return
	 */
	protected boolean addToExternalPopulation(S neighborSolution) {
		if (!plsUtils.isDominatedByList(neighborSolution, externalPopulation)
				&& !plsUtils.isEqualsToList(neighborSolution, externalPopulation)) {
			externalPopulation.add(neighborSolution);
			reduceDominatedInList(neighborSolution, externalPopulation);
			return true;
		} else
			return false;
	}

	/**
	 * 删除population所有被neighborSolution支配的解
	 * 
	 * @param neighborSolution
	 * @param population
	 */
	protected void reduceDominatedInList(S neighborSolution, List<S> population) {
		for (int i = population.size() - 1; i >= 0; i--) {
			S comparaSolution = population.get(i);
			DominanceComparator<S> comparator = new DominanceComparator<>();
			if (comparator.compare(neighborSolution, comparaSolution) == -1) {
				population.remove(i);
			}
		}
	}

	/**
	 * 更新边的候选集
	 */
	protected void updateCandidateEdges() {
		clearCandidateEdges();// 先清除候选边集

		// 将所有在外部集中的边加入到候选边集
		for (int i = 0; i < workPopulation.size(); i++) {
			S solution = workPopulation.get(i);
			for (int j = 0; j < solution.getNumberOfVariables() - 1; j++) {
				int frontNode = (Integer) solution.getVariableValue(j);
				int nextNode = (Integer) solution.getVariableValue(j + 1);
				candidateEdge[frontNode][nextNode] = 1;
				candidateEdge[nextNode][frontNode] = 1;
			}
		}
	}

	private void clearCandidateEdges() {
		for (int i = 0; i < problem.getNumberOfVariables(); i++) {
			for (int j = 0; j < problem.getNumberOfVariables(); j++) {
				candidateEdge[i][j] = 0;
			}
		}
	}

	public abstract S getANeighborSolution(S solution, int startIndex,int endIndex);
	
	
	protected double[] adjustObjectives(S solution){
		double[] vector = new double[solution.getNumberOfObjectives()];
		for(int i = 0;i<vector.length;i++){
			vector[i] = solution.getObjective(i)-idealPoint[i];
		}
		return vector;
	}
	
	  double fitnessFunction(S individual, double[] lambda) throws JMetalException {
		    double fitness;

		    if (FunctionType.TCHE.equals(functionType)) {
		      double maxFun = -1.0e+30;

		      for (int n = 0; n < problem.getNumberOfObjectives(); n++) {
		        double diff = Math.abs(individual.getObjective(n) - idealPoint[n]);

		        double feval;
		        if (lambda[n] == 0) {
		          feval = 0.0001 * diff;
		        } else {
		          feval = diff * lambda[n];
		        }
		        if (feval > maxFun) {
		          maxFun = feval;
		        }
		      }

		      fitness = maxFun;
		    } else if (FunctionType.AGG.equals(functionType)) {
		      double sum = 0.0;
		      for (int n = 0; n < problem.getNumberOfObjectives(); n++) {
		        sum += (lambda[n]) * individual.getObjective(n);
		      }

		      fitness = sum;

		    } else if (FunctionType.PBI.equals(functionType)) {
		      double d1, d2, nl;
		      double theta = 5.0;

		      d1 = d2 = nl = 0.0;

		      for (int i = 0; i < problem.getNumberOfObjectives(); i++) {
		        d1 += (individual.getObjective(i) - idealPoint[i]) * lambda[i];
		        nl += Math.pow(lambda[i], 2.0);
		      }
		      nl = Math.sqrt(nl);
		      d1 = Math.abs(d1) / nl;

		      for (int i = 0; i < problem.getNumberOfObjectives(); i++) {
		        d2 += Math.pow((individual.getObjective(i) - idealPoint[i]) - d1 * (lambda[i] / nl), 2.0);
		      }
		      d2 = Math.sqrt(d2);

		      fitness = (d1 + theta * d2);
		    } else {
		      throw new JMetalException(" MOEAD.fitnessFunction: unknown type " + functionType);
		    }
		    return fitness;
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

}
