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
package com.clearnlp.propbank;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.clearnlp.constituent.CTLibEn;
import com.clearnlp.constituent.CTTree;


/**
 * PropBank instance.
 * @see PBLoc
 * @see PBArg
 * @since 1.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class PBInstance implements Comparable<PBInstance>
{
	/** The path to the Treebank file. */
	public String treePath;
	/** The ID of the tree (starting with 0). */
	public int    treeId;
	/** The terminal ID of the predicate (starting with 0). */
	public int    predId;
	/** The ID of the annotator (e.g., gold). */
	public String annotator;
	/** The type of the predicate (e.g., take-v). */
	public String type;
	/** The roleset ID of the predicate (e.g., take.01). */
	public String roleset;
	/** The aspects of the predicate (no longe used). */
	public String aspects;
	/** The arguments of the predicate. */
	protected List<PBArg> l_args;
	/** The constituent tree associated with this instance (default: {@code null}). */
	protected CTTree tree = null;
	
	/** Constructs a PropBank instance. */
	public PBInstance()
	{
		l_args = new ArrayList<PBArg>();
	}
	
	/**
	 * Constructs a PropBank instance using the specific string.
	 * @param str {@code <treePath><treeId><predId><annotator><type><roleset><aspects>(<argument>)+}.
	 */
	public PBInstance(String str)
	{
		String[] tmp = str.split(PBLib.DELIM_INST);
		
		if (tmp.length < 7)
		{
			System.err.println("Error: missing fields - "+str);
			System.exit(1);
		}
		
		try
		{
			treePath  = tmp[0];
			treeId    = Integer.parseInt(tmp[1]);
			predId    = Integer.parseInt(tmp[2]);
			annotator = tmp[3];
			type      = tmp[4];
			roleset   = tmp[5];
			aspects   = tmp[6];
		}
		catch (NumberFormatException e)
		{
			System.err.println("Error: illegal format - "+str);
			System.exit(1);
		}
		
		int i, size = tmp.length;
		l_args = new ArrayList<PBArg>();
		
		for (i=7; i<size; i++)
			addArg(new PBArg(tmp[i]));
	}
	
	/**
	 * Returns the index'th argument of this instance.
	 * if the index is out-of-range, returns {@code null}.
	 * @param index the index of the argument to be returned.
	 * @return the index'th argument of this instance.
	 */
	public PBArg getArg(int index)
	{
		return (0 <= index && index < l_args.size()) ? l_args.get(index) : null;
	}
	
	/**
	 * Returns the first argument with the specific PropBank label.
	 * @param label the PropBank label.
	 * @return the first argument with the specific PropBank label.
	 */
	public PBArg getFirstArg(String label)
	{
		for (PBArg arg : l_args)
			if (arg.isLabel(label))
				return arg;
		
		return null;
	}
	
	/**
	 * Returns the list of all arguments of this instance.
	 * @return the list of all arguments of this instance.
	 */
	public List<PBArg> getArgs()
	{
		return l_args;
	}
	
	/**
	 * Adds the specific argument to this instance.
	 * @param arg the argument to be added.
	 */
	public void addArg(PBArg arg)
	{
		l_args.add(arg);
	}
	
	/**
	 * Adds the specific collection of arguments to this instance.
	 * @param args the collection of arguments to be added.
	 */
	public void addArgs(Collection<PBArg> args)
	{
		l_args.addAll(args);
	}
	
	/**
	 * Removes the specific collection of arguments from this instance.
	 * @param args the collection of arguments to be removed.
	 */
	public void removeArgs(Collection<PBArg> args)
	{
		l_args.removeAll(args);
	}
	
	/**
	 * Removes all argument with the specific label.
	 * @param label the PropBank label.
	 */
	public void removeArgs(String label)
	{
		List<PBArg> remove = new ArrayList<PBArg>();
		
		for (PBArg arg : l_args)
		{
			if (arg.isLabel(label))
				remove.add(arg);
		}
		
		l_args.removeAll(remove);
	}
	
	/**
	 * Sorts the arguments of this instance.
	 * @see PBArg#sortLocs()
	 */
	public void sortArgs()
	{
		for (PBArg arg : l_args)
			arg.sortLocs();
				
		Collections.sort(l_args);
	}
	
	/**
	 * Returns the number of arguments in this instance.
	 * @return the number of arguments in this instance.
	 */
	public int getArgSize()
	{
		return l_args.size();
	}
	
	/**
	 * Returns the constituent tree associated with this instance.
	 * If the tree is not set, returns {@code null}.
	 * @see PBInstance#setTree(CTTree)
	 * @return the constituent tree associated with this instance.
	 */
	public CTTree getTree()
	{
		return tree;
	}
	
	/**
	 * Returns {@link PBInstance#treePath}+"_"+{@link PBInstance#treeId}+"_"+{@link PBInstance#predId}.
	 * @return {@link PBInstance#treePath}+"_"+{@link PBInstance#treeId}+"_"+{@link PBInstance#predId}.
	 */
	public String getKey()
	{
		return getKey(predId);
	}
	
	/**
	 * Returns {@link PBInstance#treePath}+"_"+{@link PBInstance#treeId}+"_"+{@code PredId}.
	 * @param predId the predicate ID.
	 * @return {@link PBInstance#treePath}+"_"+{@link PBInstance#treeId}+"_"+{@code PredId}.
	 */
	public String getKey(int predId)
	{
		StringBuilder build = new StringBuilder();
		
		build.append(treePath);
		build.append("_");
		build.append(treeId);
		build.append("_");
		build.append(predId);
		
		return build.toString();
	}
	
	/**
	 * Sets the constituent tree associated with this instance.
	 * @param tree the constituent tree to be set.
	 */
	public void setTree(CTTree tree)
	{
		this.tree = tree;
	}
	
	/**
	 * Returns {@code true} if the predicate of this instance is a compound noun of a light verb.
	 * @return {@code true} if the predicate of this instance is a compound noun of a light verb.
	 */
	public boolean isLVNounPredicate(CTTree cTree)
	{
		if (!isNounPredicate())	return false;
		PBArg rel = getFirstArg(PBLib.PB_REL);
		if (rel == null)		return false;
		
		for (PBLoc loc : rel.l_locs)
		{
			if (CTLibEn.isVerb(cTree.getNode(loc)))
				return true;
		}
		
		return false;
	}
	
	/**
	 * Returns {@code true} if the predicate of this instance is a verb.
	 * In other words, the instance type ends with "-v".
	 * @return {@code true} if the predicate of this instance is a verb.
	 */
	public boolean isVerbPredicate()
	{
		return type.endsWith("-v");
	}
	
	/**
	 * Returns {@code true} if the predicate of this instance is a noun.
	 * In other words, the instance type ends with "-n".
	 * @return {@code true} if the predicate of this instance is a noun.
	 */
	public boolean isNounPredicate()
	{
		return type.endsWith("-n");
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		StringBuilder build = new StringBuilder();
		
		build.append(treePath);		build.append(PBLib.DELIM_INST);
		build.append(treeId);		build.append(PBLib.DELIM_INST);
		build.append(predId);		build.append(PBLib.DELIM_INST);
		build.append(annotator);	build.append(PBLib.DELIM_INST);
		build.append(type);			build.append(PBLib.DELIM_INST);
		build.append(roleset);		build.append(PBLib.DELIM_INST);
		build.append(aspects);
		
		for (PBArg arg : l_args)
		{
			build.append(PBLib.DELIM_INST);
			build.append(arg.toString());
		}
		
		return build.toString();
	}
	
	@Override
	public int compareTo(PBInstance instance)
	{
		int cmp;
		
		if ((cmp = treePath.compareTo(instance.treePath)) != 0)	return cmp;
		if ((cmp = treeId - instance.treeId) != 0)	return cmp;
		if ((cmp = predId - instance.predId) != 0)	return cmp;
		
		return roleset.compareTo(instance.roleset);
	}
}
