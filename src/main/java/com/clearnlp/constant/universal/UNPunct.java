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
package com.clearnlp.constant.universal;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @since 1.4.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class UNPunct
{
	static final private Set<String> VALUE_SET = new HashSet<String>(getValueList());

	static public final String PLUS					= "+";
	static public final String EQUAL				= "=";
	static public final String PIPE					= "|";
	static public final String FORWARD_SLASH		= "/";
	static public final String UNDERSCORE			= "_";
	static public final String HYPHEN				= "-";
	static public final String COMMA				= ",";
	static public final String COLON				= ":";
	static public final String QUESTION_MARK		= "?";
	static public final String PERIOD				= ".";
	static public final String LEFT_ROUND_BRACKET	= "(";
	static public final String RIGHT_ROUND_BRACKET	= ")";
	
	/**
	 * @param lemma a lower-case string.
	 * @return {@code true} if this class contains the specific lemma; otherwise, {@code false}.
	 */
	static public boolean contains(String lemma)
	{
		return VALUE_SET.contains(lemma);
	}
	
	/** @return a list containing all field values of this class. */
	static public List<String> getValueList()
	{
		List<String> list = new ArrayList<String>();
		Class<UNPunct> cs = UNPunct.class;
		
		try
		{
			for (Field f : cs.getFields())
			{
				list.add(f.get(cs).toString());
			}
		}
		catch (IllegalArgumentException e) {e.printStackTrace();}
		catch (IllegalAccessException e)   {e.printStackTrace();}
		
		return list;
	}
}
