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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.clearnlp.constituent.CTLib;
import com.clearnlp.constituent.CTLibEn;
import com.clearnlp.constituent.CTNode;
import com.clearnlp.constituent.CTTree;
import com.clearnlp.dependency.DEPArc;
import com.clearnlp.dependency.DEPFeat;
import com.clearnlp.dependency.DEPLib;
import com.clearnlp.dependency.DEPLibEn;
import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dependency.DEPTree;
import com.clearnlp.dependency.srl.SRLArc;
import com.clearnlp.dependency.srl.SRLLib;
import com.clearnlp.headrule.HeadRule;
import com.clearnlp.headrule.HeadRuleMap;
import com.clearnlp.morphology.MPLibEn;
import com.clearnlp.util.UTArray;
import com.clearnlp.util.pair.Pair;
import com.clearnlp.util.pair.StringIntPair;
import com.google.common.collect.Lists;


/**
 * Constituent to dependency converter for English.
 * @since 1.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class EnglishC2DConverter extends AbstractC2DConverter
{
	static final public byte TYPE_STANFORD = 0;
	private final int SIZE_HEAD_FLAGS = 4;
	
	private final String[] a_semTags = {CTLibEn.FTAG_BNF, CTLibEn.FTAG_DIR, CTLibEn.FTAG_EXT, CTLibEn.FTAG_LOC, CTLibEn.FTAG_MNR, CTLibEn.FTAG_PRP, CTLibEn.FTAG_TMP, CTLibEn.FTAG_VOC};
	private final String[] a_synTags = {CTLibEn.FTAG_ADV, CTLibEn.FTAG_CLF, CTLibEn.FTAG_CLR, CTLibEn.FTAG_DTV, CTLibEn.FTAG_NOM, CTLibEn.FTAG_PUT, CTLibEn.FTAG_PRD, CTLibEn.FTAG_TPC};
	private Set<String>    s_semTags;
	private Set<String>    s_synTags;
	
	private Map<CTNode,Deque<CTNode>> m_rnr;
	private Map<CTNode,Deque<CTNode>> m_xsbj;
	private Map<String,Pattern>       m_coord;
	
	private List<Pair<String,Set<String>>> l_mergeLabels;
	
	public EnglishC2DConverter(HeadRuleMap headrules, String mergeLabels)
	{
		super(headrules);
		
		initBasic();
		initCoord();
		initMerge(mergeLabels);
	}
	
	private void initBasic()
	{
		s_semTags = UTArray.toSet(a_semTags);
		s_synTags = UTArray.toSet(a_synTags);

		m_rnr   = new HashMap<CTNode,Deque<CTNode>>();
		m_xsbj  = new HashMap<CTNode,Deque<CTNode>>();
	}
	
	private void initCoord()
	{
		m_coord = new HashMap<String,Pattern>();
		
		m_coord.put(CTLibEn.PTAG_ADJP	, Pattern.compile("^(ADJP|JJ.*|VBN|VBG)$"));
		m_coord.put(CTLibEn.PTAG_ADVP	, Pattern.compile("^(ADVP|RB.*)$"));
		m_coord.put(CTLibEn.PTAG_INTJ	, Pattern.compile("^(INTJ|UH)$"));
		m_coord.put(CTLibEn.PTAG_PP  	, Pattern.compile("^(PP|IN|VBG)$"));
		m_coord.put(CTLibEn.PTAG_PRT 	, Pattern.compile("^(PRT|RP)$"));
		m_coord.put(CTLibEn.PTAG_NAC 	, Pattern.compile("^(NP)$"));
		m_coord.put(CTLibEn.PTAG_NML 	, Pattern.compile("^(NP|NML|NN.*|PRP)$"));
		m_coord.put(CTLibEn.PTAG_NP  	, Pattern.compile("^(NP|NML|NN.*|PRP)$"));
		m_coord.put(CTLibEn.PTAG_NX  	, Pattern.compile("^(NX)$"));
		m_coord.put(CTLibEn.PTAG_VP  	, Pattern.compile("^(VP|VB.*)$"));
		m_coord.put(CTLibEn.PTAG_S   	, Pattern.compile("^(S|SINV|SQ|SBARQ)$"));
		m_coord.put(CTLibEn.PTAG_SBAR	, Pattern.compile("^(SBAR.*)$"));
		m_coord.put(CTLibEn.PTAG_SBARQ	, Pattern.compile("^(SBAR.*)$"));
		m_coord.put(CTLibEn.PTAG_SINV	, Pattern.compile("^(S|SINV)$"));
		m_coord.put(CTLibEn.PTAG_SQ		, Pattern.compile("^(S|SQ|SBARQ)$"));
		m_coord.put(CTLibEn.PTAG_WHNP	, Pattern.compile("^(NN.*|WP)$"));
		m_coord.put(CTLibEn.PTAG_WHADJP	, Pattern.compile("^(JJ.*|VBN|VBG)$"));
		m_coord.put(CTLibEn.PTAG_WHADVP	, Pattern.compile("^(RB.*|WRB|IN)$"));
	}
	
	private void initMerge(String mergeLabels)
	{
		l_mergeLabels = new ArrayList<Pair<String,Set<String>>>();
		
		if (mergeLabels != null)
		{
			String[]    tmp;
			String      nLabel;
			Set<String> oLabels;
			
			for (String ms : mergeLabels.split("\\"+DEPFeat.DELIM_FEATS))
			{
				tmp     = ms.split(DEPFeat.DELIM_KEY_VALUE);
				nLabel  = tmp[0];
				oLabels = new HashSet<String>();
				
				for (String oLabel : tmp[1].split(DEPFeat.DELIM_VALUES))
					oLabels.add(oLabel);
						
				l_mergeLabels.add(new Pair<String,Set<String>>(nLabel, oLabels));
			}
		}
	}

	@Override
	public DEPTree toDEPTree(CTTree cTree)
	{
		clearMaps();
		
		if (!mapEmtpyCategories(cTree))	return null;
		setHeads(cTree.getRoot());
		
		return getDEPTree(cTree);
	}
	
	private void clearMaps()
	{
		m_rnr.clear();
		m_xsbj.clear();
	}
	
	// ============================= Map empty categories ============================= 
	
	/**
	 * Removes, relocates empty categories in the specific tree. 
	 * Returns {@true} if the constituent tree contains nodes after relocating empty categories.
	 * @param cTree the constituent tree to be processed.
	 * @return {@true} if the constituent tree contains nodes after relocating empty categories.
	 */
	public boolean mapEmtpyCategories(CTTree cTree)
	{
		for (CTNode node : cTree.getTerminals())
		{
			if (!node.isEmptyCategory())	continue;
			if (node.getParent() == null)	continue;
			
			if      (node.form.startsWith(CTLibEn.EC_PRO))
				mapPRO(cTree, node);
			else if (node.form.startsWith(CTLibEn.EC_TRACE))
				mapTrace(cTree, node);
			else if (CTLibEn.RE_NULL.matcher(node.form).find())
				mapNull(cTree, node);
			else if (node.isForm("0"))
				continue;
			else if (CTLibEn.RE_ICH_PPA_RNR.matcher(node.form).find())
				mapICH(cTree, node);
		//	else if (node.form.startsWith(CTLibEn.EC_EXP))
		//		reloateEXP(cTree, node);
			else
				removeCTNode(node);
		}
		
		return cTree.getRoot().getChildrenSize() > 0;
	}
	
	/** Called by {@link EnglishC2DConverter#mapEmtpyCategories(CTTree)}. */
	private void mapPRO(CTTree cTree, CTNode ec)
	{
		CTNode np = ec.getParent();
		CTNode vp = np.getParent().getFirstChainedDescendant(CTLibEn.PTAG_VP);
		
		if (vp == null)								// small clauses
			relocatePRD(np, ec);
		else
		{
			CTNode ante;
			
			if ((ante = ec.getAntecedent()) != null && ante.pTag.startsWith("WH"))	// relative clauses
			{
				if (cTree.getCoIndexedEmptyCategories(ante.coIndex).size() == 1)
					mapTrace(cTree, ec);
			}
			
			addXSubject(ec, m_xsbj);
		}
	}
	
	/** Called by {@link EnglishC2DConverter#mapEmtpyCategories(CTTree)}. */
	private void mapTrace(CTTree cTree, CTNode ec)
	{
		CTNode ante = ec.getAntecedent();
		
		if (ante == null || ec.isDescendantOf(ante))
			removeCTNode(ec);
		else if (ante.hasFTag(CTLibEn.FTAG_TPC))
		{
			if (!ante.hasFTag(CTLibEn.FTAG_SBJ))
			{
				CTNode parent = ec.getParent();
				parent.removeChild(ec);
				replaceEC(parent, ante);
			}
			else
				removeCTNode(ec);
		}
		else	// relative clauses
		{
			CTNode parent = ante.getHighestChainedAncestor(CTLibEn.PTAG_SBAR);
			if (parent != null)		parent.addFTag(DEPLibEn.DEP_RCMOD);
			replaceEC(ec, ante);
		}
	}
	
	/** Called by {@link EnglishC2DConverter#mapEmtpyCategories(CTTree)}. */
	private void mapNull(CTTree cTree, CTNode ec)
	{
		CTNode np = ec.getParent();
		
		if (np.hasFTag(CTLibEn.FTAG_SBJ))
		{
			// small clauses
			if (np.getNextSibling(CTLibEn.PTAG_VP) == null)
				relocatePRD(np, ec);
			else
				addXSubject(ec, m_xsbj);
		}
	}
	
	/** Called by {@link EnglishC2DConverter#mapEmtpyCategories(CTTree)}. */
	private void mapICH(CTTree cTree, CTNode ec)
	{
		CTNode parent = ec.getParent();
		CTNode ante   = ec.getAntecedent();
		
		if (ec.form.startsWith(CTLibEn.EC_ICH) && parent.getPrevSibling("+WH.*") != null)
			removeCTNode(ec);
		else if (ante == null || ec.isDescendantOf(ante))
			removeCTNode(ec);
		else
		{
			List<CTNode> list = cTree.getCoIndexedEmptyCategories(ante.coIndex);
			boolean isRNR = ec.form.startsWith(CTLibEn.EC_RNR);
			int i, size = list.size();
			CTNode node;
			
			Deque<CTNode> dq = isRNR ? new ArrayDeque<CTNode>() : null; 
			
			if (ec.getTerminalId() < ante.getFirstTerminal().getTerminalId())
			{		
				for (i=0; i<size-1; i++)
				{
					node = list.get(i);
					if (isRNR)	dq.addLast(node.getParent().getParent());
					removeCTNode(node);
				}
				
				ec = list.get(size-1);
			}
			else
			{
				for (i=size-1; i>0; i--)
				{
					node = list.get(i);
					if (isRNR)	dq.addFirst(node.getParent().getParent());
					removeCTNode(node);
				}
				
				ec = list.get(0);
			}
			
			if (isRNR && !dq.isEmpty())
				m_rnr.put(ante, dq);
			
			parent = ec.getParent();
			parent.removeChild(ec);
			replaceEC(parent, ante);
		}
	}
	
	/** Called by {@link EnglishC2DConverter#mapPRO(CTTree, CTNode)} and {@link EnglishC2DConverter#mapNull(CTTree, CTNode)}. */
	private void relocatePRD(CTNode np, CTNode ec)
	{
		CTNode s   = np.getParent();
		CTNode prd = s.getFirstChild("-"+CTLibEn.FTAG_PRD);
		Set<String> fTags = s.getFTags();
		
		if (prd != null && (fTags.isEmpty() || fTags.contains(CTLibEn.FTAG_CLR)))
		{
			fTags.clear();
			fTags.add(DEPLibEn.DEP_OPRD);
		}

		removeCTNode(ec);
	}
	
/*	private void reloateEXP(CTTree cTree, CTNode ec)
	{
		int idx = ec.form.lastIndexOf("-");
		
		if (idx != -1)
		{
			int coIndex = Integer.parseInt(ec.form.substring(idx+1));
			CTNode ante = cTree.getCoIndexedAntecedent(coIndex);
			if (ante != null)	ante.addFTag(DEPLibEn.CONLL_EXTR);
		}
		
		removeCTNode(ec);
	}*/
	
	/**
	 * @param ec empty subject.
	 * @param map key: antecedent, value: list of clauses containing empty subjects.
	 */
	private void addXSubject(CTNode ec, Map<CTNode, Deque<CTNode>> map)
	{
		CTNode ante = ec.getAntecedent();
		
		while (ante != null && ante.isEmptyCategoryRec() && !ante.pTag.startsWith("WH"))
			ante = ante.getFirstTerminal().getAntecedent();
		
		if (ante != null)
		{
			CTNode s = ec.getNearestAncestor(CTLibEn.PTAG_S);
			
			if (s != null)
			{
				Deque<CTNode> dq = map.get(ante);
				if (dq == null)	dq = new ArrayDeque<CTNode>();
				
				dq.add(s);
				map.put(ante, dq);
			}
		}
	}
	
	private void removeCTNode(CTNode node)
	{
		CTNode parent = node.getParent();
	
		if (parent != null)
		{
			parent.removeChild(node);
			
			if (parent.getChildrenSize() == 0)
				removeCTNode(parent);			
		}
	}
	
	private void replaceEC(CTNode ec, CTNode ante)
	{
		removeCTNode(ante);
		ec.getParent().setChild(ec.getSiblingId(), ante);
	}
	
	// ============================= Find heads =============================
	
	@Override
	protected void setHeadsAux(HeadRule rule, CTNode curr)
	{
		if (findHeadsCoordination(rule, curr))	return;
		
		findHyphens(curr);
		findHeadsApposition(curr);
		findHeadsSmallClause(curr);

		CTNode head = getHead(rule, curr.getChildren(), SIZE_HEAD_FLAGS);
		if (head.c2d.getLabel() != null)	head.c2d.setLabel(null); 
		curr.c2d = new C2DInfo(head);
	}
	
	
	/**
	 * If the specific node contains a coordination structure, find the head of each coordination.
	 * @param curr the specific node to be compared. 
	 * @return {@code true} if this node contains a coordination structure.
	 */
	private boolean findHeadsCoordination(HeadRule rule, CTNode curr)
	{
		// skip pre-conjunctions and punctuation
		int i, sId, size = curr.getChildrenSize();
		CTNode node;
		
		for (sId=0; sId<size; sId++)
		{
			node = curr.getChild(sId);
			
			if (!CTLibEn.isPunctuation(node) && !CTLibEn.isConjunction(node))
				break;
		}
		
		if (!CTLibEn.containsCoordination(curr, curr.getChildren(sId)))
			return false;
		
		// find conjuncts
		Pattern rTags = getConjunctPattern(curr, sId, size);
		CTNode prevHead = null, mainHead = null;
		boolean isFound = false;
		int bId = 0, eId = sId;
		
		for (; eId<size; eId++)
		{
			node = curr.getChild(eId);
			
			if (CTLibEn.isCoordinator(node))
			{
				if (isFound)
				{
					prevHead = findHeadsCoordinationAux(rule, curr, bId, eId, prevHead);
					setHeadCoord(node, prevHead, getDEPLabel(node, curr, prevHead));
					if (mainHead == null)	mainHead = prevHead;
					isFound = false;
			
					bId = eId + 1;
				}
				else if (prevHead != null)
				{
					for (i=bId; i<=eId; i++)
					{
						node = curr.getChild(i);
						setHeadCoord(node, prevHead, getDEPLabel(node, curr, prevHead));
					}
					
					bId = eId + 1;
				}
			}
			else if (isConjunct(node, curr, rTags))
				isFound = true;
		}
		
		if (mainHead == null)	return false;
		
		if (eId - bId > 0)
			findHeadsCoordinationAux(rule, curr, bId, eId, prevHead);
		
		curr.c2d = new C2DInfo(mainHead);
		return true;
	}
	
	/** Called by {@link EnglishC2DConverter#findHeadsCoordination(HeadRule, CTNode)}. */
	private Pattern getConjunctPattern(CTNode curr, int sId, int size)
	{
		Pattern rTags = m_coord.get(curr.pTag);
		
		if (rTags != null)
		{
			boolean b = false;
			int i;
			
			for (i=sId; i<size; i++)
			{
				if (rTags.matcher(curr.getChild(i).pTag).find())
				{
					b = true;
					break;
				}
			}
			
			if (!b)	rTags = Pattern.compile(".*");
		}
		else
			rTags = Pattern.compile(".*");
		
		return rTags;
	}
	
	/** Called by {@link EnglishC2DConverter#findHeadsCoordination(HeadRule, CTNode)}. */
	private boolean isConjunct(CTNode C, CTNode P, Pattern rTags)
	{
		if (P.isPTag(CTLibEn.PTAG_SBAR) && C.isPTagAny(CTLibEn.POS_IN, CTLibEn.POS_DT))
			return false;
		else if (rTags.pattern().equals(".*"))
			return getSpecialLabel(C) == null;
		else if (rTags.matcher(C.pTag).find())
		{
			if (P.isPTag(CTLibEn.PTAG_VP) && getAuxLabel(C) != null)
				return false;
			
			if (P.isPTagAny(CTLibEn.PTAG_S, CTLibEn.PTAG_SQ, CTLibEn.PTAG_SINV) && C.isPTag(CTLibEn.PTAG_S) && hasAdverbialTag(C))
				return false;
			
			return true;
		}
		else if (P.isPTagAny(CTLibEn.PTAG_NP))
		{
			return C.hasFTag(CTLibEn.FTAG_NOM);
		}
		
		return false;
	}
	
	/** Called by {@link EnglishC2DConverter#findHeadsCoordination(HeadRule, CTNode)}. */
	private CTNode findHeadsCoordinationAux(HeadRule rule, CTNode curr, int bId, int eId, CTNode lastHead)
	{
		CTNode currHead = (eId - bId == 1) ? curr.getChild(bId) : getHead(rule, curr.getChildren(bId, eId), SIZE_HEAD_FLAGS);
		
		if (lastHead != null)
		{
			String label = DEPLibEn.DEP_CONJ;
			
			if (isIntj(currHead))						label = DEPLibEn.DEP_INTJ;
			else if (CTLibEn.isPunctuation(currHead))	label = DEPLibEn.DEP_PUNCT;

			setHeadCoord(currHead, lastHead, label);
		}
		
		return currHead;
	}
	
	private void setHeadCoord(CTNode node, CTNode head, String label)
	{
		if (head.isPhrase())
			node.c2d.setHead(head, label);
		else
			node.c2d.setHeadTerminal(head, label);
	}
	
	private boolean findHyphens(CTNode node)
	{
		int i, size = node.getChildrenSize();
		CTNode prev, hyph, next;
		boolean isFound = false;
		boolean isVP = node.isPTag(CTLibEn.PTAG_VP);
		
		for (i=0; i<size-2; i++)
		{
			prev = node.getChild(i);
			hyph = node.getChild(i+1);
			next = node.getChild(i+2);
			
			if (hyph.isPTag(CTLibEn.POS_HYPH))
			{
				if (isVP)
				{
					prev.c2d.setLabel(DEPLibEn.DEP_HMOD);
					hyph.c2d.setLabel(DEPLibEn.DEP_HYPH);
					next.c2d.setLabel(DEPLibEn.DEP_HMOD);
				}
				else
				{
					prev.c2d.setHead(next, DEPLibEn.DEP_HMOD);
					hyph.c2d.setHead(next, DEPLibEn.DEP_HYPH);
				}
				
				isFound = true;
				i++;
			}
		}
		
		return isFound;
	}
	
	
	/**
	 * Finds the head of appositional modifiers.
	 * @param curr the constituent node to be processed.
	 * @return {@code true} if the specific node contains appositional modifiers. 
	 */
	private boolean findHeadsApposition(CTNode curr)
	{
		if (!curr.isPTagAny(CTLibEn.PTAG_NP, CTLibEn.PTAG_NML) || curr.containsTags("+NN.*"))
			return false;
		
		CTNode fst = curr.getFirstChild("+NP|NML"), snd;
		while (fst != null && fst.containsTags(CTLibEn.POS_POS))
			fst = fst.getNextSibling("+NP|NML");
		
		if (fst == null || fst.c2d.hasHead())	return false;
		
		int i, size = curr.getChildrenSize();
		boolean hasAppo = false;
		
		for (i=fst.getSiblingId()+1; i<size; i++)
		{
			snd = curr.getChild(i);
			if (snd.c2d.hasHead())	continue;
			
			if ((snd.isPTagAny(CTLibEn.PTAG_NP, CTLibEn.PTAG_NML) && !hasAdverbialTag(snd)) ||
				(snd.hasFTagAny(CTLibEn.FTAG_HLN, CTLibEn.FTAG_TTL)) ||
				(snd.isPTag(CTLibEn.PTAG_RRC) && snd.containsTags(CTLibEn.PTAG_NP, "-"+CTLibEn.FTAG_PRD)))
			{
				snd.c2d.setHead(fst, DEPLibEn.DEP_APPOS);
				hasAppo = true;
			}
		}
		
		return hasAppo;
	}

	private boolean findHeadsSmallClause(CTNode node)
	{
		CTNode parent = node.getParent();
		
		if (node.isPTag(CTLibEn.PTAG_S) && !node.containsTags(CTLibEn.PTAG_VP))
		{
			CTNode sbj = node.getFirstChild("-"+CTLibEn.FTAG_SBJ);
			CTNode prd = node.getFirstChild("-"+CTLibEn.FTAG_PRD);
			
			if (sbj != null && prd != null)
			{
				if (parent.isPTag(CTLibEn.PTAG_SQ))
				{
					CTNode vb = parent.getFirstChild("+VB.*");
					
					if (vb != null)
					{
						sbj.c2d.setHead(vb, getDEPLabel(sbj, parent, vb));
						node.pTag = prd.pTag;
						node.addFTag(CTLibEn.FTAG_PRD);
					}
				}
				
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	protected int getHeadFlag(CTNode child)
	{
		if (child.c2d.hasHead())
			return -1;
		
		if (hasAdverbialTag(child))
			return 1;
		
		if (isMeta(child))
			return 2;
		
		if (child.isEmptyCategoryRec() || CTLibEn.isPunctuation(child))
			return 3;
		
		return 0;
	}
	
	// ============================= Get Stanford labels ============================= 
	
	@Override
	protected String getDEPLabel(CTNode C, CTNode P, CTNode p)
	{
		CTNode c = C.c2d.getPhraseHead();
		CTNode d = C.c2d.getDependencyHead();
		String label;
		
		// function tags
		if (hasAdverbialTag(C))
		{
			if (C.isPTagAny(CTLibEn.PTAG_S, CTLibEn.PTAG_SBAR, CTLibEn.PTAG_SINV))
				return DEPLibEn.DEP_ADVCL;
			
			if (C.isPTagAny(CTLibEn.PTAG_NML, CTLibEn.PTAG_NP, CTLibEn.PTAG_QP))
				return DEPLibEn.DEP_NPADVMOD;
		}
		
		if ((label = getSubjectLabel(C, d)) != null)
			return label;
		
		// coordination
		if (C.isPTag(CTLibEn.PTAG_UCP))
		{
			c.addFTags(C.getFTags());
			return getDEPLabel(c, P, p);
		}
		
		// complements
		if (P.isPTagAny(CTLibEn.PTAG_VP, CTLibEn.PTAG_SINV, CTLibEn.PTAG_SQ))
		{
			if (isAcomp(C))	return DEPLibEn.DEP_ACOMP;
			if ((label = getObjectLabel(C)) != null)	return label;
			if (isOprd(C))	return DEPLibEn.DEP_OPRD;
			if (isXcomp(C))	return DEPLibEn.DEP_XCOMP;
			if (isCcomp(C))	return DEPLibEn.DEP_CCOMP;
			if ((label = getAuxLabel(C)) != null)		return label;
		}
		
		if (P.isPTagAny(CTLibEn.PTAG_ADJP, CTLibEn.PTAG_ADVP))
		{
			if (isXcomp(C))	return DEPLibEn.DEP_XCOMP;
			if (isCcomp(C))	return DEPLibEn.DEP_CCOMP;
		}
		
		if (P.isPTagAny(CTLibEn.PTAG_NML, CTLibEn.PTAG_NP, CTLibEn.PTAG_WHNP))
		{
			if (isNfmod(C))	return isInfMod(C) ? DEPLibEn.DEP_INFMOD : DEPLibEn.DEP_PARTMOD;
			if (isRcmod(C))	return DEPLibEn.DEP_RCMOD;
			if (isCcomp(C))	return DEPLibEn.DEP_CCOMP;
		}
		
		if (isPoss(C, P))
			return DEPLibEn.DEP_POSS;
		
		// simple labels
		if ((label = getSimpleLabel(C)) != null)
			return label;
			
		// default
		if (P.isPTagAny(CTLibEn.PTAG_PP, CTLibEn.PTAG_WHPP))
		{
			if (p.getParent() == C.getParent())	// p and C are siblings
			{
				if (p.getSiblingId() < C.getSiblingId())
					return getPmodLabel(C, d);
			}
			else								// UCP
			{
				if (p.getFirstTerminal().getTerminalId() < C.getFirstTerminal().getTerminalId())
					return getPmodLabel(C, d);
			}
		}
		
		if (C.isPTag(CTLibEn.PTAG_SBAR) || isXcomp(C) || (P.isPTag(CTLibEn.PTAG_PP) && CTLibEn.isClause(C)))
			return DEPLibEn.DEP_ADVCL;
		
		if (C.isPTagAny(CTLibEn.PTAG_S, CTLibEn.PTAG_SINV, CTLibEn.PTAG_SQ, CTLibEn.PTAG_SBARQ))
			return DEPLibEn.DEP_CCOMP;
		
		if (P.isPTag(CTLibEn.PTAG_QP))
		{
			if (C.isPTagAny(CTLibEn.POS_CD))
				return DEPLibEn.DEP_NUMBER;
			else
				return DEPLibEn.DEP_QUANTMOD;
		}
		
		if (P.isPTagAny(CTLibEn.PTAG_NML, CTLibEn.PTAG_NP, CTLibEn.PTAG_NX, CTLibEn.PTAG_WHNP) || CTLibEn.isNoun(p))
			return getNmodLabel(C);
		
		if (c != null)
		{
			if ((label = getSimpleLabel(c)) != null)
				return label;
			
			if (d.isPTag(CTLibEn.POS_IN))
				return DEPLibEn.DEP_PREP;
			
			if (CTLibEn.isAdverb(d))
				return DEPLibEn.DEP_ADVMOD;
		}
		
		if ((P.isPTagAny(CTLibEn.PTAG_ADJP, CTLibEn.PTAG_ADVP, CTLibEn.PTAG_PP) || CTLibEn.isAdjective(p) || CTLibEn.isAdverb(p)))
		{
			if (C.isPTagAny(CTLibEn.PTAG_NML, CTLibEn.PTAG_NP, CTLibEn.PTAG_QP) || CTLibEn.isNoun(C))
				return DEPLibEn.DEP_NPADVMOD;
			
			return DEPLibEn.DEP_ADVMOD;
		}
		
		if (d.c2d != null && (label = d.c2d.getLabel()) != null)
			return label;
		
		return DEPLibEn.DEP_DEP;
	}
	
	private boolean hasAdverbialTag(CTNode node)
	{
		return node.hasFTag(CTLibEn.FTAG_ADV) ||  node.hasFTagAny(a_semTags);
	}
	
	private String getObjectLabel(CTNode node)
	{
		if (node.isPTagAny(CTLibEn.PTAG_NP, CTLibEn.PTAG_NML))
		{
			if (node.hasFTag(CTLibEn.FTAG_PRD))
				return DEPLibEn.DEP_ATTR;
			else
				return DEPLibEn.DEP_DOBJ;
		}
		
		return null;
	}
	
	private String getSubjectLabel(CTNode C, CTNode d)
	{
		if (C.hasFTag(CTLibEn.FTAG_SBJ))
		{
			if (CTLibEn.isClause(C))
				return DEPLibEn.DEP_CSUBJ;
			else if (d.isPTag(CTLibEn.POS_EX))
				return DEPLibEn.DEP_EXPL;
			else
				return DEPLibEn.DEP_NSUBJ;
		}
		else if (C.hasFTag(CTLibEn.FTAG_LGS))
			return DEPLibEn.DEP_AGENT;
		
		return null;
	}
	
	private String getSimpleLabel(CTNode C)
	{
		String label;
		
		if (isHyph(C))
			return DEPLibEn.DEP_HYPH;
		
		if (isAmod(C))
			return DEPLibEn.DEP_AMOD;
		
		if (C.isPTagAny(CTLibEn.PTAG_PP, CTLibEn.PTAG_WHPP))
			return DEPLibEn.DEP_PREP;
		
		if (CTLibEn.isCorrelativeConjunction(C))
			return DEPLibEn.DEP_PRECONJ;
		
		if (CTLibEn.isConjunction(C))
			return DEPLibEn.DEP_CC;
		
		if (isPrt(C))
			return DEPLibEn.DEP_PRT;

		if ((label = getSpecialLabel(C)) != null)
			return label;
		
		return null;
	}
	
	private String getSpecialLabel(CTNode C)
	{
		CTNode d = C.c2d.getDependencyHead();
		
		if (CTLibEn.isPunctuation(C) || CTLibEn.isPunctuation(d))
			return DEPLibEn.DEP_PUNCT;
		
		if (isIntj(C) || isIntj(d))
			return DEPLibEn.DEP_INTJ;
		
		if (isMeta(C))
			return DEPLibEn.DEP_META;
		
		if (isPrn(C))
			return DEPLibEn.DEP_PARATAXIS;

		if (isAdv(C))
			return DEPLibEn.DEP_ADVMOD;
		
		return null;
	}
	
	private String getAuxLabel(CTNode node)
	{
		if (node.isPTagAny(CTLibEn.POS_MD, CTLibEn.POS_TO))
			return DEPLibEn.DEP_AUX;

		CTNode vp;
		
		if (CTLibEn.isVerb(node) && (vp = node.getNextSibling(CTLibEn.PTAG_VP)) != null)
		{
			if ((MPLibEn.isBe(node.form) || MPLibEn.isBecome(node.form) || MPLibEn.isGet(node.form)))
			{
				if (vp.containsTags("+VBN|VBD"))
					return DEPLibEn.DEP_AUXPASS;
				
				if (!vp.containsTags("+VB.*") && (vp = vp.getFirstChild(CTLibEn.PTAG_VP)) != null && vp.containsTags("+VBN|VBD"))
					return DEPLibEn.DEP_AUXPASS;
			}
			
			return DEPLibEn.DEP_AUX;
		}
		
		return null;
	}
	
	private String getNmodLabel(CTNode node)
	{
		if (node.isPTagAny(CTLibEn.POS_VBG, CTLibEn.POS_VBN))
			return DEPLibEn.DEP_AMOD;
		
		if (node.isPTagAny(CTLibEn.POS_DT, CTLibEn.POS_WDT, CTLibEn.POS_WP))
			return DEPLibEn.DEP_DET;
		
		if (node.isPTagAny(CTLibEn.PTAG_NML, CTLibEn.PTAG_NP, CTLibEn.POS_FW) || node.pTag.startsWith(CTLibEn.POS_NN))
			return DEPLibEn.DEP_NN;
		
		if (node.isPTagAny(CTLibEn.POS_CD, CTLibEn.PTAG_QP))
			return DEPLibEn.DEP_NUM;

		if (node.isPTag(CTLibEn.POS_POS))
			return DEPLibEn.DEP_POSSESSIVE;
		
		if (node.isPTag(CTLibEn.POS_PDT))
			return DEPLibEn.DEP_PREDET;
		
		return DEPLibEn.DEP_NMOD;
	}
	
	private String getPmodLabel(CTNode C, CTNode d)
	{
		if (C.isPTagAny(CTLibEn.PTAG_NP, CTLibEn.PTAG_NML) || CTLibEn.RE_COMP_POS.matcher(d.pTag).find())
			return DEPLibEn.DEP_POBJ;
		else
			return DEPLibEn.DEP_PCOMP;	
	}
	
	private boolean isHyph(CTNode node)
	{
		return node.isPTag(CTLibEn.POS_HYPH);
	}
	
	private boolean isAmod(CTNode node)
	{
		return node.isPTagAny(CTLibEn.PTAG_ADJP, CTLibEn.PTAG_WHADJP) || CTLibEn.isAdjective(node);
	}
	
	private boolean isAdv(CTNode C)
	{
		if (C.isPTag(CTLibEn.PTAG_ADVP) || CTLibEn.isAdverb(C))
		{
			CTNode P = C.getParent();
			int id = C.getSiblingId();
			
			if (P.isPTagAny(CTLibEn.PTAG_PP, CTLibEn.PTAG_WHPP) && id+1 == P.getChildrenSize() && P.getChild(id-1).isPTagAny(CTLibEn.POS_IN, CTLibEn.POS_TO))
				return false;

			return true;
		}
		
		return false;
	}
	
	private boolean isIntj(CTNode node)
	{
		return node.isPTagAny(CTLibEn.PTAG_INTJ, CTLibEn.POS_UH);
	}
	
	private boolean isMeta(CTNode node)
	{
		return node.isPTagAny(CTLibEn.PTAG_EDITED, CTLibEn.PTAG_EMBED, CTLibEn.PTAG_LST, CTLibEn.PTAG_META, CTLibEn.POS_CODE, CTLibEn.PTAG_CAPTION, CTLibEn.PTAG_CIT, CTLibEn.PTAG_HEADING, CTLibEn.PTAG_TITLE);
	}
	
	private boolean isPrn(CTNode node)
	{
		return node.isPTag(CTLibEn.PTAG_PRN);
	}
	
	private boolean isPrt(CTNode curr)
	{
		return curr.isPTagAny(CTLibEn.PTAG_PRT, CTLibEn.POS_RP);
	}
	
	private boolean isAcomp(CTNode node)
	{
		return node.isPTag(CTLibEn.PTAG_ADJP);
	}
	
	private boolean isOprd(CTNode curr)
	{
		if (curr.hasFTag(DEPLibEn.DEP_OPRD))
			return true;
		
		if (curr.isPTag(CTLibEn.PTAG_S) && !curr.containsTags(CTLibEn.PTAG_VP) && curr.containsTags("-"+CTLibEn.FTAG_PRD))
		{
			CTNode sbj = curr.getFirstChild("-"+CTLibEn.FTAG_SBJ);
			return sbj != null && sbj.isEmptyCategoryRec();
		}
		
		return false;
	}
	
	private boolean isPoss(CTNode curr, CTNode parent)
	{
		if (curr.isPTagAny(CTLibEn.POS_PRPS, CTLibEn.POS_WPS))
			return true;
		
		if (parent.isPTagAny(CTLibEn.PTAG_NML, CTLibEn.PTAG_NP, CTLibEn.PTAG_WHNP, CTLibEn.PTAG_QP, CTLibEn.PTAG_ADJP))
			return curr.containsTags(CTLibEn.POS_POS);
		
		return false;
	}
	
	private boolean isXcomp(CTNode node)
	{
		if (node.isPTag(CTLibEn.PTAG_S))
		{
			CTNode sbj = node.getFirstChild("-"+CTLibEn.FTAG_SBJ);
			
			if (node.containsTags(CTLibEn.PTAG_VP) && (sbj == null || sbj.isEmptyCategoryRec()))
				return true;
		}
		else if (node.hasFTag(DEPLibEn.DEP_RCMOD))
		{
			CTNode s = node.getFirstChild(CTLibEn.PTAG_S);
			if (s != null)	return isXcomp(s);
		}

		return false;
	}
	
	private boolean isCcomp(CTNode node)
	{
		if (node.isPTagAny(CTLibEn.PTAG_S, CTLibEn.PTAG_SQ, CTLibEn.PTAG_SINV, CTLibEn.PTAG_SBARQ))
			return true;
		
		if (node.isPTag(CTLibEn.PTAG_SBAR))
		{
			CTNode comp;
			
			if ((comp = node.getFirstChild(CTLib.POS_NONE)) != null && comp.isForm("0"))
				return true;
			
			if ((comp = node.getFirstChild("+IN|DT")) != null)
			{
				if (comp.form.equalsIgnoreCase("that") || comp.form.equalsIgnoreCase("if") || comp.form.equalsIgnoreCase("whether"))
				{
					comp.c2d.setLabel(DEPLibEn.DEP_COMPLM);
					return true;
				}
			}
			
			if (node.hasFTag(DEPLibEn.DEP_RCMOD) || node.containsTags("+WH.*"))
				return true;
		}
		
		return false;
	}
	
	private boolean isNfmod(CTNode curr)
	{
		return isXcomp(curr) || curr.isPTag(CTLibEn.PTAG_VP);
	}
	
	private boolean isInfMod(CTNode curr)
	{
		CTNode vp = curr.isPTag(CTLibEn.PTAG_VP) ? curr : curr.getFirstDescendant(CTLibEn.PTAG_VP);
		
		if (vp != null)
		{
			CTNode vc = vp.getFirstChild(CTLibEn.PTAG_VP);
			
			while (vc != null)
			{
				vp = vc;
				
				if (vp.getPrevSibling(CTLibEn.POS_TO) != null)
					return true;
				
				vc = vp.getFirstChild(CTLibEn.PTAG_VP);
			}
			
			return vp.containsTags(CTLibEn.POS_TO);
		}
		
		return false;
	}
	
	private boolean isRcmod(CTNode curr)
	{
		return curr.isPTag(CTLibEn.PTAG_RRC) || curr.hasFTag(DEPLibEn.DEP_RCMOD) || (curr.isPTag(CTLibEn.PTAG_SBAR) && curr.containsTags("+WH.*"));
	}
	
	// ============================= Get a dependency tree =============================
	
	private DEPTree getDEPTree(CTTree cTree)
	{
		DEPTree dTree = initDEPTree(cTree);
		addDEPHeads(dTree, cTree);
		
		if (dTree.containsCycle())
			System.err.println("Error: cyclic dependencies exist");
		
		splitLabels(dTree);
		addXHeads(dTree);
		addFeats(dTree, cTree, cTree.getRoot());
		addPBArgs(dTree, cTree);
		mergeLabels(dTree);
		
		return dTree;
	}
	
	private void mergeLabels(DEPTree dTree)
	{
		int i, size = dTree.size();
		DEPNode node;
		
		for (Pair<String,Set<String>> p : l_mergeLabels)
		{
			for (i=1; i<size; i++)
			{
				node = dTree.get(i);
				
				if (p.o2.contains(node.getLabel()))
					node.setLabel(p.o1);
			}
		}
	}
	
	/** Adds dependency heads. */
	private void addDEPHeads(DEPTree dTree, CTTree cTree)
	{
		int currId, headId, size = dTree.size(), rootCount = 0;
		CTNode cNode, ante;
		DEPNode dNode;
		String label;
		
		for (currId=1; currId<size; currId++)
		{
			dNode  = dTree.get(currId);
			cNode  = cTree.getToken(currId-1);
			headId = cNode.c2d.d_head.getTokenId() + 1;
			
			if (currId == headId)	// root
			{
				dNode.setHead(dTree.get(DEPLib.ROOT_ID), DEPLibEn.DEP_ROOT);
				rootCount++;
			}
			else
			{
				label = cNode.c2d.s_label;
				
				if (cNode.isPTagAny(CTLibEn.POS_IN, CTLibEn.POS_TO, CTLibEn.POS_DT) && cNode.getParent().isPTag(CTLibEn.PTAG_SBAR) && !label.equals(DEPLibEn.DEP_COMPLM))
					label = DEPLibEn.DEP_MARK;
				
				dNode.setHead(dTree.get(headId), label);
			}
			
			if ((ante = cNode.getAntecedent()) != null)
				dNode.addXHead(getDEPNode(dTree, ante), DEPLibEn.DEP_REF);
		}
		
		if (rootCount > 1)	System.err.println("Warning: multiple roots exist");
	}
	
	/** Splits certain Stanford dependency labels into finer-grained labels. */
	private void splitLabels(DEPTree tree)
	{
		int i, size = tree.size();
		List<DEPNode> list;
		DEPNode node;

		tree.setDependents();
		
		for (i=1; i<size; i++)
		{
			node = tree.get(i);
			
			if (node.isLabel(DEPLibEn.DEP_ADVMOD))
			{
				if (MPLibEn.RE_NEG.matcher(node.form.toLowerCase()).find())
					node.setLabel(DEPLibEn.DEP_NEG);
			}
			
			if (node.containsDependent(DEPLibEn.DEP_AUXPASS))
			{
				for (DEPNode child : node.getDependentsByLabels(DEPLibEn.DEP_CSUBJ, DEPLibEn.DEP_NSUBJ))
					child.setLabel(child.getLabel()+DEPLibEn.DEP_PASS);
			}
			
			if ((list = node.getDependentsByLabels(DEPLibEn.DEP_DOBJ)).size() > 1)
				list.get(0).setLabel(DEPLibEn.DEP_IOBJ);
		}
	}
	
	/** Adds secondary dependency heads. */
	private void addXHeads(DEPTree dTree)
	{
		for (CTNode curr : m_xsbj.keySet())
		{
			if (curr.c2d != null)
				addXHeadsAux(dTree, curr, m_xsbj.get(curr), DEPLibEn.DEP_XSUBJ);
		}
		
		for (CTNode curr : m_rnr.keySet())
		{
			if (curr.getParent() == null)
				continue;
			
			if (curr.getParent().c2d.getPhraseHead() != curr)
				addXHeadsAux(dTree, curr, m_rnr.get(curr), DEPLibEn.DEP_RNR);
			else
				addXChildren(dTree, curr, m_rnr.get(curr), DEPLibEn.DEP_RNR);
		}
	}
	
	/** Called by {@link EnglishC2DConverter#addDEPHeads(DEPTree, CTTree)} */
	private void addXHeadsAux(DEPTree dTree, CTNode cNode, Deque<CTNode> dq, String label)
	{
		DEPNode node = getDEPNode(dTree, cNode);
		DEPNode head;
		
		for (CTNode cHead : dq)
		{
			head = getDEPNode(dTree, cHead);
			node.addXHead(head, label);
			
			if (label.equals(DEPLibEn.DEP_XSUBJ) && head.isLabel(DEPLibEn.DEP_CCOMP))
				head.setLabel(DEPLibEn.DEP_XCOMP);
		}
	}
	
	/** {@link EnglishC2DConverter#addDEPHeads(DEPTree, CTTree)} */
	private void addXChildren(DEPTree dTree, CTNode cHead, Deque<CTNode> dq, String label)
	{
		DEPNode head = getDEPNode(dTree, cHead);
		DEPNode node;
		
		for (CTNode cNode : dq)
		{
			node = getDEPNode(dTree, cNode);
			node.addXHead(head, label);			
		}
	}
	
	/** Add extra features. */
	private void addFeats(DEPTree dTree, CTTree cTree, CTNode cNode)
	{
		CTNode ante;
		String feat;
		
		if (cNode.gapIndex != -1 && cNode.getParent().gapIndex == -1 && (ante = cTree.getCoIndexedAntecedent(cNode.gapIndex)) != null)
		{
			DEPNode dNode = getDEPNode(dTree, cNode);
			dNode.addXHead(getDEPNode(dTree, ante), DEPLibEn.DEP_GAP);
		}
		
		if ((feat = getFunctionTags(cNode, s_semTags)) != null)
			cNode.c2d.putFeat(DEPLibEn.FEAT_SEM, feat);
		
		if ((feat = getFunctionTags(cNode, s_synTags)) != null)
			cNode.c2d.putFeat(DEPLibEn.FEAT_SYN, feat);

		for (CTNode child : cNode.getChildren())
			addFeats(dTree, cTree, child);
	}
	
	/** Called by {@link EnglishC2DConverter#addFeats(DEPTree, CTTree, CTNode)}. */
	private String getFunctionTags(CTNode node, Set<String> sTags)
	{
		List<String> tags = new ArrayList<String>();
		
		for (String tag : node.getFTags())
		{
			if (sTags.contains(tag))
				tags.add(tag);
		}
		
		if (tags.isEmpty())	return null;
		Collections.sort(tags);

		StringBuilder build = new StringBuilder();
		
		for (String tag : tags)
		{
			build.append(DEPFeat.DELIM_VALUES);
			build.append(tag);
		}
		
		return build.substring(DEPFeat.DELIM_VALUES.length());
	}
	
	// ============================= Add PropBank arguments =============================
	
	private void addPBArgs(DEPTree dTree, CTTree cTree)
	{
		CTNode root = cTree.getRoot();
		dTree.initSHeads();
		
		if (root.pbArgs != null)
		{
			initPBArgs(dTree, cTree, root);
			arrangePBArgs(dTree);
			relabelArgNs(dTree);
		}
	}
	
	private void initPBArgs(DEPTree dTree, CTTree cTree, CTNode cNode)
	{
		if (!cNode.isPTag(CTLib.PTAG_TOP))
		{
			DEPNode dNode, sHead;
			
			if (cNode.isPhrase())
				dNode = getDEPNode(dTree, cNode);
			else
				dNode = dTree.get(cNode.getTokenId()+1);
			
			for (StringIntPair p : cNode.pbArgs)
			{
				sHead = dTree.get(p.i);
				
				if (isRefArgument(cNode))
					p.s = "R-"+p.s;
				
				if (!dNode.containsSHead(sHead) && dNode != sHead)
					dNode.addSHead(sHead, p.s);
			}
		}
		
		for (CTNode child : cNode.getChildren())
			initPBArgs(dTree, cTree, child);
	}
	
	private boolean isRefArgument(CTNode cNode)
	{
		if (CTLibEn.isRelPhrase(cNode))
			return true;
		
		if (cNode.isPTag(CTLibEn.PTAG_PP) && containsRefArgument(cNode))
			return true;

		return false;
	}
	
	private boolean containsRefArgument(CTNode cNode)
	{
		for (CTNode child : cNode.getChildren()) 
		{
			if (child.isPTagAny(CTLibEn.PTAG_ADJP, CTLibEn.PTAG_ADVP, CTLibEn.PTAG_NP, CTLibEn.PTAG_PP))
			{
				for (CTNode gc : child.getChildren())
				{
					if (!gc.isEmptyCategoryRec() && CTLibEn.isRelPhrase(gc))
						return true;		
				}
			}
		}
		
		return false;
	}
	
	private void arrangePBArgs(DEPTree dTree)
	{
		int i, size = dTree.size();
		List<SRLArc> remove;
		DEPNode node, head;
		String label;
		
		for (i=1; i<size; i++)
		{
			node   = dTree.get(i);
			remove = Lists.newArrayList();
			
			for (SRLArc arc : node.getSHeads())
			{
				head  = arc.getNode();
				label = arc.getLabel();
				
				if (ancestorHasSHead(node, head, label))
					remove.add(arc);
			//	else if (rnrHasSHead(node, head, label))
			//		remove.add(arc);
			}
			
			node.removeSHeads(remove);
		}
	}
	
	private boolean ancestorHasSHead(DEPNode dNode, DEPNode sHead, String label)
	{
		DEPNode dHead = dNode.getHead();
		
		while (dHead != null)
		{
			if (dHead.isArgumentOf(sHead, label))
				return true;
			
			dHead = dHead.getHead();
		}
		
		return false;
	}
	
	protected boolean rnrHasSHead(DEPNode dNode, DEPNode sHead, String label)
	{
		for (DEPArc rnr : dNode.getXHeads(DEPLibEn.DEP_RNR))
		{
			if (rnr.getNode().isArgumentOf(sHead, label))
				return true;
		}
		
		return false;
	}
	
	private void relabelArgNs(DEPTree dTree)
	{
		Map<String,DEPNode> map = new HashMap<String,DEPNode>();
		int i, size = dTree.size();
		List<SRLArc> remove;
		DEPNode node;
		String key;
		
		for (i=1; i<size; i++)
		{
			node   = dTree.get(i);
			remove = Lists.newArrayList();
			
			for (DEPArc arc : node.getSHeads())
			{
				if (arc.getLabel().startsWith(SRLLib.PREFIX_REFERENT))
					continue;
				
				if (arc.getLabel().startsWith("AM"))
					continue;
				
				key = arc.toString();
				
				if (map.containsKey(key))
					arc.setLabel(SRLLib.PREFIX_CONCATENATION + arc.getLabel());
				else
					map.put(key, node);
			}
			
			node.removeSHeads(remove);
		}
	}
	
	private DEPNode getDEPNode(DEPTree dTree, CTNode cNode)
	{
		return dTree.get(cNode.c2d.getDependencyHead().getTokenId() + 1);
	}
	
	
	// ============================= Get CoNLL labels =============================
	
/*	private void convertToCoNLLLabels(DEPTree tree)
	{
		int i, size = tree.size();
		DEPNode node;
		
		for (i=1; i<size; i++)
		{
			node = tree.get(i);
			
			if (node.getLabel().equals(DEPLibEn.DEP_ADVMOD))
				node.setLabel(DEPLibEn.CONLL_ADV);
			else if (node.getLabel().equals(DEPLibEn.DEP_APPOS))
				node.setLabel(DEPLibEn.CONLL_APPO);
			else if (node.getLabel().equals(DEPLibEn.DEP_CONJ))
				node.setLabel(DEPLibEn.CONLL_CONJ);
			else if (node.getLabel().equals(DEPLibEn.DEP_INTJ))
				node.setLabel(DEPLibEn.CONLL_INTJ);
			else if (node.getLabel().equals(DEPLibEn.DEP_META))
				node.setLabel(DEPLibEn.CONLL_META);
			else if (node.getLabel().equals(DEPLibEn.DEP_PARATAXIS))
				node.setLabel(DEPLibEn.CONLL_PRN);
			else if (node.getLabel().equals(DEPLibEn.DEP_PRT))
				node.setLabel(DEPLibEn.CONLL_PRT);
			else if (node.getLabel().equals(DEPLibEn.DEP_PUNCT))
				node.setLabel(DEPLibEn.CONLL_P);
			else if (node.getLabel().equals(DEPLibEn.DEP_ROOT))
				node.setLabel(DEPLibEn.CONLL_ROOT);
		}
	}
	
	public String getCoNLLLabel(CTNode C, CTNode P, CTNode p)
	{
		CTNode c = C.c2d.getPhraseHead();
		CTNode d = C.c2d.getDependencyHead();
		String label;
		
		// function tags
		if (hasAdverbialTag(C))
			return DEPLibEn.CONLL_ADV;

		if ((label = getCoNLLFunctionTag(C)) != null)
			return label;
		
		// coordination
		if (C.isPTag(CTLibEn.PTAG_UCP))
		{
			c.addFTags(C.getFTags());
			return getCoNLLLabel(c, P, p);
		}
		
		// complements
		if (P.isPTagAny(CTLibEn.PTAG_VP, CTLibEn.PTAG_SINV, CTLibEn.PTAG_SQ))
		{
			if (getObjectLabel(C) != null)	return DEPLibEn.CONLL_OBJ;
			if (isOprd(C))	return DEPLibEn.CONLL_OPRD;
			if (isXcomp(C))	return DEPLibEn.CONLL_XCOMP;
			if (isCcomp(C))	return DEPLibEn.CONLL_OBJ;
			if ((label = getCoNLLAuxLabel(C, p, d)) != null)	return label;
		}
		
		// subordinate conjunctions
		if (P.isPTag(CTLibEn.PTAG_SBAR) && p.isPTagAny(CTLibEn.POS_IN, CTLibEn.POS_TO, CTLibEn.POS_DT))
			return DEPLibEn.CONLL_SUB;
	
		// simple labels
		if ((label = getCoNLLSimpleLabel(C)) != null)
			return label;
		
		// default
		if (P.isPTagAny(CTLibEn.PTAG_PP, CTLibEn.PTAG_WHPP))
			return DEPLibEn.CONLL_PMOD;
		
		if (C.isPTag(CTLibEn.PTAG_SBAR) || isXcomp(C) || C.isPTag(CTLibEn.PTAG_PP))
			return DEPLibEn.CONLL_ADV;
		
		if (P.isPTag(CTLibEn.PTAG_QP))
			return DEPLibEn.CONLL_QMOD;
		
		if (P.isPTagAny(CTLibEn.PTAG_NML, CTLibEn.PTAG_NP, CTLibEn.PTAG_NX, CTLibEn.PTAG_WHNP) || CTLibEn.isNoun(p))
			return DEPLibEn.CONLL_NMOD;
		
		if ((P.isPTagAny(CTLibEn.PTAG_ADJP, CTLibEn.PTAG_ADVP, CTLibEn.PTAG_WHADJP, CTLibEn.PTAG_WHADVP) || CTLibEn.isAdjective(p) || CTLibEn.isAdverb(p)))
			return DEPLibEn.CONLL_AMOD;

		if (c != null)
		{
			if ((label = getCoNLLSimpleLabel(c)) != null)
				return label;
			
			if (CTLibEn.isAdverb(d))
				return DEPLibEn.CONLL_ADV;
		}
		
		return DEPLibEn.CONLL_DEP;
	}
	
	private String getCoNLLFunctionTag(CTNode C)
	{
		if (C.hasFTag(CTLibEn.FTAG_SBJ))
			return DEPLibEn.CONLL_SBJ;
		
		if (C.hasFTag(CTLibEn.FTAG_LGS))
			return DEPLibEn.CONLL_LGS;
		
		if (C.hasFTag(CTLibEn.FTAG_DTV))
			return DEPLibEn.CONLL_DTV;
		
		if (C.hasFTag(CTLibEn.FTAG_PRD))
			return DEPLibEn.CONLL_PRD;
		
		if (C.hasFTag(CTLibEn.FTAG_PUT))
			return DEPLibEn.CONLL_PUT;
		
		if (C.hasFTag(DEPLibEn.CONLL_EXTR))
			return DEPLibEn.CONLL_EXTR;
		
		return null;
	}
	
	private String getCoNLLAuxLabel(CTNode C, CTNode p, CTNode d)
	{
		CTNode pd = p.c2d.getDependencyHead();
		
		if (C.isPTag(CTLibEn.PTAG_VP) || CTLibEn.isVerb(d))
		{
			if (pd.isPTag(CTLibEn.POS_TO))
				return DEPLibEn.CONLL_IM;
			
			if (CTLibEn.isVerb(pd))
				return DEPLibEn.CONLL_VC;
		}
		
		return null;
	}
	
	private String getCoNLLSimpleLabel(CTNode C)
	{
		String label;
		
		if (CTLibEn.isConjunction(C))
			return DEPLibEn.CONLL_COORD;
		
		if (isPrt(C))
			return DEPLibEn.DEP_PRT;

		if ((label = getSpecialLabel(C)) != null)
			return label;
		
		return null;
	}*/
}
