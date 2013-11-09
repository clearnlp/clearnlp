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
package com.clearnlp.dependency;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import com.clearnlp.reader.DEPReader;


/**
 * Dependency feature map.
 * @since 1.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
@SuppressWarnings("serial")
public class DEPFeat extends HashMap<String,String>
{
	/** The delimiter between feature values ({@code ","}). */
	static public final String DELIM_VALUES    = ",";
	/** The delimiter between features ({@code "|"}). */
	static public final String DELIM_FEATS     = "|";
	/** The delimiter between keys and values ({@code "="}). */
	static public final String DELIM_KEY_VALUE = "=";
	
	static public final Pattern P_FEATS = Pattern.compile("\\"+DELIM_FEATS);

	/** Constructs an empty feature map. */
	public DEPFeat() {}
	
	/**
	 * Constructs a feature map by decoding the specific features.
	 * @param feats the features to be added.
	 * See the {@code feats} parameter in {@link DEPFeat#add(String)}.
	 */
	public DEPFeat(String feats)
	{
		add(feats);
	}
		
	/**
	 * Adds the specific features to this map.
	 * @param feats {@code "_"} or {@code feat(|feat)*}.<br>
	 * {@code "_"}: indicates no feature.<br>
	 * {@code feat ::= key=value} (e.g., {@code pos=VBD}).
	 */
	public void add(String feats)
	{
		if (feats.equals(DEPReader.BLANK_COLUMN))
			return;
		
		String key, value;
		int    idx;
		
		for (String feat : P_FEATS.split(feats))
		{
			idx = feat.indexOf(DELIM_KEY_VALUE);
			
			if (idx > 0)
			{
				key   = feat.substring(0, idx);
				value = feat.substring(idx+1);
				put(key, value);				
			}
		}
	}

	/* (non-Javadoc)
	 * @see java.util.AbstractMap#toString()
	 */
	public String toString()
	{
		if (isEmpty())	return DEPReader.BLANK_COLUMN;
		
		StringBuilder build = new StringBuilder();
		List<String>  keys  = new ArrayList<String>(keySet());
		
		Collections.sort(keys);
		for (String key : keys)
		{
			build.append(DELIM_FEATS);
			build.append(key);
			build.append(DELIM_KEY_VALUE);
			build.append(get(key));
		}
		
		return build.toString().substring(DELIM_FEATS.length());
	}
}
