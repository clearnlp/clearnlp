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
package com.clearnlp.segmentation;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.clearnlp.tokenization.AbstractTokenizer;

/**
 * @since 1.1.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class EnglishSegmenter extends AbstractSegmenter
{
	/** Patterns of terminal punctuation. */
	protected final Pattern  P_TERMINAL_PUNCTUATION = Pattern.compile("^(\\.|\\?|\\!)+$");
	protected final String[] L_BRACKETS = {"\"","(","{","["};
	protected final String[] R_BRACKETS = {"\"",")","}","]"};
	
	public EnglishSegmenter(AbstractTokenizer tokenizer)
	{
		super(tokenizer);
	}
	
	@Override
	public List<List<String>> getSentences(BufferedReader fin)
	{
		List<List<String>> sentences = new ArrayList<List<String>>();
		List<String> tokens = g_tokenizer.getTokens(fin);
		int[] brackets = new int[R_BRACKETS.length];
		int bIdx, i, size = tokens.size();
		boolean isTerminal = false;
		String curr;
		
		for (i=0, bIdx=0; i<size; i++)
		{
			curr = tokens.get(i);
			countBrackets(curr, brackets);
			
			if (isTerminal || P_TERMINAL_PUNCTUATION.matcher(curr).find())
			{
				if (i+1 < size && isFollowedByBracket(tokens.get(i+1), brackets))
				{
					isTerminal = true;
					continue;
				}
				
				sentences.add(tokens.subList(bIdx, bIdx = i+1));
				isTerminal = false;
			}
		}
		
		if (bIdx < size)
			sentences.add(tokens.subList(bIdx, size));
		
		return sentences;
	}
		
	/** Called by {@link EnglishSegmenter#getSentencesRaw(BufferedReader)}. */
	private void countBrackets(String str, int[] brackets)
	{
		if (str.equals("\""))
			brackets[0] += (brackets[0] == 0) ? 1 : -1;
		else
		{
			int i, size = brackets.length;
			
			for (i=1; i<size; i++)
			{
				if      (str.equals(L_BRACKETS[i]))
					brackets[i]++;
				else if (str.equals(R_BRACKETS[i]))
					brackets[i]--; 
			}
		}
	}
	
	/** Called by {@link EnglishSegmenter#getSentencesRaw(BufferedReader)}. */
	private boolean isFollowedByBracket(String str, int[] brackets)
	{
		int i, size = R_BRACKETS.length;
		
		for (i=0; i<size; i++)
		{
			if (brackets[i] > 0 && str.equals(R_BRACKETS[i]))
				return true;
		}
		
		return false;
	}		
}
