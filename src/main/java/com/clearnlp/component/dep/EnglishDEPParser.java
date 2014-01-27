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
package com.clearnlp.component.dep;

import java.io.ObjectInputStream;
import java.util.List;
import java.util.regex.Pattern;

import com.clearnlp.classification.feature.JointFtrXml;
import com.clearnlp.classification.model.StringModel;
import com.clearnlp.classification.prediction.StringPrediction;
import com.clearnlp.classification.train.StringTrainSpace;
import com.clearnlp.component.state.DEPState;
import com.clearnlp.constant.english.ENAux;
import com.clearnlp.constituent.CTLibEn;
import com.clearnlp.dependency.DEPLabel;
import com.clearnlp.dependency.DEPLib;
import com.clearnlp.dependency.DEPLibEn;
import com.clearnlp.dependency.DEPNode;
import com.clearnlp.morphology.MPLibEn;
import com.clearnlp.util.UTCollection;

/**
 * Dependency parser using selectional branching.
 * @since 1.3.2
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class EnglishDEPParser extends AbstractDEPParser
{
//	====================================== CONSTRUCTORS ======================================
	
	/** Constructs a dependency parsing for training. */
	public EnglishDEPParser(JointFtrXml[] xmls, StringTrainSpace[] spaces, Object[] lexica, double margin, int beams)
	{
		super(xmls, spaces, lexica, margin, beams);
	}
	
	/** Constructs a dependency parsing for developing. */
	public EnglishDEPParser(JointFtrXml[] xmls, StringModel[] models, Object[] lexica, double margin, int beams)
	{
		super(xmls, models, lexica, margin, beams);
	}
	
	/** Constructs a dependency parser for bootsrapping. */
	public EnglishDEPParser(JointFtrXml[] xmls, StringTrainSpace[] spaces, StringModel[] models, Object[] lexica, double margin, int beams)
	{
		super(xmls, spaces, models, lexica, margin, beams);
	}
	
	/** Constructs a dependency parser for decoding. */
	public EnglishDEPParser(ObjectInputStream in)
	{
		super(in);
	}
	
//	================================ RE-RANKING ================================

	@Override
	protected void rerankPredictions(List<StringPrediction> ps, DEPState state)
	{
		if (!isDecode()) return;
		
		DEPNode lambda = state.getLambda();
		DEPNode beta   = state.getBeta();
		
		int i, size = ps.size(), count = 0;
		boolean lChanged, gChanged = false;
		StringPrediction prediction;
		DEPLabel label;
		
		for (i=0; i<size; i++)
		{
			lChanged = false;
			prediction = ps.get(i);
			label = new DEPLabel(prediction.label, prediction.score);
			
			if (label.isArc(LB_LEFT))
			{
				if (rerankUnique(prediction, label, beta, DEPLibEn.P_SBJ, lambda.id+1, beta.id, state))
					lChanged = true;
				else if (rerankNonHead(prediction, beta))
					lChanged = true;
			}
			else if (label.isArc(LB_RIGHT))
			{
				if (rerankUnique(prediction, label, lambda, DEPLibEn.P_SBJ, 1, beta.id, state))
					lChanged = true;
				else if (rerankNonHead(prediction, lambda))
					lChanged = true;
			}
			
			if (lChanged)
				gChanged = true;
			else
			{
				count++;
				if (count >= 2) break;
			}
		}
		
		if (gChanged) UTCollection.sortReverseOrder(ps);
	}
	
	private boolean rerankUnique(StringPrediction prediction, DEPLabel label, DEPNode head, Pattern p, int bIdx, int eIdx, DEPState state)
	{
		if (p.matcher(label.deprel).find())
		{
			DEPNode node;
			int i;
			
			for (i=bIdx; i<eIdx; i++)
			{
				node = state.getNode(i);
				
				if (node.isDependentOf(head) && p.matcher(node.getLabel()).find())
				{
					prediction.score = -1;
					return true;
				}
			}
		}
		
		return false;
	}
	
	private boolean rerankNonHead(StringPrediction prediction, DEPNode head)
	{
		if (isNotHead(head))
		{
			prediction.score = -1;
			return true;
		}
		
		return false;
	}
	
//	================================ RESET PRE ================================
	
	@Override
	protected boolean resetPre(DEPState state)
	{
		if (!isDecode()) return false;
		int idx = resetBeVerb(state);
		
		if (idx > 0)
		{
			state.setLambda(idx-1);
			state.addScore(100d);
			return true;
		}
		
		return false;
	}
	
	private int resetBeVerb(DEPState state)
	{
		DEPNode lambda = state.getLambda();
		DEPNode beta   = state.getBeta();
		
		if (isOutOfDomain(state, lambda, beta))
			return -1;
		
		DEPNode beVerb = lambda.getHead();
		
		if (!isAuxiliaryBe(state, beVerb, lambda, beta))
			return -1;
		
		String subj  = lambda.getLabel();
		int    vType = 0;
		
		if (beta.isPos(CTLibEn.POS_VBN))
			vType = 1;
		else if (beta.isPos(CTLibEn.POS_VBG))
			vType = 2;
		else if (beta.isPos(CTLibEn.POS_VBD))
		{
			String p2 = beta.getFeat(DEPLib.FEAT_POS2);
			if (p2 != null && p2.equals(CTLibEn.POS_VBN))	vType = 1;
		}
		
		// be - subj(lambda) -  vb[ng](beta)
		if (vType > 0 && (DEPLibEn.isSubject(subj) || (subj.equals(DEPLibEn.DEP_ATTR) && hasNoDependent(beVerb, 1, lambda.id, state) && hasNoDependent(beVerb, lambda.id+1, beta.id, state))))
		{
		//	System.out.println(beVerb.form+" "+lambda.id+" "+beta.id+"\n"+state.getTree().toStringDEP()+"\n");
			int i, size = beta.id;
			DEPNode node;
			
			for (i=beVerb.id+1; i<size; i++)
			{
				node = state.getNode(i);
				
				if (node.isDependentOf(beVerb))
					node.setHead(beta);
			}
			
			clearPreviousDependents(beVerb, state);
			beVerb.setHead(beta);
			
			if (vType == 1)
			{
				beVerb.setLabel(DEPLibEn.DEP_AUXPASS);
				
				if (subj.equals(DEPLibEn.DEP_NSUBJ) || subj.equals(DEPLibEn.DEP_ATTR))
					lambda.setLabel(DEPLibEn.DEP_NSUBJPASS);
				else if (subj.equals(DEPLibEn.DEP_CSUBJ))
					lambda.setLabel(DEPLibEn.DEP_CSUBJPASS);
			}
			else
			{
				beVerb.setLabel(DEPLibEn.DEP_AUX);
			}
			
			if (beta.isPos(CTLibEn.POS_VBD))
				beta.pos = CTLibEn.POS_VBN;
			
			return beVerb.id;
		}
		
		return -1;
	}
	
	private boolean isOutOfDomain(DEPState state, DEPNode lambda, DEPNode beta)
	{
		DEPNode node;
		int i;
		
		for (i=beta.id-1; i>lambda.id; i--)
		{
			node = state.getNode(i);
			
			if (node.isDependentOf(beta) && (DEPLibEn.isSubject(node.getLabel()) || DEPLibEn.isAuxiliary(node.getLabel())))
				return true;
		}
		
		return false;
	}
	
	private boolean isAuxiliaryBe(DEPState state, DEPNode beVerb, DEPNode lambda, DEPNode beta)
	{
		if (beVerb != null && beVerb.isLemma(ENAux.BE) && beVerb.id < lambda.id && !beta.isDescendentOf(beVerb))
		{
			DEPNode prev = state.getNode(beVerb.id-1);
			if (prev != null && (prev.isLemma("here") || prev.isLemma("there"))) return false;
			
			DEPNode gHead = beVerb.getHead();
			return gHead == null || gHead.id < beVerb.id;
		}
		
		return false;
	}
	
	private boolean hasNoDependent(DEPNode head, int bIdx, int eIdx, DEPState state)
	{
		DEPNode node;
		int i;
		
		for (i=bIdx; i<eIdx; i++)
		{
			node = state.getNode(i);
			
			if (node.isDependentOf(head))
				return false;
		}
		
		return true;
	}
	
	private boolean clearPreviousDependents(DEPNode head, DEPState state)
	{
		boolean found = false;
		DEPNode node;
		int i;
		
		for (i=head.id-1; i>0; i--)
		{
			node = state.getNode(i);
			
			if (node.isDependentOf(head))
			{
				node.clearHead();
				state.pushBack(node.id);
				found = true;
			}
		}
		
		return found;
	}
	
//	================================ RESET POST ================================
	
	@Override
	protected void resetPost(DEPNode lambda, DEPNode beta, DEPLabel label, DEPState state)
	{
		if (!isDecode()) return;
		
		if (lambda.isDependentOf(beta))
		{
			resetVerbPOSTag(beta, lambda, state);
			resetNotHead(lambda, state);
		}
		else if (beta.isDependentOf(lambda))
		{
			resetVerbPOSTag(lambda, beta, state);
		}
	}
	
	private void resetVerbPOSTag(DEPNode head, DEPNode dep, DEPState state)
	{
		String p2 = head.getFeat(DEPLib.FEAT_POS2);
		
		if (p2 != null)
		{
			boolean vb2 = p2.equals(CTLibEn.POS_UH) || p2.equals(CTLibEn.POS_FW);
			
			if ((MPLibEn.isNoun(head.pos) || head.isPos(CTLibEn.POS_IN)) && ((MPLibEn.isVerb(p2) || vb2)))
			{
				if (dep.isLabel(DEPLibEn.DEP_DOBJ) || DEPLibEn.isAuxiliary(dep.getLabel()) || dep.isLabel(DEPLibEn.DEP_PRT) || dep.isLabel(DEPLibEn.DEP_ACOMP))// || DEPLibEn.isSubject(dep.getLabel()) || dep.equals(DEPLibEn.DEP_EXPL)) || dep.isLabel(DEPLibEn.DEP_AGENT) || dep.isLabel(DEPLibEn.DEP_ATTR) || dep.isLabel(DEPLibEn.DEP_IOBJ)))
				{
					if (vb2) head.addFeat(DEPLib.FEAT_POS2, CTLibEn.POS_VB);
					state.add2ndPOSScore(head.id, 1d);
				}
			}
		}
	}
	
	private boolean resetNotHead(DEPNode lambda, DEPState state)
	{
		if (isNotHead(lambda) && clearPreviousDependents(lambda, state))
		{
			state.setLambda(lambda.id);
			state.passAux();
			return true;
		}
		
		return false;
	}
	
	@Override
	protected boolean isNotHead(DEPNode node)
	{
		if (!isDecode()) return false;
		String label = node.getLabel();
		return label != null && DEPLibEn.isAuxiliary(label);
	}
	
//	================================ POST PARSE ================================
	
	@Override
	protected void postProcess(DEPState state)
	{
		if (!isDecode()) return;
		
		int i, size = state.getTreeSize(), vCount = 0;
		DEPNode node;
		
		for (i=1; i<size; i++)
		{
			node = state.getNode(i);
			
			if (!postProcessPP(node))
				if (!postProcessBeProperNounAdjective(node, state))
					postProcessAttributeInQuestion(node, state);
			
			if (MPLibEn.isVerb(node.pos)) vCount++;
		}
		
		if (vCount == 0)
		{
			for (DEPNode root : state.getTree().getRoots())
			{
				if (resetVerbPOSTag(root))
					break;
			}
		}
	}
	
	private boolean postProcessPP(DEPNode node)
	{
		DEPNode head = node.getHead();
		if (head == null)	return false;
		
		DEPNode gHead = head.getHead();
		if (gHead == null)	return false;
		
		DEPNode ggHead = gHead.getHead();
		if (ggHead == null)	return false;
		
		// node ggHead:VB gHead:NN head:IN
		if (node.id < ggHead.id && ggHead.id < gHead.id && gHead.id < head.id && head.isPos(CTLibEn.POS_IN) && MPLibEn.isNoun(gHead.pos) && MPLibEn.isVerb(ggHead.pos))
		{
			head.setHead(ggHead);
			return true;
		}
		else if (gHead.id < head.id && head.id < node.id && gHead.isLemma("ask") && head.isLabel(DEPLibEn.DEP_DOBJ) && node.isLabel(DEPLibEn.DEP_CCOMP))
		{
			node.setHead(gHead);
			return true;
		}
		
		return false;
	}
	
	private boolean postProcessBeProperNounAdjective(DEPNode be, DEPState state)
	{
		if (be.isLemma(ENAux.BE) && MPLibEn.isVerb(be.pos) && be.id+2 < state.getTreeSize())
		{
			DEPNode jj = state.getNode(be.id+2);
			
			if (MPLibEn.isAdjective(jj.pos) && jj.isDependentOf(be) && jj.isLabel(DEPLibEn.DEP_NSUBJ))
			{
				DEPNode nnp = state.getNode(be.id+1);
				
				if (nnp.isPos(CTLibEn.POS_NNP) && nnp.isDependentOf(jj) && nnp.isLabel(DEPLibEn.DEP_NPADVMOD) && !containsDependent(state, be, DEPLibEn.DEP_ATTR, jj.id))
				{
					nnp.setHead(be, DEPLibEn.DEP_NSUBJ);
					jj.setLabel(DEPLibEn.DEP_ATTR);
					return true;
				}
			}
		}
		
		return false;
	}
	
	private boolean postProcessAttributeInQuestion(DEPNode appos, DEPState state)
	{
		if (!appos.isLabel(DEPLibEn.DEP_APPOS))
			return false;
		
		DEPNode nsubj = appos.getHead();
		if (nsubj == null || !nsubj.isLabel(DEPLibEn.DEP_NSUBJ) || nsubj.id > appos.id)
			return false;
		
		DEPNode be = nsubj.getHead();
		if (be == null || !be.isLemma(ENAux.BE) || be.id > appos.id)
			return false;
		
		int i, size = state.getTreeSize();
		DEPNode node;
		
		for (i=appos.id; i<size; i++)
		{
			node = state.getNode(i);
					
			if (node.isDependentOf(be, DEPLibEn.DEP_ATTR))
				return false;
		}
		
		appos.setHead(be, DEPLibEn.DEP_ATTR);
		return true;
	}
	
	private boolean containsDependent(DEPState state, DEPNode head, String label, int bIdx)
	{
		DEPNode node;
		int i;
		
		for (i=state.getTreeSize()-1; i>bIdx; i--)
		{
			node = state.getNode(i);
			
			if (node.isDependentOf(head) && node.isLabel(label))
				return true;
		}
		
		return false;
	}
	
	private boolean resetVerbPOSTag(DEPNode root)
	{
		String p2 = root.getFeat(DEPLib.FEAT_POS2);
				
		if (p2 != null && MPLibEn.isVerb(p2))
		{
			root.pos = p2;
			return true;
		}
		
		return false;
	}
}
