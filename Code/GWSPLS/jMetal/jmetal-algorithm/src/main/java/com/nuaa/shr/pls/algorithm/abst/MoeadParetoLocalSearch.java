package com.nuaa.shr.pls.algorithm.abst;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.StringTokenizer;

import org.uma.jmetal.algorithm.multiobjective.moead.util.MOEADUtils;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.GridPermutationSolution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.fileoutput.SolutionListOutput;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;

@SuppressWarnings("serial")
public abstract class MoeadParetoLocalSearch extends AbstractParetoLocalSearch<GridPermutationSolution<Integer>>{
	private String baseProcedureDirectory = "results/procedure";
	
	public enum FunctionType {
		TCHE, PBI, AGG
	};

	public FunctionType functionType = FunctionType.TCHE;
	
	public MoeadParetoLocalSearch(Problem<GridPermutationSolution<Integer>> problem, int maxIteration,
			int populationSize) {
		super(problem, maxIteration, populationSize);
		lambda = new double[populationSize][problem.getNumberOfObjectives()];
		neighborhood = new int[populationSize][neighborSize];
	}

	protected double[][] lambda;
	protected int[][] neighborhood;
	protected boolean isUpdate;
	
	protected int neighborSize = 20;
	protected int iteration = 0;
	protected int updateCount = 0;
	
	@Override
	public void run() {
		initializeUniformWeight();
		initializeNeighborhood();
		List<GridPermutationSolution<Integer>> initPopulation = createInitialPopulation();
		evaluator.evaluate(initPopulation, problem);
		workPopulation = initPopulation;
		printWorkPopulation(0);
		initialNadirPoint();
		initializeIdealPoint();
		updateSomething();
//		while(iteration++<maxIteration){
			do {
				
				isUpdate = false;
				int[] permutation = new int[populationSize];
				MOEADUtils.randomPermutation(permutation, populationSize);
				for (int i = 0; i < permutation.length; i++) {
//					searchCount++;
					searchAndUpdateSubProblem(permutation[i], workPopulation);
					if(searchCount % 50 == 0){
						printWorkPopulation(searchCount / 50) ;
					}
				}
				updateSomething();
				globalSearch();
				System.out.println(iteration+" iteration...");
			} while (iteration++ < maxIteration && isUpdate);
			StringBuffer filePath = new StringBuffer(this.baseProcedureDirectory)
					.append("/")
					.append(this.getName())
					.append("/")
					.append(problem.getName())
					.append("/RUN_")
					.append(this.getRunId()).append("/SEARCH_COUNT");
		    FileWriter os;
		    try {
		      os = new FileWriter(filePath.toString());
		      os.write("" + searchCount + "\n");
		      os.close();
		    } catch (IOException ex) {
		      throw new JMetalException("Error writing search count file" + ex) ;
		    }
			
	}
	
	public void setBaseProcedureDirectory(String baseProcedureDirectory){
		this.baseProcedureDirectory = baseProcedureDirectory;
	}
	
	private void printWorkPopulation(int position) {
		StringBuffer folderPath = new StringBuffer(this.baseProcedureDirectory)
				.append("/")
				.append(this.getName())
				.append("/")
				.append(problem.getName())
				.append("/RUN_")
				.append(this.getRunId());
		if(position == 0){
			File folder = new File(folderPath.toString());
			if(!folder.exists()||!folder.isDirectory()){
				folder.mkdirs();
			}
		}
		String filePath = 
				folderPath
				.append("/FUN")
				.append(position)
				.append(".tsv").toString();
		
		new SolutionListOutput(workPopulation)
			.setSeparator("\t")
			.setFunFileOutputContext(new DefaultFileOutputContext(filePath))
			.print();
	}


	protected abstract void globalSearch();

	protected abstract void updateSomething();

	protected abstract void searchAndUpdateSubProblem(int i, List<GridPermutationSolution<Integer>> workPopulation);

	protected double fitnessFunction(GridPermutationSolution<Integer> individual, double[] lambda) throws JMetalException {
		double fitness;

		if (FunctionType.TCHE.equals(functionType)) {
			double maxFun = -1.0e+30;

			for (int n = 0; n < problem.getNumberOfObjectives(); n++) {
				double diff = Math.abs(individual.getObjective(n) - idealPoint[n]);

				double feval;
				if (lambda[n] == 0) {
					feval = 0.0001 * diff;
				} else {
					feval = diff * lambda[n];
				}
				if (feval > maxFun) {
					maxFun = feval;
				}
			}

			fitness = maxFun;
		} else if (FunctionType.AGG.equals(functionType)) {
			double sum = 0.0;
			for (int n = 0; n < problem.getNumberOfObjectives(); n++) {
				sum += (lambda[n]) * individual.getObjective(n);
			}

			fitness = sum;

		} else if (FunctionType.PBI.equals(functionType)) {
			double d1, d2, nl;
			double theta = 5.0;

			d1 = d2 = nl = 0.0;

			for (int i = 0; i < problem.getNumberOfObjectives(); i++) {
				d1 += (individual.getObjective(i) - idealPoint[i]) * lambda[i];
				nl += Math.pow(lambda[i], 2.0);
			}
			nl = Math.sqrt(nl);
			d1 = Math.abs(d1) / nl;

			for (int i = 0; i < problem.getNumberOfObjectives(); i++) {
				d2 += Math.pow((individual.getObjective(i) - idealPoint[i]) - d1 * (lambda[i] / nl), 2.0);
			}
			d2 = Math.sqrt(d2);

			fitness = (d1 + theta * d2);
		} else {
			throw new JMetalException(" MOEAD.fitnessFunction: unknown type " + functionType);
		}
		return fitness;
	}
	
	/**
	 * Initialize neighborhoods
	 */
	protected void initializeNeighborhood() {
		double[] x = new double[populationSize];
		int[] idx = new int[populationSize];

		for (int i = 0; i < populationSize; i++) {
			// calculate the distances based on weight vectors
			for (int j = 0; j < populationSize; j++) {
				x[j] = MOEADUtils.distVector(lambda[i], lambda[j]);
				idx[j] = j;
			}

			// find 'niche' nearest neighboring subproblems
			MOEADUtils.minFastSort(x, idx, populationSize, neighborSize);

			System.arraycopy(idx, 0, neighborhood[i], 0, neighborSize);
		}
	}

	protected void initializeUniformWeight() {
		String dataDirectory = "MOEAD_Weights";
		String dataFileName;
		dataFileName = "W" + problem.getNumberOfObjectives() + "D_" + populationSize + ".dat";

		try {
			InputStream in = getClass().getResourceAsStream("/" + dataDirectory + "/" + dataFileName);
			InputStreamReader isr = new InputStreamReader(in);
			BufferedReader br = new BufferedReader(isr);

			int i = 0;
			int j = 0;
			String aux = br.readLine();
			while (aux != null) {
				StringTokenizer st = new StringTokenizer(aux);
				j = 0;
				while (st.hasMoreTokens()) {
					double value = new Double(st.nextToken());
					lambda[i][j] = value;
					j++;
				}
				aux = br.readLine();
				i++;
			}
			br.close();
		} catch (Exception e) {
			throw new JMetalException(
					"initializeUniformWeight: failed when reading for file: " + dataDirectory + "/" + dataFileName, e);
		}
	}
	
	public void setFunctionType(FunctionType functionType) {
		this.functionType = functionType;
	}
	
	@Override
	public String getName() {
		return "MOEAD-LS("+functionType+")";
	}

	@Override
	public String getDescription() {
		return "moead-pls";
	}
	
	@Override
	public List<GridPermutationSolution<Integer>> getResult() {
		return workPopulation;
	}

}
