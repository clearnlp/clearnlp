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
package com.clearnlp.experiment;

import com.clearnlp.dependency.DEPArc;
import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dependency.DEPTree;
import com.clearnlp.reader.DEPReader;
import com.clearnlp.util.UTInput;

public class DEPErrors
{
	public DEPErrors(String mergeFile)
	{
		DEPReader gReader = new DEPReader(0, 1, 2, 4, 6, 7, 9);
		DEPReader sReader = new DEPReader(0, 1, 3, 5, 6, 8, 10);
		DEPTree gTree, sTree;
		
		gReader.open(UTInput.createBufferedFileReader(mergeFile));
		sReader.open(UTInput.createBufferedFileReader(mergeFile));
		
		String[] errors = {
				"sHead is a dependent       of gHead",
				"sHead is a grand-dependent of gHead",
				"gHead is a sibling         of sHead",
				"sHead is a descendent      of gHead",
				"gHead is a sibling         of cHead",
				"gHead is a dependent       of cNode",
				"gHead is a grand-dependent of sHead"
		};
		
		int[] counts = new int[errors.length+1];		
		
		while ((gTree = gReader.next()) != null)
		{
			sTree = sReader.next();
			gTree.setDependents();
			sTree.setDependents();
			checkErrors(gTree, sTree, counts);
		}
		
		int i, size = counts.length, total = counts[0], count, sum = 0;
		
		for (i=1; i<size; i++)
		{
			count = counts[i];
			sum += count;
			System.out.printf("%40s: %5.2f (%5d/%d)\n", errors[i-1], 100d*count/total, count, total);
		}
		
		System.out.printf("%40s: %5.2f (%d/%d)\n", "SUM", 100d*sum/total, sum, total);
	}
	
	public void checkErrors(DEPTree gTree, DEPTree sTree, int[] counts)
	{
		DEPNode cNode, gHead, sHead, tmp;
		int i, size = gTree.size();
		
		outer: for (i=1; i<size; i++)
		{
			cNode = sTree.get(i);
			
			gHead = sTree.get(gTree.get(i).getHead().id);
			sHead = cNode.getHead();
			
			if (gHead.id != sHead.id)
			{
				counts[0]++;
				
				if  (sHead.isDependentOf(gHead))
				{
					counts[1]++;
					continue outer;
				}
				
				if ((tmp = sHead.getHead()) != null)
				{
					if (tmp.isDependentOf(gHead))
					{
						counts[2]++;
						continue outer;
					}
					
					for (DEPArc arc : tmp.getDependents())
					{
						if (arc.isNode(gHead))
						{
							counts[3]++;
							continue outer;
						}
					}
				}
				
				if (sHead.isDescendentOf(gHead))
				{
					counts[4]++;
					continue outer;
				}
				
				for (DEPArc arc : sHead.getDependents())
				{
					if (arc.isNode(gHead))
					{
						counts[5]++;
						continue outer;
					}
				}
				
				if  (gHead.isDependentOf(cNode))
				{
					counts[6]++;
					continue outer;
				}
				
				for (DEPArc arc : sHead.getGrandDependents())
				{
					if (arc.isNode(gHead))
					{
						counts[7]++;
						continue outer;
					}
				}
			}
		}
	}
	
	static public void main(String[] args)
	{
		new DEPErrors(args[0]);
	}

}
