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
package com.clearnlp.classification.model;

import static org.junit.Assert.assertEquals;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.junit.Test;

import com.clearnlp.classification.model.SparseModel;
import com.clearnlp.classification.prediction.StringPrediction;
import com.clearnlp.classification.vector.SparseFeatureVector;


/** @author Jinho D. Choi ({@code jdchoi77@gmail.com}) */
public class SparseModelTest
{
	@Test
	public void testSparseModelMultiClassification() throws Exception
	{
		SparseModel model = new SparseModel();
		String[] labels   = {"A", "B", "C"};
		int[]    features = {1, 2, 3, 4};

		for (String label : labels)
			model.addLabel(label);
		
		model.initLabelArray();
		model.addFeatures(features);

		assertEquals(3, model.getLabelSize());
		assertEquals(5, model.getFeatureSize());
		
		for (int i=0; i<labels.length; i++)
			assertEquals(i, model.getLabelIndex(labels[i]));

		double[][] weights = {{1,0.1,0.01,0.001,0.0001},{3,0.3,0.03,0.003,0.0003},{2,0.2,0.02,0.002,0.0002}};
		model.initWeightVector();
		
		for (int i=0; i<weights.length; i++)
			model.copyWeights(weights[i], i);
		
		testSparseModelMultiClassificationAux(model);
		model = saveAndGetModel(model);;
		testSparseModelMultiClassificationAux(model);
	}
	
	private SparseModel saveAndGetModel(SparseModel model) throws Exception
	{
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(bout));
		out.writeObject(model);
		out.close();
		
		ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new ByteArrayInputStream(bout.toByteArray())));
		model = (SparseModel)in.readObject();
		in.close();
		
		return model;
	}
	
	private void testSparseModelMultiClassificationAux(SparseModel model)
	{
		SparseFeatureVector x = new SparseFeatureVector();
		
		x.addFeature(1);
		x.addFeature(3);
		
		assertEquals("1 3", x.toString());
		
		StringPrediction p = model.predictBest(x);
		assertEquals("B", p.label);
		assertEquals(true, p.score == 3.303);
		
		List<StringPrediction> list = model.predictAll(x);
		
		p = list.get(1);
		assertEquals("C", p.label);
		assertEquals(true, p.score == 2.202);
		
		p = list.get(2);
		assertEquals("A", p.label);
		assertEquals(true, p.score == 1.101);
		
		x = new SparseFeatureVector(true);
		
		x.addFeature(1, 2);
		x.addFeature(3, 4);
		
		p = model.predictAll(x).get(2);
		assertEquals("A", p.label);
		assertEquals(true, 1.204 == p.score);
	}
	
	@Test
	public void testSparseModelBinaryClassification() throws Exception
	{
		SparseModel model = new SparseModel();
		String[] labels   = {"A", "B"};
		int[]    features = {1, 2, 3, 4};

		for (String label : labels)
			model.addLabel(label);
		
		model.initLabelArray();
		model.addFeatures(features);
		
		double[] weights = {1,0.1,0.01,0.001,0.0001};
		
		model.initWeightVector();
		model.copyWeights(weights);
		
		SparseFeatureVector vector = new SparseFeatureVector();
		
		vector.addFeature(1);
		vector.addFeature(3);
		
		StringPrediction p = model.predictBest(vector);
		assertEquals("A", p.label);
		assertEquals(true, p.score == 1.101);
		
		List<StringPrediction> list = model.predictAll(vector);
		
		p = list.get(1);
		assertEquals("B", p.label);
		assertEquals(true, p.score == -1.101);
		
		model  = saveAndGetModel(model);
		vector = new SparseFeatureVector(true);
		
		vector.addFeature(1, 2);
		vector.addFeature(3, 4);
		
		p = model.predictBest(vector);
		assertEquals("A", p.label);
		assertEquals(true, 1.204 == p.score);
	}
}
