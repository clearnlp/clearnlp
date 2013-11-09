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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.carrotsearch.hppc.IntStack;
import com.clearnlp.coreference.Mention;
import com.clearnlp.dependency.DEPArc;
import com.clearnlp.dependency.DEPFeat;
import com.clearnlp.dependency.DEPLib;
import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dependency.DEPTree;
import com.clearnlp.dependency.srl.SRLArc;
import com.google.common.collect.Lists;


/**
 * @since 1.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class JointReader extends AbstractColumnReader<DEPTree>
{
	protected int i_id;
	protected int i_form;
	protected int i_lemma;
	protected int i_gpos;
	protected int i_pos;
	protected int i_feats;
	protected int i_headId;
	protected int i_deprel;
	protected int i_xheads;
	protected int i_sheads;
	protected int i_nament;
	protected int i_coref;
	
	/** For part-of-speech tagging. */
	public JointReader(int iForm, int iPos)
	{
		init(-1, iForm, -1, iPos, -1, -1, -1, -1, -1, -1, -1);
	}
	
	/** For dependency parsing. */
	public JointReader(int iId, int iForm, int iLemma, int iPos, int iFeats, int iHeadId, int iDeprel)
	{
		init(iId, iForm, iLemma, iPos, iFeats, iHeadId, iDeprel, -1, -1, -1, -1);
	} 
	
	/**
	 * Constructs a dependency reader.
	 * @param iId the column index of the node ID field.
	 * @param iForm the column index of the word-form field.
	 * @param iLemma the column index of the lemma field.
	 * @param iPos the column index of the POS field.
	 * @param iFeats the column index of the feats field.
	 * @param iHeadId the column index of the head ID field.
	 * @param iDeprel the column index of the dependency label field.
	 * @param iXHeads the column index of the secondary dependency field.
	 * @param iSHeads the column index of the semantic head field.
	 * @param iNament the column index of the named entity tag.
	 * @param iCoref the column index of the coreference mentions.
	 */
	public JointReader(int iId, int iForm, int iLemma, int iPos, int iFeats, int iHeadId, int iDeprel, int iXHeads, int iSHeads, int iNament, int iCoref)
	{
		init(iId, iForm, iLemma, iPos, iFeats, iHeadId, iDeprel, iXHeads, iSHeads, iNament, iCoref);
	}
	
	public void init(int iId, int iForm, int iLemma, int iPos, int iFeats, int iHeadId, int iDeprel, int iXHeads, int iSHeads, int iNament, int iCoref)
	{
		i_id     = iId;
		i_form   = iForm;
		i_lemma  = iLemma;
		i_pos    = iPos;
		i_feats  = iFeats;
		i_headId = iHeadId;
		i_deprel = iDeprel;
		i_xheads = iXHeads;
		i_sheads = iSHeads;
		i_nament = iNament;
		i_coref  = iCoref;
		i_gpos   = -1;
	}
	
	public void initGoldPOSTag(int gpos)
	{
		i_gpos = gpos;
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
		}
		catch (Exception e) {e.printStackTrace();}
		
		return tree;
	}

	protected DEPTree getDEPTree(List<String[]> lines)
	{
		String form, lemma, pos, feats, nament;
		int id, i, size = lines.size();
		DEPTree tree = new DEPTree();
		DEPNode node;
		String[] tmp;
		
		// initialize place holders
		for (i=0; i<size; i++)
			tree.add(new DEPNode());
		
		if (i_xheads >= 0)
			tree.get(0).setXHeads(new ArrayList<DEPArc>());
		
		if (i_sheads >= 0)
			tree.get(0).setXHeads(new ArrayList<DEPArc>());
		
		for (i=0; i<size; i++)
		{
			tmp    = lines.get(i);
			form   = tmp[i_form];
			id     = (i_id     < 0) ? i+1  : Integer.parseInt(tmp[i_id]);
			lemma  = (i_lemma  < 0) ? null : tmp[i_lemma];
			pos    = (i_pos    < 0) ? null : tmp[i_pos];
			feats  = (i_feats  < 0) ? AbstractColumnReader.BLANK_COLUMN : tmp[i_feats];
			nament = (i_nament < 0) ? null : tmp[i_nament]; 

			node = tree.get(id);
			node.init(id, form, lemma, pos, new DEPFeat(feats));
			node.nament = nament;
			
			if (i_headId >= 0 && !tmp[i_headId].equals(AbstractColumnReader.BLANK_COLUMN))
				node.setHead(tree.get(Integer.parseInt(tmp[i_headId])), tmp[i_deprel]);
			
			if (i_xheads >= 0)
				node.setXHeads(getXHeads(tree, tmp[i_xheads]));
			
			if (i_sheads >= 0)
				node.setSHeads(getSHeads(tree, tmp[i_sheads]));
			
			if (i_gpos >= 0)
				node.addFeat(DEPLib.FEAT_GPOS, tmp[i_gpos]);
		}
		
		if (i_coref >= 0) tree.setMentions(getMentions(lines));
		return tree;
	}
	
	private List<DEPArc> getXHeads(DEPTree tree, String heads)
	{
		List<DEPArc> sHeads = Lists.newArrayList();
		
		if (heads.equals(AbstractColumnReader.BLANK_COLUMN))
			return sHeads;
		
		int headId, idx;
		String label;
		
		for (String head : heads.split(DEPLib.DELIM_HEADS))
		{
			idx    = head.indexOf(DEPLib.DELIM_HEADS_KEY);
			headId = Integer.parseInt(head.substring(0, idx));
			label  = head.substring(idx+1);
			
			sHeads.add(new DEPArc(tree.get(headId), label));
		}
		
		return sHeads;
	}
	
	private List<SRLArc> getSHeads(DEPTree tree, String heads)
	{
		List<SRLArc> sHeads = Lists.newArrayList();
		
		if (heads.equals(AbstractColumnReader.BLANK_COLUMN))
			return sHeads;
		
		int headId, idx;
		String label;
		
		for (String head : heads.split(DEPLib.DELIM_HEADS))
		{
			idx    = head.indexOf(DEPLib.DELIM_HEADS_KEY);
			headId = Integer.parseInt(head.substring(0, idx));
			label  = head.substring(idx+1);
			
			sHeads.add(new SRLArc(tree.get(headId), label));
		}
		
		return sHeads;
	}
	
	private List<Mention> getMentions(List<String[]> lines)
	{
		Map<String,IntStack> map = new HashMap<String,IntStack>();
		List<Mention> mentions = new ArrayList<Mention>();
		int i, size = lines.size();
		String corefs, key;
		IntStack stack;
		
		for (i=0; i<size; i++)
		{
			corefs = lines.get(i)[i_coref];
			
			if (corefs.equals("-"))
				continue;
			
			for (String coref : DEPFeat.P_FEATS.split(corefs))
			{
				if (coref.startsWith("("))
				{
					if (coref.endsWith(")"))
					{
						key = coref.substring(1, coref.length()-1);
						mentions.add(new Mention(key, i+1, i+1));
					}
					else
					{
						key = coref.substring(1);
						stack = map.get(key);
						
						if (stack == null)
						{
							stack = new IntStack();
							map.put(key, stack);
						}
						
						stack.push(i+1);
					}
				}
				else //if (coref.endsWith(")"))
				{
					key = coref.substring(0, coref.length()-1);
					mentions.add(new Mention(key, map.get(key).pop(), i+1));
				}
			}
		}
		
		return mentions;
	}
	
	public String getType()
	{
		if (i_form < 0)
		{
			throw new IllegalArgumentException("The form column must be specified.");
		}
		else
		{
			if (i_pos >= 0)
			{
				if (i_lemma >= 0)
				{
					if (i_headId >= 0 && i_deprel >= 0)
					{
						if (i_feats >= 0 && i_sheads >= 0)
						{
							return TYPE_SRL;
						}
						
						return TYPE_DEP;
					}
					
					return TYPE_MORPH;
				}
				
				return TYPE_POS;
			}
			
			return TYPE_TOK;
		}
	}
}
