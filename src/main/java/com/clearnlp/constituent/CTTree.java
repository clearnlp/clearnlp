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
import java.util.Collections;
import java.util.List;

import com.carrotsearch.hppc.IntObjectOpenHashMap;
import com.clearnlp.propbank.PBLoc;


/**
 * Constituent tree.
 * @see CTReader
 * @see CTNode 
 * @since 1.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class CTTree
{
	private CTNode       nd_root;
	/** Gets initialized by {@link CTTree#setTerminals()}. */
	private List<CTNode> ls_termainals;
	/** Gets initialized by {@link CTTree#setTerminals()}. */
	private List<CTNode> ls_tokens;
	/** Gets initialized by {@link CTTree#linkCoIndexedEmtpyCategories()}. */
	private IntObjectOpenHashMap<List<CTNode>> mp_nulls;
	
	/**
	 * Constructs a constituent tree using the specific root node.
	 * Once the tree is constructed, it becomes immutable.
	 * @param root the root node of this tree.
	 */
	public CTTree(CTNode root)
	{
		nd_root = root;
		setTerminals();
		linkCoIndexedEmtpyCategories();
	}
	
	/** Adds all terminals nodes of the root to this tree. */
	private void setTerminals()
	{
		List<CTNode> terminals = new ArrayList<CTNode>();
		List<CTNode> tokens    = new ArrayList<CTNode>();
		
		setTerminalsAux(nd_root, terminals, tokens);

		ls_termainals = Collections.unmodifiableList(terminals);
		ls_tokens     = Collections.unmodifiableList(tokens);
	}

	/** Called by {@link CTTree#addTerminals()}. */
	private void setTerminalsAux(CTNode curr, List<CTNode> terminals, List<CTNode> tokens)
	{
		if (curr.isPhrase())
		{
			for (CTNode child : curr.ls_children)
				setTerminalsAux(child, terminals, tokens);
		}
		else
		{
			curr.i_terminalId = terminals.size();
			terminals.add(curr);
			
			if (!curr.isEmptyCategory())
			{
				curr.i_tokenId = tokens.size();
				tokens.add(curr);
			}
		}
	}
	
	/** Links all co-indexed empty categories to their antecedents. */
	private void linkCoIndexedEmtpyCategories()
	{
		int idx, coIndex;
		mp_nulls = new IntObjectOpenHashMap<List<CTNode>>();
		
		for (CTNode node : ls_termainals)
		{
			if (node.isEmptyCategory() && (idx = node.form.lastIndexOf("-")) >= 0)
			{
				coIndex = Integer.parseInt(node.form.substring(idx+1));
				node.antecedent = getCoIndexedAntecedent(coIndex);
				
				if (node.antecedent != null)
				{
					List<CTNode> list;
					
					if (mp_nulls.containsKey(coIndex))
						list = mp_nulls.get(coIndex);
					else
					{
						list = new ArrayList<CTNode>();
						mp_nulls.put(coIndex, list);
					}
					
					list.add(node);
				}
			}
		}
	}
	
	/** Assigns PropBank locations to all nodes (see {@link CTNode#pb_loc}). */
	public void setPBLocs()
	{
		int terminalId, height;
		
		for (CTNode node : ls_termainals)
		{
			terminalId  = node.i_terminalId;
			height      = 0;
			node.pb_loc = new PBLoc(terminalId, height);
			
			while (node.parent != null && node.parent.pb_loc == null)
			{
				node = node.parent;
				node.pb_loc = new PBLoc(terminalId, ++height);
			}
		}
	}
	
//	======================== Getters ========================

	/**
	 * Returns the root of this tree.
	 * @return the root of this tree.
	 */
	public CTNode getRoot()
	{
		return nd_root;
	}
	
	/**
	 * Returns a node in this tree with the specific terminal ID and height.
	 * @param terminalId {@link CTNode#i_terminalId}.
	 * @param height the height (starting at 0) of the node from its first terminal node.
	 * @return a node in this tree with the specific terminal ID and height.
	 */
	public CTNode getNode(int terminalId, int height)
	{
		CTNode node = getTerminal(terminalId);
		
		for (int i=height; i>0; i--)
			node = node.parent;
		
		return node;
	}
	
	/**
	 * Returns the node in this tree using the specific PropBank location.
	 * @param loc the PropBank location.
	 * @return the node in this tree using the specific PropBank location.
	 */
	public CTNode getNode(PBLoc loc)
	{
		return getNode(loc.terminalId, loc.height);
	}
	
	/**
	 * Returns a terminal node in this tree with the specific ID.
	 * @param terminalId {@link CTNode#i_terminalId}.
	 * @return a terminal node in this tree with the specific ID.
	 */
	public CTNode getTerminal(int terminalId)
	{
		return ls_termainals.get(terminalId);
	}
	
	/**
	 * Returns the immutable list of all terminal nodes.
	 * @return the list of all terminal nodes.
	 */
	public List<CTNode> getTerminals()
	{
		return ls_termainals;
	}
	
	/**
	 * Returns a terminal node in this tree with respect to its token ID.
	 * @param tokenId {@link CTNode#i_tokenId}.
	 * @return a terminal node in this tree with respect to its token ID.
	 */
	public CTNode getToken(int tokenId)
	{
		return ls_tokens.get(tokenId);
	}
	
	/**
	 * Returns the list of all terminal nodes discarding empty categories.
	 * @return the list of all terminal nodes discarding empty categories.
	 */
	public List<CTNode> getTokens()
	{
		return ls_tokens;
	}
	
	/**
	 * Returns a co-indexed antecedent, or {@code null} if such an antecedent doesn't exist.
	 * @param coIndex the co-index of the antecedent.
	 * @return a co-indexed antecedent, or {@code null} if such an antecedent doesn't exist.
	 */
	public CTNode getCoIndexedAntecedent(int coIndex)
	{
		return getCoIndexedAntecedentAux(coIndex, nd_root);
	}
	
	/** Called by {@link CTTree#getCoIndexedAntecedent(int)}. */
	private CTNode getCoIndexedAntecedentAux(int coIndex, CTNode curr)
	{
		if (curr.coIndex == coIndex)
			return curr;
		else if (curr.gapIndex == coIndex)
		{
			int t = curr.coIndex;
			curr.coIndex  = curr.gapIndex;
			curr.gapIndex = t;
			
			return curr;
		}
		
		CTNode ante;
		
		for (CTNode child : curr.getChildren())
		{
			if ((ante = getCoIndexedAntecedentAux(coIndex, child)) != null)
				return ante;
		}
		
		return null;
	}
	
	/**
	 * Returns a list of co-indexed empty categories, or {@code null} if such empty categories don't exist.
	 * @param coIndex the co-index of the empty categories.
	 * @return a list of co-indexed empty categories, or {@code null} if such empty categories don't exist.
	 */
	public List<CTNode> getCoIndexedEmptyCategories(int coIndex)
	{
		return mp_nulls.get(coIndex);
	}
	
//	======================== Boolean ========================
	
	/**
	 * Return {@code true} if both the specific terminal ID and height are within the range of this tree.
	 * @param terminalId the terminal ID to be compared.
	 * @param height the height to be compared.
	 * @return {@code true} if both the specific terminal ID and height are within the range of this tree.
	 */
	public boolean isRange(int terminalId, int height)
	{
		if (terminalId < 0 || terminalId >= ls_termainals.size())
			return false;
		
		CTNode node = ls_termainals.get(terminalId);
		
		for (int i=height; i>0; i--)
		{
			if (node.parent == null)
				return false;
			
			node = node.parent;
		}
		
		return true;
	}
	
	/**
	 * Returns {@code true} if the specific PropBank location is within the range of this tree.
	 * @param loc the PropBank location to be compared.
	 * @return {@code true} if the specific PropBank location is within the range of this tree.
	 */
	public boolean isRange(PBLoc loc)
	{
		return isRange(loc.terminalId, loc.height);
	}
	
	public boolean compareBrackets(CTTree tree)
	{
		int i, size = ls_termainals.size();
		
		if (size != tree.getTerminals().size())
			return false;
		
		CTNode node1, node2;
		
		for (i=0; i<size; i++)
		{
			node1 = getTerminal(i);
			node2 = tree.getTerminal(i);
			
			if (node1.getDistanceToTop() != node2.getDistanceToTop())
				return false;
			
			if (!node1.form.equals(node2.form))
			{
				System.out.println(node1.form+" "+node2.form);
				return false;
			}
		}
		
		return true;
	}

//	======================== Strings ========================
	
	/**
	 * Returns {@link CTTree#toForms(boolean, String)}, where {@code (includeNulls=true, delim=" ")}.
	 * @return {@link CTTree#toForms(boolean, String)}, where {@code (includeNulls=true, delim=" ")}.
	 */
	public String toForms()
	{
		return toForms(true, " ");
	}
	
	/**
	 * Returns ordered word-forms of this tree.
	 * @param includeNulls if {@code true}, include forms of empty categories.
	 * @param delim the delimiter between forms.
	 * @return ordered word-forms of this tree.
	 */
	public String toForms(boolean includeNulls, String delim)
	{
		StringBuilder build = new StringBuilder();
		
		if (includeNulls)
		{
			for (CTNode node : ls_termainals)
			{
				build.append(delim);
				build.append(node.form);
			}	
		}
		else
		{
			for (CTNode node : ls_tokens)
			{
				build.append(delim);
				build.append(node.form);
			}
		}
		
		return build.length() == 0 ? "" : build.substring(delim.length());
	}
	
	public String toCoNLLPOS(boolean includeNulls, String delim)
	{
		StringBuilder build = new StringBuilder();
		
		if (includeNulls)
		{
			for (CTNode node : ls_termainals)
			{
				build.append(node.form);
				build.append(delim);
				build.append(node.pTag);
				build.append("\n");
			}	
		}
		else
		{
			for (CTNode node : ls_tokens)
			{
				build.append(node.form);
				build.append(delim);
				build.append(node.pTag);
				build.append("\n");
			}
		}
		
		return build.toString();
	}
	
	/**
	 * Returns {@link CTTree#toString(boolean...)}, where {@code args = {false, false}}.
	 * @return {@link CTTree#toString(boolean...)}, where {@code args = {false, false}}.
	 */
	public String toString()
	{
		return toString(false, false);
	}
	
	/**
	 * Returns the string representation of this tree in one line.
	 * @return the string representation of this tree in one line.
	 */
	public String toStringLine()
	{
		return nd_root.toStringLine();
	}
	
	/**
	 * Returns the Penn Treebank style constituent tree.
	 * @param args see the {@code args} parameter in {@link CTNode#toString(boolean...)}.
	 * @return the Penn Treebank style constituent tree.
	 */
	public String toString(boolean... args)
	{
		return nd_root.toString(args);
	}
}
