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

import com.clearnlp.dependency.DEPFeat;
import com.clearnlp.dependency.DEPLib;
import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dependency.DEPTree;


/**
 * @since 1.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class DAGReader extends AbstractColumnReader<DEPTree>
{
	private int i_id;
	private int i_form;
	private int i_lemma;
	private int i_pos;
	private int i_feats;
	private int i_xheads;
	
	/**
	 * Constructs an acyclic directed graph reader.
	 * @param iId the column index of the node ID field.
	 * @param iForm the column index of the word-form field.
	 * @param iLemma the column index of the lemma field.
	 * @param iPos the column index of the POS field.
	 * @param iFeats the column index of the feats field.
	 * @param iXheads the column index of the dependency heads field.
	 */
	public DAGReader(int iId, int iForm, int iLemma, int iPos, int iFeats, int iXheads)
	{
		init(iId, iForm, iLemma, iPos, iFeats, iXheads);
	}
	
	/**
	 * Initializes column indexes of fields.
	 * @param iId the column index of the node ID field.
	 * @param iForm the column index of the word-form field.
	 * @param iLemma the column index of the lemma field.
	 * @param iPos the column index of the POS field.
	 * @param iFeats the column index of the feats field.
	 * @param iXheads the column index of the dependency heads field.
	 */
	public void init(int iId, int iForm, int iLemma, int iPos, int iFeats, int iXheads)
	{
		i_id     = iId;
		i_form   = iForm;
		i_lemma  = iLemma;
		i_pos    = iPos;
		i_feats  = iFeats;
		i_xheads = iXheads;
	}
	
	@Override
	public DEPTree next()
	{
		DEPTree tree = null;
		
		try
		{
			List<String[]> lines = readLines();
			if (lines == null)	return null;
			
			tree = getDAG(lines);
		}
		catch (Exception e) {e.printStackTrace();}
		
		return tree;
	}
	
	protected DEPTree getDAG(List<String[]> lines)
	{
		int id, headId, i, size = lines.size();
		String form, lemma, pos, label;
		DEPFeat feats;
		String[] tmp;
		DEPNode node;
		
		DEPTree tree = new DEPTree();
		for (i=0; i<size; i++)	tree.add(new DEPNode());
		
		for (i=0; i<size; i++)
		{
			tmp   = lines.get(i);
			id    = Integer.parseInt(tmp[i_id]);
			form  = tmp[i_form];
			lemma = tmp[i_lemma];
			pos   = tmp[i_pos];
			feats = new DEPFeat(tmp[i_feats]);
			
			node = tree.get(id);
			node.init(id, form, lemma, pos, feats);
					
			if (i_xheads >= 0 && !tmp[i_xheads].equals(AbstractColumnReader.BLANK_COLUMN))
			{
				for (String head : tmp[i_xheads].split(DEPLib.DELIM_HEADS))
				{
					tmp    = head.split(DEPLib.DELIM_HEADS_KEY);
					headId = Integer.parseInt(tmp[0]);
					label  = tmp[1];
					
					node.addXHead(tree.get(headId), label);
				}
			}
		}
		
		return tree;
	}
	
	public String getType()
	{
		return TYPE_DAG;
	}
}
