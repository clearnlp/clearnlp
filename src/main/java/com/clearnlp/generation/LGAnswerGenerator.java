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
import java.util.regex.Pattern;

import com.clearnlp.constant.english.ENAux;
import com.clearnlp.constant.universal.UNConstant;
import com.clearnlp.constant.universal.UNPunct;
import com.clearnlp.constituent.CTLibEn;
import com.clearnlp.dependency.DEPArc;
import com.clearnlp.dependency.DEPLib;
import com.clearnlp.dependency.DEPLibEn;
import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dependency.DEPTree;
import com.clearnlp.dependency.srl.ArgInfo;
import com.clearnlp.dependency.srl.SRLArc;
import com.clearnlp.dependency.srl.SRLLib;
import com.clearnlp.dependency.srl.SRLTree;
import com.clearnlp.util.UTRegex;
import com.clearnlp.util.UTString;
import com.clearnlp.util.pair.Pair;

/**
 * Designed for Eliza at IPsoft.
 * @since 1.4.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class LGAnswerGenerator
{
	final boolean USE_COREF  = true;
	final Pattern P_REMOVE   = UTRegex.getORPatternExact(DEPLibEn.DEP_CONJ, DEPLibEn.DEP_CC, DEPLibEn.DEP_CONJ);
	final Pattern P_PRESERVE = UTRegex.getORPatternExact(DEPLibEn.DEP_AGENT, DEPLibEn.DEP_EXPL, DEPLibEn.DEP_HMOD, DEPLibEn.DEP_HYPH, DEPLibEn.DEP_NEG, DEPLibEn.DEP_PRT, DEPLibEn.DEP_PUNCT);
	
	/** For short answers. */
	public LGAnswerGenerator() {}
	
	public String getAnswer(List<DEPTree> rTrees, List<ArgInfo> rArgs, String conjunction, String delim, boolean verbose, boolean trivialize)
	{
		List<Pair<String,String>> answers = getShortAnswers(rTrees, rArgs, conjunction, delim, verbose, trivialize);
		if (verbose) answers = getLongAnswers(rTrees, rArgs, conjunction, delim);
		return joinAnswers(answers, conjunction, delim);
	}
	
	private List<Pair<String,String>> getLongAnswers(List<DEPTree> rTrees, List<ArgInfo> rArgs, String conjunction, String delim)
	{
		List<Pair<String,String>> answers = new ArrayList<Pair<String,String>>();
		DEPTree rTree;
		ArgInfo rArg;
		int i;
		
		for (i=rTrees.size()-1; i>=0; i--)
		{
			rTree = rTrees.get(i);
			rArg  = rArgs.get(i);
			answers.add(getAnswerString(rTree.get(rArg.getPredicateId()), delim));
		}

		return answers;
	}
	
	public List<Pair<String,String>> getShortAnswers(List<DEPTree> rTrees, List<ArgInfo> rArgs, String conjunction, String delim, boolean verbose, boolean trivialize)
	{
		List<Pair<String,String>> answers = new ArrayList<Pair<String,String>>();
		Pair<DEPTree,SRLTree> p;
		ArgInfo rArg;
		int i;
		
		for (i=rTrees.size()-1; i>=0; i--)
		{
			rArg = rArgs.get(i);
			p = getTrees(rTrees.get(i), rArg.getPredicateId());
			answers.add(getShortAnswer(p.o1, p.o2, rArg, delim, verbose, trivialize));
			rTrees.set(i, p.o1);
		}
		
		return answers;
	}
	
	public Pair<String,String> getShortAnswer(DEPTree rTree, SRLTree sTree, ArgInfo rArg, String delim, boolean verbose, boolean trivialize)
	{
		removeDependents(rTree.get(DEPLib.ROOT_ID), sTree.getPredicate());
		SRLLib.relinkRelativeClause(sTree);
		SRLLib.relinkCoordination(sTree);
		
		return getShortAnswerAux(rTree, sTree, rArg, delim, verbose, trivialize);
	}
	
	private Pair<DEPTree,SRLTree> getTrees(DEPTree dTree, int predID)
	{
		dTree = dTree.clone();
		dTree.setDependents();
		LGLibEn.convertUnI(dTree);
		LGLibEn.convertFirstFormToLowerCase(dTree);
		
		return new Pair<DEPTree,SRLTree>(dTree, dTree.getSRLTree(predID));
	}
	
	private void removeDependents(DEPNode root, DEPNode verb)
	{
		List<DEPArc> remove = new ArrayList<DEPArc>();
		
		for (DEPArc arc : verb.getDependents())
		{
			if (arc.isLabel(P_REMOVE))
			{
				arc.getNode().setHead(root);
				remove.add(arc);
			}
		}
		
		verb.removeDependents(remove);
	}
	
	private void removeDependents(DEPNode root, DEPNode verb, Set<DEPNode> keep, boolean verbose, boolean trivialize)
	{
		if (verbose) return;
		
		List<DEPArc> remove = new ArrayList<DEPArc>();
		boolean changeDo = true, hasModal = false;
		DEPNode dep;
		
		for (DEPArc arc : verb.getDependents())
		{
			dep = arc.getNode();
			
			if (arc.isLabel(DEPLibEn.P_AUX))
			{
				if (dep.isPos(CTLibEn.POS_MD) || dep.isLemma(ENAux.DO) || dep.isLemma(ENAux.HAVE))
					hasModal = true;
			}
			else if (!keep.contains(dep) && !arc.isLabel(P_PRESERVE) && !arc.isLabel(DEPLibEn.P_SBJ))
			{
				dep.setHead(root);
				remove.add(arc);
			}
			else if (dep.id > verb.id && !dep.isLabel(DEPLibEn.DEP_PUNCT))
				changeDo = false;
		}
		
		verb.removeDependents(remove);
		
		if (trivialize && changeDo && !verb.isLemma(ENAux.BE))
		{
			if (hasModal)
				verb.form = UNConstant.EMPTY;
			else
			{
				if (verb.isPos(CTLibEn.POS_VB) || verb.isPos(CTLibEn.POS_VBP))
					verb.form = ENAux.DO;
				else if (verb.isPos(CTLibEn.POS_VBZ))
					verb.form = ENAux.DOES;
				else if (verb.isPos(CTLibEn.POS_VBD))
					verb.form = ENAux.DID;
				else if (verb.isPos(CTLibEn.POS_VBN))
					verb.form = ENAux.DONE;
				else if (verb.isPos(CTLibEn.POS_VBG))
					verb.form = ENAux.DOING;
			}
			
			verb.lemma = ENAux.DO;
		}
	}
	
	private Pair<String,String> getShortAnswerAux(DEPTree rTree, SRLTree sTree, ArgInfo rArg, String delim, boolean verbose, boolean trivialize)
	{
		DEPNode root = rTree.get(DEPLib.ROOT_ID);
		DEPNode pred = sTree.getPredicate();
		
		if (rArg.hasSemanticInfo())
		{
			List<DEPNode> nodes = sTree.getArgumentNodes(getBaseLabels(rArg.getSemanticInfo()));
			
			if (nodes.isEmpty())
				return null;
			else
			{
				removeDependents(root, pred, getSubNodeSet(pred, nodes), verbose, trivialize);
				stripArgs(pred, nodes);
				return getAnswerString(nodes, delim);
			}
		}
		
		if (rArg.hasSyntacticInfo())
		{
			DEPNode dep = getNodeFromSyntacticInfo(pred, rArg, delim);
			
			if (dep == null)
				return null;
			else
			{
				removeDependents(root, pred, getSubNodeSet(pred, dep), verbose, trivialize);
				return getAnswerString(dep, delim);				
			}
		}
		
		return getAnswerString(pred, delim);
	}
	
	private void stripArgs(DEPNode pred, List<DEPNode> args)
	{
		if (args.size() >= 2)
		{
			DEPNode fst = args.get(0);
			DEPNode snd = args.get(1);
			
			if (fst.id < pred.id && fst.isLabel(DEPLibEn.P_SBJ) && snd.id > pred.id && snd.getSHead(pred, SRLLib.P_ARG_CONCATENATION) != null)
				args.remove(fst);			
		}
	}
	
	private Set<DEPNode> getSubNodeSet(DEPNode pred, DEPNode node)
	{
		Set<DEPNode> set = new HashSet<DEPNode>();
		set.add(getDependent(pred, node));
				
		return set;
	}
	
	private Set<DEPNode> getSubNodeSet(DEPNode pred, List<DEPNode> nodes)
	{
		Set<DEPNode> set = new HashSet<DEPNode>();
		
		for (DEPNode node : nodes)
			set.add(getDependent(pred, node));
		
		return set;
	}
	
	private DEPNode getDependent(DEPNode pred, DEPNode node)
	{
		if (node.isDependentOf(pred))
			return node;
		else
			return getDependent(pred, node.getHead());
	}
	
	private Pattern getBaseLabels(String label)
	{
		label = SRLLib.getBaseLabel(label);
		return UTRegex.getORPatternExact(label, SRLLib.PREFIX_CONCATENATION+label);
	}
	
	private DEPNode getNodeFromSyntacticInfo(DEPNode head, ArgInfo rArg, String delim)
	{
		Pair<String,String> p = rArg.popNextSyntacticInfo();
		String label = p.o1, lemma = p.o2;
		DEPNode dep;
		
		for (DEPArc arc : head.getDependents())
		{
			dep = arc.getNode();
			
			if (dep.isLemma(lemma) && (arc.isLabel(label) || (label.equals(DEPLibEn.DEP_PREP) && dep.isPos(CTLibEn.POS_IN))))
			{
				if (!rArg.hasSyntacticInfo())
					return dep;
				else
					return getNodeFromSyntacticInfo(dep, rArg, delim);
			}
		}
		
		return null;
	}
	
	private Pair<String,String> getAnswerString(DEPNode node, String delim)
	{
		return getAnswerStringPost(LGLibEn.getForms(node, USE_COREF, delim), delim, node.getFirstNode());
	}
	
	private Pair<String,String> getAnswerString(List<DEPNode> nodes, String delim)
	{
		StringBuilder build = new StringBuilder();
		
		for (DEPNode node : nodes)
		{
			build.append(delim);
			build.append(LGLibEn.getForms(node, USE_COREF, delim));
		}
		
		return getAnswerStringPost(build.substring(delim.length()), delim, nodes.get(0).getFirstNode());
	}
	
	private Pair<String,String> getAnswerStringPost(String answer, String delim, DEPNode fst)
	{
		answer = UTString.stripPunctuation(answer);
		String prep = UNConstant.EMPTY;
		
		if (fst.isPos(CTLibEn.POS_IN))
		{
			prep = fst.lemma + delim;
			int len = prep.length();
			String sub = answer.substring(0, len).toLowerCase();
			
			if (prep.equals(sub))
			{
				answer = answer.substring(len);
				
				if (fst.isLabel(DEPLibEn.DEP_AGENT))
					prep = UNConstant.EMPTY;
			}
			else
				prep = UNConstant.EMPTY;
		}
		
		return new Pair<String,String>(answer.trim(), prep);
	}
	
	private String joinAnswers(List<Pair<String,String>> answers, String conjunction, String delim)
	{
		StringBuilder build = new StringBuilder();
		String prep = UNConstant.EMPTY;
		int i, size = answers.size();
		Pair<String,String> p;
		
		for (i=0; i<size; i++)
		{
			p = answers.get(i);
			
			if (i > 0)
			{
				if (i+1 == size)
				{
					build.append(delim);
					build.append(conjunction);
				}
				else
					build.append(UNPunct.COMMA);
			}
			
			if (!prep.equals(p.o2))
			{
				prep = p.o2;
				build.append(delim);
				build.append(prep);
			}

			build.append(delim);
			build.append(p.o1);
		}
		
		return build.substring(delim.length());
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	@Deprecated
	public String getAnswer(DEPTree qdTree, DEPTree rdTree, int qVerbID, int rVerbID, String delim)
	{
		qdTree = qdTree.clone();	qdTree.setDependents();
		rdTree = rdTree.clone();	rdTree.setDependents();
		
		SRLTree qsTree = qdTree.getSRLTree(qVerbID);
		SRLTree rsTree = rdTree.getSRLTree(rVerbID);
		
		stripUnnecessaries(rsTree.getPredicate());
		SRLLib.relinkRelativeClause(rsTree);
		SRLLib.relinkCoordination(rsTree);
		LGLibEn.convertUnI(qdTree);
		LGLibEn.convertUnI(rdTree);

		if (isShortAnswer(qsTree, rsTree))
			return getShortAnswer(qdTree, rdTree, qsTree, rsTree, delim);
		else
			return getLongAnswer(qdTree, qsTree, rsTree.getPredicate(), delim);
	}
	
	@Deprecated
	private void stripUnnecessaries(DEPNode node)
	{
		List<DEPArc> remove = new ArrayList<DEPArc>();
		
		for (DEPArc arc : node.getDependents())
		{
			if (arc.isLabel(DEPLibEn.DEP_PUNCT) || arc.isLabel(DEPLibEn.DEP_COMPLM) || arc.isLabel(DEPLibEn.DEP_MARK))
				remove.add(arc);
		}
		
		node.removeDependents(remove);
	}
	
	@Deprecated
	private boolean isShortAnswer(SRLTree qsTree, SRLTree rsTree)
	{
		if (matchPassive(qsTree, rsTree, "A0"))	return true;
		if (matchPassive(qsTree, rsTree, "A1"))	return true;
		
		DEPNode rVerb = rsTree.getPredicate();
		DEPNode sbj = rVerb.getFirstDependentByLabel(DEPLibEn.P_SBJ);
		
		if (sbj != null)	return DEPLibEn.isSmallClause(rVerb);
		return true;
	}
	
	@Deprecated
	private boolean matchPassive(SRLTree qsTree, SRLTree rsTree, String label)
	{
		SRLArc arc = rsTree.getFirstArgument(label);
		return matchPassive(qsTree.getFirstArgument(SRLLib.PREFIX_REFERENT+label), arc) || matchPassive(qsTree.getFirstArgument(label), arc);
	}
	
	@Deprecated
	private boolean matchPassive(SRLArc qArc, SRLArc rArc)
	{
		return qArc != null && rArc != null && (qArc.getNode().isLabel(DEPLibEn.P_SBJ) && rArc.getNode().isLabel(DEPLibEn.DEP_AGENT) || qArc.getNode().isLabel(DEPLibEn.DEP_AGENT) && rArc.getNode().isLabel(DEPLibEn.P_SBJ));
	}
	
	@Deprecated
	private String getShortAnswer(DEPTree qdTree, DEPTree rdTree, SRLTree qsTree, SRLTree rsTree, String delim)
	{
		return getShortAnswerFromDeclarative(qdTree, rdTree, qsTree, rsTree, delim);
	}
	
	@Deprecated
	private String getShortAnswerFromDeclarative(DEPTree qdTree, DEPTree rdTree, SRLTree qsTree, SRLTree rsTree, String delim)
	{
		List<SRLArc> arcs = new ArrayList<SRLArc>();
		DEPNode qArg, rArg;
		String answer;
		
		for (SRLArc qArc : qsTree.getArguments())
		{
			if (qArc.isLabel(SRLLib.P_ARG_REF))
			{
				arcs = rsTree.getArguments(getBaseLabels(qArc.getLabel()));
				
				if (arcs.isEmpty())
				{
					String label = SRLLib.getBaseLabel(qArc.getLabel());
					DEPNode node = qArc.getNode();
					
					     if (node.isLemma("where") || label.equals(SRLLib.ARGM_LOC) || label.equals(SRLLib.ARGM_DIR) || label.equals(SRLLib.ARGM_GOL))
						arcs = rsTree.getArguments(Pattern.compile("^(AM-LOC|AM-DIR|AM-GOL)$"));
					else if (node.isLemma("when") || label.equals(SRLLib.ARGM_TMP))
						arcs = rsTree.getArguments(Pattern.compile("^AM-TMP$"));
					else if (label.equals("A1"))
						arcs = rsTree.getArguments(getBaseLabels("A2"));
					else if (label.equals("A2"))
						arcs = rsTree.getArguments(getBaseLabels("A1"));
					else if (label.equals("AM-MNR"))
					{
						for (SRLArc arc : rsTree.getArguments())
						{
							rArg = arc.getNode();
							
							if (rArg.isLabel(DEPLibEn.DEP_ACOMP) || rArg.isLabel(DEPLibEn.DEP_ADVMOD))
								arcs.add(arc);
						}
					}
					else if (node.isPos(CTLibEn.POS_IN))
					{
						for (SRLArc arc : rsTree.getArguments())
						{
							rArg = arc.getNode();
							
							if (rArg.isPos(CTLibEn.POS_IN) && rArg.isLemma(node.lemma))
								arcs.add(arc);
						}
					}
				}				
				
				return arcs.isEmpty() ? null : getAnswer(arcs, delim);
			}
		}
		
		for (SRLArc qArc : qsTree.getArguments())
		{
			qArg = qArc.getNode();
			
			if (qArg.getFeat(DEPLibEn.FEAT_PB) != null)
			{
				arcs = rsTree.getArguments(getBaseLabels(qArc.getLabel()));
				
				for (SRLArc rArc : arcs)
				{
					rArg = rArc.getNode();
					
					if (rArg.getFeat(DEPLibEn.FEAT_PB) != null && rArg.isLemma(qArg.lemma))
					{
						answer = getShortAnswer(qdTree, rdTree, qdTree.getSRLTree(qArg), rdTree.getSRLTree(rArg), delim);
						if (answer != null)	return answer;
					}
				}
			}
		}
		
		return null;
	}
	
	@Deprecated
	private String getLongAnswer(DEPTree qdTree, SRLTree qsTree, DEPNode rVerb, String delim)
	{
		StringBuilder build = new StringBuilder();
		
		getLongAnswerFromDeclarative(qdTree, qsTree, rVerb, delim, build);
		return getAnswerPost(build, delim);
	}
	
	@Deprecated
	private void getLongAnswerFromDeclarative(DEPTree qdTree, SRLTree qsTree, DEPNode rVerb, String delim, StringBuilder build)
	{
		Set<String> qPreps  = getLemmaSet(qsTree, CTLibEn.POS_IN);
		Set<String> qLabels = qsTree.getBaseLabelSet();
		boolean[] bMod = getModifierAspects(qsTree);
		boolean notAdded = true;
		DEPNode rDep, qDep;
		DEPArc  rHead;
		
		for (DEPArc rArc : rVerb.getDependents())
		{
			rDep  = rArc.getNode();
			rHead = rDep.getSHead(rVerb);
			
			if (notAdded && rDep.id > rVerb.id)
			{
				build.append(delim);
				build.append(rVerb.form);
				notAdded = false;
			}
			
			if (rArc.isLabel(DEPLibEn.DEP_CONJ) || rArc.isLabel(DEPLibEn.DEP_CC) || rArc.isLabel(DEPLibEn.DEP_PRECONJ))
				continue;
			else if (rHead == null || rHead.isLabel(SRLLib.ARGM_MOD) || rHead.isLabel(SRLLib.ARGM_NEG))
			{
				build.append(delim);
				build.append(LGLibEn.getForms(rDep, USE_COREF, delim));
			}
			else if (containsLabel(qsTree, qLabels, qPreps, SRLLib.getBaseLabel(rHead.getLabel()), rDep, bMod[0], bMod[1], bMod[2]))
			{
				if (rDep.getFeat(DEPLibEn.FEAT_PB) != null && (qDep = findPredicateInQuestion(qsTree, rHead.getLabel(), rDep.lemma)) != null) 
				{
					getLongAnswerFromDeclarative(qdTree, qdTree.getSRLTree(qDep), rDep, delim, build);
				}
				else
				{
					build.append(delim);
					build.append(LGLibEn.getForms(rDep, USE_COREF, delim));
				}
			}
		}
		
		if (notAdded)
		{
			build.append(delim);
			build.append(rVerb.form);
		}
	}
	
	private Set<String> getLemmaSet(SRLTree sTree, String pos)
	{
		Set<String> set = new HashSet<String>();
		DEPNode arg;
		
		for (SRLArc arc : sTree.getArguments())
		{
			arg = arc.getNode();
			
			if (arg.isPos(pos))
				set.add(arg.lemma);
		}
		
		return set;
	}
	
	private boolean[] getModifierAspects(SRLTree qsTree)
	{
		boolean[] b = {false, false, false};
		String label;
		DEPNode  arg;
		
		for (SRLArc arc : qsTree.getArguments())
		{
			label = SRLLib.getBaseLabel(arc.getLabel());
			arg   = arc.getNode();
			
			if (arg.isLemma("where") || isLocative(label))
				b[0] = true;
			else if (arg.isLemma("when") || isTemporal(label))
				b[1] = true;
			else if (isManner(label))
				b[2] = true;
		}
		
		return b;
	}
	
	@Deprecated
	private boolean isLocative(String label)
	{
		return label.equals(SRLLib.ARGM_LOC) || label.equals(SRLLib.ARGM_DIR) || label.equals(SRLLib.ARGM_GOL);
	}
	
	@Deprecated
	private boolean isTemporal(String label)
	{
		return label.equals(SRLLib.ARGM_TMP);
	}
	
	@Deprecated
	private boolean isManner(String label)
	{
		return label.equals(SRLLib.ARGM_MNR);
	}
	
	@Deprecated
	private boolean containsLabel(SRLTree qsTree, Set<String> qLabels, Set<String> qPreps, String rLabel, DEPNode rDep, boolean qLocative, boolean qTemporal, boolean qManner)
	{
		if (qLabels.contains(rLabel))
			return true;
		
		if (isLocative(rLabel) && qLocative)
			return true;
		
		if (isTemporal(rLabel) && qTemporal)
			return true;
		
		if ((rDep.isLabel(DEPLibEn.DEP_ACOMP) || rDep.isLabel(DEPLibEn.DEP_ADVMOD)) && qManner)
			return true;
		
		if (rLabel.equals(SRLLib.ARG1) && qLabels.contains(SRLLib.ARG2))
			return true;
		
		if (rLabel.equals(SRLLib.ARG2) && qLabels.contains(SRLLib.ARG1))
			return true;
		
		if (rDep.isPos(CTLibEn.POS_IN) && qPreps.contains(rDep.lemma))
			return true;
		
		return false;
	}
	
	@Deprecated
	private DEPNode findPredicateInQuestion(SRLTree qsTree, String label, String lemma)
	{
		SRLArc qArc = qsTree.getFirstArgument(label);
		
		if (qArc != null && qArc.getNode().getFeat(DEPLibEn.FEAT_PB) != null)
		{
			return qArc.getNode();
		}
		else
		{
			DEPNode arg;
			
			for (SRLArc arc : qsTree.getArguments())
			{
				arg = arc.getNode();
				
				if (arg.getFeat(DEPLibEn.FEAT_PB) != null && arg.isLemma(lemma))
					return arg;
			}
		}
		
		return null;
	}
	
	@Deprecated
	private String getAnswer(List<SRLArc> arcs, String delim)
	{
		StringBuilder build = new StringBuilder();
		
		for (SRLArc arc : arcs)
		{
			build.append(delim);
			build.append(LGLibEn.getForms(arc.getNode(), USE_COREF, delim));
		}
		
		String s = getAnswerPost(build, delim);
		
		SRLArc arc = arcs.get(0);
		
		if (arc.getNode().isLabel(DEPLibEn.DEP_AGENT) && s.startsWith("By"))
			s = s.substring(2).trim();
		
		return s;
	}
	
	@Deprecated
	private String getAnswerPost(StringBuilder build, String delim)
	{
		String s = build.substring(delim.length());
		
		s = UTString.stripPunctuation(s);
		s = UTString.convertFirstCharToUpper(s);
		
		return s;
	}
}
