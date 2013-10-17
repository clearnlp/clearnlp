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
	
	protected boolean update(int L, int y, int[] x, double[] v, double[] gs, float[] weights)
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
	
	protected IntPrediction getPrediction(int L, int y, int[] x, double[] v, float[] weights)
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
	
	protected void updateCounts(int L, double[] gs, int yp, int yn, int[] x, double[] v)
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
	
	protected void updateWeights(int L, double[] gs, int yp, int yn, int[] x, double[] v, float[] weights)
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
}	