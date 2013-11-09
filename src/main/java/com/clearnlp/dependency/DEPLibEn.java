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
package com.clearnlp.dependency;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.regex.Pattern;

import com.clearnlp.constituent.CTLibEn;
import com.clearnlp.dependency.srl.SRLArc;
import com.clearnlp.dependency.srl.SRLLib;
import com.clearnlp.morphology.MPLibEn;
import com.clearnlp.util.pair.Pair;

/**
 * Dependency library for English.
 * @since 1.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class DEPLibEn extends DEPLib
{
	/** The dependency label for passive. */
	static public final String DEP_PASS	= "pass";
	/** The dependency label for subjects. */
	static public final String DEP_SUBJ	= "subj";
	
	/** The dependency label for adjectival complements. */
	static public final String DEP_ACOMP		= "acomp";
	/** The dependency label for adverbial clause modifiers. */
	static public final String DEP_ADVCL		= "advcl";
	/** The dependency label for adverbial modifiers. */
	static public final String DEP_ADVMOD		= "advmod";
	/** The dependency label for agents. */
	static public final String DEP_AGENT		= "agent";
	/** The dependency label for adjectival modifiers. */
	static public final String DEP_AMOD			= "amod";
	/** The dependency label for appositional modifiers. */
	static public final String DEP_APPOS		= "appos";
	/** The dependency label for attributes. */
	static public final String DEP_ATTR			= "attr";
	/** The dependency label for auxiliary verbs. */
	static public final String DEP_AUX			= "aux";
	/** The dependency label for passive auxiliary verbs. */
	static public final String DEP_AUXPASS		= DEP_AUX+DEP_PASS;
	/** The dependency label for coordinating conjunctions. */
	static public final String DEP_CC			= "cc";
	/** The dependency label for clausal complements. */
	static public final String DEP_CCOMP		= "ccomp";
	/** The dependency label for complementizers. */
	static public final String DEP_COMPLM		= "complm";
	/** The dependency label for conjuncts. */
	static public final String DEP_CONJ			= "conj";
	/** The dependency label for clausal subjects. */
	static public final String DEP_CSUBJ		= "c"+DEP_SUBJ;
	/** The dependency label for clausal passive subjects. */
	static public final String DEP_CSUBJPASS	= DEP_CSUBJ+DEP_PASS;
	/** The dependency label for unknown dependencies. */
	static public final String DEP_DEP  		= "dep";
	/** The dependency label for determiners. */
	static public final String DEP_DET			= "det";
	/** The dependency label for direct objects. */
	static public final String DEP_DOBJ 		= "dobj";
	/** The dependency label for expletives. */
	static public final String DEP_EXPL 		= "expl";
	/** The dependency label for modifiers in hyphenation. */
	static public final String DEP_HMOD 		= "hmod";
	/** The dependency label for hyphenation. */
	static public final String DEP_HYPH 		= "hyph";
	/** The dependency label for indirect objects. */
	static public final String DEP_IOBJ 		= "iobj";
	/** The dependency label for interjections. */
	static public final String DEP_INTJ			= "intj";
	/** The dependency label for markers. */
	static public final String DEP_MARK			= "mark";
	/** The dependency label for meta modifiers. */
	static public final String DEP_META			= "meta";
	/** The dependency label for negation modifiers. */
	static public final String DEP_NEG			= "neg";
	/** The dependency label for non-finite modifiers. */
	static public final String DEP_NFMOD		= "nfmod";
	/** The dependency label for infinitival modifiers. */
	static public final String DEP_INFMOD		= "infmod";
	/** The dependency label for noun phrase modifiers. */
	static public final String DEP_NMOD 		= "nmod";
	/** The dependency label for noun compound modifiers. */
	static public final String DEP_NN			= "nn";
	/** The dependency label for noun phrase as adverbial modifiers. */
	static public final String DEP_NPADVMOD		= "npadvmod";
	/** The dependency label for nominal subjects. */
	static public final String DEP_NSUBJ		= "n"+DEP_SUBJ;
	/** The dependency label for nominal passive subjects. */
	static public final String DEP_NSUBJPASS	= DEP_NSUBJ+DEP_PASS;
	/** The dependency label for numeric modifiers. */
	static public final String DEP_NUM			= "num";
	/** The dependency label for elements of compound numbers. */
	static public final String DEP_NUMBER		= "number";
	/** The dependency label for object predicates. */
	static public final String DEP_OPRD			= "oprd";
	/** The dependency label for parataxis. */
	static public final String DEP_PARATAXIS 	= "parataxis";
	/** The dependency label for participial modifiers. */
	static public final String DEP_PARTMOD		= "partmod";
	/** The dependency label for modifiers of prepositions. */
	static public final String DEP_PMOD 		= "pmod";
	/** The dependency label for prepositional complements. */
	static public final String DEP_PCOMP 		= "pcomp";
	/** The dependency label for objects of prepositions. */
	static public final String DEP_POBJ 		= "pobj";
	/** The dependency label for possession modifiers. */
	static public final String DEP_POSS			= "poss";
	/** The dependency label for possessive modifiers. */
	static public final String DEP_POSSESSIVE 	= "possessive";
	/** The dependency label for pre-conjuncts. */
	static public final String DEP_PRECONJ		= "preconj";
	/** The dependency label for pre-determiners. */
	static public final String DEP_PREDET		= "predet";
	/** The dependency label for prepositional modifiers. */
	static public final String DEP_PREP			= "prep";
	/** The dependency label for particles. */
	static public final String DEP_PRT 			= "prt";
	/** The dependency label for punctuation. */
	static public final String DEP_PUNCT		= "punct";
	/** The dependency label for modifiers of quantifiers. */
	static public final String DEP_QMOD			= "qmod";
	/** The dependency label for quantifier phrase modifiers. */
	static public final String DEP_QUANTMOD		= "quantmod";
	/** The dependency label for relative clause modifiers. */
	static public final String DEP_RCMOD		= "rcmod";
	/** The dependency label for roots. */
	static public final String DEP_ROOT 		= "root";
	/** The dependency label for open clausal modifiers. */
	static public final String DEP_XCOMP		= "xcomp";
	/** The dependency label for open clausal subjects. */
	static public final String DEP_XSUBJ		= "x"+DEP_SUBJ;
	/** The secondary dependency label for gapping relations. */
	static public final String DEP_GAP			= "gap";
	/** The secondary dependency label for referents. */
	static public final String DEP_REF			= "ref";
	/** The secondary dependency label for right node raising. */
	static public final String DEP_RNR			= "rnr";
	
	static public Pattern P_SBJ = Pattern.compile("^[nc]subj");
	static public Pattern P_OBJ = Pattern.compile("^[di]obj");
	static public Pattern P_AUX = Pattern.compile("^aux");
	static public Pattern P_NSUBJ = Pattern.compile("^nsubj");
	static public Pattern P_PUNCT = Pattern.compile("^(hyph|punct)$");
	
/*	static public final String CONLL_ADV	= "ADV";
	static public final String CONLL_AMOD	= "AMOD";
	static public final String CONLL_APPO	= "APPO";
	static public final String CONLL_COORD	= "COORD";
	static public final String CONLL_CONJ	= "CONJ";
	static public final String CONLL_DEP	= "DEP";
	static public final String CONLL_DTV	= "DTV";
	static public final String CONLL_EXTR	= "EXTR";
	static public final String CONLL_IM		= "IM";
	static public final String CONLL_INTJ	= "INTJ";
	static public final String CONLL_LGS	= "LGS";
	static public final String CONLL_LOC	= "LOC";
	static public final String CONLL_META	= "META";
	static public final String CONLL_NMOD	= "NMOD";
	static public final String CONLL_OBJ	= "OBJ";
	static public final String CONLL_OPRD	= "OPRD";
	static public final String CONLL_P		= "P";
	static public final String CONLL_PMOD	= "PMOD";
	static public final String CONLL_PRD	= "PRD";
	static public final String CONLL_PRN	= "PRN";
	static public final String CONLL_PRT	= "PRT";
	static public final String CONLL_PUT	= "PUT";
	static public final String CONLL_QMOD	= "QMOD";
	static public final String CONLL_ROOT	= "ROOT";
	static public final String CONLL_SBJ	= "SBJ";
	static public final String CONLL_SUB	= "SUB";
	static public final String CONLL_VC		= "VC";
	static public final String CONLL_XCOMP	= "XCOMP";*/
	
	static public boolean isSubject(String label)
	{
		return P_SBJ.matcher(label).find();
	}
	
	static public boolean isObject(String label)
	{
		return P_OBJ.matcher(label).find();
	}
	
	static public boolean isAuxiliary(String label)
	{
		return P_AUX.matcher(label).find();
	}

	static public Deque<DEPNode> getPreviousConjuncts(DEPNode node)
	{
		Deque<DEPNode> deque = new ArrayDeque<DEPNode>();
		
		while (node.isLabel(DEPLibEn.DEP_CONJ))
		{
			node = node.getHead();
			deque.add(node);
		}
		
		return deque;
	}
	
	static public Deque<DEPNode> getNextConjuncts(DEPNode node)
	{
		Deque<DEPNode> deque = new ArrayDeque<DEPNode>();
		
		getNextConjunctsAux(node, deque);
		return deque;
	}
	
	static private void getNextConjunctsAux(DEPNode node, Deque<DEPNode> deque)
	{
		for (DEPNode dep : node.getDependentsByLabels(DEPLibEn.DEP_CONJ))
		{
			deque.add(dep);
			getNextConjunctsAux(dep, deque);
		}
	}
	
	static public boolean isSmallClause(DEPNode verb)
	{
		DEPNode sbj = verb.getFirstDependentByLabel(P_SBJ);
		
		if (sbj != null)
			return isSmallClauseAux(verb);
		
		return false;
	}
	
	static public boolean isSmallClauseAux(DEPNode verb)
	{
		DEPNode aux = verb.getFirstDependentByLabel(DEP_AUX);
		
		if (aux == null)	return verb.isPos(CTLibEn.POS_VBG);
		else				return aux.isPos(CTLibEn.POS_TO);
	}
	
	static public List<List<DEPArc>> postLabel(DEPTree tree)
	{
		List<List<DEPArc>> argLists;
		int i, size = tree.size();
		List<DEPArc> argList;
		DEPNode node;
		
		tree.setDependents();
		argLists = tree.getArgumentList();
		
		for (i=1; i<size; i++)
		{
			node = tree.get(i);
			argList = argLists.get(i);
			
			if (node.isLabel(DEP_PREP))
			{
				relinkPreposition(node);
			}
			else if (MPLibEn.isVerb(node.pos))
			{
				labelReferentOfRelativeClause(node, argList);
				
				if (!relinkReferent(node))
					relabelPrepositionWithReferent(node);
			}
		}
		
		tree.resetDependents();
		return argLists;
	}
	
	/**
	 * Called by {@link DEPLibEn#postLabel(DEPTree)}.
	 * @param verb a dependency node whose dependency relation to its head is {@link DEPLibEn#DEP_PREP}.
	 */
	static private void relinkPreposition(DEPNode prep)
	{
		DEPNode head = prep.getHead();
		
		if (MPLibEn.isNoun(head.pos) || head.isPos(CTLibEn.POS_IN) || head.isPos(CTLibEn.POS_RP))
		{
			DEPNode gHead = head.getHead();
			SRLArc  sp, sh;
			
			if (gHead != null && (sp = prep.getSHead(gHead)) != null)
			{
				if ((sh = head.getSHead(gHead)) != null)
				{
					if (head.isPos(CTLibEn.POS_IN) && sh.isLabel(SRLLib.C_V))
					{
						head.pos = CTLibEn.POS_RP;
						head.setLabel(DEP_PRT);
					}
					
					prep.setHead(gHead);
				}
				else
				{
					prep.removeSHead(sp);
					head.addSHead(gHead, sp.getLabel());
				}
			}
			
//			if (gHead != null && (sp = prep.getSHead(gHead)) != null && (sh = head.getSHead(gHead)) != null)
//				prep.setHead(gHead);
		}
	}
	
	/** Called by {@link DEPLibEn#postLabel(DEPTree)}. */
	static private void labelReferentOfRelativeClause(DEPNode verb, List<DEPArc> argList)
	{
		DEPNode top  = getTopVerbChain(verb);
		DEPNode head = top.getHead();
		
		if (top.isLabel(DEPLibEn.DEP_RCMOD) && !head.containsSHead(verb))
		{
			for (DEPArc arc : argList)
			{
				if (arc.isLabel(SRLLib.P_ARG_REF) && isReferentArgument(arc.getNode()))
				{
					head.addSHead(verb, SRLLib.getBaseLabel(arc.getLabel()));
					return;
				}
			}
		}
	}
	
	/** Called by {@link DEPLibEn#labelReferentOfRelativeClause(DEPNode, List)}. */
	static private DEPNode getTopVerbChain(DEPNode verb)
	{
		while (MPLibEn.isVerb(verb.getHead().pos) && (verb.isLabel(DEP_CONJ) || verb.isLabel(DEP_XCOMP)))
			verb = verb.getHead();
			
		return verb;
	}
	
	/** Called by {@link DEPLibEn#labelReferentOfRelativeClause(DEPNode, List)}. */
	static boolean isReferentArgument(DEPNode arg)
	{
		return arg.getFirstDependentByLabel(DEP_POBJ) != null || arg.isLemma("that") || arg.isLemma("which");
	}
	
	static public boolean containsRelativizer(DEPNode arg)
	{
		return containsRelativizerAux(arg, arg);
	}
	
	static private boolean containsRelativizerAux(DEPNode arg, DEPNode head)
	{
		DEPNode dep;
		
		for (DEPArc arc : arg.getDependents())
		{
			dep = arc.getNode();
			
			if (dep.pos.startsWith("W") || ((!MPLibEn.isVerb(dep.pos) || (arg == head && dep.isLabel(DEP_XCOMP))) && containsRelativizerAux(dep, head)))
				return true;
		}
		
		return false;
	}
	
	/** Called by {@link DEPLibEn#postLabel(DEPTree)}. */
	static private boolean relinkReferent(DEPNode verb)
	{
		Pair<DEPNode,SRLArc> c = getFirstRelativizer(verb);
		if (c == null)	return false;
		Pair<DEPNode,SRLArc> p = getLastPrepositionWithoutDependent(verb);
		if (p == null)	return false;
		
		DEPNode comp = c.o1;
		
		comp.setHead(p.o1, DEP_POBJ);
		comp.removeSHead(c.o2);
		
		if (p.o2 != null)	SRLLib.toReferentArgument(p.o2);
		else				p.o1.addSHead(c.o2);
		
		return true;
	}
	
	/** Called by {@link DEPLibEn#relinkReferent(DEPNode)}. */
	static private Pair<DEPNode,SRLArc> getFirstRelativizer(DEPNode verb)
	{
		SRLArc  sHead;
		DEPNode dep;
		
		for (DEPArc arc : verb.getDependents())
		{
			dep = arc.getNode();
			
			if (dep.id > verb.id)
				return null;
			
			if ((sHead = dep.getSHead(verb, SRLLib.P_ARG_REF)) != null)
				return (dep.isPos(CTLibEn.POS_IN)) ? null : new Pair<DEPNode,SRLArc>(dep, sHead);
		}
		
		return null;
	}
	
	/** Called by {@link DEPLibEn#relinkReferent(DEPNode)}. */
	static private Pair<DEPNode,SRLArc> getLastPrepositionWithoutDependent(DEPNode verb)
	{
		List<DEPArc> arcs = verb.getDependents();
		DEPNode dep;
		DEPArc arc;
		SRLArc sHead;
		int i;
		
		for (i=arcs.size()-1; i>=0; i--)
		{
			arc = arcs.get(i);
			dep = arc.getNode();
			
			if (dep.id < verb.id)
				return null;
			
			if (dep.isPos(CTLibEn.POS_IN) && (sHead = dep.getSHead(verb)) != null)
				return dep.getDependents().isEmpty() ? new Pair<DEPNode,SRLArc>(dep, sHead) : null;
			
//			if (dep.isPos(CTLibEn.POS_IN))
//			{
//				if (!dep.getDependents().isEmpty())
//					return null;
//				
//				return new Pair<DEPNode,SRLArc>(dep, dep.getSHead(verb));
//			}
		}
		
		return null;
	}
	
	/** Called by {@link DEPLibEn#postLabel(DEPTree)}. */
	static private void relabelPrepositionWithReferent(DEPNode verb)
	{
		DEPNode dep, pobj;
		SRLArc sHead;
		
		for (DEPArc arc : verb.getDependents())
		{
			dep = arc.getNode();
			
			if (dep.isPos(CTLibEn.POS_IN) && (sHead = dep.getSHead(verb)) != null && !sHead.isLabel(SRLLib.P_ARG_REF))
			{
				pobj = dep.getFirstDependentByLabel(DEPLibEn.DEP_POBJ);
				
				if (pobj != null && getRefDependentNode(pobj) != null)
				{
					SRLLib.toReferentArgument(sHead);
					break;
				}
			}
		}
	}
	
	/** @return a relativizer node in the subtree of the specific node; otherwise, {@code null}. */
	static public DEPNode getRefDependentNode(DEPNode head)
	{
		if (head.isLabel(DEP_RCMOD) || head.isLabel(DEP_CCOMP) || (head.isLabel(DEP_XCOMP) && !isNonProjectiveXcomp(head)) || head.isLabel(DEP_DEP) || head.isLabel(DEP_CONJ))
			return null;
		
		if (isCommonRelativizer(head))
			return head;
		
		DEPNode dep, ref;
		
		for (DEPArc arc : head.getDependents())
		{
			dep = arc.getNode();
			ref = getRefDependentNode(dep);
			if (ref != null)	return ref;
		}
		
		return null;
	}
	
	static private boolean isNonProjectiveXcomp(DEPNode node)
	{
		DEPNode head = node.getHead();
		if (head == null || head.id > node.id) return false;
		
		List<DEPNode> deps = node.getDependentNodeList();
		if (!deps.isEmpty() && deps.get(0).id < head.id) return true;
		
		return false;
	}
	
	static public boolean isCommonRelativizer(DEPNode node)
	{
		return node.pos.startsWith("W") && MPLibEn.RE_WH_COMMON.matcher(node.lemma).find();
	}
}
