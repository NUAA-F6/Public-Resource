package org.uma.jmetal.experiment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.problem.multiobjective.GridManyObjectiveKnapsack;
import org.uma.jmetal.problem.multiobjective.GridManyObjectiveTSP;
import org.uma.jmetal.qualityindicator.impl.Epsilon;
import org.uma.jmetal.qualityindicator.impl.GenerationalDistance;
import org.uma.jmetal.qualityindicator.impl.GenericIndicator;
import org.uma.jmetal.qualityindicator.impl.InvertedGenerationalDistance;
import org.uma.jmetal.qualityindicator.impl.InvertedGenerationalDistancePlus;
import org.uma.jmetal.qualityindicator.impl.Spread;
import org.uma.jmetal.qualityindicator.impl.hypervolume.PISAHypervolume;
import org.uma.jmetal.qualityindicator.impl.hypervolume.WFGHypervolume;
import org.uma.jmetal.solution.GridPermutationSolution;
import org.uma.jmetal.util.experiment.Experiment;
import org.uma.jmetal.util.experiment.ExperimentBuilder;
import org.uma.jmetal.util.experiment.component.ComputeProcedureIndicator;
import org.uma.jmetal.util.experiment.component.ComputeProcedureIndicatorMean;
import org.uma.jmetal.util.experiment.component.ComputeQualityIndicators;
import org.uma.jmetal.util.experiment.component.ExecuteAlgorithms;
import org.uma.jmetal.util.experiment.component.GenerateBoxplotsWithR;
import org.uma.jmetal.util.experiment.component.GenerateFriedmanTestTables;
import org.uma.jmetal.util.experiment.component.GenerateLatexTablesWithStatistics;
import org.uma.jmetal.util.experiment.component.GenerateReferenceParetoFront;
import org.uma.jmetal.util.experiment.component.GenerateWilcoxonTestTablesWithR;
import org.uma.jmetal.util.experiment.util.TaggedAlgorithm;

import com.nuaa.shr.pls.algorithm.KnapsackGridWeakDominatePls;
import com.nuaa.shr.pls.algorithm.KnapsackMoead;
import com.nuaa.shr.pls.algorithm.TspGridWeakDominate;
import com.nuaa.shr.pls.algorithm.TspMoead;
import com.nuaa.shr.pls.algorithm.abst.MoeadParetoLocalSearch;



public class PlsManyIndicatorStudy {
	private static final int INDIPENDENT_RUNS = 30;
	
	public static void main(String[] args) throws IOException {
	    String experimentBaseDirectory = "E:/sevn/experiment/data/pls_many_procedure" ;
	    String paretoFrontDirectoty = experimentBaseDirectory+"/pfs";
	    List<Problem<GridPermutationSolution<Integer>>> problemList = configProblems();
	    List<TaggedAlgorithm<List<GridPermutationSolution<Integer>>>> algorithms = 
	    		configAlgorithms(problemList, INDIPENDENT_RUNS);
	    GenericIndicator<GridPermutationSolution<Integer>> indicator = new WFGHypervolume<>();
	    Experiment<GridPermutationSolution<Integer>,List<GridPermutationSolution<Integer>>> experiment =
	    		new ExperimentBuilder<GridPermutationSolution<Integer>,List<GridPermutationSolution<Integer>>>("plsStudy")
	    		.setAlgorithmList(algorithms)
	    		.setProblemList(problemList)
	    		.setExperimentBaseDirectory(experimentBaseDirectory)
	    		.setReferenceFrontDirectory(paretoFrontDirectoty)
	    		.setIndependentRuns(INDIPENDENT_RUNS)
	    		.setOutputParetoSetFileName("VAR")
	    		.setOutputParetoFrontFileName("FUN")
	            .setIndicatorList(Arrays.asList(
	            		indicator))
	            .setNumberOfCores(3)
	            .build();
//	    new ExecuteAlgorithms<>(experiment).run();
//	    new GenerateReferenceParetoFront(experiment).run();
//	    new ComputeQualityIndicators<>(experiment).run() ;
	    
//	    new ComputeProcedureIndicator<>(experiment).run();
	    new ComputeProcedureIndicatorMean<>(experiment).run();
	    
//	    new GenerateLatexTablesWithStatistics(experiment).run() ;
//	    new GenerateWilcoxonTestTablesWithR<>(experiment).run() ;
//	    new GenerateFriedmanTestTables<>(experiment).run();
//	    new GenerateBoxplotsWithR<>(experiment).setRows(3).setColumns(3).setDisplayNotch().run() ;
	    
	    		
	}

	static List<Problem<GridPermutationSolution<Integer>>> configProblems() {
		List<Problem<GridPermutationSolution<Integer>>> problemList = new ArrayList<>();
		Problem<GridPermutationSolution<Integer>> problem = new GridManyObjectiveKnapsack
				("/knapsack/profit250_4.txt",
				"/knapsack/weight250_4.txt", 
				"/knapsack/capacity250_4.txt", 250, 4);
		((GridManyObjectiveKnapsack) problem).setName("knapsack250_4");
		problemList.add(problem);

		problem = new GridManyObjectiveKnapsack("/knapsack/profit500_4.txt", "/knapsack/weight500_4.txt",
				"/knapsack/capacity500_4.txt", 500, 4);
		((GridManyObjectiveKnapsack) problem).setName("knapsack500_4");
		problemList.add(problem);

		problem = new GridManyObjectiveKnapsack("/knapsack/profit750_4.txt", "/knapsack/weight750_4.txt",
				"/knapsack/capacity750_4.txt", 750, 4);
		((GridManyObjectiveKnapsack) problem).setName("knapsack750_4");
		problemList.add(problem);

		problem = new GridManyObjectiveKnapsack("/knapsack/profit250_5.txt", "/knapsack/weight250_5.txt",
				"/knapsack/capacity250_5.txt", 250, 5);
		((GridManyObjectiveKnapsack) problem).setName("knapsack250_5");
		problemList.add(problem);

		problem = new GridManyObjectiveKnapsack("/knapsack/profit500_5.txt", "/knapsack/weight500_5.txt",
				"/knapsack/capacity500_5.txt", 500, 5);
		((GridManyObjectiveKnapsack) problem).setName("knapsack500_5");
		problemList.add(problem);

		problem = new GridManyObjectiveKnapsack("/knapsack/profit750_5.txt", "/knapsack/weight750_5.txt",
				"/knapsack/capacity750_5.txt", 750, 5);
		((GridManyObjectiveKnapsack) problem).setName("knapsack750_5");
		problemList.add(problem);

		problem = new GridManyObjectiveKnapsack(
				"/knapsack/profit250_6.txt", 
				"/knapsack/weight250_6.txt",
				"/knapsack/capacity250_6.txt", 250, 6);
		((GridManyObjectiveKnapsack) problem).setName("knapsack250_6");
		problemList.add(problem);

		problem = new GridManyObjectiveKnapsack(
				"/knapsack/profit500_6.txt", 
				"/knapsack/weight500_6.txt",
				"/knapsack/capacity500_6.txt", 500, 6);
		((GridManyObjectiveKnapsack) problem).setName("knapsack500_6");
		problemList.add(problem);

		problem = new GridManyObjectiveKnapsack(
				"/knapsack/profit750_6.txt", 
				"/knapsack/weight750_6.txt",
				"/knapsack/capacity750_6.txt", 750, 6);
		((GridManyObjectiveKnapsack) problem).setName("knapsack750_6");
		problemList.add(problem);
		
		return problemList;
	}
	
	static List<TaggedAlgorithm<List<GridPermutationSolution<Integer>>>> configAlgorithms(
			List<Problem<GridPermutationSolution<Integer>>> problemList, int independentRuns){
		List<TaggedAlgorithm<List<GridPermutationSolution<Integer>>>> algorithms = new ArrayList<>();
		
		String baseProcedureDirectory = "F:/sevn/experiment/data/pls_many_procedure/plsStudy/procedure";
//	    for (int run = 0; run < independentRuns; run++) {
//	        for (int i = 0; i < problemList.size(); i++) {
//			  Algorithm<List<GridPermutationSolution<Integer>>> algorithm = 
//						new KnapsackGridWeakDominatePls(problemList.get(i), 200, 300, 200);
//			  
//			  ((KnapsackGridWeakDominatePls)algorithm).setRunId(run);
//			  ((KnapsackGridWeakDominatePls)algorithm).setBaseProcedureDirectory(baseProcedureDirectory);
//			  
//	          algorithms.add(
//	        		  new TaggedAlgorithm<List<GridPermutationSolution<Integer>>>(
//	        				  algorithm, "GPLS-GWS", problemList.get(i), run));
//	        }
//	    }
//	    
//	    for (int run = 0; run < independentRuns; run++) {
//	        for (int i = 0; i < problemList.size(); i++) {
//			  Algorithm<List<GridPermutationSolution<Integer>>> algorithm = 
//						new KnapsackGridWeakDominatePls(problemList.get(i), 200, 300, 200);
//			  
//			  ((KnapsackGridWeakDominatePls)algorithm).setRunId(run);
//			  ((KnapsackGridWeakDominatePls)algorithm).setBaseProcedureDirectory(baseProcedureDirectory);
//			  
//	          algorithms.add(
//	        		  new TaggedAlgorithm<List<GridPermutationSolution<Integer>>>(
//	        				  algorithm, "GPLS-MD", problemList.get(i), run));
//	        }
//	    }
	    
	    for (int run = 0; run < independentRuns; run++) {
	        for (int i = 0; i < problemList.size(); i++) {
			  Algorithm<List<GridPermutationSolution<Integer>>> algorithm = 
						new KnapsackMoead(problemList.get(i), 200, 300);
			  ((KnapsackMoead)algorithm).setFunctionType(MoeadParetoLocalSearch.FunctionType.AGG);
			  ((KnapsackMoead)algorithm).setRunId(run);
			  ((KnapsackMoead)algorithm).setBaseProcedureDirectory(baseProcedureDirectory);
			  
	          algorithms.add(
	        		  new TaggedAlgorithm<List<GridPermutationSolution<Integer>>>(
	        				  algorithm, "MOEAD-LS(WS)", problemList.get(i), run));
	        }
	    }
	    
	    for (int run = 0; run < independentRuns; run++) {
	        for (int i = 0; i < problemList.size(); i++) {
			  Algorithm<List<GridPermutationSolution<Integer>>> algorithm = 
						new KnapsackMoead(problemList.get(i), 200, 300);
			  ((KnapsackMoead)algorithm).setFunctionType(MoeadParetoLocalSearch.FunctionType.TCHE);
			  ((KnapsackMoead)algorithm).setRunId(run);
			  ((KnapsackMoead)algorithm).setBaseProcedureDirectory(baseProcedureDirectory);
			  
	          algorithms.add(
	        		  new TaggedAlgorithm<List<GridPermutationSolution<Integer>>>(
	        				  algorithm, "MOEAD-LS(TCH)", problemList.get(i), run));
	        }
	    }
	    
	    for (int run = 0; run < independentRuns; run++) {
	        for (int i = 0; i < problemList.size(); i++) {
			  Algorithm<List<GridPermutationSolution<Integer>>> algorithm = 
						new KnapsackMoead(problemList.get(i), 200, 300);
			  ((KnapsackMoead)algorithm).setFunctionType(MoeadParetoLocalSearch.FunctionType.PBI);
			  ((KnapsackMoead)algorithm).setRunId(run);
			  ((KnapsackMoead)algorithm).setBaseProcedureDirectory(baseProcedureDirectory);
			  
	          algorithms.add(
	        		  new TaggedAlgorithm<List<GridPermutationSolution<Integer>>>(
	        				  algorithm, "MOEAD-LS(PBI)", problemList.get(i), run));
	        }
	    }
	    
	    for (int run = 0; run < independentRuns; run++) {
        for (int i = 0; i < problemList.size(); i++) {
		  Algorithm<List<GridPermutationSolution<Integer>>> algorithm = 
					new KnapsackGridWeakDominatePls(problemList.get(i), 200, 300, 200);
		  
		  ((KnapsackGridWeakDominatePls)algorithm).setRunId(run);
		  ((KnapsackGridWeakDominatePls)algorithm).setBaseProcedureDirectory(baseProcedureDirectory);
		  
          algorithms.add(
        		  new TaggedAlgorithm<List<GridPermutationSolution<Integer>>>(
        				  algorithm, "EpsilonMOEA", problemList.get(i), run));
        }
        
        for (int i = 0; i < problemList.size(); i++) {
		  Algorithm<List<GridPermutationSolution<Integer>>> algorithm = 
					new KnapsackGridWeakDominatePls(problemList.get(i), 200, 300, 200);
		  
		  ((KnapsackGridWeakDominatePls)algorithm).setRunId(run);
		  ((KnapsackGridWeakDominatePls)algorithm).setBaseProcedureDirectory(baseProcedureDirectory);
		  
          algorithms.add(
        		  new TaggedAlgorithm<List<GridPermutationSolution<Integer>>>(
        				  algorithm, "GPLS-D", problemList.get(i), run));
        }
    }
	    
		return algorithms;
		
	} 
	

}
