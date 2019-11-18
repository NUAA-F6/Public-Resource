package com.nuaa.shr.test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import org.uma.jmetal.util.JMetalException;

import com.nuaa.shr.pls.WekaUtils;

public class Test {
	public static void main(String[] args) {
		WekaUtils utils = new WekaUtils();
		double[][] objectiveMatrix = getObjectiveValuesFromFile("/test-data/FUN60", 200, 2);
		try {
			System.out.println(utils.svmLearning(objectiveMatrix));
			double[][] testObjectiveMatrix = getTestObjectiveMatrix("/test-data/FUN61","/test-data/FUN59",100,2);
			System.out.println(utils.testModel(testObjectiveMatrix));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private static double[][] getTestObjectiveMatrix(String positiveDataFile, String negativeDataFile, int size, int numberOfObjectives) {
		try{
			double[][] positiveData = getTestObjectivesFromFile(positiveDataFile,size,numberOfObjectives,0);
			double[][] negativeData = getTestObjectivesFromFile(negativeDataFile,size,numberOfObjectives,1);
			double[][] testData = new double[2*size][numberOfObjectives+1];
			int i=0;
			while(i<size){
				testData[i] = positiveData[i];
				testData[i+size] = negativeData[i];
				i++;
			}
			return testData;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}


	private static double[][] getTestObjectivesFromFile(String path,int size, int numberOfObjectives,int label) {
		try {
			InputStream in = new Test().getClass().getResourceAsStream(path);
			InputStreamReader isr = new InputStreamReader(in);
			BufferedReader br = new BufferedReader(isr);

			int i = 0;
			String aux = br.readLine();
			double[][] objectiveValues = new double[size][numberOfObjectives+1];
			
			while (aux != null && i<size) {
				StringTokenizer st = new StringTokenizer(aux);
				int j;
				for(j = 0;j<numberOfObjectives;j++){
					if(st.hasMoreTokens()){
						double value = new Double(st.nextToken());
						objectiveValues[i][j] = value;
					}
				}
				objectiveValues[i][j] = label;
				aux = br.readLine();
				i++;
			}
			br.close();
			return objectiveValues;

		} catch (Exception e) {
			throw new JMetalException("getObjectiveValuesFromFile: failed when reading for file: " + path, e);
		}
	}


	private static double[][] getObjectiveValuesFromFile(String path, int size, int numberOfObjectives) {
		try {
			InputStream in = new Test().getClass().getResourceAsStream(path);
			InputStreamReader isr = new InputStreamReader(in);
			BufferedReader br = new BufferedReader(isr);

			int i = 0;
			int j = 0;
			String aux = br.readLine();
			double[][] objectiveValues = new double[size][numberOfObjectives];
			while (aux != null && i<size) {
				StringTokenizer st = new StringTokenizer(aux);
				j = 0;
				while (st.hasMoreTokens()) {
					double value = new Double(st.nextToken());
					objectiveValues[i][j] = value;
					j++;
				}
				aux = br.readLine();
				i++;
			}
			br.close();
			return objectiveValues;

		} catch (Exception e) {
			throw new JMetalException("getObjectiveValuesFromFile: failed when reading for file: " + path, e);
		}
	}
}
