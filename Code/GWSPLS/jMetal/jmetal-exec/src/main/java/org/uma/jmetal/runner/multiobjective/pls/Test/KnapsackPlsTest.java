package org.uma.jmetal.runner.multiobjective.pls.Test;

import java.util.List;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.problem.multiobjective.GridManyObjectiveKnapsack;
import org.uma.jmetal.solution.GridPermutationSolution;
import org.uma.jmetal.util.AlgorithmRunner;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.fileoutput.SolutionListOutput;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;

import com.nuaa.shr.pls.algorithm.KnapGrWeakDomiRandom;
import com.nuaa.shr.pls.algorithm.KnapsackGridWeakDominatePls;
import com.nuaa.shr.pls.algorithm.KnapsackMoead;
import com.nuaa.shr.pls.algorithm.KnapsackMoeadRandom;

public class KnapsackPlsTest {
	public static void main(String[] args) {
		GridManyObjectiveKnapsack problem = new GridManyObjectiveKnapsack("/knapsack/profit250_7.txt",
				"/knapsack/weight250_7.txt", "/knapsack/capacity250_7.txt", 250, 7);

		int run = 30;
		for (int i = 0; i < 1; i++) {
			Algorithm<List<GridPermutationSolution<Integer>>> algorithm = 
					new KnapGrWeakDomiRandom(problem, 100, 1716, 4);
 
			AlgorithmRunner runner = new AlgorithmRunner.Executor(algorithm).execute();
			List<GridPermutationSolution<Integer>> population = algorithm.getResult();
			long computingTime = runner.getComputingTime();

			new SolutionListOutput(population).setSeparator("\t")
					.setVarFileOutputContext(
							new DefaultFileOutputContext("results/temp/VAR-7D-" + i + ".tsv"))
					.setFunFileOutputContext(
							new DefaultFileOutputContext("results/temp/FUN-7D-" + i + ".tsv"))
					.print();

			JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");
			JMetalLogger.logger.info("Objectives values have been written to file FUN.tsv");
			JMetalLogger.logger.info("Variables values have been written to file VAR.tsv");
		}
	}
}
