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
package com.clearnlp.dependency;

import java.util.Collections;
import java.util.List;

import com.clearnlp.dependency.srl.SRLArc;
import com.clearnlp.reader.AbstractColumnReader;
import com.clearnlp.util.pair.StringIntPair;
import com.google.common.collect.Lists;

/**
 * Dependency library.
 * @since 1.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class DEPLib
{
	/** The node ID of an artificial root. */
	static public final int ROOT_ID =  0;
	/** The node ID of a null node. */
	static public final int NULL_ID = -1;

	/** A dummy tag for the root node. */
	static public final String ROOT_TAG = "_R_";
	/** The feat-key of semantic function tags. */
	static public final String FEAT_SEM	= "sem";
	/** The feat-key of syntactic function tags. */
	static public final String FEAT_SYN	= "syn";
	/** The feat-key of sentence types. */
	static public final String FEAT_SNT	= "snt";
	/** The feat-key of propbank rolesets. */
	static public final String FEAT_PB	= "pb";
	/** The feat-key of verbnet classes. */
	static public final String FEAT_VN	= "vn";
	/** The feat-key of word senses. */
	static public final String FEAT_WS	= "ws";
	/** The feat-key of 2nd POS prediction. */
	static public final String FEAT_POS2 = "p2";
	/** The feat-key of gold POS tag. */
	static public final String FEAT_GPOS = "gpos";
	/** The feat-key of coreference value. */
	static public final String FEAT_COREF = "coref";
	/** The feat-key of verb type. */
	static public final String FEAT_VERB_TYPE = "vtype";
	
	/** The delimiter between secondary/semantic heads. */
	static public final String DELIM_HEADS     = ";";
	/** The delimiter between head ID and label pairs. */
	static public final String DELIM_HEADS_KEY = ":";
	
	static public final String DEP_NON_PROJ = "#NPRJ!";
	
	/** @return [Total, LAS, UAS, LS]. */
	static public int[] getScores(DEPTree tree, StringIntPair[] gHeads)
	{
		int[] counts = new int[4];
		int i, size = tree.size();
		StringIntPair head;
		DEPNode node;
		
		counts[0] += size - 1;
		
		for (i=1; i<size; i++)
		{
			if (!(node = tree.get(i)).hasHead())
				continue;
			
			head = gHeads[i];
			
			if (head.i == node.getHead().id)
				counts[2]++;
			
			if (gHeads[i].s.equals(node.getLabel()))
			{
				if (head.i == node.getHead().id)
					counts[1]++;
				
				counts[3]++;
			}
		}
		
		return counts;
	}

	static public <T extends DEPArc>String toString(List<T> arcs)
	{
		StringBuilder build = new StringBuilder();
		Collections.sort(arcs);
		
		for (DEPArc arc : arcs)
		{
			build.append(DELIM_HEADS);
			build.append(arc.toString());
		}
		
		if (build.length() > 0)
			return build.substring(DELIM_HEADS.length());
		else
			return AbstractColumnReader.BLANK_COLUMN;
	}
	
	static public List<DEPArc> getDEPArcs(DEPTree tree, String arcsStr)
	{
		List<DEPArc> arcs = Lists.newArrayList();
		
		if (arcsStr.equals(AbstractColumnReader.BLANK_COLUMN))
			return arcs;
		
		for (String arc : arcsStr.split(DELIM_HEADS))
			arcs.add(new DEPArc(tree, arc));
		
		return arcs;
	}
	
	static public List<SRLArc> getSRLArcs(DEPTree tree, String arcsStr)
	{
		List<SRLArc> arcs = Lists.newArrayList();
		
		if (arcsStr.equals(AbstractColumnReader.BLANK_COLUMN))
			return arcs;
		
		for (String arc : arcsStr.split(DELIM_HEADS))
			arcs.add(new SRLArc(tree, arc));
		
		return arcs;
	}
}
