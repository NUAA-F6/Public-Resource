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

import com.nuaa.shr.pls.CdgGridParetoLocalSearch;
import com.nuaa.shr.pls.CdgParetoLocalSearch;

public class GridPaetoLocalSearchRunner {

	public static void main(String[] args) throws IOException {
		String[] tspPaths = { "/tspInstances/kroA100.tsp", "/tspInstances/kroB100.tsp"};
		// DefaultManyObjectiveTSP problem = new
		// DefaultManyObjectiveTSP(tspPaths);
		// Algorithm<List<PermutationSolution<Integer>>> algorithm = new
		// LearningParetoLocalSearch(problem, 200, 200);

		GridManyObjectiveTSP problem = new GridManyObjectiveTSP(tspPaths);

//		int maxRun = 30;
//		for (int run = 0; run < maxRun; run++) {
		int run = 1;
			Algorithm<List<GridPermutationSolution<Integer>>> algorithm = new CdgGridParetoLocalSearch(problem, 300, 300,
					250);
			AlgorithmRunner runner = new AlgorithmRunner.Executor(algorithm).execute();
			List<GridPermutationSolution<Integer>> population = algorithm.getResult();
			long computingTime = runner.getComputingTime();

			new SolutionListOutput(population).setSeparator("\t")
					.setVarFileOutputContext(new DefaultFileOutputContext(
							"results/tsp-" + problem.getNumberOfObjectives() + "D-VAR"+run+".tsv"))
					.setFunFileOutputContext(new DefaultFileOutputContext(
							"results/tsp-" + problem.getNumberOfObjectives() + "D-FUN"+run+".tsv"))
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
//	}

}
