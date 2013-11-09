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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import com.carrotsearch.hppc.IntOpenHashSet;
import com.clearnlp.constituent.CTTree;


/**
 * PropBank argument.
 * @see PBLoc
 * @since 1.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class PBArg implements Comparable<PBArg>
{
	/** The label of this argument. */
	public    String      label;
	/** The locations of this argument. */
	protected List<PBLoc> l_locs;
	
	/** Constructs a PropBank argument. */
	public PBArg()
	{
		l_locs = new ArrayList<PBLoc>();
	}
	
	/**
	 * Constructs a PropBank argument using the specific string.
	 * @param str {@code <location>(<type><location>)*-label}.
	 */
	public PBArg(String str)
	{
		int    idx;
		String type;

		l_locs = new ArrayList<PBLoc>();
		idx = str.indexOf(PBLib.DELIM_LABEL);
	
		if (idx == -1)
		{
			System.err.println("Error: illegal format - "+str);
			System.exit(1);
		}
		
		label = str.substring(idx+1);
		StringTokenizer tok = new StringTokenizer(str.substring(0, idx), PBLib.LOC_TYPES, true);
		
		if (!tok.hasMoreTokens())
		{
			System.err.println("Error: illegal format - "+str);
			System.exit(1);
		}
		
		addLoc(new PBLoc(tok.nextToken(), ""));
		
		while (tok.hasMoreTokens())
		{
			type = tok.nextToken();
		
			if (!tok.hasMoreTokens())
			{
				System.err.println("Error: illegal format - "+str);
				System.exit(1);
			}
			
			addLoc(new PBLoc(tok.nextToken(), type));
		}
	}
	
	/**
	 * Returns {@code true} if the specific label equals to this argument's label.
	 * @param label the label to be compared.
	 * @return {@code true} if the specific label equals to this argument's label.
	 */
	public boolean isLabel(String label)
	{
		return this.label.equals(label);
	}
	
	/**
	 * Returns the index'th location of this argument.
	 * If the index is out-of-range, returns {@code null}.
	 * @param index the index of the location to be returned.
	 * @return the index'th location of this argument.
	 */
	public PBLoc getLoc(int index)
	{
		return (0 <= index && index < l_locs.size()) ? l_locs.get(index) : null;
	}
	
	/**
	 * Returns the location matching the specific terminal ID and height.
	 * If there is no such location, returns {@code null}.
	 * @param terminalId the terminal ID to be compared.
	 * @param height the height to be compared.
	 * @return the location matching the specific terminal ID and height.
	 */
	public PBLoc getLoc(int terminalId, int height)
	{
		for (PBLoc loc : l_locs)
		{
			if (loc.equals(terminalId, height))
				return loc;
		}
		
		return null;
	}
	
	/**
	 * Returns all the locations of this argument.
	 * @return all the locations of this argument.
	 */
	public List<PBLoc> getLocs()
	{
		return l_locs;
	}
	
	/**
	 * Returns a set of terminal IDs belonging to this argument given the specific tree.
	 * @param tree the constituent tree.
	 * @return a set of terminal IDs belonging to this argument.
	 */
	public IntOpenHashSet getTerminalIdSet(CTTree tree)
	{
		IntOpenHashSet set = new IntOpenHashSet();
		
		for (PBLoc loc : l_locs)
			set.addAll(tree.getNode(loc).getSubTerminalIdSet());
		
		return set;
	}
	
	/**
	 * Returns the sorted list of terminal IDs belonging to this argument give the specific tree.
	 * @param tree the constituent tree.
	 * @return the sorted list of terminal IDs belonging to this argument give the specific tree.
	 */
	public int[] getSortedTerminalIdList(CTTree tree)
	{
		IntOpenHashSet set = getTerminalIdSet(tree);
		int[] ids = set.toArray();
		
		Arrays.sort(ids);
		return ids;
	}
	
	/**
	 * Adds the specific location to this argument.
	 * @param loc the location to be added.
	 */
	public void addLoc(PBLoc loc)
	{
		l_locs.add(loc);
	}
	
	/**
	 * Adds the specific collection of locations to this argument.
	 * @param locs the collection of locations to be added.
	 */
	public void addLocs(Collection<PBLoc> locs)
	{
		l_locs.addAll(locs);
	}
	
	/**
	 * Removes the first location matching the specific terminal ID and height from this argument.
	 * @param terminalId the terminal ID of the location.
	 * @param height the height of the location.
	 */
	public void removeLoc(int terminalId, int height)
	{
		for (PBLoc loc : l_locs)
		{
			if (loc.equals(terminalId, height))
			{
				l_locs.remove(loc);
				break;
			}
		}
	}
	
	/**
	 * Removes the specific collection of locations from this argument.
	 * @param locs the collection of locations to be removed.
	 */
	public void removeLocs(Collection<PBLoc> locs)
	{
		l_locs.removeAll(locs);
		if (!l_locs.isEmpty())	l_locs.get(0).type = "";
	}
	
	/**
	 * Replaces the locations of this argument.
	 * @param locs the locations to be added.
	 */
	public void replaceLocs(List<PBLoc> locs)
	{
		l_locs = locs;
	}
	
	/**
	 * Sorts the locations of this argument by their terminal IDs.
	 * @see PBLoc#compareTo(PBLoc)
	 */
	public void sortLocs()
	{
		if (l_locs.isEmpty())	return;
		
		Collections.sort(l_locs);
		PBLoc fst = l_locs.get(0), loc;
		
		if (!fst.type.equals(""))
		{
			for (int i=1; i<l_locs.size(); i++)
			{
				loc = l_locs.get(i);
				
				if (loc.type.equals(""))
				{
					loc.type = fst.type;
					break;
				}
			}
			
			fst.type = "";
		}
	}
	
	/**
	 * Returns the number of locations in this argument.
	 * @return the number of locations in this argument.
	 */
	public int getLocSize()
	{
		return l_locs.size();
	}
	
	/**
	 * Returns {@code true} if this argument has no location.
	 * @return {@code true} if this argument has no location.
	 */
	public boolean hasNoLoc()
	{
		return l_locs.isEmpty();
	}
	
	/**
	 * Returns {@code true} if this argument has the specific location type.
	 * @param type the type of a location to be found.
	 * @return {@code true} if this argument has the specific location type.
	 */
	public boolean hasType(String type)
	{
		for (PBLoc loc : l_locs)
		{
			if (loc.isType(type))
				return true;
		}
		
		return false;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		StringBuilder build = new StringBuilder();
		
		for (PBLoc loc : l_locs)
			build.append(loc.toString());
				
		build.append(PBLib.DELIM_LABEL);
		build.append(label);
		
		return build.toString();
	}
	
	@Override
	public int compareTo(PBArg arg)
	{
		return getLoc(0).compareTo(arg.getLoc(0));
	}
}
