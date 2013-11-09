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
import java.util.List;
import java.util.regex.Pattern;

import com.clearnlp.constituent.CTLibKaist;
import com.clearnlp.constituent.CTNode;
import com.clearnlp.constituent.CTTree;
import com.clearnlp.dependency.DEPLib;
import com.clearnlp.dependency.DEPLibKr;
import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dependency.DEPTree;
import com.clearnlp.headrule.HeadRule;
import com.clearnlp.headrule.HeadRuleMap;


/**
 * Constituent to dependency converter for KAIST Treebank.
 * @since 1.3.2
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class KaistC2DConverter extends AbstractC2DConverter
{
	private final Pattern DELIM_PLUS  = Pattern.compile("\\+");
	private final int SIZE_HEAD_FLAGS = 6;
	
	public KaistC2DConverter(HeadRuleMap headrules)
	{
		super(headrules);
	}
	
	@Override
	public DEPTree toDEPTree(CTTree cTree)
	{
		setHeads(cTree.getRoot());
		return getDEPTree(cTree);
	}
	
	// ============================= Find heads =============================
	
	@Override
	protected void setHeadsAux(HeadRule rule, CTNode curr)
	{
		findConjuncts(rule, curr);

		CTNode head = getHead(rule, curr.getChildren(), SIZE_HEAD_FLAGS);
		curr.c2d = new C2DInfo(head);
	}
	
	/**
	 * If the specific node contains a coordination structure, find the head of each coordination.
	 * @param curr the specific node to be compared. 
	 * @return {@code true} if this node contains a coordination structure.
	 */
	private void findConjuncts(HeadRule rule, CTNode curr)
	{
		List<CTNode> children = curr.getChildren();
		int i, size = children.size();
		String label;
		CTNode child;
		
		for (i=0; i<size; i++)
		{
			child = children.get(i);
			
			if ((label = getSpecialLabel(child)) != null)
				child.addFTag(label);
			else
				break;
		}
		
		if (CTLibKaist.containsCoordination(children.subList(i,size), DELIM_PLUS))
		{
			for (; i<size; i++)
			{
				child = children.get(i);
				
				if ((label = getConjunctLabel(curr, child)) != null)
					child.addFTag(label);
			}
		}
	}
	
	private String getConjunctLabel(CTNode parent, CTNode child)
	{
		String label;
		
		if (CTLibKaist.isConjunct(child, DELIM_PLUS))
			return DEPLibKr.DEP_CONJ;
		
		if ((label = getSpecialLabel(child)) != null)
			return label;
		
		if (child.isPTag(CTLibKaist.PTAG_ADVP) && !parent.isPTag(CTLibKaist.PTAG_ADVP))
			return DEPLibKr.DEP_ADV;
		
		return DEPLibKr.DEP_CONJ;
	}
	
	@Override
	protected int getHeadFlag(CTNode child)
	{
		if (child.c2d.hasHead())
			return -1;
		
		if (child.isPTag(CTLibKaist.PTAG_AUXP))
			return 1;
		
		if (child.isPTag(CTLibKaist.PTAG_IP))
			return 2;
		
		if (child.hasFTag(CTLibKaist.FTAG_PRN))
			return 3;
		
		if (CTLibKaist.isOnlyEJX(child, DELIM_PLUS))
			return 4;
		
		if (CTLibKaist.isPunctuation(child))
			return 5;
		
		return 0;
	}
	
	// ============================= Get Kaist labels ============================= 
	
	@Override
	protected String getDEPLabel(CTNode C, CTNode P, CTNode p)
	{
		String label;
		
		if ((label = getFunctionLabel(C)) != null)
			return label;
		
		if ((label = getSpecialLabel(C)) != null)
			return label;
		
		String[] pTags = CTLibKaist.getLastPOSTags(C, DELIM_PLUS);
		String   pTag  = pTags[pTags.length-1];
		
		if ((label = getRoleLabel(pTag)) != null)
			return label;
		
		if ((label = getSubLabel(pTags)) != null)
			return label;
		
		if ((label = getSimpleLabel(C)) != null)
			return label;
		
		CTNode d = C.c2d.getDependencyHead();

		if ((label = getSimpleLabel(d)) != null)
			return label;
		
		if (P.isPTag(CTLibKaist.PTAG_ADJP))
			return DEPLibKr.DEP_AMOD;
		
		if (P.isPTag(CTLibKaist.PTAG_ADVP))
			return DEPLibKr.DEP_ADV;
		
		if (P.isPTag(CTLibKaist.PTAG_NP))
			return DEPLibKr.DEP_NMOD;

		if (P.isPTag(CTLibKaist.PTAG_VP))
			return DEPLibKr.DEP_VMOD;
		
		return DEPLibKr.DEP_DEP;
	}
	
	// KAIST
	private String getFunctionLabel(CTNode C)
	{
		if (C.hasFTag(CTLibKaist.FTAG_PRN))
			return DEPLibKr.DEP_PRN;
		
		List<String> list = new ArrayList<String>(C.getFTags());
		return (list.size() == 1) ? list.get(0) : null;
	}
	
	// KAIST
	private String getSpecialLabel(CTNode C)
	{
		CTNode d = C.c2d.getDependencyHead();
		
		if (CTLibKaist.isPunctuation(C) || CTLibKaist.isPunctuation(d))
			return DEPLibKr.DEP_PUNCT;
		
		if (CTLibKaist.isOnlyEJX(C, DELIM_PLUS))
			return DEPLibKr.DEP_EJX;
		
		if (C.isPTag(CTLibKaist.PTAG_AUXP))
			return DEPLibKr.DEP_AUX;
		
		if (CTLibKaist.isConjunction(C, DELIM_PLUS))
			return DEPLibKr.DEP_CC;
		
		if (CTLibKaist.isConjunct(C, DELIM_PLUS))
			return DEPLibKr.DEP_CONJ;
		
		return null;
	}
	
	private String getRoleLabel(String pTag)
	{
		if (pTag.equals(CTLibKaist.POS_JCC))
			return DEPLibKr.DEP_COMP;
		
		if (pTag.equals(CTLibKaist.POS_JCO))
			return DEPLibKr.DEP_OBJ;
		
		if (pTag.equals(CTLibKaist.POS_JCS))
			return DEPLibKr.DEP_SBJ;
		
		if (pTag.equals(CTLibKaist.POS_JCT))
			return DEPLibKr.DEP_COMIT;
		
		if (pTag.equals(CTLibKaist.POS_JXT))
			return DEPLibKr.DEP_TPC;
		
		return null;
	}
	
	private String getSubLabel(String[] pTags)
	{
		for (String pTag : pTags)
		{
			if (pTag.equals(CTLibKaist.POS_ECS))
				return DEPLibKr.DEP_SUB;
			else if (pTag.equals(CTLibKaist.POS_JCR))
				return DEPLibKr.DEP_QUOT;
		}
		
		return null;
	}
	
	private String getSimpleLabel(CTNode C)
	{
		if (C.isPTag(CTLibKaist.PTAG_MODP) || CTLibKaist.isAdnoun(C, DELIM_PLUS))
			return DEPLibKr.DEP_ADN;
		
		if (C.isPTag(CTLibKaist.PTAG_ADVP) || CTLibKaist.isAdverb(C, DELIM_PLUS))
			return DEPLibKr.DEP_ADV;
		
		if (C.isPTagAny(CTLibKaist.PTAG_IP) || CTLibKaist.isInterjection(C, DELIM_PLUS))
			return DEPLibKr.DEP_INTJ;
		
		return null;
	}
		
	// ============================= Get a dependency tree =============================
	
	private DEPTree getDEPTree(CTTree cTree)
	{
		DEPTree dTree = initDEPTree(cTree);
		addDEPHeads(dTree, cTree);
		
		if (dTree.containsCycle())
			System.err.println("Error: cyclic dependencies exist");
		
		return dTree;
	}
	
	/** Adds dependency heads. */
	private void addDEPHeads(DEPTree dTree, CTTree cTree)
	{
		int currId, headId, size = dTree.size(), rootCount = 0;
		DEPNode dNode;
		CTNode cNode;
		String label;
		
		for (currId=1; currId<size; currId++)
		{
			dNode  = dTree.get(currId);
			cNode  = cTree.getToken(currId-1);
			headId = cNode.c2d.d_head.getTokenId() + 1;
			
			if (currId == headId)	// root
			{
				dNode.setHead(dTree.get(DEPLib.ROOT_ID), DEPLibKr.DEP_ROOT);
				rootCount++;
			}
			else
			{
				label = cNode.c2d.s_label;
				dNode.setHead(dTree.get(headId), label);
			}
		}
		
		if (rootCount > 1)	System.err.println("Warning: multiple roots exist");
	}
}
