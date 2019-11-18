package org.uma.jmetal.runner.multiobjective.pls.utils;

import org.uma.jmetal.operator.NeighborSearchOperator;
import org.uma.jmetal.operator.impl.neighborsearch.GreedyFlipNeighborSearch;
import org.uma.jmetal.operator.impl.neighborsearch.MultiFlipNeighborSearch;
import org.uma.jmetal.operator.impl.neighborsearch.TwoOpt4QAP;
import org.uma.jmetal.operator.impl.neighborsearch.TwoOptNeighborSearch;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.problem.multiobjective.ManyObjectiveKnapsack;
import org.uma.jmetal.problem.multiobjective.ManyObjectiveQAP;
import org.uma.jmetal.problem.multiobjective.ManyObjectiveTSP;
import org.uma.jmetal.problem.multiobjective.MultiObjectiveNRP;
import org.uma.jmetal.solution.PermutationSolution;
import org.uma.jmetal.util.JMetalException;

/**
 * Operator factory
 * @author sevn
 *
 */
public class OperatorFactory {

	public static NeighborSearchOperator<PermutationSolution<Integer>> getOperator(
			double searchProbilityOrFlipNum,Problem<PermutationSolution<Integer>> problem) {
		if(ManyObjectiveKnapsack.class.equals(problem.getClass()))
			return new GreedyFlipNeighborSearch(searchProbilityOrFlipNum, problem);
		else if(ManyObjectiveTSP.class.equals(problem.getClass()))
			return new TwoOptNeighborSearch(searchProbilityOrFlipNum, problem);
		else if(MultiObjectiveNRP.class.equals(problem.getClass()))
			return new MultiFlipNeighborSearch(problem, (int) searchProbilityOrFlipNum);
		else if(ManyObjectiveQAP.class.equals(problem.getClass()))
			return new TwoOpt4QAP(searchProbilityOrFlipNum, problem);
		else throw new JMetalException("The factory not support this type problem!");
	}

}
