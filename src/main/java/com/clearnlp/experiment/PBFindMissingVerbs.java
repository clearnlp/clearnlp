/**
* Copyright (c) 2009-2012, Regents of the University of Colorado
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
*
* Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
* Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
* Neither the name of the University of Colorado at Boulder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
* AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
* IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
* ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
* LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
* CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
* ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
* POSSIBILITY OF SUCH DAMAGE.
*/
package com.clearnlp.experiment;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.kohsuke.args4j.Option;

import com.carrotsearch.hppc.IntOpenHashSet;
import com.carrotsearch.hppc.cursors.IntCursor;
import com.clearnlp.component.morph.EnglishMPAnalyzer;
import com.clearnlp.constituent.CTLibEn;
import com.clearnlp.constituent.CTNode;
import com.clearnlp.constituent.CTTree;
import com.clearnlp.morphology.MPLibEn;
import com.clearnlp.propbank.PBArg;
import com.clearnlp.propbank.PBInstance;
import com.clearnlp.propbank.PBLib;
import com.clearnlp.propbank.PBLoc;
import com.clearnlp.run.AbstractRun;
import com.clearnlp.util.UTOutput;


public class PBFindMissingVerbs extends AbstractRun
{
	@Option(name="-t", usage="the tree directory (input; required)", required=true, metaVar="<filepath>")
	String s_treeDir;
	@Option(name="-d", usage="the dictionary file (input; required)", required=true, metaVar="<filename>")
	String s_dictFile;
	@Option(name="-p", usage="the propbank file (input; required)", required=true, metaVar="<filename>")
	String s_propFile;
	@Option(name="-o", usage="the output file (output; required)", required=true, metaVar="<filename>")
	String s_outFile;
	
	public PBFindMissingVerbs(String[] args) throws Exception
	{
		initArgs(args);
		
		Map<String,List<PBInstance>> map = PBLib.getPBInstanceMap(s_propFile, s_treeDir, false);
		EnglishMPAnalyzer morph = new EnglishMPAnalyzer();
		List<List<PBInstance>> lists = new ArrayList<List<PBInstance>>();
		IntOpenHashSet[] sets = new IntOpenHashSet[4];
		int i, size = sets.length;
		List<PBInstance> list;
		PBInstance fst;
		CTTree tree;
		
		init(sets, lists);
		
		for (String key : map.keySet())
		{
			list = map.get(key);
			fst  = list.get(0);
			tree = fst.getTree();
			
		//	if (fst.treePath.startsWith("wb/sel"))
		//		continue;
			
			getPredicateIds(tree, sets);
			removeExistingPredicates(sets, list);

			for (i=0; i<size; i++)
				addMissingPredicates(tree, morph, fst, sets[i], lists.get(i));
		}

		for (i=0; i<size; i++)
			printPredicates(lists.get(i), s_outFile+"."+i);
	}
	
	private void init(IntOpenHashSet[] sets, List<List<PBInstance>> lists)
	{
		int i, size = sets.length;
		
		for (i=0; i<size; i++)
		{
			sets[i] = new IntOpenHashSet();
			lists.add(new ArrayList<PBInstance>());
		}
	}
	
	private void getPredicateIds(CTTree tree, IntOpenHashSet[] sets)
	{
		for (IntOpenHashSet set : sets)
			set.clear();
				
		int idx;
		
		for (CTNode node : tree.getTokens())
		{
			idx = isVerbPredicate(tree, node);
			if (idx >= 0)	sets[idx].add(node.getTerminalId());
		}
	}
	
	/**
	 * Returns 0 if the specific node is not a verb predicate.
	 * Returns 1 if the specific node is an auxiliary-like verb predicate.
	 * Returns 2 if the specific node is a verb predicate.
	 * @param node the node to be compared.
	 * @return 0, 1, or 2.
	 */
	public int isVerbPredicate(CTTree tree, CTNode node)
	{
		CTNode parent = node.getParent();
		
		if (CTLibEn.isVerb(node) && parent.isPTag(CTLibEn.PTAG_VP) && !parent.containsTags(CTLibEn.PTAG_VP))
		{
			String lower = node.form.toLowerCase();
			
			if (MPLibEn.isDo(lower) || hasEditedAncestor(node) || parent.hasFTag(CTLibEn.FTAG_UNF))
				return -1;
			
			if (isHyphenated(tree, node))
				return 0;
			
			if (isAuxiliaryLike(tree, node, lower))
				return 1;
			
			if (MPLibEn.isBe(lower) || MPLibEn.isBecome(lower) || MPLibEn.isGet(lower) || MPLibEn.isHave(lower))
				return 2;
			
			return 3;
		}
		
		return -1;
	}
	
	private boolean hasEditedAncestor(CTNode node)
	{
		CTNode parent = node.getParent();
		
		while (parent != null)
		{
			if (parent.isPTag(CTLibEn.PTAG_EDITED))
				return true;
			
			parent = parent.getParent();
		}
		
		return false;
	}
	
	private boolean isHyphenated(CTTree tree, CTNode node)
	{
		int tokenId = node.getTokenId(), size = tree.getTokens().size();
		
		if (tokenId - 1 >= 0 && tree.getToken(tokenId-1).isPTag(CTLibEn.POS_HYPH))
			return true;
			
		if (tokenId + 1 < size && tree.getToken(tokenId+1).isPTag(CTLibEn.POS_HYPH))
			return true;
		
		return false;
	}
	
	private boolean isAuxiliaryLike(CTTree tree, CTNode node, String lower)
	{
		if (lower.equals("going") || lower.equals("used") || MPLibEn.isHave(lower))
		{
			int nextId = node.getTokenId() + 1;
			
			if (nextId < tree.getTokens().size())
				return tree.getToken(nextId).isPTag(CTLibEn.POS_TO);
		}
		
		return false;
	}
	
	private void removeExistingPredicates(IntOpenHashSet[] sets, List<PBInstance> list)
	{
		for (PBInstance inst : list)
		{
			for (IntOpenHashSet set : sets)
				set.remove(inst.predId);
		}
	}
	
	private void addMissingPredicates(CTTree tree, EnglishMPAnalyzer morph, PBInstance fst, IntOpenHashSet set, List<PBInstance> list)
	{
		PBInstance inst;
		String lemma;
		CTNode node;
		int predId;
		PBArg arg;
		
		for (IntCursor cur : set)
		{
			predId = cur.value;
			node   = tree.getTerminal(predId);
			lemma  = morph.getLemma(node.form, node.pTag);
			if (lemma.equals("'s"))	lemma = "be";
			
			arg = new PBArg();
			arg.label = PBLib.PB_REL;
			arg.addLoc(new PBLoc(predId, 0));
			
			inst = new PBInstance();
			
			inst.treePath  = fst.treePath;
			inst.treeId    = fst.treeId;
			inst.predId    = cur.value;
			inst.annotator = "miss";
			inst.type      = lemma+"-v";
			inst.roleset   = lemma+".XX";
			inst.aspects   = "-----";
			inst.addArg(arg);
			
			list.add(inst);
		}
	}
	
	private void printPredicates(List<PBInstance> list, String outFile)
	{
		PrintStream fout = UTOutput.createPrintBufferedFileStream(outFile);
		Collections.sort(list);
		
		for (PBInstance inst : list)
			fout.println(inst.toString());
		
		fout.close();
	}
	
	static public void main(String[] args)
	{
		try
		{
			new PBFindMissingVerbs(args);
		}
		catch (Exception e) {e.printStackTrace();}
	}
}
