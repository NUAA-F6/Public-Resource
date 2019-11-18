package org.uma.jmetal.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PermutationUtility {
	public Integer[] initPermutation(int length){
		List<Integer> permList = new ArrayList<>();
		Integer[] permArray = new Integer[length] ;
		for(int i = 0;i<length;i++){
			permList.add(i);
		}
		Collections.shuffle(permList);
		return permList.toArray(permArray);
	}
}
