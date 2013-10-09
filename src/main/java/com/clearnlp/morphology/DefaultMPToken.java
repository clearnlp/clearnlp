/**
* Copyright 2013 IPSoft Inc.
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
package com.clearnlp.morphology;

import java.util.ArrayDeque;
import java.util.Deque;

import com.clearnlp.constant.universal.UNPunct;
import com.clearnlp.reader.DEPReader;

/**
 * @since 2.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class DefaultMPToken
{
	static public final String DELIM = UNPunct.PLUS; 
	protected Deque<Morpheme> q_morphemes;
	
	public DefaultMPToken()
	{
		q_morphemes = new ArrayDeque<Morpheme>();
	}
	
	public void addFirst(Morpheme morpheme)
	{
		q_morphemes.addFirst(morpheme);		
	}
	
	public void addLast(Morpheme morpheme)
	{
		q_morphemes.addLast(morpheme);		
	}
	
	public Morpheme removeFirst(Morpheme morpheme)
	{
		return q_morphemes.removeFirst();		
	}
	
	public Morpheme removeLast(Morpheme morpheme)
	{
		return q_morphemes.removeLast();		
	}
	
	public boolean isEmpty()
	{
		return q_morphemes.isEmpty();
	}
	
	public String toString()
	{
		StringBuilder build = new StringBuilder();
		int len = DELIM.length();
		
		for (Morpheme morpheme : q_morphemes)
		{
			build.append(DELIM);
			build.append(morpheme.toString());
		}
		
		return build.length() > len ? build.substring(len) : DEPReader.DELIM_COLUMN;
	}
}
