package org.uma.jmetal.problem.multiobjective;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.uma.jmetal.problem.impl.AbstractIntegerPermutationProblem;
import org.uma.jmetal.solution.PermutationSolution;

@SuppressWarnings("serial")
public class ManyObjectiveQAP extends AbstractIntegerPermutationProblem{

	protected int numberOfFacility ;
	protected Integer[][] locationMatrics ;
	protected List<Integer[][]> facilityMatrics ;
	
	public ManyObjectiveQAP(String locationPath, String[] facilityPaths, String name) {
		try {
			locationMatrics = getMatric(locationPath) ;
			numberOfFacility = locationMatrics.length ;
			facilityMatrics = new ArrayList<>();
			for(int i = 0;i<facilityPaths.length;i++){
				Integer[][] facilityMatric = getMatric(facilityPaths[i]);
				facilityMatrics.add(facilityMatric) ;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		setNumberOfObjectives(facilityPaths.length);
		setNumberOfVariables(numberOfFacility);
		setName(name);
	}


	private Integer[][] getMatric(String path) throws IOException, URISyntaxException {
		List<String> allLines = Files.readAllLines(Paths.get(getClass().getResource(path).toURI())) ;
		Integer[][] matrix = new Integer[allLines.size()][allLines.size()];
		for(int i = 0;i<matrix.length;i++){
			String aLine = allLines.get(i).trim();
			String[] items = aLine.split("\\s+");
			for(int j = 0;j<items.length;j++){
				matrix[i][j] = Integer.parseInt(items[j]) ;
			}
		}
		return matrix;
	}

	@Override
	public int getPermutationLength() {
		return numberOfFacility;
	}

	@Override
	public void evaluate(PermutationSolution<Integer> solution) {
		for(int i = 0;i<getNumberOfObjectives();i++){
			Integer[][] facilityMatrix = facilityMatrics.get(i);
			int fitness = 0;
			for(int idx = 0;idx<solution.getNumberOfVariables();idx++){
				for(int jdx = 0;jdx<solution.getNumberOfVariables();jdx++){
					fitness += 
							locationMatrics[idx][jdx] * facilityMatrix[solution.getVariableValue(idx)][solution.getVariableValue(jdx)] ;
				}
			}
			
			solution.setObjective(i, fitness);
		}
	}

}
