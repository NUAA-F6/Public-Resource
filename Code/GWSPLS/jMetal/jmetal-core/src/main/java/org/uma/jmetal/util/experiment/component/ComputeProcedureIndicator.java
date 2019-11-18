package org.uma.jmetal.util.experiment.component;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;

import org.uma.jmetal.qualityindicator.impl.GenericIndicator;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.experiment.Experiment;
import org.uma.jmetal.util.experiment.ExperimentComponent;
import org.uma.jmetal.util.experiment.util.TaggedAlgorithm;
import org.uma.jmetal.util.front.Front;
import org.uma.jmetal.util.front.imp.ArrayFront;
import org.uma.jmetal.util.front.util.FrontNormalizer;
import org.uma.jmetal.util.front.util.FrontUtils;
import org.uma.jmetal.util.point.util.PointSolution;

public class ComputeProcedureIndicator <S extends Solution<?>, Result> implements ExperimentComponent{

	private final Experiment<S, Result> experiment ;
	
	public ComputeProcedureIndicator(Experiment<S, Result> exp) {
		this.experiment = exp;
		this.experiment.removeDuplicatedAlgorithms();
	}

	@Override
	public void run() throws IOException {
		for(GenericIndicator<S> indicator : experiment.getIndicatorList()){
			JMetalLogger.logger.info("Computing indicator: " + indicator.getName());
			for(TaggedAlgorithm<Result> algorithm : experiment.getAlgorithmList()){
				String algorithmDirectory = experiment.getExperimentBaseDirectory() + "/procedure/"
						+algorithm.getTag() ;
				for(int problemId = 0; problemId < experiment.getProblemList().size(); problemId++){
					String problemDirectory = algorithmDirectory + "/"+experiment.getProblemList().get(problemId).getName() ;
					
					String referenceFrontDirectory = experiment.getReferenceFrontDirectory() ;
			        String referenceFrontName = referenceFrontDirectory +
			                "/" + experiment.getProblemList().get(problemId).getName()+".rf" ;
			        JMetalLogger.logger.info("RF: " + referenceFrontName);
			        Front referenceFront = new ArrayFront(referenceFrontName);
			        
			        FrontNormalizer frontNormalizer = new FrontNormalizer(referenceFront);
			        Front normalizedReferenceFront = frontNormalizer.normalize(referenceFront);
			        indicator.setReferenceParetoFront(normalizedReferenceFront);
			        
			        for(int runId = 0;runId<experiment.getIndependentRuns(); runId++){
			        	String runDirectory = problemDirectory + "/RUN_"+ runId ; 
			        	File runFolder = new File(runDirectory);
			        	String[] allFile = runFolder.list(new FilenameFilter() {
							
							@Override
							public boolean accept(File dir, String name) {
								return name.startsWith("FUN");
							}
						});
			        	
			        	String indicatorFile = runDirectory + "/" + indicator.getName();
			        	resetFile(indicatorFile);
			        	for(int cycleId = 0;cycleId<allFile.length;cycleId++){
			        		String frontFile = runDirectory + "/" + 
			        				experiment.getOutputParetoFrontFileName() + cycleId + ".tsv";
			        		Front front = new ArrayFront(frontFile);
			        		Front normalizedFront = frontNormalizer.normalize(front);
			                List<PointSolution> normalizedPopulation = FrontUtils.convertFrontToSolutionList(normalizedFront) ;
			        		
			                @SuppressWarnings("unchecked")
							Double indicatorValue = indicator.evaluate((List<S>) normalizedPopulation);
			        		JMetalLogger.logger.info(indicator.getName() + ": " + indicatorValue) ;
			        		
			        		writeQualityIndicatorValueToFile(indicatorValue, indicatorFile) ;
			        	}
			        }
				}
			}
		}
	}

	private void resetFile(String file) {
		File f = new File(file);
		if (f.exists()) {
			JMetalLogger.logger.info("File " + file + " exist.");

			if (f.isDirectory()) {
				JMetalLogger.logger.info("File " + file + " is a directory. Deleting directory.");
				if (f.delete()) {
					JMetalLogger.logger.info("Directory successfully deleted.");
				} else {
					JMetalLogger.logger.info("Error deleting directory.");
				}
			} else {
				JMetalLogger.logger.info("File " + file + " is a file. Deleting file.");
				if (f.delete()) {
					JMetalLogger.logger.info("File succesfully deleted.");
				} else {
					JMetalLogger.logger.info("Error deleting file.");
				}
			}
		} else {
			JMetalLogger.logger.info("File " + file + " does NOT exist.");
		}
	}
	
	private void writeQualityIndicatorValueToFile(Double indicatorValue, String qualityIndicatorFile) {
		FileWriter os;
		try {
			os = new FileWriter(qualityIndicatorFile, true);
			os.write("" + indicatorValue + "\n");
			os.close();
		} catch (IOException ex) {
			throw new JMetalException("Error writing indicator file" + ex);
		}
	}

}
