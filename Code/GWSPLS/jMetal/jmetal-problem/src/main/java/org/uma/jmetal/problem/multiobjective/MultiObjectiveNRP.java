package org.uma.jmetal.problem.multiobjective;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.uma.jmetal.problem.impl.AbstractIntegerPermutationProblem;
import org.uma.jmetal.solution.PermutationSolution;

@SuppressWarnings("serial")
public class MultiObjectiveNRP extends AbstractIntegerPermutationProblem{

	private int[] costArray;
	private int[] profitArray;
	private List<List<Integer>> customRequireList;
	private int numberOfCustoms;
	private int numberOfRequire;
	
	public MultiObjectiveNRP(String customRequirePath,String profitPath,String costPath, String name) {
		try {
			readProblem(customRequirePath, profitPath, costPath);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Read File Error!");
		}
		setNumberOfObjectives(2);
		setNumberOfVariables(numberOfCustoms);
		setName(name);
	}
	
	@Override
	public int getPermutationLength() {
		return getNumberOfVariables();
	}

	@Override
	public void evaluate(PermutationSolution<Integer> solution) {
		Set<Integer>requireSet = repairSolution(solution);
		int profit = 0;
		for(int i = 0;i<solution.getNumberOfVariables();i++){
			if(solution.getVariableValue(i) == 1){
				profit += profitArray[i];
			}
		}
		solution.setObjective(0, -profit);
		
		int cost = 0;
		Iterator<Integer> itr = requireSet.iterator();
		while(itr.hasNext()){
			cost += costArray[itr.next()-1];
		}
		
		solution.setObjective(1, cost);
		
	}
	
	private void readProblem(String customRequirePath, String profitPath, String costPath) 
			throws IOException {
		InputStream in = getClass().getResourceAsStream(profitPath);
		InputStreamReader isr = new InputStreamReader(in);
		BufferedReader br = new BufferedReader(isr);
		
		String profitString = br.readLine();
		String[] profitStrArray = profitString.split("\\s+");
		numberOfCustoms = profitStrArray.length;
		profitArray = new int[numberOfCustoms];
		for(int i = 0;i<profitStrArray.length;i++){
			profitArray[i] = Integer.parseInt(profitStrArray[i]);
		}
		
		in = getClass().getResourceAsStream(costPath);
		isr = new InputStreamReader(in);
		br = new BufferedReader(isr);
		String costString = br.readLine();
		String[] costStrArray = costString.split("\\s+");
		numberOfRequire = costStrArray.length;
		costArray = new int[numberOfRequire];
		for(int i = 0;i<costStrArray.length;i++){
			costArray[i] = Integer.parseInt(costStrArray[i]);
		}
		
		in = getClass().getResourceAsStream(customRequirePath);
		isr = new InputStreamReader(in);
		br = new BufferedReader(isr);
		customRequireList = new ArrayList<>();
		for(int i = 0;i<numberOfCustoms;i++){
			String customRequireStr = br.readLine();
			String [] customRequires = customRequireStr.split("\\s+");
			List<Integer> requireList = new ArrayList<>();
			for(int j = 0;j<customRequires.length;j++){
				requireList.add(Integer.parseInt(customRequires[j]));
			}
			customRequireList.add(requireList);
		}
		br.close();
		
	}
	
	@Override
	public PermutationSolution<Integer> createSolution() {
		PermutationSolution<Integer> solution = super.createSolution();
		
		for(int i = 0;i<solution.getNumberOfVariables();i++){
			if(Math.random()<0.5){
				solution.setVariableValue(i, 0);
			}else {
				solution.setVariableValue(i, 1);
			}
		}
		return solution;
		
	}
	
	private Set<Integer> repairSolution(PermutationSolution<Integer> solution) {
		Set<Integer> requireSet = new HashSet<>();
		for(int i = 0;i<solution.getNumberOfVariables();i++){
			if(solution.getVariableValue(i) == 1){
				List<Integer> requireList = customRequireList.get(i);
				requireSet.addAll(requireList);
			}
		}
		for(int i = 0;i<solution.getNumberOfVariables();i++){
			if(requireSet.containsAll(customRequireList.get(i))){
				solution.setVariableValue(i, 1);
			}else solution.setVariableValue(i,0);
		}
		return requireSet;
		
	}

}
