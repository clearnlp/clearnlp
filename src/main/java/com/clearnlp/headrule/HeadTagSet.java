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
