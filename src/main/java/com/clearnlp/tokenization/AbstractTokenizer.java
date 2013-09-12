/**
* Copyright 2012-2013 University of Massachusetts Amherst
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
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

