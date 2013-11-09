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
package com.clearnlp.run;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kohsuke.args4j.Option;

import com.carrotsearch.hppc.IntOpenHashSet;
import com.clearnlp.constituent.CTLib;
import com.clearnlp.constituent.CTLibEn;
import com.clearnlp.constituent.CTNode;
import com.clearnlp.constituent.CTTree;
import com.clearnlp.propbank.PBArg;
import com.clearnlp.propbank.PBInstance;
import com.clearnlp.propbank.PBLib;
import com.clearnlp.propbank.PBLoc;
import com.clearnlp.reader.AbstractReader;
import com.clearnlp.util.UTHppc;
import com.clearnlp.util.UTOutput;
import com.clearnlp.util.list.SortedIntArrayList;
import com.clearnlp.util.pair.Pair;


public class PBPostProcess extends AbstractRun
{
	/** The error code for mis-aligned arguments. */	
	static final public String ERR_ALIGN   = "A";
	/** The error code for cyclic relations. */	
	static final public String ERR_CYCLIC  = "C";
	/** The error code for overlapping arguments. */	
	static final public String ERR_OVERLAP = "O";
	/** The error code for no counterpart of light-verb. */	
	static final public String ERR_LV      = "L";
	
	@Option(name="-i", usage="the PropBank file to be post-processed (required)", required=true, metaVar="<filename>")
	private String s_propFile;
	@Option(name="-o", usage="the post-processed PropBank file (default: null)", required=false, metaVar="<filename>")
	private String s_postFile = null;
	@Option(name="-t", usage="the directory path to Treebank files (required)", required=true, metaVar="<dirpath>")
	private String s_treeDir;
	@Option(name="-n", usage="if set, normalize co-indices of constituent trees", required=false)
	private boolean b_norm;
	@Option(name="-l", usage="language (default: "+AbstractReader.LANG_EN+")", required=false, metaVar="<language>")
	private String s_language = AbstractReader.LANG_EN;
	
	public PBPostProcess(String[] args)
	{
		initArgs(args);
		postProcess(s_propFile, s_postFile, s_treeDir, b_norm, s_language);
	}
	
	public void postProcess(String propFile, String postFile, String treeDir, boolean norm, String language)
	{
		List<PBInstance> instances = PBLib.getPBInstanceList(propFile, treeDir, norm);
		List<PBInstance> remove = new ArrayList<PBInstance>();
		mergeLightVerbs(instances);
		CTTree tree;
		PBArg  aDSP;
		
		for (PBInstance instance : instances)
		{
			System.out.println(instance.getKey());
			tree = instance.getTree();
			
			// LINK-SLC, LINK-PSV are found here
			if (language.equals(AbstractReader.LANG_EN))
				CTLibEn.preprocessTree(tree);
			else if (language.equals(AbstractReader.LANG_AR))
				;
			
			// removes instances that do not align with the constiteunt tree
			if (isSkip(instance, tree))		// varies by languages
			{
				remove.add(instance);
				continue;
			}
			
			// sorts by arguments' terminal IDs
			instance.sortArgs();
			
			joinConcatenations(instance);
			fixCyclicLocs(instance);
			removeRedundantLocs(instance);
			// annotating NP(PRO) under S following the verb
			if (instance.isVerbPredicate())				// English only
				fixIllegalPROs(instance);
			aDSP = getArgDSP(instance);					// English only
			getLinks(instance);
			normalizeLinks(instance);					// varies by languages
			instance.sortArgs();
			removeRedundantLocs(instance);
			findOverlappingArguments(instance);
			addLinks(instance);
			raiseEmptyArguments(instance);				// English only
			if (aDSP != null)	instance.addArg(aDSP);	// English only
		}
		
		instances.removeAll(remove);
		
		if (postFile == null)
			printInstances(instances, treeDir);
		else
			PBLib.printPBInstances(instances, postFile);
	}
	
	
	/**
	 * Returns {@code true} if the specific PropBank instance is valid.
	 * @param instance a PropBank instance
	 * @param tree a constiteunt tree associated with the PropBank instance.
	 * @return {@code true} if the specific PropBank instance is valid.
	 */
	private boolean isSkip(PBInstance instance, CTTree tree)
	{
		if (PBLib.ILLEGAL_ROLESET.matcher(instance.roleset).find())
			return true;
		
		if (findMisalignedArgs(instance))
			return true;
		
		if (instance.isVerbPredicate() && tree.getTerminal(instance.predId).getParent().isPTag(CTLibEn.PTAG_PP))
			return true;
		
		return false;
	}
	
	private void mergeLightVerbs(List<PBInstance> instances)
	{
		Map<String,PBInstance> mNouns = new HashMap<String,PBInstance>();
		List<PBInstance> lVerbs = new ArrayList<PBInstance>();
		List<PBInstance> remove = new ArrayList<PBInstance>();
		PBInstance nInst;
		List<PBArg> args;
		PBArg rel;
		
		for (PBInstance instance : instances)
		{
			if (instance.isVerbPredicate())
			{
				if (instance.roleset.endsWith("LV"))
					lVerbs.add(instance);
			}
			else
				mNouns.put(instance.getKey(), instance);
		}
		
		for (PBInstance instance : lVerbs)
		{
			nInst = null;
			args  = new ArrayList<PBArg>();
			
			for (PBArg arg : instance.getArgs())
			{
				if (arg.label.endsWith("PRR"))
					nInst = mNouns.get(instance.getKey(arg.getLoc(0).terminalId));
				else if (arg.label.startsWith("LINK") || arg.isLabel(PBLib.PB_ARG0))
					args.add(arg);
			}
			
			if (nInst == null)
			{
				StringBuilder build = new StringBuilder();
				
				build.append(ERR_LV);
				build.append(":");
				build.append(" ");
				build.append(instance.toString());
				
				System.err.println(build.toString());
				remove.add(instance);
			}
			else
			{
				nInst.addArgs(args);
				rel = nInst.getFirstArg(PBLib.PB_REL);
				rel.addLoc(new PBLoc(instance.predId, 0, ","));
				
				args.clear();
				
				for (PBArg arg : instance.getArgs())
				{
					if (!arg.isLabel(PBLib.PB_REL) && !arg.label.endsWith("PRR"))
						args.add(arg);
				}
				
				instance.removeArgs(args);
			}
		}
		
		instances.removeAll(remove);
	}
	
	/** Returns {@code true} if the specific instance includes arguments misaligned to the constituent tree. */
	private boolean findMisalignedArgs(PBInstance instance)
	{
		CTTree tree  = instance.getTree();
		String label = null;
		
		if (!tree.isRange(instance.predId, 0) ||
			(instance.isVerbPredicate() && !tree.getTerminal(instance.predId).pTag.startsWith("VB")) ||
			(instance.isNounPredicate() && !tree.getTerminal(instance.predId).pTag.startsWith("NN")))
		{
			label = PBLib.PB_REL;
		}
		else
		{
			outer: for (PBArg arg : instance.getArgs())
			{
				for (PBLoc loc : arg.getLocs())
				{
					if (!tree.isRange(loc))
					{
						label = arg.label;
						break outer;
					}
					
					if (loc.isType("&"))
						loc.type = "*";
				}
			}
		}
		
		if (label != null)
		{
			StringBuilder build = new StringBuilder();
			
			build.append(ERR_ALIGN);
			build.append(":");
			build.append(label);
			build.append(" ");
			build.append(instance.toString());
			
			System.err.println(build.toString());
			return true;
		}
		
		return false;
	}
	
	/**
	 * Joins concatenated locations by replacing them with higher nodes.
	 * PRE: {@link PBInstance#sortArgs()} is called.
	 */
	private void joinConcatenations(PBInstance instance)
	{
		SortedIntArrayList ids = new SortedIntArrayList();
		CTTree tree = instance.getTree();
		int terminalId, height;
		CTNode node, parent;
		List<PBLoc> lNew;
		
		for (PBArg arg : instance.getArgs())
		{
			if (arg.isLabel(PBLib.PB_REL))	continue;
			ids.clear();
			
			for (PBLoc loc : arg.getLocs())
			{
				if (!loc.isType("") && !loc.isType(","))	return;
				if (loc.height > 0)							return;
				ids.add(loc.terminalId);
			}
			
			lNew = new ArrayList<PBLoc>();
			
			while (!ids.isEmpty())
			{
				terminalId = ids.get(0);
				height     = 0;
				node       = tree.getNode(terminalId, height);
				
				while ((parent = node.getParent()) != null && !parent.isPTag(CTLib.PTAG_TOP) && UTHppc.isSubset(ids, parent.getSubTerminalIdSet()))
				{
					node = parent;
					height++;
				}
				
				lNew.add(new PBLoc(terminalId, height, ","));
				ids.removeAll(node.getSubTerminalIdSet());
			}
			
			if (lNew.size() < arg.getLocSize())
			{
				lNew.get(0).type = "";
				arg.replaceLocs(lNew);
			}
		}
	}
	
	/**
	 * Fixes locations cyclic to its predicate.
	 * PRE: {@link PBInstance#sortArgs()} is called.
	 */
	private void fixCyclicLocs(PBInstance instance)
	{
		CTTree  tree  = instance.getTree();
		int    predId = instance.predId;
		boolean isCyc = false;
		CTNode  node, tmp;
		
		StringBuilder build = new StringBuilder();
		build.append(ERR_CYCLIC);
		
		for (PBArg arg : instance.getArgs())
		{
			if (arg.isLabel(PBLib.PB_REL))	continue;
			
			for (PBLoc loc : arg.getLocs())
			{
				if ((node = tree.getNode(loc)).getSubTerminalIdSet().contains(predId))
				{
					if (arg.isLabel(PBLib.PB_ARGM_MOD))
						loc.height = 0;
					else if (arg.isLabel(PBLib.PB_LINK_SLC) && node.isPTag(CTLibEn.PTAG_SBAR) && (tmp = node.getFirstChild("+WH.*")) != null)
						loc.set(tmp.getPBLoc(), loc.type);
					else if (node.isPTag(CTLibEn.PTAG_NP) && (tmp = node.getChild(0)).isPTag(CTLibEn.PTAG_NP) && !tmp.getSubTerminalIdSet().contains(predId))
						loc.height--;
					else
					{
						build.append(":");
						build.append(arg.label);
						isCyc = true;
						break;
					}
				}
			}
		}
		
		if (isCyc)
		{
			build.append(" ");
			build.append(instance.toString());
			System.err.println(build.toString());
		//	System.err.println(tree.toString(true,true));
		}
	}
	
	/**
	 * Removes redundant or overlapping locations of this argument.
	 * PRE: {@link PBInstance#sortArgs()} is called.
	 */
	private void removeRedundantLocs(PBInstance instance)
	{
		List<PBLoc> lDel = new ArrayList<PBLoc>();
		PBLoc curr, next;
		int i, size;
		
		for (PBArg arg : instance.getArgs())
		{
			size = arg.getLocSize() - 1;
			lDel.clear();
			
			for (i=0; i<size; i++)
			{
				curr = arg.getLoc(i);
				next = arg.getLoc(i+1);
				
				if (curr.terminalId == next.terminalId)
					lDel.add(curr);
			}
			
			if (!lDel.isEmpty())
				arg.removeLocs(lDel);
		}
	}
	
	/** Fixes illegal PROs. */
	private void fixIllegalPROs(PBInstance instance)
	{
		CTTree tree = instance.getTree();
		CTNode node;
		
		for (PBArg arg : instance.getArgs())
		{
			if (arg.isLabel(PBLib.PB_REL))	continue;
			
			for (PBLoc loc : arg.getLocs())
			{
				if (loc.terminalId > instance.predId)
				{
					node = tree.getNode(loc);
					
					if (node.isEmptyCategoryRec() && node.hasFTag(CTLibEn.FTAG_SBJ) && node.getParent().isPTag(CTLibEn.PTAG_S))
						loc.height++;
				}
			}
		}
	}
	
	/**
	 * Adds antecedents from manual annotation of LINK-*.
	 * PRE: {@link PBInstance#sortArgs()} is called. 
	 */
	private void getLinks(PBInstance instance)
	{
		CTTree tree = instance.getTree();
		CTNode node, link;
		List<PBArg> lLinks = new ArrayList<PBArg>();
		PBLoc loc; int i;
		
		for (PBArg arg : instance.getArgs())
		{
			if (arg.label.startsWith("LINK"))
				lLinks.add(arg);
			
			for (i=arg.getLocSize()-1; i>0; i--)
			{
				loc  = arg.getLoc(i);
				node = tree.getNode(loc);
				
				if (node.pTag.startsWith("WH"))
				{
					link = CTLibEn.getComplementizer(node);

					if (link.getAntecedent() == null)
					{
						link.setAntecedent(tree.getNode(arg.getLoc(i-1)));
						break;
					}
				}
				else if (CTLibEn.isComplementizer(node))
				{
					if (node.getAntecedent() == null)
					{
						node.setAntecedent(tree.getNode(arg.getLoc(i-1)));
						break;
					}
				}
				else if (node.isEmptyCategoryRec() && loc.isType("*"))
				{
					link = node.getFirstTerminal();
					
					if (link.getAntecedent() == null)
						link.setAntecedent(tree.getNode(arg.getLoc(i-1)));
				}
			}
		}
		
		if (!lLinks.isEmpty())
			instance.removeArgs(lLinks);
	}
	
	/**
	 * Normalizes links.
	 * PRE: {@link CTTree#setPBLocs()} and {@link C} needs to be called before.
	 */
	private void normalizeLinks(PBInstance instance)
	{
		List<PBLoc> lDel = new ArrayList<PBLoc>();
		CTTree tree = instance.getTree();
		CTNode curr, node, ante;
		PBLoc  cLoc; int i;
		List<CTNode> list;
		CTNode pred = tree.getTerminal(instance.predId);
		
		for (PBArg arg : instance.getArgs())
		{
			if (arg.isLabel(PBLib.PB_REL))	continue;
			lDel.clear();
			
			for (i=0; i<arg.getLocSize(); i++)	// size() gets changed dynamically
			{
				cLoc = arg.getLoc(i);
				curr = tree.getNode(cLoc);
				
				if (CTLibEn.isComplementizer(curr))
				{
					if ((ante = curr.getAntecedent()) != null)
						arg.addLoc(new PBLoc(ante.getPBLoc(), "*"));
					
					if ((node = getCoIndexedWHNode(curr)) != null)
						cLoc.set(node.getPBLoc(), "*");
				}
				else if (curr.pTag.startsWith("WH"))
				{
					if ((node = CTLibEn.getComplementizer(curr)) != null && (ante = node.getAntecedent()) != null)
						arg.addLoc(new PBLoc(ante.getPBLoc(), "*"));
				}
				else if (curr.isEmptyCategoryRec())		// *T*, *
				{
					cLoc.height = 0;
					node = tree.getTerminal(cLoc.terminalId);
					
					if ((ante = node.getAntecedent()) != null)
						arg.addLoc(new PBLoc(ante.getPBLoc(), "*"));
				}
				else if (!(list = curr.getIncludedEmptyCategory("\\*(ICH|RNR)\\*.*")).isEmpty())
				{
					for (CTNode ec : list)
					{
						lDel.add(new PBLoc(ec.getPBLoc(), ""));
						
						if ((ante = ec.getAntecedent()) != null)
						{
							if (ante.isDescendantOf(curr) || pred.isDescendantOf(ante))
								lDel.add(new PBLoc(ante.getPBLoc(), ""));
							else
								arg.addLoc(new PBLoc(ante.getPBLoc(), ";"));
						}
					}
				}
				else if (curr.isPTag(CTLibEn.PTAG_S) && (node = curr.getFirstChild("-"+CTLibEn.FTAG_SBJ)) != null && node.isEmptyCategoryRec() && curr.containsTags(CTLibEn.PTAG_VP))
				{
					node = node.getFirstTerminal();
					
					if (CTLibEn.RE_NULL.matcher(node.form).find() && (ante = node.getAntecedent()) != null && ante.hasFTag(CTLibEn.FTAG_SBJ) && !ante.isEmptyCategoryRec() && !existsLoc(instance, ante.getPBLoc()))
						arg.addLoc(new PBLoc(ante.getPBLoc(), "*"));
				}
			}
			
			// removes errorneous arguments
			for (PBLoc rLoc : lDel)
				arg.removeLoc(rLoc.terminalId, rLoc.height);
		}
	}
	
	/** Called by {@link PBLibEn#normalizeLinks(CTTree, PBArg, int)}. */
	private CTNode getCoIndexedWHNode(CTNode node)
	{
		CTNode parent = node.getParent();
		
		while (parent != null)
		{
			if (!parent.pTag.startsWith("WH"))
				break;
			
			if (parent.coIndex != -1)
				return parent;
			
			parent = parent.getParent();
		}
		
		return null;
	}
	
	private boolean existsLoc(PBInstance instance, PBLoc loc)
	{
		for (PBArg arg : instance.getArgs())
		{
			for (PBLoc l : arg.getLocs())
			{
				if (l.equals(loc.terminalId, loc.height))
					return true;
			}
		}
		
		return false;
	}
	
	private boolean findOverlappingArguments(PBInstance instance)
	{
		CTTree  tree = instance.getTree();
		PBArg ai, aj;
		IntOpenHashSet si, sj;
		int i, j, size = instance.getArgSize(), ni, nj;
		List<PBArg> lDel = new ArrayList<PBArg>();
		
		for (i=0; i<size; i++)
		{
			ai = instance.getArg(i);
			si = getTerminalIdSet(ai, tree);
			ni = si.size();
			
			for (j=i+1; j<size; j++)
			{
				aj = instance.getArg(j);
				sj = getTerminalIdSet(aj, tree);
				nj = sj.size();
				
				if (UTHppc.isSubset(si, sj) && ni != nj)
				{
					lDel.add(aj);
				}
				else if (UTHppc.isSubset(sj, si) && ni != nj)
				{
					lDel.add(ai);
				}
				else if (!UTHppc.intersection(si, sj).isEmpty())
				{
					StringBuilder build = new StringBuilder();
					
					build.append(ERR_OVERLAP);
					build.append(":");
					build.append(ai.label);
					build.append(":");
					build.append(aj.label);
					build.append(" ");
					build.append(instance.toString());
					
					System.err.println(build.toString());
				//	System.err.println(tree.toString(true,true));
					return true;
				}
			}
		}
		
		instance.removeArgs(lDel);
		return false;
	}
	
	/** Returns the set of terminal IDs associated with this argument. */
	private IntOpenHashSet getTerminalIdSet(PBArg arg, CTTree tree)
	{
		IntOpenHashSet set = new IntOpenHashSet();
		
		for (PBLoc loc : arg.getLocs())
		{
			if (!loc.isType(";"))
				set.addAll(tree.getNode(loc).getSubTerminalIdSet());
		}
		
		return set;
	}
	
	private void addLinks(PBInstance instance)
	{
		CTTree tree = instance.getTree();
		CTNode node, comp, ante = null;
		String label;
		List<PBArg> lAdd = new ArrayList<PBArg>();
		PBArg nArg;
		
		for (PBArg arg : instance.getArgs())
		{
			for (PBLoc loc : arg.getLocs())
			{
				node  = tree.getNode(loc);
				label = null;
				
				if (node.pTag.startsWith("WH"))
				{
					if ((comp = CTLibEn.getComplementizer(node)) != null && (ante = comp.getAntecedent()) != null)
						label = PBLib.PB_LINK_SLC;
				}
				else if (node.isEmptyCategory())
				{
					if ((ante = node.getAntecedent()) != null)
					{
						if (node.form.equals(CTLibEn.EC_NULL))
							label = PBLib.PB_LINK_PSV;
						else if (node.form.equals(CTLibEn.EC_PRO))
							label = PBLib.PB_LINK_PRO;
					}
				}
				
				if (label != null)
				{
					nArg = new PBArg();
					nArg.label = label;
					nArg.addLoc(new PBLoc(ante.getPBLoc(), ""));
					nArg.addLoc(new PBLoc(node.getPBLoc(), "*"));
					
					lAdd.add(nArg);
				}
			}
		}
		
		instance.addArgs(lAdd);
	}
	
	private void raiseEmptyArguments(PBInstance instance)
	{
		CTTree tree = instance.getTree();
		CTNode node, parent;
		int i, size;
		PBLoc loc;
		
		for (PBArg arg : instance.getArgs())
		{
			if (arg.isLabel(PBLib.PB_REL))	continue;
			size = arg.getLocSize();
			
			for (i=0; i<size; i++)
			{
				loc  = arg.getLoc(i);
				node = tree.getNode(loc);
				parent = node.getParent();
				
				if (parent != null && !parent.isPTag(CTLib.PTAG_TOP) && parent.getChildrenSize() == 1)
					node = parent;
				
				loc.set(node.getPBLoc(), loc.type);
			}
		}
	}
	
	private void printInstances(List<PBInstance> instances, String treeDir)
	{
		String treePath = "", propPath;
		PrintStream fout = null;
		
		for (PBInstance instance : instances)
		{
			if (!treePath.equals(instance.treePath))
			{
				if (fout != null)	fout.close();
				treePath = instance.treePath;
				propPath = treePath.substring(0, treePath.lastIndexOf(".")) + ".prop";
				
				if (new File(propPath).exists())
					System.err.println("Warning: '"+propPath+"' already exists");
				
				fout = UTOutput.createPrintBufferedFileStream(treeDir+File.separator+propPath);
			}
			
			fout.println(instance.toString());
		}
		
		if (fout != null)	fout.close();
	}
	
	private PBArg getArgDSP(PBInstance instance)
	{
		CTTree tree = instance.getTree();
		CTNode pred = tree.getTerminal(instance.predId);
		Pair<CTNode,CTNode> pair = getESMPair(pred);
		if (pair == null)	return null;
		
		Pair<PBArg,IntOpenHashSet> max = new Pair<PBArg,IntOpenHashSet>(null, new IntOpenHashSet());
		IntOpenHashSet set;
		CTNode prn = pair.o1;
		CTNode esm = pair.o2;
		
		for (PBArg arg : instance.getArgs())
		{
			if (!PBLib.isNumberedArgument(arg) || arg.isLabel(PBLib.PB_ARG0))
				continue;
			
			set = arg.getTerminalIdSet(tree);
			
			if (set.contains(esm.getTerminalId()))
			{
				max.set(arg, set);
				break;
			}
			
			if (arg.hasType(",") && max.o2.size() < set.size())
				max.set(arg, set);
		}
		
		if (max.o1 == null)	return null;
		CTNode dsp = esm.getAntecedent();
		if (dsp == null)	dsp = prn.getNearestAncestor("+S.*");
		
		if (dsp != null)
		{
			PBArg arg = new PBArg();
			arg.addLoc(dsp.getPBLoc());
			arg.label = max.o1.label+"-"+PBLib.PB_DSP;
			instance.removeArgs(max.o1.label);
			
			return arg;
		}
		
		return null;
	}
	
	private Pair<CTNode,CTNode> getESMPair(CTNode pred)
	{
		CTNode s = pred.getNearestAncestor("+S.*");
		
		if (s != null && s.getParent().isPTag(CTLibEn.PTAG_PRN))
		{
			CTNode next = pred.getNextSibling("+S|SBAR");
			
			if (next != null)
			{
				CTNode ec = getESM(next);
				if (ec != null)	return new Pair<CTNode,CTNode>(s.getParent(), ec);
			}
		}
		
		return null;
	}
	
	private CTNode getESM(CTNode node)
	{
		if (node.isPTag(CTLibEn.PTAG_S))
			return getESMAux(node);
		else if (node.isPTag(CTLibEn.PTAG_SBAR))
		{
			if (node.getChildrenSize() == 2)
			{
				CTNode fst = node.getChild(0);
				CTNode snd = node.getChild(1);
				
				if (fst.isEmptyCategory() && fst.form.equals(CTLibEn.EC_ZERO))
					return getESMAux(snd);
			}
		}
		
		return null;
	}
	
	private CTNode getESMAux(CTNode node)
	{
		if (node.isEmptyCategoryRec())
		{
			CTNode ec = node.getFirstTerminal();
			
			if (ec != null && (ec.form.startsWith(CTLibEn.EC_TRACE) || ec.form.startsWith(CTLibEn.EC_ESM)))
				return ec;
		}
		
		return null;
	}
	
	static public void main(String[] args)
	{
		new PBPostProcess(args);
	//	new PBPostProcess().postProcess(propFile, postFile, treeDir, norm);
	}
}
