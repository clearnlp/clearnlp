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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.clearnlp.coreference.Mention;
import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dependency.DEPTree;
import com.clearnlp.reader.JointReader;
import com.clearnlp.util.UTInput;

public class CheckMentions
{
	public CheckMentions(String inDir)
	{
		JointReader reader = new JointReader(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
		File file = new File(inDir);
		DEPTree tree;
		
		for (String filename : file.list())
		{
			if (!filename.endsWith("c"))	continue;
			reader.open(UTInput.createBufferedFileReader(inDir+File.separator+filename));
			System.out.println(filename);
			
			while ((tree = reader.next()) != null)
			{
				tree.setDependents();
				check(tree);
			}
			
			reader.close();
		}
	}

	public void check(DEPTree tree)
	{
		List<Mention> mentions = tree.getMentions();
		List<DEPNode> heads;
		
		for (Mention m : mentions)
		{
			heads = getHeads(tree, m.beginIndex, m.endIndex);

			if (heads.size() > 1)
				System.out.println(m.beginIndex+" "+m.endIndex+"\n"+tree.toStringDEP()+"\n");
		}
	}
	
	private List<DEPNode> getHeads(DEPTree tree, int bIdx, int eIdx)
	{
		List<DEPNode> heads = new ArrayList<DEPNode>();
		int i, headId;
		DEPNode node;
		
		for (i=bIdx; i<=eIdx; i++)
		{
			node = tree.get(i);
			headId = node.getHead().id;
			
			if (headId < bIdx || headId > eIdx)
				heads.add(node);
		}
		
		return heads;
	}
	
	static public void main(String[] args)
	{
		new CheckMentions(args[0]);
	}
}

