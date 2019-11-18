package org.uma.jmetal.runner.multiobjective.pls;

import java.io.IOException;
import java.util.List;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.operator.NeighborSearchOperator;
import org.uma.jmetal.operator.impl.neighborsearch.MultiFlipNeighborSearch;
import org.uma.jmetal.problem.multiobjective.MultiObjectiveNRP;
import org.uma.jmetal.runner.multiobjective.pls.utils.ProblemFactory;
import org.uma.jmetal.solution.PermutationSolution;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.fileoutput.SolutionListOutput;

import com.nuaa.shr.pls.algorithm.momad.MOMAD.InitialSolutionMethod;
import com.nuaa.shr.pls.algorithm.momad.MOMADBuilder;

public class MOMADNrpRunner {
	public static void main(String[] args) throws IOException {
		MultiObjectiveNRP problem = (MultiObjectiveNRP) ProblemFactory.getNRP("e1");
		NeighborSearchOperator<PermutationSolution<Integer>> operator = 
				new MultiFlipNeighborSearch(problem, 5) ;
		Algorithm<List<PermutationSolution<Integer>>> algorithm = 
				new MOMADBuilder<>(problem)
				.setInitialSolutionMethod(InitialSolutionMethod.RANDOM_METHOD)
				.setNeighborSearchOperator(operator)
				.setMaxIteration(200)
				.setMaxPopulationSize(300)
				.build();
		long currentMills = System.currentTimeMillis();
		algorithm.run();
		JMetalLogger.logger.info("Use time: "+ (System.currentTimeMillis()-currentMills) +"ms.");
		new SolutionListOutput(algorithm.getResult()).printObjectivesToFile("results/FUN.tsv");
		JMetalLogger.logger.info("FUN.tsv have been written to results/FUN.tsv");
	}
}
