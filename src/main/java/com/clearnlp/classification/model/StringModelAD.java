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
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntIntOpenHashMap;
import com.carrotsearch.hppc.cursors.ObjectCursor;
import com.clearnlp.classification.instance.IntInstance;
import com.clearnlp.classification.instance.StringInstance;
import com.clearnlp.classification.prediction.IntPrediction;
import com.clearnlp.classification.prediction.StringPrediction;
import com.clearnlp.classification.train.InstanceCollector;
import com.clearnlp.classification.vector.SparseFeatureVector;
import com.clearnlp.classification.vector.StringFeatureVector;
import com.clearnlp.collection.list.FloatArrayList;
import com.clearnlp.collection.map.ObjectIntHashMap;
import com.clearnlp.util.UTArray;
import com.clearnlp.util.UTCollection;
import com.clearnlp.util.pair.ObjectIntPair;
import com.clearnlp.util.pair.Pair;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * String online model.
 * @since 2.0.1
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class StringModelAD implements Serializable
{
	private static final long serialVersionUID = -8388835844936751367L;
	
	/** The map between labels and their indices. */
	protected ObjectIntHashMap<String> m_labels;
	/** The list of all labels. */
	protected List<String> a_labels;
	/** The total number of labels. */
	protected int n_labels;
	
	/** The map between features and their indices. */
	protected Map<String,ObjectIntHashMap<String>> m_features;
	/** The total dimension of features. */
	protected int n_features;
	
	/** The weight vector for all labels. */
	protected FloatArrayList f_weights;
	
	// For training
	protected InstanceCollector i_collector;
	protected List<IntInstance> l_instances;
	protected IntArrayList      l_indices;
	protected Random            r_shuffle;
	
	/** Constructs a string online model for training. */
	public StringModelAD()
	{
		i_collector = new InstanceCollector();
		init();
	}
	
	public void init()
	{
		m_labels    = new ObjectIntHashMap<String>();
		a_labels    = Lists.newArrayList();
		n_labels    = 0;
		m_features  = Maps.newHashMap();
		n_features  = 1;
		f_weights   = new FloatArrayList();		
	}
	
	public void trimFeatures(Logger log, float threshold)
	{
		FloatArrayList tWeights = new FloatArrayList(f_weights.size());
		IntIntOpenHashMap map = new IntIntOpenHashMap();
		ObjectIntHashMap<String> m;
		int i, j, tFeatures = 1;
		boolean trim;
		String s;
		
		log.info("Trimming: ");
		
		// bias
		for (j=0; j<n_labels; j++)
			tWeights.add(f_weights.get(j));
		
		// rest
		for (i=1; i<n_features; i++)
		{
			trim = true;
			
			for (j=0; j<n_labels; j++)
			{
				if (Math.abs(f_weights.get(i*n_labels+j)) > threshold)
				{
					trim = false;
					break;
				}
			}
			
			if (!trim)
			{
				map.put(i, tFeatures++);
				
				for (j=0; j<n_labels; j++)
					tWeights.add(f_weights.get(i*n_labels+j));				
			}
		}
		
		log.info(String.format("%d -> %d\n", n_features, tFeatures));
		tWeights.trimToSize();
		
		// map
		for (String type : Lists.newArrayList(m_features.keySet()))
		{
			m = m_features.get(type);
			
			for (ObjectIntPair<String> p : m.toList())
			{
				i = map.get(p.i);
				s = (String)p.o;
				
				if (i > 0)	m.put(s, i);
				else		m.remove(s);
			}
			
			if (m.isEmpty())
				m_features.remove(type);
		}
		
		f_weights  = tWeights;
		n_features = tFeatures;
	}
	
// ================================ SERIALIZE ================================
	
	@SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		m_labels    = (ObjectIntHashMap<String>)in.readObject();
		a_labels    = (List<String>)in.readObject();
		n_labels    = (int)in.readObject();
		m_features  = (Map<String,ObjectIntHashMap<String>>)in.readObject();
		n_features  = (int)in.readObject();
		f_weights   = (FloatArrayList)in.readObject();
		i_collector = new InstanceCollector();
	}
		
	private void writeObject(ObjectOutputStream out) throws IOException
	{
		out.writeObject(m_labels);
		out.writeObject(a_labels);
		out.writeObject(n_labels);
		out.writeObject(m_features);
		out.writeObject(n_features);
		out.writeObject(f_weights);
	}
	
// ================================ LABEL ================================
	
	public void addLabel(String label)
	{
		if (m_labels.containsKey(label))
			return;

		m_labels.put(label, ++n_labels);
		a_labels.add(label);
		
		int i, index;
		
		for (i=0; i<n_features; i++)
		{
			index = (i+1) * n_labels - 1;
			f_weights.insert(index, 0f);
		}
	}
	
	public String getLabel(int labelIndex)
	{
		return a_labels.get(labelIndex);
	}
	
	public List<String> getLabels()
	{
		return a_labels;
	}
	
	/**
	 * Returns the index of the specific label.
	 * Returns {@code -1} if the label does not exist in this model.
	 */
	public int getLabelIndex(String label)
	{
		return m_labels.get(label) - 1;
	}
				
	/** @return the total number of labels in this model. */
	public int getLabelSize()
	{
		return n_labels;
	}
	
	/** @return {@code true} if this model contains only 2 labels. */
	public boolean isBinaryLabel()
	{
		return n_labels == 2;
	}
	
// ================================ FEATURE ================================
	
	public Map<String,ObjectIntHashMap<String>> getFeatureMap()
	{
		return m_features;
	}
	
	/** @return the total number of features in this model. */
	public int getFeatureSize()
	{
		return n_features;
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
			map = m_features.get(type);
		else
		{
			map = new ObjectIntHashMap<String>();
			m_features.put(type, map);
		}
		
		if (!map.containsKey(value))
		{
			map.put(value, n_features++);
			
			int i; for (i=0; i<n_labels; i++)
				f_weights.add(0f);
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
		ObjectIntHashMap<String> map;
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
	
	/** @return {@code true} if the specific feature index is within the range of this model. */
	public boolean isValidFeature(int featureIndex)
	{
		return 0 <= featureIndex && featureIndex < n_features;
	}
	
// ================================ WEIGHT ================================
	
	public FloatArrayList cloneWeights()
	{
		return f_weights.clone();
	}
	
	public FloatArrayList getWeights()
	{
		return f_weights;
	}
	
	public int getWeightIndex(int labelIndex, int featureIndex)
	{
		return featureIndex * n_labels + labelIndex;
	}
	
	public void setWeights(FloatArrayList weights)
	{
		f_weights = weights;
	}
	
	public void setWeights(double[] weights)
	{
		int i, size = f_weights.size();
		
		for (i=0; i<size; i++)
			f_weights.set(i, (float)weights[i]);
	}
	
	public void setAverageWeights(double[] weights, int count)
	{
		int i, size = weights.length;
		double c = 1d / count;
		
		for (i=0; i<size; i++)
			f_weights.set(i, (float)(f_weights.get(i) - weights[i]*c));
	}
	
	public void updateWeight(int labelIndex, int featureIndex, float update)
	{
		int index = getWeightIndex(labelIndex, featureIndex); 
		f_weights.set(index, f_weights.get(index)+update);
	}
	
// ================================ INSTANCE ================================
	
	public void addInstances(Collection<StringInstance> instances)
	{
		for (StringInstance instance : instances)
			addInstance(instance);
	}
	
	public void addInstance(StringInstance instance)
	{
		i_collector.addInstance(instance);
	}
	
	public IntInstance getInstance(int index)
	{
		return l_instances.get(index);
	}
	
	public int getInstanceSize()
	{
		return l_instances.size();
	}
	
	public void shuffleIndices()
	{
		UTArray.shuffle(r_shuffle, l_indices);
	}
	
	public int getShuffledIndex(int index)
	{
		return l_indices.get(index);
	}
	
// ================================ BUILD ================================

	public void build(int labelCutoff, int featureCutoff, int randomSeed, boolean initialize)
	{
		SparseFeatureVector vector;
		StringInstance instance;
		int label;
		
		if (initialize) init();
		buildLabels(labelCutoff);
		buildFeatures(featureCutoff);
		
		l_instances = Lists.newArrayList();
		r_shuffle = new Random(randomSeed);
		l_indices = new IntArrayList();
		
		while ((instance = i_collector.pollInstance()) != null)
		{
			if ((label = getLabelIndex(instance.getLabel())) < 0)
				continue;
			
			vector = toSparseFeatureVector(instance.getFeatureVector());
			
			if (!vector.isEmpty())
			{
				l_instances.add(new IntInstance(label, vector));
				l_indices.add(l_indices.size());
			}
		}
	}
	
	/** Called by {@link #build(int, int)}. */
	private void buildLabels(int labelCutoff)
	{
		for (String label : i_collector.getLabels())
		{
			if (i_collector.getLabelCount(label) > labelCutoff)
				addLabel(label);
		}
		
		i_collector.clearLabels();
	}
	
	/** Called by {@link #build(int, int)}. */
	private void buildFeatures(int featureCutoff)
	{
		ObjectIntHashMap<String> map;
		String value;
		
		for (String type : i_collector.getFeatureTypes())
		{
			map = i_collector.getFeatureMap(type);
			
			for (ObjectCursor<String> cur : map.keys())
			{
				value = cur.value;
				
				if (map.get(value) > featureCutoff)
					addFeature(type, value);
			}
		}
		
		i_collector.clearFeatures();
	}

// ================================ PREDICT ================================
	
	/** @return the best prediction given the sparse feature vector. */
	public StringPrediction predictBest(SparseFeatureVector x)
	{
		return Collections.max(getStringPredictions(x));
	}
	
	/** @return the best prediction given the string feature vector. */
	public StringPrediction predictBest(StringFeatureVector x)
	{
		return predictBest(toSparseFeatureVector(x));
	}
	
	/** @return the first and second best predictions given the sparse feature vector. */
	public Pair<StringPrediction,StringPrediction> predictTop2(SparseFeatureVector x)
	{
		return predictTop2(getStringPredictions(x));
	}
	
	/** @return the first and second best predictions given the string feature vector. */
	public Pair<StringPrediction,StringPrediction> predictTop2(StringFeatureVector x)
	{
		return predictTop2(toSparseFeatureVector(x));
	}
	
	/** @return the first and second best predictions given the list of string predictions. */
	public Pair<StringPrediction,StringPrediction> predictTop2(List<StringPrediction> list)
	{
		StringPrediction fst = list.get(0), snd = list.get(1), p;
		int i, size = list.size();
		
		if (fst.score < snd.score)
		{
			fst = snd;
			snd = list.get(0);
		}
		
		for (i=2; i<size; i++)
		{
			p = list.get(i);
			
			if (fst.score < p.score)
			{
				snd = fst;
				fst = p;
			}
			else if (snd.score < p.score)
				snd = p;
		}
		
		return new Pair<StringPrediction,StringPrediction>(fst, snd);
	}
	
	/** @return a sorted list of predictions given the sparse feature vector. */
	public List<StringPrediction> predictAll(SparseFeatureVector x)
	{
		List<StringPrediction> list = getStringPredictions(x);
		UTCollection.sortReverseOrder(list);
		
		return list;
	}
	
	/** @return a sorted list of predictions given the string feature vector. */
	public List<StringPrediction> predictAll(StringFeatureVector x)
	{
		return predictAll(toSparseFeatureVector(x));
	}
	
	/** @return an unsorted list of string predictions given the sparse feature vector. */
	public List<StringPrediction> getStringPredictions(SparseFeatureVector x)
	{
		List<StringPrediction> list = Lists.newArrayList();
		double[] scores = getScores(x);
		int i;
		
		for (i=0; i<n_labels; i++)
			list.add(new StringPrediction(a_labels.get(i), scores[i]));
		
		return list;		
	}
	
	/** @return an unsorted list of string  predictions given the string feature vector. */
	public List<StringPrediction> getStringPredictions(StringFeatureVector x)
	{
		return getStringPredictions(toSparseFeatureVector(x));
	}
	
	/** @return an unsorted list of int predictions given the sparse feature vector. */
	public List<IntPrediction> getIntPredictions(SparseFeatureVector x)
	{
		List<IntPrediction> list = Lists.newArrayList();
		double[] scores = getScores(x);
		int i;
		
		for (i=0; i<n_labels; i++)
			list.add(new IntPrediction(i, scores[i]));
		
		return list;
	}
	
	/** @return an unsorted list of int predictions given the string feature vector. */
	public List<IntPrediction> getIntPredictions(StringFeatureVector x)
	{
		return getIntPredictions(toSparseFeatureVector(x));
	}
	
	// ========================= SCORE =========================
	
	/**
	 * For binary classification, this method calls {@link #getScoresBinary(SparseFeatureVector)}.
	 * For multi-classification, this method calls {@link #getScoresMulti(SparseFeatureVector)}.
	 * @param x the feature vector.
	 * @return the scores of all labels given the feature vector.
	 */
	public double[] getScores(SparseFeatureVector x)
	{
		return isBinaryLabel() ? getScoresBinary(x) : getScoresMulti(x);
	}
	
	public double[] getScores(SparseFeatureVector x, boolean normalize)
	{
		double[] scores = getScores(x);
		
		if (normalize) normalize(scores);
		return scores;
	}

	/**
	 * @param x the feature vector.
	 * @return the scores of all labels given the feature vector.
	 */
	private double[] getScoresBinary(SparseFeatureVector x)
	{
		int i, featureIndex, weightIndex, size = x.size();
		double score = f_weights.get(0);
		
		for (i=0; i<size; i++)
		{
			featureIndex = x.getIndex(i);
			
			if (isValidFeature(featureIndex))
			{		
				weightIndex = getWeightIndex(0, featureIndex);
				score += f_weights.get(weightIndex) * x.getWeight(i);
			}
		}
		
		double[] scores = {score, -score};
		return scores;
	}
	
	/**
	 * @param x the feature vector.
	 * @return the scores of all labels given the feature vector.
	 */
	private double[] getScoresMulti(SparseFeatureVector x)
	{
		int i, featureIndex, weightIndex, labelIndex, size = x.size();
		double[] scores = f_weights.toDoubleArray(0, n_labels);
		double weight;
		
		for (i=0; i<size; i++)
		{
			featureIndex = x.getIndex(i);
			weight = x.getWeight(i);
			
			if (isValidFeature(featureIndex))
			{
				for (labelIndex=0; labelIndex<n_labels; labelIndex++)
				{
					weightIndex = getWeightIndex(labelIndex, featureIndex);
					scores[labelIndex] += f_weights.get(weightIndex) * weight;
				}
			}
		}
		
		return scores;
	}
	
	private void normalize(double[] scores)
	{
		int i, size = scores.length;
		double d, sum = 0;
		
		for (i=0; i<size; i++)
		{
			d = Math.exp(scores[i]);
			scores[i] = d;
			sum += d;
		}
		
		for (i=0; i<size; i++)
			scores[i] /= sum;
	}
	
	public void printInfo(Logger log)
	{
		log.info("- # of labels   : "+getLabelSize()+"\n");
		log.info("- # of features : "+getFeatureSize()+"\n");
		log.info("- # of instances: "+getInstanceSize()+"\n");
	}
}
