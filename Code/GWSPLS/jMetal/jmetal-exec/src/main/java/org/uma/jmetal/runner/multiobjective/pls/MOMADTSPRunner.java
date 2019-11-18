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

import com.nuaa.shr.pls.algorithm.momad.MOMADBuilder;

public class MOMADTSPRunner {
	public static void main(String[] args) throws IOException {
		Problem<PermutationSolution<Integer>> problem = ProblemFactory.getQAP(60, 2);
		NeighborSearchOperator<PermutationSolution<Integer>> operator = 
				OperatorFactory.getOperator(0.5, problem);
		Algorithm<List<PermutationSolution<Integer>>> algorithm = 
				new MOMADBuilder<>(problem)
//				.setInitialSolutionMethod(InitialSolutionMethod.GREEDY_METHOD)
				.setNeighborSearchOperator(operator)
				.setMaxIteration(200)
				.setMaxPopulationSize(300)
				.build();
		long currentMills = System.currentTimeMillis();
		algorithm.run();
		JMetalLogger.logger.info("Use time: "+ (System.currentTimeMillis()-currentMills) +"ms.");
		new SolutionListOutput(algorithm.getResult()).printObjectivesToFile("results/MOMADFUN.tsv");
		JMetalLogger.logger.info("FUN.tsv have been written to results/MOMADFUN.tsv");
	}
}
