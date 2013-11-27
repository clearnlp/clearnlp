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
package com.clearnlp.constituent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntOpenHashSet;
import com.clearnlp.conversion.C2DInfo;
import com.clearnlp.pattern.PTNumber;
import com.clearnlp.propbank.PBLoc;
import com.clearnlp.util.pair.StringIntPair;


/**
 * Constituent node.
 * @since 1.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class CTNode implements Comparable<CTNode>
{
	/** The phrase or pos tag of this node. */
	public String      pTag;
	/** The function tags of this node (default: empty). */
	protected Set<String> s_fTags;
	/** The co-index of this node (default: {@code -1}). */
	public int coIndex  = -1;
	/** The gap-index of this node (default: {@code -1}). */
	public int gapIndex = -1;
	/** The word-form of this node (default: {@code null}). */
	public String form  = null;
	
	/** The parent of this node (default: {@code null}). */
	protected CTNode parent     = null;
	/** The antecedent of this node (default: {@code null}). */
	protected CTNode antecedent = null;
	/** The children of this node. */
	protected List<CTNode> ls_children;
	/** The ID (starting at 0) of this node among other terminal nodes within the tree (default: {@code -1}). */
	protected int i_terminalId = -1;
	/** The ID (starting at 0) of this node among other terminal nodes (disregarding empty categories) within the tree (default: {@code -1}). */
	protected int i_tokenId    = -1;
	/** The ID (starting at 0) of this node among its siblings (default: {@code -1}). */
	protected int i_siblingId  = -1;
	/** The PropBank location of this node (default: {@code null}). */
	protected PBLoc  pb_loc    = null;
	
	/** The information of constituent-to-dependency conversion. */
	public C2DInfo c2d = null;
	/** The list of PropBank predicate ID and label pairs. */
	public List<StringIntPair> pbArgs = null;
	
	/**
     * Constructs a constituent node.
     * @param tags {@link CTNode#pTag}{@code (-}{@link CTNode#s_fTags}{@code )*(-}{@link CTNode#coIndex}{@code ){0,1}(=}{@link CTNode#gapIndex}{@code ){0,1}}.
     */
	public CTNode(String tags)
	{
		setTags(tags);
		ls_children = new ArrayList<CTNode>();
	}
	
	/**
     * Constructs a constituent node with the specific word-form.
     * @param tags see the {@code tags} parameter in {@link CTNode#CTNode(String, CTNode)}.
     * @param form the word-form of this node.
     */
	public CTNode(String tags, String form)
	{
		this(tags);
		this.form = form;
	}
	
//	======================== Getters ========================

	/**
     * Returns all tags of this node in the Penn Treebank format.
     * See the {@code tags} parameter in {@link CTNode#CTNode(String, CTNode)}.
     * @return all tags of this node in the Penn Treebank format.
     */
	public String getTags()
	{
		StringBuilder build = new StringBuilder();
		
		build.append(pTag);
		
		for (String fTag : s_fTags)
		{
			build.append("-");
			build.append(fTag);
		}
		
		if (coIndex != -1)
		{
			build.append("-");
			build.append(coIndex);
		}
		
		if (gapIndex != -1)
		{
			build.append("=");
			build.append(gapIndex);
		}
		
		return build.toString();
	}
	
	/**
     * Returns the set of function tags of this node.
     * @return the set of function tags of this node.
     */
	public Set<String> getFTags()
	{
		return s_fTags;
	}
	
	/**
     * Returns the ID (starting at 0) of this node among other terminal nodes within the tree (default: {@code -1}).
     * @return the ID (starting at 0) of this node among other terminal nodes within the tree (default: {@code -1}).
     */
	public int getTerminalId()
	{
		return i_terminalId;
	}
	
	/**
     * Returns the ID (starting at 0) of this node among other terminal nodes (disregarding empty categories) within the tree (default: {@code -1}).
     * @return the ID (starting at 0) of this node among other terminal nodes (disregarding empty categories) within the tree (default: {@code -1}).
     */
	public int getTokenId()
	{
		return i_tokenId;
	}
	
	/**
     * Returns the ID (starting at 0) of this node among its siblings (default: {@code -1}).
     * @return the ID (starting at 0) of this node among its siblings (default: {@code -1}).
     */
	public int getSiblingId()
	{
		return i_siblingId;
	}
	
	/**
     * Returns the PropBank location of this node (default: {@code null}).
     * @return the PropBank location of this node (default: {@code null}).
     */
	public PBLoc getPBLoc()
	{
		return pb_loc;
	}
	
	/**
     * Returns the parent of this node.
     * @return the parent of this node.
     */
	public CTNode getParent()
	{
		return parent;
	}
	
	/**
     * Returns the antecedent of this node (default: {@code null}).
     * @return the antecedent of this node (default: {@code null}).
     */
	public CTNode getAntecedent()
	{
		return antecedent;
	}
	
	/**
     * Returns a list of all children of this node.
     * @return a list of all children of this node.
     */
	public List<CTNode> getChildren()
	{
		return ls_children;
	}
	
	/**
     * Returns an immutable list of sub-children of this node.
     * The sublist begins at the specific position and extends to the end.
     * @param fstId the ID of the first child (inclusive).
     * @return an immutable list of sub-children of this node.
     * @throws IndexOutOfBoundsException for an illegal ID.
     */
	public List<CTNode> getChildren(int fstId)
	{
		return ls_children.subList(fstId, ls_children.size());
	}
	
	/**
	 * Returns an immutable list of sub-children of this node.
     * The sublist begins and ends at the specific positions.
     * @param fstId the ID of the first child (inclusive).
     * @param lstId the ID of the last child (exclusive)
     * @return an immutable list of sub-children of this node.
     * @throws IndexOutOfBoundsException for an illegal ID.
     */
	public List<CTNode> getChildren(int fstId, int lstId)
	{
		return ls_children.subList(fstId, lstId);
	}
	
	public CTNode getChild(int childId)
	{
		return (0 <= childId && childId < ls_children.size()) ? ls_children.get(childId) : null;
	}
	
	public CTNode getFirstChild(String... tags)
	{
		for (CTNode child : ls_children)
		{
			if (child.isTag(tags))
				return child;
		}
		
		return null;
	}
	
	public CTNode getLastChild(String... tags)
	{
		CTNode child;	int i;
		
		for (i=ls_children.size()-1; i>=0; i--)
		{
			child = ls_children.get(i);
			
			if (child.isTag(tags))
				return child;
		}
		
		return null;
	}
	
	public List<CTNode> getAllChildren(String... tags)
	{
		List<CTNode> list = new ArrayList<CTNode>();
		
		for (CTNode child : ls_children)
		{
			if (child.isTag(tags))
				list.add(child);
		}
		
		return list;
	}
	
	public CTNode getFirstDescendant(String... tags)
	{
		return getFirstDescendantAux(ls_children, tags);
	}
	
	private CTNode getFirstDescendantAux(List<CTNode> nodes, String... tags)
	{
		CTNode desc;
		
		for (CTNode node : nodes)
		{
			if (node.isTag(tags))	return node;
			
			desc = getFirstDescendantAux(node.ls_children, tags);
			if (desc != null)	return desc;
		}
		
		return null;
	}

	public CTNode getPrevSibling()
	{
		List<CTNode> siblings = parent.ls_children;
		return (0 <= i_siblingId-1) ? siblings.get(i_siblingId-1) : null;
	}
	
	public CTNode getPrevSibling(String... tags)
	{
		if (parent == null) return null;
		List<CTNode> siblings = parent.ls_children;
		CTNode node; int i;
		
		for (i=i_siblingId-1; i>=0; i--)
		{
			node = siblings.get(i);
			
			if (node.isTag(tags))
				return node;
		}
		
		return null;
	}
	
	public List<CTNode> getPrevSiblings()
	{
		return (parent != null) ? parent.getChildren(0, this.i_siblingId) : new ArrayList<CTNode>(0);
	}
	
	public CTNode getNextSibling()
	{
		List<CTNode> siblings = parent.ls_children;
		return (i_siblingId+1 < siblings.size()) ? siblings.get(i_siblingId+1) : null;
	}
	
	public CTNode getNextSibling(String... tags)
	{
		if (parent == null) return null;
		List<CTNode> siblings = parent.ls_children;
		CTNode node; int i, size = siblings.size();
		
		for (i=i_siblingId+1; i<size; i++)
		{
			node = siblings.get(i);
			
			if (node.isTag(tags))
				return node;
		}
		
		return null;
	}
	
	public CTNode getNearestAncestor(String... tags)
	{
		CTNode curr = this;
		
		while (curr.parent != null)
		{
			curr = curr.parent;
			if (curr.isTag(tags)) return curr;
		}
		
		return null;
	}
	
	public CTNode getFirstChainedDescendant(String... tags)
	{
		CTNode desc = this, child;
		
		while ((child = desc.getFirstChild(tags)) != null)
			desc = child;
		
		return (desc != this) ? desc : null;
	}
	
	public CTNode getHighestChainedAncestor(String... tags)
	{
		CTNode curr = this;
		
		while (curr.parent != null && curr.parent.isTag(tags))
			curr = curr.parent;
	
		return (curr == this) ? null : curr;
	}
	
	public List<CTNode> getIncludedEmptyCategory(String regex)
	{
		List<CTNode> list = new ArrayList<CTNode>();
		
		getIncludedEmptyCategoriesAux(this, list, regex);
		return list;
	}
	
	private void getIncludedEmptyCategoriesAux(CTNode curr, List<CTNode> list, String regex)
	{
		if (curr.isEmptyCategory() && curr.form.matches(regex))
			list.add(curr);
		
		for (CTNode child : curr.ls_children)
			getIncludedEmptyCategoriesAux(child, list, regex);
	}
	
	public List<CTNode> getSubTerminals()
	{
		List<CTNode> terminals = new ArrayList<CTNode>();
		
		getSubTerminals(this, terminals);
		return terminals;
	}
	
	private void getSubTerminals(CTNode curr, List<CTNode> terminals)
	{
		if (curr.isPhrase())
		{
			for (CTNode child : curr.ls_children)
				getSubTerminals(child, terminals);
		}
		else
			terminals.add(curr);
	}
	
	public IntArrayList getSubTerminalIdList()
	{
		IntArrayList list = new IntArrayList();
		
		for (CTNode node : getSubTerminals())
			list.add(node.getTerminalId());
		
		return list;
	}
	
	public IntOpenHashSet getSubTerminalIdSet()
	{
		IntOpenHashSet set = new IntOpenHashSet();
		
		for (CTNode node : getSubTerminals())
			set.add(node.getTerminalId());
		
		return set;
	}
	
	public List<CTNode> getSubTokens()
	{
		List<CTNode> tokens = new ArrayList<CTNode>();
		
		getSubTokens(this, tokens);
		return tokens;
	}
	
	private void getSubTokens(CTNode curr, List<CTNode> tokens)
	{
		if (curr.isPhrase())
		{
			for (CTNode child : curr.ls_children)
				getSubTokens(child, tokens);
		}
		else if (!curr.isEmptyCategory())
			tokens.add(curr);
	}
	
	public CTNode getFirstTerminal()
	{
		return getFirstTerminalAux(this);
	}
	
	private CTNode getFirstTerminalAux(CTNode node)
	{
		List<CTNode> children = node.getChildren();
		if (children.isEmpty())	return node;
		
		return getFirstTerminalAux(children.get(0));
	}
	
	public CTNode getLastTerminal()
	{
		return getLastTerminalAux(this);
	}
	
	private CTNode getLastTerminalAux(CTNode node)
	{
		List<CTNode> children = node.getChildren();
		if (children.isEmpty())	return node;
		
		return getLastTerminalAux(children.get(children.size()-1));
	}
	
	public int getChildrenSize()
	{
		return ls_children.size();
	}
	
	public CTNode getLowestCommonAncestor(CTNode node)
	{
		if (this.isDescendantOf(node))	return node;
		if (node.isDescendantOf(this))	return this;
		
		CTNode parent = getParent();
		
		while (parent != null)
		{
			if (node.isDescendantOf(parent))
				return parent;
			
			parent = parent.getParent();
		}
		
		return null;
	}
	
	public int getDistanceToTop()
	{
		CTNode node = getParent();
		int dist = 0;
		
		while (node != null)
		{
			dist++;
			node = node.getParent();
		}
		
		return dist;
	}
	
//	======================== Setters ========================
	
	public void setTags(String tags)
	{
		s_fTags = new TreeSet<String>();
		
		if (tags.charAt(0) == '-')
		{
			pTag = tags;
			return;
		}
		
		StringTokenizer tok = new StringTokenizer(tags, "-=", true);
		String delim, tag;
		
		pTag = tok.nextToken();
		
		while (tok.hasMoreTokens())
		{
			delim = tok.nextToken();
			if (!tok.hasMoreTokens())
			{
				System.err.println("Error: illegal tag \""+tags+"\"");
				break;
			}
			tag = tok.nextToken();
			
			if (delim.equals("-"))
			{
				if (PTNumber.containsOnlyDigits(tag))
				{
					if (coIndex == -1)
						coIndex = Integer.parseInt(tag);
					else
						gapIndex = Integer.parseInt(tag);
				}
				else
					s_fTags.add(tag);
			}
			else // if (delim.equals("="))
				gapIndex = Integer.parseInt(tag);
		}
	}
	
	public void addFTag(String fTag)
	{
		s_fTags.add(fTag);
	}
	
	public void addFTags(Collection<String> fTags)
	{
		s_fTags.addAll(fTags);
	}
	
	public void removeFTag(String fTag)
	{
		s_fTags.remove(fTag);
	}
	
	public void removeFTagAll()
	{
		s_fTags.clear();
	}
	
	public void setAntecedent(CTNode ante)
	{
		antecedent = ante;
	}
	
	public void addChild(CTNode child)
	{
		child.parent = this;
		child.i_siblingId = ls_children.size();
		
		ls_children.add(child);
	}
	
	public void addChild(int index, CTNode child)
	{
		ls_children.add(index, child);
		
		child.parent = this;
		resetSiblingIDs(index);
	}
	
	public void setChild(int index, CTNode child)
	{
		ls_children.set(index, child).parent = null;
		
		child.parent = this;
		child.i_siblingId = index;
	}
	
	public void removeChild(int index)
	{
		if (!isChildrenRange(index))
			throw new IndexOutOfBoundsException(Integer.toString(index));
		
		ls_children.remove(index).parent = null;
		resetSiblingIDs(index);
	}
	
	private void resetSiblingIDs(int index)
	{
		int i, size = ls_children.size();
		
		for (i=index; i<size; i++)
			ls_children.get(i).i_siblingId = i;
	}
	
	public void removeChild(CTNode child)
	{
		removeChild(ls_children.indexOf(child));
	}

	public void resetChildren(Collection<CTNode> children)
	{
		ls_children.clear();
		
		for (CTNode child : children)
			addChild(child);
	}
	
//	======================== Booleans ========================
	
	public boolean isPTag(String pTag)
	{
		return this.pTag.equals(pTag);
	}
	
	public boolean isPTagAny(String... pTags)
	{
		for (String pTag : pTags)
		{
			if (isPTag(pTag))
				return true;
		}
		
		return false;
	}
	
	public boolean matchesPTag(String regex)
	{
		return pTag.matches("^"+regex+"$");
	}
	
	public boolean isFTag(String fTag)
	{
		return (s_fTags.size() == 1) && (s_fTags.contains(fTag)); 
	}
	
	public boolean hasFTag(String fTag)
	{
		return s_fTags.contains(fTag);
	}
	
	public boolean hasFTagAll(Collection<String> fTags)
	{
		return this.s_fTags.containsAll(fTags);
	}
	
	public boolean hasFTagAny(Collection<String> fTags)
	{
		for (String fTag : fTags)
		{
			if (this.s_fTags.contains(fTag))
				return true;
		}
		
		return false;
	}
	
	public boolean hasFTagAny(String... fTags)
	{
		for (String fTag : fTags)
		{
			if (this.s_fTags.contains(fTag))
				return true;
		}
		
		return false;
	}
	
	public boolean isTag(String... tags)
	{
		String      pTag  = null;
		String      pRex  = null;
		Set<String> fTags = new HashSet<String>();
		
		for (String tag : tags)
		{
			if (tag.equals(CTLib.POS_NONE) || tag.equals(CTLibEn.POS_LRB) || tag.equals(CTLibEn.POS_RRB))
				pTag = tag;
			else
			{
				switch (tag.charAt(0))
				{
				case '-': fTags.add(tag.substring(1));	break;
				case '+': pRex = tag.substring(1);		break;
				default : pTag = tag;
				}				
			}
		}
		
		return (pTag == null || isPTag (pTag)) &&
		       (pRex == null || matchesPTag(pRex)) &&
		       hasFTagAll(fTags);
	}
	
	public boolean isForm(String form)
	{
		return this.form != null && this.form.equals(form);
	}
	
	public boolean isPhrase()
	{
		return !ls_children.isEmpty();
	}
	
	public boolean isEmptyCategory()
	{
		return pTag.equals(CTLib.POS_NONE);
	}
	
	public boolean isEmptyCategoryRec()
	{
		CTNode curr = this;
		
		while (curr.isPhrase())
		{
			if (curr.ls_children.size() > 1)
				return false;
			else
				curr = curr.getChild(0);
		}
		
		return curr.isEmptyCategory();
	}
	
	public boolean isDescendantOf(CTNode node)
	{
		CTNode parent = getParent();

		while (parent != null)
		{
			if (parent == node)
				return true;
			
			parent = parent.getParent();
		}
		
		return false;
	}
	
	public boolean containsTags(String... tags)
	{
		for (CTNode child : ls_children)
		{
			if (child.isTag(tags))
				return true;
		}
		
		return false;
	}
	
	/**
	 * 
	 *
	 * @param index 
	 * @return 
	 */
	public boolean isChildrenRange(int index)
	{
		return 0 <= index && index < ls_children.size();
	}

	
//	======================== Strings ========================

	public String toForms(boolean includeNulls, String delim)
	{
		StringBuilder build = new StringBuilder();
		
		for (CTNode node : getSubTerminals())
		{
			if (includeNulls || !node.isEmptyCategory())
			{
				build.append(delim);
				build.append(node.form);
			}
		}
		
		return build.length() == 0 ? "" : build.substring(delim.length());
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return toString(false, false);
	}
	
	public String toString(boolean... args)
	{
		boolean includeLineNumbers  = (args.length > 0) ? args[0] : false;
		boolean includeAntePointers = (args.length > 1) ? args[1] : false;
		
		ArrayList<String> lTree = new ArrayList<String>();
		toStringAux(this, lTree, "", includeAntePointers);
		
		StringBuilder build = new StringBuilder();
		
		for (int i=0; i<lTree.size(); i++)
		{
			if (includeLineNumbers)
				build.append(String.format("%3d: %s\n", i, lTree.get(i)));
			else
				build.append(lTree.get(i)+"\n");
		}
			
		build.deleteCharAt(build.length()-1);	// remove the last '\n'
		return build.toString();
	}
	
	public String toStringLine()
	{
		ArrayList<String> lTree = new ArrayList<String>();
		toStringAux(this, lTree, "", false);
		
		StringBuilder build = new StringBuilder();
		
		for (int i=0; i<lTree.size(); i++)
			build.append(lTree.get(i).trim()+" ");
			
		build.deleteCharAt(build.length()-1);	// remove the last ' '
		return build.toString();
	}
	
	private void toStringAux(CTNode curr, List<String> lTree, String sTags, boolean includeAntePointers)
	{
		if (curr.isPhrase())
		{
			sTags += "("+curr.getTags()+" ";
		//	sTags += "("+curr.getTags()+"-"+curr.pbLoc+" ";
			
			for (CTNode child : curr.ls_children)
			{
				toStringAux(child, lTree, sTags, includeAntePointers);
				
				if (child.i_siblingId == 0)
					sTags = sTags.replaceAll(".", " ");		// indent
			}

			int last = lTree.size() - 1;
			lTree.set(last, lTree.get(last)+")");
		}
		else
		{
			String tag = sTags+"("+curr.getTags()+" "+curr.form+")";
			if (includeAntePointers && curr.antecedent != null)
				tag += "["+curr.antecedent.getTags()+"]"; 
			lTree.add(tag);
		}
	}
	
	@Override
	public int compareTo(CTNode node)
	{
		return i_terminalId - node.i_terminalId;
	}
}
