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

import com.carrotsearch.hppc.IntArrayList;
import com.clearnlp.classification.train.AbstractTrainSpace;
import com.clearnlp.util.UTMath;


/**
 * Vector containing sparse features.
 * @since 1.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class SparseFeatureVector extends AbstractFeatureVector
{
	private IntArrayList i_indices;
	
	/** Constructs a vector containing sparse features without weights. */
	public SparseFeatureVector()
	{
		super();
	}
	
	/**
	 * Constructs a vector containing sparse features.
	 * @param hasWeight {@code true} if features are assigned with different weights.
	 */
	public SparseFeatureVector(boolean hasWeight)
	{
		super(hasWeight);
	}
	
	/* (non-Javadoc)
	 * @see edu.colorado.clear.classification.vector.AbstractFeatureVector#init()
	 */
	protected void init()
	{
		i_indices = new IntArrayList();
	}
	
	/**
	 * Adds a feature.
	 * @param index  the feature type.
	 */
	public void addFeature(int index)
	{
		i_indices.add(index);
	}
	
	/**
	 * Adds a feature.
	 * @param index the feature index.
	 * @param weight the feature weight.
	 */
	public void addFeature(int index, double weight)
	{
		i_indices.add(index);
		d_weights.add(weight);
	}
	
	public void addFeatures(int[] indices)
	{
		i_indices.add(indices);
	}
	
	public void addFeatures(int[] indices, double[] weights)
	{
		i_indices.add(indices);
		d_weights.add(weights);
	}
	
	/**
	 * Adds a feature.
	 * @param feature {@code <index>}{@link SparseFeatureVector#DELIM}{@code [}{@link SparseFeatureVector#DELIM}{@code <weight>]}.
	 */
	public void addFeature(String feature)
	{
		if (b_weight)
		{
			String[] tmp = feature.split(DELIM);
			
			i_indices.add(Integer.parseInt(tmp[0]));
			d_weights.add(Double.parseDouble(tmp[1]));	
		}
		else
			i_indices.add(Integer.parseInt(feature));
	}
	
	/**
	 * Returns the index'th feature index.
	 * @param index the index of the feature index to return.
	 * @return the index'th feature index.
	 */
	public int getIndex(int index)
	{
		return i_indices.get(index);
	}
	
	/**
	 * Returns all feature indices.
	 * @return all feature indices.
	 */
	public int[] getIndices()
	{
		return i_indices.toArray();
	}
	
	/**
	 * Returns the total number of features in this vector.
	 * @return the total number of features in this vector.
	 */
	public int size()
	{
		return i_indices.size();
	}
	
	/** Trims the internal buffer to the current size. */
	public void trimToSize()
	{
		i_indices.trimToSize();
		if (b_weight)	d_weights.trimToSize();
	}
	
	public boolean isEmpty()
	{
		return i_indices.isEmpty();
	}
	
	public double getSumOfSquaredWeights()
	{
		int size = i_indices.size();
		
		if (b_weight)
		{
			double sum = 0; int i;
			
			for (i=0; i<size; i++)
				sum += UTMath.sq(d_weights.get(i));
			
			return sum;
		}
		else
			return size;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		StringBuilder build = new StringBuilder();
		int i, size = i_indices.size();
		
		for (i=0; i<size; i++)
		{
			build.append(AbstractTrainSpace.DELIM_COL);
			build.append(i_indices.get(i));
			
			if (b_weight)
			{
				build.append(DELIM);
				build.append(d_weights.get(i));
			}
		}
		
		return build.toString().substring(AbstractTrainSpace.DELIM_COL.length());
	}
}
