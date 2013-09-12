/**
* Copyright 2013 IPSoft Inc.
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*   http://www.apache.org/licenses/LICENSE-2.0
*   
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.clearnlp.dependency;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dependency.srl.SRLArc;
import com.google.common.collect.Lists;

/**
 * @since 1.4.2
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class SLRArcTest
{
	@Test
	public void testSRLArc()
	{
		List<SRLArc> arcs = Lists.newArrayList();
		
		arcs.add(new SRLArc(new DEPNode(1, "1"), "A1"));
		arcs.add(new SRLArc(new DEPNode(2, "2"), "A0"));
		arcs.add(new SRLArc(new DEPNode(3, "3"), "A2"));

		Collections.sort(arcs);
		
		String[] labels = {"A0", "A1", "A2"};
		
		for (int i=0; i<arcs.size(); i++)
			assertEquals(labels[i], arcs.get(i).getLabel());
	}		
}
