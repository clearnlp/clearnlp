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

import com.clearnlp.classification.model.StringModelAD;
import com.clearnlp.classification.prediction.StringPrediction;
import com.clearnlp.classification.vector.SparseFeatureVector;
import com.clearnlp.classification.vector.StringFeatureVector;

/**
 * @since 2.0.1
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class StringModelADTest
{
	@Test
	public void testStringOnlineModelMultiClassification() throws Exception
	{
		StringModelAD model = new StringModelAD();

		model.addLabel("A");
		model.addFeature("0", "F00");
		model.addFeature("0", "F01");
		
		model.addLabel("B");
		model.addFeature("1", "F10");
		
		model.addLabel("C");
		model.addFeature("2", "F20");
		model.addFeature("2", "F21");
		model.addFeature("2", "F22");

		model.addLabel("B");
		model.addFeature("0", "F00");
		model.addFeature("2", "F22");
		
		assertEquals(0, model.getLabelIndex("A"));
		assertEquals(2, model.getLabelIndex("C"));
					
		assertEquals(3, model.getLabelSize());
		assertEquals(7, model.getFeatureSize());
		
		float[] weights = new float[]{1,0.1f,0.01f,0.001f,0.0001f,0.00001f,0.000001f};

		for (int i=0; i<model.getLabelSize(); i++)
			for (int j=0; j<weights.length; j++)
				model.updateWeight(i, j, weights[j]*(i+1));
		
		testStringModelMultiClassificationAux(model);
		model = saveAndGetModel(model);
		testStringModelMultiClassificationAux(model);
	}
	
	private StringModelAD saveAndGetModel(StringModelAD model) throws Exception
	{
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(bout));
		out.writeObject(model);
		out.close();
		
		ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new ByteArrayInputStream(bout.toByteArray())));
		model = (StringModelAD)in.readObject();
		in.close();
		
		return model;
	}
	
	private void testStringModelMultiClassificationAux(StringModelAD model)
	{
		StringFeatureVector vector = new StringFeatureVector();
		
		vector.addFeature("0", "F00");
		vector.addFeature("1", "F10");
		vector.addFeature("2", "F21");
		vector.addFeature("2", "F22");
		vector.addFeature("2", "F23");
		vector.addFeature("3", "F00");

		SparseFeatureVector x = model.toSparseFeatureVector(vector);
		assertEquals("1 3 5 6", x.toString());
		
		StringPrediction p = model.predictBest(vector);
		assertEquals("C", p.label);
		assertEquals(true, Double.toString(p.score).startsWith("3.303033"));
		
		List<StringPrediction> list = model.predictAll(vector);
		
		p = list.get(1);
		assertEquals("B", p.label);
		assertEquals(true, Double.toString(p.score).startsWith("2.202022"));
		
		p = list.get(2);
		assertEquals("A", p.label);
		assertEquals(true, Double.toString(p.score).startsWith("1.101011"));
		
		vector = new StringFeatureVector(true);
		
		vector.addFeature("0", "F00", 1);
		vector.addFeature("1", "F10", 2);
		vector.addFeature("2", "F21", 3);
		vector.addFeature("2", "F22", 4);
		
		p = model.predictAll(vector).get(2);
		assertEquals("A", p.label);
		assertEquals(true, Double.toString(p.score).startsWith("1.102034"));
	}
	
	@Test
	public void testStringModelBinaryClassification() throws Exception
	{
		StringModelAD model = new StringModelAD();

		model.addLabel("A");
		model.addFeature("0", "F00");
		model.addFeature("0", "F01");
		
		model.addLabel("B");
		model.addFeature("1", "F10");
		
		model.addFeature("2", "F20");
		model.addFeature("2", "F21");
		model.addFeature("2", "F22");

		float[] weights = new float[]{1,0.1f,0.01f,0.001f,0.0001f,0.00001f,0.000001f};

		for (int j=0; j<model.getFeatureSize(); j++)
			model.updateWeight(0, j, weights[j]);
		
		for (int j=0; j<model.getFeatureSize(); j++)
			model.updateWeight(1, j, -weights[j]);
		
		StringFeatureVector vector = new StringFeatureVector();
		
		vector.addFeature("0", "F00");
		vector.addFeature("1", "F10");
		vector.addFeature("2", "F21");
		vector.addFeature("2", "F22");
		vector.addFeature("2", "F23");
		vector.addFeature("3", "F00");
		
		StringPrediction p = model.predictBest(vector);
		assertEquals("A", p.label);
		System.out.println(Double.toString(p.score));
		assertEquals(true, Double.toString(p.score).startsWith("1.101011"));
		
		List<StringPrediction> list = model.predictAll(vector);
		
		p = list.get(1);
		assertEquals("B", p.label);
		assertEquals(true, Double.toString(p.score).startsWith("-1.101011"));
		
		model  = saveAndGetModel(model);
		vector = new StringFeatureVector(true);
		
		vector.addFeature("0", "F00", 1);
		vector.addFeature("1", "F10", 2);
		vector.addFeature("2", "F21", 3);
		vector.addFeature("2", "F22", 4);
		
		p = model.predictBest(vector);
		assertEquals("A", p.label);
		assertEquals(true, Double.toString(p.score).startsWith("1.102034"));
	}
}
