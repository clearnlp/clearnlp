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
package com.clearnlp.reader;

import java.util.List;

import com.clearnlp.dependency.DEPLib;
import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dependency.DEPTree;


/**
 * @since 1.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class SRLReader extends DEPReader
{
	private int i_sheads;

	/**
	 * Constructs a semantic role reader.
	 * @param iId the column index of the node ID field.
	 * @param iForm the column index of the word-form field.
	 * @param iLemma the column index of the lemma field.
	 * @param iPos the column index of the POS field.
	 * @param iFeats the column index of the feats field.
	 * @param iHeadId the column index of the head ID field.
	 * @param iDeprel the column index of the dependency label field.
	 * @param iSheads the column index of the semantic head field.
	 */
	public SRLReader(int iId, int iForm, int iLemma, int iPos, int iFeats, int iHeadId, int iDeprel, int iSheads)
	{
		super(iId, iForm, iLemma, iPos, iFeats, iHeadId, iDeprel);
		i_sheads = iSheads;
	}
	
	/**
	 * Initializes column indexes of fields.
	 * @param iId the column index of the node ID field.
	 * @param iForm the column index of the word-form field.
	 * @param iLemma the column index of the lemma field.
	 * @param iPos the column index of the POS field.
	 * @param iFeats the column index of the feats field.
	 * @param iHeadId the column index of the head ID field.
	 * @param iDeprel the column index of the dependency label field.
	 * @param iSheads the column index of the semantic head field.
	 */
	public void init(int iId, int iForm, int iLemma, int iPos, int iFeats, int iHeadId, int iDeprel, int iSheads)
	{
		super.init(iId, iForm, iLemma, iPos, iFeats, iHeadId, iDeprel);
		i_sheads = iSheads;
	}
	
	@Override
	public DEPTree next()
	{
		DEPTree tree = null;
		
		try
		{
			List<String[]> lines = readLines();
			if (lines == null)	return null;
			
			tree = getDEPTree(lines);
			tree.initSHeads();
			if (i_sheads >= 0)	setSHeads(lines, tree);
		}
		catch (Exception e) {e.printStackTrace();}
		
		return tree;
	}
	
	/** Sets semantic heads of the specific dependency tree given the input lines. */
	private void setSHeads(List<String[]> lines, DEPTree tree)
	{
		int i, headId, size = tree.size();
		String heads, label;
		String[] tmp;
		DEPNode node;
		
		tree.initSHeads();

		for (i=1; i<size; i++)
		{
			node  = tree.get(i);
			heads = lines.get(i-1)[i_sheads];
			if (heads.equals(AbstractColumnReader.BLANK_COLUMN))	continue;

			for (String head : heads.split(DEPLib.DELIM_HEADS))
			{
				tmp    = head.split(DEPLib.DELIM_HEADS_KEY);
				headId = Integer.parseInt(tmp[0]);
				label  = tmp[1];
				
				node.addSHead(tree.get(headId), label);				
			}
		}
	}
	
	public String getType()
	{
		return TYPE_SRL;
	}
}
