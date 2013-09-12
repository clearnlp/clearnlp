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
package com.clearnlp.propbank.frameset;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.clearnlp.constant.universal.UNConstant;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @since 1.4.2
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class PBFrameset implements Serializable
{
	private static final long serialVersionUID = 3023563544488823541L;
	private Map<String,PBPredicate> m_predicates;
	private String s_lemma;

	/** @param lemma the base lemma (e.g., "run", but not "run_out"). */
	public PBFrameset(Element eFrameset, String lemma)
	{
		init();
		
		setLemma(lemma);
		addPredicates(eFrameset.getElementsByTagName(PBFLib.E_PREDICATE));
	}
	
	public void init()
	{
		m_predicates = Maps.newHashMap();
	}
	
	public void addPredicates(NodeList list)
	{
		int i, size = list.getLength();
		
		for (i=0; i<size; i++)
			addPredicate((Element)list.item(i));
	}
	
	public void addPredicate(Element element)
	{
		addPredicate(new PBPredicate(element));
	}
	
	public void addPredicate(PBPredicate predicate)
	{
		if (m_predicates.put(predicate.getLemma(), predicate) != null)
			System.err.printf("Duplicated predicate: %s\n", predicate.getLemma());
	}
	
	/** @param lemma the specific lemma of the predicate (e.g., "run_out"). */
	public PBPredicate getPredicate(String lemma)
	{
		return m_predicates.get(lemma);
	}
	
	public PBRoleset getRoleset(String rolesetID)
	{
		PBRoleset roleset;
		
		for (PBPredicate predicate : m_predicates.values())
		{
			roleset = predicate.getRoleset(rolesetID);
			
			if (roleset != null)
				return roleset;
		}
		
		return null;
	}
	
	public List<PBPredicate> getPredicateSortedList()
	{
		List<PBPredicate> list = Lists.newArrayList(m_predicates.values());
		
		Collections.sort(list);
		return list;
	}
	
	/** @return the base lemma (e.g., "run", but not "run_out"). */
	public String getLemma()
	{
		return s_lemma;
	}
	
	/** @param lemma the base lemma (e.g., "run", but not "run_out"). */
	public void setLemma(String lemma)
	{
		s_lemma = lemma;
	}
	
	public List<PBRoleset> getRolesetsFromVerbNet(String vncls, boolean polysemousOnly)
	{
		List<PBRoleset> list = Lists.newArrayList();
		
		for (PBPredicate predicate : m_predicates.values())
			list.addAll(predicate.getRolesetsFromVerbNet(vncls, polysemousOnly));
		
		return list;
	}
	
	public String toString()
	{
		StringBuilder build = new StringBuilder();
		
		for (PBPredicate predicate : getPredicateSortedList())
		{
			build.append(UNConstant.NEW_LINE);
			build.append(predicate.toString());
		}
		
		return build.toString().trim();
	}
}
