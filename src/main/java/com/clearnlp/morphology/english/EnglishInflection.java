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
package com.clearnlp.morphology.english;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.clearnlp.morphology.AbstractAffixMatcher;

/**
 * @since 2.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class EnglishInflection
{
	String                     base_pos;
	Set<String>                base_set;
	String                     exception_pos;
	Map<String,String>         exception_map;
	List<AbstractAffixMatcher> suffix_matchers;
	
	public EnglishInflection(String basePOS, Set<String> baseSet, Map<String,String> exceptionMap, List<AbstractAffixMatcher> affixMatchers)
	{
		init(basePOS, baseSet, exceptionMap, affixMatchers);
	}
	
	private void init(String basePOS, Set<String> baseSet, Map<String,String> exceptionMap, List<AbstractAffixMatcher> affixMatchers)
	{
		base_pos        = basePOS;
		base_set        = baseSet;
		exception_map   = exceptionMap;
		suffix_matchers = affixMatchers;
		
		if      (base_set == null)
			throw new IllegalArgumentException("The base set must not be null.");
		else if (suffix_matchers == null)
			throw new IllegalArgumentException("The suffix matcher list must not be null.");
	}
	
	public String getBasePOS()
	{
		return base_pos;
	}
	
	public Set<String> getBaseSet()
	{
		return base_set;
	}
	
	public Map<String,String> getExceptionMap()
	{
		return exception_map;
	}
	
	public List<AbstractAffixMatcher> getSuffixMatchers()
	{
		return suffix_matchers;
	}
	
	public boolean isBaseForm(String form)
	{
		return base_set.contains(form);
	}
	
	/** @param form the word-form in lower-case. */
	public String getBaseForm(String form, String pos)
	{
		String token;
		
		if ((token = getBaseFormFromExceptions(form)) != null)
			return token;
		
		if ((token = getBaseFormFromSuffixes(form, pos)) != null)
			return token;
		
		return null;
	}

	public String getBaseFormFromExceptions(String form)
	{ 
		String base;
		
		if (exception_map != null && (base = exception_map.get(form)) != null)
			return base;
		
		return null;
	}
	
	public String getBaseFormFromSuffixes(String form, String pos)
	{
		String base;
		
		for (AbstractAffixMatcher matcher : suffix_matchers)
		{
			base = matcher.getBaseForm(base_set, form, pos);
			if (base != null) return base;
		}
		
		return null;
	}
}
