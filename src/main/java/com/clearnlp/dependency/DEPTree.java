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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntObjectOpenHashMap;
import com.carrotsearch.hppc.IntOpenHashSet;
import com.carrotsearch.hppc.cursors.IntCursor;
import com.clearnlp.coreference.Mention;
import com.clearnlp.dependency.factory.DefaultDEPNodeDatumFactory;
import com.clearnlp.dependency.factory.DefaultDEPTreeDatumFactory;
import com.clearnlp.dependency.factory.IDEPNodeDatum;
import com.clearnlp.dependency.factory.IDEPNodeDatumFactory;
import com.clearnlp.dependency.factory.IDEPTreeDatum;
import com.clearnlp.dependency.factory.IDEPTreeDatumFactory;
import com.clearnlp.dependency.srl.SRLArc;
import com.clearnlp.dependency.srl.SRLTree;
import com.clearnlp.reader.DEPReader;
import com.clearnlp.util.UTCollection;
import com.clearnlp.util.pair.IntIntPair;
import com.clearnlp.util.pair.StringIntPair;
import com.google.common.collect.Lists;


/**
 * Dependency tree.
 * See <a target="_blank" href="http://code.google.com/p/clearnlp/source/browse/trunk/src/edu/colorado/clear/test/dependency/DPTreeTest.java">DPTreeTest</a> for the use of this class.
 * @see DEPNode
 * @since v0.1
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class DEPTree extends ArrayList<DEPNode>
{
	private static final long serialVersionUID = -8007954222948953695L;
	private List<Mention> l_mentions;
	
	/**
	 * Constructs a dependency tree.
	 * An artificial root node gets inserted automatically.
	 */
	public DEPTree()
	{
		DEPNode root = new DEPNode();
		
		root.initRoot();
		add(root);
	}
	
	public void initXHeads()
	{
		int i, size = size();
		
		for (i=0; i<size; i++)
			get(i).x_heads = new ArrayList<DEPArc>();
	}
	
	public void initSHeads()
	{
		int i, size = size();
		
		for (i=0; i<size; i++)
			get(i).initSHeads();
	}
	
	/**
	 * Returns the dependency node with the specific ID.
	 * If there is no such node, returns {@code null}.
	 * @return the dependency node with the specific ID.
	 */
	public DEPNode get(int id)
	{
		try
		{
			return super.get(id);
		}
		catch (IndexOutOfBoundsException e)
		{
			return null;
		}
	}
	
	public DEPNode getFirstRoot()
	{
		DEPNode node, root = get(0);
		int i, size = size();
		
		for (i=1; i<size; i++)
		{
			node = get(i);
			
			if (node.getHead() == root)
				return node;
		}
		
		return null;
	}
	
	/** @return a list of root nodes in this tree. */
	public List<DEPNode> getRoots()
	{
		List<DEPNode> roots = new ArrayList<DEPNode>();
		DEPNode node, root = get(0);
		int i, size = size();
		
		for (i=1; i<size; i++)
		{
			node = get(i);
			
			if (node.getHead() == root)
				roots.add(node);
		}

		return roots;
	}
	
	public int getLeftValency(int id)
	{
		DEPNode node = get(id);
		int i, c = 0;
		
		for (i=node.id-1; i>0; i--)
		{
			if (get(i).getHead() == node)
				c++;
		}
		
		return c;
	}
	
	public int getRightValency(int id)
	{
		int i, c = 0, size = size();
		DEPNode node = get(id);
		
		for (i=node.id+1; i<size; i++)
		{
			if (get(i).getHead() == node)
				c++;
		}
		
		return c;
	}
	
	public DEPNode getLeftMostDependent(int id)
	{
		DEPNode node, head = get(id);
		int i;
		
		for (i=1; i<id; i++)
		{
			node = get(i);
			
			if (node.getHead() == head)
				return node;
		}
		
		return null;
	}
	
	public DEPNode getLeftMostDependent(int id, int order)
	{
		DEPNode node, head = get(id);
		int i;
		
		for (i=1; i<id; i++)
		{
			node = get(i);
			
			if (node.getHead() == head)
			{
				if (order == 0)	return node;
				order--;
			}
		}
		
		return null;
	}
	
	public DEPNode getRightMostDependent(int id)
	{
		DEPNode node, head = get(id);
		int i;
		
		for (i=size()-1; i>id; i--)
		{
			node = get(i);
			
			if (node.getHead() == head)
				return node;
		}
		
		return null;
	}
	
	public DEPNode getRightMostDependent(int id, int order)
	{
		DEPNode node, head = get(id);
		int i;
		
		for (i=size()-1; i>id; i--)
		{
			node = get(i);
			
			if (node.getHead() == head)
			{
				if (order == 0)	return node;
				order--;
			}
		}
		
		return null;
	}
	
	public DEPNode getLeftNearestSibling(int id)
	{
		DEPNode node, head = get(id).getHead();
		if (head == null)	return null;
		int i, eIdx = (head.id < id) ? head.id : 0;
		
		for (i=id-1; i>eIdx; i--)
		{
			node = get(i);
			
			if (node.getHead() == head)
				return node;
		}
		
		return null;
	}
	
	public DEPNode getRightNearestSibling(int id)
	{
		DEPNode node, head = get(id).getHead();
		if (head == null)	return null;
		int i, eIdx = (id < head.id) ? head.id : size();
		
		for (i=id+1; i<eIdx; i++)
		{
			node = get(i);
			
			if (node.getHead() == head)
				return node;
		}
		
		return null;
	}
	
	public DEPNode getNextPredicate(int prevId)
	{
		int i, size = size();
		DEPNode pred;
		
		for (i=prevId+1; i<size; i++)
		{
			pred = get(i);
			
			if (pred.getFeat(DEPLib.FEAT_PB) != null)
				return pred;
		}
		
		return null;
	}
	
	/** Removes the specific node from this tree. */
	public boolean removeNode(int index)
	{
		try
		{
			remove(index);
			reassignIDs(index);
			return true;
		}
		catch (IndexOutOfBoundsException e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	/** Inserts the specific node to this tree. */
	public boolean insertNode(int index, DEPNode node)
	{
		try
		{
			add(index, node);
			reassignIDs(index);
			return true;
		}
		catch (IndexOutOfBoundsException e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	/** @param bId beginning ID (inclusive). */
	private void reassignIDs(int bId)
	{
		int i;
		
		for (i=size()-1; i>=bId; i--)
			get(i).id = i;
	}
	
	public boolean containsPredicate()
	{
		int i, size = size();
		
		for (i=1; i<size; i++)
		{
			if (get(i).getFeat(DEPLib.FEAT_PB) != null)
				return true;
		}
		
		return false;
	}
	
	/** 
	 * Sets dependents of each node in this tree only if no dependent has ever been set before.
	 * If you are not sure if you have set any dependent or not, use {@link DEPTree#resetDependents()} instead.
	 */
	public void setDependents()
	{
		if (get(0).l_dependents != null)
			return;

		resetDependents();
	}
	
	/** Resets dependents of each node in this tree. */
	public void resetDependents()
	{
		int i, size = size();
		DEPNode node, head;
		
		for (i=0; i<size; i++)
			get(i).l_dependents = new ArrayList<DEPArc>();
		
		for (i=1; i<size; i++)
		{
			node = get(i);
			head = node.getHead();
			
			if (head != null)
				head.addDependent(node, node.getLabel());
		}
	}
	
	public void resetIDs()
	{
		int i, size = size();
		
		for (i=0; i<size; i++)
			get(i).id = i;
	}
	
	public List<List<DEPArc>> getArgumentList()
	{
		int i, size = size();
		List<DEPArc> args;
		DEPNode node;
		
		List<List<DEPArc>> list = new ArrayList<List<DEPArc>>();
		for (i=0; i<size; i++)	list.add(new ArrayList<DEPArc>());
		
		for (i=1; i<size; i++)
		{
			node = get(i);
			
			for (DEPArc arc : node.getSHeads())
			{
				args = list.get(arc.getNode().id);
				args.add(new DEPArc(node, arc.getLabel()));
			}
		}
		
		return list;
	}
	
	/**
	 * Returns {@code true} if this tree contains a cycle.
	 * @return {@code true} if this tree contains a cycle.
	 */
	public boolean containsCycle()
	{
		int i, size = size();
		DEPNode node;
		
		for (i=1; i<size; i++)
		{
			node = get(i);
			
			if (node.getHead().isDescendentOf(node))
				return true;
		}
		
		return false;
	}
	
	public List<Mention> getMentions()
	{
		return l_mentions;
	}
	
	public void setMentions(List<Mention> mentions)
	{
		l_mentions = mentions;
	}
	
	// --------------------------------- merge ---------------------------------
	
	@Deprecated
	public void merge(List<StringIntPair[]> lHeads)
	{
		int i, size = size();
			
		StringIntPair[]   H = new StringIntPair[size];
		List<DEPCountArc> F = new ArrayList<DEPCountArc>(); 
		IntOpenHashSet    T = new IntOpenHashSet();
		DEPCountArc a;
		
		StringIntPair[] t = lHeads.get(0);
			
		for (i=1; i<size; i++)
			H[i] = new StringIntPair(t[i].s, t[i].i);
			
		T.add(DEPLib.ROOT_ID);
		F.addAll(getArcs(lHeads, T));
			
		while (!F.isEmpty())
		{
			UTCollection.sortReverseOrder(F);
			a = F.get(0);
			
			H[a.depId].i = a.headId;
			H[a.depId].s = a.deprel;
		
			T.add(a.depId);
			removeArcs(F, a.depId);
			
			F.addAll(getArcs(lHeads, T));			
		}
			
		resetHeads(H);
	}
	
	@Deprecated
	private List<DEPCountArc> getArcs(List<StringIntPair[]> lHeads, IntOpenHashSet T)
	{
		Map<String,DEPCountArc> map = new HashMap<String,DEPCountArc>();
		int i, depId, len = size(), size = lHeads.size();
		DEPCountArc val;
		StringIntPair[] heads;
		StringIntPair head;
		String key;
		
		for (i=0; i<size; i++)
		{
			heads = lHeads.get(i);
			
			for (depId=1; depId<len; depId++)
			{
				head = heads[depId];
				
				if (head != null && T.contains(head.i) && !T.contains(depId))
				{
					key = depId+"_"+head.i+"_"+head.s;
					val = map.get(key);
						
					if (val == null)
					{
						val = new DEPCountArc(1, i, depId, head.i, head.s);
						map.put(key, val);
					}
					else
						val.count++;
					
					heads[depId] = null;	
				}
			}
		}
		
		return new ArrayList<DEPCountArc>(map.values());
	}
	
	private void removeArcs(List<DEPCountArc> F, int depId)
	{
		List<DEPCountArc> remove = new ArrayList<DEPCountArc>();
			
		for (DEPCountArc p : F)
		{
			if (p.depId == depId)
				remove.add(p);
		}
		
		F.removeAll(remove);
	}
	
	// --------------------------------- projectivize ---------------------------------
	
	public void projectivize()
	{
		IntArrayList ids = new IntArrayList();
		int i, size = size();
		DEPNode nonProj;
		
		for (i=1; i<size; i++)
			ids.add(i);
		
		while ((nonProj = getSmallestNonProjectiveArc(ids)) != null)
			nonProj.setHead(nonProj.getHead().getHead(), DEPLib.DEP_NON_PROJ);
	}
	
	/** Called by {@link DEPTree#projectivize()}. */
	private DEPNode getSmallestNonProjectiveArc(IntArrayList ids)
	{
		IntOpenHashSet remove = new IntOpenHashSet();
		DEPNode wk, nonProj = null;
		int np, max = 0;
		
		for (IntCursor cur : ids)
		{
			wk = get(cur.value);
			np = isNonProjective(wk);
			
			if (np == 0)
			{
				remove.add(cur.value);
			}
			else if (np > max)
			{
				nonProj = wk;
				max = np;
			}
		}
		
		ids.removeAll(remove);
		return nonProj;
	}
	
	/** @return > 0 if w_k is non-projective. */
	public int isNonProjective(DEPNode wk)
	{
		DEPNode wi = wk.getHead();
		if (wi == null) return 0;
		DEPNode wj;
		
		int bId, eId, j;

		if (wk.id < wi.id)
		{
			bId = wk.id;
			eId = wi.id;
		}
		else
		{
			bId = wi.id;
			eId = wk.id;
		}
		
		for (j=bId+1; j<eId; j++)
		{
			wj = get(j);
			
			if (!wj.isDescendentOf(wi))
				return Math.abs(wi.id - wk.id);
		}

		return 0;
	}
	
	// --------------------------------- clearGoldTags ---------------------------------
	
	public void clearPOSTags()
	{
		for (DEPNode node : this)
			node.pos = null;
	}
	
	public void clearHeads()
	{
		for (DEPNode node : this)
			node.d_head.clear();
	}
	
	public void clearXHeads()
	{
		int i, size = size();
		
		for (i=1; i<size; i++)
			get(i).x_heads.clear();
	}
	
	public void clearSHeads()
	{
		int i, size = size();
		
		for (i=1; i<size; i++)
			get(i).s_heads.clear();
	}
	
	public void clearPredicates()
	{
		for (DEPNode node : this)
			node.removeFeat(DEPLib.FEAT_PB);
	}
	
	// --------------------------------- reset ---------------------------------
	
	public void resetPOSTags(String[] tags)
	{
		int i, size = size();
		
		for (i=1; i<size; i++)
			get(i).pos = tags[i];
	}
	
	public void resetHeads(StringIntPair[] heads)
	{
		int i, size = size(), len = heads.length;
		StringIntPair head;
		DEPNode node;
		
		for (i=1; i<len && i<size; i++)
		{
			node = get(i);
			head = heads[i];
			
			if (head.i == DEPLib.NULL_ID)
				node.clearHead();
			else
				node.setHead(get(head.i), head.s);
		}
		
		for (i=len; i<size; i++)
			get(i).clearHead();
	}
	
	public StringIntPair[] getDiff(StringIntPair[] heads)
	{
		int i, size = size();
		DEPNode node, head;
		
		StringIntPair[] diff = new StringIntPair[size];
		StringIntPair p;
		
		for (i=1; i<size; i++)
		{
			node = get(i);
			head = node.getHead();
			p    = heads[i];
			
			if (head != null && head.id != p.i && !node.isLabel(p.s))
				diff[i] = new StringIntPair(node.getLabel(), head.id);
			else
				diff[i] = new StringIntPair(null, DEPLib.NULL_ID);
		}
		
		return diff;
	}
	
	public void appendHeads(StringIntPair[] heads)
	{
		int i, size = size();
		StringIntPair p;
		
		for (i=1; i<size; i++)
		{
			p = heads[i];
			
			if (p.i != DEPLib.NULL_ID)
				get(i).setHead(get(p.i), p.s);
		}
	}
	
	// --------------------------------- getGoldTags ---------------------------------
	
	public String[] getPOSTags()
	{
		int i, size = size();
		String[] tags = new String[size];
		
		for (i=1; i<size; i++)
			tags[i] = get(i).pos;
		
		return tags;
	}
	
	public void setPOSTags(String[] tags)
	{
		int i, size = size();
		
		for (i=1; i<size; i++)
			get(i).setPOSTag(tags[i]);
	}
	
	public StringIntPair[] getHeads()
	{
		return getHeads(size());
	}
	
	/** @param endIndex the ending index (exclusive). */
	public StringIntPair[] getHeads(int endIndex)
	{
		DEPArc head;
		int i;
		
		StringIntPair[] heads = new StringIntPair[endIndex];
		heads[0] = new StringIntPair(DEPLib.ROOT_TAG, DEPLib.NULL_ID);
		
		for (i=1; i<endIndex; i++)
		{
			head = get(i).d_head;
			heads[i] = (head.node != null) ? new StringIntPair(head.label, head.getNode().id) : new StringIntPair(null, DEPLib.NULL_ID);
		}
		
		return heads;
	}
	
	public String[] getSenses(String key)
	{
		int i, size = size();
		String[] senses = new String[size];
		
		for (i=1; i<size; i++)
			senses[i] = get(i).getFeat(key);
		
		return senses;
	}
	
	public String[] getRolesetIDs()
	{
		int i, size = size();
		String[] rolesets = new String[size];
		
		for (i=1; i<size; i++)
			rolesets[i] = get(i).getFeat(DEPLib.FEAT_PB);
		
		return rolesets;
	}
	
	public StringIntPair[][] getSHeads()
	{
		return getSHeadsAux();
	}
	
	private StringIntPair[][] getSHeadsAux()
	{
		int i, j, len, size = size();
		StringIntPair[] heads;
		List<SRLArc> arcs;
		SRLArc arc;
		
		StringIntPair[][] sHeads = new StringIntPair[size][];
		sHeads[0] = new StringIntPair[0];
		
		for (i=1; i<size; i++)
		{
			arcs  = get(i).getSHeads();
			len   = arcs.size();
			heads = new StringIntPair[len];
			
			for (j=0; j<len; j++)
			{
				arc = arcs.get(j);
				heads[j] = new StringIntPair(arc.label, arc.getNode().id);
			}
			
			sHeads[i] = heads;
		}
		
		return sHeads;
	}
	
	// --------------------------------- semantic heads ---------------------------------
	
	/**
	 * Returns a semantic tree representing a predicate-argument structure of the specific token.
	 * Returns {@code null} if the specific token is not a predicate.
	 * @param predId the token ID of a predicate.
	 * @return a semantic tree representing a predicate-argument structure of the specific token.
	 */
	public SRLTree getSRLTree(int predId)
	{
		DEPNode pred = get(predId);
		return getSRLTree(pred);
	}
	
	public SRLTree getSRLTree(DEPNode pred)
	{
		if (pred.getFeat(DEPLib.FEAT_PB) == null)
			return null;
		
		SRLTree tree = new SRLTree(pred);
		int i, size = size();
		DEPNode node;
		String label;
		
		for (i=1; i<size; i++)
		{
			node  = get(i);
			label = node.getSLabel(pred);
			
			if (label != null)
				tree.addArgument(node, label);
		}
		
		return tree;
	}
	
	// --------------------------------- toString ---------------------------------
	
	public String toString()
	{
		StringBuilder build = new StringBuilder();
		int i, size = size();
		
		for (i=1; i<size; i++)
		{
			build.append(DEPReader.DELIM_SENTENCE);
			build.append(get(i));
		}

		return build.substring(DEPReader.DELIM_SENTENCE.length());
	}
	
	public String toStringRaw()
	{
		StringBuilder build = new StringBuilder();
		int i, size = size();
		
		for (i=1; i<size; i++)
		{
			build.append(" ");
			build.append(get(i).form);
		}
		
		return build.substring(1);
	}
	
	public String toStringPOS()
	{
		StringBuilder build = new StringBuilder();
		int i, size = size();
		
		for (i=1; i<size; i++)
		{
			build.append(DEPReader.DELIM_SENTENCE);
			build.append(get(i).toStringPOS());
		}

		return build.substring(DEPReader.DELIM_SENTENCE.length());
	}
	
	public String toStringMorph()
	{
		StringBuilder build = new StringBuilder();
		int i, size = size();
		
		for (i=1; i<size; i++)
		{
			build.append(DEPReader.DELIM_SENTENCE);
			build.append(get(i).toStringMorph());
		}

		return build.substring(DEPReader.DELIM_SENTENCE.length());
	}
	
	public String toStringDEP()
	{
		StringBuilder build = new StringBuilder();
		int i, size = size();
		
		for (i=1; i<size; i++)
		{
			build.append(DEPReader.DELIM_SENTENCE);
			build.append(get(i).toStringDEP());
		}

		return build.substring(DEPReader.DELIM_SENTENCE.length());
	}
	
	public String toStringDAG()
	{
		StringBuilder build = new StringBuilder();
		int i, size = size();
		
		for (i=1; i<size; i++)
		{
			build.append(DEPReader.DELIM_SENTENCE);
			build.append(get(i).toStringDAG());
		}

		return build.substring(DEPReader.DELIM_SENTENCE.length());
	}
	
	public String toStringCoNLL()
	{
		StringBuilder build = new StringBuilder();
		int i, size = size();
		
		for (i=1; i<size; i++)
		{
			build.append(DEPReader.DELIM_SENTENCE);
			build.append(get(i).toStringCoNLL());
		}

		return build.substring(DEPReader.DELIM_SENTENCE.length());
	}
	
	public String toStringSRL()
	{
		StringBuilder build = new StringBuilder();
		int i, size = size();
		
		for (i=1; i<size; i++)
		{
			build.append(DEPReader.DELIM_SENTENCE);
			build.append(get(i).toStringSRL());
		}

		return build.substring(DEPReader.DELIM_SENTENCE.length());
	}
	
	public String getSentenceFromPA(int verbId, Pattern labels, String delim)
	{
		List<DEPNode> subs = new ArrayList<DEPNode>();
		StringBuilder build = new StringBuilder();
		DEPNode node, verb = get(verbId);
		int i, size = size();
		
		subs.add(verb);
		
		for (i=1; i<size; i++)
		{
			if (i != verbId)
			{
				node = get(i);
				
				if (node.isArgumentOf(verb, labels))
					subs.addAll(node.getSubNodeSet());				
			}
		}
		
		Collections.sort(subs);
		
		for (DEPNode sub : subs)
		{
			build.append(delim);
			build.append(sub.form);
		}
		
		return build.substring(delim.length());
	}
	
	public DEPTree clone()
	{
		IntObjectOpenHashMap<DEPNode> map = new IntObjectOpenHashMap<DEPNode>();
		DEPNode oNode, nNode, oHead, nHead;
		DEPTree tree = new DEPTree();
		int i, size = size();
		
		for (i=1; i<size; i++)
		{
			oNode = get(i);
			nNode = new DEPNode(oNode);
			tree.add(nNode);
			nNode.id = i;
			map.put(oNode.id, nNode);
			
			if (oNode.x_heads != null)
				nNode.initXHeads();
			
			if (oNode.s_heads != null)
				nNode.initSHeads();
		}
		
		for (i=1; i<size; i++)
		{
			oNode = get(i);
			nNode = tree.get(i);
			oHead = oNode.getHead();
			nHead = map.get(oHead.id);
			
			if (nHead == null)
			{
				nHead = tree.get(0);
			}
			else
			{
				if (oNode.x_heads != null)
				{
					for (DEPArc xHead : oNode.x_heads)
					{
						oHead = xHead.getNode();
						nNode.addXHead(map.get(oHead.id), xHead.getLabel());
					}				
				}
				
				if (oNode.s_heads != null)
				{
					for (DEPArc sHead : oNode.s_heads)
					{
						oHead = sHead.getNode();
						nNode.addSHead(map.get(oHead.id), sHead.getLabel());
					}				
				}
			}
			
			nNode.setHead(nHead, oNode.getLabel());
		}
		
		return tree;
	}
	
	public IDEPTreeDatum getDEPTreeDatum()
	{
		return getDEPTreeDatum(new DefaultDEPTreeDatumFactory(), new DefaultDEPNodeDatumFactory());
	}
	
	public IDEPTreeDatum getDEPTreeDatum(IDEPTreeDatumFactory treeFactory, IDEPNodeDatumFactory nodeFactory)
	{
		IDEPTreeDatum datum = treeFactory.createDEPTreeDatum();
		List<IDEPNodeDatum> nodeData = Lists.newArrayList();
		int i, size = size();
		DEPNode node;
		
		for (i=1; i<size; i++)
		{
			node = get(i);
			nodeData.add(node.getDEPNodeDatum(nodeFactory));
		}
		
		datum.setDEPNodeData(nodeData);
		return datum;
	}
	
	static public DEPTree buildFrom(IDEPTreeDatum treeDatum)
	{
		List<IDEPNodeDatum> nodeData = treeDatum.getDEPNodeData();
		DEPTree tree = new DEPTree();
		int i, size = nodeData.size();
		IDEPNodeDatum nd;
		DEPNode node;
		
		for (i=0; i<size; i++)
		{
			nd = nodeData.get(i);
			tree.add(new DEPNode(nd.getID(), nd.getForm(), nd.getLemma(), nd.getPOS(), nd.getNamedEntity(), new DEPFeat(nd.getFeats())));
		}

		for (i=0; i<size; i++)
		{
			nd = nodeData.get(i);
			node = tree.get(i+1);
			
			node.initSHeads();
			node.setHead(new DEPArc(tree, nd.getSyntacticHead()));
			node.addSHeads(DEPLib.getSRLArcs(tree, nd.getSemanticHeads()));
		}
		
		tree.resetDependents();
		return tree;
	}
	
	// --------------------------------- depredicated ---------------------------------
	
	@Deprecated
	public IntOpenHashSet getNonProjectiveSet()
	{
		IntObjectOpenHashMap<IntOpenHashSet> map = new IntObjectOpenHashMap<IntOpenHashSet>();
		int i, j, bIdx, eIdx, size = size();
		DEPNode curr, head, dep;
		
		for (i=1; i<size; i++)
		{
			curr = get(i);
			head = curr.getHead();
			
			if (curr.id < head.id)
			{
				bIdx = curr.id;
				eIdx = head.id;
			}
			else
			{
				bIdx = head.id;
				eIdx = curr.id;
			}
			
			for (j=bIdx+1; j<eIdx; j++)
			{
				curr = get(j);
				head = curr.getHead();
				
				if (head.id < bIdx || head.id > eIdx)
				{
					addNonProjectiveMap(map, i, j);
					addNonProjectiveMap(map, j, i);
				}

				for (DEPArc arc : curr.getDependents())
				{
					dep = arc.getNode();
					
					if (dep.id < bIdx || dep.id > eIdx)
					{
						addNonProjectiveMap(map, i, dep.id);
						addNonProjectiveMap(map, dep.id, i);						
					}
				}
			}
		}
		
		return getNonProjectiveMapAux(map);
	}
	
	@Deprecated
	private void addNonProjectiveMap(IntObjectOpenHashMap<IntOpenHashSet> map, int cIdx, int nIdx)
	{
		IntOpenHashSet set;
		
		if (map.containsKey(cIdx))
			set = map.get(cIdx);
		else
		{
			set = new IntOpenHashSet();
			map.put(cIdx, set);
		}
		
		set.add(nIdx);
	}
	
	@Deprecated
	private IntOpenHashSet getNonProjectiveMapAux(IntObjectOpenHashMap<IntOpenHashSet> map)
	{
		IntIntPair max = new IntIntPair(-1, -1);
		IntOpenHashSet set, remove;
		boolean removed;
		int[] keys;
		
		do
		{
			max.set(-1, -1);
			keys = map.keys().toArray();
			Arrays.sort(keys);
			
			for (int key : keys)
			{
				set = map.get(key);
				
				if (set.size() > max.i2)
					max.set(key, set.size());
			}
			
			removed = false;
			
			if (max.i2 > 0)
			{
				remove = new IntOpenHashSet();
				
				for (IntCursor cur : map.get(max.i1))
				{
					if (map.containsKey(cur.value))
					{
						set = map.get(cur.value);
						
						if (set.contains(max.i1))
						{
							removed = true;
							set.remove(max.i1);
							if (set.isEmpty())	remove.add(cur.value);
						}
					}
				}
				
				for (IntCursor cur : remove)
					map.remove(cur.value);
			}
		}
		while (removed);
						
		return new IntOpenHashSet(map.keys());
	}
}