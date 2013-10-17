/**
* Copyright 2012-2013 University of Massachusetts Amherst
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*   http://www.apache.org/licenses/LICENSE-2.0
*   
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.clearnlp.classification.algorithm;


/**
 * AdaGrad algorithm using logistic regression.
 * @since 1.3.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class AdaGradLR extends AbstractAdaGrad
{
	/**
	 * @param alpha the learning rate.
	 * @param rho the smoothing denominator.
	 */
	public AdaGradLR(double alpha, double rho, double eps)
	{
		super(alpha, rho, eps);
	}
	
	protected boolean update(int L, int y, int[] x, double[] v, double[] gs, float[] weights)
	{
		double[] grad = getGradients(L, y, x, v, weights);
		
		if (grad[y] > 0.01)
		{
			updateCounts (L, gs, grad, x, v);
			updateWeights(L, gs, grad, x, v, weights);
			return true;
		}
		
		return false;
	}
	
	protected double[] getGradients(int L, int y, int[] x, double[] v, float[] weights)
	{
		double[] scores = getScores(L, x, v, weights);
		normalize(scores);

		int i; for (i=0; i<L; i++) scores[i] *= -1;
		scores[y] += 1;
		
		return scores;
	}
	
	protected void updateCounts(int L, double[] gs, double[] grad, int[] x, double[] v)
	{
		int i, label, len = x.length;
		double[] g = new double[L];
		double d;

		for (label=0; label<L; label++)
			g[label] = grad[label] * grad[label];
		
		if (v != null)
		{
			for (i=0; i<len; i++)
			{
				d = v[i] * v[i];
				
				for (label=0; label<L; label++)
					gs[getWeightIndex(L, label, x[i])] += d * g[label];
			}
		}
		else
		{
			for (i=0; i<len; i++)
				for (label=0; label<L; label++)
					gs[getWeightIndex(L, label, x[i])] += g[label];
		}
	}
	
	protected void updateWeights(int L, double[] gs, double[] grad, int[] x, double[] v, float[] weights)
	{
		int i, label, len = x.length;
		
		if (v != null)
		{
			for (i=0; i<len; i++)
				for (label=0; label<L; label++)
					weights[getWeightIndex(L, label, x[i])] += getCost(L, gs, label, x[i]) * grad[label] * v[i];
		}
		else
		{
			for (i=0; i<len; i++)
				for (label=0; label<L; label++)
					weights[getWeightIndex(L, label, x[i])] += getCost(L, gs, label, x[i]) * grad[label];
		}
	}
}
	