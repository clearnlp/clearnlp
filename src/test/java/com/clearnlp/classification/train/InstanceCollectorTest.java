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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.clearnlp.classification.instance.StringInstance;
import com.clearnlp.classification.model.StringModelAD;
import com.clearnlp.classification.train.InstanceCollector;
import com.clearnlp.classification.vector.StringFeatureVector;

/**
 * @since 2.0.1
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class InstanceCollectorTest
{
	@Test
	public void testStringOnlineModel()
	{
		StringModelAD model = new StringModelAD();
	
		model.addInstance(new StringInstance("L1", getStringFeatureVector1()));
		model.addInstance(new StringInstance("L2", getStringFeatureVector2()));
		model.addInstance(new StringInstance("L2", getStringFeatureVector3()));
		
		model.build(1, 1, 11, false);
		assertEquals("[L2]", model.getLabels().toString());
		assertEquals("{A=[a2=>1], B=[b1=>2], C=[c2=>3]}", model.getFeatureMap().toString());
	}
	
	@Test
	public void testInstanceCollector()
	{
		InstanceCollector collector = new InstanceCollector();
		
		collector.addInstance(new StringInstance("L1", getStringFeatureVector1()));
		collector.addInstance(new StringInstance("L2", getStringFeatureVector2()));
		collector.addInstance(new StringInstance("L2", getStringFeatureVector3()));

		assertEquals("[L1, L2]", collector.getLabels().toString());
		assertEquals("[A, B, C]", collector.getFeatureTypes().toString());
		assertEquals(2, collector.getLabelCount("L2"));
		assertEquals("[a1=>1, a2=>2]", collector.getFeatureMap("A").toString());
	}
	
	private StringFeatureVector getStringFeatureVector1()
	{
		StringFeatureVector vector = new StringFeatureVector();
		
		vector.addFeature("A", "a1");
		vector.addFeature("B", "b1");
		
		return vector;
	}
	
	private StringFeatureVector getStringFeatureVector2()
	{
		StringFeatureVector vector = new StringFeatureVector();
		
		vector.addFeature("A", "a2");
		vector.addFeature("C", "c2");
		
		return vector;
	}
	
	private StringFeatureVector getStringFeatureVector3()
	{
		StringFeatureVector vector = new StringFeatureVector();
		
		vector.addFeature("A", "a2");
		vector.addFeature("B", "b1");
		vector.addFeature("C", "c2");
		
		return vector;
	}
}
