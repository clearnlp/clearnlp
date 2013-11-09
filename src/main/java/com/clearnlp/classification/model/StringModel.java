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
package com.clearnlp.classification.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import com.clearnlp.classification.prediction.StringPrediction;
import com.clearnlp.classification.vector.SparseFeatureVector;
import com.clearnlp.classification.vector.StringFeatureVector;
import com.clearnlp.collection.map.ObjectIntHashMap;
import com.clearnlp.util.pair.Pair;


/**
 * String vector model.
 * @since 1.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class StringModel extends AbstractModel
{
	private static final long serialVersionUID = 5238076252059236959L;

	/** The map between features and their indices. */
	protected Map<String,ObjectIntHashMap<String>> m_features;
	
	/** Constructs a string model for training. */
	public StringModel()
	{
		super();
		m_features = new HashMap<String,ObjectIntHashMap<String>>();
	}
	
	@SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		loadDefault(in);
		m_features = (Map<String,ObjectIntHashMap<String>>)in.readObject();
	}

	private void writeObject(ObjectOutputStream out) throws IOException
	{
		saveDefault(out);
		out.writeObject(m_features);
	}
	
	/**
	 * Adds the specific feature to this model.
	 * @param type the feature type.
	 * @param value the feature value.
	 */
	public void addFeature(String type, String value)
	{
		ObjectIntHashMap<String> map;
		
		if (m_features.containsKey(type))
		{
			map = m_features.get(type);
			if (!map.containsKey(value))
				map.put(value, n_features++);
		}
		else
		{
			map = new ObjectIntHashMap<String>();
			map.put(value, n_features++);
			m_features.put(type, map);
		}
	}

	/**
	 * Returns the sparse feature vector converted from the string feature vector.
	 * During the conversion, discards features not found in this model.
	 * @param vector the string feature vector.
	 * @return the sparse feature vector converted from the string feature vector.
	 */
	public SparseFeatureVector toSparseFeatureVector(StringFeatureVector vector)
	{
		SparseFeatureVector sparse = new SparseFeatureVector(vector.hasWeight());
		int i, index, size = vector.size();
		ObjectIntOpenHashMap<String> map;
		String type, value;
		
		for (i=0; i<size; i++)
		{
			type  = vector.getType(i);
			value = vector.getValue(i);
			
			if ((map = m_features.get(type)) != null && (index = map.get(value)) > 0)
			{
				if (sparse.hasWeight())
					sparse.addFeature(index, vector.getWeight(i));
				else
					sparse.addFeature(index);
			}
		}
		
		sparse.trimToSize();
		return sparse;
	}
	
	public StringFeatureVector trimFeatures(StringFeatureVector oVector, String label, double threshold)
	{
		StringFeatureVector nVector = new StringFeatureVector(oVector.hasWeight());
		int i, size = oVector.size(), fIndex, lIndex = getLabelIndex(label);
		ObjectIntOpenHashMap<String> map;
		String type, value;
		boolean add;
		
		for (i=0; i<size; i++)
		{
			type  = oVector.getType(i);
			value = oVector.getValue(i);
			add   = false;
			
			if ((map = m_features.get(type)) != null && (fIndex = map.get(value)) > 0)
			{
				if (d_weights[getWeightIndex(lIndex, fIndex)] == threshold)
					add = true;
			}
			else
				add = true;
			
			if (add)
			{
				if (nVector.hasWeight())
					nVector.addFeature(type, value, oVector.getWeight(i));
				else
					nVector.addFeature(type, value);
			}
		}
		
		return nVector;
	}
	
	public StringPrediction predictBest(StringFeatureVector x)
	{
		return predictBest(toSparseFeatureVector(x));
	}
	
	public Pair<StringPrediction,StringPrediction> predictTwo(StringFeatureVector x)
	{
		return predictTwo(toSparseFeatureVector(x));
	}
	
	public List<StringPrediction> predictAll(StringFeatureVector x)
	{
		return predictAll(toSparseFeatureVector(x));
	}
	
	public List<StringPrediction> getPredictions(StringFeatureVector x)
	{
		return getPredictions(toSparseFeatureVector(x));
	}	
}
