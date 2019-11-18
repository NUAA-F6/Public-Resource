package com.nuaa.shr.test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class Test2 {
	public static void main(String[]args){
		String fileName = "VAR.tsv";
		try {
			InputStreamReader read = new InputStreamReader(
                    new FileInputStream(fileName));//考虑到编码格式
                    BufferedReader bufferedReader = new BufferedReader(read);
                    String lineTxt = null;
                    while((lineTxt = bufferedReader.readLine()) != null){
                    	String [] arr = lineTxt.split(" ");
                        for(int i=0;i<100;i++){
                        	for(int j=0;j<100;j++){
                        		System.out.println(lineTxt);
                        		if(i!=j && arr[i].equals(arr[j])){
                        			System.out.println("come in...");
                        		}
                        	}
                        }
                    }
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
