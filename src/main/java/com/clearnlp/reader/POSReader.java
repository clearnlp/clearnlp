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
package com.clearnlp.reader;

import java.util.List;

import com.clearnlp.pos.POSNode;

/**
 * @since 1.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class POSReader extends AbstractColumnReader<POSNode[]>
{
	private int i_form;
	private int i_pos;
	
	/**
	 * Constructs a part-of-speech reader.
	 * @param iForm the column index of the word-form field.
	 * @param iPos the column index of the POS field.
	 */
	public POSReader(int iForm, int iPos)
	{
		init(iForm, iPos);
	}
	
	/**
	 * Initializes column indexes of fields.
	 * @param iForm the column index of the form field.
	 * @param iPos the column index of the POS field.
	 */
	public void init(int iForm, int iPos)
	{
		i_form = iForm;
		i_pos  = iPos;
	}
	
	@Override
	public POSNode[] next()
	{
		POSNode[] nodes = null;
		
		try
		{
			List<String[]> lines = readLines();
			if (lines == null)	return null;
			
			int i, size = lines.size();
			String  form;
			String[] tmp;
			
			nodes = new POSNode[size];
			
			for (i=0; i<size; i++)
			{
				tmp  = lines.get(i);
				form = tmp[i_form];
				
				if (i_pos < 0)	nodes[i] = new POSNode(form);
				else			nodes[i] = new POSNode(form, tmp[i_pos]);
			}
		}
		catch (Exception e) {e.printStackTrace();}
		
		return nodes;
	}
	
	public String getType()
	{
		return TYPE_POS;
	}
}
