package org.uma.jmetal.runner.multiobjective;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.problem.multiobjective.GridManyObjectiveTSP;
import org.uma.jmetal.solution.GridPermutationSolution;
import org.uma.jmetal.util.AlgorithmRunner;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.fileoutput.SolutionListOutput;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;

import com.nuaa.shr.pls.TwoPhGridParetoLocalSearch;

public class TwoPhGridParetoLocalSearchRunner {
	public static void main(String[] args) throws IOException {
		String[] tspPaths = { "/tspInstances/kroA100.tsp", "/tspInstances/kroB100.tsp"};

		GridManyObjectiveTSP problem = new GridManyObjectiveTSP(tspPaths);
		Algorithm<List<GridPermutationSolution<Integer>>> algorithm = new TwoPhGridParetoLocalSearch(problem, 200, 300,
				300,20);
		AlgorithmRunner runner = new AlgorithmRunner.Executor(algorithm).execute();
		List<GridPermutationSolution<Integer>> population = algorithm.getResult();
		long computingTime = runner.getComputingTime();

		new SolutionListOutput(population).setSeparator("\t")
				.setVarFileOutputContext(new DefaultFileOutputContext(
						"results/2pgridpls" + problem.getNumberOfObjectives() + "D-VAR.tsv"))
				.setFunFileOutputContext(new DefaultFileOutputContext(
						"results/2pgridpls" + problem.getNumberOfObjectives() + "D-FUN-dominate.tsv"))
				.print();

		FileOutputStream out = new FileOutputStream(new File("coordinate.tsv"));
		for (GridPermutationSolution<Integer> solution : population) {
			for (int i = 0; i < solution.getNumberOfObjectives(); i++) {
				out.write((solution.getGridCoordinate(i) + " ").getBytes());
			}
			out.write("\n".getBytes());
		}
		out.close();

		JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");
		JMetalLogger.logger.info("Objectives values have been written to file FUN.tsv");
		JMetalLogger.logger.info("Variables values have been written to file VAR.tsv");
	}

}
