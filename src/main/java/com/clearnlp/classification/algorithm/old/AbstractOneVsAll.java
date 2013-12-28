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

import com.carrotsearch.hppc.IntArrayList;
import com.clearnlp.classification.train.AbstractTrainSpace;

/**
 * @since 1.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
abstract public class AbstractOneVsAll extends AbstractAlgorithm
{
	/**
	 * @param space the training space.
	 * @param currLabel the label to get the weight vector for.
	 * @return the weight vector for the specific label given the training space.
	 */
	abstract public float[] getWeight(AbstractTrainSpace space, int currLabel);
	
	/** @return an array of 1 or -1. */
	protected byte[] getBinaryLabels(IntArrayList ys, int currLabel)
	{
		int i, size = ys.size();
		byte[] aY = new byte[size];
		
		for (i=0; i<size; i++)
			aY[i] = (ys.get(i) == currLabel) ? (byte)1 : (byte)-1;
			
		return aY;
	}
	
	protected double getScore(double[] weight, int[] x, double[] v, double bias)
	{
		double score = weight[0] * bias;
		int i, size = x.length;
		
		for (i=0; i<size; i++)
		{
			if (v != null)
				score += weight[x[i]] * v[i];
			else
				score += weight[x[i]];
		}
		
		return score;
	}
	
	protected void updateWeight(double[] weight, double cost, int[] x, double[] v, double bias)
	{
		int i, size = x.length;
		weight[0] += cost * bias;
		
		for (i=0; i<size; i++)
		{
			if (v != null)
				weight[x[i]] += cost * v[i];
			else
				weight[x[i]] += cost;
		}
	}
}
