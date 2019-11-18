package com.nuaa.shr.pls.algorithm;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.problem.multiobjective.GridManyObjectiveKnapsack;
import org.uma.jmetal.solution.GridPermutationSolution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.comparator.DominanceComparator;

import com.nuaa.shr.pls.algorithm.abst.GrParetoLocalSearch;

@SuppressWarnings("serial")
public class KnapsackGridPls extends GrParetoLocalSearch{

	private double[][] weightVector;
	
	public KnapsackGridPls(Problem<GridPermutationSolution<Integer>> problem, int maxIteration, int populationSize,
			int numberOfGrid) {
		super(problem, maxIteration, populationSize, numberOfGrid);
		weightVector = new double[populationSize][problem.getNumberOfObjectives()];
	}

	@Override
	public String getName() {
		return "kanpsack-GrPLS";
	}

	@Override
	public String getDescription() {
		return getName();
	}

	@Override
	protected void updateSomething() {}

	@Override
	protected void searchNeighborhood(GridPermutationSolution<Integer> solution,
			List<GridPermutationSolution<Integer>> tempList) {
		List<Integer> unSelectedIndex = ((GridManyObjectiveKnapsack)problem).getWorstIndex(solution, 0.2);
		List<Integer> selectedBestIndex = ((GridManyObjectiveKnapsack)problem).getBestIndex(solution, 0.2);
		for(int i = 0;i<selectedBestIndex.size();i++){
			int aSelectedIndex = selectedBestIndex.get(i);
			for(int j = 0;j<unSelectedIndex.size();j++){
				int aUnSelectedIndex = unSelectedIndex.get(j);
				GridPermutationSolution<Integer> neighborSolution = getNeighborhood(solution, aSelectedIndex, aUnSelectedIndex);
				if (neighborSolution != null) {
					this.problem.evaluate(neighborSolution);
					updateIdealPoint(neighborSolution);
					setGridCoordinate(neighborSolution);
					DominanceComparator<GridPermutationSolution<Integer>> dmtComparator= new DominanceComparator<>();
					int dmtFlag = dmtComparator.compare(solution, neighborSolution);
					if(dmtFlag >= 0 ){
						if(addSolutionToExternal(neighborSolution)){
							reduceDominatedInList(neighborSolution, externalPopulation);
							updateIdealPoint(neighborSolution);
							updateNadirPoint(neighborSolution);
							
							addToTempPopulation(neighborSolution, tempList);
						}
					}
				}
			}
		}
		
	
	}
	
	public GridPermutationSolution<Integer> getNeighborhood(GridPermutationSolution<Integer> solution, int aSelectedIndex, int aUnSelectedIndex) {
//		GridPermutationSolution<Integer> neighborSolution = (GridPermutationSolution<Integer>) solution.copy();
		GridPermutationSolution<Integer> neighborSolution = this.problem.createSolution();
//		System.out.println(((GridManyObjectiveKnapsack)problem).isValide(neighborSolution));
//		System.out.println(neighborSolution.getVariableValue(aSelectedIndex));
//		System.out.println(neighborSolution.getVariableValue(aUnSelectedIndex));
		
		for(int i = 0;i<solution.getNumberOfVariables();i++){
			neighborSolution.setVariableValue(i, solution.getVariableValue(i));
		}
		
		neighborSolution.setVariableValue(aSelectedIndex, 0);
		neighborSolution.setVariableValue(aUnSelectedIndex, 1);
		if(((GridManyObjectiveKnapsack)problem).isValide(neighborSolution)){
			((GridManyObjectiveKnapsack)problem).greedyRepair(neighborSolution);
			return neighborSolution;
		}
		else return null;
	}
	
	@Override
	protected List<GridPermutationSolution<Integer>> createInitialPopulation() {
		
		initializeUniformWeight();
		List<GridPermutationSolution<Integer>> population = new ArrayList<>();
		for(int i = 0;i<populationSize;i++){
			GridPermutationSolution<Integer> solution = 
					((GridManyObjectiveKnapsack)this.problem).createSolution(weightVector[i]);
			population.add(solution);
		}
		return population;
	}
	
	
	/**
	 * 初始化解的时候用到
	 */
	private void initializeUniformWeight() {
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
					weightVector[i][j] = value;
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
}
