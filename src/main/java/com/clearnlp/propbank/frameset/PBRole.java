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
