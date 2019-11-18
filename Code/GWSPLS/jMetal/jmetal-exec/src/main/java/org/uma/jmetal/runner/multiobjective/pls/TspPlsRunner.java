package org.uma.jmetal.runner.multiobjective.pls;

import java.io.IOException;
import java.util.List;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.problem.multiobjective.GridManyObjectiveTSP;
import org.uma.jmetal.solution.GridPermutationSolution;
import org.uma.jmetal.util.AlgorithmRunner;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.fileoutput.SolutionListOutput;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;

import com.nuaa.shr.pls.algorithm.TspGridWeakDominate;
import com.nuaa.shr.pls.algorithm.TspMoead;

public class TspPlsRunner {
	public static void main(String[] args) throws IOException {
		String[] tspPaths = 
			{ "/tspInstances/kroA100.tsp", "/tspInstances/kroB100.tsp"};
		GridManyObjectiveTSP problem = new GridManyObjectiveTSP(tspPaths);
		for(int i = 0;i<1;i++){
//			int i = 0;
			Algorithm<List<GridPermutationSolution<Integer>>> algorithm = 
					new TspMoead(problem, 200, 300);
			AlgorithmRunner runner = new AlgorithmRunner.Executor(algorithm).execute();
			List<GridPermutationSolution<Integer>> population = algorithm.getResult();
			long computingTime = runner.getComputingTime();
	
			new SolutionListOutput(population).setSeparator("\t")
					.setVarFileOutputContext(new DefaultFileOutputContext(
							"results/VAR"+i+".tsv"))
					.setFunFileOutputContext(new DefaultFileOutputContext(
							"results/FUN"+i+".tsv"))
					.print();
			
			JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");
			JMetalLogger.logger.info("Objectives values have been written to file FUN.tsv");
			JMetalLogger.logger.info("Variables values have been written to file VAR.tsv");
		}
	}

}
