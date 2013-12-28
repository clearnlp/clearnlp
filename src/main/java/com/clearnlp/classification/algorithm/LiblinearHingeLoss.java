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

import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.clearnlp.classification.instance.IntInstance;
import com.clearnlp.classification.model.StringModelAD;
import com.clearnlp.classification.prediction.IntPrediction;
import com.clearnlp.classification.vector.SparseFeatureVector;
import com.clearnlp.util.UTArray;

/**
 * Abstract algorithm.
 * @since 1.3.2
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class LiblinearHingeLoss extends AbstractAlgorithm
{
	private final int MAX_ITER = 1000;
	private double d_cost;
	private double d_eps;
	
	public LiblinearHingeLoss(double cost, double eps)
	{
		super(LEARN_BATCH);
		init(cost, eps);
	}
	
	private void init(double cost, double eps)
	{
		d_cost = cost;
		d_eps  = eps;
	}
	
	@Override
	public void train(StringModelAD model)
	{	
		final int N = model.getInstanceSize();

		double[] alpha = new double[N];
		double[] qd    = getQD(model);
		
		double Gmax_old = Double.POSITIVE_INFINITY;
		double Gmin_old = Double.NEGATIVE_INFINITY;
		double G, d, violation, Gmax_new, Gmin_new;
		
		int active_size = N, iter, i, s;
		int [] index = UTArray.range(N);
		Random rand = new Random(5);
		List<IntPrediction> ps;
		IntInstance instance;
		IntPrediction max;
		int y;
		
		for (iter=0; iter<MAX_ITER; iter++)
		{
			UTArray.shuffle(rand, index, active_size);
			Gmax_new = Double.NEGATIVE_INFINITY;
			Gmin_new = Double.POSITIVE_INFINITY;
			
			for (s=0; s<active_size; s++)
			{
				i = index[s];
				instance = model.getInstance(i);
				y = instance.getLabel();
				
				ps  = model.getIntPredictions(instance.getFeatureVector());
				max = Collections.max(ps);
				
				G = max.score;
				if (G < 0) G = 0;
				else if (max.label != y) G *= -1d;
				G -= 1d;
				
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
				else if (alpha[i] == d_cost)
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
					d = alpha[i];
					alpha[i] = Math.min(Math.max(d - G / qd[i], 0d), d_cost);
					d = alpha[i] - d;
					if (d != 0) updateWeights(model, instance, y, max.label, d);
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
			System.out.print(".");
		}	System.out.print("\n");
	}
	
	protected double[] getQD(StringModelAD model)
	{
		int i, size = model.getInstanceSize();
		double[] qd = new double[size];
		IntInstance instance;
		
		for (i=0; i<size; i++)
		{
			instance = model.getInstance(i);
			qd[i] = instance.getFeatureVector().getSumOfSquaredWeights();
		}
		
		return qd;
	}
	
	private void updateWeights(StringModelAD model, IntInstance instance, int yp, int yn, double d)
	{
		SparseFeatureVector x = instance.getFeatureVector();
		int i, xi, len = x.size();
		float vi;
		
		for (i=0; i<len; i++)
		{
			xi = x.getIndex(i);
			vi = (float)(x.getWeight(i) * d);
			
			model.updateWeight(yp, xi,  vi);
			model.updateWeight(yn, xi, -vi);
		}
	}
}
