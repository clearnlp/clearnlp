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
package com.clearnlp.dependency.srl;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.regex.Pattern;

import com.clearnlp.constituent.CTLibEn;
import com.clearnlp.dependency.DEPArc;
import com.clearnlp.dependency.DEPLibEn;
import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dependency.DEPTree;
import com.clearnlp.generation.LGLibEn;
import com.clearnlp.propbank.PBLib;

public class SRLLib
{
	static public final String DELIM_PATH_UP	= "^";
	static public final String DELIM_PATH_DOWN	= "|";
	static public final String DELIM_SUBCAT		= "_";
	
	static public final String PREFIX_CONCATENATION = "C-";
	static public final String PREFIX_REFERENT = "R-";
	static public final String ARGM_MOD = "AM-MOD";
	static public final String ARGM_NEG = "AM-NEG";
	static public final String ARGM_LOC = "AM-LOC";
	static public final String ARGM_DIR = "AM-DIR";
	static public final String ARGM_DIS = "AM-DIS";
	static public final String ARGM_GOL = "AM-GOL";
	static public final String ARGM_ADV = "AM-ADV";
	static public final String ARGM_TMP = "AM-TMP";
	static public final String ARGM_MNR = "AM-MNR";
	static public final String ARGM_PRR	= "AM-PRR";
	static public final String ARG0 	= "A0";
	static public final String ARG1 	= "A1";
	static public final String ARG2 	= "A2";
	static public final String C_V	 	= "C-V";
	
	static public final Pattern P_ARG_CONCATENATION = Pattern.compile("^"+PREFIX_CONCATENATION+".+$");
	static public final Pattern P_ARG_REF = Pattern.compile("^"+PREFIX_REFERENT+".+$");
	
	static public String getBaseLabel(String label)
	{
		if (label.startsWith(SRLLib.PREFIX_CONCATENATION))
			return label.substring(SRLLib.PREFIX_CONCATENATION.length());
		else if (label.startsWith(SRLLib.PREFIX_REFERENT))
			return label.substring(SRLLib.PREFIX_REFERENT.length());
		else
			return label;
	}
	
	static public List<List<DEPArc>> getArgumentList(DEPTree tree)
	{
		int i, size = tree.size();
		List<DEPArc> args;
		DEPNode node;
		
		List<List<DEPArc>> list = new ArrayList<List<DEPArc>>();
		for (i=0; i<size; i++)	list.add(new ArrayList<DEPArc>());
		
		for (i=1; i<size; i++)
		{
			node = tree.get(i);
			
			for (DEPArc arc : node.getSHeads())
			{
				args = list.get(arc.getNode().id);
				args.add(new DEPArc(node, arc.getLabel()));
			}
		}
		
		return list;
	}

	static public void relinkRelativeClause(SRLTree sTree)
	{
		DEPNode pred = sTree.getPredicate();
		DEPArc ref = null;
		DEPNode dep, arg;
		
		for (DEPArc arc : pred.getDependents())
		{
			dep = arc.getNode();
			
			if (dep.containsSHead(pred, P_ARG_REF))
			{
				ref = arc;
				break;
			}
		}
		
		for (SRLArc sArc : sTree.getArguments())
		{
			arg = sArc.getNode();
			
			for (DEPArc dArc : arg.getDependents())
			{
				dep = dArc.getNode();
				
				if (dep == pred || pred.isDescendentOf(dep))
				{
					arg.removeDependent(dArc);
					dep.setHead(arg.getHead(), arg.getLabel());
					
					if (ref != null) // && ref.isLabel(SRLLib.S_PREFIX_REFERENT+arg.getLabel())
					{
						DEPNode rDep = ref.getNode();
						DEPArc rHead = rDep.getSHead(pred);
						DEPArc whose = rDep.getAnyDescendentArcByPOS(CTLibEn.POS_WPS);
						
						if (whose != null)
						{
							DEPNode tmp = whose.getNode();
							arg.setHead(tmp.getHead(), tmp.getLabel());
							arg.id = tmp.id;
							whose.setNode(arg);
							rHead.setLabel(sArc.getLabel());
							arg.removeSHead(pred);
							
							tmp = arg.getLastNode();
							tmp.form = LGLibEn.getPossessiveForm(tmp.form);
						}
						else if (ref.isLabel(DEPLibEn.DEP_PREP))
						{
							DEPArc tmp = new DEPArc(arg, DEPLibEn.DEP_POBJ);
							
							arg.setHead(rDep, tmp.getLabel());
							arg.id = rDep.id + 1;
							rDep.clearDependents();
							rDep.addDependent(tmp);
							rHead.setLabel(sArc.getLabel());
							arg.removeSHead(pred);
						}
						else if (ref.isLabel(DEPLibEn.P_SBJ))
						{
							arg.setHead(pred, ref.getLabel());
							arg.id = rDep.id;
							ref.setNode(arg);
							rHead.setLabel(sArc.getLabel());
						}
						else
						{
							DEPArc tmp = new DEPArc(arg, ref.getLabel());
							arg.setHead(pred, tmp.getLabel());
							arg.id = pred.id + 1;
							
							if (ref.isLabel(DEPLibEn.P_OBJ) || ref.isLabel(DEPLibEn.DEP_ATTR))
								pred.addDependentRightNextToSelf(tmp);
							else
								pred.addDependent(tmp);
							
							pred.removeDependent(ref);
						}
					}
					else
					{
						DEPArc tmp = new DEPArc(arg, DEPLibEn.DEP_DEP);
						arg.setHead(pred, tmp.getLabel());
						arg.id = pred.id + 1;
						
						if (sArc.isLabel(PBLib.P_ARGN))
							pred.addDependentRightNextToSelf(tmp);
						else
							pred.addDependent(tmp);
					}
					
					break;
				}
			}
		}
	}

	static public void relinkCoordination(SRLTree sTree)
	{
		DEPNode pred = sTree.getPredicate();
		boolean noSbj = pred.getDependentsByLabels(DEPLibEn.P_SBJ).isEmpty();
		Deque<DEPNode> conjuncts;
		List<DEPArc> arcs;
		DEPNode conj, dep;
		DEPArc sArc;
	
		conjuncts = DEPLibEn.getPreviousConjuncts(pred);
		
		if (!conjuncts.isEmpty())
		{
			conj = conjuncts.getLast();
			arcs = conj.getDependents();
			
			int i; for (i=arcs.size()-1; i>=0; i--)
			{
				dep = arcs.get(i).getNode();
				sArc = dep.getSHead(pred);
				
				if (noSbj && (dep.isLabel(DEPLibEn.P_SBJ) || dep.isLabel(DEPLibEn.P_AUX)) || (sArc != null && sArc.isLabel(ARGM_NEG)))
				{
					dep.setHead(pred);
					pred.addDependentFront(new DEPArc(dep, dep.getLabel()));
				}
				else if (dep.containsSHead(pred))
				{
					dep.setHead(pred);
					dep.id = pred.id+1;
					pred.addDependent(new DEPArc(dep, dep.getLabel()));
				}
			}
		}
		
		conjuncts = DEPLibEn.getNextConjuncts(pred);
		
		if (!conjuncts.isEmpty())
		{
			conj = conjuncts.getLast();
			
			for (DEPArc arc : conj.getDependents())
			{
				dep = arc.getNode();
				
				if (dep.containsSHead(pred))
				{
					dep.setHead(pred);
					pred.addDependent(new DEPArc(dep, dep.getLabel()));
				}
			}
		}
	}

	static public void toReferentArgument(DEPArc arc)
	{
		String label = arc.getLabel();
		
		if (label.startsWith("A"))
			arc.setLabel(PREFIX_REFERENT + label);
		else if (label.startsWith(PREFIX_CONCATENATION))
			arc.setLabel(PREFIX_REFERENT + label.substring(PREFIX_CONCATENATION.length()));
	}
	
	static public boolean containsNegation(SRLTree tree)
	{
		DEPNode pred = tree.getPredicate();
		
		for (DEPArc arc : pred.getDependents())
		{
			if (arc.isLabel(DEPLibEn.DEP_NEG))
				return true;
		}
		
		for (SRLArc arc : tree.getArguments())
		{
			if (arc.isLabel(SRLLib.ARGM_NEG))
				return true;
		}
		
		return false;
	}
}
