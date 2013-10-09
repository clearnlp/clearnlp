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
package com.clearnlp.component.evaluation;

import java.util.List;

import com.clearnlp.dependency.DEPArc;
import com.clearnlp.dependency.DEPTree;
import com.clearnlp.dependency.srl.SRLArc;
import com.clearnlp.util.pair.StringIntPair;

/**
 * @since 2.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class SRLEval extends AbstractF1Eval
{
	@Override
	public void countAccuracy(DEPTree sTree, Object[] gSHeads)
	{
		StringIntPair[][] heads = (StringIntPair[][])gSHeads;
		int i, size = sTree.size();
		StringIntPair[] gHeads;
		List<SRLArc>    sHeads;
		
		for (i=1; i<size; i++)
		{
			sHeads = sTree.get(i).getSHeads();
			gHeads = heads[i];
			
			p_total += sHeads.size();
			r_total += gHeads.length;
			
			for (StringIntPair p : gHeads)
				for (DEPArc arc : sHeads)
					if (arc.getNode().id == p.i && arc.isLabel(p.s))
						n_correct++;
		}
	}
}
