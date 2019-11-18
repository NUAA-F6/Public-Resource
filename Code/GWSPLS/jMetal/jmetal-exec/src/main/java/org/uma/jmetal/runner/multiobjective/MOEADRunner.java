//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU Lesser General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU Lesser General Public License for more details.
// 
//  You should have received a copy of the GNU Lesser General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.

package org.uma.jmetal.runner.multiobjective;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.multiobjective.moead.AbstractMOEAD;
import org.uma.jmetal.algorithm.multiobjective.moead.MOEADBuilder;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.impl.crossover.DifferentialEvolutionCrossover;
import org.uma.jmetal.operator.impl.mutation.PolynomialMutation;
import org.uma.jmetal.problem.DoubleProblem;
import org.uma.jmetal.problem.multiobjective.cdtlz.ConvexC2_DTLZ2;
import org.uma.jmetal.problem.multiobjective.cec2009Competition.UF1;
import org.uma.jmetal.problem.multiobjective.cec2009Competition.UF2;
import org.uma.jmetal.problem.multiobjective.cec2009Competition.UF3;
import org.uma.jmetal.problem.multiobjective.cec2009Competition.UF4;
import org.uma.jmetal.problem.multiobjective.cec2009Competition.UF5;
import org.uma.jmetal.problem.multiobjective.cec2009Competition.UF6;
import org.uma.jmetal.problem.multiobjective.cec2009Competition.UF7;
import org.uma.jmetal.problem.multiobjective.dtlz.DTLZ1;
import org.uma.jmetal.problem.multiobjective.dtlz.DTLZ2;
import org.uma.jmetal.problem.multiobjective.dtlz.DTLZ5;
import org.uma.jmetal.problem.multiobjective.glt.GLT3;
import org.uma.jmetal.problem.multiobjective.zdt.ZDT1;
import org.uma.jmetal.problem.multiobjective.zdt.ZDT3;
import org.uma.jmetal.problem.multiobjective.zdt.ZDT6;
import org.uma.jmetal.runner.AbstractAlgorithmRunner;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.AlgorithmRunner;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.ProblemUtils;

import java.io.FileNotFoundException;
import java.util.List;

/**
 * Class for configuring and running the MOEA/D algorithm
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class MOEADRunner extends AbstractAlgorithmRunner {
  /**
   * @param args Command line arguments.
 * @throws ClassNotFoundException 
   * @throws SecurityException
   * Invoking command:
  java org.uma.jmetal.runner.multiobjective.MOEADRunner problemName [referenceFront]
   */
  public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException {
    DoubleProblem problem;
    Algorithm<List<DoubleSolution>> algorithm;
    MutationOperator<DoubleSolution> mutation;
    DifferentialEvolutionCrossover crossover;

    String problemName ;
    String referenceParetoFront = "";
    if (args.length == 1) {
      problemName = args[0];
    } else if (args.length == 2) {
      problemName = args[0] ;
      referenceParetoFront = args[1] ;
    } else {
//      problemName = "org.uma.jmetal.problem.multiobjective.cdtlz.ConvexC2_DTLZ2";
//      referenceParetoFront = "../jmetal-problem/src/test/resources/pareto_fronts/DTLZ2.5D.pf";
    }

    problem = new DTLZ2(14, 5);

    double cr = 1.0 ;
    double f = 0.5 ;
    crossover = new DifferentialEvolutionCrossover(cr, f, "rand/1/bin");

    double mutationProbability = 1.0 / problem.getNumberOfVariables();
    double mutationDistributionIndex = 20.0;
    mutation = new PolynomialMutation(mutationProbability, mutationDistributionIndex);

    algorithm = new MOEADBuilder(problem, MOEADBuilder.Variant.MOEAD)
        .setCrossover(crossover)
        .setMutation(mutation)
        .setMaxEvaluations(126*1000)
        .setPopulationSize(126)
        .setResultPopulationSize(126)
        .setNeighborhoodSelectionProbability(1.0)
        .setMaximumNumberOfReplacedSolutions(2)
        .setNeighborSize(20)
        .setFunctionType(AbstractMOEAD.FunctionType.TCHE)
        .setDataDirectory("MOEAD_Weights")
        .build() ;

    AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm)
        .execute() ;

    List<DoubleSolution> population = algorithm.getResult() ;
    long computingTime = algorithmRunner.getComputingTime() ;

    JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");

    printFinalSolutionSet(population);
    if (!referenceParetoFront.equals("")) {
      printQualityIndicators(population, referenceParetoFront) ;
    }
  }
}