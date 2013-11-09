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
package com.clearnlp.propbank.frameset;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

import org.w3c.dom.Element;

import com.clearnlp.constant.universal.UNPunct;
import com.clearnlp.io.FileExtFilter;
import com.clearnlp.util.UTXml;
import com.google.common.collect.Maps;

/**
 * @since 1.4.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class MultiFrames extends AbstractFrames
{
	static private final long serialVersionUID = 63545998371283421L;
	private Map<String,PBFrameset> m_verbs;
	private Map<String,PBFrameset> m_nouns;
	
	/** @param framesDir the directory containing PropBank frame files. */
	public MultiFrames(String framesDir)
	{
		init();
		addFramesets(framesDir);
	}
	
	public void init()
	{
		m_verbs = Maps.newHashMap();
		m_nouns = Maps.newHashMap();
	}
	
	/** @param framesDir the directory containing PropBank frame files. */
	public void addFramesets(String framesDir)
	{
		String[] filelist = new File(framesDir).list(new FileExtFilter(".*.xml"));
		InputStream in;
		String lemma;
		PBType type;
		int idx;
		
		try
		{
			for (String filename : filelist)	// filename = "study-v.xml"
			{
				in = new BufferedInputStream(new FileInputStream(framesDir+UNPunct.FORWARD_SLASH+filename));
				idx = filename.length() - 6;
				
				if (idx > 0)
				{
					lemma = filename.substring(0, idx);
					type = getType(filename.substring(idx+1, idx+2));
					if (type != null) addFrameset(in, lemma, type);
				}
			}
		}
		catch (Exception e) {e.printStackTrace();}
	}
	
	private PBType getType(String value)
	{
		if (PBType.VERB.isValue(value))	return PBType.VERB;
		if (PBType.NOUN.isValue(value))	return PBType.NOUN;
		
		return null;
	}

	/**
	 * @param in the input-stream of a PropBank frame file.
	 * @param lemma the base lemma (e.g., "run", but not "run_out"). 
	 */
	public void addFrameset(InputStream in, String lemma, PBType type)
	{
		try
		{
			Element eFrameset = UTXml.getDocumentElement(in);
			addFrameset(eFrameset, lemma, type);
		}
		catch (Exception e) {e.printStackTrace();}
	}
	
	/** @param lemma the base lemma (e.g., "run", but not "run_out"). */
	public void addFrameset(Element eFrameset, String lemma, PBType type)
	{
		addFrameset(new PBFrameset(eFrameset, lemma), type);
	}
	
	public void addFrameset(PBFrameset frameset, PBType type)
	{
		     if (type == PBType.VERB)
			m_verbs.put(frameset.getLemma(), frameset);
		else if (type == PBType.NOUN)
			m_nouns.put(frameset.getLemma(), frameset);
	}
	
	/** @param lemma the base lemma (e.g., "run", but not "run_out"). */
	public PBFrameset getFrameset(PBType type, String lemma)
	{
		if (type == PBType.VERB)	return m_verbs.get(lemma);
		if (type == PBType.NOUN)	return m_nouns.get(lemma);
		
		return null;
	}
	
	public Map<String,PBFrameset> getFramesetMap(PBType type)
	{
		if (type == PBType.VERB)	return m_verbs;
		if (type == PBType.NOUN)	return m_nouns;
		
		return null;
	}

	/** @param lemma the base lemma (e.g., "run", but not "run_out"). */
	public PBRoleset getRoleset(PBType type, String lemma, String rolesetID)
	{
		PBFrameset frameset = getFrameset(type, lemma);
		
		if (frameset != null)
			return frameset.getRoleset(rolesetID);
		
		return null;
	}
}
