package org.uma.jmetal.util.experiment.component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.experiment.Experiment;
import org.uma.jmetal.util.experiment.ExperimentComponent;
import org.uma.jmetal.util.experiment.util.TaggedAlgorithm;

public class ComputeProcedureIndicatorMean<S extends Solution<?>, Result> implements ExperimentComponent{

	private double indicators[][][][][];
	
	private final Experiment<S, Result> experiment ;
	
	public ComputeProcedureIndicatorMean(Experiment<S, Result> exp) {
		this.experiment = exp;
		this.experiment.removeDuplicatedAlgorithms();
	}
	
	@Override
	public void run() throws IOException {
		int indicatorNumbers = experiment.getIndicatorList().size();
		int algorithmNumbers = experiment.getAlgorithmList().size();
		int problemsNumbers = experiment.getProblemList().size();
		int independentRuns = experiment.getIndependentRuns();
		int cycleNumbers = 300;
		
		indicators = new double[indicatorNumbers][algorithmNumbers]
				[problemsNumbers][independentRuns][cycleNumbers];
		readData(indicatorNumbers, algorithmNumbers, problemsNumbers, independentRuns,cycleNumbers);
		calculateMean(indicatorNumbers, algorithmNumbers, problemsNumbers, independentRuns,cycleNumbers);
	}

	
	private void calculateMean(int indicatorNumbers, int algorithmNumbers, int problemsNumbers, int independentRuns,
			int cycleNumbers) {

		for (int indicator = 0; indicator < indicatorNumbers; indicator++) {
			JMetalLogger.logger.info("compute " + experiment.getIndicatorList().get(indicator).getName() + " start...");
			for (int algorithm = 0; algorithm < algorithmNumbers; algorithm++) {
				TaggedAlgorithm<Result> algorithmObj = experiment.getAlgorithmList().get(algorithm);
				String algorithmDirectory = experiment.getExperimentBaseDirectory() + "/procedure/"
						+ algorithmObj.getTag();
				for (int problem = 0; problem < problemsNumbers; problem++) {
					Problem<S> problemObj = experiment.getProblemList().get(problem);
					String problemDirectory = algorithmDirectory + "/"+ problemObj.getName();
					
					String indicatorName = experiment.getIndicatorList().get(indicator).getName();
					String indicatorFileName = problemDirectory+"/MEAN_"+indicatorName;
					resetFile(indicatorFileName);
					for(int cycle = 0;cycle<cycleNumbers;cycle++){
						DescriptiveStatistics stats = new DescriptiveStatistics();
						for(int run = 0;run<independentRuns;run++){
							stats.addValue(indicators[indicator][algorithm][problem][run][cycle]);
						}
						FileWriter os;
						try {
							os = new FileWriter(indicatorFileName, true);
							double meanValue = stats.getMean();
							os.write("" + meanValue + "\n");
							JMetalLogger.logger.info("write "+indicatorName+": "+meanValue);
							os.close();
						} catch (IOException ex) {
							throw new JMetalException("Error writing indicator file" + ex);
						}
					}
				}
			}
		}
	}

	private void readData(int indicatorNumbers, int algorithmNumbers, int problemsNumbers, int independentRuns,int cycleNumbers)
			throws FileNotFoundException, IOException {
		for(int indicator = 0;indicator<indicatorNumbers;indicator++){
			JMetalLogger.logger.info("Read " + experiment.getIndicatorList().get(indicator).getName()+" start...");
			for(int algorithm = 0;algorithm<algorithmNumbers;algorithm++){
				TaggedAlgorithm<Result> algorithmObj = experiment.getAlgorithmList().get(algorithm);
				String algorithmDirectory = experiment.getExperimentBaseDirectory() + "/procedure/"
						+ algorithmObj.getTag() ;
				
				for(int problem = 0;problem<problemsNumbers;problem++){
					Problem<S> problemObj = experiment.getProblemList().get(problem);
					String problemDirectory = algorithmDirectory + "/"+ problemObj.getName();
					for(int run = 0;run<independentRuns;run++){
						String runDirectory = problemDirectory+"/RUN_"+run;
						String indicatorName = experiment.getIndicatorList().get(indicator).getName();
						String fileName = runDirectory + "/" +indicatorName;
						FileInputStream fis = new FileInputStream(fileName);
						InputStreamReader isr = new InputStreamReader(fis);
						BufferedReader br = new BufferedReader(isr);
						String aux = br.readLine();
						int cycle = 0;
						JMetalLogger.logger.info("Read: "+fileName);
						while (aux != null && cycle<cycleNumbers) {
							JMetalLogger.logger.info(indicatorName+": "+aux);
							indicators[indicator][algorithm][problem][run][cycle++] = Double.parseDouble(aux);
							aux = br.readLine();
						}
						br.close();
						
						if (cycle < cycleNumbers) {
							String finalIndicatorFile = experiment.getExperimentBaseDirectory() + "/data/"
									+ algorithmObj.getTag() + "/" + problemObj.getName() + "/" + indicatorName;
							List<String> finalIndicatorStr = Files.readAllLines(Paths.get(finalIndicatorFile));
							DescriptiveStatistics stats = new DescriptiveStatistics();
							for(int i = 0;i<finalIndicatorStr.size();i++){
								stats.addValue(Double.parseDouble(finalIndicatorStr.get(i)));
							}
							double finalMeanIndicator = stats.getMean();
							for (; cycle < cycleNumbers; cycle++) {
								indicators[indicator][algorithm][problem][run][cycle] = finalMeanIndicator;
							}
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
	
}
