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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.carrotsearch.hppc.cursors.ObjectCursor;
import com.clearnlp.classification.prediction.StringPrediction;
import com.clearnlp.classification.vector.SparseFeatureVector;
import com.clearnlp.collection.map.ObjectIntHashMap;
import com.clearnlp.util.UTArray;
import com.clearnlp.util.UTCollection;
import com.clearnlp.util.pair.Pair;

/**
 * Abstract model.
 * @since 1.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
abstract public class AbstractModel implements Serializable
{
	private static final long serialVersionUID = 1851285537812008020L;
	
	/** The total number of labels. */
	protected int      n_labels;
	/** The total number of features. */
	protected int      n_features;
	/** The weight vector for all labels. */
	protected float[]  d_weights;
	/** The list of all labels. */
	protected String[] a_labels;
	/** The map between labels and their indices. */
	protected ObjectIntHashMap<String> m_labels;
	
	protected double[] t_weights;
	
	/** Constructs an abstract model for training. */
	public AbstractModel()
	{
		n_labels   = 0;
		n_features = 1;
		m_labels   = new ObjectIntHashMap<String>();
	}
	
	// ========================= INITIALIZATION =========================
	
	/**
	 * Initializes the label array after adding all labels.
	 * @see StringModel#addLabel(String)
	 */
	public void initLabelArray()
	{
		a_labels = new String[n_labels];
		String label;
		
		for (ObjectCursor<String> cur : m_labels.keys())
		{
			label = cur.value;
			a_labels[getLabelIndex(label)] = label;
		}
	}
	
	/** Initializes the weight vector given the label and feature sizes. */
	public void initWeightVector()
	{
		d_weights = isBinaryLabel() ? new float[n_features] : new float[n_features * n_labels];
	}
	
	// ========================= GETTER =========================
	
	/** @return the total number of labels in this model. */
	public int getLabelSize()
	{
		return n_labels;
	}
	
	/** @return the total number of features in this model. */
	public int getFeatureSize()
	{
		return n_features;
	}
	
	/**
	 * Returns the index of the specific label.
	 * Returns {@code -1} if the label is not found in this model.
	 * @param label the label to get the index for.
	 * @return the index of the specific label.
	 */
	public int getLabelIndex(String label)
	{
		return m_labels.get(label) - 1;
	}
	
	/** @return the index of the weight vector given the label and the feature index. */
	protected int getWeightIndex(int label, int index)
	{
		return index * n_labels + label;
	}
	
	public String getLabel(int index)
	{
		return a_labels[index];
	}
	
	public String[] getLabels()
	{
		return a_labels;
	}
	
	public float[] getWeights()
	{
		return d_weights;
	}
	
	public float[] getWeights(int label)
	{
		float[] weights = new float[n_features];
		int i;
		
		for (i=0; i<n_features; i++)
			weights[i] = d_weights[getWeightIndex(label, i)];
		
		return weights;
	}
	
	// ========================= SETTER =========================
	
	/**
	 * Adds the specific label to this model.
	 * @param label the label to be added.
	 */
	public void addLabel(String label)
	{
		if (!m_labels.containsKey(label))
			m_labels.put(label, ++n_labels);
	}
	
	public void setWeights(float[] weights)
	{
		d_weights = weights; 
	}
	
	/**
	 * Copies a weight vector for binary classification.
	 * @param weights the weight vector to be copied. 
	 */
	public void copyWeights(float[] weights)
	{
		System.arraycopy(weights, 0, d_weights, 0, n_features);
	}
	
	/**
	 * Copies a weight vector of the specific label (for multi-classification).
	 * @param weights the weight vector to be copied.
	 * @param label the label of the weight vector.
	 */
	public void copyWeights(float[] weights, int label)
	{
		int i;
		
		for (i=0; i<n_features; i++)
			d_weights[getWeightIndex(label, i)] = weights[i];
	}
	
	// ========================= BOOLEAN =========================
	
	/** @return {@code true} if this model contains only 2 labels. */
	public boolean isBinaryLabel()
	{
		return n_labels == 2;
	}
	
	/**
	 * @param featureIndex the index of the feature.
	 * @return {@code true} if the specific feature index is within the range of this model.
	 */
	public boolean isRange(int featureIndex)
	{
		return 0 < featureIndex && featureIndex < n_features;
	}
	
	// ========================= LOAD/SAVE =========================
	
	/**
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("unchecked")
	protected void loadDefault(ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		a_labels   = (String[])in.readObject();
		m_labels   = (ObjectIntHashMap<String>)in.readObject();
		d_weights  = (float[])in.readObject();
		
		n_labels   = a_labels.length;
		n_features = d_weights.length;
		if (!isBinaryLabel()) n_features /= n_labels;
	}
	
	/** @throws IOException */
	protected void saveDefault(ObjectOutputStream out) throws IOException
	{
		out.writeObject(a_labels);
		out.writeObject(m_labels);
		out.writeObject(d_weights);
	}
	
	// ========================= SCORES =========================
	
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

	/**
	 * @param x the feature vector.
	 * @return the scores of all labels given the feature vector.
	 */
	private double[] getScoresBinary(SparseFeatureVector x)
	{
		double score = d_weights[0];
		int    i, index, size = x.size();
		
		for (i=0; i<size; i++)
		{
			index = x.getIndex(i);
			
			if (isRange(index))
			{
				if (x.hasWeight())
					score += d_weights[index] * x.getWeight(i);
				else
					score += d_weights[index];
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
		double[] scores = UTArray.copyOf(d_weights, n_labels);
		int      i, index, label, weightIndex, size = x.size();
		double   weight = 1;
		
		for (i=0; i<size; i++)
		{
			index = x.getIndex(i);
			if (x.hasWeight())	weight = x.getWeight(i);
			
			if (isRange(index))
			{
				for (label=0; label<n_labels; label++)
				{
					weightIndex = getWeightIndex(label, index);
					
					if (x.hasWeight())	scores[label] += d_weights[weightIndex] * weight;
					else				scores[label] += d_weights[weightIndex];
				}
			}
		}
		
		return scores;
	}
	
	/**
	 * Returns the best prediction given the feature vector.
	 * @param x the feature vector.
	 * @return the best prediction given the feature vector.
	 */
	public StringPrediction predictBest(SparseFeatureVector x)
	{
		return Collections.max(getPredictions(x));
	}
	
	/**
	 * Returns the first and second best predictions given the feature vector.
	 * @param x the feature vector.
	 * @return the first and second best predictions given the feature vector.
	 */
	public Pair<StringPrediction,StringPrediction> predictTwo(SparseFeatureVector x)
	{
		return predictTwo(getPredictions(x));
	}
	
	public Pair<StringPrediction,StringPrediction> predictTwo(List<StringPrediction> list)
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
	
	/**
	 * Returns a sorted list of predictions given the specific feature vector.
	 * @param x the feature vector.
	 * @return a sorted list of predictions given the specific feature vector.
	 */
	public List<StringPrediction> predictAll(SparseFeatureVector x)
	{
		List<StringPrediction> list = getPredictions(x);
		UTCollection.sortReverseOrder(list);
		
		return list;
	}
	
	/**
	 * Returns an unsorted list of predictions given the specific feature vector.
	 * @param x the feature vector.
	 * @return an unsorted list of predictions given the specific feature vector.
	 */
	public List<StringPrediction> getPredictions(SparseFeatureVector x)
	{
		List<StringPrediction> list = new ArrayList<StringPrediction>(n_labels);
		double[] scores = getScores(x);
		int i;
		
		for (i=0; i<n_labels; i++)
			list.add(new StringPrediction(a_labels[i], scores[i]));
		
		return list;		
	}

	
	
	
	
	
	
	
	
	
	static public String LABEL_TRUE  = "T";
	static public String LABEL_FALSE = "F";
	
	static public String getBooleanLabel(boolean b)
	{
		return b ? LABEL_TRUE : LABEL_FALSE;
	}
	
	static public boolean toBoolean(String label)
	{
		return label.equals(LABEL_TRUE);
	}
}
