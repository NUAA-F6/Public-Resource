package org.uma.jmetal.runner.multiobjective.pls;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.problem.multiobjective.GridManyObjectiveKnapsack;
import org.uma.jmetal.solution.GridPermutationSolution;
import org.uma.jmetal.util.AlgorithmRunner;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.fileoutput.SolutionListOutput;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;

import com.nuaa.shr.pls.algorithm.KnapsackGridWeakDominatePls;
import com.nuaa.shr.pls.algorithm.KnapsackMoead;
import com.nuaa.shr.pls.algorithm.abst.MoeadParetoLocalSearch.FunctionType;

public class KnapsackPlsRunner {
	public static void main(String[] args) {

		String baseDirectory = "results/md/";
		int[] itemNumber = {750 };
		int[] dimension = { 4, 5, 6 };

//		FunctionType[] functionType = { FunctionType.AGG, FunctionType.TCHE, FunctionType.PBI };
//		for (int dimensionIdx = 0; dimensionIdx < dimension.length; dimensionIdx++) {
			// int gridNumber = (dimension[dimensionIdx] == 2) ? 200 : 14;
//			for (int function = 0; function < 3; function++) {
				for (int itemIdx = 0; itemIdx < 3; itemIdx++) {
					int dimensionIdx = 0;
					GridManyObjectiveKnapsack problem = new GridManyObjectiveKnapsack(
							"/knapsack/profit" + itemNumber[itemIdx] + "_" + dimension[dimensionIdx] + ".txt",
							"/knapsack/weight" + itemNumber[itemIdx] + "_" + dimension[dimensionIdx] + ".txt",
							"/knapsack/capacity" + itemNumber[itemIdx] + "_" + dimension[dimensionIdx] + ".txt",
							itemNumber[itemIdx], dimension[dimensionIdx]);
					problem.setName("knapsack" + itemNumber[itemIdx] + "_" + dimension[dimensionIdx]);
					
					double neighborRatio = (itemNumber[itemIdx] == 750) ? 0.3:0.5;
					int run = 30;
					for (int i = 0; i < run; i++) {
						Algorithm<List<GridPermutationSolution<Integer>>> algorithm = new KnapsackGridWeakDominatePls(problem, 100,
								364,7);
						((KnapsackGridWeakDominatePls) algorithm).setRunId(i);
						((KnapsackGridWeakDominatePls) algorithm).setBaseProcedureDirectory(baseDirectory + "/procedure");
						((KnapsackGridWeakDominatePls) algorithm).setNeighborRatio(neighborRatio);

						JMetalLogger.logger
								.info(algorithm.getName() + " on " + problem.getName() + " run " + i + " starting...");

						AlgorithmRunner runner = new AlgorithmRunner.Executor(algorithm).execute();
						long computingTime = runner.getComputingTime();
						JMetalLogger.logger.info("Total run time: " + computingTime + "ms");

						List<GridPermutationSolution<Integer>> population = algorithm.getResult();
						String path = baseDirectory + "/final/" + algorithm.getName() + "/" + problem.getName();
						if (i == 0) {
							File folder = new File(path);
							if (!folder.exists() || !folder.isDirectory()) {
								folder.mkdirs();
							}
						}
						String funFile = path + "/FUN" + i + ".tsv";
						String varFile = path + "/VAR" + i + ".tsv";
						new SolutionListOutput(population).setSeparator("\t")
								.setVarFileOutputContext(new DefaultFileOutputContext(varFile))
								.setFunFileOutputContext(new DefaultFileOutputContext(funFile)).print();
						JMetalLogger.logger.info("Objectives values have been written to file " + funFile);
						JMetalLogger.logger.info("Variables values have been written to file " + varFile);

						int searchCount = ((KnapsackGridWeakDominatePls) algorithm).getSearchCount();
						JMetalLogger.logger.info("Search count is: " + searchCount);

						int popSize = population.size();
						JMetalLogger.logger.info("Pop size is: " + popSize);

						FileWriter searchCountOs;
						FileWriter popSizeOs;
						try {
							searchCountOs = new FileWriter(path + "/SEARCH_COUNT", true);
							searchCountOs.write("" + searchCount + "\n");
							popSizeOs = new FileWriter(path + "/POP_SIZE", true);
							popSizeOs.write("" + popSize + "\n");
							searchCountOs.close();
							popSizeOs.close();
						} catch (Exception e) {
							throw new JMetalException("Write to file error!");
						}
					}
				}
			}
//		}
//	}
}
