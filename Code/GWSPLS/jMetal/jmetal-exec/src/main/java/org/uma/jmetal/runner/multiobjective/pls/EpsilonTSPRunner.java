package org.uma.jmetal.runner.multiobjective.pls;

import java.io.IOException;

import org.uma.jmetal.operator.NeighborSearchOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.runner.multiobjective.pls.utils.OperatorFactory;
import org.uma.jmetal.runner.multiobjective.pls.utils.ProblemFactory;
import org.uma.jmetal.solution.PermutationSolution;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.fileoutput.SolutionListOutput;

import com.nuaa.shr.pls.algorithm.nepsilonls.EpsilonLocalSearch;
import com.nuaa.shr.pls.algorithm.nepsilonls.EpsilonLocalSearch.InitialSolutionMethod;
import com.nuaa.shr.pls.algorithm.nepsilonls.EpsilonLocalSearchBuilder;

/**
 * 2-dimension epsilon=100
 * 3-dimension epsilon=1000
 * @author sevn
 *
 */
public class EpsilonTSPRunner {
	public static void main(String[] args) throws IOException {
		Problem<PermutationSolution<Integer>> manyObjectiveTSP = 
				ProblemFactory.getTSP("kro",100,2);
		NeighborSearchOperator<PermutationSolution<Integer>> operator = 
				OperatorFactory.getOperator(1.0, manyObjectiveTSP);
		
		EpsilonLocalSearch<PermutationSolution<Integer>> epsilonAlg = new EpsilonLocalSearchBuilder<>(manyObjectiveTSP)
				.setEpsilon(100)
				.setMaxIteration(200)
				.setNeighborSearchOperator(operator)
				.setInitializeSolutionMethod(InitialSolutionMethod.RANDOM_METHOD)
				.setMaxPopulationSize(300).build();
		epsilonAlg.run();
		new SolutionListOutput(epsilonAlg.getResult()).printObjectivesToFile("results/FUN.tsv");
		JMetalLogger.logger.info("FUN.tsv have been written to results/FUN.tsv");
	}

}
