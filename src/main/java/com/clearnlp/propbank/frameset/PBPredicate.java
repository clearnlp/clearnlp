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
package com.clearnlp.propbank.frameset;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.clearnlp.constant.universal.UNConstant;
import com.clearnlp.util.UTXml;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * @since 1.4.2
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class PBPredicate implements Serializable, Comparable<PBPredicate>
{
	private static final long serialVersionUID = 2334930059960115328L;
	private Map<String,PBRoleset> m_rolesets;
	private Set<String> s_rolesetIDs;
	private String s_lemma;
	
	public PBPredicate(Element ePredicate)
	{
		init();
		
		setLemma(UTXml.getTrimmedAttribute(ePredicate, PBFLib.A_LEMMA));
		addRolesets(ePredicate.getElementsByTagName(PBFLib.E_ROLESET));
	}
	
	public void init()
	{
		m_rolesets   = Maps.newHashMap();
		s_rolesetIDs = Sets.newHashSet();
	}
	
	public void addRolesets(NodeList list)
	{
		int i, size = list.getLength();
		
		for (i=0; i<size; i++)
			addRoleset((Element)list.item(i));
	}
	
	public void addRoleset(Element eRoleset)
	{
		addRoleset(new PBRoleset(eRoleset));
	}

	public void addRoleset(PBRoleset roleset)
	{
		String id = roleset.getID();
		s_rolesetIDs.add(id);
		
		if (m_rolesets.put(id, roleset) != null)
			System.err.printf("Duplicated roleset: %s\n", id);
	}
	
	public List<PBRoleset> getRolesetSortedList()
	{
		List<PBRoleset> list = Lists.newArrayList(m_rolesets.values());
		
		Collections.sort(list);
		return list;
	}
	
	/** @return {@code null} if not exist. */
	public PBRoleset getRoleset(String id)
	{
		return m_rolesets.get(id);
	}
	
	public Set<String> getRolesetIdSet()
	{
		return s_rolesetIDs;
	}
	
	/** @return the specific lemma of this predicate (e.g., "run_out"). */
	public String getLemma()
	{
		return s_lemma;
	}
	
	/** @param lemma the specific lemma of this predicate (e.g., "run_out"). */
	public void setLemma(String lemma)
	{
		s_lemma = lemma;
	}
	
	/** @param lemma the specific lemma of this predicate (e.g., "run_out"). */
	public boolean isLemma(String lemma)
	{
		return s_lemma.equals(lemma);
	}
	
	public List<PBRoleset> getRolesetsFromVerbNet(String vncls, boolean polysemousOnly)
	{
		List<PBRoleset> rolesets = Lists.newArrayList();
		Set<String> set;
		
		for (PBRoleset roleset : m_rolesets.values())
		{
			set = roleset.getVerbNetClasses();
			
			if (set.contains(vncls) && (set.size() > 1 || !polysemousOnly))
				rolesets.add(roleset);
		}
		
		return rolesets;
	}
	
	@Override
	public String toString()
	{
		StringBuilder build = new StringBuilder();
		build.append("===== "+s_lemma+" =====");
		
		for (PBRoleset roleset : getRolesetSortedList())
		{
			build.append(UNConstant.NEW_LINE);
			build.append(roleset.toString());
		}
		
		return build.toString();
	}

	@Override
	public int compareTo(PBPredicate predicate)
	{
		return s_lemma.compareTo(predicate.s_lemma);
	}
}
