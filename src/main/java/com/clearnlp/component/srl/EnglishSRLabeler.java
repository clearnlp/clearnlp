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
package com.clearnlp.component.srl;

import java.io.ObjectInputStream;
import java.util.List;

import com.clearnlp.classification.feature.JointFtrXml;
import com.clearnlp.classification.model.StringModel;
import com.clearnlp.classification.prediction.StringPrediction;
import com.clearnlp.classification.train.StringTrainSpace;
import com.clearnlp.component.state.SRLState;
import com.clearnlp.constant.english.ENPrep;
import com.clearnlp.constant.english.ENTime;
import com.clearnlp.constituent.CTLibEn;
import com.clearnlp.dependency.DEPArc;
import com.clearnlp.dependency.DEPLibEn;
import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dependency.DEPTree;
import com.clearnlp.dependency.srl.SRLLib;
import com.clearnlp.morphology.MPLibEn;
import com.clearnlp.propbank.PBLib;
import com.clearnlp.propbank.frameset.AbstractFrames;
import com.clearnlp.propbank.frameset.PBRoleset;
import com.clearnlp.propbank.frameset.PBType;

/**
 * @since 1.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class EnglishSRLabeler extends AbstractSRLabeler
{
	/** Constructs a semantic role labeler for collecting lexica. */
	public EnglishSRLabeler(JointFtrXml[] xmls, AbstractFrames frames)
	{
		super(xmls, frames);
	}
	
	/** Constructs a semantic role labeler for training. */
	public EnglishSRLabeler(JointFtrXml[] xmls, StringTrainSpace[] spaces, Object[] lexica)
	{
		super(xmls, spaces, lexica);
	}
	
	/** Constructs a semantic role labeler for developing. */
	public EnglishSRLabeler(JointFtrXml[] xmls, StringModel[] models, Object[] lexica)
	{
		super(xmls, models, lexica);
	}
	
	/** Constructs a semantic role labeler for decoding. */
	public EnglishSRLabeler(ObjectInputStream in)
	{
		super(in);
	}
	
	/** Constructs a semantic role labeler for bootstrapping. */
	public EnglishSRLabeler(JointFtrXml[] xmls, StringTrainSpace[] spaces, StringModel[] models, Object[] lexica)
	{
		super(xmls, spaces, models, lexica);
	}
	
	@Override
	protected boolean rerankFromArgument(StringPrediction prediction, DEPNode arg)
	{
		if (prediction.label.endsWith(SRLLib.ARGM_DIS) && DEPLibEn.isSubject(arg.getLabel()))
		{
			prediction.score = -1;
			return true;
		}
		
		return false;
	}

	@Override
	protected String getHardLabel(SRLState state, String label)
	{
		DEPNode pred = state.getCurrentPredicate();
		DEPNode arg  = state.getCurrentArgument();
		DEPNode dep;
		
		if (arg.isLemma(ENPrep.FOR))
		{
			if (pred.isLemma("search") && PBLib.isCoreNumberedArgument(label))
				return SRLLib.ARG2;
		}
		else if (arg.isLemma(ENPrep.AT))
		{
			if ((dep = state.getRightmostDependent(arg.id)) != null && dep.isPos(CTLibEn.POS_NN) && (ENTime.isTemporalSuffix(dep.lemma)))
				return SRLLib.ARGM_TMP;
		}
		
		return null;
	}
	
	@Override
	protected PBType getPBType(DEPNode pred)
	{
		if (MPLibEn.isVerb(pred.pos))	return PBType.VERB;
		if (MPLibEn.isNoun(pred.pos))	return PBType.NOUN;
		
		return null;
	}
	
	@Override
	protected void postLabel(SRLState state)
	{
		if (isDecode())
		{
			DEPTree tree = state.getTree();
			List<List<DEPArc>> argLists = DEPLibEn.postLabel(tree);
			postLabelReferent(tree, argLists);
		}
	}
	
	@Override
	protected DEPNode getPossibleDescendent(DEPNode pred, DEPNode arg)
	{
		DEPNode pobj;
		
		if (arg.isLabel(DEPLibEn.DEP_POBJ))
			return arg;

		if (arg.isPos(CTLibEn.POS_IN) && (pobj = arg.getFirstDependentByLabel(DEPLibEn.DEP_POBJ)) != null)
			return pobj;
		
		return null;
	}
	
	private void postLabelReferent(DEPTree tree, List<List<DEPArc>> argLists)
	{
		DEPNode pred = tree.get(0);
		List<DEPArc> args;
		PBRoleset roleset;
		String label, n;
		DEPNode node;
		int i, size;
		DEPArc arg;
		
		while ((pred = tree.getNextPredicate(pred.id)) != null)
		{
			args = argLists.get(pred.id);
			size = args.size();
			
			for (i=0; i<size; i++)
			{
				arg   = args.get(i);
				label = arg.getLabel();
				
				if (SRLLib.P_ARG_REF.matcher(label).find())
				{
					if ((n = PBLib.getNumber(label)) != null && !containsNumberedArugment(args, n, 0, i) && containsNumberedArugment(args, n, i+1, size))
					{
						roleset = m_frames.getRoleset(getPBType(pred), pred.lemma, pred.getFeat(DEPLibEn.FEAT_PB));
						n = Integer.toString(Integer.parseInt(n)+1);
						
						if (roleset.isValidArgumentNumber(n) && !containsNumberedArugment(args, n, i+1, size))
						{
							node = arg.getNode();
							
							if (node.getSHead(pred) != null)
								node.getSHead(pred).setLabel(SRLLib.PREFIX_REFERENT+"A"+n);
						}
					}
					
					break;
				}
			}
		}
	}
	
	private boolean containsNumberedArugment(List<DEPArc> arguments, String n, int bIdx, int eIdx)
	{
		DEPArc arg;
		int i;
		
		for (i=bIdx; i<eIdx; i++)
		{
			arg = arguments.get(i);
			
			if (n.equals(PBLib.getNumber(arg.getLabel())))
				return true;
		}
		
		return false;
	}
}
