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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import com.carrotsearch.hppc.IntArrayList;
import com.clearnlp.classification.train.AbstractTrainSpace;
import com.clearnlp.util.UTArray;
import com.clearnlp.util.UTMath;

/**
 * Abstract algorithm.
 * @since 1.3.2
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
abstract public class AbstractAdaGrad extends AbstractMulticlass
{
	protected final int MAX_ITER = 1000;
	
	protected double d_alpha;
	protected double d_rho;
	protected double d_eps;
	
	public AbstractAdaGrad(double alpha, double rho, double eps)
	{
		d_alpha = alpha;
		d_rho   = rho;
		d_eps   = eps;
	}
	
	abstract protected boolean update(int L, int y, int[] x, double[] v, double[] gs, double[] weights);
	
	@Override
	protected void updateWeights(AbstractTrainSpace space)
	{	
		final int D  = space.getFeatureSize();
		final int L  = space.getLabelSize();
		final int N  = space.getInstanceSize();
		final int WS = D * L;
		
		IntArrayList        ys = space.getYs();
		ArrayList<int[]>    xs = space.getXs();
		ArrayList<double[]> vs = space.getVs();
		
		double[] cWeights = space.getModel().getWeights();
		double[] gs = new double[WS];
		
		double stdev, prevScore, currScore = 0;
		int[] indices = UTArray.range(N);
		int i, j, correct;
		
		int      yi;
		int[]    xi;
		double[] vi = null;
		
		for (i=0; i<MAX_ITER; i++)
		{
			UTArray.shuffle(new Random(5), indices, N);
			prevScore = currScore;
			Arrays.fill(gs, 0);
			correct = 0;
			
			for (j=0; j<N; j++)
			{
				yi = ys.get(indices[j]);
				xi = xs.get(indices[j]);
				if (space.hasWeight())	vi = vs.get(indices[j]);
				
				if (!update(L, yi, xi, vi, gs, cWeights))
					correct++;
			}
			
			currScore = 100d * correct / N;
			stdev = UTMath.stdev(prevScore, currScore);
			LOG.info(String.format("%4d: acc = %5.2f, stdev = %7.4f\n", i+1, currScore, stdev));
			if (stdev < d_eps) break;
		}
	}
	
	protected double getCost(int L, double[] gs, int y, int x)
	{
		return d_alpha / (d_rho + Math.sqrt(gs[getWeightIndex(L, y, x)]));
	}
}
