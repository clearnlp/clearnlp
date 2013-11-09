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

import java.util.regex.Pattern;

import com.clearnlp.reader.AbstractReader;

/**
 * Dependency arc.
 * @since 1.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class DEPArc implements Comparable<DEPArc>
{
	/** The linking node. */
	protected DEPNode node;
	/** The dependency label to the linking node. */
	protected String label;
	
	/** Constructs an empty dependency arc. */
	public DEPArc()
	{
		clear();
	}
	
	/**
	 * Constructs a dependency arc.
	 * @param node the linking node.
	 * @param label the dependency label for the linking node.
	 */
	public DEPArc(DEPNode node, String label)
	{
		set(node, label);
	}
	
	public DEPArc(DEPTree tree, String arc)
	{
		int idx = arc.indexOf(DEPLib.DELIM_HEADS_KEY);
		int nodeId = Integer.parseInt(arc.substring(0, idx));
		
		node  = tree.get(nodeId);
		label = arc.substring(idx+1);
	}
	
	/** Sets the node to {@code null} and the label to {@link AbstractReader#DUMMY_TAG}. */
	public void clear()
	{
		set(null, null);
	}
	
	/**
	 * Returns the linking node.
	 * @return the linking node
	 */
	public DEPNode getNode()
	{
		return node;
	}
	
	/**
	 * Set the linking node to the specific node.
	 * @param node the node to be set.
	 */
	public void setNode(DEPNode node)
	{
		this.node = node;
	}
	
	/**
	 * Returns the dependency label.
	 * @return the dependency label.
	 */
	public String getLabel()
	{
		return label;
	}
	
	/**
	 * Sets the dependency label to the linking node.
	 * @param label the dependency label to the linking node.
	 */
	public void setLabel(String label)
	{
		this.label = label;
	}
	
	public void appendLabel(String label)
	{
		this.label += DEPFeat.DELIM_VALUES + label;
	}
	
	/**
	 * Sets the linking node and dependency label. 
	 * @param node the linking node.
	 * @param label the dependency label to the linking node.
	 */
	public void set(DEPNode node, String label)
	{
		this.node  = node;
		this.label = label;
	}
	
	/**
	 * Returns {@code true} if the specific node is its linking node.
	 * @param node the node to be compared.
	 * @return {@code true} if the specific node is its linking node.
	 */
	public boolean isNode(DEPNode node)
	{
		return this.node == node;
	}
	
	/**
	 * Returns {@code true} if the specific label is its label.
	 * @param label the label to be compared.
	 * @return {@code true} if the specific label is its label.
	 */
	public boolean isLabel(String label)
	{
		return this.label.equals(label);
	}
	
	public boolean isLabel(Pattern regex)
	{
		return regex.matcher(label).find();
	}
	
	@Override
	public String toString()
	{
		StringBuilder build = new StringBuilder();
		
		build.append(node.id);
		build.append(DEPLib.DELIM_HEADS_KEY);
		build.append(label);
		
		return build.toString();
	}
	
	/**
	 * If both the specific node and label match, returns 1.
	 * If only the specific node matches, returns 2.
	 * Otherwise, returns 0.
	 * @param node the node to be compared.
	 * @param label the label to be compared.
	 * @return If both the specific node and label match, returns 1.
	 * If only the  specific node matches, returns 2.
	 * Otherwise, returns 0.
	 */
	public int compareTo(DEPNode node, String label)
	{
		if (isNode(node))
			 return isLabel(label) ? 1 : 2;
		
		return 0;
	}
	
	@Override
	public int compareTo(DEPArc arc)
	{
		return node.compareTo(arc.node);
	}	
}
