package org.uma.jmetal.solution;

/**
 * 
 * @author sevn
 *
 * @param <T>
 */
public interface GridPermutationSolution<T> extends Solution<T>{
	public void setGridCoordinate(int index, int value);
	public int getGridCoordinate(int index);
	
	public int getConstraint(int index);
	public void setConstraint(int index, int value);
}
