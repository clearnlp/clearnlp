/**
* Copyright 2013 IPSoft Inc.
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
import com.clearnlp.reader.DEPReader;
import com.clearnlp.util.UTInput;

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
				
				if (node.isPos(CTLibEn.POS_TO) || node.isPos(CTLibEn.POS_MD) || node.pos.startsWith("W"))
					return false;	
			}
			
			if (node.id < verb.id && (dep.isLabel(P_SBJ) || dep.isLabel(DEPLibEn.DEP_EXPL)))
				return false;
		}
		
		return true;
	}
	
	static public void main(String[] args)
	{
		DEPReader fin = new DEPReader(0, 1, 2, 3, 4, 5, 6);
		DEPTree tree = new DEPTree();
		
		fin.open(UTInput.createBufferedFileReader(args[0]));
		tree = fin.next();
		
		CSenTypeClassifierEN sen = new CSenTypeClassifierEN();
		tree.setDependents();
		System.out.println(sen.isInterrogative(tree.get(1)));
	}
}
