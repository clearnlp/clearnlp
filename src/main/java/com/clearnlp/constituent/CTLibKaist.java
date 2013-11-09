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
package com.clearnlp.constituent;

import java.util.List;
import java.util.regex.Pattern;


/**
 * Constituent library for English.
 * @since 1.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class CTLibKaist extends CTLib
{
	static final public String PTAG_S    = "S";
	static final public String PTAG_ADJP = "ADJP";
	static final public String PTAG_ADVP = "ADVP";
	static final public String PTAG_AUXP = "AUXP";
	static final public String PTAG_IP   = "IP";
	static final public String PTAG_MODP = "MODP";
	static final public String PTAG_NP   = "NP";
	static final public String PTAG_VP   = "VP";
	
	// endings
	static final public String POS_ECC  = "ecc";
	static final public String POS_ECS  = "ecs";
	static final public String POS_ECX  = "ecx";
	static final public String POS_EF   = "ef";
	static final public String POS_EP   = "ep";
	static final public String POS_ETM  = "etm";
	static final public String POS_ETN  = "etn";
	
	static final public String POS_F    = "f";
	static final public String POS_II   = "ii";
	
	// josas
	static final public String POS_JCA  = "jca";
	static final public String POS_JCC  = "jcc";
	static final public String POS_JCJ  = "jcj";
	static final public String POS_JCM  = "jcm";
	static final public String POS_JCO  = "jco";
	static final public String POS_JCR  = "jcr";
	static final public String POS_JCS  = "jcs";
	static final public String POS_JCT  = "jct";
	static final public String POS_JCV  = "jcv";
	static final public String POS_JP   = "jp";
	static final public String POS_JXC  = "jxc";
	static final public String POS_JXF  = "jxf";
	static final public String POS_JXT  = "jxt";
	
	// modifiers
	static final public String POS_MAD  = "mad";
	static final public String POS_MAG  = "mag";
	static final public String POS_MAJ  = "maj";
	static final public String POS_MMA  = "mma";
	static final public String POS_MMD  = "mmd";
	
	// nominals
	static final public String POS_NBN  = "nbn";
	static final public String POS_NBS  = "nbs";
	static final public String POS_NBU  = "nbu";
	static final public String POS_NCN  = "ncn";
	static final public String POS_NCPA = "ncpa";
	static final public String POS_NCPS = "ncps";
	static final public String POS_NCR  = "ncr";
	static final public String POS_NNC  = "nnc";
	static final public String POS_NNO  = "nno";
	static final public String POS_NPD  = "npd";
	static final public String POS_NPP  = "npp";
	static final public String POS_NQ   = "nq";

	// predicates
	static final public String POS_PAA  = "paa";
	static final public String POS_PAD  = "pad";
	static final public String POS_PVD  = "pvd";
	static final public String POS_PVG  = "pvg";
	static final public String POS_PX   = "px";
	
	// symbols
	static final public String POS_SD   = "sd";
	static final public String POS_SF   = "sf";
	static final public String POS_SL   = "sl";
	static final public String POS_SP   = "sp";
	static final public String POS_SR   = "sr";
	static final public String POS_SU   = "su";
	static final public String POS_SY   = "sy";
	
	// affixes
	static final public String POS_XP   = "xp";
	static final public String POS_XSA  = "xsa";
	static final public String POS_XSM  = "xsm";
	static final public String POS_XSN  = "xsn";
	static final public String POS_XSV  = "xsv";

	static final public String FTAG_PRN = "PRN";
	
	/**
	 * Returns {@code true} if the specific list of siblings contains coordination.
	 * @param parent the parent of all siblings.
	 * @param siblings the list of siblings.
	 * @return {@code true} if the specific list of siblings contains coordination.
	 */
	static public boolean containsCoordination(List<CTNode> siblings, Pattern delim)
	{
		for (CTNode node : siblings)
		{
			if (isConjunction(node, delim))//|| isConjunct(node, delim))
				return true;
		}

		return false;
	}

	/**
	 * Returns {@code true} if this node is a conjunction.
	 * @param node the node to be compared.
	 * @return {@code true} if this node is a conjunction.
	 */
	static public boolean isConjunction(CTNode node, Pattern delim)
	{
		if (node.isPTag(POS_MAJ))
			return true;
		
		String pos = getLastPOSTag(node, delim);
		
		if (node.isPTag(PTAG_IP) && (pos.equals(POS_MAJ) || pos.equals(POS_ECS)))
			return true;
		
		return false;
	}
	
	static public boolean isConjunct(CTNode node, Pattern delim)
	{
		String pos = getLastPOSTag(node, delim);
		return pos.equals(POS_ECC) || pos.equals(POS_JCJ);
	}
	
	/**
	 * Returns {@code true} if the specific node is an adnoun.
	 * @param node the node to be compared.
	 * @return {@code true} if the specific node is an adnoun.
	 */
	static public boolean isAdnoun(CTNode node, Pattern delim)
	{
		if (node.pTag.startsWith("mm"))
			return true;
		
		String pos = getLastPOSTag(node, delim);
		return pos.equals(POS_ETM) || pos.equals(POS_JCM);
	}
	
	/**
	 * Returns {@code true} if the specific node is an adverb.
	 * @param node the node to be compared.
	 * @return {@code true} if the specific node is an adverb.
	 */
	static public boolean isAdverb(CTNode node, Pattern delim)
	{
		if (!node.isPhrase())
		{
			if (node.pTag.startsWith("ma"))
				return true;
			
			for (String pos : delim.split(node.pTag))
			{
				if (pos.equals(POS_XSA) || pos.equals(POS_JCA))
					return true;
			}			
		}
		
		return false;
	}
	
	static public boolean isInterjection(CTNode node, Pattern delim)
	{
		if (node.pTag.equals(POS_II))
			return true;
		
		String pos = getLastPOSTag(node, delim);
		return pos.equals(POS_JCV);
	}
	
	/**
	 * Returns {@code true} if the specific node is punctuation.
	 * @param node the node to be compared.
	 * @return {@code true} if the specific node is punctuation.
	 */
	static public boolean isPunctuation(CTNode node)
	{
		return node.pTag.startsWith("s");
	}
	
	static public boolean isOnlyEJX(CTNode node, Pattern delim)
	{
		for (String pos : delim.split(node.pTag))
		{
			if (!pos.startsWith("e") && !pos.startsWith("j") && !pos.startsWith("x"))
				return false;
		}
			
		return true;
	}
	
	static public String[] getLastPOSTags(CTNode node, Pattern delim)
	{
		return delim.split(getLastTerminal(node).pTag);
	}
	
	static public String getLastPOSTag(CTNode node, Pattern delim)
	{
		String[] tmp = getLastPOSTags(node, delim);
		return tmp[tmp.length-1];
	}
	
	static public CTNode getLastTerminal(CTNode node)
	{
		List<CTNode> list = node.getSubTerminals();
		int i, last = list.size() - 1;
 		CTNode t;
		
		for (i=last; i>=0; i--)
		{
			t = list.get(i);
			if (!isPunctuation(t))	return t;
		}
		
		return list.get(last);
	}
}
