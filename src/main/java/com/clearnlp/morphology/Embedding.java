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
 * Copyright 2012/09-2013/04, University of Massachusetts Amherst
 * Copyright 2013/05-Present, IPSoft Inc.
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
package com.clearnlp.morphology;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.clearnlp.pattern.PTLib;
import com.clearnlp.util.UTArray;
import com.clearnlp.util.UTMath;
import com.clearnlp.util.pair.StringDoublePair;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @since 2.0.2
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class Embedding implements Serializable
{
	private static final long serialVersionUID = 8939407738519904380L;
	private final String DELIM = " ";
	
	private Map<String,double[]> m_1gram;
	private Map<String,double[]> m_ngram;
	
	public Embedding() {}
	
	public Embedding(InputStream in)
	{
		init(in);
	}
	
	@SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		m_1gram = (Map<String,double[]>)in.readObject();
		m_ngram = (Map<String,double[]>)in.readObject();
	}

	private void writeObject(ObjectOutputStream out) throws IOException
	{
		out.writeObject(m_1gram);
		out.writeObject(m_ngram);
	}
	
	public void init(InputStream in)
	{
		try
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			m_1gram = Maps.newHashMap();
			m_ngram = Maps.newHashMap();
			String line, phrase;
			double[] vector;
			String[] t;
			
			while ((line = reader.readLine()) != null)
			{
				t      = PTLib.splitTabs(line);
				phrase = t[0];
				vector = UTArray.toDoubleArray(t[1], PTLib.SPACE);
				getMap(phrase).put(phrase, vector);
			}
			
			in.close();
		}
		catch (IOException e) {e.printStackTrace();}
	}
	
	public double[] getEmbedding(String phrase)
	{
		return getMap(phrase).get(phrase);
	}
	
	private Map<String,double[]> getMap(String phrase)
	{
		return phrase.contains(DELIM) ? m_ngram : m_1gram;
	}
	
	public List<StringDoublePair> getSimilarPhrases(String phrase, int top)
	{
		return getSimilarPhrases(phrase, top, true);
	}
	
	public List<StringDoublePair> getSimilarPhrases(String phrase, int top, boolean only1gram)
	{
		List<StringDoublePair> ps = Lists.newArrayList();
		double[] curr = getEmbedding(phrase);
		if (curr == null)	return ps;
		
		getSimilarPhrasesAux(phrase, top, curr, ps, m_1gram);
		if (!only1gram)	getSimilarPhrasesAux(phrase, top, curr, ps, m_ngram);
			
		return ps;
	}
	
	private void getSimilarPhrasesAux(String phrase, int top, double[] curr, List<StringDoublePair> ps, Map<String,double[]> map)
	{
		StringDoublePair p;
		double sim;
		
		for (String key : map.keySet())
		{
			if (phrase.equals(key))	continue;
			sim = UTMath.cosineSimilarity(curr, map.get(key));
		
			if (ps.size() < top)
				ps.add(new StringDoublePair(key, sim));
			else
			{
				p = ps.get(top-1);
				if (p.d < sim) p.set(key, sim);
			}
			
			Collections.sort(ps, Collections.reverseOrder());
		}		
	}
}
