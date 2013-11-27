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
package com.clearnlp.classification.vector;

import com.carrotsearch.hppc.DoubleArrayList;

/**
 * Abstract feature vector.
 * @since 1.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
abstract public class AbstractFeatureVector
{
	/** The delimiter between key, value, and weight ({@code ":"}). */
	static public final String DELIM = ":";
	
	protected DoubleArrayList d_weights;
	protected boolean         b_weight;
	
	/** Constructs an abstract feature vector without weights. */
	public AbstractFeatureVector()
	{
		init();
		initWeights(false);
	}
	
	/**
	 * Constructs an abstract feature vector.
	 * @param hasWeight {@code true} if features are assigned with different weights.
	 */
	public AbstractFeatureVector(boolean hasWeight)
	{
		init();
		initWeights(hasWeight);
	}
	
	/** Initializes this feature vector. */
	abstract protected void init();
	
	private void initWeights(boolean hasWeight)
	{
		d_weights = hasWeight ? new DoubleArrayList() : null;
		b_weight  = hasWeight;		
	}
	
	/**
	 * Returns {@code true} if features are assigned with different weights.
	 * @return {@code true} if features are assigned with different weights.
	 */
	public boolean hasWeight()
	{
		return b_weight;
	}
	
	/**
	 * Returns the index'th feature weight.
	 * @param index the index of the feature weight to return.
	 * @return the index'th feature weight.
	 */
	public double getWeight(int index)
	{
		return b_weight ? d_weights.get(index) : 1d;
	}
	
	/**
	 * Returns all feature weights.
	 * @return all feature weights.
	 */
	public double[] getWeights()
	{
		return d_weights.toArray();
	}
}