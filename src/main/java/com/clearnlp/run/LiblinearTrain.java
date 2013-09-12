/**
* Copyright (c) 2009-2012, Regents of the University of Colorado
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
*
* Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
* Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
* Neither the name of the University of Colorado at Boulder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
* AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
* IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
* ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
* LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
* CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
* ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
* POSSIBILITY OF SUCH DAMAGE.
*/
package com.clearnlp.run;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

import org.kohsuke.args4j.Option;

import com.clearnlp.classification.algorithm.AbstractAlgorithm;
import com.clearnlp.classification.algorithm.AbstractLiblinear;
import com.clearnlp.classification.algorithm.LiblinearL2LR;
import com.clearnlp.classification.algorithm.LiblinearL2SVC;
import com.clearnlp.classification.model.AbstractModel;
import com.clearnlp.classification.train.AbstractTrainSpace;
import com.clearnlp.classification.train.OneVsAllTrainer;
import com.clearnlp.classification.train.SparseTrainSpace;
import com.clearnlp.classification.train.StringTrainSpace;
import com.clearnlp.util.UTInput;


/**
 * Trains a Liblinear model.
 * @since 0.1.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class LiblinearTrain extends AbstractRun
{
	@Option(name="-i", usage="the training file (input; required)", required=true, metaVar="<filename>")
	private String s_trainFile;
	
	@Option(name="-m", usage="the model file (output; required)", required=true, metaVar="<filename>")
	private String s_modelFile;

	@Option(name="-nl", usage="label frequency cutoff (default: 0)\n"+"exclusive, string vector space only", required=false, metaVar="<integer>")
	private int i_labelCutoff = 0; 
	
	@Option(name="-nf", usage="feature frequency cutoff (default: 0)\n"+"exclusive, string vector space only", required=false, metaVar="<integer>")
	private int i_featureCutoff = 0;
	
	@Option(name="-nt", usage="the number of threads to be used (default: 1)", required=false, metaVar="<integer>")
	private int i_numThreads = 1;
	
	@Option(name="-v", usage="the type of vector space (default: "+AbstractTrainSpace.VECTOR_STRING+")\n"+
							AbstractTrainSpace.VECTOR_SPARSE+": sparse vector space\n"+
            				AbstractTrainSpace.VECTOR_STRING+": string vector space\n",
            required=false, metaVar="<byte>")
	private byte i_vectorType = AbstractTrainSpace.VECTOR_STRING;
	
	@Option(name="-s", usage="the type of solver (default: "+AbstractAlgorithm.SOLVER_LIBLINEAR_LR2_L1_SVC+")\n"+
							AbstractAlgorithm.SOLVER_LIBLINEAR_LR2_L1_SVC+": L2-regularized L1-loss support vector classification (dual)\n"+
							AbstractAlgorithm.SOLVER_LIBLINEAR_LR2_L2_SVC+": L2-regularized L2-loss support vector classification (dual)\n"+
							AbstractAlgorithm.SOLVER_LIBLINEAR_LR2_LR   +": L2-regularized logistic regression (dual)",
			required=false, metaVar="<byte>")
	private byte i_solver = AbstractAlgorithm.SOLVER_LIBLINEAR_LR2_L1_SVC;
	
	@Option(name="-c", usage="the cost (default: 0.1)", required=false, metaVar="<double>")
	private double d_cost = 0.1;
	
	@Option(name="-e", usage="the tolerance of termination criterion (default: 0.1)", required=false, metaVar="<double>")
	private double d_eps = 0.1;
	
	@Option(name="-b", usage="the bias (default: 0)", required=false, metaVar="<double>")
	private double d_bias = 0.0;
	
	public LiblinearTrain() {}
	
	public LiblinearTrain(String[] args)
	{
		initArgs(args);

		try
		{
			train(s_trainFile, s_modelFile, i_vectorType, i_labelCutoff, i_featureCutoff, i_numThreads, i_solver, d_cost, d_eps, d_bias);
		}
		catch (Exception e) {e.printStackTrace();}
	}
	
	public void train(String trainFile, String modelFile, byte vectorType, int labelCutoff, int featureCutoff, int numThreads, byte solver, double cost, double eps, double bias) throws Exception
	{
		AbstractTrainSpace space = null;
		boolean hasWeight = AbstractTrainSpace.hasWeight(vectorType, trainFile);
		
		switch (vectorType)
		{
		case AbstractTrainSpace.VECTOR_SPARSE:
			space = new SparseTrainSpace(hasWeight); break;
		case AbstractTrainSpace.VECTOR_STRING:
			space = new StringTrainSpace(hasWeight, labelCutoff, featureCutoff); break;
		}
		
		space.readInstances(UTInput.createBufferedFileReader(trainFile));
		space.build();
		
		AbstractModel model = getModel(space, numThreads, solver, cost, eps, bias);
		ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(modelFile)));
		
		out.writeObject(model);
		out.close();
	}
	
	static public AbstractModel getModel(AbstractTrainSpace space, int numThreads, byte solver, double cost, double eps, double bias)
	{
		AbstractLiblinear algorithm = null;
		
		switch (solver)
		{
		case AbstractAlgorithm.SOLVER_LIBLINEAR_LR2_L1_SVC:
			algorithm = new LiblinearL2SVC((byte)1, cost, eps, bias); break;
		case AbstractAlgorithm.SOLVER_LIBLINEAR_LR2_L2_SVC:
			algorithm = new LiblinearL2SVC((byte)2, cost, eps, bias); break;
		case AbstractAlgorithm.SOLVER_LIBLINEAR_LR2_LR:
			algorithm = new LiblinearL2LR(cost, eps, bias); break;
		}

		new OneVsAllTrainer(space, algorithm, numThreads);
		return space.getModel();
	}

	static public void main(String[] args)
	{
		new LiblinearTrain(args);
	}
}
