package org.uma.jmetal.runner.multiobjective.pls.utils;

import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.problem.multiobjective.ManyObjectiveKnapsack;
import org.uma.jmetal.problem.multiobjective.ManyObjectiveQAP;
import org.uma.jmetal.problem.multiobjective.ManyObjectiveTSP;
import org.uma.jmetal.problem.multiobjective.MultiObjectiveNRP;
import org.uma.jmetal.solution.PermutationSolution;

public class ProblemFactory {

	public static Problem<PermutationSolution<Integer>> getKnapsack(int itemNumber, int dimension) {
		String profitFile = "/knapsack/profit"+itemNumber+"_"+dimension+".txt" ;
		String weightFile = "/knapsack/weight"+itemNumber+"_"+dimension+".txt" ;
		String capacityFile = "/knapsack/capacity"+itemNumber+"_"+dimension+".txt" ;
		String name = "knapsack"+itemNumber+"_"+dimension;
		return new ManyObjectiveKnapsack
				(profitFile, weightFile, capacityFile, itemNumber, dimension, name);
	}

	public static Problem<PermutationSolution<Integer>> getNRP(String name) {
		String requireFile = "/nrp/"+name+"-custom-require.dat" ;
		String profitFile = "/nrp/"+name+"-profit.dat" ;
		String costFile = "/nrp/"+name+"-cost.dat" ;
		String problemName = "nrp_"+name;
		return new MultiObjectiveNRP(requireFile, profitFile, costFile, problemName);
	}

	public static Problem<PermutationSolution<Integer>> getTSP(String name, 
			int numberOfVariables, int numberOfObjective) {
		String[] tspPaths = new String[numberOfObjective];
		String objs = "";
		for(int i = 0;i<tspPaths.length;i++){
			int diff = 'A' - 0;
			char obj = (char) (i + diff);
			objs += obj;
			tspPaths[i] = "/tspInstances/"+name+obj+numberOfVariables+".tsp";
		}
		return new ManyObjectiveTSP(tspPaths, name+objs+numberOfVariables+"");
		
	}
	public static Problem<PermutationSolution<Integer>> getQAP(
			int numberOfVariables, int numberOfObjective) {
		String locationPath = "/qap_instances/location" + numberOfVariables + ".txt" ;
		String[] facilityPaths = new String[numberOfObjective]; 
		for(int i = 0;i<facilityPaths.length;i++){
			facilityPaths[i] = "/qap_instances/facility" + numberOfVariables + "_" + (i+1) +".txt";
		}
		
		return new ManyObjectiveQAP(locationPath, facilityPaths, "QAP"+numberOfVariables+"_"+numberOfObjective) ;
	}
	
}
