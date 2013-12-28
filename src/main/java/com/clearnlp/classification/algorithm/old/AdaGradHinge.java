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
package com.clearnlp.classification.algorithm.old;

import com.clearnlp.classification.prediction.IntPrediction;
import com.clearnlp.util.UTMath;

/**
 * AdaGrad algorithm using hinge loss.
 * @since 1.3.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class AdaGradHinge extends AbstractAdaGrad
{
	/**
	 * @param alpha the learning rate.
	 * @param rho the smoothing denominator.
	 */
	public AdaGradHinge(double alpha, double rho, double eps)
	{
		super(alpha, rho, eps);
	}
	
	protected boolean update(int L, int y, int[] x, double[] v, double[] gs, double[] weights)
	{
		IntPrediction max = getPrediction(L, y, x, v, weights);
		
		if (max.label != y)
		{
			updateCounts (L, gs, y, max.label, x, v);
			updateWeights(L, gs, y, max.label, x, v, weights);
			return true;
		}
		
		return false;
	}
	
	protected boolean update(int L, int y, int[] x, double[] v, double[] gs, double[] cWeights, double[] aWeights, int count)
	{
		IntPrediction max = getPrediction(L, y, x, v, cWeights);
		
		if (max.label != y)
		{
			updateCounts (L, gs, y, max.label, x, v);
			updateWeights(L, gs, y, max.label, x, v, cWeights, aWeights, count);
			return true;
		}
		
		return false;
	}
	
	private IntPrediction getPrediction(int L, int y, int[] x, double[] v, double[] weights)
	{
		double[] scores = getScores(L, x, v, weights);
		scores[y] -= 1;
		
		IntPrediction max = new IntPrediction(0, scores[0]);
		int label;
		
		for (label=1; label<L; label++)
		{
			if (max.score < scores[label])
				max.set(label, scores[label]);
		}
	
		return max;
	}
	
	private void updateCounts(int L, double[] gs, int yp, int yn, int[] x, double[] v)
	{
		int i, len = x.length;
		
		if (v != null)
		{
			double d;
			
			for (i=0; i<len; i++)
			{
				d = UTMath.sq(v[i]);
				
				gs[getWeightIndex(L, yp, x[i])] += d;
				gs[getWeightIndex(L, yn, x[i])] += d;
			}
		}
		else
		{
			for (i=0; i<len; i++)
			{
				gs[getWeightIndex(L, yp, x[i])]++;
				gs[getWeightIndex(L, yn, x[i])]++;
			}
		}
	}
	
	private void updateWeights(int L, double[] gs, int yp, int yn, int[] x, double[] v, double[] weights)
	{
		int i, xi, len = x.length;
		double vi;
		
		if (v != null)
		{
			for (i=0; i<len; i++)
			{
				xi = x[i]; vi = v[i];
				weights[getWeightIndex(L, yp, xi)] += getCost(L, gs, yp, xi) * vi;
				weights[getWeightIndex(L, yn, xi)] -= getCost(L, gs, yn, xi) * vi;
			}
		}
		else
		{
			for (i=0; i<len; i++)
			{
				xi = x[i];
				weights[getWeightIndex(L, yp, xi)] += getCost(L, gs, yp, xi);
				weights[getWeightIndex(L, yn, xi)] -= getCost(L, gs, yn, xi);
			}
		}
	}
	
	private void updateWeights(int L, double[] gs, int yp, int yn, int[] x, double[] v, double[] cWeights, double[] aWeights, int count)
	{
		int i, xi, len = x.length;
		double vi;
		
		if (v != null)
		{
			for (i=0; i<len; i++)
			{
				xi = x[i]; vi = v[i];
				updateWeightForAveraging(getWeightIndex(L, yp, xi),  getCost(L, gs, yp, xi) * vi, cWeights, aWeights, count);
				updateWeightForAveraging(getWeightIndex(L, yn, xi), -getCost(L, gs, yn, xi) * vi, cWeights, aWeights, count);
			}
		}
		else
		{
			for (i=0; i<len; i++)
			{
				xi = x[i];
				updateWeightForAveraging(getWeightIndex(L, yp, xi),  getCost(L, gs, yp, xi), cWeights, aWeights, count);
				updateWeightForAveraging(getWeightIndex(L, yn, xi), -getCost(L, gs, yn, xi), cWeights, aWeights, count);
			}
		}
	}
}	