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
package com.clearnlp.propbank.verbnet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.clearnlp.util.UTXml;


@SuppressWarnings("serial")
public class PVRoleset extends HashMap<String,PVRoles>
{
	static public final String ATTR_ID = "id";
	String s_rolesetId;
	
	public PVRoleset(Element eRoleset, String rolesetId, boolean fromMap)
	{
		init(eRoleset, rolesetId, fromMap);
	}
	
	private void init(Element eRoleset, String rolesetId, boolean fromMap)
	{
		s_rolesetId = rolesetId;
		
		if (fromMap)	initRolesFromMap(eRoleset);
		else			initRolesFromFrameset(eRoleset);
	}
	
	private void initRolesFromMap(Element eRoleset)
	{
		NodeList list = eRoleset.getElementsByTagName(PVMap.E_ROLES);
		int i, size = list.getLength();
		Element eRoles;
		String  vncls;
		
		for (i=0; i<size; i++)
		{
			eRoles = (Element)list.item(i);
			vncls  = UTXml.getTrimmedAttribute(eRoles, PVRoles.ATTR_VNCLS);
			
			put(vncls, new PVRoles(eRoles, vncls));
		}
	}
	
	private void initRolesFromFrameset(Element eRoleset)
	{
		String[] vnclses = UTXml.getTrimmedAttribute(eRoleset, PVRoles.ATTR_VNCLS).split(" ");
		NodeList nRoles  = eRoleset.getElementsByTagName(PVMap.E_ROLE);
		PVRoles  pvRoles;
		
		for (String vncls : vnclses)
		{
			if (vncls.isEmpty() || vncls.equals("-"))	continue;
			pvRoles = new PVRoles(nRoles, vncls);
			
			if (pvRoles.isEmpty())
				System.err.println("Mismatch: "+s_rolesetId+" "+vncls);
			else
				put(vncls, pvRoles);
		}
	}
	
	public PVRoles getSubVNRoles(String superVNClass)
	{
		for (String vncls : keySet())
		{
			if (vncls.startsWith(superVNClass))
				return get(vncls);
		}
		
		return null;
	}
	
	public PVRoles getSuperVNRoles(String subVNClass)
	{
		for (String vncls : keySet())
		{
			if (subVNClass.startsWith(vncls))
				return get(vncls);
		}
		
		return null;
	}
	
	public String toString()
	{
		List<String> vnclses = new ArrayList<String>(keySet());
		Collections.sort(vnclses);
		
		StringBuilder build = new StringBuilder();
		
		for (String vncls : vnclses)
		{
			build.append("\n");
			build.append(get(vncls));				
		}
		
		return UTXml.getTemplate(PVMap.E_ROLESET, build.substring(1), "  ", ATTR_ID, s_rolesetId);
	}
}
