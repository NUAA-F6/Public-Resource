package org.uma.jmetal.problem.multiobjective;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class TransferTspMain {
	public static void main(String[] args) throws IOException {
		String[] tspPaths = {"/tspInstances/kroA100.tsp","/tspInstances/kroB100.tsp"};
		DefaultManyObjectiveTSP tsp = new DefaultManyObjectiveTSP(tspPaths);
		List<double[][]> matrics = tsp.getDistanceMatrics();
		
		for(int i = 0;i<matrics.size();i++){
			double[][] matrix = matrics.get(i);
			OutputStream out = new FileOutputStream(new File("transfer/kro100_"+i+".txt"));
			
			out.write("1\n".getBytes());
			for(int j = 0;j<matrix.length;j++){
				double[] vector = matrix[j];
				for(int k = j;k<vector.length;k++){
					out.write(((int)vector[k]+"\n").getBytes());
				}
			}
			out.close();
			
		}
		System.out.println("end");
	}
}
