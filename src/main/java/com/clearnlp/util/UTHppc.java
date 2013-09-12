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
package com.clearnlp.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.carrotsearch.hppc.IntContainer;
import com.carrotsearch.hppc.IntOpenHashSet;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import com.carrotsearch.hppc.cursors.ObjectCursor;

public class UTHppc
{
	static public int max(IntContainer c)
	{
		int max = Integer.MIN_VALUE;
		
		for (int i : c.toArray())
			max = Math.max(max, i);
		
		return max;
	}
	
	static public int min(IntContainer c)
	{
		int min = Integer.MAX_VALUE;
		
		for (int i : c.toArray())
			min = Math.min(min, i);
		
		return min;
	}
	
	/**
	 * Returns {@code true} if {@code s2} is the subset of {@code s1}.
	 * @return {@code true} if {@code s2} is the subset of {@code s1}.
	 */
	static public boolean isSubset(IntContainer s1, IntContainer s2)
	{
		for (int i : s2.toArray())
		{
			if (!s1.contains(i))
				return false;
		}
		
		return true;
	}
	
	static public IntOpenHashSet intersection(IntContainer c1, IntContainer c2)
	{
		IntOpenHashSet s1 = new IntOpenHashSet(c1);
		IntOpenHashSet s2 = new IntOpenHashSet(c2);
		
		s1.retainAll(s2);
		return s1;
	}
	
	static public List<String> getSortedKeys(ObjectIntOpenHashMap<String> map)
	{
		List<String> keys = new ArrayList<String>(map.size());
		
		for (ObjectCursor<String> cur : map.keys())
			keys.add(cur.value);
		
		Collections.sort(keys);
		return keys;
	}
}
