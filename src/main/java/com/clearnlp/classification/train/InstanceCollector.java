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
 * Copyright 2012/09-2013/04, University of Massachusetts Amherst
 * Copyright 2013/05-Present, IPSoft Inc.
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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Set;

import com.clearnlp.classification.instance.StringInstance;
import com.clearnlp.classification.vector.StringFeatureVector;
import com.clearnlp.collection.map.ObjectIntHashMap;
import com.clearnlp.util.UTHppc;
import com.google.common.collect.Maps;

/**
 * @since 2.0.1
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class InstanceCollector
{
	private Deque<StringInstance>                s_instances;
	private ObjectIntHashMap<String>             m_labels;
	private Map<String,ObjectIntHashMap<String>> m_features;
	
	public InstanceCollector()
	{
		s_instances = new ArrayDeque<StringInstance>();
		m_labels    = new ObjectIntHashMap<String>();
		m_features  = Maps.newHashMap();
	}
	
	public void addInstance(StringInstance instance)
	{
		s_instances.add(instance);
		addLabel(instance.getLabel());
		addFeatures(instance.getFeatureVector());
	}
	
	/** Called by {@link #addLexica(StringInstance)}. */
	private void addLabel(String label)
	{
		m_labels.put(label, m_labels.get(label)+1);
	}
	
	/** Called by {@link #addLexica(StringInstance)}. */
	private void addFeatures(StringFeatureVector vector)
	{
		ObjectIntHashMap<String> map;
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
				map = new ObjectIntHashMap<String>();
				map.put(value, 1);
				m_features.put(type, map);
			}
		}
	}

	public StringInstance pollInstance()
	{
		return s_instances.poll();
	}
	
	public Set<String> getLabels()
	{
		return UTHppc.getKeySet(m_labels);
	}
	
	public Set<String> getFeatureTypes()
	{
		return m_features.keySet();
	}
	
	public int getLabelCount(String label)
	{
		return m_labels.get(label);
	}
	
	public ObjectIntHashMap<String> getFeatureMap(String type)
	{
		return m_features.get(type);
	}
	
	public void clearLabels()
	{
		m_labels.clear();
	}
	
	public void clearFeatures()
	{
		m_features.clear();
	}
}
