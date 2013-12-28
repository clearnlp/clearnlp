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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import com.carrotsearch.hppc.IntArrayList;
import com.clearnlp.classification.model.AbstractModel;
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
	abstract protected boolean update(int L, int y, int[] x, double[] v, double[] gs, double[] cWeights, double[] aWeights, int count);
	
	@Override
	public void updateWeights(AbstractTrainSpace space, boolean average)
	{	
		final int D  = space.getFeatureSize();
		final int L  = space.getLabelSize();
		final int N  = space.getInstanceSize();
		final int WS = D * L;
		
		IntArrayList        ys = space.getYs();
		ArrayList<int[]>    xs = space.getXs();
		ArrayList<double[]> vs = space.getVs();
		
		AbstractModel model = space.getModel();
		double[] cWeights = new double[WS];
		double[] aWeights = average ? new double[WS] : null;
		double[] gs       = new double[WS];
		
		double stdev, prevScore, currScore = 0;
		int[] indices = UTArray.range(N);
		int i, j, correct, count = 1;
		
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
				
				if (average)
				{
					if (!update(L, yi, xi, vi, gs, cWeights, aWeights, count))
						correct++;
					
					count++;
				}
				else if (!update(L, yi, xi, vi, gs, cWeights))
					correct++;
			}
			
			currScore = 100d * correct / N;
			stdev = UTMath.stdev(prevScore, currScore);
			LOG.info(String.format("%4d: acc = %5.2f, stdev = %7.4f\n", i+1, currScore, stdev));
			if (stdev < d_eps) break;
		}
		
		if (average)	model.setWeights(getWeights(cWeights, aWeights, count));
		else			model.setWeights(UTArray.toFloatArray(cWeights));
	}
	
	protected double getCost(int L, double[] gs, int y, int x)
	{
		return d_alpha / (d_rho + Math.sqrt(gs[getWeightIndex(L, y, x)]));
	}
	
	protected float[] getWeights(double[] cWeights, double[] aWeights, int count)
	{
		int i, size = cWeights.length;
		float[] fs  = new float[size];
		double c = 1d / count;
		
		for (i=0; i<size; i++)
			fs[i] = (float)(cWeights[i] - c * aWeights[i]);
		
		return fs;
	}
	
	protected void updateWeightForAveraging(int idx, double cost, double[] cWeights, double[] aWeights, int count)
	{
		cWeights[idx] += cost;
		aWeights[idx] += cost * count;
	}
}
