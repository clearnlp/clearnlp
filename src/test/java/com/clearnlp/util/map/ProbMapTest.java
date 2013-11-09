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
package com.clearnlp.util.map;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.clearnlp.util.UTArray;
import com.clearnlp.util.map.Prob2DMap;
import com.clearnlp.util.pair.StringDoublePair;


/** @author Jinho D. Choi ({@code jdchoi77@gmail.com}) */
public class ProbMapTest
{
	@Test
	public void test()
	{
		Prob2DMap map = new Prob2DMap();
		
		map.add("study", "NN");
		map.add("study", "VB");
		map.add("study", "NN");
		map.add("study", "NN");
		
		map.add("home", "RB");
		map.add("home", "NNP");
		map.add("home", "NN");
		map.add("home", "NN");
		
		StringDoublePair[] p = map.getProb1D("study");
		UTArray.sortReverseOrder(p);
		
		assertEquals("NN", p[0].s);
		assertEquals("VB", p[1].s);
		assertEquals(0.75 == p[0].d, true);
		assertEquals(0.25 == p[1].d, true);
		
		p = map.getProb2D("home");
		UTArray.sortReverseOrder(p);
		
		assertEquals("NN", p[0].s);
		assertEquals(0.25  == p[0].d, true);
		assertEquals(0.125 == p[1].d, true);
		assertEquals(0.125 == p[2].d, true);
	}
}
