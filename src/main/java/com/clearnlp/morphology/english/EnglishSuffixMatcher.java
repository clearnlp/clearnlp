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

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.clearnlp.morphology.AbstractAffixMatcher;
import com.clearnlp.morphology.AbstractAffixReplacer;
import com.clearnlp.util.map.Prob2DMap;
import com.google.common.collect.Maps;


/**
 * @since 2.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class EnglishSuffixMatcher extends AbstractAffixMatcher
{
	public EnglishSuffixMatcher(String affixCanonicalForm, String affixPOS, Pattern originalPOS)
	{
		super(affixCanonicalForm, affixPOS, originalPOS);
	}
	
	@Override
	public String getBaseForm(Map<String,Set<String>> baseMap, String form, String pos)
	{
		if (!matchesOriginalPOS(pos)) return null;
		String base;
		
		for (AbstractAffixReplacer replacer : l_replacers)
		{
			base = replacer.getBaseForm(baseMap, form);
			if (base != null) return base;
		}
		
		return null;
	}
	
	@Override
	public String getBaseForm(Set<String> baseSet, String form, String pos)
	{
		if (!matchesOriginalPOS(pos)) return null;
		String base;
		
		for (AbstractAffixReplacer replacer : l_replacers)
		{
			base = replacer.getBaseForm(baseSet, form);
			if (base != null) return base;
		}
		
		return null;
	}
	
	public void evaluateInflection(Map<String,Map<String,Prob2DMap>> smap, Set<String> baseSet, String goldBase, String form, String pos)
	{
		if (!matchesOriginalPOS(pos)) return;

		String key = s_affixCanonicalForm+"_"+p_originalPOS.toString();
		Map<String,Prob2DMap> rmap = smap.get(key);
		
		if (rmap == null)
		{
			rmap = Maps.newHashMap();
			smap.put(key, rmap);
		}
		
		for (AbstractAffixReplacer replacer : l_replacers)
			((EnglishSuffixReplacer)replacer).evaluateInflection(rmap, baseSet, goldBase, form);
	}
	
	public void evaluateDerivation(Map<String,Map<String,Prob2DMap>> smap, Map<String,Set<String>> baseMap, String goldPOS, String form)
	{
		String key = s_affixCanonicalForm+"_"+p_originalPOS.toString();
		Map<String,Prob2DMap> rmap = smap.get(key);
		boolean correct = matchesOriginalPOS(goldPOS);
		
		if (rmap == null)
		{
			rmap = Maps.newHashMap();
			smap.put(key, rmap);
		}
		
		for (AbstractAffixReplacer replacer : l_replacers)
			((EnglishSuffixReplacer)replacer).evaluateDerivation(rmap, baseMap, correct, form);
	}
}
