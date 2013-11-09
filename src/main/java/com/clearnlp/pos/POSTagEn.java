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
package com.clearnlp.pos;

/**
 * @since 2.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public enum POSTagEn
{
	/** The pos tag of emails. */
	ADD("ADD"),
	/** The pos tag of affixes. */
	AFX("AFX"),
	/** The pos tag of coordinating conjunctions. */
	CC("CC"),
	/** The pos tag of cardinal numbers. */
	CD("CD"),
	/** The pos tag of codes. */
	CODE("CODE"),
	/** The pos tag of determiners. */
	DT("DT"),
	/** The pos tag of existentials. */
	EX("EX"),
	/** The pos tag of foreign words. */
	FW("FW"),
	/** The pos tag of prepositions or subordinating conjunctions. */
	IN("IN"),
	/** The pos tag of adjectives. */
	JJ("JJ"),
	/** The pos tag of comparative adjectives. */
	JJR("JJR"),
	/** The pos tag of superlative adjectives. */
	JJS("JJS"),
	/** The pos tag of list item markers. */
	LS("LS"),
	/** The pos tag of modals. */
	MD("MD"),
	/** The pos tag of singular or mass nouns. */
	NN("NN"),
	/** The pos tag of plural nouns. */
	NNS("NNS"),
	/** The pos tag of singular proper nouns. */
	NNP("NNP"), 
	/** The pos tag of plural proper nouns. */
	NNPS("NNPS"),
	/** The pos tag of predeterminers. */
	PDT("PDT"),
	/** The pos tag of possessive endings. */
	POS("POS"),
	/** The pos tag of personal pronouns. */
	PRP("PRP"),
	/** The pos tag of possessive pronouns. */
	PRPS("PRP$"),
	/** The pos tag of adverbs. */
	RB("RB"),
	/** The pos tag of comparative adverbs. */
	RBR("RBR"),
	/** The pos tag of superlative adverbs. */
	RBS("RBS"),
	/** The pos tag of particles. */
	RP("RP"), 
	/** The pos tag of "to". */
	TO("TO"),
	/** The pos tag of interjections. */
	UH("UH"),
	/** The pos tag of base form verbs. */
	VB("VB"),
	/** The pos tag of past tense verbs. */
	VBD("VBD"),
	/** The pos tag of gerunds. */
	VBG("VBG"),
	/** The pos tag of past participles. */
	VBN("VBN"),
	/** The pos tag of non-3rd person singular present verbs. */
	VBP("VBP"),
	/** The pos tag of 3rd person singular present verbs. */
	VBZ("VBZ"),
	/** The pos tag of wh-determiners. */
	WDT("WDT"),
	/** The pos tag of wh-pronouns. */
	WP("WP"),
	/** The pos tag of possessive wh-pronouns. */
	WPS("WP$"),
	/** The pos tag of wh-adverbs. */
	WRB("WRB"), 
	
	/** The pos tag of dollar signs. */
	DOLLAR("$"),
	/** The pos tag of colons. */	
	COLON(":"),
	/** The pos tag of commas. */
	COMMA(","),
	/** The pos tag of periods. */
	PERIOD("."),
	/** The pos tag of left quotes. */
	LQ("``"),
	/** The pos tag of right quotes. */
	RQ("''"),
	/** The pos tag of left round brackets. */
	LRB("-LRB-"),
	/** The pos tag of right round brackets. */
	RRB("-RRB-"),
	/** The pos tag of hyphens. */
	HYPH("HYPH"),
	/** The pos tag of superfluous punctuation. */
	NFP("NFP"),
	/** The pos tag of symbols. */
	SYM("SYM"),
	/** Punctuation. */
	PUNC("PUNC");
	
	private final String tag;
	
	POSTagEn(String tag)
	{
		this.tag = tag;
	}
	
	@Override
	public String toString()
	{
		return tag;
	}
}
