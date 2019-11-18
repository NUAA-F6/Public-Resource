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

import com.nuaa.shr.pls.algorithm.nepsilonls.EpsilonLocalSearch.InitialSolutionMethod;
import com.nuaa.shr.pls.algorithm.nepsilonls.EpsilonLocalSearchBuilder;

/**
 * 2-dimension epsilom=5
 * 3-dimension epsilon=70
 * 250,500 search probility = 0.5
 * 750 search probility = 0.3
 * @author sevn
 *
 */
public class EpsilonKnapsackRunner {
	public static void main(String[] args) throws IOException {
		Problem<PermutationSolution<Integer>> problem = 
				ProblemFactory.getKnapsack(250,3);
		NeighborSearchOperator<PermutationSolution<Integer>> operator = 
				OperatorFactory.getOperator(0.5, problem);
		Algorithm<List<PermutationSolution<Integer>>> algorithm = new EpsilonLocalSearchBuilder<PermutationSolution<Integer>>(problem)
				.setEpsilon(70)
				.setMaxIteration(100)
				.setNeighborSearchOperator(operator)
				.setInitializeSolutionMethod(InitialSolutionMethod.GREEDY_METHOD)
				.setMaxPopulationSize(300)
				.build() ;
		long currentTime = System.currentTimeMillis();
		algorithm.run();
		JMetalLogger.logger.info("Use time: "+(System.currentTimeMillis()-currentTime)+"ms");
		new SolutionListOutput(algorithm.getResult()).printObjectivesToFile("results/FUN.tsv");
	}
}
