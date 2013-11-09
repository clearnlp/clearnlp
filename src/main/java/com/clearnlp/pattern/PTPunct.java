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
package com.clearnlp.pattern;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import jregex.MatchResult;
import jregex.Replacer;
import jregex.Substitution;
import jregex.TextBuffer;

import com.clearnlp.util.pair.Pair;


/**
 * @since 2.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class PTPunct
{
	static final public Pattern PUNCT_CHAR   = Pattern.compile("\\p{Punct}");
	static final public Pattern PUNCT_ONLY   = Pattern.compile("^\\p{Punct}+$");
	static final public Pattern PUNCT_PERIOD = Pattern.compile("^(\\.|\\?|\\!)+$");
	
	static final private jregex.Pattern PUNCT_REPEAT = new jregex.Pattern("\\.{2,}|\\!{2,}|\\?{2,}|\\-{2,}|\\*{2,}|\\={2,}|\\~{2,}|\\,{2,}");	// ".","!","?","-","*","=","~",","
	static final private Replacer PUNCT_REPEAT_REPLACE = PUNCT_REPEAT.replacer(new Substitution()
	{
		public void appendSubstitution(MatchResult match, TextBuffer dest)
		{
			char c = match.group(0).charAt(0);
			dest.append(c);
			dest.append(c);
		}
	});
	
	@SuppressWarnings("serial")
	static private final List<Pair<Pattern, String>> BRACKET_LIST = new ArrayList<Pair<Pattern,String>>()
	{{
		add(new Pair<Pattern,String>(Pattern.compile("-LRB-"), "("));
		add(new Pair<Pattern,String>(Pattern.compile("-RRB-"), ")"));
		add(new Pair<Pattern,String>(Pattern.compile("-LSB-"), "["));
		add(new Pair<Pattern,String>(Pattern.compile("-RSB-"), "]"));
		add(new Pair<Pattern,String>(Pattern.compile("-LCB-"), "{"));
		add(new Pair<Pattern,String>(Pattern.compile("-RCB-"), "}"));
		
		trimToSize();
	}};

	/**
	 * Collapses redundant punctuation in the specific word-form (e.g., {@code "!!!" -> "!!"}).
	 * @return the collapsed form.
	 */
	static public String collapsePunctuation(String form)
	{
		return PTPunct.PUNCT_REPEAT_REPLACE.replace(form);
	}
	
	/** @return {@code true} if the specific word-form contains any punctuation. */
	static public boolean containsAnyPunctuation(String form)
	{
		return PUNCT_CHAR.matcher(form).find();
	}
	
	/** @return {@code true} if the specific word-form contains only punctuation. */
	static public boolean containsOnlyPunctuation(String form)
	{
		return PUNCT_ONLY.matcher(form).find();
	}
	
	/** @return {@code true} if the specific word-form contains any of the specified punctuation. */
	static public boolean containsAnySpecificPunctuation(String form, char... punctuation)
	{
		int i, size = form.length();
		
		for (i=0; i<size; i++)
		{
			for (char p : punctuation)
			{
				if (form.charAt(i) == p)
					return true;	
			}
		}
		
		return false;
	}
	
	/** @return {@code true} if the specific word-form is {@code "."}, {@code "?"}, or {@code "!"}. */
	static public boolean isPeriodLike(String form)
	{
		if (PUNCT_PERIOD.matcher(form).find())
			return true;
		
		if (form.length() > 1 && form.charAt(0) == '/')
			return PUNCT_PERIOD.matcher(form.substring(1)).find();
		
		return false;
	}

	/**
	 * Reverts coded brackets to their original forms (e.g., from {@code "-LBR-"} to {@code "("}).
	 * @return the reverted form of coded brackets.
	 */
	static public String revertBracket(String form)
	{
		for (Pair<Pattern,String> p : BRACKET_LIST)
			form = p.o1.matcher(form).replaceAll(p.o2);
		
		return form;
	}
}
