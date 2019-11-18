package org.uma.jmetal.qualityIndicator;

import java.io.IOException;
import java.util.List;

import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.problem.multiobjective.GridManyObjectiveTSP;
import org.uma.jmetal.qualityIndicator.utils.FileRoW;
import org.uma.jmetal.qualityindicator.impl.CMetric;
import org.uma.jmetal.solution.GridPermutationSolution;

public class CMtricCompute {
	public static void main(String[] args) throws IOException {
		String[] tspPaths = { "/tspInstances/kroA100.tsp", "/tspInstances/kroB100.tsp", "/tspInstances/kroC100.tsp"};
		Problem<GridPermutationSolution<Integer>> problem = new GridManyObjectiveTSP(tspPaths);
		
		String root = "E:/sevn/experiment/data/pls_multi_indicator/plsStudy/data";
		double[] cAB = new double[900];
		double[] cBA = new double[900];
		int index = 0;
		for (int run = 0; run < 30; run++) {
			String popAName = root+"/GPLS-D/knapsack750_3/FUN" + run + ".tsv";
			int popASize = 1000;
			for (int run2 = 0; run2 < 30; run2++) {
				String popBName = root+"/EpsilonMOEA/knapsack750_3/FUN" + run2 + ".tsv";
				int popBSize = 1000;

				List<GridPermutationSolution<Integer>> popB = new FileRoW<>(problem).getPopulationFromFile(popBName,
						popBSize);

				List<GridPermutationSolution<Integer>> popA = new FileRoW<>(problem).getPopulationFromFile(popAName,
						popASize);

				System.out.println("run:" + run + "...");
				double metricValue = new CMetric<GridPermutationSolution<Integer>>(popB).evaluate(popA);
				cAB[index] = metricValue;
				System.out.println("C( " + popAName + ", " + popBName + " ): " + metricValue);

				double metricValue2 = new CMetric<GridPermutationSolution<Integer>>(popA).evaluate(popB);
				cBA[index] = metricValue2;
				
				index++;
				
				System.out.println("C( " + popBName + ", " + popAName + " ): " + metricValue2);
				System.out.println("\n");
			}
		}
		double sumAB = 0.0;
		double sumBA = 0.0;
		for (int i = 0; i < cAB.length; i++) {
			sumAB += cAB[i];
			sumBA += cBA[i];
		}
		System.out.println("mean:" + sumAB / (double) cAB.length + "||" + sumBA / (double) cBA.length);
	}
}