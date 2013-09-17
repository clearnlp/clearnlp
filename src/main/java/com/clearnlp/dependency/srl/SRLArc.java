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
package com.clearnlp.dependency.srl;

import com.clearnlp.constant.universal.UNConstant;
import com.clearnlp.constant.universal.UNPunct;
import com.clearnlp.dependency.DEPArc;
import com.clearnlp.dependency.DEPLib;
import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dependency.DEPTree;

/**
 * Dependency arc.
 * @since 1.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class SRLArc extends DEPArc
{
	static private String DELIM_FTAG = UNPunct.EQUAL;
	private String functionTag = UNConstant.EMPTY;
	
	public SRLArc()
	{
		super();
	}
	
	public SRLArc(DEPNode node, String label)
	{
		super(node, label);
	}
	
	public SRLArc(DEPNode node, String label, String functionTag)
	{
		super(node, label);
		this.functionTag = functionTag;
	}
	
	public SRLArc(DEPTree tree, String arc)
	{
		int idx = arc.indexOf(DEPLib.DELIM_HEADS_KEY);
		int nodeId = Integer.parseInt(arc.substring(0, idx));
		
		node  = tree.get(nodeId);
		label = arc.substring(idx+1);
		idx   = label.lastIndexOf(DELIM_FTAG);
		
		if (idx >= 0)
		{
			functionTag = label.substring(idx+1);
			label = label.substring(0, idx);
		}
	}
	
	public String getFunctionTag()
	{
		return functionTag;
	}
	
	@Override
	public String toString()
	{
		StringBuilder build = new StringBuilder();
		
		build.append(node.id);
		build.append(DEPLib.DELIM_HEADS_KEY);
		build.append(label);
		
		if (!functionTag.isEmpty())
		{
			build.append(DELIM_FTAG);
			build.append(functionTag);
		}
		
		return build.toString();
	}
	
	@Override
	public int compareTo(DEPArc arc)
	{
		return label.compareTo(arc.getLabel());
	}	
}
