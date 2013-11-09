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
package com.clearnlp.conversion;

import com.clearnlp.constituent.CTNode;
import com.clearnlp.dependency.DEPFeat;

/**
 * Constituent to dependency information.
 * @since 1.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class C2DInfo
{
	CTNode  d_head;
	CTNode  p_head;
	String  s_label;
	DEPFeat d_feats;
	
	private boolean b_head;
	
	/** Initializes the dependency head of a constituent. */
	public C2DInfo(CTNode head)
	{
		s_label = null;
		b_head  = false;
		
		if (head.c2d == null)	// for terminals: head = itself
		{
			d_head  = head;
			p_head  = null;
			d_feats = new DEPFeat();
		}
		else					// for phrases: head = child
		{
			d_head = head.c2d.getDependencyHead();
			p_head = head;
		}
	}
	
	/** Sets heads for siblings */
	public void setHead(CTNode head, String label)
	{
		d_head.c2d.d_head = head.c2d.getDependencyHead();
		setLabel(label);
		b_head = true;
	}
	
	public void setHeadTerminal(CTNode head, String label)
	{
		d_head.c2d.d_head = head;
		setLabel(label);
		b_head = true;
	}
	
	public boolean hasHead()
	{
		return b_head;
	}
	
	public void setLabel(String label)
	{
		if (p_head == null)
			s_label = label;
		else
			d_head.c2d.s_label = label;
	}
	
	public String getLabel()
	{
		return s_label;
	}
	
	public String putFeat(String key, String value)
	{
		return d_head.c2d.d_feats.put(key, value);
	}
	
	public String getFeat(String key)
	{
		return d_feats.get(key);
	}
	
	public CTNode getDependencyHead()
	{
		return d_head;
	}
	
	public CTNode getPhraseHead()
	{
		return p_head;
	}
}
