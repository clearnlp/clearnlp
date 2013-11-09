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

import com.clearnlp.classification.model.AbstractModel;
import com.clearnlp.morphology.AbstractAffixReplacer;
import com.clearnlp.util.map.Prob2DMap;

/**
 * @since 2.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class EnglishSuffixReplacer extends AbstractAffixReplacer
{
	boolean b_doubleConsonants;
	
	public EnglishSuffixReplacer(String basePOS, String affixForm, String[] replacements, boolean doubleConsonants)
	{
		super(basePOS, affixForm, replacements);
		b_doubleConsonants = doubleConsonants;
	}
	
	@Override
	public String getBaseForm(Map<String,Set<String>> baseMap, String form)
	{
		return getBaseForm(baseMap.get(s_basePOS), form);
	}

	@Override
	public String getBaseForm(Set<String> baseSet, String form)
	{
		if (!form.endsWith(s_affixForm)) return null;

		int    subLen = form.length() - s_affixForm.length();
		String stem   = form.substring(0, subLen);
		String base   = getBaseFormAux(baseSet, stem);
		
		if (b_doubleConsonants && base == null && isDoubleConsonant(form, subLen))
		{
			stem = form.substring(0, subLen-1);
			base = getBaseFormAux(baseSet, stem);
		}
		
		return base;
	}
	
	private String getBaseFormAux(Set<String> baseSet, String stem)
	{
		String base;
		
		for (String replacement : s_replacements)
		{
			base = stem + replacement;
			
			if (baseSet.contains(base))
				return base;
		}
		
		return null;
	}
	
	private boolean isDoubleConsonant(String form, int subLen)
	{
		return subLen >= 4 && form.charAt(subLen-2) == form.charAt(subLen-1);
	}
	
	public void evaluateInflection(Map<String,Prob2DMap> rmap, Set<String> baseSet, String goldBase, String form)
	{
		if (!form.endsWith(s_affixForm)) return;
		Prob2DMap map = rmap.get(s_affixForm);
		
		if (map == null)
		{
			map = new Prob2DMap();
			rmap.put(s_affixForm, map);
		}

		int subLen = form.length() - s_affixForm.length();
		String stem = form.substring(0, subLen);
		evaluateInflectionAux(map, baseSet, goldBase, stem, "");
		
		if (isDoubleConsonant(form, subLen))
		{
			stem = form.substring(0, subLen-1);
			evaluateInflectionAux(map, baseSet, goldBase, stem, "*");
		}
	}
	
	private void evaluateInflectionAux(Prob2DMap map, Set<String> baseSet, String goldBase, String stem, String dc)
	{
		String base, value;
		
		for (String replacement : s_replacements)
		{
			base = stem + replacement;
			
			if (baseSet.contains(base))
			{
				value = base.equals(goldBase) ? AbstractModel.LABEL_TRUE : AbstractModel.LABEL_FALSE;
				map.add(dc+replacement, value);
			}
		}
	}
	
	public void evaluateDerivation(Map<String,Prob2DMap> rmap, Map<String,Set<String>> baseMap, boolean correct, String form)
	{
		if (!form.endsWith(s_affixForm)) return;
		Prob2DMap map = rmap.get(s_affixForm);
		
		if (map == null)
		{
			map = new Prob2DMap();
			rmap.put(s_affixForm, map);
		}

		int subLen = form.length() - s_affixForm.length();
		String stem = form.substring(0, subLen);
		Set<String> baseSet = baseMap.get(s_basePOS);
		
		evaluateDerivationAux(map, baseSet, correct, stem);
	}
	
	private void evaluateDerivationAux(Prob2DMap map, Set<String> baseSet, boolean correct, String stem)
	{
		
		String base, value;
		
		for (String replacement : s_replacements)
		{
			base = stem + replacement;
			
			if (baseSet.contains(base))
			{
				value = correct ? AbstractModel.LABEL_TRUE : AbstractModel.LABEL_FALSE;
				map.add(replacement, value);
			}
		}
	}
}
