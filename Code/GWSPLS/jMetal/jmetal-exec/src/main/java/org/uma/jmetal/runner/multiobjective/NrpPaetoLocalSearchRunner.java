package org.uma.jmetal.runner.multiobjective;

import java.io.IOException;
import java.util.List;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.problem.multiobjective.NrpProblem;
import org.uma.jmetal.solution.GridPermutationSolution;
import org.uma.jmetal.util.AlgorithmRunner;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.fileoutput.SolutionListOutput;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;

import com.nuaa.shr.pls.algorithm.NrpMoead;

public class NrpPaetoLocalSearchRunner {
	
	public static void main(String[] args) throws IOException {
		NrpProblem problem = 
				new NrpProblem("/nrp/c200-1000-custom-require.dat","/nrp/c200-1000-profit.dat", "/nrp/c200-1000-cost.dat");
		Algorithm<List<GridPermutationSolution<Integer>>> algorithm = 
				new NrpMoead(problem, 200, 300);
		
		AlgorithmRunner runner = new AlgorithmRunner.Executor(algorithm).execute();
		List<GridPermutationSolution<Integer>> population = algorithm.getResult() ;
	    long computingTime = runner.getComputingTime() ;

	    new SolutionListOutput(population)
	            .setSeparator("\t")
	            .setVarFileOutputContext(new DefaultFileOutputContext("results/nrp-VAR.tsv"))
	            .setFunFileOutputContext(new DefaultFileOutputContext("results/nrp-FUN.tsv"))
	            .print();
	    
	    JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");
	    JMetalLogger.logger.info("Objectives values have been written to file FUN.tsv");
	    JMetalLogger.logger.info("Variables values have been written to file VAR.tsv");
	}
	

	
}
