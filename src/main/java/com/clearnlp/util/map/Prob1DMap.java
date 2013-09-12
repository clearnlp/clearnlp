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
package com.clearnlp.util.map;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import com.carrotsearch.hppc.cursors.ObjectCursor;
import com.clearnlp.util.UTCollection;
import com.clearnlp.util.pair.StringIntPair;


public class Prob1DMap extends ObjectIntOpenHashMap<String>
{
	private int i_total;
	
	public void add(String key)
	{
		put(key, get(key)+1);
		i_total++;
	}
	
	public void addAll(Collection<String> col)
	{
		for (String s : col)
			add(s);
	}
	
	public double getProb(String key)
	{
		return (double)get(key) / i_total;
	}
	
	public List<StringIntPair> toSortedList()
	{
		List<StringIntPair> list = new ArrayList<StringIntPair>();
		String key;
		
		for (ObjectCursor<String> cur : keys())
		{
			key = cur.value;
			list.add(new StringIntPair(key, get(key)));
		}
		
		UTCollection.sortReverseOrder(list);
		return list;
	}
	
	public Set<String> toSet(int cutoff)
	{
		Set<String> set = new HashSet<String>();
		String key;
		
		for (ObjectCursor<String> cur : keys())
		{
			key = cur.value;
			
			if (get(key) > cutoff)
				set.add(key);
		}
		
		return set;
	}
}
