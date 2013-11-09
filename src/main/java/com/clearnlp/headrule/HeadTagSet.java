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
package com.clearnlp.headrule;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import com.clearnlp.constituent.CTNode;


/**
 * Head tagset.
 * @since 1.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class HeadTagSet
{
	/** The delimiter between tags ({@code "|"}). */
	static final public String DELIM_TAGS  = "|";
	/** The prefix of function tags ({@code '-'}). */
	static final public char   PREFIX_FTAG = '-';
	
	/** The regular expression of phrase/pos tags (e.g., {@code "^(NN.*|NP)$"}). */
	private Pattern     p_tags;
	/** The set of function tags. */
	private Set<String> f_tags;
	
	/**
	 * Construct a new head tagset by decoding the specific tags.
	 * @param tags an array of phrase, pos, or function tags (e.g., {@code {"NN.*","-SBJ","-TPC","NP"}}).
	 * Each phrase/pos tag is in a form of regular expression (e.g., {@code "NN.*", "NP"}).
	 * Each function tag must be preceded by {@link HeadTagSet#PREFIX_FTAG} (e.g., {@code "-SBJ"}).
	 */
	public HeadTagSet(String[] tags)
	{
		StringBuilder pTags = new StringBuilder();
		f_tags = new HashSet<String>();
		
		for (String tag : tags)
		{
			if (tag.charAt(0) == PREFIX_FTAG)
				f_tags.add(tag.substring(1));
			else
			{
				pTags.append(DELIM_TAGS);
				pTags.append(tag);
			}
		}
		
		p_tags = (pTags.length() != 0) ? Pattern.compile("^("+pTags.substring(1)+")$") : null;
	}
	
	/**
	 * Returns {@code true} if the specific node matches any of the tags.
	 * @param node the node to be compared.
	 * @return {@code true} if the specific node matches any of the tags.
	 */
	public boolean matches(CTNode node)
	{
		if (node != null && p_tags != null && p_tags.matcher(node.pTag).find())
			return true;
		else if (node.hasFTagAny(f_tags))
			return true;
		
		return false;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		StringBuilder build = new StringBuilder();
		
		if (p_tags != null)
		{
			String tags = p_tags.pattern().substring(2);
			
			build.append(DELIM_TAGS);
			build.append(tags.substring(0, tags.length()-2));
		}
		
		for (String fTag : f_tags)
		{
			build.append(DELIM_TAGS);
			build.append(PREFIX_FTAG);
			build.append(fTag);
		}
		
		return build.substring(DELIM_TAGS.length());
	}
}
