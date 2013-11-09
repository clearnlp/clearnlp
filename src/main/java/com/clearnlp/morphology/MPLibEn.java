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
package com.clearnlp.morphology;

import java.util.regex.Pattern;

import com.clearnlp.constituent.CTLibEn;


/**
 * Morphology library for English.
 * @since 1.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class MPLibEn extends MPLib
{
	/** Derivations of a verb "be". */
	static public final Pattern RE_BE = Pattern.compile("^(be|been|being|am|is|was|are|were|'m|'s|'re)$");
	static public final Pattern BE_FINITE = Pattern.compile("^(am|is|was|are|were)$");
	/** Derivations of a verb "become". */
	static public final Pattern RE_BECOME	= Pattern.compile("^(become|becomes|became|becoming)$");
	/** Derivations of a verb "get". */
	static public final Pattern RE_GET	= Pattern.compile("^(get|gets|got|gotten|getting)$");
	/** Derivations of a verb "have". */
	static public final Pattern RE_HAVE	= Pattern.compile("^(have|has|had|having|'ve|'d)$");
	/** Derivations of a verb "do". */
	static public final Pattern RE_DO		= Pattern.compile("^(do|does|did|done|doing)$");
	/** Common wh-pronouns. */
	static public final Pattern RE_WH_COMMON = Pattern.compile("^(how|what|which|who|whom|whose|where|when|why)$");
	/** Negations. */
	static public final Pattern RE_NEG = Pattern.compile("^(never|not|n't|'nt|no)$");

	static public String[][] RULE_SUFFIXES = {
		{"s"	,""},		{"ses"	,"s"},		{"xes"	,"x"},		{"zes"	,"z"},
		{"ches"	,"ch"},		{"shes"	,"sh"},		{"men"	,"man"},	{"ies"	,"y"},
		{"es"	,"", "e"},	{"ed"	,"", "e"},	{"ing"	,"", "e"},	{"er"	,"", "e"},
		{"est"	,"", "e"},	{"ment"	,"", "e"},	{"ion"	,"", "e"},	{"able"	,"", "e"},
		{"en"	,"", "e"},	{"ize"	,"", "y"},	{"less"	,""},		{"ness"	,""},
		{"iness","y"},		{"ful"	,""},		{"iful"	,"y"},		{"ly"	,""},		{"ily"	,"y"}};
	
//	static public String[] RULE_PREFIXES = {"a","anti","be","de","dis","en","in","mal","mis","over","post","pre","re","un","under"};
	
	/**
	 * Returns {@code true} if the specific word-form is a derivation of a verb "be".
	 * @param form the word-form to be compared.
	 * @return {@code true} if the specific form is a derivation of a verb "be".
	 */
	static public boolean isBe(String form)
	{
		return RE_BE.matcher(form.toLowerCase()).find();
	}
	
	/**
	 * Returns {@code true} if the specific word-form is a derivation of a verb "get".
	 * @param form the word-form to be compared.
	 * @return {@code true} if the specific form is a derivation of a verb "get".
	 */
	static public boolean isGet(String form)
	{
		return RE_GET.matcher(form.toLowerCase()).find();
	}
	
	/**
	 * Returns {@code true} if the specific word-form is a derivation of a verb "become".
	 * @param form the word-form to be compared.
	 * @return {@code true} if the specific form is a derivation of a verb "become".
	 */
	static public boolean isBecome(String form)
	{
		return RE_BECOME.matcher(form.toLowerCase()).find();
	}
	
	/**
	 * Returns {@code true} if the specific word-form is a derivation of a verb "have".
	 * @param form the word-form to be compared.
	 * @return {@code true} if the specific form is a derivation of a verb "have".
	 */
	static public boolean isHave(String form)
	{
		return RE_HAVE.matcher(form.toLowerCase()).find();
	}
	
	/**
	 * Returns {@code true} if the specific word-form is a derivation of a verb "do".
	 * @param form the word-form to be compared.
	 * @return {@code true} if the specific form is a derivation of a verb "do".
	 */
	static public boolean isDo(String form)
	{
		return RE_DO.matcher(form.toLowerCase()).find();
	}
	
	/**
	 * Returns {@code true} if the specific POS tag is a noun.
	 * @param pos the POS tag to be compared.
	 * @return {@code true} if the specific POS tag is a noun.
	 */
	static public boolean isNoun(String pos)
	{
		return pos.startsWith(CTLibEn.POS_NN) || pos.equals(CTLibEn.POS_PRP) || pos.equals(CTLibEn.POS_WP);
	}
	
	/**
	 * Returns {@code true} if the specific POS tag is a verb.
	 * @param pos the POS tag to be compared.
	 * @return {@code true} if the specific POS tag is a verb.
	 */
	static public boolean isVerb(String pos)
	{
		return pos.startsWith(CTLibEn.POS_VB);
	}
	
	/**
	 * Returns {@code true} if the specific POS tag is a adjective.
	 * @param pos the POS tag to be compared.
	 * @return {@code true} if the specific POS tag is a adjective.
	 */
	static public boolean isAdjective(String pos)
	{
		return pos.startsWith(CTLibEn.POS_JJ);
	}
	
	/**
	 * Returns {@code true} if the specific POS tag is a adverb.
	 * @param pos the POS tag to be compared.
	 * @return {@code true} if the specific POS tag is a adverb.
	 */
	static public boolean isAdverb(String pos)
	{
		return pos.startsWith(CTLibEn.POS_RB) || pos.equals(CTLibEn.POS_WRB);
	}
	
	/**
	 * @param fpos a fine-grained POS tag.
	 * @return the coarse-grained POS tag of the specific fine-grained POS tag.
	 */
	static public String toCPOSTag(String fpos)
	{
		if (isAdjective(fpos))	return CTLibEn.POS_JJ;
		if (isAdverb(fpos))		return CTLibEn.POS_RB;
		if (isNoun(fpos))		return CTLibEn.POS_NN;
		if (isVerb(fpos))		return CTLibEn.POS_VB;
		
		return fpos;
	}
	
	static public boolean isVowel(char c)
	{
		c = Character.toLowerCase(c);
		return c == 'a' || c == 'e' || c == 'i' || c == 'o' || c == 'u' || c == 'w' || c == 'y';
	}
}
