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
