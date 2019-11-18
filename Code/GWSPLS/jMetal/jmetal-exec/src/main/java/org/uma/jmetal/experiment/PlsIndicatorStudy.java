package org.uma.jmetal.experiment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.experiment.emptyobj.EmptyAlgorithm;
import org.uma.jmetal.experiment.emptyobj.EmptyProblem4ResetName;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.qualityindicator.impl.Epsilon;
import org.uma.jmetal.qualityindicator.impl.GenerationalDistance;
import org.uma.jmetal.qualityindicator.impl.InvertedGenerationalDistance;
import org.uma.jmetal.qualityindicator.impl.InvertedGenerationalDistancePlus;
import org.uma.jmetal.qualityindicator.impl.Spread;
import org.uma.jmetal.qualityindicator.impl.hypervolume.PISAHypervolume;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.experiment.Experiment;
import org.uma.jmetal.util.experiment.ExperimentBuilder;
import org.uma.jmetal.util.experiment.component.ComputeProcedureIndicator;
import org.uma.jmetal.util.experiment.component.ComputeProcedureIndicatorMean;
import org.uma.jmetal.util.experiment.component.ComputeQualityIndicators;
import org.uma.jmetal.util.experiment.component.GenerateBoxplotsWithR;
import org.uma.jmetal.util.experiment.component.GenerateFriedmanTestTables;
import org.uma.jmetal.util.experiment.component.GenerateLatexTablesWithStatistics;
import org.uma.jmetal.util.experiment.component.GenerateReferenceParetoFront;
import org.uma.jmetal.util.experiment.component.GenerateWilcoxonTestTablesWithR;
import org.uma.jmetal.util.experiment.util.TaggedAlgorithm;



public class PlsIndicatorStudy {
	private static final int INDIPENDENT_RUNS = 30;
	
	public static void main(String[] args) throws IOException {
	    String experimentBaseDirectory = "E:/sevn/experiment/data/pls_multi_indicator" ;
//	    String experimentBaseDirectory = "F:/sevn/experiment/data/gene_pf" ;
	    String paretoFrontDirectoty = experimentBaseDirectory+"/pfs";
	    List<Problem<DoubleSolution>> problemList = configProblems();
	    List<TaggedAlgorithm<List<DoubleSolution>>> algorithms = 
	    		configAlgorithms(problemList, INDIPENDENT_RUNS);
	    
	    Experiment<DoubleSolution,List<DoubleSolution>> experiment =
	    		new ExperimentBuilder<DoubleSolution,List<DoubleSolution>>("plsStudy")
	    		.setAlgorithmList(algorithms)
	    		.setProblemList(problemList)
	    		.setExperimentBaseDirectory(experimentBaseDirectory)
	    		.setReferenceFrontDirectory(paretoFrontDirectoty)
	    		.setIndependentRuns(INDIPENDENT_RUNS)
	    		.setOutputParetoSetFileName("VAR")
	    		.setOutputParetoFrontFileName("FUN")
	            .setIndicatorList(Arrays.asList(
	                    new Epsilon<DoubleSolution>(), new Spread<DoubleSolution>(), new GenerationalDistance<DoubleSolution>(),
	                    new PISAHypervolume<DoubleSolution>(),
	                    new InvertedGenerationalDistance<DoubleSolution>(),
	                    new InvertedGenerationalDistancePlus<DoubleSolution>()))
	            .setNumberOfCores(3)
	            .build();
	    
	    new GenerateReferenceParetoFront(experiment).run();
	    new ComputeQualityIndicators<>(experiment).run() ;
	    new ComputeProcedureIndicator<>(experiment).run();
	    new ComputeProcedureIndicatorMean<>(experiment).run();
	    
	    new GenerateLatexTablesWithStatistics(experiment).run() ;
	    new GenerateWilcoxonTestTablesWithR<>(experiment).run() ;
	    new GenerateFriedmanTestTables<>(experiment).run();
	    new GenerateBoxplotsWithR<>(experiment).setRows(3).setColumns(3).setDisplayNotch().run() ;
	    
	    		
	}

	static List<Problem<DoubleSolution>> configProblems() {
		List<Problem<DoubleSolution>> problemList = new ArrayList<>();
		
		/*nrp problem start*/
//		for (int i = 0; i < 4; i++) {
//			EmptyProblem4ResetName problem = new EmptyProblem4ResetName();
//			problem.setName("e" + (i + 1));
//			problemList.add(problem);
//		}
//		for (int i = 0; i < 4; i++) {
//			EmptyProblem4ResetName problem = new EmptyProblem4ResetName();
//			problem.setName("g" + (i + 1));
//			problemList.add(problem);
//		}
		/*TSP problem start*/
		for(int item = 0;item<3;item++){
			EmptyProblem4ResetName kroProblem = new EmptyProblem4ResetName();
			kroProblem.setName("kroAB"+100*(item+1));
			problemList.add(kroProblem);
		}
		
		for(int item = 0;item<3;item = item+2){
			EmptyProblem4ResetName euclidProblem = new EmptyProblem4ResetName();
			euclidProblem.setName("euclidAB"+100*(item+1));
			problemList.add(euclidProblem);
		}
		
		
		for(int item = 0;item<3;item = item+2){
			EmptyProblem4ResetName clusterProblem = new EmptyProblem4ResetName();
			clusterProblem.setName("ClusterAB"+100*(item+1));
			problemList.add(clusterProblem);
		}
		
		EmptyProblem4ResetName kro3Problem = new EmptyProblem4ResetName();
		kro3Problem.setName("kroABC100");
		problemList.add(kro3Problem);
		
		EmptyProblem4ResetName euclid3Problem = new EmptyProblem4ResetName();
		euclid3Problem.setName("euclidABC100");
		problemList.add(euclid3Problem);
		
		
		/*knapsack problem start*/
		for(int dimension = 0;dimension < 2;dimension++){
			for(int item = 0;item<3;item++){
				EmptyProblem4ResetName knapsackProblem = new EmptyProblem4ResetName();
				knapsackProblem.setName("knapsack"+250*(item+1)+"_"+(dimension+2));
				problemList.add(knapsackProblem);
			}
		}

		return problemList;
	}

	static List<TaggedAlgorithm<List<DoubleSolution>>> configAlgorithms(
			List<Problem<DoubleSolution>> problemList, int independentRuns){
		
		List<TaggedAlgorithm<List<DoubleSolution>>> algorithms = new ArrayList<>();
		
		String[] algorithmNames = {"MOEAD-LS(WS)","MOEAD-LS(TCH)","MOEAD-LS(PBI)","EpsilonMOEA","GPLS-D"};
//		String[] algorithmNames = {"PLS", "MOMAD", "GPLS-D"};
//		String[] algorithmNames = {"GPLS-MD","GPLS-GWS"} ;
		String baseProcedureDirectory = "E:/sevn/experiment/data/pls_multi_indicator/plsStudy/procedure";
		for (int algNumber = 0;algNumber<algorithmNames.length;algNumber++)
		{
			String algorithmName = algorithmNames[algNumber];
		    for (int run = 0; run < independentRuns; run++) {
		        for (int i = 0; i < problemList.size(); i++) {
				  Algorithm<List<DoubleSolution>> algorithm = 
							new EmptyAlgorithm<DoubleSolution>(problemList.get(i), algorithmName);
				  
				  ((EmptyAlgorithm<DoubleSolution>)algorithm).setRunId(run);
				  ((EmptyAlgorithm<DoubleSolution>)algorithm).setBaseProcedureDirectory(baseProcedureDirectory);
				  
		          algorithms.add(
		        		  new TaggedAlgorithm<List<DoubleSolution>>(
		        				  algorithm, algorithmName, problemList.get(i), run));
		        }
		    }
		}
		return algorithms;
	} 
	

}
