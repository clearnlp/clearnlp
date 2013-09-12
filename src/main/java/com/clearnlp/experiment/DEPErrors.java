/**
* Copyright 2012-2013 University of Massachusetts Amherst
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
