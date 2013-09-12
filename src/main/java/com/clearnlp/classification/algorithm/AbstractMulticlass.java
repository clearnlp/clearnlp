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
package com.clearnlp.classification.algorithm;

import com.clearnlp.classification.model.AbstractModel;
import com.clearnlp.classification.train.AbstractTrainSpace;

/**
 * Abstract algorithm.
 * @since 1.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
abstract public class AbstractMulticlass extends AbstractAlgorithm
{
	abstract protected void updateWeights(AbstractTrainSpace space);
	
	/**
	 * Trains all instances in the training space.
	 * @param space the training space.
	 */
	public void train(AbstractTrainSpace space)
	{
		AbstractModel model = space.getModel();
		
		if (model.getWeights() == null)
			model.initWeightVector();
		
		updateWeights(space);
	}
	
	protected double[] getScores(int L, int[] x, double[] v, double[] weights)
	{
		double[] scores = new double[L];
		int i, label, len = x.length;
		
		for (i=0; i<len; i++)
		{
			for (label=0; label<L; label++)
			{
				if (v != null)
					scores[label] += weights[getWeightIndex(L, label, x[i])] * v[i];
				else
					scores[label] += weights[getWeightIndex(L, label, x[i])];
			}
		}
		
		return scores;
	}
	
	protected int getWeightIndex(int L, int label, int index)
	{
		return index * L + label;
	}
}
