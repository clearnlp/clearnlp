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

import org.w3c.dom.Element;

import com.clearnlp.util.UTXml;


public class PVRole implements Comparable<PVRole>
{
	static public final String ATTR_N		= "n";
	static public final String ATTR_F		= "f";
	static public final String ATTR_VNTHETA	= "vntheta";

	public String n;
	public String f;
	public String vntheta;
	
	public PVRole(Element eRole)
	{
		n       = UTXml.getTrimmedAttribute(eRole, ATTR_N);
		f       = UTXml.getTrimmedAttribute(eRole, ATTR_F);
		vntheta = UTXml.getTrimmedAttribute(eRole, ATTR_VNTHETA);
	}
	
	public PVRole(String n, String f, String vntheta)
	{
		this.n       = n;
		this.f       = f;
		this.vntheta = vntheta;
	}
	
	/** @param argn the numbered argument (e.g., ARG0). */
	public boolean isArgN(String argn)
	{
		return argn.length() > 3 && n.equals(argn.substring(3,4));
	}
	
	@Override
	public int compareTo(PVRole role)
	{
		int diff = n.compareTo(role.n);
		
		if      (diff > 0)	return  1;
		else if (diff < 0)	return -1;
		else				return f.compareTo(role.f);
	}
	
	public String toString()
	{
		return UTXml.startsElement(true, PVMap.E_ROLE, ATTR_N, n, ATTR_F, f, ATTR_VNTHETA, vntheta);		
	}
}
