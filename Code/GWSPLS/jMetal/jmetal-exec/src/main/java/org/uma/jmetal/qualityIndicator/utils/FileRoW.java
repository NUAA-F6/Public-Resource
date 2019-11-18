package org.uma.jmetal.qualityIndicator.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;

public class FileRoW<S extends Solution<?>> {
	
	private Problem<S> problem;
	
	public FileRoW(Problem<S> problem){
		this.problem = problem;
	}
	
	/**
	 * 从文件中读取size个解并返回
	 * @param path
	 * @param size
	 * @return
	 */
	public List<S> getPopulationFromFile(String path,int size) {

		List<S> population = new ArrayList<S>();

		try {
			// Open the file
			FileInputStream fis = new FileInputStream(path);
			InputStreamReader isr = new InputStreamReader(fis);
			BufferedReader br = new BufferedReader(isr);

//			int numberOfObjectives = 0;
			String aux = br.readLine();
			int i = 0;
			while (aux != null&&i<size) {
				StringTokenizer st = new StringTokenizer(aux);
				int j = 0;
//				numberOfObjectives = st.countTokens();
				S solution = this.problem.createSolution();
				while (st.hasMoreTokens()) {
					double value = (new Double(st.nextToken())).doubleValue();
					solution.setObjective(j, value);
					j++;
				}
				population.add(i, solution);
				aux = br.readLine();
				i++;
			}
			br.close();
		} catch (Exception e) {
			System.out
					.println("FileRoW: Failed when reading for file: "
							+ "/" + path);
			e.printStackTrace();
		}

		return population;
	}
}
