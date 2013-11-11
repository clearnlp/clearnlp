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
package com.clearnlp.generation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipFile;

import com.clearnlp.constant.english.ENAux;
import com.clearnlp.constant.english.ENModal;
import com.clearnlp.constant.english.ENPronoun;
import com.clearnlp.constant.universal.UNConstant;
import com.clearnlp.constant.universal.UNPunct;
import com.clearnlp.constituent.CTLibEn;
import com.clearnlp.dependency.DEPArc;
import com.clearnlp.dependency.DEPFeat;
import com.clearnlp.dependency.DEPLib;
import com.clearnlp.dependency.DEPLibEn;
import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dependency.DEPTree;
import com.clearnlp.dependency.srl.SRLLib;
import com.clearnlp.morphology.MPLibEn;
import com.google.common.collect.Lists;

/**
 * Used for Eliza.
 * @since 1.4.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class LGAsk
{
	private final String USER    		= "user";
	private final String PLEASE    		= "please";
	private final String NON_FINITE		= "non-finite";
	private final String WH_NON_FINITE	= "wh-non-finite";
	
	private LGVerbEn g_verb;
	
	public LGAsk()
	{
		g_verb = new LGVerbEn();
	}
	
	public LGAsk(ZipFile file)
	{
		g_verb = new LGVerbEn(file);
	}
	
	/** Generates a declarative sentence with "ask" from a question. */
	public DEPTree generateAskFromQuestion(DEPTree tree)
	{
		tree = tree.clone();
		tree.setDependents();
		
		DEPNode root = tree.getFirstRoot();
		return (root == null) ? null : generateAskFromQuestionAux(tree, root);
	}
	
	/** {@link LGAsk#generateAskFromQuestion(DEPTree, String)}. */
	private DEPTree generateAskFromQuestionAux(DEPTree tree, DEPNode verb)
	{
		LGLibEn.convertFirstFormToLowerCase(tree);
		DEPNode ref = getReferentArgument(verb);
		
		if (ref == null || !ref.isLabel(DEPLibEn.P_SBJ))
			relocateAuxiliary(tree, verb);
		
		addPrefix(tree, verb, ref);
		convertYouToUser(tree, verb);
		addPeriod(tree, verb);
		
		tree.resetIDs();
		tree.resetDependents();
		normalizeBe(tree);
		
		return tree;
	}
	
	private void normalizeBe(DEPTree tree)
	{
		int i, size = tree.size();
		DEPNode node;
		
		for (i=1; i<size; i++)
		{
			node = tree.get(i);
			if (node.isLemma("be"))
			{
				     if (node.isForm("'s"))		node.form = "is";
				else if (node.isForm("'re"))	node.form = "are";
			}
		}
	}
	
	/** {@link LGAsk#generateAskFromQuestion(DEPTree, String)}. */
	private DEPNode getReferentArgument(DEPNode verb)
	{
		DEPNode dep;
		
		for (DEPArc arc : verb.getDependents())
		{
			dep = arc.getNode();
			
			if (dep.containsSHead(verb, SRLLib.P_ARG_REF))
				return dep;
		}
		
		return null;
	}
	
	/** {@link LGAsk#generateAskFromQuestion(DEPTree, String)}. */
	private void relocateAuxiliary(DEPTree tree, DEPNode verb)
	{
		List<DEPNode> auxes = new ArrayList<DEPNode>();
		DEPNode sbj = null;

		for (DEPArc arc : verb.getDependents())
		{
			if (arc.isLabel(DEPLibEn.P_AUX))
				auxes.add(arc.getNode());
			else if (arc.isLabel(DEPLibEn.P_SBJ))
				sbj = arc.getNode();
		}
		
		if (sbj != null)
		{
			if (!auxes.isEmpty() && auxes.get(0).id < sbj.id)
			{
				relocateAuxiliaryAux(tree, verb, auxes, sbj);
			}
			else if (verb.isLemma(ENAux.BE) && verb.id < sbj.id)
			{
				tree.remove(verb);
				tree.add(sbj.getLastNode().id, verb);
				setBeVerbForm(verb, sbj);
			}
		}
	}
	
	/** {@link LGAsk#generateAskFromQuestion(DEPTree, String)}. */
	private void relocateAuxiliaryAux(DEPTree tree, DEPNode verb, List<DEPNode> auxes, DEPNode sbj)
	{
		DEPNode aux = auxes.get(0);
		tree.remove(aux);
		
		if (aux.isLemma(ENAux.DO))
		{
			if (auxes.size() > 1)
			{
				DEPNode node = auxes.get(1);
				
				if (MPLibEn.isVerb(node.pos))
					verb = node;
			}
			
			verb.pos = aux.pos;
			
			if (aux.isPos(CTLibEn.POS_VBD))
				verb.form = g_verb.getPastForm(verb.lemma);
			else if (aux.isPos(CTLibEn.POS_VBZ))
				verb.form = LGVerbEn.get3rdSingularForm(verb.lemma);
			else if (aux.isPos(CTLibEn.POS_VBP) && sbj.isLemma(ENPronoun.YOU))
			{
				verb.form = LGVerbEn.get3rdSingularForm(verb.lemma);
				verb.pos  = CTLibEn.POS_VBZ;
			}
		}
		else
		{
			tree.add(sbj.getLastNode().id, aux);
			
			if (aux.isLemma(ENAux.BE))
				setBeVerbForm(aux, sbj);
			else if (aux.isLemma(ENAux.HAVE))
				set3rdSingularVerbForm(aux, sbj);
		}
	}
	
	/** {@link LGAsk#generateAskFromQuestion(DEPTree, String)}. */
	private void setBeVerbForm(DEPNode verb, DEPNode sbj)
	{
		if (sbj.isLemma(ENPronoun.YOU))
		{
			if (verb.isPos(CTLibEn.POS_VBD))
				verb.form = ENAux.WAS;
			else if (verb.isPos(CTLibEn.POS_VBP))
			{
				verb.form = ENAux.IS;
				verb.pos  = CTLibEn.POS_VBZ;
			}
		}
	}
	
	/** {@link LGAsk#generateAskFromQuestion(DEPTree, String)}. */
	private void set3rdSingularVerbForm(DEPNode verb, DEPNode sbj)
	{
		if (sbj.isLemma(ENPronoun.YOU))
		{
			if (verb.isPos(CTLibEn.POS_VBP))
			{
				verb.form = LGVerbEn.get3rdSingularForm(verb.lemma);
				verb.pos  = CTLibEn.POS_VBZ;
			}
		}
	}
	
	/** {@link LGAsk#generateAskFromQuestion(DEPTree, String)}. */
	private void addPrefix(DEPTree tree, DEPNode verb, DEPNode ref)
	{
		DEPNode ask = getNode(tree.get(0), "Ask", "ask", CTLibEn.POS_VB, DEPLibEn.DEP_ROOT, null);
		verb.setHead(ask, DEPLibEn.DEP_CCOMP);
		tree.add(1, ask);
		
		if (ref == null && !DEPLibEn.containsRelativizer(verb))
		{
			DEPNode complm = getNode(verb, "whether", "whether", CTLibEn.POS_IN, DEPLibEn.DEP_COMPLM, null);
			tree.add(2, complm);			
		}
	}
	
	/** {@link LGAsk#generateAskFromQuestion(DEPTree, String)}. */
	private void convertYouToUser(DEPTree tree, DEPNode head)
	{
		if (head.isLemma(ENPronoun.YOU) || head.isLemma(ENPronoun.YOURSELF))
		{
			head.form = head.lemma = USER;
			head.pos  = CTLibEn.POS_NN;
			matchUserVerb(head);
			tree.add(tree.indexOf(head), getNode(head, UNConstant.THE, UNConstant.THE, CTLibEn.POS_DT, DEPLibEn.DEP_DET, null));
		}
		else if (head.isLemma(ENPronoun.YOUR) || head.isLemma(ENPronoun.YOURS))
		{
			int idx = tree.indexOf(head);
			
			head.form = head.lemma = USER;
			head.pos  = CTLibEn.POS_NN;
			
			tree.add(idx  , getNode(head, UNConstant.THE, UNConstant.THE, CTLibEn.POS_DT, DEPLibEn.DEP_DET, null));
			tree.add(idx+2, getNode(head, UNConstant.APOSTROPHE_S, UNConstant.APOSTROPHE_S, CTLibEn.POS_POS, DEPLibEn.DEP_POSSESSIVE, null));
		}
		
		for (DEPArc arc : head.getDependents())
			convertYouToUser(tree, arc.getNode());
	}
	
	private void matchUserVerb(DEPNode node)
	{
		if (node.isLabel(DEPLibEn.P_SBJ))
		{
			DEPNode verb = node.getHead();
			
			if (verb != null && MPLibEn.isVerb(verb.pos))
			{
				DEPNode aux = verb.getFirstDependentByLabel(DEPLibEn.P_AUX);
				
				if (aux != null)	to3rdNumber(aux);
				else				to3rdNumber(verb);
			}
		}
	}
	
	private void to3rdNumber(DEPNode verb)
	{
		if (verb.isPosAny(CTLibEn.POS_VB, CTLibEn.POS_VBP, CTLibEn.POS_VBZ))
		{
			verb.form = LGVerbEn.get3rdSingularForm(verb.lemma);
			verb.pos  = CTLibEn.POS_VBZ;
		}
	}
	
	private void addPeriod(DEPTree tree, DEPNode root)
	{
		DEPNode last = tree.get(tree.size()-1);
		
		if (last.isPos(CTLibEn.POS_PERIOD))
			last.form = last.lemma = UNPunct.PERIOD;
		else
			tree.add(getNode(root, UNPunct.PERIOD, UNPunct.PERIOD, CTLibEn.POS_PERIOD, DEPLibEn.DEP_PUNCT, null));
	}
	
//	private boolean hasRelativizer(DEPTree tree, DEPNode verb)
//	{
//		int i, size = tree.size();
//		DEPNode node;
//		
//		for (i=1; i<size; i++)
//		{
//			node = tree.get(i);
//			
//			if (node == verb)
//				break;
//			
//			if (node.containsSHead(SRLLib.P_ARG_REF))
//				return true;
//		}
//		
//		return false;
//	}
	
	/** Generates a question from a declarative sentence with "ask". */
	public DEPTree generateQuestionFromAsk(DEPTree tree)
	{
		tree = tree.clone();
		tree.setDependents();
		
		DEPNode root = tree.getFirstRoot();
		if (root == null) return null;
		DEPNode dep, dobj = null;
		
		for (DEPArc arc : root.getDependents())
		{
			dep = arc.getNode();
			
			if (MPLibEn.isVerb(dep.pos))
				return generateQuestion(dep, getPrevDependents(tree, root, dep));
			else if (dep.isLabel(DEPLibEn.DEP_DOBJ))
				dobj = dep;
		}
		
		if (dobj != null && (dep = dobj.getFirstDependentByLabel(DEPLibEn.DEP_CCOMP)) != null)
			return generateQuestion(dep, null);
		
		return null;
	}
	
	private List<DEPNode> getPrevDependents(DEPTree tree, DEPNode root, DEPNode verb)
	{
		List<DEPNode> list = Lists.newArrayList();
		int i, size = root.id;
		DEPNode node;
		
		for (i=1; i<size; i++)
		{
			node = tree.get(i);
			
			if (node.isDependentOf(root))
			{
				node.setHead(verb);
				list.addAll(node.getSubNodeSortedList());				
			}
		}
		
		return list;
	}
	
	/** Generates a question from a declarative sentence. */
	public DEPTree generateQuestionFromDeclarative(DEPTree tree, boolean convertUnI)
	{
		tree = tree.clone();
		tree.setDependents();
		
		LGLibEn.convertFirstFormToLowerCase(tree);
		if (convertUnI)  LGLibEn.convertUnI(tree);
		
		DEPNode root = tree.getFirstRoot();
		if (root == null)	return null;
		
		return generateQuestion(root, getPrevDependents(tree, root));
	}
	
	private List<DEPNode> getPrevDependents(DEPTree tree, DEPNode verb)
	{
		List<DEPNode> list = Lists.newArrayList();
		int i, bIdx = -1;
		DEPNode node;
				
		for (i=verb.id-1; i>0; i--)
		{
			node = tree.get(i);
			
			if (DEPLibEn.getRefDependentNode(node) != null)
			{
				bIdx = node.getSubNodeSortedList().get(0).id;
				break;
			}
		}
		
		for (i=1; i<bIdx; i++)
			list.add(tree.get(i));
		
		return list;
	}
	
	/** Generates a question from a declarative sentence. */
	public DEPTree generateQuestionFromDeclarative(DEPNode root, boolean convertUnI)
	{
		DEPTree tree = new DEPTree();
		
		for (DEPNode node : root.getSubNodeSortedList())
			tree.add(node);
		
		return generateQuestionFromDeclarative(tree, convertUnI);
	}
	
	/** Called by {@link LGAsk#generateQuestionFromAsk(DEPTree, String)}. */
	public DEPTree generateQuestion(DEPNode verb, List<DEPNode> prevNodes)
	{
		Set<DEPNode> added = new HashSet<DEPNode>();
		DEPTree tree = new DEPTree();
		DEPNode rel, aux;
		
		if (prevNodes != null)
		{
			tree.addAll(prevNodes);
			added.addAll(prevNodes);
		}
		
		verb.setHead(tree.get(0), DEPLibEn.DEP_ROOT);
		rel = setRelativizer(tree, verb, added);
		
		aux = setAuxiliary(tree, verb, added, rel);
		setRest(tree, verb, added);
		resetDEPTree(tree, verb);
		if (aux != null) matchNumber(verb, aux);
		
		return tree;
	}
	
	/** Called by {@link LGAsk#generateQuestionFromAsk(DEPTree, String)}. */
	private DEPNode setRelativizer(DEPTree tree, DEPNode verb, Set<DEPNode> added)
	{
		DEPNode dep, rel, head;
		
		for (DEPArc arc : verb.getDependents())
		{
			dep = arc.getNode();
			rel = DEPLibEn.getRefDependentNode(dep);
			
			if (rel != null)
			{
				if (verb.id < rel.id)
				{
					head = rel.getHead();
					
					while (head != verb && !head.isPos(CTLibEn.POS_IN) && !MPLibEn.isVerb(head.pos))
					{
						rel  = head;
						head = head.getHead();
					}
				}
				else
				{
					head = rel.getHead();
					
					while (head != verb && head.id < verb.id)
					{
						rel  = head;
						head = head.getHead();
					}
				}
				
				addSubtree(tree, rel, added);
				return rel;
			}
		}
		
		return null;
	}
	
	/** Called by {@link LGAsk#generateQuestionFromAsk(DEPTree, String)}. */
	private DEPNode setAuxiliary(DEPTree tree, DEPNode verb, Set<DEPNode> added, DEPNode rel)
	{
		if (!verb.isLabel(DEPLibEn.DEP_XCOMP) && verb.getFirstDependentByLabel(DEPLibEn.P_SBJ) == null)
			return null;
		
		if (rel != null && DEPLibEn.P_SBJ.matcher(rel.getLabel()).find())
			return null;

		DEPNode dep;
		
		for (DEPArc arc : verb.getDependents())
		{
			dep = arc.getNode();
			
			if (arc.isLabel(DEPLibEn.P_AUX) && !dep.isPos(CTLibEn.POS_TO))
			{
				if (dep.isLemma(ENAux.GET))
					return addDoAuxiliary(tree, dep, verb);
				else
				{
					addSubtree(tree, dep, added);
					return dep;
				}
			}
		}

		if (verb.isLabel(DEPLibEn.DEP_XCOMP))
		{
			toNonFinite(verb);
			
			if (rel != null)
			{
				dep = getNode(verb, ENModal.SHOULD, ENModal.SHOULD, CTLibEn.POS_MD, DEPLibEn.DEP_AUX, SRLLib.ARGM_MOD);
				tree.add(dep);
				tree.add(getNode(verb, ENPronoun.I, ENPronoun.I, CTLibEn.POS_PRP, DEPLibEn.DEP_NSUBJ, SRLLib.ARG0));
				verb.addFeat(DEPLib.FEAT_VERB_TYPE, WH_NON_FINITE);
				return dep;
			}
			else
			{
				verb.addFeat(DEPLib.FEAT_VERB_TYPE, NON_FINITE);
				return null;
			}
		}
			
		if (verb.isLemma(ENAux.BE))
		{
			tree .add(verb);
			added.add(verb);
			return verb;
		}
		else
			return addDoAuxiliary(tree, verb, verb);
	}
	
	/** Called by {@link LGAsk#generateQuestionFromAsk(DEPTree, String)}. */
	private DEPNode addDoAuxiliary(DEPTree tree, DEPNode verb, DEPNode head)
	{
		DEPNode aux;
		
		if (verb.isPos(CTLibEn.POS_VBZ))
			tree.add(aux = getNode(head, ENAux.DOES, ENAux.DO, verb.pos, DEPLibEn.DEP_AUX, null));
		else if (verb.isPos(CTLibEn.POS_VBD) || verb.isPos(CTLibEn.POS_VBN))
			tree.add(aux = getNode(head, ENAux.DID , ENAux.DO, CTLibEn.POS_VBD, DEPLibEn.DEP_AUX, null));
		else
			tree.add(aux = getNode(head, ENAux.DO  , ENAux.DO, verb.pos, DEPLibEn.DEP_AUX, null));
		
		toNonFinite(verb);
		return aux;
	}
	
	/** Called by {@link LGAsk#generateQuestionFromAsk(DEPTree, String)}. */
	private void setRest(DEPTree tree, DEPNode verb,  Set<DEPNode> added)
	{
		for (DEPNode node : verb.getSubNodeSortedList())
		{
			if (added.contains(node))
				continue;
			else if (node.isDependentOf(verb) && (node.isPos(CTLibEn.POS_TO) || node.isLabel(DEPLibEn.DEP_COMPLM) || node.isLabel(DEPLibEn.DEP_MARK)))
				continue;
			else
				tree.add(node);
		}
	}
	
	/** Called by {@link LGAsk#generateQuestionFromAsk(DEPTree, String)}. */
	private void resetDEPTree(DEPTree tree, DEPNode root)
	{
		List<DEPNode> remove = new ArrayList<DEPNode>();
		convertUserToYou(root, remove);
		tree.removeAll(remove);
		
		resetDEPTreePost(tree, root);
		tree.resetIDs();
		tree.resetDependents();
	}

	/** Called by {@link LGAsk#generateQuestionFromAsk(DEPTree, String)}. */
	private void resetDEPTreePost(DEPTree tree, DEPNode root)
	{
		String end = UNPunct.QUESTION_MARK;
		String vtype;
		
		if ((vtype = root.getFeat(DEPLib.FEAT_VERB_TYPE)) != null && vtype.equals(NON_FINITE))
		{
			tree.add(1, getNode(root, PLEASE, PLEASE, CTLibEn.POS_UH, DEPLibEn.DEP_INTJ, SRLLib.ARGM_DIS));
			end = UNPunct.PERIOD;
		}
		
		DEPNode last = root.getLastNode();
		
		if (last.isPos(CTLibEn.POS_PERIOD))
		{
			last.form  = end;
			last.lemma = end;
		}
		else
			tree.add(getNode(root, end, end, CTLibEn.POS_PERIOD, DEPLibEn.DEP_PUNCT, null));
	}
	
	/** Called by {@link LGAsk#generateQuestionFromAsk(DEPTree, String)}. */
	private void addSubtree(DEPTree tree, DEPNode head, Set<DEPNode> added)
	{
		List<DEPNode> list = head.getSubNodeSortedList();
		
		tree .addAll(list);
		added.addAll(list);
	}
	
	/** Called by {@link LGAsk#generateQuestionFromAsk(DEPTree, String)}. */
	private void toNonFinite(DEPNode verb)
	{
		verb.form = verb.lemma;
		verb.pos  = CTLibEn.POS_VB;
	}
	
	/** Called by {@link LGAsk#generateQuestionFromAsk(DEPTree, String)}. */
	private DEPNode getNode(DEPNode head, String form, String lemma, String pos, String deprel, String label)
	{
		DEPNode aux = new DEPNode(0, form, lemma, pos, new DEPFeat());
		aux.initXHeads();
		aux.initSHeads();
		
		aux.setHead(head, deprel);
		if (label != null)	aux.addSHead(head, label);

		return aux;
	}
	
	/** Called by {@link LGAsk#generateQuestionFromAsk(DEPTree, String)}. */
	private void matchNumber(DEPNode verb, DEPNode aux)
	{
		for (DEPArc arc : verb.getDependents())
		{
			if (arc.isLabel(DEPLibEn.P_SBJ))
			{
				DEPNode dep = arc.getNode();
				
				if (dep.isLemma(ENPronoun.YOU))
				{
					if (aux.isLemma(ENAux.DO))
					{
						if (!aux.isPos(CTLibEn.POS_VBD) && !aux.isPos(CTLibEn.POS_VBN))
						{
							aux.form = ENAux.DO;
							aux.pos  = CTLibEn.POS_VBP;
						}
					}
					else if (aux.isLemma(ENAux.BE))
					{
						if (aux.isPos(CTLibEn.POS_VBD) || aux.isPos(CTLibEn.POS_VBN))
						{
							aux.form = ENAux.WERE;
							aux.pos  = CTLibEn.POS_VBD;
						}
						else
						{
							aux.form = ENAux.ARE;
							aux.pos  = CTLibEn.POS_VBP;
						}
					}
					else if (aux.isLemma(ENAux.HAVE))
					{
						if (!aux.isPos(CTLibEn.POS_VBD) && !aux.isPos(CTLibEn.POS_VBN))
						{
							aux.form = ENAux.HAVE;
							aux.pos  = CTLibEn.POS_VBP;
						}
					}
				}
				
				break;
			}
		}
	}
	
	/** Called by {@link LGAsk#generateQuestionFromAsk(DEPTree, String)}. */
	private void convertUserToYou(DEPNode node, List<DEPNode> remove)
	{
		List<DEPArc> deps = node.getDependents();
		
		if (node.isPos(CTLibEn.POS_PRPS) && !ENPronoun.is1stSingular(node.lemma))
		{
			node.form = node.lemma = ENPronoun.YOUR;
		}
		else if (node.isLabel(DEPLibEn.DEP_POSS) && isUser(node))
		{
			node.form = node.lemma = ENPronoun.YOUR;
			node.pos = CTLibEn.POS_PRPS;
			remove.addAll(node.getDependentNodeList());
		}
		else if (node.isPos(CTLibEn.POS_PRP) && !ENPronoun.is1stSingular(node.lemma) && !node.isLemma("it"))
		{
			if (node.lemma.endsWith("self"))
				node.form = node.lemma = ENPronoun.YOURSELF;
			else if (node.lemma.endsWith("s"))
				node.form = node.lemma = ENPronoun.YOURS;
			else
			{
				node.form = node.lemma = ENPronoun.YOU;
				matchYouVerb(node);
			}
		}
		else if (isUser(node))
		{
			node.form = node.lemma = ENPronoun.YOU;
			node.pos = CTLibEn.POS_PRP;
			matchYouVerb(node);
			remove.addAll(node.getDependentNodeList());
		}
		else if (!deps.isEmpty())
		{
			DEPNode poss = node.getFirstDependentByLabel(DEPLibEn.DEP_POSS);
			boolean hasPoss = (poss != null) && isUser(poss);
			DEPNode dep;
			
			for (DEPArc arc : deps)
			{
				dep = arc.getNode();
				
				if (hasPoss && arc.isLabel(DEPLibEn.DEP_DET))
					remove.add(dep);
				else
					convertUserToYou(dep, remove);
			}
			
			deps.removeAll(remove);
		}
	}
	
	private void matchYouVerb(DEPNode node)
	{
		if (node.isLabel(DEPLibEn.P_SBJ))
		{
			DEPNode verb = node.getHead();
			
			if (verb != null && MPLibEn.isVerb(verb.pos))
			{
				DEPNode aux = verb.getFirstDependentByLabel(DEPLibEn.P_AUX);
				
				if (aux != null)	to2ndNumber(aux);
				else				to2ndNumber(verb);
			}
		}
	}
	
	private void to2ndNumber(DEPNode verb)
	{
		if (verb.isPosAny(CTLibEn.POS_VB, CTLibEn.POS_VBP, CTLibEn.POS_VBZ))
		{
			verb.form = verb.isLemma(ENAux.BE) ? ENAux.ARE : verb.lemma;
			verb.pos  = CTLibEn.POS_VBP;
		}
		else if (verb.isPosAny(CTLibEn.POS_VBD))
		{
			if (verb.isLemma(ENAux.BE)) verb.form = ENAux.WERE;
		}
	}
	
	private boolean isUser(DEPNode node)
	{
		if (!USER.equalsIgnoreCase(node.form))
			return false;
		
		for (DEPArc arc : node.getDependents())
		{
			if (!arc.isLabel(DEPLibEn.DEP_DET) && !arc.isLabel(DEPLibEn.DEP_POSSESSIVE))
				return false;
		}
		
		return true;
	}
}
