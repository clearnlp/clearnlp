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

import com.clearnlp.classification.model.StringModel;
import com.clearnlp.classification.prediction.StringPrediction;
import com.clearnlp.classification.vector.SparseFeatureVector;
import com.clearnlp.classification.vector.StringFeatureVector;


/** @author Jinho D. Choi ({@code jdchoi77@gmail.com}) */
public class StringModelTest
{
	@Test
	public void testStringModelMultiClassification() throws Exception
	{
		StringModel model    = new StringModel();
		String[]    labels   = {"A", "B", "C"};
		String[][]  features = {{"F00","F01"},{"F10"},{"F20","F21","F22"}};

		for (String label : labels)
			model.addLabel(label);
		
		model.addLabel("B");
		model.initLabelArray();
		
		for (int i=0; i<features.length; i++)
			for (String ftr : features[i])
				model.addFeature(Integer.toString(i), ftr);

		model.addFeature("0", "F00");
		model.addFeature("2", "F22");
					
		assertEquals(3, model.getLabelSize());
		assertEquals(7, model.getFeatureSize());
		
		for (int i=0; i<labels.length; i++)
			assertEquals(i, model.getLabelIndex(labels[i]));
		
		double[][] weights = {{1,0.1,0.01,0.001,0.0001,0.00001,0.000001},{3,0.3,0.03,0.003,0.0003,0.00003,0.000003},{2,0.2,0.02,0.002,0.0002,0.00002,0.000002}};
		model.initWeightVector();
		
		for (int i=0; i<weights.length; i++)
			model.copyWeights(weights[i], i);
		
		testStringModelMultiClassificationAux(model);
		model = saveAndGetModel(model);
		testStringModelMultiClassificationAux(model);
	}
	
	private StringModel saveAndGetModel(StringModel model) throws Exception
	{
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(bout));
		out.writeObject(model);
		out.close();
		
		ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new ByteArrayInputStream(bout.toByteArray())));
		model = (StringModel)in.readObject();
		in.close();
		
		return model;
	}
	
	private void testStringModelMultiClassificationAux(StringModel model)
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
		assertEquals("B", p.label);
		assertEquals(true, p.score == 3.303033);
		
		List<StringPrediction> list = model.predictAll(vector);
		
		p = list.get(1);
		assertEquals("C", p.label);
		assertEquals(true, p.score == 2.202022);
		
		p = list.get(2);
		assertEquals("A", p.label);
		assertEquals(true, p.score == 1.101011);
		
		vector = new StringFeatureVector(true);
		
		vector.addFeature("0", "F00", 1);
		vector.addFeature("1", "F10", 2);
		vector.addFeature("2", "F21", 3);
		vector.addFeature("2", "F22", 4);
		
		p = model.predictAll(vector).get(2);
		assertEquals("A", p.label);
		assertEquals(true, 1.102034 == p.score);
	}
	
	@Test
	public void testStringModelBinaryClassification() throws Exception
	{
		StringModel model    = new StringModel();
		String[]    labels   = {"A", "B"};
		String[][]  features = {{"F00","F01"},{"F10"},{"F20","F21","F22"}};

		for (String label : labels)
			model.addLabel(label);
		
		model.initLabelArray();
		
		for (int i=0; i<features.length; i++)
			for (String ftr : features[i])
				model.addFeature(Integer.toString(i), ftr);

		double[] weights = {1,0.1,0.01,0.001,0.0001,0.00001,0.000001};
		
		model.initWeightVector();
		model.copyWeights(weights);
		
		StringFeatureVector vector = new StringFeatureVector();
		
		vector.addFeature("0", "F00");
		vector.addFeature("1", "F10");
		vector.addFeature("2", "F21");
		vector.addFeature("2", "F22");
		vector.addFeature("2", "F23");
		vector.addFeature("3", "F00");
		
		StringPrediction p = model.predictBest(vector);
		assertEquals("A", p.label);
		assertEquals(true, p.score == 1.101011);
		
		List<StringPrediction> list = model.predictAll(vector);
		
		p = list.get(1);
		assertEquals("B", p.label);
		assertEquals(true, p.score == -1.101011);
		
		model  = saveAndGetModel(model);
		vector = new StringFeatureVector(true);
		
		vector.addFeature("0", "F00", 1);
		vector.addFeature("1", "F10", 2);
		vector.addFeature("2", "F21", 3);
		vector.addFeature("2", "F22", 4);
		
		p = model.predictBest(vector);
		assertEquals("A", p.label);
		assertEquals(true, 1.102034 == p.score);
	}
}
