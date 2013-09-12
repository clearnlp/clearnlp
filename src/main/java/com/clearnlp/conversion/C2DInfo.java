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
