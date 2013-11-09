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

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.clearnlp.constant.universal.UNConstant;
import com.clearnlp.constant.universal.UNPunct;
import com.clearnlp.pattern.PTNumber;
import com.clearnlp.propbank.PBLib;
import com.clearnlp.util.UTXml;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * @since 1.4.2
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class PBRoleset implements Serializable, Comparable<PBRoleset>
{
	static Logger LOG = Logger.getLogger(PBRoleset.class.getName());
	
	private static final long serialVersionUID = 1632699410581892419L;
	private Map<String,PBRole> m_roles;
	private Set<String> s_vncls;
	private String s_name;
	private String s_id;
	
	public PBRoleset(Element eRoleset)
	{
		init();
		
		setID(UTXml.getTrimmedAttribute(eRoleset, PBFLib.A_ID));
		setName(UTXml.getTrimmedAttribute(eRoleset, PBFLib.A_NAME));
		addVerbNetClasses(UTXml.getTrimmedAttribute(eRoleset, PBFLib.A_VNCLS));
		addRoles(eRoleset.getElementsByTagName(PBFLib.E_ROLE));
	}
	
	public void init()
	{
		s_vncls = Sets.newHashSet();
		m_roles = Maps.newHashMap();
	}
	
	private void addVerbNetClasses(String classes)
	{
		if (!classes.equals(UNConstant.EMPTY) && !classes.equals(UNPunct.HYPHEN))
		{
			for (String vncls : classes.split(UNConstant.SPACE))
				addVerbNetClass(vncls);
		}
	}
	
	public void addVerbNetClass(String vncls)
	{
		s_vncls.add(vncls);
	}
	
	public void addRoles(NodeList list)
	{
		int i, size = list.getLength();
		
		for (i=0; i<size; i++)
			addRole((Element)list.item(i));
	}
	
	public void addRole(Element eRole)
	{
		addRole(new PBRole(eRole));
	}
	
	public void addRole(PBRole role)
	{
		if (!isValidAnnotation(role))
		{
			if (!s_id.endsWith(PBLib.LIGHT_VERB))
				LOG.debug("Invalid argument: "+s_id+" - "+role.getArgKey()+"\n");
		}
		else
		{
			m_roles.put(role.getArgNumber(), role);
			
			for (String vncls : role.getVNClasses())
			{
				if (!s_vncls.contains(vncls))
					System.err.printf("VerbNet class mismatch: %s - %s\n", s_id, role.getArgKey());
			}
		}
	}
	
	private boolean isValidAnnotation(PBRole role)
	{
		String n = role.getArgNumber();
		if (n.length() != 1) return false;
		
		if (PTNumber.containsOnlyDigits(n))	return true;
		if (role.isArgNumber("A"))			return true;
		if (role.isArgNumber("M") && !role.isFunctionTag(UNConstant.EMPTY))	return true;
		
		return false;
	}
	
	public Set<String> getVerbNetClasses()
	{
		return s_vncls;
	}
	
	public List<PBRole> getRoleSortedList()
	{
		List<PBRole> list = Lists.newArrayList(m_roles.values());
		
		Collections.sort(list);
		return list;
	}
	
	/** @param argNumber e.g., {@code "0"}, {@code "2"}. */
	public PBRole getRole(String argNumber)
	{
		return m_roles.get(argNumber);
	}
	
	public String getFunctionTag(String argNumber)
	{
		PBRole role = getRole(argNumber);
		return (role != null) ? role.getFunctionTag() : UNConstant.EMPTY;
	}

	public String getID()
	{
		return s_id;
	}
	
	public String getName()
	{
		return s_name;
	}
	
	public void setID(String id)
	{
		s_id = id;
	}
	
	public void setName(String name)
	{
		s_name = name;
	}
	
	public boolean isValidArgumentNumber(String n)
	{
		return m_roles.containsKey(n);
	}
	
	public boolean isValidArgument(String label)
	{
		// TODO: to be removed
		if (s_id.endsWith(PBLib.LIGHT_VERB))
			return true;	

		String n = PBLib.getNumber(label);
		return (n != null) ? m_roles.containsKey(n) : true;
	}
	
	@Override
	public String toString()
	{
		StringBuilder build = new StringBuilder();
		
		build.append(s_id);
		build.append(": ");
		build.append(s_name);
		
		for (String vncls : s_vncls)
		{
			build.append(", ");
			build.append(vncls);
		}
		
		build.append(UNConstant.NEW_LINE);
		
		for (PBRole role : getRoleSortedList())
		{
			build.append(role.toString());
			build.append(UNConstant.NEW_LINE);
		}
		
		return build.toString();
	}

	@Override
	public int compareTo(PBRoleset roleset)
	{
		return s_id.compareTo(roleset.s_id);
	}
}
