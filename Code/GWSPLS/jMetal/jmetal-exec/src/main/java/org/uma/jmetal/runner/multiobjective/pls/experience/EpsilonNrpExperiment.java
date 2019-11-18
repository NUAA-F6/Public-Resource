package org.uma.jmetal.runner.multiobjective.pls.experience;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.operator.NeighborSearchOperator;
import org.uma.jmetal.problem.multiobjective.MultiObjectiveNRP;
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

public class EpsilonNrpExperiment {
	public static void main(String[] args) throws IOException {
		String[] problemName = { "e1", "e2", "e3", "e4", "g1", "g2", "g3", "g4" };
		String baseDirectory = "results";
		for (int i = 0; i < problemName.length; i++) {
			for (int run = 0; run < 30; run++) {
				
				MultiObjectiveNRP problem = (MultiObjectiveNRP) ProblemFactory.getNRP(problemName[i]);
				NeighborSearchOperator<PermutationSolution<Integer>> operator = OperatorFactory.getOperator(5, problem);

				Algorithm<List<PermutationSolution<Integer>>> algorithm = new EpsilonLocalSearchBuilder<>(problem)
						.setEpsilon(20).setInitializeSolutionMethod(InitialSolutionMethod.RANDOM_METHOD)
						.setMaxPopulationSize(300).setMaxIteration(200).setNeighborSearchOperator(operator).build();
				long currentMills = System.currentTimeMillis();
				JMetalLogger.logger.info(algorithm.getName() + " on "+problem.getName()+" run "+run+" starting...");
				((EpsilonLocalSearch<PermutationSolution<Integer>>) algorithm).setRunId(run);
				((EpsilonLocalSearch<PermutationSolution<Integer>>) algorithm).setBaseProcedureDirectory(baseDirectory+"/procedure");
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
