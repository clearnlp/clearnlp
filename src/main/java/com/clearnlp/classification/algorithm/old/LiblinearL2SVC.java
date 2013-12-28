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
import java.util.Random;

import com.carrotsearch.hppc.IntArrayList;
import com.clearnlp.classification.train.AbstractTrainSpace;
import com.clearnlp.util.UTArray;

/**
 * Liblinear L2-regularized support vector classification.
 * @since 1.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class LiblinearL2SVC extends AbstractLiblinear
{
	private byte i_lossType;
	
	/**
	 * Constructs the liblinear L2-regularized support vector classification algorithm.
	 * @param lossType 1 for L1-loss, 2 for L2-loss.
	 * @param cost the cost.
	 * @param eps the tolerance of termination criterion.
	 * @param bias the bias.
	 */
	public LiblinearL2SVC(byte lossType, double cost, double eps, double bias)
	{
		super(cost, eps, bias);
		i_lossType = lossType;
	}
	
	@Override
	public float[] getWeight(AbstractTrainSpace space, int currLabel)
	{
		Random rand = new Random(5);
		
		final int N = space.getInstanceSize();
		final int D = space.getFeatureSize();
		
		IntArrayList        ys = space.getYs();
		ArrayList<int[]>    xs = space.getXs();
		ArrayList<double[]> vs = space.getVs();
		
		double[] alpha  = new double[N];
		double[] weight = new double[D];
		double G, d, alpha_old;
		
		// Projected gradient, for shrinking and stopping
		double Gmax_old = Double.POSITIVE_INFINITY;
		double Gmin_old = Double.NEGATIVE_INFINITY;
		double violation, Gmax_new, Gmin_new;
		
		// L1/L2 loss
		double diag = 0;
		double upper_bound = d_cost;
		
		if (i_lossType == 2)
		{
			diag = 0.5 / d_cost;
			upper_bound = Double.POSITIVE_INFINITY;
		}
		
		int      active_size = N, iter, i, s;
		byte     yi;
		int[]    xi;
		double[] vi = null;
		
		int []   index = UTArray.range(N);
		byte[]   aY    = getBinaryLabels(ys, currLabel);
		double[] QD    = getQD(xs, vs, diag, d_bias);
		
		for (iter=0; iter<MAX_ITER; iter++)
		{
			Gmax_new = Double.NEGATIVE_INFINITY;
			Gmin_new = Double.POSITIVE_INFINITY;
			UTArray.shuffle(rand, index, active_size);
			
			for (s=0; s<active_size; s++)
			{
				i  = index[s];
				yi = aY[i];
				xi = xs.get(i);
				if (space.hasWeight()) vi = vs.get(i);
				G  = getScore(weight, xi, vi, d_bias) * yi - 1;
 				G += alpha[i] * diag;
				
				if (alpha[i] == 0)
				{
					if (G > Gmax_old)
					{
						active_size--;
						UTArray.swap(index, s, active_size);
						s--;
						continue;
					}
					
					violation = Math.min(G, 0);
                }
				else if (alpha[i] == upper_bound)
				{
					if (G < Gmin_old)
					{
						active_size--;
						UTArray.swap(index, s, active_size);
						s--;
						continue;
					}
					
					violation = Math.max(G, 0);
				}
				else
				{
					violation = G;
				}
				
				Gmax_new = Math.max(Gmax_new, violation);
				Gmin_new = Math.min(Gmin_new, violation);
				
				if (Math.abs(violation) > 1.0e-12)
				{
					alpha_old = alpha[i];
					alpha[i]  = Math.min(Math.max(alpha[i] - G / QD[i], 0d), upper_bound);
					d = (alpha[i] - alpha_old) * yi;
					if (d != 0) updateWeight(weight, d, xi, vi, d_bias);
				}
			}
			
			if (Gmax_new - Gmin_new <= d_eps)
			{
				if (active_size == N)
					break;
				else
				{
					active_size = N;
					Gmax_old = Double.POSITIVE_INFINITY;
					Gmin_old = Double.NEGATIVE_INFINITY;
					continue;
				}
			}
			
			Gmax_old = Gmax_new;
			Gmin_old = Gmin_new;
			if (Gmax_old <= 0) Gmax_old = Double.POSITIVE_INFINITY;
			if (Gmin_old >= 0) Gmin_old = Double.NEGATIVE_INFINITY;
		}
		
		weight[0] *= d_bias;
		int nSV = 0;
		
		for (i=0; i<N; i++)
			if (alpha[i] > 0) ++nSV;
		
		StringBuilder build = new StringBuilder();
		
		build.append("- label = ");		build.append(currLabel);
		build.append(": iter = ");		build.append(iter);
		build.append(", nSV = ");		build.append(nSV);
		build.append("\n");

		LOG.info(build.toString());
		return UTArray.toFloatArray(weight);
	}
}
	