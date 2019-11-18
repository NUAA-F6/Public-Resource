package com.nuaa.shr.pls.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import org.uma.jmetal.util.JMetalException;

public class InitializeMethod {
	public double[][] initializeUniformWeight(int numberOfObjectives, int numberOPpopulation) {
		
		String dataDirectory = "MOEAD_Weights";
		String dataFileName;
		dataFileName = "W" + numberOfObjectives + "D_" + numberOPpopulation + ".dat";
		double[][] weightVector = new double[numberOPpopulation][numberOfObjectives];
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
					weightVector[i][j] = value;
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
		
		return weightVector;
	}
}
