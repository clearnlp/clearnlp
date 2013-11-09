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
package com.clearnlp.component.state;

import java.util.List;
import java.util.Map;

import com.carrotsearch.hppc.IntOpenHashSet;
import com.clearnlp.classification.feature.FtrToken;
import com.clearnlp.classification.feature.JointFtrXml;
import com.clearnlp.dependency.DEPArc;
import com.clearnlp.dependency.DEPLib;
import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dependency.DEPTree;
import com.clearnlp.propbank.frameset.PBRoleset;
import com.clearnlp.util.pair.ObjectDoublePair;
import com.clearnlp.util.pair.StringIntPair;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @since 2.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class SRLState extends DefaultState
{
	int            i_pred;
	int            i_arg;
	PBRoleset      p_roleset;
	DEPNode        d_lca;
	List<String>   l_argns;
	IntOpenHashSet s_skip;
	Map<String,ObjectDoublePair<DEPNode>> m_argns;
	Map<String,ObjectDoublePair<DEPNode>> m_refs;

	DEPNode[] lm_deps, rm_deps;
	DEPNode[] ln_sibs, rn_sibs;
	StringIntPair[][] g_labels;
	
	public SRLState(DEPTree tree)
	{
		super(tree);
		
		d_tree  = tree;
		i_pred  = 0;
		s_skip  = new IntOpenHashSet();
		l_argns = Lists.newArrayList();
		m_argns = Maps.newHashMap();
		m_refs  = Maps.newHashMap();
		
		initArcs(tree);
	}
	
//	====================================== INITIALIZATION ======================================
	
	/** Initializes dependency arcs of all nodes. */
	protected void initArcs(DEPTree tree)
	{
		int i, j, len, size = tree.size();
		DEPNode curr, prev, next;
		List<DEPArc> deps;
		DEPArc lmd, rmd;
		
		lm_deps = new DEPNode[size];
		rm_deps = new DEPNode[size];
		ln_sibs = new DEPNode[size];
		rn_sibs = new DEPNode[size];
		
		d_tree.setDependents();
		
		for (i=1; i<size; i++)
		{
			deps = d_tree.get(i).getDependents();
			if (deps.isEmpty())	continue;
			
			len = deps.size(); 
			lmd = deps.get(0);
			rmd = deps.get(len-1);
			
			if (lmd.getNode().id < i)	lm_deps[i] = lmd.getNode();
			if (rmd.getNode().id > i)	rm_deps[i] = rmd.getNode();
			
			for (j=1; j<len; j++)
			{
				curr = deps.get(j  ).getNode();
				prev = deps.get(j-1).getNode();

				if (ln_sibs[curr.id] == null || ln_sibs[curr.id].id < prev.id)
					ln_sibs[curr.id] = prev;
			}
			
			for (j=0; j<len-1; j++)
			{
				curr = deps.get(j  ).getNode();
				next = deps.get(j+1).getNode();

				if (rn_sibs[curr.id] == null || rn_sibs[curr.id].id > next.id)
					rn_sibs[curr.id] = next;
			}
		}
	}
	
//	====================================== GETTERS ======================================
	
	public StringIntPair[][] getGoldLabels()
	{
		return g_labels;
	}
	
	public StringIntPair[] getGoldLabel()
	{
		return g_labels[i_arg];
	}
	
	public DEPNode getCurrentPredicate()
	{
		return getNode(i_pred);
	}
	
	public DEPNode getCurrentArgument()
	{
		return getNode(i_arg);
	}
	
	public DEPNode getLowestCommonAncestor()
	{
		return d_lca;
	}
	
	public PBRoleset getRoleset()
	{
		return p_roleset;
	}
	
	public String getNumberedArgument(int backIndex)
	{
		int idx = l_argns.size() - backIndex - 1;
		return (idx >= 0) ? l_argns.get(idx) : null;
	}
	
	public int getDirection()
	{
		return (i_arg < i_pred) ? 0 : 1;
	}
	
	public int getCurrPredicateID()
	{
		return i_pred;
	}
	
	public int getCurrArgumentID()
	{
		return i_arg;
	}
	
	public ObjectDoublePair<DEPNode> getCoreNumberedArgument(String label)
	{
		return m_argns.get(label);
	}
	
	public ObjectDoublePair<DEPNode> getReferentArgument(String label)
	{
		return m_refs.get(label);
	}
	
	public DEPNode getLeftmostDependent(int id)
	{
		return lm_deps[id];
	}
	
	public DEPNode getRightmostDependent(int id)
	{
		return rm_deps[id];
	}
	
	public DEPNode getLeftnearestSibling(int id)
	{
		return ln_sibs[id];
	}
	
	public DEPNode getRightnearestSibling(int id)
	{
		return rn_sibs[id];
	}
	
//	====================================== SETTERS ======================================
	
	public void setGoldLabels(StringIntPair[][] labels)
	{
		g_labels = labels;
	}
	
	public void setRoleset(PBRoleset roleset)
	{
		p_roleset = roleset;
	}
	
	public void setArgument(DEPNode node)
	{
		i_arg = node.id;
	}
	
	public void addArgumentToSkipList()
	{
		s_skip.add(i_arg);
	}
	
	public void addNumberedArgument(String label)
	{
		l_argns.add(label);
	}
	
	public void putCoreNumberedArgument(String label, ObjectDoublePair<DEPNode> p)
	{
		m_argns.put(label, p);
	}
	
	public void putReferentArgument(String label, ObjectDoublePair<DEPNode> p)
	{
		m_argns.put(label, p);
	}
	
//	====================================== BOOLEANS ======================================

	public boolean isSkip(DEPNode node)
	{
		return s_skip.contains(node.id);
	}
	
	public boolean isLowestCommonAncestor(DEPNode node)
	{
		return d_lca == node;
	}
	
//	====================================== MOVES ======================================

	public DEPNode moveToNextPredicate()
	{
		DEPNode pred = d_tree.getNextPredicate(i_pred);
		
		if (pred != null)
		{
			i_pred = pred.id;
			d_lca  = pred;
			l_argns.clear();
			m_argns.clear();
			s_skip.clear();
			s_skip.add(i_pred);
			s_skip.add(DEPLib.ROOT_ID);
		}
			
		return pred;
	}
	
	public boolean moveToNextLowestCommonAncestor()
	{
		d_lca = d_lca.getHead();
		return d_lca != null;
	}
	
//	====================================== NODES ======================================
	
	public DEPNode getNode(FtrToken token)
	{
		DEPNode node = (token.source == JointFtrXml.S_PRED) ? getNode(token, i_pred, 0, t_size) : getNode(token, i_arg, 0, t_size);
		if (node == null)	return null;
		
		if (token.relation != null)
		{
			     if (token.isRelation(JointFtrXml.R_H))		node = node.getHead();
			else if (token.isRelation(JointFtrXml.R_LMD))	node = getLeftmostDependent  (node.id);
			else if (token.isRelation(JointFtrXml.R_RMD))	node = getRightmostDependent (node.id);			
			else if (token.isRelation(JointFtrXml.R_LNS))	node = getLeftnearestSibling (node.id);
			else if (token.isRelation(JointFtrXml.R_RNS))	node = getRightnearestSibling(node.id);
		}
		
		return node;
	}
}
