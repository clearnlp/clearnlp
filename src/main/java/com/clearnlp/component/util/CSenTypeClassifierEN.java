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
package com.clearnlp.component.util;

import java.util.List;
import java.util.regex.Pattern;

import com.clearnlp.component.AbstractComponent;
import com.clearnlp.constituent.CTLibEn;
import com.clearnlp.dependency.DEPArc;
import com.clearnlp.dependency.DEPLib;
import com.clearnlp.dependency.DEPLibEn;
import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dependency.DEPTree;
import com.clearnlp.morphology.MPLibEn;

/**
 * @since 1.4.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class CSenTypeClassifierEN extends AbstractComponent
{
	private Pattern P_QM  = Pattern.compile("^\\?+$");
	private Pattern P_SBJ = Pattern.compile("^[nc]subj.*");
	private Pattern P_AUX = Pattern.compile("^aux.*");
	
	@Override
	public void process(DEPTree tree)
	{
		int i, size = tree.size();
		DEPNode node;
		
		tree.setDependents();
		
		for (i=1; i<size; i++)
		{
			node = tree.get(i);
			
			if (isMainVerb(node))
			{
				if (isInterrogative(node))
					node.addFeat(DEPLib.FEAT_SNT, CTLibEn.FTAG_INT);
				else if (isImperative(node))
					node.addFeat(DEPLib.FEAT_SNT, CTLibEn.FTAG_IMP);
			}
		}
	}
	
	public  boolean isMainVerb(DEPNode verb)
	{
		if (!MPLibEn.isVerb(verb.pos) ||
			verb.isLabel(DEPLibEn.DEP_AUX) || verb.isLabel(DEPLibEn.DEP_AUXPASS) ||
			verb.isLabel(DEPLibEn.DEP_XCOMP) || verb.isLabel(DEPLibEn.DEP_PARTMOD) || verb.isLabel(DEPLibEn.DEP_RCMOD) ||
			verb.isLabel(DEPLibEn.DEP_CONJ) || verb.isLabel(DEPLibEn.DEP_HMOD))
			return false;
		else
			return true;
	}
	
	public boolean isInterrogative(DEPNode verb)
	{
		List<DEPArc> deps = verb.getDependents();
		int i, size = deps.size();
		boolean hasAux = false;
		DEPNode node;
		DEPArc  curr;
		String  label;
		
		for (i=size-1; i>=0; i--)
		{
			curr = deps.get(i);
			node = curr.getNode();
			
			if (node.id < verb.id || !curr.isLabel(DEPLibEn.DEP_PUNCT))
				break;
			else if (P_QM.matcher(node.lemma).find())
				return true;
		}
		
		for (i=0; i<size; i++)
		{
			curr  = deps.get(i);
			node  = curr.getNode();
			label = curr.getLabel();
			
			if (node.id < verb.id)
			{
				if (curr.isLabel(DEPLibEn.DEP_PRECONJ))
					return false;
				else if (P_AUX.matcher(label).find())
					hasAux = true;
				else if (node.pos.startsWith("W"))
					return true;
				else if (P_SBJ.matcher(label).find())
					return hasAux;
			}
			else
				break;
		}
		
		if (verb.isLemma("be") || verb.isLemma("do") || verb.isLemma("have"))
		{
			for (; i<size; i++)
			{
				curr  = deps.get(i);
				label = curr.getLabel();
				
				if (P_SBJ.matcher(label).find())
					return true;
			}			
		}
		
		return false;
	}
	
	public boolean isImperative(DEPNode verb)
	{
		if (!verb.isPos(CTLibEn.POS_VB) && !verb.isPos(CTLibEn.POS_VBP))
			return false;
		
		if (verb.isLemma("let") || verb.isLemma("thank") || verb.isLemma("welcome"))
			return false;

		List<DEPArc> deps = verb.getDependents();
		int i, size = deps.size();
		DEPNode node;
		DEPArc  dep;
		
		for (i=0; i<size; i++)
		{
			dep  = deps.get(i);
			node = dep.getNode();
			
			if (node.id < verb.id)
			{
				if (dep.isLabel(DEPLibEn.DEP_COMPLM) || dep.isLabel(DEPLibEn.DEP_MARK))
					return false;
				
				if (dep.isLabel(P_AUX) && !node.isLemma("do"))
					return false;
				
				if ((node.isPos(CTLibEn.POS_TO) && node.containsDependent(DEPLibEn.DEP_POBJ)) || node.isPos(CTLibEn.POS_MD) || node.pos.startsWith("W"))
					return false;	
			}
			
			if (node.id < verb.id && (dep.isLabel(P_SBJ) || dep.isLabel(DEPLibEn.DEP_EXPL)))
				return false;
		}
		
		return true;
	}
	
//	static public void main(String[] args)
//	{
//		DEPReader fin = new DEPReader(0, 1, 2, 3, 4, 5, 6);
//		DEPTree tree = new DEPTree();
//		DEPNode verb;
//		
//		fin.open(UTInput.createBufferedFileReader(args[0]));
//		tree = fin.next();
//		
//		CSenTypeClassifierEN sen = new CSenTypeClassifierEN();
//		tree.setDependents();
//		verb = tree.get(9);
//		System.out.println(sen.isImperative(verb));
//	}
}
