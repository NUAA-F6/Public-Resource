package org.uma.jmetal.runner.multiobjective.pls.experience;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.operator.NeighborSearchOperator;
import org.uma.jmetal.problem.multiobjective.ManyObjectiveKnapsack;
import org.uma.jmetal.runner.multiobjective.pls.utils.OperatorFactory;
import org.uma.jmetal.runner.multiobjective.pls.utils.ProblemFactory;
import org.uma.jmetal.solution.PermutationSolution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.fileoutput.SolutionListOutput;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;

import com.nuaa.shr.pls.algorithm.nepsilonls.EpsilonLocalSearch;
import com.nuaa.shr.pls.algorithm.nepsilonls.EpsilonLocalSearch.InitialSolutionMethod;
import com.nuaa.shr.pls.algorithm.nepsilonls.EpsilonLocalSearchBuilder;

/**
 * 注意设为50!!!!!!!!!!!
 * 
 * @author sevn
 *
 */
public class EpsilonKnapsackExperiment3 {
	public static void main(String[] args) throws IOException {
		int[] itemNumbers = { 250, 500, 750 };
		String baseDirectory = "results";
		for (int i = 0; i < itemNumbers.length; i++) {
			for (int dimension = 2; dimension < 4; dimension++) {
				for (int run = 0; run < 30; run++) {

					double searchProbility = itemNumbers[i]==750?0.3:0.5;
					ManyObjectiveKnapsack problem = 
							(ManyObjectiveKnapsack) ProblemFactory.getKnapsack(itemNumbers[i], dimension);
					NeighborSearchOperator<PermutationSolution<Integer>> operator = 
							OperatorFactory.getOperator(searchProbility,problem);
					double epsilon = (dimension==2)?5:70;
					Algorithm<List<PermutationSolution<Integer>>> algorithm = 
							new EpsilonLocalSearchBuilder<>(problem)
							.setEpsilon(epsilon).
							setInitializeSolutionMethod(InitialSolutionMethod.GREEDY_METHOD)
							.setMaxPopulationSize(300).
							setMaxIteration(100).
							setNeighborSearchOperator(operator).build();
					long currentMills = System.currentTimeMillis();
					JMetalLogger.logger
							.info(algorithm.getName() + " on " + problem.getName() + " run " + run + " starting...");
					((EpsilonLocalSearch<PermutationSolution<Integer>>) algorithm).setRunId(run);
					((EpsilonLocalSearch<PermutationSolution<Integer>>) algorithm)
							.setBaseProcedureDirectory(baseDirectory + "/procedure");
					algorithm.run();
					JMetalLogger.logger.info("Use time: " + (System.currentTimeMillis() - currentMills) + "ms.");

					List<PermutationSolution<Integer>> population = algorithm.getResult();
					String path = baseDirectory + "/final/" + algorithm.getName() + "/" + problem.getName();
					if (run == 0) {
						File folder = new File(path);
						if (!folder.exists() || !folder.isDirectory()) {
							folder.mkdirs();
						}
					}
					String funFile = path + "/FUN" + run + ".tsv";
					String varFile = path + "/VAR" + run + ".tsv";
					new SolutionListOutput(population).setSeparator("\t")
							.setVarFileOutputContext(new DefaultFileOutputContext(varFile))
							.setFunFileOutputContext(new DefaultFileOutputContext(funFile)).print();
					JMetalLogger.logger.info("Objectives values have been written to file " + funFile);
					JMetalLogger.logger.info("Variables values have been written to file " + varFile);

					int searchCount = ((EpsilonLocalSearch<PermutationSolution<Integer>>) algorithm).getSearchCount();
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
					JMetalLogger.logger.info("FUN.tsv have been written to results/FUN.tsv");
				}
			}
		}
	}
}
