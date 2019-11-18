package com.nuaa.shr.pls;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.uma.jmetal.solution.PermutationSolution;
import org.uma.jmetal.solution.Solution;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.LibSVM;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.SelectedTag;

public class WekaUtils {
	private LibSVM classifier;
	private final static double DELTA = 0.03;
	Instances trainData;
	
	int iteration = 0;
	/**
	 * 返回一个分类器
	 * @param solutionSet solutionSet
	 * @return
	 * @throws Exception
	 */
	public Classifier svmLearning(List<PermutationSolution<Integer>> solutionSet, int iteration) throws Exception{
		double[][] objectiveValues = getObjectives(solutionSet);
		this.iteration = iteration;
		return svmLearning(objectiveValues);
	}

	/**
	 * 返回一个分类器
	 * @param objectiveValues 目标值矩阵
	 * @return
	 * @throws Exception
	 */
	public Classifier svmLearning(double[][] objectiveValues) throws Exception{
		trainData = matrixConvertToInstances(objectiveValues);
		classifier = new LibSVM();//default KERNELTYPE RBF
		classifier.setKernelType(new SelectedTag(LibSVM.KERNELTYPE_POLYNOMIAL, LibSVM.TAGS_KERNELTYPE));
//		classifier.setCost(100.0);
//		classifier.setGamma(1.0);
		classifier.buildClassifier(trainData);
		return classifier;
	}
	
	/**
	 * 预测
	 * @param solution solution
	 * @return
	 * @throws Exception
	 */
	public String predict(Solution<?> solution) throws Exception{
		int numberOfObjectives = solution.getNumberOfObjectives();
		double[] objectives = new double[numberOfObjectives];
		for(int i = 0;i<numberOfObjectives;i++){
			objectives[i] = solution.getObjective(i);
		}
		return predict(objectives);
		
	}
	
	/**
	 * 预测
	 * @param objectives 目标值数组
	 * @return
	 * @throws Exception
	 */
	public String predict(double[] objectives) throws Exception{
		if(classifier!=null){
			int numberOfObjectives = objectives.length;
			
			ArrayList<Attribute> attributes = getAttributes(numberOfObjectives);
			
			Instances testData = new Instances("relation",attributes,0);
			testData.setClassIndex(testData.numAttributes()-1);
			
			double[] instance = new double[numberOfObjectives+1];
			for(int i = 0;i<numberOfObjectives;i++){
				instance[i] = objectives[i];
			}
			testData.add(new DenseInstance(1.0,instance));
			
			double classValue = classifier.classifyInstance(testData.instance(0));
			return testData.classAttribute().value((int)classValue);
		}else throw new Exception("classification must be initialized first!");
	}

	/**
	 * 测试模型 标签在每一行最后一个, 0代表正例, 1代表负例
	 *  objectives[0].length == numberOfObjectives+1
	 * @param objectiveMatrix
	 * @return
	 * @throws Exception 
	 */
	public String testModel(double[][] objectiveMatrix) throws Exception{
		if(classifier!=null){
			int numberOfObjectives = objectiveMatrix[0].length-1;
			ArrayList<Attribute> attributes = getAttributes(numberOfObjectives);
			
			Instances testData = new Instances("test",attributes,0);
			testData.setClassIndex(testData.numAttributes()-1);
			
			for(int i = 0;i<objectiveMatrix.length;i++){
				double[] objectives = objectiveMatrix[i];
				double[] instance = new double[objectives.length];
				for(int j = 0;j<objectives.length;j++){
					instance[j] = objectives[j];
				}
				testData.add(new DenseInstance(1.0,instance));
			}
			Evaluation eval = new Evaluation(trainData);
			
			eval.evaluateModel(classifier, testData);
//			System.out.println(eval.errorRate());
//			System.out.println(eval.toClassDetailsString());
//			System.out.println(eval.evaluateModelOnceAndRecordPrediction(classifier, testData.instance(0)));
			for(int i = 0;i<testData.size();i++){
				String classTag = testData.instance(i).classAttribute().value((int)classifier.classifyInstance(testData.instance(i)));
				System.out.println("orignal-class: "+testData.instance(i).stringValue(testData.instance(i).classIndex())+" predict-class:"+classTag);
			}
			return eval.toSummaryString();
			
		}else throw new Exception("classification must be initialized first!");
	}
	
	/**
	 * 初始化option和class标题
	 * @param numberOfObjectives
	 * @return
	 */
	private ArrayList<Attribute> getAttributes(int numberOfObjectives) {
		ArrayList<Attribute> attributes = new ArrayList<Attribute>();
		for(int i = 0;i<numberOfObjectives;i++){
			attributes.add(new Attribute("f"+(i+1)));
		}
		List<String> labels = new ArrayList<>();
		labels.add("positive");
		labels.add("negative");
		
		attributes.add(new Attribute("class", labels));
		return attributes;
	}
	

	/**
	 * 目标值矩阵转化为Inatances
	 * @param objectiveValues
	 * @return
	 */
	public Instances matrixConvertToInstances(double[][] objectiveValues) {
		int numberOfObjectives = objectiveValues[0].length;
		ArrayList<Attribute> attributes = getAttributes(numberOfObjectives);
		
		Instances trainData = new Instances("relation",attributes,0);
		trainData.setClassIndex(trainData.numAttributes()-1);
		
		for(int i = 0;i<objectiveValues.length;i++){
			double[] vector = objectiveValues[i];
			double[] negativeInstance = new double[numberOfObjectives+1];
			double[] positiveInstance = new double[numberOfObjectives+1];
			int j;
			for(j = 0;j<vector.length;j++){
				negativeInstance[j] = vector[j]*(1.0+DELTA);
				positiveInstance[j] = vector[j]*(1.0-DELTA-0.01);
			}
			negativeInstance[j] = 1;
			positiveInstance[j] = 0;//indexOf("positive")
			
			trainData.add(new DenseInstance(1.0, positiveInstance));
			trainData.add(new DenseInstance(1.0, negativeInstance));
		}
		
		trainData.randomize(new Random(System.currentTimeMillis()));
		return trainData;
	}

	/**
	 * 根据solution Set 获取目标值矩阵
	 * @param solutionSet
	 * @return
	 */
	private double[][] getObjectives(List<PermutationSolution<Integer>> solutionSet) {
		double[][] objectiveValues = 
				new double[solutionSet.size()][solutionSet.get(0).getNumberOfObjectives()];
		
		for(int i = 0;i<objectiveValues.length;i++){
			for(int j = 0;j<objectiveValues[0].length;j++){
				objectiveValues[i][j] = solutionSet.get(i).getObjective(j);
			}
		}
		return objectiveValues;
	}
}
