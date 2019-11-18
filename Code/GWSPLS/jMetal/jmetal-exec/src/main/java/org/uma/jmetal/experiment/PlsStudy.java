package org.uma.jmetal.experiment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.problem.multiobjective.GridManyObjectiveTSP;
import org.uma.jmetal.qualityindicator.impl.Epsilon;
import org.uma.jmetal.qualityindicator.impl.GenerationalDistance;
import org.uma.jmetal.qualityindicator.impl.InvertedGenerationalDistance;
import org.uma.jmetal.qualityindicator.impl.InvertedGenerationalDistancePlus;
import org.uma.jmetal.qualityindicator.impl.Spread;
import org.uma.jmetal.qualityindicator.impl.hypervolume.PISAHypervolume;
import org.uma.jmetal.solution.GridPermutationSolution;
import org.uma.jmetal.util.experiment.Experiment;
import org.uma.jmetal.util.experiment.ExperimentBuilder;
import org.uma.jmetal.util.experiment.component.ComputeQualityIndicators;
import org.uma.jmetal.util.experiment.component.ExecuteAlgorithms;
import org.uma.jmetal.util.experiment.component.GenerateBoxplotsWithR;
import org.uma.jmetal.util.experiment.component.GenerateFriedmanTestTables;
import org.uma.jmetal.util.experiment.component.GenerateLatexTablesWithStatistics;
import org.uma.jmetal.util.experiment.component.GenerateReferenceParetoFront;
import org.uma.jmetal.util.experiment.component.GenerateWilcoxonTestTablesWithR;
import org.uma.jmetal.util.experiment.util.TaggedAlgorithm;

import com.nuaa.shr.pls.algorithm.TspGridWeakDominate;
import com.nuaa.shr.pls.algorithm.TspMoead;
import com.nuaa.shr.pls.algorithm.abst.MoeadParetoLocalSearch;



public class PlsStudy {
	private static final int INDIPENDENT_RUNS = 2;
	
	public static void main(String[] args) throws IOException {
	    String experimentBaseDirectory = "F:\\sevn\\experiment\\data\\pls_test_2_24" ;
	    String paretoFrontDirectoty = experimentBaseDirectory+"/pfs";
	    List<Problem<GridPermutationSolution<Integer>>> problemList = configProblems();
	    List<TaggedAlgorithm<List<GridPermutationSolution<Integer>>>> algorithms = 
	    		configAlgorithms(problemList, INDIPENDENT_RUNS);
	    
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
	                    new Epsilon<GridPermutationSolution<Integer>>(), new Spread<GridPermutationSolution<Integer>>(), new GenerationalDistance<GridPermutationSolution<Integer>>(),
	                    new PISAHypervolume<GridPermutationSolution<Integer>>(),
	                    new InvertedGenerationalDistance<GridPermutationSolution<Integer>>(),
	                    new InvertedGenerationalDistancePlus<GridPermutationSolution<Integer>>()))
	            .setNumberOfCores(3)
	            .build();
	    new ExecuteAlgorithms<>(experiment).run();
	    new GenerateReferenceParetoFront(experiment).run();
	    new ComputeQualityIndicators<>(experiment).run() ;
	    new GenerateLatexTablesWithStatistics(experiment).run() ;
	    new GenerateWilcoxonTestTablesWithR<>(experiment).run() ;
	    new GenerateFriedmanTestTables<>(experiment).run();
	    new GenerateBoxplotsWithR<>(experiment).setRows(3).setColumns(3).setDisplayNotch().run() ;
	    
	    		
	}

	static List<Problem<GridPermutationSolution<Integer>>> configProblems(
			){
		List<Problem<GridPermutationSolution<Integer>>> problemList = new ArrayList<>();
		try {
			String[] tspPaths1 = 
				{ "/tspInstances/kroA100.tsp", "/tspInstances/kroB100.tsp"};
			Problem<GridPermutationSolution<Integer>> problem1 = new GridManyObjectiveTSP(tspPaths1);
			((GridManyObjectiveTSP)problem1).setName("kroAB100");
			problemList.add(problem1);
			
			String[] tspPaths2 = 
				{ "/tspInstances/euclidA100.tsp", "/tspInstances/euclidB100.tsp"};
			Problem<GridPermutationSolution<Integer>> problem2 = new GridManyObjectiveTSP(tspPaths2);
			((GridManyObjectiveTSP)problem2).setName("euclidAB100");
			problemList.add(problem2);
			
			String[] tspPaths3 = 
				{ "/tspInstances/ClusterA100.tsp", "/tspInstances/ClusterB100.tsp" } ;
			Problem<GridPermutationSolution<Integer>> problem3 = new GridManyObjectiveTSP(tspPaths3);
			((GridManyObjectiveTSP)problem3).setName("ClusterAB100");
			problemList.add(problem3);
			
			String[] tspPaths4 = 
				{ "/tspInstances/kroA100.tsp", "/tspInstances/kroB100.tsp", "/tspInstances/kroC100.tsp" } ;
			Problem<GridPermutationSolution<Integer>> problem4 = new GridManyObjectiveTSP(tspPaths4);
			((GridManyObjectiveTSP)problem4).setName("kroABC100");
			problemList.add(problem4);
			
			String[] tspPaths5 = 
				{ "/tspInstances/euclidA100.tsp", "/tspInstances/euclidB100.tsp", "/tspInstances/euclidC100.tsp" } ;
			Problem<GridPermutationSolution<Integer>> problem5 = new GridManyObjectiveTSP(tspPaths5);
			((GridManyObjectiveTSP)problem5).setName("euclidABC100");
			problemList.add(problem5);
			
		} catch (IOException e) {
			System.err.println("Exception in initilize problem");
			e.printStackTrace();
		}
		return problemList;
	}
	
	static List<TaggedAlgorithm<List<GridPermutationSolution<Integer>>>> configAlgorithms(
			List<Problem<GridPermutationSolution<Integer>>> problemList, int independentRuns){
		List<TaggedAlgorithm<List<GridPermutationSolution<Integer>>>> algorithms = new ArrayList<>();
		
		String baseProcedureDirectory = "F:/sevn/experiment/data/pls_test_2_24/procedure";
	    for (int run = 0; run < independentRuns; run++) {
	        for (int i = 0; i < problemList.size(); i++) {
			  Algorithm<List<GridPermutationSolution<Integer>>> algorithm = 
						new TspGridWeakDominate(problemList.get(i), 200, 300, 200);
			  
			  ((TspGridWeakDominate)algorithm).setRunId(run);
			  ((TspGridWeakDominate)algorithm).setBaseProcedureDirectory(baseProcedureDirectory);
			  
	          algorithms.add(
	        		  new TaggedAlgorithm<List<GridPermutationSolution<Integer>>>(
	        				  algorithm, "GPLS-D", problemList.get(i), run));
	        }
	    }
	    
	    for (int run = 0; run < independentRuns; run++) {
	        for (int i = 0; i < problemList.size(); i++) {
			  Algorithm<List<GridPermutationSolution<Integer>>> algorithm = 
						new TspMoead(problemList.get(i), 200, 300);
			  ((TspMoead)algorithm).setFunctionType(MoeadParetoLocalSearch.FunctionType.AGG);
			  ((TspMoead)algorithm).setRunId(run);
			  ((TspMoead)algorithm).setBaseProcedureDirectory(baseProcedureDirectory);
			  
	          algorithms.add(
	        		  new TaggedAlgorithm<List<GridPermutationSolution<Integer>>>(
	        				  algorithm, "MOEAD-LS(WS)", problemList.get(i), run));
	        }
	    }
	    
	    for (int run = 0; run < independentRuns; run++) {
	        for (int i = 0; i < problemList.size(); i++) {
			  Algorithm<List<GridPermutationSolution<Integer>>> algorithm = 
						new TspMoead(problemList.get(i), 200, 300);
			  ((TspMoead)algorithm).setFunctionType(MoeadParetoLocalSearch.FunctionType.TCHE);
			  ((TspMoead)algorithm).setRunId(run);
			  ((TspMoead)algorithm).setBaseProcedureDirectory(baseProcedureDirectory);
			  
	          algorithms.add(
	        		  new TaggedAlgorithm<List<GridPermutationSolution<Integer>>>(
	        				  algorithm, "MOEAD-LS(TCH)", problemList.get(i), run));
	        }
	    }
	    
	    for (int run = 0; run < independentRuns; run++) {
	        for (int i = 0; i < problemList.size(); i++) {
			  Algorithm<List<GridPermutationSolution<Integer>>> algorithm = 
						new TspMoead(problemList.get(i), 200, 300);
			  ((TspMoead)algorithm).setFunctionType(MoeadParetoLocalSearch.FunctionType.PBI);
			  ((TspMoead)algorithm).setRunId(run);
			  ((TspMoead)algorithm).setBaseProcedureDirectory(baseProcedureDirectory);
			  
	          algorithms.add(
	        		  new TaggedAlgorithm<List<GridPermutationSolution<Integer>>>(
	        				  algorithm, "MOEAD-LS(PBI)", problemList.get(i), run));
	        }
	    }
	    
		return algorithms;
		
	} 
	

}
