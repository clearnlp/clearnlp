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

import com.clearnlp.util.UTArray;

/**
 * @since 2.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class MPTag
{
	public static final String JOINER = "+";
	
	/** Inflection: verb, irregular. */
	public static final String IVX = "IVX";
	/** Inflection: noun, irregular. */
	public static final String INX = "INX";
	/** Inflection: adjective, irregular. */
	public static final String IJX = "IJX";
	/** Inflection: adverb, irregular. */
	public static final String IRX = "IRX";
	/** Inflection: cardinal, irregular. */
	public static final String ICX = "ICX";
	
	/** Inflectional suffix: noun, plural. */
	public static final String ISP = "ISP";
	/** Inflectional suffix: verb, 3rd-person singular. */
	public static final String ISZ = "ISZ";
	/** Inflectional suffix: verb, past. */
	public static final String ISD = "ISD";
	/** Inflectional suffix: verb, past participial. */
	public static final String ISN = "ISN";
	/** Inflectional suffix: verb, gerund/progressive. */
	public static final String ISG = "ISG";
	/** Inflectional suffix: adjective/adverb, comparative. */
	public static final String ISR = "ISR";
	/** Inflectional suffix: adjective/adverb, superlative. */
	public static final String IST = "IST";
	
	/** Verb derivational suffix. */
	public static final String DSV = "DSV";
	/** Noun derivational suffix. */
	public static final String DSN = "DSN";
	/** Adjective derivational suffix. */
	public static final String DSJ = "DSJ";
	/** Adverb derivational suffix. */
	public static final String DSR = "DSR";
	
	public static final String LEMMA_CARDINAL = "#crd#";
	public static final String LEMMA_ORDINAL  = "#ord#";
	public static final String IRREGULAR_MORPHEME = "*";
	
	public static String join(String... tags)
	{
		return UTArray.join(tags, JOINER);
	}
}
