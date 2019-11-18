package org.uma.jmetal.runner.multiobjective;

import java.io.IOException;
import java.util.List;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.problem.multiobjective.DefaultManyObjectiveTSP;
import org.uma.jmetal.solution.PermutationSolution;
import org.uma.jmetal.util.AlgorithmRunner;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.fileoutput.SolutionListOutput;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;

import com.nuaa.shr.pls.OriginalParetoLocalSearch;

public class PaetoLocalSearchRunner {
	
	public static void main(String[] args) throws IOException {
		String[] tspPaths = {"/tspInstances/kroA100.tsp", "/tspInstances/kroB100.tsp"};
		DefaultManyObjectiveTSP problem = new DefaultManyObjectiveTSP(tspPaths);
		Algorithm<List<PermutationSolution<Integer>>> algorithm = new OriginalParetoLocalSearch(problem, 200, 200);
		
		AlgorithmRunner runner = new AlgorithmRunner.Executor(algorithm).execute();
		List<PermutationSolution<Integer>> population = algorithm.getResult() ;
	    long computingTime = runner.getComputingTime() ;

	    new SolutionListOutput(population)
	            .setSeparator("\t")
	            .setVarFileOutputContext(new DefaultFileOutputContext("results/original-VAR.tsv"))
	            .setFunFileOutputContext(new DefaultFileOutputContext("results/original-FUN.tsv"))
	            .print();
	    
	    JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");
	    JMetalLogger.logger.info("Objectives values have been written to file FUN.tsv");
	    JMetalLogger.logger.info("Variables values have been written to file VAR.tsv");
	}
	

	
}
