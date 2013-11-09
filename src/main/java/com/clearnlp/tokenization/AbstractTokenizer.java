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
package com.clearnlp.tokenization;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.clearnlp.util.pair.StringBooleanPair;

/**
 * @since 1.1.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
abstract public class AbstractTokenizer
{
	protected boolean b_twit   = false;
	protected boolean b_userId = true;
	
	/**
	 * Returns a list of token in the specific reader.
	 * @param fin the reader to retrieve tokens from.
	 * @return a list of token in the specific reader.
	 */
	public List<String> getTokens(BufferedReader fin)
	{
		List<String> tokens = new ArrayList<String>();
		String line;
		
		try
		{
			while ((line = fin.readLine()) != null)
				tokens.addAll(getTokens(line.trim()));
		}
		catch (IOException e) {e.printStackTrace();}
		
		return tokens;
	}
	
	/**
	 * Returns a list of tokens from the specific string.
	 * @param str the string to retrieve tokens from.
	 * @return a list of tokens from the specific string.
	 */
	public List<String> getTokens(String str)
	{
		List<StringBooleanPair> lTokens = getTokenList(str);
		List<String> tokens = new ArrayList<String>(lTokens.size());
		
		for (StringBooleanPair token : lTokens)
			tokens.add(token.s);
		
		return tokens;
	}
	
	public void setTwit(boolean isTwit)
	{
		b_twit = isTwit;
	}
	
	public void setUserID(boolean isUserID)
	{
		b_userId = isUserID;
	}
	
	abstract public List<StringBooleanPair> getTokenList(String str);
}

