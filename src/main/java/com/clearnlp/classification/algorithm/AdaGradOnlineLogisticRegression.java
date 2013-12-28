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
package com.clearnlp.classification.algorithm;

import com.clearnlp.classification.instance.IntInstance;
import com.clearnlp.classification.model.StringModelAD;
import com.clearnlp.classification.vector.SparseFeatureVector;
import com.clearnlp.util.UTMath;

/**
 * AdaGrad algorithm using hinge loss.
 * @since 1.3.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class AdaGradOnlineLogisticRegression extends AbstractAdaGrad
{
	/**
	 * @param alpha the learning rate.
	 * @param rho the smoothing denominator.
	 */
	public AdaGradOnlineLogisticRegression(double alpha, double rho, boolean average)
	{
		super(alpha, rho, average);
	}
	
	@Override
	protected boolean update(StringModelAD model, IntInstance instance, int averageCount)
	{
		double[] gradients = getGradients(model, instance);
		
		if (gradients[instance.getLabel()] > 0.01)
		{
			updateCounts (model, instance, gradients);
			updateWeights(model, instance, gradients, averageCount);
			return true;
		}
		
		return false;
	}
	
	private double[] getGradients(StringModelAD model, IntInstance instance)
	{
		double[] scores = model.getScores(instance.getFeatureVector(), true);
		int i, size = scores.length;
		
		for (i=0; i<size; i++) scores[i] *= -1;
		scores[instance.getLabel()] += 1;
		
		return scores;
	}
	
	protected void updateCounts(StringModelAD model, IntInstance instance, double[] gradidents)
	{
		SparseFeatureVector x = instance.getFeatureVector();
		int i, j, len = x.size(), L = model.getLabelSize();
		double[] g = new double[L];
		double d;
		
		for (j=0; j<L; j++)
			g[j] = gradidents[j] * gradidents[j];
		
		for (i=0; i<len; i++)
		{
			d = UTMath.sq(x.getWeight(i));
			
			for (j=0; j<L; j++)
				d_gradients[model.getWeightIndex(j, x.getIndex(i))] += d * g[j];
		}
	}
	
	private void updateWeights(StringModelAD model, IntInstance instance, double[] gradients, int averageCount)
	{
		SparseFeatureVector x = instance.getFeatureVector();
		int i, j, len = x.size(), L = model.getLabelSize();
		int xi; double vi;
		
		for (i=0; i<len; i++)
		{
			xi = x.getIndex(i);
			vi = x.getWeight(i);
			
			for (j=0; j<L; j++)
				updateWeight(model, j, xi, gradients[j]*vi, averageCount);
		}
	}
}	