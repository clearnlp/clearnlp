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
import java.util.regex.Pattern;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.clearnlp.constant.universal.UNConstant;
import com.clearnlp.morphology.AbstractAffixMatcher;
import com.clearnlp.morphology.AbstractAffixReplacer;
import com.clearnlp.pattern.PTLib;
import com.clearnlp.util.UTXml;
import com.google.common.collect.Lists;

/**
 * @since 2.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class EnglishAffixMatcherFactory
{
	final String ELEM_AFFIX             = "affix";
	final String ELEM_RULE              = "rule";
	final String ATTR_TYPE              = "type";
	final String ATTR_FORM              = "form";
	final String ATTR_POS               = "pos";
	final String ATTR_ORG_POS           = "org_pos";
	final String ATTR_BASE_POS          = "base_pos";
	final String ATTR_AFFIX_FORM        = "affix_form";
	final String ATTR_REPLACEMENTS      = "replacements";
	final String ATTR_DOUBLE_CONSONANTS = "doubleConsonants";
	final String VAL_SUFFIX             = "suffix";
	
	public List<AbstractAffixMatcher> createAffixMatchers(Element eAffixes)
	{
		List<AbstractAffixMatcher> affixes = Lists.newArrayList();
		NodeList list = eAffixes.getElementsByTagName(ELEM_AFFIX);
		int i, size = list.getLength();
		Element eAffix;
		
		for (i=0; i<size; i++)
		{
			eAffix = (Element)list.item(i);
			affixes.add(createAffixMatcher(eAffix));
		}
		
		return affixes;
	}
	
	public AbstractAffixMatcher createAffixMatcher(Element eAffix)
	{
		String   type = UTXml.getTrimmedAttribute(eAffix, ATTR_TYPE);
		String   form = UTXml.getTrimmedAttribute(eAffix, ATTR_FORM);
		String    pos = UTXml.getTrimmedAttribute(eAffix, ATTR_POS);
		String orgPOS = UTXml.getTrimmedAttribute(eAffix, ATTR_ORG_POS);
		Pattern  oPOS = orgPOS.equals(UNConstant.EMPTY) ? null : Pattern.compile("^("+orgPOS+")$");
		
		boolean bSuffix = type.equals(VAL_SUFFIX);
		AbstractAffixMatcher matcher;
		
		if (bSuffix)	matcher = new EnglishSuffixMatcher(form, pos, oPOS);
		else			throw new IllegalArgumentException("Invalid affix type: "+type);
		
		NodeList list = eAffix.getElementsByTagName(ELEM_RULE);
		AbstractAffixReplacer replacer;
		int i, size = list.getLength();
		
		for (i=0; i<size; i++)
		{
			replacer = getAffixReplacer(bSuffix, (Element)list.item(i));
			if (replacer != null) matcher.addReplacer(replacer);
		}
		
		return matcher;
	}
	
	/** Called by {@link #createAffixMatcher(Element)}. */
	private AbstractAffixReplacer getAffixReplacer(boolean bSuffix, Element eRule)
	{
		String   basePOS      = UTXml.getTrimmedAttribute(eRule, ATTR_BASE_POS); 
		String   affixForm    = UTXml.getTrimmedAttribute(eRule, ATTR_AFFIX_FORM);
		String[] replacements = PTLib.splitCommas(UTXml.getTrimmedAttribute(eRule, ATTR_REPLACEMENTS));
		
		String dc = UTXml.getTrimmedAttribute(eRule, ATTR_DOUBLE_CONSONANTS);
		boolean doubleConsonants = dc.equals(UNConstant.EMPTY) ? false : Boolean.parseBoolean(dc);
		
		return bSuffix ? new EnglishSuffixReplacer(basePOS, affixForm, replacements, doubleConsonants) : null;
	}
}
