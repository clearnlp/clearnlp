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

import com.clearnlp.dependency.factory.DefaultArgInfoDatum;
import com.clearnlp.dependency.factory.DefaultArgInfoDatumFactory;
import com.clearnlp.dependency.factory.IArgInfoDatum;
import com.clearnlp.dependency.factory.IArgInfoDatumFactory;
import com.clearnlp.pattern.PTLib;
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
	
	public Deque<Pair<String,String>> getSyntacticInfo()
	{
		return syntacticInfo;
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
	
	public IArgInfoDatum getArgInfoDatum()
	{
		return getArgInfoDatum(new DefaultArgInfoDatumFactory());
	}
	
	public IArgInfoDatum getArgInfoDatum(IArgInfoDatumFactory factory)
	{
		IArgInfoDatum datum = new DefaultArgInfoDatum();
		
		datum.setPredicateID(predicateId);
		datum.setSemanticInfo(semanticInfo);
		datum.setSyntacticInfo(fromSyntacticInfoToString());
		
		return datum;
	}
	
	static public ArgInfo buildFrom(IArgInfoDatum datum)
	{
		ArgInfo info = new ArgInfo();
		
		info.predicateId   = datum.getPredicateID();
		info.semanticInfo  = datum.getSemanticInfo();
		info.syntacticInfo = fromStringToSyntacticInfo(datum.getSyntacticInfo());
		
		return info;
	}
	
	private String fromSyntacticInfoToString()
	{
		StringBuilder build = new StringBuilder();
		
		for (Pair<String,String> p : syntacticInfo)
		{
			build.append(p.o1);
			build.append(" ");
			build.append(p.o2);
			build.append(" ");
		}
		
		return build.toString().trim();
	}
	
	private static Deque<Pair<String,String>> fromStringToSyntacticInfo(String syntacticInfo)
	{
		Deque<Pair<String,String>> deque = new ArrayDeque<Pair<String,String>>();
		String[] tokens = PTLib.SPACE.split(syntacticInfo);
		int i, size = tokens.length;
		
		for (i=0; i+1<size; i+=2)
			deque.add(new Pair<String,String>(tokens[i], tokens[i+1]));
		
		return deque;
	}
}
