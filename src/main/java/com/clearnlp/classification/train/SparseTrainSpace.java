/**
* Copyright (c) 2009-2012, Regents of the University of Colorado
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
*
* Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
* Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
* Neither the name of the University of Colorado at Boulder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
* AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
* IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
* ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
* LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
* CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
* ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
* POSSIBILITY OF SUCH DAMAGE.
*/
package com.clearnlp.classification.train;

import java.util.ArrayList;
import java.util.List;

import com.clearnlp.classification.model.SparseModel;
import com.clearnlp.classification.vector.SparseFeatureVector;
import com.clearnlp.util.pair.Pair;


/**
 * Train space containing sparse vectors.
 * @since 1.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class SparseTrainSpace extends AbstractTrainSpace
{
	/** Casted from {@likn AbstractTrainSpace#m_model}. */
	private SparseModel s_model;
	/** The list of all labels. */
	private List<String> s_ys;
	
	/**
	 * Constructs a train space containing sparse vectors.
	 * @param hasWeight {@code true} if features are assigned with different weights.
	 */
	public SparseTrainSpace(boolean hasWeight)
	{
		super(new SparseModel(), hasWeight);
		s_model = (SparseModel)m_model;
		s_ys    = new ArrayList<String>();
	}
	
	/**
	 * Adds a training instance to this space.
	 * @param label the label to be added.
	 * @param vector the feature vector to be added.
	 */
	public void addInstance(String label, SparseFeatureVector vector)
	{
		int[] x = vector.getIndices();
		
		s_model.addLabel(label);
		s_model.addFeatures(x);
		
		s_ys.add(label);
		a_xs.add(x);
		if (b_weight)	a_vs.add(vector.getWeights());
	}
	
	/**
	 * Adds a training instance to this space.
	 * @param line {@code <label>}{@link AbstractTrainSpace#DELIM_COL}{@link SparseFeatureVector#toString()}.
	 */
	public void addInstance(String line)
	{
		Pair<String,SparseFeatureVector> instance = toInstance(line, b_weight);
		addInstance(instance.o1, instance.o2);
	}
	
	@Override
	public void build(boolean clearInstances)
	{
		LOG.info("Building:\n");
		s_model.initLabelArray();
		
		for (String label : s_ys)
			a_ys.add(s_model.getLabelIndex(label));
		
		LOG.info("- # of labels   : "+s_model.getLabelSize()+"\n");
		LOG.info("- # of features : "+s_model.getFeatureSize()+"\n");
		LOG.info("- # of instances: "+a_ys.size()+"\n");
		
		if (clearInstances)	s_ys.clear();
	}
		
	@Override
	public void build()
	{
		build(true);
	}
	
	/**
	 * Returns the pair of label and feature vector parsed from the specific string.
	 * @param line the string to get the pair of label and feature vector from.
	 * @param hasWeight {@code true} if this vector has a different weight for each feature. 
	 * @return the pair of label and feature vector parsed from the specific string.
	 */
	static public Pair<String,SparseFeatureVector> toInstance(String line, boolean hasWeight)
	{
		String[] tmp = line.split(DELIM_COL);
		String label = tmp[0];
		
		SparseFeatureVector vector = new SparseFeatureVector(hasWeight);
		int i, size = tmp.length;
		
		for (i=1; i<size; i++)
			vector.addFeature(tmp[i]);
		
		return new Pair<String,SparseFeatureVector>(label, vector);
	}
}
