package org.uma.jmetal.problem.multiobjective.glt;

import java.util.ArrayList;
import java.util.List;

import org.uma.jmetal.problem.impl.AbstractDoubleProblem;
import org.uma.jmetal.solution.DoubleSolution;

@SuppressWarnings("serial")
public class GLT3 extends AbstractDoubleProblem {

	/**
	 * Constructor. Creates default instance of problem GLT3 (10 decision
	 * variables)
	 */
	public GLT3() {
		this(10);
	}

	/**
	 * Creates a new instance of problem GLT3.
	 *
	 * @param numberOfVariables
	 *            Number of variables.
	 */
	public GLT3(Integer numberOfVariables) {
		setNumberOfVariables(numberOfVariables);
		setNumberOfObjectives(2);
		setName("GLT3");

		List<Double> lowerLimit = new ArrayList<>(getNumberOfVariables());
		List<Double> upperLimit = new ArrayList<>(getNumberOfVariables());

		lowerLimit.add(0.0);
		upperLimit.add(1.0);

		for (int i = 1; i < getNumberOfVariables(); i++) {
			lowerLimit.add(-1.0);
			upperLimit.add(1.0);
		}

		setLowerLimit(lowerLimit);
		setUpperLimit(upperLimit);
	}

	@Override
	public void evaluate(DoubleSolution solution) {
		double[] f = new double[2];
		double g = 0.0;
		for (int i = 1; i < solution.getNumberOfVariables(); i++) {
			double t = 2.0 * Math.PI * solution.getVariableValue(0)
					+ (double) (i) / (double) (solution.getNumberOfVariables()) * Math.PI;
			g += Math.pow(solution.getVariableValue(i) - Math.sin(t), 2.0);
		}
		g += 1.0;
		f[0] = g * solution.getVariableValue(0);
		if (f[0] <= 0.05)
			f[1] = g * (1.0 - 19.0 * solution.getVariableValue(0));
		else
			f[1] = g * ((1.0 - solution.getVariableValue(0)) / 19.0);

		solution.setObjective(0, f[0]);
		solution.setObjective(1, f[1]);
	}

}
