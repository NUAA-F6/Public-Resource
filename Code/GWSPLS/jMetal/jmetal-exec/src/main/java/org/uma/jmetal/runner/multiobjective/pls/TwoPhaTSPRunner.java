package org.uma.jmetal.runner.multiobjective.pls;

import java.io.IOException;
import java.util.List;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.operator.NeighborSearchOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.runner.multiobjective.pls.utils.OperatorFactory;
import org.uma.jmetal.runner.multiobjective.pls.utils.ProblemFactory;
import org.uma.jmetal.solution.PermutationSolution;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.fileoutput.SolutionListOutput;

import com.nuaa.shr.pls.algorithm.gplsd.GPLSD;
import com.nuaa.shr.pls.algorithm.gplsd.GPLSD.InitialSolutionMethod;
import com.nuaa.shr.pls.algorithm.gplsd.GPLSDBuillder;
import com.nuaa.shr.pls.algorithm.moeadls.MoeadLsBuilder;
import com.nuaa.shr.pls.algorithm.moeadls.MoeadLs.FunctionType;

public class TwoPhaTSPRunner {
	public static void main(String[] args) throws IOException {
		Problem<PermutationSolution<Integer>> problem = 
				ProblemFactory.getTSP("kro", 100, 3);
		NeighborSearchOperator<PermutationSolution<Integer>> operator = 
				OperatorFactory.getOperator(1.0, problem);
		Algorithm<List<PermutationSolution<Integer>>> algorithm = 
				new MoeadLsBuilder<>(problem)
				.setMaxIteration(200)
				.setMaxPopulationSize(300)
				.setNeighborSearchOperator(operator)
				.setFunctionType(FunctionType.TCH)
				.build();
		algorithm.run();
		JMetalLogger.logger.info("phase 1 end... phase 2 starting...");
		new SolutionListOutput(algorithm.getResult()).printObjectivesToFile("results/RUN4FUN1.tsv");
		JMetalLogger.logger.info("FUN1.tsv have been written to results/RUN4FUN1.tsv");
		List<PermutationSolution<Integer>> ph1Population = algorithm.getResult();
		GPLSD<PermutationSolution<Integer>> alg2 = 
				new GPLSDBuillder<>(problem)
				.setMaxIteration(200)
				.setMaxPopulationSize(300)
				.setNumberOfGrid(14)
				.setNeighborSearchOperator(operator)
				.setInitializeSolutionMethod(InitialSolutionMethod.USER_DEFINR_METHOD)
				.build();
		alg2.setUserPopulation(ph1Population);
		alg2.run();
		new SolutionListOutput(alg2.getResult()).printObjectivesToFile("results/RUN4FUN2.tsv");
		JMetalLogger.logger.info("FUN2.tsv have been written to results/RUN4FUN2.tsv");
	}
}
