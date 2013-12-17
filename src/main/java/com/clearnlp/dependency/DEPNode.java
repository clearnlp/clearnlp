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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import com.carrotsearch.hppc.IntOpenHashSet;
import com.clearnlp.constituent.CTLibEn;
import com.clearnlp.dependency.factory.DefaultDEPNodeDatumFactory;
import com.clearnlp.dependency.factory.IDEPNodeDatum;
import com.clearnlp.dependency.factory.IDEPNodeDatumFactory;
import com.clearnlp.dependency.srl.SRLArc;
import com.clearnlp.morphology.MPLibEn;
import com.clearnlp.ner.NERNode;
import com.clearnlp.pos.POSNode;
import com.clearnlp.reader.AbstractColumnReader;
import com.clearnlp.reader.DEPReader;
import com.google.common.collect.Lists;


/**
 * @since 1.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class DEPNode extends NERNode implements Comparable<DEPNode>
{
	/** The ID of this node (default: {@link DEPLib#NULL_ID}). */
	public   int           id;
	/** The extra features of this node (default: empty). */
	protected DEPFeat      d_feats;
	/** The dependency head of this node (default: empty). */
	protected DEPArc       d_head;
	/** The list of secondary heads of this node (default: empty). */
	protected List<DEPArc> x_heads;
	/** The list of semantic heads of this node (default: empty). */
	protected List<SRLArc> s_heads;
	/** The sorted list of all dependents of this node (default: empty). */
	protected List<DEPArc> l_dependents;
	
	//	====================================== CONSTRUCTOR ======================================
	
	public DEPNode()
	{
		super();
	}
	
	public DEPNode(int id, String form)
	{
		init(id, form, null, null, new DEPFeat());
	}
	
	public DEPNode(int id, POSNode node)
	{
		init(id, node.form, node.lemma, node.pos, new DEPFeat());
	}
	
	public DEPNode(int id, String form, String lemma, String pos, DEPFeat feats)
	{
		init(id, form, lemma, pos, feats);
	}
	
	public DEPNode(int id, String form, String lemma, String pos, String nament, DEPFeat feats)
	{
		init(id, form, lemma, pos, nament, feats);
	}
	
	public DEPNode(DEPNode node)
	{
		copy(node);
	}
	
	//	====================================== INITIALIZATION ======================================
	
	/** Initializes this node as an artificial root node. */
	public void initRoot()
	{
		init(DEPLib.ROOT_ID, DEPLib.ROOT_TAG, DEPLib.ROOT_TAG, DEPLib.ROOT_TAG, new DEPFeat());
	}
	
	/**
	 * Initializes the values of this node.
	 * @param id the ID of this node.
	 * @param form the word-form of this node.
	 * @param lemma the lemma of the word-form.
	 * @param pos the part-of-speech tag of this node.
	 * @param feats the extra features of this node.
	 */
	public void init(int id, String form, String lemma, String pos, DEPFeat feats)
	{
		this.id      = id;
		this.form    = form;
		this.lemma   = lemma;
		this.pos     = pos;
		this.nament  = AbstractColumnReader.BLANK_COLUMN;
		this.d_feats = feats;
		this.d_head  = new DEPArc();
	}
	
	public void init(int id, String form, String lemma, String pos, String nament, DEPFeat feats)
	{
		this.id      = id;
		this.form    = form;
		this.lemma   = lemma;
		this.pos     = pos;
		this.nament  = nament;
		this.d_feats = feats;
		this.d_head  = new DEPArc();
	}

	/** Initializes semantic heads of this node. */
	public void initSHeads()
	{
		s_heads = new ArrayList<SRLArc>();
	}
	
	public void copy(DEPNode node)
	{
		init(node.id, node.form, node.lemma, node.pos, node.nament, (DEPFeat)node.d_feats.clone());
	}
	
	//	====================================== FEATS ======================================

	/** @return the value of the specific feature if exists; otherwise, {@code null}. */
	public String getFeat(String key)
	{
		return d_feats.get(key);
	}
	
	public DEPFeat getFeats()
	{
		return d_feats;
	}
	
	/**
	 * Puts an extra feature to this node using the specific key and value.
	 * This method overwrites an existing value of the same key with the current value. 
	 */
	public void addFeat(String key, String value)
	{
		d_feats.put(key, value);
	}
	
	public void setFeats(DEPFeat feats)
	{
		d_feats = feats;
	}
	
	/** Removes the feature with the specific key. */
	public String removeFeat(String key)
	{
		return d_feats.remove(key);
	}
	
	//	====================================== DEPENDENCY LABEL ======================================
	
	/**
	 * Returns the dependency label of this node to its head. 
	 * @return the dependency label of this node to its head.
	 */
	public String getLabel()
	{
		return d_head.label;
	}
	
	/**
	 * Sets the dependency label of this node to the specific label.
	 * @param label the dependency label to be assigned.
	 */
	public void setLabel(String label)
	{
		d_head.setLabel(label);
	}
	
	/**
	 * Returns {@code true} if the dependency label of this node equals to the specific label.
	 * @param label the dependency label to be compared.
	 * @return {@code true} if the dependency label of this node equals to the specific label.
	 */
	public boolean isLabel(String label)
	{
		return d_head.label != null && d_head.isLabel(label);
	}
	
	public boolean isLabel(Pattern regex)
	{
		return d_head.label != null && d_head.isLabel(regex);
	}
	
	//	====================================== BOOLEAN ======================================
	
	/** @return {@code true} if this node is a dependent of an artificial root. */
	public boolean isRoot()
	{
		DEPNode head = getHead();
		return head != null && head.id == DEPLib.ROOT_ID;
	}
	
	public DEPArc getHeadArc()
	{
		return d_head;
	}
	
	/**
	 * Returns the dependency head of this node.
	 * If the head does not exists, returns {@code null}.
	 * @return the dependency head of this node.
	 */
	public DEPNode getHead()
	{
		return d_head.node;
	}
	
	public void setHead(DEPArc arc)
	{
		d_head = arc;
	}
	
	public void setHead(DEPNode head)
	{
		d_head.setNode(head);
	}
	
	/**
	 * Sets the dependency head and label of this node to the specific node and label.
	 * If the head already exists, replaces the head with the specific node.
	 * @param headId the ID of the head.
	 * @param label the dependency label to the head.
	 */
	public void setHead(DEPNode head, String label)
	{
		d_head.set(head, label);
	}
	
	/**
	 * Returns {@code true} if this node has a dependency head.
	 * @return {@code true} if this node has a dependency head.
	 */
	public boolean hasHead()
	{
		return d_head.node != null;
	}
	
	public void clearHead()
	{
		d_head.clear();
	}
	
	public void clearDependents()
	{
		l_dependents.clear();
	}
	
	public DEPNode getGrandHead()
	{
		DEPNode head = getHead();
		return (head == null) ? null : head.getHead();
	}
	
	/**
	 * Returns {@code true} if this node is a dependent of the specific node.
	 * @param node the potential head.
	 * @return {@code true} if this node is a dependent of the specific node.
	 */
	public boolean isDependentOf(DEPNode node)
	{
		return d_head.isNode(node);
	}
	
	public boolean isDependentOf(DEPNode node, String label)
	{
		return d_head.isNode(node) && d_head.isLabel(label);
	}
	
	/**
	 * Returns {@code true} if this node is a descendant of the specific node. 
	 * @param node the potential ancestor.
	 * @return {@code true} if this node is a descendant of the specific node.
	 */
	public boolean isDescendentOf(DEPNode node)
	{
		DEPNode head = getHead();
		
		while (head != null)
		{
			if (head == node)	return true;
			head = head.getHead();
		}
		
		return false;
	}
	
	public String getSLabel(DEPNode sHead)
	{
		for (SRLArc arc : s_heads)
		{
			if (arc.isNode(sHead))
				return arc.label;
		}
		
		return null;
	}
	
	public boolean isSiblingOf(DEPNode node)
	{
		return node.isDependentOf(getHead());
	}
	
	public boolean hasSHead()
	{
		return s_heads != null && !s_heads.isEmpty();
	}
	
	public SRLArc getSHead(DEPNode head)
	{
		for (SRLArc arc : s_heads)
		{
			if (arc.isNode(head))
				return arc;
		}
		
		return null;
	}
	
	public SRLArc getSHead(DEPNode head, Pattern labels)
	{
		for (SRLArc arc : s_heads)
		{
			if (arc.isNode(head) && arc.isLabel(labels))
				return arc;
		}
		
		return null;
	}
	
	public List<SRLArc> getSHeads()
	{
		return s_heads;
	}
	
	public void setSHeads(List<SRLArc> sHeads)
	{
		s_heads = sHeads;
	}
	
	public List<SRLArc> getSHeadsByLabel(String label)
	{
		List<SRLArc> sHeads = Lists.newArrayList();
		
		for (SRLArc arc : s_heads)
		{
			if (arc.isLabel(label))
				sHeads.add(arc);
		}
		
		return sHeads;
	}
	
	public SRLArc getFirstSHead(DEPNode head, Pattern label)
	{
		for (SRLArc arc : s_heads)
		{
			if (arc.isNode(head) && arc.isLabel(label))
				return arc;
		}
		
		return null;
	}
	
	public void addSHeads(Collection<SRLArc> arcs)
	{
		s_heads.addAll(arcs);
	}
	
	public void addSHead(SRLArc arc)
	{
		s_heads.add(arc);
	}
	
	public void addSHead(DEPNode head, String label)
	{
		s_heads.add(new SRLArc(head, label));
	}
	
	public void addSHead(DEPNode head, String label, String functionTag)
	{
		s_heads.add(new SRLArc(head, label, functionTag));
	}
	
	public boolean removeSHead(DEPNode head)
	{
		for (SRLArc arc : s_heads)
		{
			if (arc.isNode(head))
				return s_heads.remove(arc);
		}
		
		return false;
	}
	
	public void removeSHead(SRLArc sHead)
	{
		s_heads.remove(sHead);
	}
	
	public void removeSHeads(Collection<SRLArc> sHeads)
	{
		s_heads.removeAll(sHeads);
	}
	
	public void removeSHeadsByLabel(String label)
	{
		s_heads.removeAll(getSHeadsByLabel(label));
	}
	
	public boolean containsSHead(DEPNode sHead)
	{
		for (SRLArc arc : s_heads)
		{
			if (arc.isNode(sHead))
				return true;
		}
		
		return false;
	}
	
	public boolean containsSHead(String label)
	{
		for (SRLArc arc : s_heads)
		{
			if (arc.isLabel(label))
				return true;
		}
		
		return false;
	}
	
	public boolean containsSHead(Pattern regex)
	{
		for (SRLArc arc : s_heads)
		{
			if (regex.matcher(arc.getLabel()).find())
				return true;
		}
		
		return false;
	}
	
	public boolean containsSHead(DEPNode sHead, Pattern p)
	{
		for (SRLArc arc : s_heads)
		{
			if (arc.isNode(sHead) && arc.isLabel(p))
				return true;
		}
		
		return false;
	}
	
	public boolean isArgumentOf(DEPNode sHead)
	{
		for (SRLArc arc : s_heads)
		{
			if (arc.isNode(sHead))
				return true;
		}
		
		return false;
	}
	
	public boolean isArgumentOf(DEPNode sHead, String label)
	{
		for (SRLArc arc : s_heads)
		{
			if (arc.isNode(sHead) && arc.isLabel(label))
				return true;
		}
		
		return false;
	}
	
	public boolean isArgumentOf(DEPNode sHead, Pattern regex)
	{
		for (SRLArc arc : s_heads)
		{
			if (arc.isNode(sHead) && regex.matcher(arc.getLabel()).find())
				return true;
		}
		
		return false;
	}
	
	public List<DEPArc> getDependents()
	{
		return l_dependents;
	}
	
	public List<DEPNode> getDependentNodeList()
	{
		List<DEPNode> list = new ArrayList<DEPNode>();
		
		for (DEPArc arc : l_dependents)
			list.add(arc.getNode());
		
		return list;
	}
	
	public DEPNode getFirstNode()
	{
		return getFirstNodeAux(this);
	}
	
	public DEPNode getFirstNodeAux(DEPNode node)
	{
		List<DEPArc> deps = node.getDependents();
		if (deps.isEmpty())	return node;
		
		DEPNode dep = getFirstNodeAux(deps.get(0).getNode());
		return (dep.id > node.id) ? node : dep;
	}
	
	/** @return the last node in the subtree (inclusive). */
	public DEPNode getLastNode()
	{
		return getLastNodeAux(this);
	}
	
	private DEPNode getLastNodeAux(DEPNode node)
	{
		List<DEPArc> deps = node.getDependents();
		if (deps.isEmpty())	return node;
		
		DEPNode dep = getLastNodeAux(deps.get(deps.size()-1).getNode());
		return (dep.id < node.id) ? node : dep;
	}
	
	public void initDependents()
	{
		l_dependents = new ArrayList<DEPArc>();
	}
	
	public void addDependentFront(DEPArc arc)
	{
		l_dependents.add(0, arc);
	}
	
	public void addDependent(DEPArc arc)
	{
		l_dependents.add(arc);
	}
	
	public boolean removeDependent(DEPArc arc)
	{
		return l_dependents.remove(arc);
	}
	
	public boolean removeDependents(Collection<DEPArc> arcs)
	{
		return l_dependents.removeAll(arcs);
	}
	
	public void removeFirstDependentByLabel(String label)
	{
		for (DEPArc arc : l_dependents)
		{
			if (arc.isLabel(label))
			{
				l_dependents.remove(arc);
				break;
			}
		}
	}
	
	public void removeDependentsByLabels(Pattern regex)
	{
		List<DEPArc> remove = new ArrayList<DEPArc>();
		
		for (DEPArc arc : l_dependents)
		{
			if (arc.isLabel(regex))
				remove.add(arc);
		}
		
		l_dependents.removeAll(remove);
	}
	
	public void addDependentRightNextToSelf(DEPArc dep)
	{
		int i, size = l_dependents.size();
		boolean added = false;
		DEPArc arc;
		
		for (i=0; i<size; i++)
		{
			arc = l_dependents.get(i);
			
			if (arc.getNode().id > id)
			{
				l_dependents.add(i, dep);
				added = true;
				break;
			}
		}
		
		if (!added) l_dependents.add(dep);
	}
	
	
	public List<DEPNode> getLeftDependents()
	{
		List<DEPNode> list = new ArrayList<DEPNode>();
		int i, size = l_dependents.size();
		DEPArc arc;
		
		for (i=0; i<size; i++)
		{
			arc = l_dependents.get(i);
			
			if (arc.getNode().id > id)
				break;
			
			list.add(arc.getNode());
		}
		
		return list;
	}
	
	public List<DEPNode> getRightDependents()
	{
		List<DEPNode> list = new ArrayList<DEPNode>();
		int i;
		DEPArc arc;
		
		for (i=l_dependents.size()-1; i>=0; i--)
		{
			arc = l_dependents.get(i);
			
			if (arc.getNode().id < id)
				break;
			
			list.add(arc.getNode());
		}
		
		return list;
	}
	
	public List<DEPArc> getGrandDependents()
	{
		List<DEPArc> list = new ArrayList<DEPArc>();
		
		for (DEPArc arc : l_dependents)
			list.addAll(arc.getNode().getDependents());
	
		return list;
	}
	
	public DEPNode getLeftNearestDependent()
	{
		DEPArc arc;
		int i;
		
		for (i=l_dependents.size()-1; i>=0; i--)
		{
			arc = l_dependents.get(i);
			
			if (arc.getNode().id < id)
				return arc.getNode();
		}
		
		return null;
	}
	
	public DEPNode getRightNearestDependent()
	{
		int i, size = l_dependents.size();
		DEPArc arc;
		
		for (i=0; i<size; i++)
		{
			arc = l_dependents.get(i);
			
			if (arc.getNode().id > id)
				return arc.getNode();
		}
		
		return null;
	}
	
	public DEPNode getLeftMostDependent()
	{
		DEPNode dep;
		
		if (!l_dependents.isEmpty())
		{
			dep = l_dependents.get(0).getNode();
			if (dep.id < id)	return dep;
		}

		return null;
	}
	
	public DEPNode getRightMostDependent()
	{
		DEPNode dep;
		
		if (!l_dependents.isEmpty())
		{
			dep = l_dependents.get(l_dependents.size()-1).getNode();
			if (dep.id > id)	return dep;
		}

		return null;
	}
	
	/** @return a list of descendents with the specific labels. */
	public List<DEPNode> getDescendents(Pattern regex)
	{
		List<DEPNode> desc = new ArrayList<DEPNode>();
		getDescendentsAux(this, desc, regex);
		
		return desc;
	}
	
	public void getDescendentsAux(DEPNode node, List<DEPNode> desc, Pattern regex)
	{
		for (DEPArc arc : node.getDependents())
		{
			if (arc.isLabel(regex))
			{
				getDescendentsAux(arc.getNode(), desc, regex);
				desc.add(arc.getNode());
			}
		}
	}
	
	/** @param depth > 0. */
	public List<DEPArc> getDescendents(int depth)
	{
		List<DEPArc> list = new ArrayList<DEPArc>();
		
		getDescendentsAux(this, list, depth-1);
		return list;
	}
	
	private void getDescendentsAux(DEPNode curr, List<DEPArc> list, int depth)
	{
		List<DEPArc> deps = curr.getDependents();
		list.addAll(deps);
		
		if (depth == 0)	return;
		
		for (DEPArc arc : deps)
			getDescendentsAux(arc.getNode(), list, depth-1);
	}
	
	public Set<DEPNode> getArgumentCandidateSet(int depth, boolean includeSelf)
	{
		Set<DEPNode> set = new HashSet<DEPNode>();
		
		for (DEPArc arc : getDescendents(depth))
			set.add(arc.getNode());
		
		DEPNode head = getHead();
		
		while (head != null)
		{
			set.add(head);
			
			for (DEPArc arc : head.getDependents())
				set.add(arc.getNode());
						
			head = head.getHead();
		}
		
	/*	if (head != null)
		{
			for (DEPArc arc : head.getGrandDependents())
				set.add(arc.getNode());
					
			do
			{
				for (DEPArc arc : head.getDependents())
					set.add(arc.getNode());
							
				head = head.getHead();
			}
			while (head != null);
		}*/
		
		if (includeSelf)	set.add   (this);
		else				set.remove(this);
		
		return set;
	}
	
	public DEPArc getAnyDescendentArcByPOS(String label)
	{
		return getAnyDescendentArcByPOSAux(this, label);
	}
	
	private DEPArc getAnyDescendentArcByPOSAux(DEPNode node, String pos)
	{
		DEPNode dep;
		
		for (DEPArc arc : node.getDependents())
		{
			dep = arc.getNode();
			if (dep.isPos(pos)) return arc;
			
			arc = getAnyDescendentArcByPOSAux(dep, pos);
			if (arc != null) return arc;
		}
		
		return null;
	}
	
	public List<DEPNode> getPreviousDependentsExcluding(Pattern exclude)
	{
		List<DEPNode> deps = new ArrayList<DEPNode>();
		DEPNode dep;
		
		for (DEPArc arc : l_dependents)
		{
			dep = arc.getNode();
			
			if (dep.id > id)
				break;
			
			if (!dep.isLabel(exclude))
				deps.add(dep);
		}
		
		return deps;
	}
	
	public List<DEPNode> getNextDependentsExcluding(Pattern exclude)
	{
		List<DEPNode> deps = new ArrayList<DEPNode>();
		DEPNode dep;
		
		for (DEPArc arc : l_dependents)
		{
			dep = arc.getNode();
			
			if (dep.id < id)
				continue;
			
			if (!dep.isLabel(exclude))
				deps.add(dep);
		}
		
		return deps;
	}
	
	public DEPNode getFirstDependentByLabel(String label)
	{
		for (DEPArc arc : l_dependents)
		{
			if (arc.isLabel(label))
				return arc.getNode();
		}
		
		return null;
	}
	
	public DEPNode getFirstDependentByLabel(Pattern p)
	{
		for (DEPArc arc : l_dependents)
		{
			if (p.matcher(arc.getLabel()).find())
				return arc.getNode();
		}
		
		return null;
	}
	
	public List<DEPNode> getDependentsByLabels(Pattern regex)
	{
		List<DEPNode> list = new ArrayList<DEPNode>();
		
		for (DEPArc arc : l_dependents)
		{
			if (arc.isLabel(regex))
				list.add(arc.getNode());
		}
		
		return list;
	}
	
	public List<DEPNode> getDependentsByLabels(String... labels)
	{
		List<DEPNode> list = new ArrayList<DEPNode>();
		
		for (DEPArc arc : l_dependents)
		{
			for (String label : labels)
			{
				if (arc.isLabel(label))
					list.add(arc.getNode());
			}
		}
		
		return list;
	}
	
	void addDependent(DEPNode node, String label)
	{
		l_dependents.add(new DEPArc(node, label));
	}
	
	void removeDependent(DEPNode node)
	{
		l_dependents.remove(node);
	}
	
	public boolean containsDependent(String label)
	{
		for (DEPArc node : l_dependents)
		{
			if (node.isLabel(label))
				return true;
		}
		
		return false;
	}
	
	/** @return a set of nodes in the subtree of this node (inclusive). */
	public Set<DEPNode> getSubNodeSet()
	{
		Set<DEPNode> set = new HashSet<DEPNode>();
		
		getSubNodeCollectionAux(set, this);
		return set;
	}
	
	/** @return a sorted list of nodes in the subtree of this node (inclusive). */
	public List<DEPNode> getSubNodeSortedList()
	{
		List<DEPNode> list = new ArrayList<DEPNode>();
		
		getSubNodeCollectionAux(list, this);
		Collections.sort(list);
		
		return list;
	}
	
	private void getSubNodeCollectionAux(Collection<DEPNode> col, DEPNode curr)
	{
		col.add(curr);
		
		for (DEPArc arc : curr.getDependents())
			getSubNodeCollectionAux(col, arc.getNode());
	}
	
	public IntOpenHashSet getSubIdSet()
	{
		IntOpenHashSet set = new IntOpenHashSet();
		
		getSubIdSetAux(set, this);
		return set;
	}

	private void getSubIdSetAux(IntOpenHashSet set, DEPNode curr)
	{
		set.add(curr.id);
		
		for (DEPArc arc : curr.getDependents())
			getSubIdSetAux(set, arc.getNode());
	}
	
	/**
	 * Returns an array of IDs from the subtree of this node, including the ID of this node.
	 * The array is sorted in ascending order.
	 * @return an array of IDs from the subtree of this node, including the ID of this node.
	 */
	public int[] getSubIdArray()
	{
		IntOpenHashSet set = getSubIdSet();
		int[] list = set.toArray();
		Arrays.sort(list);
		
		return list;
	}
	
	public String toStringPOS()
	{
		StringBuilder build = new StringBuilder();
		
		build.append(form);		build.append(DEPReader.DELIM_COLUMN);
		build.append(pos);
		
		if (d_feats != null)
		{
			build.append(DEPReader.DELIM_COLUMN);
			build.append(d_feats);			
		}
		
		return build.toString();
	}
	
	public String toStringMorph()
	{
		StringBuilder build = new StringBuilder();
		
		build.append(form);		build.append(DEPReader.DELIM_COLUMN);
		build.append(lemma);	build.append(DEPReader.DELIM_COLUMN);
		build.append(pos);
		
		if (d_feats != null)
		{
			build.append(DEPReader.DELIM_COLUMN);
			build.append(d_feats);			
		}
		
		return build.toString();
	}
	
	public String toStringDEP()
	{
		StringBuilder build = new StringBuilder();
		
		build.append(id);		build.append(DEPReader.DELIM_COLUMN);
		build.append(form);		build.append(DEPReader.DELIM_COLUMN);
		build.append(lemma);	build.append(DEPReader.DELIM_COLUMN);
		build.append(pos);		build.append(DEPReader.DELIM_COLUMN);
		build.append(d_feats);	build.append(DEPReader.DELIM_COLUMN);
		
		if (hasHead())
		{
			build.append(d_head.node.id);	build.append(DEPReader.DELIM_COLUMN);
			build.append(d_head.label);
		}
		else
		{
			build.append(AbstractColumnReader.BLANK_COLUMN);	build.append(DEPReader.DELIM_COLUMN);
			build.append(AbstractColumnReader.BLANK_COLUMN);
		}
		
		return build.toString();
	}
	
	public String toStringCoNLL()
	{
		StringBuilder build = new StringBuilder();
		
		build.append(id);						build.append(DEPReader.DELIM_COLUMN);
		build.append(form);						build.append(DEPReader.DELIM_COLUMN);
		build.append(lemma);					build.append(DEPReader.DELIM_COLUMN);
		build.append(pos);						build.append(DEPReader.DELIM_COLUMN);
		build.append(pos);						build.append(DEPReader.DELIM_COLUMN);
		build.append(DEPReader.BLANK_COLUMN);	build.append(DEPReader.DELIM_COLUMN);
		
		if (hasHead())
		{
			build.append(d_head.node.id);	build.append(DEPReader.DELIM_COLUMN);
			build.append(d_head.label);
		}
		else
		{
			build.append(AbstractColumnReader.BLANK_COLUMN);	build.append(DEPReader.DELIM_COLUMN);
			build.append(AbstractColumnReader.BLANK_COLUMN);
		}
		
		return build.toString();
	}
	
	public String toStringDAG()
	{
		StringBuilder build = new StringBuilder();
		
		build.append(toStringDEP());		build.append(DEPReader.DELIM_COLUMN);
		build.append(DEPLib.toString(x_heads));
		
		return build.toString();
	}
	
	public String toStringSRL()
	{
		StringBuilder build = new StringBuilder();
		
		build.append(toStringDEP());		build.append(DEPReader.DELIM_COLUMN);
		build.append(DEPLib.toString(s_heads));
		
		return build.toString();
	}
		
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		StringBuilder build = new StringBuilder();
		
		build.append(toStringDEP());		build.append(DEPReader.DELIM_COLUMN);
		build.append(DEPLib.toString(x_heads));	build.append(DEPReader.DELIM_COLUMN);
		build.append(DEPLib.toString(s_heads));	build.append(DEPReader.DELIM_COLUMN);
		build.append(nament);
		
		return build.toString();
	}
	
	public String getSubForms(String delim)
	{
		StringBuilder build = new StringBuilder();
		
		for (DEPNode node : getSubNodeSortedList())
		{
			build.append(delim);
			build.append(node.form);
		}
		
		return build.substring(delim.length());
	}
	
	/** @return a sequence of lemmas for nouns (used for a very specific purpose).  */
	public String getSubLemmasEnNoun(String delim)
	{
		StringBuilder build = new StringBuilder();
		boolean add = true;
		DEPNode dep;
		
		for (DEPArc arc : getDependents())
		{
			dep = arc.getNode();
			
			if (add && dep.id > id)
			{
				build.append(delim);
				build.append(lemma);
				add = false;
			}
			
			if (arc.isLabel(DEPLibEn.DEP_NN) || dep.isPos(CTLibEn.POS_PRPS))
			{
				build.append(delim);
				build.append(dep.lemma);
			}
		}
		
		if (add)
		{
			build.append(delim);
			build.append(lemma);
		}
		
		return build.substring(delim.length());
	}
	
	public String getSubLemmasEnPP(String delim)
	{
		StringBuilder build = new StringBuilder();
		build.append(lemma);

		DEPNode pobj = getFirstDependentByLabel(DEPLibEn.DEP_POBJ);
		
		if (pobj != null)
		{
			build.append(delim);
			
			if (MPLibEn.isNoun(pobj.pos))
				build.append(pobj.getSubLemmasEnNoun(delim));
			else
				build.append(pobj.lemma);
		}
		
		return build.toString();
	}

	@Override
	public int compareTo(DEPNode node)
	{
		return id - node.id;
	}
	
	
	
	
	
	
	
	
	
	
	/**
	 * Returns secondary heads of this node.
	 * @return secondary heads of this node.
	 */
	public List<DEPArc> getXHeads()
	{
		return x_heads;
	}
	
	public Set<DEPNode> getXAncestorSet()
	{
		Set<DEPNode> set = new HashSet<DEPNode>();
		
		getXAncestorIdSetAux(this, set);
		return set;
	}
	
	private void getXAncestorIdSetAux(DEPNode node, Set<DEPNode> set)
	{
		DEPNode head;
		
		for (DEPArc arc : node.x_heads)
		{
			head = arc.getNode();
			
			set.add(head);
			getXAncestorIdSetAux(head, set);
		}
	}
	
	public DEPArc getXHead(DEPNode head)
	{
		for (DEPArc arc : x_heads)
		{
			if (arc.isNode(head))
				return arc;
		}
		
		return null;
	}
	
	public List<DEPArc> getXHeads(String label)
	{
		List<DEPArc> list = new ArrayList<DEPArc>();
		
		for (DEPArc arc : x_heads)
		{
			if (arc.isLabel(label))
				list.add(arc);
		}
		
		return list;
	}
	
	public void addXHead(DEPNode head, String label)
	{
		x_heads.add(new DEPArc(head, label));
	}
	
	public boolean hasXHead()
	{
		return !x_heads.isEmpty();
	}
	
	public boolean isXDescendentOf(DEPNode node)
	{
		return isXDescendentOfAux(this, node);
	}
	
	private boolean isXDescendentOfAux(DEPNode curr, DEPNode node)
	{
		for (DEPArc arc : curr.x_heads)
		{
			if (arc.isNode(node) || isXDescendentOfAux(arc.getNode(), node))
				return true;
		}
		
		return false;
	}
	
	public boolean containsXHead(DEPNode xHead)
	{
		for (DEPArc arc : x_heads)
		{
			if (arc.isNode(xHead))
				return true;
		}
		
		return false;
	}
	
	public void initXHeads()
	{
		x_heads = new ArrayList<DEPArc>();
	}
	
	public void setXHeads(List<DEPArc> xHeads)
	{
		x_heads = xHeads;
	}
	
	public IDEPNodeDatum getDEPNodeDatum()
	{
		return getDEPNodeDatum(new DefaultDEPNodeDatumFactory());
	}
	
	public IDEPNodeDatum getDEPNodeDatum(IDEPNodeDatumFactory factory)
	{
		IDEPNodeDatum datum = factory.createDEPTreeDatum();
		
		datum.setID(id);
		datum.setForm(form);
		datum.setLemma(lemma);
		datum.setPOS(pos);
		datum.setNamedEntity(nament);
		datum.setFeats(d_feats.toString());
		datum.setSyntacticHead(d_head.toString());
		datum.setSemanticHeads(DEPLib.toString(s_heads));
		
		return datum;
	}
	
/*	protected void addChild(DPNode child)
	{
		int idx = Collections.binarySearch(l_dependents, child);
		if (idx < 0)	l_dependents.add(-idx-1, child);
	}
	
	public String getXPath(DEPNode node, int flag)
	{
		StringIntPair path = getXPathUp(node, flag);
		if (path.s != null)	return path.s;
		
		path = getXPathDown(node, flag);
		if (path.s != null)	return path.s;
	
		getXPathAux(this, node, node.getXAncestorSet(), "", path, 0, flag);
		return path.s;
	}
	
	public StringIntPair getXPathUp(DEPNode node, int flag)
	{
		StringIntPair path = new StringIntPair(null, Integer.MAX_VALUE);

		getXPathUpAux(this, node, "", path, 0, flag, DEPLib.DELIM_PATH_UP);
		return path;
	}
	
	public StringIntPair getXPathDown(DEPNode node, int flag)
	{
		StringIntPair path = new StringIntPair(null, Integer.MAX_VALUE);

		getXPathUpAux(node, this, "", path, 0, flag, DEPLib.DELIM_PATH_DOWN);
		return path;
	}
	
	private String getXPathAux(DEPNode source, DEPNode target, Set<DEPNode> targetAncestors, String prevPath, StringIntPair path, int height, int flag)
	{
		String currPath = (flag == 0) ? prevPath + DEPLib.DELIM_PATH_DOWN + source.pos : prevPath + DEPLib.DELIM_PATH_DOWN;
		DEPNode head;
		
		for (DEPArc arc : source.x_heads)
		{
			head = arc.getNode();
			
			if (targetAncestors.contains(head))
			{
				StringIntPair sPath = target.getXPathUp(head, flag);
				
				if (height < path.i)
				{
					switch (flag)
					{
					case 0: path.set(sPath.s + currPath, sPath.i + height); break;
					case 1: path.set(sPath.s + currPath + arc.label, sPath.i + height); break;
					case 2: path.set(sPath.s + DEPLib.DELIM_PATH_DOWN + height, sPath.i + height);
					}
				}
				
				break;
			}
			else
			{
				if (flag == 0)	getXPathAux(head, target, targetAncestors, currPath, path, height+1, flag);
				else			getXPathAux(head, target, targetAncestors, currPath + arc.label, path, height+1, flag);
			}
		}
		
		return null;
	}
	
	private void getXPathUpAux(DEPNode curr, DEPNode head, String prevPath, StringIntPair path, int height, int flag, String delim)
	{
		String currPath = (flag == 0) ? prevPath + delim + curr.pos : prevPath + delim;
		
		for (DEPArc arc : curr.x_heads)
		{
			if (arc.isNode(head))
			{
				if (height < path.i)
				{
					switch (flag)
					{
					case 0: path.set(currPath + delim + head.pos, height); break;
					case 1: path.set(currPath + arc.label, height); break;
					case 2: path.set(delim+height, height);
					}
				}
				
				break;
			}
			else
			{
				if (flag == 0)	getXPathUpAux(arc.getNode(), head, currPath, path, height+1, flag,  delim);
				else			getXPathUpAux(arc.getNode(), head, currPath + arc.label, path, height+1, flag, delim);
			}
		}
	} */
}