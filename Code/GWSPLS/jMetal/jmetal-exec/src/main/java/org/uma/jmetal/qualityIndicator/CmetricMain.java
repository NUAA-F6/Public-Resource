package org.uma.jmetal.qualityIndicator;

import java.io.IOException;
import java.util.List;

import org.uma.jmetal.problem.multiobjective.GridManyObjectiveTSP;
import org.uma.jmetal.qualityIndicator.utils.FileRoW;
import org.uma.jmetal.qualityindicator.impl.CMetric;
import org.uma.jmetal.solution.GridPermutationSolution;

public class CmetricMain {
	public static void main(String[] args) throws IOException {

		String[] tspPaths = { "/tspInstances/euclidA100.tsp", "/tspInstances/euclidB100.tsp","/tspInstances/euclidC100.tsp","/tspInstances/euclidD100.tsp","/tspInstances/euclidE100.tsp"};
		GridManyObjectiveTSP problem = new GridManyObjectiveTSP(tspPaths);

		String popAName = "results/temp";
		int popASize = 4000;
		String popBName = "results/temp3";
		int popBSize = 400;

		List<GridPermutationSolution<Integer>> popB = new FileRoW<>(problem).getPopulationFromFile(popBName, popBSize);

		List<GridPermutationSolution<Integer>> popA = new FileRoW<>(problem).getPopulationFromFile(popAName, popASize);

		double metricValue = new CMetric<GridPermutationSolution<Integer>>(popB).evaluate(popA);
		System.out.println("C( " + popAName + ", " + popBName + " ): " + metricValue);

		double metricValue2 = new CMetric<GridPermutationSolution<Integer>>(popA).evaluate(popB);

		System.out.println("C( " + popBName + ", " + popAName + " ): " + metricValue2);
		System.out.println("\n");
	}
}
