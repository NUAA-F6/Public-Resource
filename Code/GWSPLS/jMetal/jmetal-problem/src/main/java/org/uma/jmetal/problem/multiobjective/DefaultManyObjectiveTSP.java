package org.uma.jmetal.problem.multiobjective;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.List;

import org.uma.jmetal.problem.impl.AbstractIntegerPermutationProblem;
import org.uma.jmetal.solution.PermutationSolution;
import org.uma.jmetal.util.JMetalException;

@SuppressWarnings("serial")
public class DefaultManyObjectiveTSP extends AbstractIntegerPermutationProblem {
	protected int numberOfCities;
	protected List<double[][]> objectiveMatrics = new ArrayList<double[][]>();

	public DefaultManyObjectiveTSP(String[] tspPaths) throws IOException {
		for (int i = 0; i < tspPaths.length; i++) {
			double[][] objectiveMatrix = readProblem(tspPaths[i]);
			objectiveMatrics.add(objectiveMatrix);
		}
	    setNumberOfVariables(numberOfCities);
	    setNumberOfObjectives(tspPaths.length);
	    setName("ManyObjectiveTSP");
	}

	public List<Double> getDistance(int firstNode, int nextNode){
		List<Double> objectiveValue = new ArrayList<>();
		for(int i = 0;i<getNumberOfObjectives();i++){
			objectiveValue.add(objectiveMatrics.get(i)[firstNode][nextNode]);
		}
		return objectiveValue;
	}
	
	
	@Override
	public int getPermutationLength() {
		return numberOfCities ;
	}

	@Override
	public void evaluate(PermutationSolution<Integer> solution) {
	    double[] fitnesses = new double[getNumberOfObjectives()] ;
	    for(int i = 0;i<fitnesses.length;i++){
	    	fitnesses[i] = 0.0;
	    }
	    
	    for (int i = 0; i < (numberOfCities - 1); i++) {
	      int x ;
	      int y ;

	      x = solution.getVariableValue(i) ;
	      y = solution.getVariableValue(i+1) ;

	      for(int j = 0;j<getNumberOfObjectives();j++){ 
	    	 double[][] objectiveMatrix = objectiveMatrics.get(j);
	    	 fitnesses[j] += objectiveMatrix[x][y];
	      }
	    }
	    int firstCity ;
	    int lastCity  ;

	    firstCity = solution.getVariableValue(0) ;
	    lastCity = solution.getVariableValue(numberOfCities - 1) ;
	    for(int j = 0;j<getNumberOfObjectives();j++){
	    	double[][] objectiveMatrix = objectiveMatrics.get(j);
	    	fitnesses[j] += objectiveMatrix[firstCity][lastCity];
	    	solution.setObjective(j, fitnesses[j]);
	    }

	}
	
	public List<double[][]> getDistanceMatrics(){
		return objectiveMatrics;
	}
	
	private double[][] readProblem(String file) throws IOException {
		double[][] matrix = null;

		InputStream in = getClass().getResourceAsStream(file);
		InputStreamReader isr = new InputStreamReader(in);
		BufferedReader br = new BufferedReader(isr);

		StreamTokenizer token = new StreamTokenizer(br);
		try {
			boolean found;
			found = false;

			token.nextToken();
			while (!found) {
				if ((token.sval != null) && ((token.sval.compareTo("DIMENSION") == 0)))
					found = true;
				else
					token.nextToken();
			}

			token.nextToken();
			token.nextToken();

			numberOfCities = (int) token.nval;

			matrix = new double[numberOfCities][numberOfCities];

			// Find the string SECTION
			found = false;
			token.nextToken();
			while (!found) {
				if ((token.sval != null) && ((token.sval.compareTo("SECTION") == 0)))
					found = true;
				else
					token.nextToken();
			}

			double[] c = new double[2 * numberOfCities];

			for (int i = 0; i < numberOfCities; i++) {
				token.nextToken();
				int j = (int) token.nval;

				token.nextToken();
				c[2 * (j - 1)] = token.nval;
				token.nextToken();
				c[2 * (j - 1) + 1] = token.nval;
			} // for

			double dist;
			for (int k = 0; k < numberOfCities; k++) {
				matrix[k][k] = 0;
				for (int j = k + 1; j < numberOfCities; j++) {
					dist = Math.sqrt(Math.pow((c[k * 2] - c[j * 2]), 2.0) + Math.pow((c[k * 2 + 1] - c[j * 2 + 1]), 2));
					dist = (int) (dist + .5);
					matrix[k][j] = dist;
					matrix[j][k] = dist;
				}
			}
		} catch (Exception e) {
			new JMetalException("TSP.readProblem(): error when reading data file " + e);
		}
		return matrix;
	}
	
	@Override
	public PermutationSolution<Integer> createSolution() {
		// TODO Auto-generated method stub
		return super.createSolution();
	}
}
