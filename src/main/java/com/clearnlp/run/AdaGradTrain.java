/**
 * Copyright (c) 2009/09-2012/08, Regents of the University of Colorado
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/**
 * Copyright 2012/09-2013/04, 2013/11-Present, University of Massachusetts Amherst
 * Copyright 2013/05-2013/10, IPSoft Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package com.clearnlp.run;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

import org.kohsuke.args4j.Option;

import com.clearnlp.classification.algorithm.old.AbstractAdaGrad;
import com.clearnlp.classification.algorithm.old.AbstractAlgorithm;
import com.clearnlp.classification.algorithm.old.AdaGradHinge;
import com.clearnlp.classification.algorithm.old.AdaGradLR;
import com.clearnlp.classification.model.AbstractModel;
import com.clearnlp.classification.train.AbstractTrainSpace;
import com.clearnlp.classification.train.SparseTrainSpace;
import com.clearnlp.classification.train.StringTrainSpace;
import com.clearnlp.util.UTInput;


/**
 * Trains a Liblinear model.
 * @since 0.1.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class AdaGradTrain extends AbstractRun
{
	@Option(name="-i", usage="the training file (input; required)", required=true, metaVar="<filename>")
	private String s_trainFile;
	
	@Option(name="-m", usage="the model file (output; required)", required=true, metaVar="<filename>")
	private String s_modelFile;

	@Option(name="-nl", usage="label frequency cutoff (default: 0)\n"+"exclusive, string vector space only", required=false, metaVar="<integer>")
	private int i_labelCutoff = 0; 
	
	@Option(name="-nf", usage="feature frequency cutoff (default: 0)\n"+"exclusive, string vector space only", required=false, metaVar="<integer>")
	private int i_featureCutoff = 0;
	
	@Option(name="-v", usage="the type of vector space (default: "+AbstractTrainSpace.VECTOR_STRING+")\n"+
							AbstractTrainSpace.VECTOR_SPARSE+": sparse vector space\n"+
            				AbstractTrainSpace.VECTOR_STRING+": string vector space\n",
            required=false, metaVar="<byte>")
	private byte i_vectorType = AbstractTrainSpace.VECTOR_STRING;
	
	@Option(name="-s", usage="the type of solver (default: "+AbstractAlgorithm.SOLVER_ADAGRAD_HINGE+")\n"+
							AbstractAlgorithm.SOLVER_ADAGRAD_HINGE+": AdaGrad using hinge loss\n"+
							AbstractAlgorithm.SOLVER_ADAGRAD_LR   +": AdaGrad using logistic regression",
			required=false, metaVar="<byte>")
	private byte i_solver = AbstractAlgorithm.SOLVER_ADAGRAD_HINGE;
	
	@Option(name="-a", usage="the cost (default: 0.01)", required=false, metaVar="<double>")
	private double d_alpha = 0.01;
	
	@Option(name="-r", usage="the ridge (default: 0.1)", required=false, metaVar="<double>")
	private double d_rho = 0.1;
	
	@Option(name="-e", usage="the terminal criterion (default: 0.05)", required=false, metaVar="<double>")
	private double d_eps = 0.05;
	
	@Option(name="-average", usage="if true, average wegiths", required=false, metaVar="<boolean>")
	private boolean b_average = false;
	
	public AdaGradTrain() {}
	
	public AdaGradTrain(String[] args)
	{
		initArgs(args);

		try
		{
			train(s_trainFile, s_modelFile, i_vectorType, i_labelCutoff, i_featureCutoff, i_solver, d_alpha, d_rho, d_eps, b_average);
		}
		catch (Exception e) {e.printStackTrace();}
	}
	
	public void train(String trainFile, String modelFile, byte vectorType, int labelCutoff, int featureCutoff, byte solver, double alpha, double rho, double eps, boolean average) throws Exception
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
		
		AbstractModel model = getModel(space, solver, alpha, rho, eps, average);
		ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(modelFile)));
		
		out.writeObject(model);
		out.close();
	}
	
	static public AbstractAdaGrad getAlgorithm(byte solver, double alpha, double rho, double eps)
	{
		switch (solver)
		{
		case AbstractAlgorithm.SOLVER_ADAGRAD_HINGE:
			return new AdaGradHinge(alpha, rho, eps);
		case AbstractAlgorithm.SOLVER_ADAGRAD_LR:
			return new AdaGradLR(alpha, rho, eps);
		}
		
		return null;
	}
	
	static public AbstractModel getModel(AbstractTrainSpace space, byte solver, double alpha, double rho, double eps, boolean average)
	{
		AbstractAdaGrad algorithm = getAlgorithm(solver, alpha, rho, eps);
		algorithm.updateWeights(space, average);
		return space.getModel();
	}
	
	static public void main(String[] args)
	{
		new AdaGradTrain(args);
	}
}
