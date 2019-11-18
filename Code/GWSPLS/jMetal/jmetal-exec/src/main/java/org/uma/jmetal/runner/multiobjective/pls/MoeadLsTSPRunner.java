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

import com.nuaa.shr.pls.algorithm.moeadls.MoeadLs.FunctionType;
import com.nuaa.shr.pls.algorithm.moeadls.MoeadLsBuilder;

public class MoeadLsTSPRunner {
	public static void main(String[] args) throws IOException {
		Problem<PermutationSolution<Integer>> problem = 
				ProblemFactory.getQAP(60, 2);
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
		new SolutionListOutput(algorithm.getResult()).printObjectivesToFile("results/PBIFUN.tsv");
		JMetalLogger.logger.info("FUN.tsv have been written to results/PBIFUN.tsv");
	}
}
