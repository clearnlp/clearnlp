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
package com.clearnlp.classification.train;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import com.carrotsearch.hppc.cursors.ObjectCursor;
import com.clearnlp.classification.instance.StringInstance;
import com.clearnlp.classification.model.StringModel;
import com.clearnlp.classification.vector.SparseFeatureVector;
import com.clearnlp.classification.vector.StringFeatureVector;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;


/**
 * Train space containing string vectors.
 * @since 1.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class StringTrainSpace extends AbstractTrainSpace
{
	/** Casted from {@likn AbstractTrainSpace#m_model}. */
	private StringModel s_model;
	/** The label count cutoff (exclusive). */
	private int l_cutoff;
	/** The feature count cutoff (exclusive). */
	private int f_cutoff;
	/** The list of all training instances. */
	private List<StringInstance> s_instances;
	/** The map between labels and their counts. */
	private ObjectIntOpenHashMap<String> m_labels;
	/** The map between features and their counts. */
	private Map<String,ObjectIntOpenHashMap<String>> m_features;
	
	/**
	 * Constructs a train space containing string vectors.
	 * @param hasWeight {@code true} if features are assigned with different weights.
	 * @param labelCutoff the label count cutoff (exclusive).
	 * @param featureCutoff the feature count cutoff (exclusive).
	 */
	public StringTrainSpace(boolean hasWeight, int labelCutoff, int featureCutoff)
	{
		super(new StringModel(), hasWeight);
		
		s_model     = (StringModel)m_model;
		l_cutoff    = labelCutoff;
		f_cutoff    = featureCutoff;
		s_instances = Lists.newArrayList();
		m_labels    = new ObjectIntOpenHashMap<String>();
		m_features  = Maps.newHashMap();
	}
	
	public void printInstances(PrintStream fout)
	{
		int i, size = s_instances.size();
		String[] instances = new String[size];
		StringInstance p;
		
		for (i=0; i<size; i++)
		{
			p = s_instances.get(i);
			instances[i] = p.getLabel() + DELIM_COL + p.getFeatureVector().toString();	
		}
		
		Arrays.sort(instances);
		
		for (String instance : instances)
			fout.println(instance);
	}
	
	/** Adds a training instance to this space. */
	public void addInstance(StringInstance instance)
	{
		addLexica(instance);
		s_instances.add(instance);
	}
	
	public void addInstances(Collection<StringInstance> instances)
	{
		for (StringInstance instance : instances)
			addInstance(instance);
	}
	
	/**
	 * Adds a training instance to this space.
	 * @param line {@code <label>}{@link AbstractTrainSpace#DELIM_COL}{@link StringFeatureVector#toString()}.
	 */
	public void addInstance(String line)
	{
		addInstance(toInstance(line, b_weight));
	}
	
	public void appendSpace(StringTrainSpace space)
	{
		appendSpaceLabels(space);
		appendSpaceFeatures(space);
		appendSpaceInstances(space);
	}
	
	private void appendSpaceLabels(StringTrainSpace space)
	{
		ObjectIntOpenHashMap<String> mLabels = space.m_labels;
		String label;
		
		for (ObjectCursor<String> cur : mLabels.keys())
		{
			label = cur.value;
			m_labels.put(label, m_labels.get(label) + mLabels.get(label));
		}
	}
	
	private void appendSpaceFeatures(StringTrainSpace space)
	{
		Map<String,ObjectIntOpenHashMap<String>> mFeatures = space.m_features;
		ObjectIntOpenHashMap<String> tMap, sMap;
		String value;
		
		for (String type : mFeatures.keySet())
		{
			sMap = mFeatures.get(type);
			
			if (m_features.containsKey(type))
			{
				tMap = m_features.get(type);
				
				for (ObjectCursor<String> cur : sMap.keys())
				{
					value = cur.value;
					tMap.put(value, tMap.get(value) + sMap.get(value));
				}
			}
			else
				m_features.put(type, sMap);
		}
	}
	
	private void appendSpaceInstances(StringTrainSpace space)
	{
		s_instances.addAll(space.s_instances);
	}
	
	public void clear()
	{
		s_instances.clear();
		m_labels   .clear();
		m_features .clear();
	}

	private void addLexica(StringInstance instance)
	{
		addLexicaLabel(instance.getLabel());
		addLexicaFeatures(instance.getFeatureVector());
	}
	
	private void addLexicaLabel(String label)
	{
		m_labels.put(label, m_labels.get(label)+1);
	}
	
	private void addLexicaFeatures(StringFeatureVector vector)
	{
		ObjectIntOpenHashMap<String> map;
		int i, size = vector.size();
		String type, value;
		
		for (i=0; i<size; i++)
		{
			type  = vector.getType(i);
			value = vector.getValue(i);
			
			if (m_features.containsKey(type))
			{
				map = m_features.get(type);
				map.put(value, map.get(value)+1);
			}
			else
			{
				map = new ObjectIntOpenHashMap<String>();
				map.put(value, 1);
				m_features.put(type, map);
			}
		}
	}
	
	@Override
	public void build(boolean clearInstances)
	{
		LOG.info("Building:\n");
		initModelMaps();
		
		StringInstance instance;
		int y, i, size = s_instances.size();
		SparseFeatureVector x;
		
		for (i=0; i<size; i++)
		{
			instance = s_instances.get(i);
			
			if ((y = s_model.getLabelIndex(instance.getLabel())) < 0)
				continue;
			
			x = s_model.toSparseFeatureVector(instance.getFeatureVector());
			
			a_ys.add(y);
			a_xs.add(x.getIndices());
			if (b_weight)	a_vs.add(x.getWeights());
		}
		
		a_ys.trimToSize();
		a_xs.trimToSize();
		if (b_weight)	a_vs.trimToSize();
		
		LOG.info("- # of labels   : "+s_model.getLabelSize()+"\n");
		LOG.info("- # of features : "+s_model.getFeatureSize()+"\n");
		LOG.info("- # of instances: "+a_ys.size()+"\n");
		
		if (clearInstances)	s_instances.clear();
	}
	
	@Override
	public void build()
	{
		build(true);
	}
	
	/** Called by {@link StringTrainSpace#build()}. */
	private void initModelMaps()
	{
		// initialize label map
		String label;
		
		for (ObjectCursor<String> cur : m_labels.keys())
		{
			label = cur.value;

			if (m_labels.get(label) > l_cutoff)
				s_model.addLabel(label);
		}
		
		s_model.initLabelArray();
		
		// initialize feature map
		ObjectIntOpenHashMap<String> map;
		String value;
		
		for (String type : m_features.keySet())
		{
			map = m_features.get(type);
			
			for (ObjectCursor<String> cur : map.keys())
			{
				value = cur.value;
				
				if (map.get(value) > f_cutoff)
					s_model.addFeature(type, value);
			}
		}
		
	/*	for (String label : UTHppc.getSortedKeys(m_labels))
		{
			if (m_labels.get(label) > l_cutoff)
				s_model.addLabel(label);
		}
		
		s_model.initLabelArray();
		
		// initialize feature map
		List<String> types = new ArrayList<String>(m_features.keySet());
		ObjectIntOpenHashMap<String> map;
		Collections.sort(types);
		
		for (String type : types)
		{
			map = m_features.get(type);
			
			for (String value : UTHppc.getSortedKeys(map))
			{
				if (map.get(value) > f_cutoff)
					s_model.addFeature(type, value);
			}
		}*/
	}
	
	/** Pair of label and feature vector. */
	static public StringInstance toInstance(String line, boolean hasWeight)
	{
		String[] tmp = line.split(DELIM_COL);
		String label = tmp[0];
		
		StringFeatureVector vector = new StringFeatureVector(hasWeight);
		int i, size = tmp.length;
		
		for (i=1; i<size; i++)
			vector.addFeature(tmp[i]);
		
		return new StringInstance(label, vector);
	}
}
