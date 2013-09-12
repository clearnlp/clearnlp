/**
* Copyright (c) 2009-2012, Regents of the University of Colorado
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
*
* Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
* Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
* Neither the name of the University of Colorado at Boulder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
* AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
* IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
* ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
* LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
* CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
* ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
* POSSIBILITY OF SUCH DAMAGE.
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
public class PVVerb extends HashMap<String,PVRoleset>
{
	static public final String ATTR_LEMMA = "lemma";
	String s_lemma;
	
	public PVVerb(Element eVerb, String lemma, boolean fromMap)
	{
		NodeList list = eVerb.getElementsByTagName(PVMap.E_ROLESET);
		int i, size = list.getLength();
		Element   eRoleset;
		String    rolesetId;
		PVRoleset pvRoleset;
		
		s_lemma = lemma;
		
		for (i=0; i<size; i++)
		{
			eRoleset  = (Element)list.item(i);
			rolesetId = UTXml.getTrimmedAttribute(eRoleset, PVRoleset.ATTR_ID);
			pvRoleset = new PVRoleset(eRoleset, rolesetId, fromMap);
			
			if (!pvRoleset.isEmpty())	put(rolesetId, pvRoleset);
		}
	}
	
	public String toString()
	{
		List<String> rolesetIds = new ArrayList<String>(keySet());
		Collections.sort(rolesetIds);
		
		StringBuilder build = new StringBuilder();
		
		for (String rolesetId : rolesetIds)
		{
			build.append("\n");
			build.append(get(rolesetId));
		}
		
		return UTXml.getTemplate(PVMap.E_VERB, build.substring(1), "", ATTR_LEMMA, s_lemma);
	}
}
