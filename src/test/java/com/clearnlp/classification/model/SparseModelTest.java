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

		float[][] weights = {{1,0.1f,0.01f,0.001f,0.0001f},{3,0.3f,0.03f,0.003f,0.0003f},{2,0.2f,0.02f,0.002f,0.0002f}};
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
		assertEquals("3.303", String.format("%.3f", p.score));
		
		List<StringPrediction> list = model.predictAll(x);
		
		p = list.get(1);
		assertEquals("C", p.label);
		assertEquals("2.202", String.format("%.3f", p.score));
		
		p = list.get(2);
		assertEquals("A", p.label);
		assertEquals("1.101", String.format("%.3f", p.score));
		
		x = new SparseFeatureVector(true);
		
		x.addFeature(1, 2);
		x.addFeature(3, 4);
		
		p = model.predictAll(x).get(2);
		assertEquals("A", p.label);
		assertEquals("1.204", String.format("%.3f", p.score));
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
		
		float[] weights = {1,0.1f,0.01f,0.001f,0.0001f};
		
		model.initWeightVector();
		model.copyWeights(weights);
		
		SparseFeatureVector vector = new SparseFeatureVector();
		
		vector.addFeature(1);
		vector.addFeature(3);
		
		StringPrediction p = model.predictBest(vector);
		assertEquals("A", p.label);
		assertEquals("1.101", String.format("%.3f", p.score));
		
		List<StringPrediction> list = model.predictAll(vector);
		
		p = list.get(1);
		assertEquals("B", p.label);
		assertEquals("-1.101", String.format("%.3f", p.score));
		
		model  = saveAndGetModel(model);
		vector = new SparseFeatureVector(true);
		
		vector.addFeature(1, 2);
		vector.addFeature(3, 4);
		
		p = model.predictBest(vector);
		assertEquals("A", p.label);
		assertEquals("1.204", String.format("%.3f", p.score));
	}
}
