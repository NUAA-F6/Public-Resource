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

import org.uma.jmetal.problem.impl.AbstractGridIntegerPermutationProblem;
import org.uma.jmetal.solution.GridPermutationSolution;

@SuppressWarnings("serial")
public class NrpProblem extends AbstractGridIntegerPermutationProblem{

	private int[] costArray;
	private int[] profitArray;
	private List<List<Integer>> customRequireList;
	private int numberOfCustoms;
	private int numberOfRequire;
	
	private List<Integer> sortedCustomIndex;
	
	public NrpProblem(String customRequirePath,String profitPath,String costPath){
		try {
			readProblem(customRequirePath, profitPath, costPath);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Read File Error!");
		}
		setNumberOfObjectives(2);
		setNumberOfVariables(numberOfCustoms);
//		setSortedIndex();
		
	}
	
/*	private void setSortedIndex() {
		List<ToolEmptySolution> selectedList = new ArrayList<ToolEmptySolution>();
		for (int i = 0; i < numberOfCustoms; i++) {
			ToolEmptySolution custom = new ToolEmptySolution(new EmptyProblem(getNumberOfObjectives()), i);
			int[] costAndProfit = getCostAndProfitOfCustom(i);
			custom.setObjective(0, costAndProfit[0]);
			custom.setObjective(1, costAndProfit[1]);
			selectedList.add(custom);
		}
	    Ranking<ToolEmptySolution> ranking = new DominanceRanking<>() ;
	    Ranking<ToolEmptySolution> rank = ranking.computeRanking(selectedList);
	    sortedCustomIndex = new ArrayList<>();
	    for(int i = 0;i<rank.getNumberOfSubfronts();i++){
	    	for(ToolEmptySolution customSolution : rank.getSubfront(i)){
	    		sortedCustomIndex.add(customSolution.getIndex());
	    	}
	    }

	}*/


	public List<Integer> getWorstIndex(GridPermutationSolution<Integer> solution, double p){
	    List<Integer> worstIndexList = new ArrayList<>();
	    int size = (int) (p*solution.getNumberOfVariables());
	    for(int i = sortedCustomIndex.size()-1;i>0&&worstIndexList.size()<size;i--){
	    	if(solution.getVariableValue(sortedCustomIndex.get(i)) == 1){
	    		worstIndexList.add(sortedCustomIndex.get(i));
	    	}
	    }
	    return worstIndexList;
	}
	
	/**
	 * flag = 0代表未选中的最好的前p，flag = 1代表选中的前p
	 * @param solution
	 * @param p
	 * @param flag
	 * @return
	 */
	public List<Integer> getBestIndex(GridPermutationSolution<Integer> solution, double p){
	    List<Integer> bestIndexList = new ArrayList<>();
	    int size = (int) (p*solution.getNumberOfVariables());
	    for(int i = 0;i<sortedCustomIndex.size()&&bestIndexList.size()<size;i++){
	    	if(solution.getVariableValue(sortedCustomIndex.get(i)) == 0){
	    		bestIndexList.add(sortedCustomIndex.get(i));
	    	}
	    }
	    return bestIndexList;
	}
	
	
	public int[] getCostAndProfitOfCustom(int index){
		int[] costAndProfit = new int[2];
		costAndProfit[1] = -profitArray[index];
		List<Integer> requireList = customRequireList.get(index);
		
		int cost = 0;
		for(int i = 0;i<requireList.size();i++){
			cost += costArray[requireList.get(i)-1];
		}
		costAndProfit[0] = cost;
		return costAndProfit;
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
	public int getPermutationLength() {
		return getNumberOfVariables();
	}

	@Override
	public void evaluate(GridPermutationSolution<Integer> solution) {
		int profit = 0;
		Set<Integer> requireSet = new HashSet<>();
		for(int i = 0;i<solution.getNumberOfVariables();i++){
			if(solution.getVariableValue(i) == 1){
				profit += profitArray[i];
				List<Integer> requireList = customRequireList.get(i);
				for(int j = 0;j<requireList.size();j++){
					requireSet.add(requireList.get(j));
				}
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
	
	
	@Override
	public GridPermutationSolution<Integer> createSolution() {
		GridPermutationSolution<Integer> solution = super.createSolution();
		
		for(int i = 0;i<solution.getNumberOfVariables();i++){
			if(Math.random()<0.5){
				solution.setVariableValue(i, 0);
			}else {
				solution.setVariableValue(i, 1);
			}
		}
		repairSolution(solution);
		return solution;
		
	}

	public void repairSolution(GridPermutationSolution<Integer> solution) {
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
		
	}
	
	public void setName(String name){
		super.setName(name);
		
	}
	
}
