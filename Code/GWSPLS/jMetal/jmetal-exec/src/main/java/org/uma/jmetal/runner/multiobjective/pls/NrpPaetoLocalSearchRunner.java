package org.uma.jmetal.runner.multiobjective.pls;

import java.io.IOException;
import java.util.List;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.problem.multiobjective.NrpProblem;
import org.uma.jmetal.solution.GridPermutationSolution;
import org.uma.jmetal.util.AlgorithmRunner;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.fileoutput.SolutionListOutput;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;

import com.nuaa.shr.pls.algorithm.NrpGridWeakDominatePls;
import com.nuaa.shr.pls.algorithm.NrpMoead;

public class NrpPaetoLocalSearchRunner {
	
	public static void main(String[] args) throws IOException {
		NrpProblem problem = 
				new NrpProblem("/nrp/e1-custom-require.dat","/nrp/e1-profit.dat", "/nrp/e1-cost.dat");
		Algorithm<List<GridPermutationSolution<Integer>>> algorithm = 
				new NrpGridWeakDominatePls(problem, 200, 300, 160);
		
		AlgorithmRunner runner = new AlgorithmRunner.Executor(algorithm).execute();
		List<GridPermutationSolution<Integer>> population = algorithm.getResult() ;
	    long computingTime = runner.getComputingTime() ;

	    new SolutionListOutput(population)
	            .setSeparator("\t")
	            .setVarFileOutputContext(new DefaultFileOutputContext("results/e1nrp-VAR.tsv"))
	            .setFunFileOutputContext(new DefaultFileOutputContext("results/e1nrp-FUN.tsv"))
	            .print();
	    
	    JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");
	    JMetalLogger.logger.info("Objectives values have been written to file FUN.tsv");
	    JMetalLogger.logger.info("Variables values have been written to file VAR.tsv");
	}
	

	
}
