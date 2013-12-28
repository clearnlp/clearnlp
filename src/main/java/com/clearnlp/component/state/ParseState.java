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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.carrotsearch.hppc.IntOpenHashSet;
import com.clearnlp.classification.feature.FtrToken;
import com.clearnlp.classification.feature.JointFtrXml;
import com.clearnlp.classification.instance.StringInstance;
import com.clearnlp.component.label.IDEPLabel;
import com.clearnlp.dependency.DEPHead;
import com.clearnlp.dependency.DEPLabel;
import com.clearnlp.dependency.DEPLib;
import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dependency.DEPTree;
import com.clearnlp.util.UTCollection;
import com.clearnlp.util.pair.StringIntPair;
import com.clearnlp.util.triple.ObjectsDoubleTriple;
import com.google.common.collect.Lists;

/**
 * @since 2.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class ParseState extends AbstractState implements IDEPLabel
{
	List<ObjectsDoubleTriple<List<StringInstance>,StringIntPair[]>> l_branches;
	List<DEPStateBranch> l_states;
	List<List<DEPHead>>  l_2ndHeads;
	double[]             n_2ndPos;
	int                  i_state;
	boolean              b_branch;

	StringIntPair[]      g_labels;
	int			      	 i_lambda;
	int			     	 i_beta;
	int           n_trans;
	double        d_score;
	IntOpenHashSet       s_reduce;
	
	public ParseState(DEPTree tree)
	{
		super(tree);
		init (tree);
	}
	
//	====================================== INITIALIZATION ======================================
	
	private void init(DEPTree tree)
	{
		initPrimitives();
		
		l_branches = Lists.newArrayList();
	 	l_states   = Lists.newArrayList();
		l_2ndHeads = Lists.newArrayList();
	 	n_2ndPos   = new double[t_size];
	 	s_reduce   = new IntOpenHashSet();
	 	
	 	int i; for (i=0; i<t_size; i++)
	 		l_2ndHeads.add(new ArrayList<DEPHead>());
	}
	
	private void initPrimitives()
	{
		i_lambda = 0;
	 	i_beta   = 1;
	 	n_trans  = 0;
	 	d_score  = 0d;
	 	i_state  = -1;
	 	b_branch = true;
	}
	
	public void reInit()
	{
		initPrimitives();
	 	
		l_branches.clear();
 		l_states.clear();
 		
	 	for (List<DEPHead> list : l_2ndHeads)
	 		list.clear();
	 	
	 	Arrays.fill(n_2ndPos, 0);
	 	s_reduce.clear();
	 	d_tree.clearHeads();	 		
	}
	
//	====================================== GETTERS ======================================
	
	public StringIntPair[] getGoldLabels()
	{
		return g_labels;
	}
	
	public DEPLabel getGoldLabel()
	{
		DEPLabel label = getGoldLabelArc();
		
		if (label.isArc(LB_LEFT))
			label.list = isGoldReduce(true) ? LB_REDUCE : LB_PASS;
		else if (label.isArc(LB_RIGHT))
			label.list = isGoldShift() ? LB_SHIFT : LB_PASS;
		else
		{
			if      (isGoldShift())			label.list = LB_SHIFT;
			else if (isGoldReduce(false))	label.list = LB_REDUCE;
			else							label.list = LB_PASS;
		}
		
		return label;
	}
	
	/** Called by {@link #getGoldLabel()}. */
	private DEPLabel getGoldLabelArc()
	{
		StringIntPair head = g_labels[i_lambda];
		
		if (head.i == i_beta)
			return new DEPLabel(LB_LEFT, head.s);
		
		head = g_labels[i_beta];
		
		if (head.i == i_lambda)
			return new DEPLabel(LB_RIGHT, head.s);
		
		return new DEPLabel(LB_NO, "");
	}
	
	/** Called by {@link #getGoldLabel()}. */
	private boolean isGoldShift()
	{
		if (g_labels[i_beta].i < i_lambda)
			return false;
		
		int i;
		
		for (i=i_lambda-1; i>0; i--)
		{
			if (s_reduce.contains(i))
				continue;
			
			if (g_labels[i].i == i_beta)
				return false;
		}
		
		return true;
	}
	
	/** Called by {@link #getGoldLabel()}. */
	private boolean isGoldReduce(boolean hasHead)
	{
		if (!hasHead && !d_tree.get(i_lambda).hasHead())
			return false;
		
		int i; for (i=i_beta+1; i<t_size; i++)
		{
			if (g_labels[i].i == i_lambda)
				return false;
		}
		
		return true;
	}
	
	public int getLambdaID()
	{
		return i_lambda;
	}
	
	public int getBetaID()
	{
		return i_beta;
	}
	
	public DEPNode getLambda()
	{
		return d_tree.get(i_lambda);
	}
	
	public DEPNode getBeta()
	{
		return d_tree.get(i_beta);
	}
	
	public List<DEPHead> get2ndHeads(int id)
	{
		return l_2ndHeads.get(id);
	}
	
	public int getDistance()
	{
		return i_beta - i_lambda;
	}
	
	public String getLeftValency(int id)
	{
		return Integer.toString(d_tree.getLeftValency(id));
	}
	
	public String getRightValency(int id)
	{
		return Integer.toString(d_tree.getRightValency(id));
	}
	
//	====================================== SETTERS ======================================
	
	public void setGoldLabels(StringIntPair[] labels)
	{
		g_labels = labels;
	}
	
	public void setLambda(int id)
	{
		i_lambda = id;
	}
	
	public void setBeta(int id)
	{
		i_beta = id;
	}
	
	public void add2ndHead(DEPLabel label)
	{
		List<DEPHead> p;
		
		if (label.isArc(LB_LEFT))
		{
			p = l_2ndHeads.get(i_lambda);
			p.add(new DEPHead(i_beta, label.deprel, label.score));
		}
		else if (label.isArc(LB_RIGHT))
		{
			p = l_2ndHeads.get(i_beta);
			p.add(new DEPHead(i_lambda, label.deprel, label.score));
		}
	}
	
	public void add2ndPOSScore(int id, double score)
	{
		n_2ndPos[id] += score;
	}
	
	public void addScore(double score)
	{
		d_score += score;
	}
	
	public void increaseTransitionCount()
	{
		n_trans++;
	}
	
	public void pushBack(int id)
	{
		s_reduce.remove(id);
	}
	
	public void resetHeads(StringIntPair[] heads)
	{
		d_tree.resetHeads(heads);
	}
	
	public double getScore()
	{
		return d_score / n_trans;
	}
	
//	====================================== BOOLEANS ======================================

	public boolean isLambdaValid()
	{
		return i_lambda >= 0;
	}
	
	public boolean isBetaValid()
	{
		return i_beta < t_size;
	}
	
	public boolean isLambdaFirst()
	{
		return i_lambda == 1;
	}
	
	public boolean isBetaLast()
	{
		return i_beta + 1 == t_size;
	}
	
	public boolean isLambdaBetaAdjacent()
	{
		return i_lambda + 1 == i_beta;
	}
	
//	====================================== MOVES ======================================
	
	public void shift()
	{
		i_lambda = i_beta++;
	}
	
	public void reduce()
	{
		s_reduce.add(i_lambda);
		passAux();
	}
	
	public void pass()
	{
		passAux();
	}
	
	public void passAux()
	{
		int i;
		
		for (i=i_lambda-1; i>=0; i--)
		{
			if (!s_reduce.contains(i))
			{
				i_lambda = i;
				return;
			}
		}
		
		i_lambda = i;
	}
	
//	====================================== NODES ======================================
	
	public DEPNode getNode(FtrToken token)
	{
		DEPNode node = null;
		
		switch (token.source)
		{
		case JointFtrXml.S_STACK : node = getNodeStack(token);	break;
		case JointFtrXml.S_LAMBDA: node = getNode(token, i_lambda, 0, i_beta);	break;
		case JointFtrXml.S_BETA  : node = getNode(token, i_beta, i_lambda, t_size);	break;
		}
		
		if (node == null)	return null;
		
		if (token.relation != null)
		{
			     if (token.isRelation(JointFtrXml.R_H))		node = node.getHead();
			else if (token.isRelation(JointFtrXml.R_H2))	node = node.getGrandHead();
			else if (token.isRelation(JointFtrXml.R_LMD))	node = d_tree.getLeftMostDependent  (node.id);
			else if (token.isRelation(JointFtrXml.R_RMD))	node = d_tree.getRightMostDependent (node.id);
			else if (token.isRelation(JointFtrXml.R_LMD2))	node = d_tree.getLeftMostDependent  (node.id, 1);
			else if (token.isRelation(JointFtrXml.R_RMD2))	node = d_tree.getRightMostDependent (node.id, 1);
			else if (token.isRelation(JointFtrXml.R_LNS))	node = d_tree.getLeftNearestSibling (node.id);
			else if (token.isRelation(JointFtrXml.R_RNS))	node = d_tree.getRightNearestSibling(node.id);
		}
		
		return node;
	}
	
	/** Called by {@link #getNode(FtrToken)}. */
	private DEPNode getNodeStack(FtrToken token)
	{
		if (token.offset == 0)
			return d_tree.get(i_lambda);
		
		int offset = Math.abs(token.offset), i;
		int dir = (token.offset < 0) ? -1 : 1;
					
		for (i=i_lambda+dir; 0<i && i<i_beta; i+=dir)
		{
			if (!s_reduce.contains(i) && --offset == 0)
				return d_tree.get(i);
		}
		
		return null;
	}
	
//	====================================== POS TAGS ======================================
	
	public boolean resetPOSTags()
	{
		boolean reset = false;
		DEPNode node;
		int i;
		
		for (i=1; i<t_size; i++)
		{
			if (n_2ndPos[i] > 0)
			{
				reset = true;
				node = d_tree.get(i);
				node.pos = node.removeFeat(DEPLib.FEAT_POS2);
			}
		}
		
		return reset;
	}
	
//	====================================== STATES ======================================
	
	public void addState(DEPLabel label)
	{
		if (b_branch)
			l_states.add(new DEPStateBranch(label));
	}
	
	public void trimStates(int beamSize)
	{
		beamSize--;
		
		if (l_states.size() > beamSize)
		{
			UTCollection.sortReverseOrder(l_states);
			l_states = l_states.subList(0, beamSize);
		}
	}
	
	public void disableBranching()
	{
		b_branch = false;
	}
	
	public boolean hasMoreState()
	{
		return i_state+1 < l_states.size();
	}
	
	public DEPLabel setToNextState()
	{
		if (!hasMoreState()) return null;
		DEPStateBranch state = l_states.get(++i_state);
		
		i_lambda = state.lambda;
		i_beta   = state.beta;
		n_trans  = state.trans;
		d_score  = state.score;
		s_reduce = state.reduce;
		d_tree.resetHeads(state.heads);
		
		return state.label;
	}
	
	public void addBranch(List<StringInstance> instances)
	{
		l_branches.add(new ObjectsDoubleTriple<List<StringInstance>,StringIntPair[]>(instances, d_tree.getHeads(), getScore()));
	}
	
	public List<ObjectsDoubleTriple<List<StringInstance>,StringIntPair[]>> getBranches()
	{
		return l_branches;
	}
	
	public ObjectsDoubleTriple<List<StringInstance>,StringIntPair[]> getBestBranch()
	{
		return Collections.max(l_branches);
	}
	
	public void setGoldScoresToBranches()
	{
		StringIntPair   gHead, sHead;
		StringIntPair[] sHeads;
		int i, c;
		
		for (ObjectsDoubleTriple<List<StringInstance>,StringIntPair[]> branch : l_branches)
		{
			sHeads = branch.o2;
			
			for (i=1,c=0; i<t_size; i++)
			{
				gHead = g_labels[i];
				sHead = sHeads[i];
				
				if (gHead.i == sHead.i && gHead.s.equals(sHead.s))
					c++;
			}
			
			branch.d = c;
		}
	}
	
	class DEPStateBranch implements Comparable<DEPStateBranch>
	{
		int             lambda;
		int             beta;
		int             trans;
		double          score;
		IntOpenHashSet  reduce;
		StringIntPair[] heads;
		DEPLabel        label;
		
		public DEPStateBranch(DEPLabel label)
		{
			this.lambda = i_lambda;
			this.beta   = i_beta;
			this.trans  = n_trans;
			this.score  = d_score;
			this.reduce = s_reduce.clone();
			this.heads  = d_tree.getHeads();
			this.label  = label;
		}
		
		@Override
		public int compareTo(DEPStateBranch p)
		{
			double diff = label.score - p.label.score;
			
			if      (diff > 0)	return  1;
			else if (diff < 0)	return -1;
			else				return  0;
		}
	}
}
