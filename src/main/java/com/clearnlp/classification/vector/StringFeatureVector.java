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

import java.util.ArrayList;
import java.util.List;

import com.carrotsearch.hppc.DoubleArrayList;
import com.clearnlp.classification.train.AbstractTrainSpace;


/**
 * Vector containing string features.
 * @since 0.1.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class StringFeatureVector extends AbstractFeatureVector
{
	private List<String> s_types;
	private List<String> s_values;
	
	/** Constructs a vector containing string features without weights. */
	public StringFeatureVector()
	{
		super();
	}
	
	/**
	 * Constructs a vector containing string features.
	 * @param hasWeight {@code true} if features are assigned with different weights.
	 */
	public StringFeatureVector(boolean hasWeight)
	{
		super(hasWeight);
	}
	
	/* (non-Javadoc)
	 * @see edu.colorado.clear.classification.vector.AbstractFeatureVector#init()
	 */
	protected void init()
	{
		s_types  = new ArrayList<String>();
		s_values = new ArrayList<String>();
	}
	
	public StringFeatureVector clone()
	{
		StringFeatureVector copy = new StringFeatureVector(b_weight);
		
		copy.s_types  = new ArrayList<String>(s_types);
		copy.s_values = new ArrayList<String>(s_values);
		if (b_weight) copy.d_weights = d_weights.clone();
		
		return copy;
	}
	
	public void populateWeights()
	{
		int i, size = s_values.size();
		b_weight = true;
		
		d_weights = new DoubleArrayList();

		for (i=0; i<size; i++)
			d_weights.add(1d);
	}
	
	/**
	 * Adds a feature.
	 * @param type  the feature type.
	 * @param value the feature value.
	 */
	public void addFeature(String type, String value)
	{
		s_types .add(type);
		s_values.add(value);
	}
	
	/**
	 * Adds a feature.
	 * @param type the feature type.
	 * @param value the feature value.
	 * @param weight the feature weight.
	 */
	public void addFeature(String type, String value, double weight)
	{
		s_types .add(type);
		s_values.add(value);
		d_weights.add(weight);
	}
	
	/**
	 * Adds a feature.
	 * @param feature {@code <type>}{@link StringFeatureVector#DELIM}{@code <value>[}{@link StringFeatureVector#DELIM}{@code <weight>]}.
	 */
	public void addFeature(String feature)
	{
		int idx0 = feature.indexOf(DELIM);
		s_types.add(feature.substring(0, idx0));
		
		if (b_weight)
		{
			int idx1 = feature.lastIndexOf(DELIM);
			s_values .add(feature.substring(idx0+1, idx1));
			d_weights.add(Double.parseDouble(feature.substring(idx1+1)));	
		}
		else
			s_values.add(feature.substring(idx0+1));
	}
	
	public void addFeatures(StringFeatureVector vector)
	{
		List<String> types  = vector.s_types;
		List<String> values = vector.s_values;
		DoubleArrayList weights = vector.d_weights;
		int i, size = vector.size();
		
		for (i=0; i<size; i++)
		{
			s_types .add(types .get(i));
			s_values.add(values.get(i));
			if (weights != null)	d_weights.add(weights.get(i));
		}
	}
	
	/**
	 * Returns the index'th feature type.
	 * @param index the index of the feature type to return.
	 * @return the index'th feature type.
	 */
	public String getType(int index)
	{
		return s_types.get(index);
	}
	
	/**
	 * Returns the index'th feature value.
	 * @param index the index of the feature value to return.
	 * @return the index'th feature value.
	 */
	public String getValue(int index)
	{
		return s_values.get(index);
	}
	
	/**
	 * Returns the total number of features in this vector.
	 * @return the total number of features in this vector.
	 */
	public int size()
	{
		return s_types.size();
	}
	
	public boolean isEmpty()
	{
		return s_types.isEmpty();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		StringBuilder build = new StringBuilder();
		int i, size = s_types.size();
		
		for (i=0; i<size; i++)
		{
			build.append(AbstractTrainSpace.DELIM_COL);
			build.append(s_types.get(i));
			build.append(DELIM);
			build.append(s_values.get(i));
			
			if (b_weight)
			{
				build.append(DELIM);
				build.append(d_weights.get(i));
			}
		}
		
		return build.toString().substring(AbstractTrainSpace.DELIM_COL.length());
	}
}
