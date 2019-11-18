package org.uma.jmetal.util.comparator;

import java.io.Serializable;
import java.util.Comparator;

import org.uma.jmetal.solution.Solution;

@SuppressWarnings("serial")
public class GwsComparator <S extends Solution<?>> implements Comparator<S>, Serializable{

	private double[] idealPoint; 
	private double[] nadPoint;
	private double numberOfGrid ;
	private double[] gridLen;
	
	public GwsComparator(double numberOfGrid) {
		this.numberOfGrid = numberOfGrid;
		idealPoint = new double[]{0};
		nadPoint = new double[]{1.0};
		gridLen = new double[]{(1.0)/numberOfGrid};
	}
	
	@Override
	public int compare(S solution1, S solution2) {
		boolean dominate1 = false;
		boolean dominate2 = false;
		boolean isEqual = false ;

		for(int i = 0;i<solution1.getNumberOfObjectives();i++){
			double index1 = Math.floor((solution1.getObjective(i) - getIdealValue(i)) / gridLen[i]);	
			double index2 = Math.floor((solution2.getObjective(i) - getIdealValue(i)) / gridLen[i]);
			
			if (index1 < index2) {
				dominate1 = true;

				if (dominate2 || isEqual) {
					return 0;
				}
			} else if (index1 > index2) {
				dominate2 = true;

				if (dominate1 || isEqual) {
					return 0;
				}
			}else isEqual = true;
		}
		
		if (!dominate1 && !dominate2) {//in the same grid
			double gws1 = 0.0;
			double gws2 = 0.0;
			for (int i = 0; i < solution1.getNumberOfObjectives(); i++) {
				double index1 = Math.floor((solution1.getObjective(i) - getIdealValue(i)) / gridLen[i]);
				double coef = 1.0 / (index1 + 0.000001);
				if(solution1.getObjective(i) < getIdealValue(i)){
					coef = 1.0 / 0.000001;
				}
				
				double gridIdealValue = index1 * gridLen[i] + getIdealValue(i);
				gws1 += (solution1.getObjective(i) - gridIdealValue)*coef;
				gws2 += (solution2.getObjective(i) - gridIdealValue)*coef;
			}

			if (gws1 < gws2) {
				return -1;
			} else if(gws1 > gws2){
				return 1;
			}else return 0;
		}else if(isEqual){//最后一维相等
			return 0;
		}else if(dominate1){
			return -1;
		}else {
			return 1;
		}
	}


	public double getIdealValue(int objective){
		return idealPoint[objective < idealPoint.length ? objective
				: idealPoint.length - 1];
	}
	
	public double getNadValue(int objective){
		return nadPoint[objective < nadPoint.length ? objective
				: nadPoint.length - 1];
	}
	
	public void setIdealAndNad(double[] idealPoint, double[] nadPoint) {
		this.idealPoint = idealPoint ;
		this.nadPoint = nadPoint ;
		gridLen = new double[idealPoint.length];
		for(int i = 0;i<idealPoint.length;i++){
			double sigma = (this.nadPoint[i] - this.idealPoint[i]) / (numberOfGrid*2) ;
			this.nadPoint[i] = this.nadPoint[i] + sigma;
			this.idealPoint[i] = this.idealPoint[i] - sigma;
			gridLen[i] = (this.nadPoint[i] - this.idealPoint[i]) / numberOfGrid ;
		}
	}
	
	public void setIdealAndNad(double idealValue, double nadValue){
		this.idealPoint = new double[]{idealValue};
		this.nadPoint = new double[]{nadValue};
		for(int i = 0;i<idealPoint.length;i++){
			double sigma = (nadPoint[i] - idealPoint[i]) / (numberOfGrid*2) ;
			nadPoint[i] = nadPoint[i] + sigma;
			idealPoint[i] = idealPoint[i] - sigma;
			gridLen[i] = (nadPoint[i] - idealPoint[i]) / numberOfGrid ;

		}
	}

}
