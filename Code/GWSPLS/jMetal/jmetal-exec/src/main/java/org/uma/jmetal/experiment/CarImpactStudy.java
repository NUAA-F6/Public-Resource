package org.uma.jmetal.experiment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.uma.jmetal.experiment.emptyobj.EmptyAlgorithm;
import org.uma.jmetal.experiment.emptyobj.EmptyProblem4ResetName;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.qualityindicator.impl.Epsilon;
import org.uma.jmetal.qualityindicator.impl.GenerationalDistance;
import org.uma.jmetal.qualityindicator.impl.InvertedGenerationalDistance;
import org.uma.jmetal.qualityindicator.impl.InvertedGenerationalDistancePlus;
import org.uma.jmetal.qualityindicator.impl.Spread;
import org.uma.jmetal.qualityindicator.impl.hypervolume.WFGHypervolume;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.experiment.Experiment;
import org.uma.jmetal.util.experiment.ExperimentBuilder;
import org.uma.jmetal.util.experiment.component.ComputeQualityIndicators;
import org.uma.jmetal.util.experiment.component.GenerateBoxplotsWithR;
import org.uma.jmetal.util.experiment.component.GenerateFriedmanTestTables;
import org.uma.jmetal.util.experiment.component.GenerateLatexTablesWithStatistics;
import org.uma.jmetal.util.experiment.component.GenerateReferenceParetoFront;
import org.uma.jmetal.util.experiment.component.GenerateWilcoxonTestTablesWithR;
import org.uma.jmetal.util.experiment.util.TaggedAlgorithm;

public class CarImpactStudy {
	private static final int INDEPENDENT_RUNS = 30 ;
	private static final String BASE_DIRECT = 
			"F:/sevn/all_workspace/workspace/data/DIR" ;
	private static final String PARETO_DIRECT = 
			"F:/sevn/all_workspace/workspace/data/DIR/crashWorthinessStudy/pf" ;
	
	public static void main(String[] args) throws IOException {
		EmptyProblem4ResetName problem = new EmptyProblem4ResetName();
		problem.setName("crash_worthiness");
		List<Problem<DoubleSolution>> problemList = Arrays.<Problem<DoubleSolution>>asList(problem) ;
	    List<TaggedAlgorithm<List<DoubleSolution>>> algorithmList = configureAlgorithmList(problemList, INDEPENDENT_RUNS) ;
	    
	    Experiment<DoubleSolution,List<DoubleSolution>> experiment =
	    		new ExperimentBuilder<DoubleSolution,List<DoubleSolution>>("crashWorthinessStudy")
	    		.setAlgorithmList(algorithmList)
	    		.setProblemList(problemList)
	    		.setExperimentBaseDirectory(BASE_DIRECT)
	    		.setReferenceFrontDirectory(PARETO_DIRECT)
	    		.setIndependentRuns(INDEPENDENT_RUNS)
	    		.setOutputParetoSetFileName("VAR_")
	    		.setOutputParetoFrontFileName("FUN_")
	            .setIndicatorList(Arrays.asList(
	                    new Epsilon<DoubleSolution>(), new Spread<DoubleSolution>(), new GenerationalDistance<DoubleSolution>(),
	                    new WFGHypervolume<DoubleSolution>(),
	                    new InvertedGenerationalDistance<DoubleSolution>(),
	                    new InvertedGenerationalDistancePlus<DoubleSolution>()))
	            .setNumberOfCores(3)
	            .build();
	    
	    new GenerateReferenceParetoFront(experiment).run();
	    new ComputeQualityIndicators<>(experiment).run() ;
	    new GenerateLatexTablesWithStatistics(experiment).run() ;
	    new GenerateWilcoxonTestTablesWithR<>(experiment).run() ;
	    new GenerateFriedmanTestTables<>(experiment).run();
	    new GenerateBoxplotsWithR<>(experiment).setRows(3).setColumns(3).setDisplayNotch().run() ;
		
	}

	private static List<TaggedAlgorithm<List<DoubleSolution>>> configureAlgorithmList(
			List<Problem<DoubleSolution>> problemList, int independentRuns) {
		List<TaggedAlgorithm<List<DoubleSolution>>> algorithms = new ArrayList<>();
		String[] algorithmNames = {"NSGAII","NSGAIII","DNSGAII"};
		for (int algNumber = 0;algNumber<algorithmNames.length;algNumber++)
		{
			String algorithmName = algorithmNames[algNumber];
		    for (int run = 0; run < independentRuns; run++) {
		        for (int i = 0; i < problemList.size(); i++) {
		          algorithms.add(
		        		  new TaggedAlgorithm<List<DoubleSolution>>(
		        				  new EmptyAlgorithm<>(problemList.get(i), algorithmName), 
		        				  algorithmName, problemList.get(i), run));
		        }
		    }
		}
		
		return algorithms ;
	}
}
