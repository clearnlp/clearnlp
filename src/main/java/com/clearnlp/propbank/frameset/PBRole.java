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
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.clearnlp.constant.universal.UNConstant;
import com.clearnlp.util.UTXml;
import com.google.common.collect.Maps;

/**
 * @since 1.4.2
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class PBRole implements Serializable, Comparable<PBRole>
{
	private static final long serialVersionUID = 5265112128708396274L;
	private Map<String,String> m_vnroles;
	private String s_argNumber;
	private String s_functionTag;
	private String s_description;
	
	public PBRole(Element eRole)
	{
		init();
		
		setArgNumber(UTXml.getTrimmedAttribute(eRole, PBFLib.A_N));
		setFunctionTag(UTXml.getTrimmedAttribute(eRole, PBFLib.A_F));
		setDescription(UTXml.getTrimmedAttribute(eRole, PBFLib.A_DESCR));
		addVNRoles(eRole.getElementsByTagName(PBFLib.E_VNROLE));
	}
	
	public void init()
	{
		m_vnroles = Maps.newHashMap();
	}
	
	private void addVNRoles(NodeList list)
	{
		int i, size = list.getLength();
		String vncls, vntheta;
		Element eVNRole;
		
		for (i=0; i<size; i++)
		{
			eVNRole = (Element)list.item(i);
			vncls   = UTXml.getTrimmedAttribute(eVNRole, PBFLib.A_VNCLS);
			vntheta = UTXml.getTrimmedAttribute(eVNRole, PBFLib.A_VNTHETA);
			
			if (!vncls.equals(UNConstant.EMPTY) && !vntheta.equals(UNConstant.EMPTY))
				addVNRole(vncls, vntheta);
		}
	}
	
	public void addVNRole(String vncls, String vntheta)
	{
		m_vnroles.put(vncls, vntheta.toLowerCase());
	}
	
	public String getArgNumber()
	{
		return s_argNumber;
	}

	public String getFunctionTag()
	{
		return s_functionTag;
	}

	public String getDescription()
	{
		return s_description;
	}
	
	public Set<String> getVNClasses()
	{
		return m_vnroles.keySet();
	}
	
	/** @return {@code null} if not exist. */
	public String getVNTheta(String vncls)
	{
		return m_vnroles.get(vncls);
	}

	public void setArgNumber(String argNumber)
	{
		s_argNumber = argNumber.toUpperCase();
	}

	public void setFunctionTag(String functionTag)
	{
		s_functionTag = functionTag.toUpperCase();
	}

	public void setDescription(String description)
	{
		s_description = description;
	}
	
	public boolean isArgNumber(String n)
	{
		return s_argNumber.equals(n);
	}
	
	public boolean isFunctionTag(String tag)
	{
		return s_functionTag.equals(tag);
	}
	
	@Override
	public String toString()
	{
		StringBuilder build = new StringBuilder();
		
		build.append(getArgKey());
		build.append(": ");
		build.append(s_description);
		
		for (String vncls : m_vnroles.keySet())
		{
			build.append(", ");
			build.append(vncls);
			build.append(":");
			build.append(m_vnroles.get(vncls));
		}
		
		return build.toString();
	}
	
	public String getArgKey()
	{
		StringBuilder build = new StringBuilder();
		build.append(s_argNumber);
		
		if (!s_functionTag.equals(UNConstant.EMPTY))
		{
			build.append("-");
			build.append(s_functionTag);
		}
		
		return build.toString();
	}
	
	@Override
	public int compareTo(PBRole role)
	{
		int n = s_argNumber.compareTo(role.s_argNumber);
		return (n != 0) ? n : s_functionTag.compareTo(role.s_functionTag);
	}
}
