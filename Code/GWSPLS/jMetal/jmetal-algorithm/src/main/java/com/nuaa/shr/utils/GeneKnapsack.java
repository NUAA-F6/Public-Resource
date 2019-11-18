package com.nuaa.shr.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;
import java.util.logging.Logger;

public class GeneKnapsack {
	public static void main(String[] args) throws IOException {
		Logger log = Logger.getLogger("Generate Knapsack Instance");
		generateKnapsackInstance(250, 7);
		log.info("Generate end!");
	}

	private static void generateKnapsackInstance(int numberOfItems, int numberOfObjectives) throws IOException {
		String dir = "test-data/" + numberOfItems + "_" + numberOfObjectives ;
		File folder = new File(dir);
		if(!folder.exists() && !folder.isDirectory()){
			folder.mkdirs();
		}
		geneProfitFile(numberOfItems, numberOfObjectives, dir);
		
		geneWeightAndCapacityFile(numberOfItems, numberOfObjectives, dir);
	}

	private static void geneWeightAndCapacityFile(int numberOfItems, int numberOfObjectives, String dir)
			throws IOException {
		String weightName = dir + "/weight" + numberOfItems+"_"+numberOfObjectives+".txt";
		
		String capacityName = dir + "/capacity" + numberOfItems+"_"+numberOfObjectives+".txt";
		
		OutputStream weightOut = new FileOutputStream(weightName);
		OutputStream capacityOut = new FileOutputStream(capacityName);
		Random random = new Random();
		for(int i = 0;i<numberOfObjectives;i++){
			int capacity = 0;
			for(int j = 0;j<numberOfItems;j++){
				int weight = random.nextInt(91)+10;
				capacity += weight;
				weightOut.write((weight+"\n").getBytes());
			}
			capacity = (int) (0.5 * capacity) ;
			capacityOut.write((capacity + "\n").getBytes());
		}
		
		weightOut.close();
		capacityOut.close();
		
	}

	private static void geneProfitFile(int numberOfItems, int numberOfObjectives, String dir)
			throws IOException {
		String profitName = dir + "/profit" + numberOfItems+"_"+numberOfObjectives+".txt";
		File profigFile = new File(profitName);
		OutputStream out = new FileOutputStream(profigFile);
		Random random = new Random();
		for(int i = 0;i<numberOfObjectives;i++){
			for(int j = 0;j<numberOfItems;j++){
				int profit = random.nextInt(91)+10;
				out.write((profit+"\n").getBytes());
			}
		}
		out.close();
		
	}
}
