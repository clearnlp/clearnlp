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
package com.clearnlp.dependency;

import com.clearnlp.util.pair.StringIntPair;

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
}
