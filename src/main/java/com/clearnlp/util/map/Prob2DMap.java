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

import java.util.HashMap;

import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import com.carrotsearch.hppc.cursors.ObjectCursor;
import com.clearnlp.collection.map.ObjectIntHashMap;
import com.clearnlp.util.pair.Pair;
import com.clearnlp.util.pair.StringDoublePair;
import com.clearnlp.util.pair.StringIntPair;



@SuppressWarnings("serial")
public class Prob2DMap extends HashMap<String,ObjectIntHashMap<String>>
{
	static private final String TOTAL = "_T_";
	private int i_total;
	
	public void add(String key, String value)
	{
		add(key, value, 1);
	}
	
	public void add(String key, String value, int count)
	{
		ObjectIntHashMap<String> map;
		
		if (containsKey(key))
		{
			map = get(key);
			
			map.put(value, map.get(value)+count);
			map.put(TOTAL, map.get(TOTAL)+count);
		}
		else
		{
			map = new ObjectIntHashMap<String>();
			put(key, map);

			map.put(value, count);
			map.put(TOTAL, count);
		}
		
		i_total += count;
	}
	
	public int getTotal1D(String key)
	{
		return containsKey(key) ? get(key).get(TOTAL) : 0;
	}
	
	public int getTotal2D()
	{
		return i_total;
	}
	
	public StringIntPair[] getCounts(String key)
	{
		ObjectIntHashMap<String> map = get(key);
		if (map == null) return null;
		String value;
		int i = 0;
		
		StringIntPair[] ps = new StringIntPair[map.size()-1];
		
		for (ObjectCursor<String> cur : map.keys())
		{
			if (!(value = cur.value).equals(TOTAL))
				ps[i++] = new StringIntPair(value, map.get(value));
		}
		
		return ps;
	}
	
	public StringDoublePair[] getProb1D(String key)
	{
		Pair<Double,StringDoublePair[]> p = getProb1DAux(key);
		return (p == null) ? null : p.o2;
	}
	
	public StringDoublePair getBestProb1D(String key)
	{
		StringDoublePair[] ps = getProb1D(key);
		
		if (ps != null)
		{
			StringDoublePair max = ps[0];
			int i, size = ps.length;
			
			for (i=1; i<size; i++)
			{
				if (ps[i].d > max.d)
					max = ps[i];
			}
			
			return max;
		}
		
		return null;
	}
	
	public StringDoublePair[] getProb2D(String key)
	{
		Pair<Double,StringDoublePair[]> p = getProb1DAux(key);
		if (p == null)	return null;
		
		double prior = p.o1;
		StringDoublePair[] probs = p.o2;
		
		for (StringDoublePair prob : probs)
			prob.d *= prior;
		
		return probs;
	}
	
	private Pair<Double,StringDoublePair[]> getProb1DAux(String key)
	{
		ObjectIntOpenHashMap<String> map = get(key);
		if (map == null)	return null;
		
		StringDoublePair[] probs = new StringDoublePair[map.size()-1];
		int i = 0, total = map.get(TOTAL);
		String value;
		
		for (ObjectCursor<String> cur : map.keys())
		{
			value = cur.value;
			
			if (!value.equals(TOTAL))
				probs[i++] = new StringDoublePair(value, (double)map.get(value)/total);
		}
		
		double prior = (double)total / i_total; 
		return new Pair<Double,StringDoublePair[]>(prior, probs);
	}
}
