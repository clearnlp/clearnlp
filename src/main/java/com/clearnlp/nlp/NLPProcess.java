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
package com.clearnlp.nlp;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.clearnlp.dependency.DEPArc;
import com.clearnlp.dependency.DEPFeat;
import com.clearnlp.dependency.DEPLib;
import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dependency.DEPTree;
import com.clearnlp.morphology.MPLib;
import com.clearnlp.pattern.PTNumber;
import com.clearnlp.pos.POSNode;
import com.clearnlp.propbank.verbnet.PVMap;
import com.clearnlp.propbank.verbnet.PVRole;
import com.clearnlp.propbank.verbnet.PVRoleset;
import com.clearnlp.segmentation.AbstractSegmenter;
import com.clearnlp.tokenization.AbstractTokenizer;
import com.clearnlp.util.UTCollection;

/**
 * @since 1.1.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class NLPProcess
{
	// ============================= input: reader =============================
	
	static public List<List<String>> getSentences(AbstractSegmenter segmenter, BufferedReader fin)
	{
		return segmenter.getSentences(fin);
	}
	
	// ============================= input: sentence =============================
	
	static public List<String> getTokens(AbstractTokenizer tokenizer, String sentence)
	{
		return tokenizer.getTokens(sentence);
	}
	
	// ============================= process: DEPTree =============================
	
	static public void simplifyForms(DEPTree tree)
	{
		int i, size = tree.size();
		DEPNode node;
		
		for (i=1; i<size; i++)
		{
			node = tree.get(i);
			node.simplifiedForm = MPLib.simplifyBasic(node.form);
			node.lowerSimplifiedForm = node.simplifiedForm.toLowerCase();
		}
	}
	
	// ============================= predict: SRL =============================
	
	static public void addVerbNet(PVMap map, DEPTree tree)
	{
		int i, size = tree.size();
		PVRoleset[] pvRolesets = new PVRoleset[size];
		PVRoleset pvRoleset;
		List<String> vnclss;
		String rolesetId;
		PVRole pvRole;
		DEPNode node;
		String n;
		
		for (i=1; i<size; i++)
		{
			node = tree.get(i);
			rolesetId = node.getFeat(DEPLib.FEAT_PB);
			
			if (rolesetId != null)
			{
				if ((pvRoleset = map.getRoleset(rolesetId)) != null)
				{
					vnclss = new ArrayList<String>(pvRoleset.keySet());
					Collections.sort(vnclss);
					node.addFeat(DEPLib.FEAT_VN, UTCollection.toString(vnclss, DEPFeat.DELIM_VALUES));
				}
				
				pvRolesets[i] = pvRoleset;
			}
		}
		
		for (i=1; i<size; i++)
			for (DEPArc arc : tree.get(i).getSHeads())
				if ((pvRoleset = pvRolesets[arc.getNode().id]) != null)
					if (PTNumber.containsOnlyDigits(n = arc.getLabel().substring(1, 2)))
					{
						vnclss = new ArrayList<String>(pvRoleset.keySet());
						Collections.sort(vnclss);
						
						for (String vncls : vnclss)
							if ((pvRole = pvRoleset.get(vncls).getRole(n)) != null)
								arc.appendLabel(pvRole.vntheta);
					}
	}
	
	// ============================= conversion =============================
	
	static public POSNode[] toPOSNodes(List<String> tokens)
	{
		int i, size = tokens.size();
		POSNode[] nodes = new POSNode[size];
		
		for (i=0; i<size; i++)
			nodes[i] = new POSNode(tokens.get(i));
		
		return nodes;
	}
	
	static public DEPTree toDEPTree(POSNode[] nodes)
	{
		DEPTree tree = new DEPTree();
		int i, size = nodes.length;
		
		for (i=0; i<size; i++)
			tree.add(new DEPNode(i+1, nodes[i]));
		
		return tree;
	}
}
