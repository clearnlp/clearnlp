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
package com.clearnlp.conversion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.clearnlp.constituent.CTLib;
import com.clearnlp.constituent.CTNode;
import com.clearnlp.constituent.CTTree;
import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dependency.DEPTree;
import com.clearnlp.headrule.HeadRule;
import com.clearnlp.headrule.HeadRuleMap;
import com.clearnlp.headrule.HeadTagSet;
import com.clearnlp.pattern.PTPunct;
import com.clearnlp.reader.AbstractColumnReader;

/**
 * Abstract constituent to dependency converter.
 * @since 1.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
abstract public class AbstractC2DConverter
{
	protected HeadRuleMap m_headrules;
	
	public AbstractC2DConverter(HeadRuleMap headrules)
	{
		m_headrules = headrules;
	}
	
	/**
	 * Sets the head of the specific node and all its sub-nodes.
	 * Calls {@link AbstractC2DConverter#findHeads(CTNode)}.
	 * @param curr the node to process.
	 */
	protected void setHeads(CTNode curr)
	{
		// terminal nodes become the heads of themselves
		if (!curr.isPhrase())
		{
			curr.c2d = new C2DInfo(curr);
			return;
		}
		
		// set the heads of all children
		for (CTNode child : curr.getChildren())
			setHeads(child);
		
		// stop traversing if it is the top node
		if (curr.isPTag(CTLib.PTAG_TOP))
			return;
		
		// only one child
		if (curr.getChildrenSize() == 1)
		{
			curr.c2d = new C2DInfo(curr.getChild(0));
			return;
		}
		
		// find the headrule of the current node
		HeadRule rule = m_headrules.get(curr.pTag);
				
		if (rule == null)
		{
			System.err.println("Error: headrules not found for \""+curr.pTag+"\"");
			rule = m_headrules.get(CTLib.PTAG_X);
		}			
				
		setHeadsAux(rule, curr);
	}
	
	/**
	 * Returns the head of the specific node list according to the specific headrule.
	 * Every other node in the list becomes the dependent of the head node.
	 * @param rule the headrule to be consulted.
	 * @param nodes the list of nodes.
	 * @param flagSize the number of head flags.
	 * @return the head of the specific node list according to the specific headrule.
	 */
	protected CTNode getHead(HeadRule rule, List<CTNode> nodes, int flagSize)
	{
		nodes = new ArrayList<CTNode>(nodes);
		if (rule.isRightToLeft())	Collections.reverse(nodes);
		
		int i, size = nodes.size(), flag;
		int[] flags = new int[size];
		
		for (i=0; i<size; i++)
			flags[i] = getHeadFlag(nodes.get(i));
		
		CTNode head = null, child;
		
		outer: for (flag=0; flag<flagSize; flag++)
		{
			for (HeadTagSet tagset : rule.getHeadTags())
			{
				for (i=0; i<size; i++)
				{
					child = nodes.get(i);
					
					if (flags[i] == flag && tagset.matches(child))
					{
						head = child;
						break outer;
					}
				}
			}
		}

		if (head == null)
		{
			System.err.println("Error: head not found.");
			System.exit(1);
		}
		
		CTNode parent = head.getParent();
		
		for (CTNode node : nodes)
		{
			if (node != head && !node.c2d.hasHead())
				node.c2d.setHead(head, getDEPLabel(node, parent, head));
		}
		
		return head;
	}
	
	/** @return the dependency tree converted from the specific constituent tree without head information. */
	protected DEPTree initDEPTree(CTTree cTree)
	{
		DEPTree dTree = new DEPTree();
		String form, lemma, pos;
		DEPNode dNode;
		int id;
		
		for (CTNode node : cTree.getTokens())
		{
			id    = node.getTokenId() + 1;
			form  = PTPunct.revertBracket(node.form);
			lemma = AbstractColumnReader.BLANK_COLUMN;
			pos   = node.pTag;
			
			dNode = new DEPNode(id, form, lemma, pos, node.c2d.d_feats);
			dTree.add(dNode);
		}
		
		dTree.initXHeads();
		return dTree;
	}
	
	/**
	 * Sets the head of the specific phrase node.
	 * This is a helper method of {@link AbstractC2DConverter#setHeads(CTNode)}.
	 * @param rule the headrule to the specific node.
	 * @param curr the phrase node.
	 */
	abstract protected void setHeadsAux(HeadRule rule, CTNode curr);
	
	/**
	 * Returns the head flag of the specific constituent node.
	 * @param child the constituent node.
	 * @return the head flag of the specific constituent node.
	 */
	abstract protected int getHeadFlag(CTNode child);
	
	/**
	 * Returns a dependency label given the specific phrase structure.
	 * @param C the current node.
	 * @param P the parent of {@code C}.
	 * @param p the head of {@code P}.
	 * @return a dependency label given the specific phrase structure.
	 */
	abstract protected String getDEPLabel(CTNode C, CTNode P, CTNode p);
	
	/**
	 * Returns the dependency tree converted from the specific constituent tree.
	 * If the constituent tree contains only empty categories, returns {@code null}.
	 * @param cTree the constituent tree to convert.
	 * @return the dependency tree converted from the specific constituent tree.
	 */
	abstract public DEPTree toDEPTree(CTTree cTree);
}
