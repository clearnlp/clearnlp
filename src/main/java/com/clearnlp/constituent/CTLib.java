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
import java.util.Arrays;
import java.util.List;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntIntOpenHashMap;
import com.carrotsearch.hppc.IntObjectOpenHashMap;

/**
 * Constituent library.
 * @since 1.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class CTLib
{
	/** The phrase tag of the top node. */
	static final public String PTAG_TOP = "TOP";
	/** The phrase tag of unknown phrases. */
	static final public String PTAG_X   = "X";
	/** The pos tag of empty categories. */
	static final public String POS_NONE = "-NONE-";
	/** The pos tag of unknown tokens. */
	static final public String POS_XX	= "XX";
	
	/**
	 * Normalizes co-indices and gap-indices of the specific tree.
	 * @param tree the tree to be normalized.
	 */
	static public void normalizeIndices(CTTree tree)
	{
		// retrieve all co-indexes
		IntObjectOpenHashMap<List<CTNode>> mOrg = new IntObjectOpenHashMap<List<CTNode>>();
		getCoIndexMap(tree.getRoot(), mOrg);
		if (mOrg.isEmpty())	return;
		
		int[] keys = mOrg.keys().toArray();
		Arrays.sort(keys);
		
		IntIntOpenHashMap mNew = new IntIntOpenHashMap();		
		int coIndex = 1, last, i;
		List<CTNode> list;
		CTNode curr, ec;
		boolean isAnteFound;
		
		for (int key : keys)
		{
			list = mOrg.get(key);
			last = list.size() - 1;
			isAnteFound = false;
			
			for (i=last; i>=0; i--)
			{
				curr = list.get(i);
				
				if (curr.isEmptyCategoryRec())
				{
					ec = curr.getSubTerminals().get(0);
					
					if (i == last || isAnteFound || CTLibEn.RE_ICH_PPA_RNR.matcher(ec.form).find() || CTLibEn.containsCoordination(curr.getLowestCommonAncestor(list.get(i+1))))
						curr.coIndex = -1;
					else
						curr.coIndex = coIndex++;

					if (isAnteFound || i > 0)
						ec.form += "-"+coIndex;
				}
				else if (isAnteFound)
				{
					curr.coIndex = -1;
				}
				else
				{
					curr.coIndex = coIndex;
					mNew.put(key, coIndex);
					isAnteFound  = true;
				}
			}
			
			coIndex++;
		}
		
		int[] lastIndex = {coIndex};
		remapGapIndices(mNew, lastIndex, tree.getRoot());
	}
	
	/** Called by {@link CTReader#normalizeIndices(CTTree)}. */
	static private void getCoIndexMap(CTNode curr, IntObjectOpenHashMap<List<CTNode>> map)
	{
		if (curr.isPhrase())
		{
			if (curr.coIndex != -1)
			{
				int key = curr.coIndex;
				List<CTNode> list;
				
				if (map.containsKey(key))
					list = map.get(key);
				else
				{
					list = new ArrayList<CTNode>();
					map.put(key, list);
				}
				
				list.add(curr);
			}
			
			for (CTNode child : curr.ls_children)
				getCoIndexMap(child, map);
		}
		else if (curr.isEmptyCategory())
		{
			if (curr.form.equals("*0*"))
				curr.form = "0";
		}
	}
	
	/** Called by {@link CTReader#normalizeIndices(CTTree)}. */
	static private void remapGapIndices(IntIntOpenHashMap map, int[] lastIndex, CTNode curr)
	{
		int gapIndex = curr.gapIndex;
		
		if (map.containsKey(gapIndex))
		{
			curr.gapIndex = map.get(gapIndex);
		}
		else if (gapIndex != -1)
		{
			curr.gapIndex = lastIndex[0];
			map.put(gapIndex, lastIndex[0]++);
		}
		
		for (CTNode child : curr.ls_children)
			remapGapIndices(map, lastIndex, child);
	}
	
	/**
	 * Returns mappings between tokens in the specific constituent trees.
	 * Indexes of the list represent the indexes of tokens in the source tree.
	 * Items of the list represent index-mappings of tokens in in the target trees.
	 * @param tree1 the source constituent tree.
	 * @param tree2 the target constituent tree.
	 * @return mappings between tokens in the specific constituent trees.
	 */
	static public IntArrayList[] getTokenMapList(CTTree tree1, CTTree tree2)
	{
		List<CTNode> tokens1 = tree1.getTokens();
		List<CTNode> tokens2 = tree2.getTokens();
		int size1 = tokens1.size(), size2 = tokens2.size(), tId1, tId2, len1, len2;
		IntArrayList[] map = new IntArrayList[size1];
		String form1, form2;
		
		for (tId1=0; tId1<size1; tId1++)
			map[tId1] = new IntArrayList();
		
		for (tId1=0,tId2=0; tId1 < size1; tId1++,tId2++)
		{
			form1 = tokens1.get(tId1).form;
			form2 = tokens2.get(tId2).form;
			len1  = form1.length();
			len2  = form2.length();
			
			if (len1 < len2)
			{
				while (form1.length() < len2 && ++tId1 < size1)
				{
					form1 += tokens1.get(tId1).form;
					map[tId1-1].add(tId2);
				}
			}
			else if (len1 > len2)
			{
				while (len1 > form2.length() && ++tId2 < size2)
				{
					form2 += tokens2.get(tId2).form;
					map[tId1].add(tId2-1);
				}
			}
			
			if (form1.equals(form2))
				map[tId1].add(tId2);
			else
				return null;
		}
		
		return map;
	}
}
