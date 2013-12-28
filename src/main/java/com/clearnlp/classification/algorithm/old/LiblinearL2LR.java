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
 * Liblinear L2-regularized logistic regression algorithm.
 * @since 1.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class LiblinearL2LR extends AbstractLiblinear
{
	private final int MAX_ITER_NEWTON = 100;
	private final double ETA = 0.1;
	
	/**
	 * Constructs the liblinear L2-regularized logistic regression algorithm.
	 * @param cost the cost.
	 * @param eps the tolerance of termination criterion.
	 * @param bias the bias.
	 */
	public LiblinearL2LR(double cost, double eps, double bias)
	{
		super(cost, eps, bias);
	}
	
	@Override
	public float[] getWeight(AbstractTrainSpace space, int currLabel)
	{
		Random rand = new Random(5);
		
		final int N = space.getInstanceSize();
		final int D = space.getFeatureSize();
		final double INNER_MIN = Math.min(1e-8, d_eps);
		
		IntArrayList        ys = space.getYs();
		ArrayList<int[]>    xs = space.getXs();
		ArrayList<double[]> vs = space.getVs();
		
		double[] alpha  = new double[2*N];
		double[] weight = new double[D];
		double G, alpha_old, qd, d, z, gp, gpp, tmpz;

		double alpha_pre = Math.min(0.001 * d_cost, 1e-8);
		double innereps  = 1e-2;
		double Gmax;
		
		int      i, s, iter, iter_newton, iter_inner, ind1, ind2, sign;
		byte     yi;
		int[]    xi;
		double[] vi = null;
		
		int []   index = UTArray.range(N);
		byte[]   aY    = getBinaryLabels(ys, currLabel);
		double[] QD    = getQD(xs, vs, 0, d_bias);
		
		for (i=0; i<N; i++)
		{
			alpha[2*i  ] = alpha_pre;
			alpha[2*i+1] = d_cost - alpha_pre;

			d  = aY[i] * alpha[2*i];
			xi = xs.get(i);
			if (space.hasWeight())	vi = vs.get(i);
			if (d != 0) updateWeight(weight, d, xi, vi, d_bias);
		}
		
		for (iter=0; iter<MAX_ITER; iter++)
		{
			Gmax = iter_newton = 0;
			UTArray.shuffle(rand, index, N);
			
			for (s=0; s<N; s++)
			{
				i  = index[s];
				yi = aY[i];
				xi = xs.get(i);
				if (space.hasWeight()) vi = vs.get(i);
				G  = getScore(weight, xi, vi, d_bias) * yi;
 				qd = QD[i];
 				
 				ind1 = 2*i;
 				ind2 = 2*i + 1;
 				sign = 1;
 				
 				// decide to minimize g_1(z) or g_2(z)
 				if (0.5 * qd * (alpha[ind2] - alpha[ind1]) + G < 0) 
 				{
 					ind1 = 2*i + 1;
 					ind2 = 2*i;
 					sign = -1;
 				}
 				
 				// g_t(z) = z*log(z) + (C-z)*log(C-z) + 0.5a(z-alpha_old)^2 + sign*G(z-alpha_old)
 				alpha_old = alpha[ind1];
 				z = alpha_old;
 				if (d_cost-z < 0.5*d_cost)	z *= 0.1; 
 					
 				gp = qd * (z-alpha_old) + sign * G + Math.log(z/(d_cost-z));
 				Gmax = Math.max(Gmax, Math.abs(gp));
 				
 				// Newton method on the sub-problem
 				for (iter_inner=0; iter_inner<=MAX_ITER_NEWTON; iter_inner++) 
 				{
 					if (Math.abs(gp) < innereps)
 						break;
 					
 					gpp  = qd + d_cost/(d_cost-z)/z;
 					tmpz = z - gp/gpp;
 					
 					if (tmpz <= 0)	z *= ETA;
 					else 			z = tmpz;
 					
 					gp = qd * (z-alpha_old) + sign * G + Math.log(z/(d_cost-z));
 					iter_newton++;
 				}

 				if (iter_inner > 0)
 				{
 					alpha[ind1] = z;
 					alpha[ind2] = d_cost-z;
 					d = sign * (z-alpha_old) * yi;
 					if (d != 0) updateWeight(weight, d, xi, vi, d_bias);
 				}
			}
			
			if (Gmax < d_eps)
				break;
			
			if (iter_newton <= N/10) 
				innereps = Math.max(INNER_MIN, 0.1*innereps);
		}
		
		weight[0] *= d_bias;
		
		StringBuilder build = new StringBuilder();
		
		build.append("- label = ");		build.append(currLabel);
		build.append(": iter = ");		build.append(iter);
		build.append("\n");

		LOG.info(build.toString());
		return UTArray.toFloatArray(weight);
	}
}
	