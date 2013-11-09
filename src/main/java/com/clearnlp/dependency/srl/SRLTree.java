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
package com.clearnlp.dependency.srl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import com.clearnlp.constituent.CTLibEn;
import com.clearnlp.dependency.DEPLib;
import com.clearnlp.dependency.DEPNode;
import com.clearnlp.morphology.MPLibEn;

/**
 * @since 1.4.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class SRLTree
{
	private DEPNode      d_predicate;
	private List<SRLArc> l_arguments;
	
	public SRLTree(DEPNode predicate)
	{
		d_predicate = predicate;
		l_arguments = new ArrayList<SRLArc>();
	}
	
	/** This methods converts the specific label to its base form automatically. */
	public boolean containsLabel(String label)
	{
		label = SRLLib.getBaseLabel(label);
		
		for (SRLArc arc : l_arguments)
		{
			if (label.equals(SRLLib.getBaseLabel(arc.getLabel())))
				return true;
		}
		
		return false;
	}
	
	public Set<String> getBaseLabelSet()
	{
		Set<String> labels = new HashSet<String>();
		
		for (SRLArc arc : l_arguments)
			labels.add(SRLLib.getBaseLabel(arc.getLabel()));
		
		return labels;
	}

	public void addArgument(DEPNode argument, String label)
	{
		l_arguments.add(new SRLArc(argument, label));
	}
	
	public DEPNode getPredicate()
	{
		return d_predicate;
	}
	
	public String getRolesetID()
	{
		return d_predicate.getFeat(DEPLib.FEAT_PB);
	}
	
	public SRLArc getFirstArgument(String label)
	{
		for (SRLArc arc : l_arguments)
		{
			if (arc.isLabel(label))
				return arc;
		}
		
		return null;
	}
	
	public List<SRLArc> getArguments(Pattern regex)
	{
		List<SRLArc> args = new ArrayList<SRLArc>();
		
		for (SRLArc arc : l_arguments)
		{
			if (arc.isLabel(regex))
				args.add(arc);
		}
		
		return args;
	}
	
	public List<DEPNode> getArgumentNodes(Pattern regex)
	{
		List<DEPNode> args = new ArrayList<DEPNode>();
		
		for (SRLArc arc : l_arguments)
		{
			if (arc.isLabel(regex))
				args.add(arc.getNode());
		}
		
		return args;
	}
	
	public List<SRLArc> getArguments()
	{
		return l_arguments;
	}
	
	public String getKey()
	{
		StringBuilder build = new StringBuilder();
		Collections.sort(l_arguments);
		
		build.append("V:");
		build.append(getRolesetID());
		
		for (SRLArc arg : l_arguments)
		{
			build.append(";");
			build.append(arg.getLabel());
			build.append(":");
			build.append(arg.getNode().lemma);
		}
		
		return build.toString();
	}
	
	public String getKey(Set<String> ignore)
	{
		StringBuilder build = new StringBuilder();
		Collections.sort(l_arguments);
		
		build.append("V:");
		build.append(getRolesetID());
		
		for (SRLArc arg : l_arguments)
		{
			if (!ignore.contains(arg.getLabel()))
			{
				build.append(";");
				build.append(arg.getLabel());
				build.append(":");
				build.append(arg.getNode().lemma);				
			}
		}
		
		return build.toString();
	}
	
	public String getKey(Pattern ignore)
	{
		StringBuilder build = new StringBuilder();
		Collections.sort(l_arguments);
		
		build.append("V:");
		build.append(getRolesetID());
		
		for (SRLArc arg : l_arguments)
		{
			if (!ignore.matcher(arg.getLabel()).find())
			{
				build.append(";");
				build.append(arg.getLabel());
				build.append(":");
				build.append(arg.getNode().lemma);				
			}
		}
		
		return build.toString();
	}
	
	public String getRichKeyEn(Pattern ignore, String delim)
	{
		StringBuilder build = new StringBuilder();
		Collections.sort(l_arguments);
		DEPNode node;
		String value;
		
		build.append("V:");
		build.append(getRolesetID());
		
		for (SRLArc arg : l_arguments)
		{
			if (!ignore.matcher(arg.getLabel()).find())
			{
				node = arg.getNode();
				
				build.append(";");
				build.append(arg.getLabel());
				build.append(":");
				
				if (MPLibEn.isNoun(node.pos))
					value = node.getSubLemmasEnNoun(delim);
				else if (node.isPos(CTLibEn.POS_IN))
					value = node.getSubLemmasEnPP(delim);
				else
					value = node.lemma;
				
				build.append(value);
			}
		}
		
		return build.toString();
	}
}
