package com.nuaa.shr.pls.algorithm;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.problem.multiobjective.GridManyObjectiveKnapsack;
import org.uma.jmetal.solution.GridPermutationSolution;
import org.uma.jmetal.util.JMetalException;

import com.nuaa.shr.pls.algorithm.abst.GrWeakDominateParetoLocalSearch;
import com.nuaa.shr.pls.utils.GridComparator;
import com.nuaa.shr.pls.utils.GridUtils;

/**
 * 根据支配关系来选择解
 * @author sevn
 *
 */
@SuppressWarnings("serial")
public class KnapsackGridWeakDominatePls extends GrWeakDominateParetoLocalSearch{

	private double[][] weightVector;
	private double neighborRatio = 0.2;
	
	public KnapsackGridWeakDominatePls(Problem<GridPermutationSolution<Integer>> problem, int maxIteration,
			int populationSize, int division) {
		super(problem, maxIteration, populationSize, division);
		weightVector = new double[populationSize][problem.getNumberOfObjectives()];
	}

	@Override
	protected void updateSomething() {}

	
	@Override
	protected List<GridPermutationSolution<Integer>> createInitialPopulation() {
		initializeUniformWeight();
		
		List<GridPermutationSolution<Integer>> population = new ArrayList<>();
		for(int i = 0;i<populationSize;i++){
			GridPermutationSolution<Integer> solution = 
					((GridManyObjectiveKnapsack)this.problem).createSolution(weightVector[i]);
//			GridPermutationSolution<Integer> solution = ((GridManyObjectiveKnapsack)this.problem).createValidSolution();
//			((GridManyObjectiveKnapsack)this.problem).repair(solution);
			population.add(solution);
		}
		Collections.shuffle(population);
		return population;
	}
	
	@Override
	protected void searchNeighborhood(GridPermutationSolution<Integer> solution,
			List<GridPermutationSolution<Integer>> tempList) {
		List<Integer> unSelectedIndex = ((GridManyObjectiveKnapsack)problem).getWorstIndex(solution, neighborRatio);
		List<Integer> selectedBestIndex = ((GridManyObjectiveKnapsack)problem).getBestIndex(solution, neighborRatio);
		for(int i = 0;i<selectedBestIndex.size();i++){
			int aSelectedIndex = selectedBestIndex.get(i);
			for(int j = 0;j<unSelectedIndex.size();j++){
				int aUnSelectedIndex = unSelectedIndex.get(j);
				GridPermutationSolution<Integer> neighborSolution = getNeighborhood(solution, aSelectedIndex, aUnSelectedIndex);
				if (neighborSolution != null) {
					this.problem.evaluate(neighborSolution);
					updateIdealPoint(neighborSolution);
					setGridCoordinate(neighborSolution);
					GridComparator grDmtComparator= new GridComparator();
					int dmtFlag = grDmtComparator.compare(solution, neighborSolution);
					if(dmtFlag >= 0 ){
						if(addSolutionToExternal(neighborSolution)){
							GridUtils.reduceDominatedInList(neighborSolution, externalPopulation);
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
//		System.out.println(((GridManyObjectiveKnapsack)problem).isValide(neighborSolution));
		neighborSolution.setVariableValue(aSelectedIndex, 0);
		neighborSolution.setVariableValue(aUnSelectedIndex, 1);
		if(((GridManyObjectiveKnapsack)problem).isValide(neighborSolution)){
			((GridManyObjectiveKnapsack)problem).greedyRepair(neighborSolution);
			return neighborSolution;
		}
		else return null;
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
	
	public void setNeighborRatio(double neiborRatio){
		this.neighborRatio = neiborRatio;
	}

}
