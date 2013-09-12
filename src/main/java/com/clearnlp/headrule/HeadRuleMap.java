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
package com.clearnlp.headrule;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Headrule map.
 * @see HeadRule
 * @since 1.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
@SuppressWarnings("serial")
public class HeadRuleMap extends HashMap<String, HeadRule>
{
	/** The delimiter between columns ({@code "\t"}). */
	static final public String DELIM_COLUMN = "\t";
	
	/**
	 * Constructs a headrule map from the specific reader.
	 * @param in the reader containing headrules.
	 * Each line indicates the headrule for a specific phrase.
	 * See <a target="_blank" href="http://code.google.com/p/clearnlp/source/browse/trunk/headrules/headrule_en_ontonotes.txt">headrule_en_ontonotes.txt</a> for a sample headrule file.
	 */
	public HeadRuleMap(BufferedReader fin)
	{
		String   line, pTag, dir, rule;
		String[] tmp;
		
		try
		{
			while ((line = fin.readLine()) != null)
			{
				tmp  = line.split(DELIM_COLUMN);
				pTag = tmp[0];
				dir  = tmp[1];
				rule = tmp[2];
				
				put(pTag, new HeadRule(dir, getTagSets(rule)));
			}
		}
		catch (IOException e) {e.printStackTrace();}
	}
	
	/** Called by {@link HeadRuleMap#HeadRuleMap(BufferedReader)}. */
	private String[][] getTagSets(String rule)
	{
		String[] sets = rule.split(HeadRule.DELIM_TAGSETS);
		int i, size = sets.length;
		
		String[][] tagsets = new String[size][];
		
		for (i=0; i<size; i++)
			tagsets[i] = sets[i].split("\\"+HeadTagSet.DELIM_TAGS);

		return tagsets;
	}
	
	/* (non-Javadoc)
	 * @see java.util.AbstractMap#toString()
	 */
	public String toString()
	{
		StringBuilder build = new StringBuilder();
		List<String>   keys = new ArrayList<String>(keySet());
		Collections.sort(keys);
		HeadRule rule;
		
		for (String key : keys)
		{
			build.append(key);
			build.append(DELIM_COLUMN);
			
			rule = get(key);
			if (rule.isRightToLeft())	build.append(HeadRule.DIR_RIGHT_TO_LEFT);
			else						build.append(HeadRule.DIR_LEFT_TO_RIGHT);
			
			build.append(DELIM_COLUMN);
			build.append(rule.toString());
	
			build.append("\n");
		}
		
		return build.toString().trim();
	}
}
