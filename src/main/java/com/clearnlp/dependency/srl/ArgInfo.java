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
package com.clearnlp.dependency.srl;

import java.util.ArrayDeque;
import java.util.Deque;

import com.clearnlp.util.pair.Pair;

/**
 * @since 1.4.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class ArgInfo
{
	private int predicateId;
	private String semanticInfo;
	private Deque<Pair<String,String>> syntacticInfo;
	
	public ArgInfo()
	{
		semanticInfo  = null;
		syntacticInfo = new ArrayDeque<Pair<String,String>>();
	}
	
	public int getPredicateId()
	{
		return predicateId;
	}
	
	public void setPredicateId(int predicateId)
	{
		this.predicateId = predicateId;
	}
	
	public String getSemanticInfo()
	{
		return semanticInfo;
	}
	
	public void setSemanticInfo(String semantic)
	{
		this.semanticInfo = semantic;
	}
	
	public boolean hasSemanticInfo()
	{
		return semanticInfo != null;
	}
	
	/** {@link Pair#o1}: dependency label, {@link Pair#o2}: lemma. */
	public Pair<String,String> popNextSyntacticInfo()
	{
		return syntacticInfo.pop();
	}
	
	public void pushSyntacticInfo(String deprel, String lemma)
	{
		syntacticInfo.push(new Pair<String,String>(deprel, lemma));
	}
	
	public boolean hasSyntacticInfo()
	{
		return !syntacticInfo.isEmpty();
	}
}
