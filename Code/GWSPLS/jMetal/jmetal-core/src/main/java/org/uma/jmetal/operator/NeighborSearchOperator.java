package org.uma.jmetal.operator;

import java.util.Comparator;
import java.util.List;

import org.uma.jmetal.solution.Solution;

/**
 * Search all neighborhoods {@code List<S>} for a solution {@code S} 
 * @author sevn
 *
 * @param <S>
 */
public interface NeighborSearchOperator<S extends Solution<?>> extends Operator<S, List<S>>{
	public void setCandidate(List<S> population) ;
	public void setComparator(Comparator<S> comparator);
}
