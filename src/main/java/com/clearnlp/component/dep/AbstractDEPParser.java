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
package com.clearnlp.component.dep;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import com.clearnlp.classification.algorithm.old.AbstractAlgorithm;
import com.clearnlp.classification.feature.FtrToken;
import com.clearnlp.classification.feature.JointFtrXml;
import com.clearnlp.classification.instance.StringInstance;
import com.clearnlp.classification.model.StringModel;
import com.clearnlp.classification.prediction.StringPrediction;
import com.clearnlp.classification.train.StringTrainSpace;
import com.clearnlp.classification.vector.StringFeatureVector;
import com.clearnlp.component.AbstractStatisticalComponentSB;
import com.clearnlp.component.evaluation.DEPEval;
import com.clearnlp.component.label.IDEPLabel;
import com.clearnlp.component.state.DEPState;
import com.clearnlp.dependency.DEPHead;
import com.clearnlp.dependency.DEPLabel;
import com.clearnlp.dependency.DEPLib;
import com.clearnlp.dependency.DEPLibEn;
import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dependency.DEPTree;
import com.clearnlp.util.UTCollection;
import com.clearnlp.util.pair.ObjectDoublePair;
import com.clearnlp.util.pair.StringIntPair;
import com.clearnlp.util.triple.ObjectsDoubleTriple;
import com.clearnlp.util.triple.Triple;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Dependency parser using selectional branching.
 * @since 1.3.2
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
abstract public class AbstractDEPParser extends AbstractStatisticalComponentSB<DEPState> implements IDEPLabel
{
//	====================================== CONSTRUCTORS ======================================
	
	/** Constructs a dependency parsing for training. */
	public AbstractDEPParser(JointFtrXml[] xmls, StringTrainSpace[] spaces, Object[] lexica, double margin, int beams)
	{
		super(xmls, spaces, lexica, margin, beams);
	}
	
	/** Constructs a dependency parsing for developing. */
	public AbstractDEPParser(JointFtrXml[] xmls, StringModel[] models, Object[] lexica, double margin, int beams)
	{
		super(xmls, models, lexica, new DEPEval(), margin, beams);
	}
	
	/** Constructs a dependency parser for bootsrapping. */
	public AbstractDEPParser(JointFtrXml[] xmls, StringTrainSpace[] spaces, StringModel[] models, Object[] lexica, double margin, int beams)
	{
		super(xmls, spaces, models, lexica, margin, beams);
	}
	
	/** Constructs a dependency parser for decoding. */
	public AbstractDEPParser(ObjectInputStream in)
	{
		super(in);
	}
	
	@Override
	protected void initLexia(Object[] lexica) {}
	
//	====================================== ABSTRACT METHODS ======================================
	
	abstract protected void    rerankPredictions(List<StringPrediction> ps, DEPState state);
	abstract protected boolean resetPre(DEPState state);
	abstract protected void    resetPost(DEPNode lambda, DEPNode beta, DEPLabel label, DEPState state);
	abstract protected void    postProcess(DEPState state);
	abstract protected boolean isNotHead(DEPNode node);
	
//	====================================== LOAD/SAVE MODELS ======================================
	
	@Override
	public void load(ObjectInputStream in) throws Exception
	{
		loadSB(in);
		loadDefault(in);
		in.close();
	}
	
	@Override
	public void save(ObjectOutputStream out)
	{
		try
		{
			saveSB(out);
			saveDefault(out);
			out.close();
		}
		catch (Exception e) {e.printStackTrace();}
	}
	
//	====================================== GETTERS/SETTERS ======================================
	
	@Override
	public Object[] getLexica() {return null;}
	
	@Override
	public Set<String> getLabels()
	{
		Set<String> set = Sets.newHashSet();
		DEPLabel lb;
		
		for (StringModel model : s_models)
		{
			for (String label : model.getLabels())
			{
				lb = new DEPLabel(label);
				set.add(lb.deprel);
			}
		}
		
		return set;
	}
	
//	====================================== PROCESS ======================================
	
	/**
	 * For decoding only.
	 * @param uniqueOnly if {@code true}, include only unique trees.
	 * @return a list of pairs containing parsed trees and their scores, sorted by scores in descending order.
	 */
	public List<ObjectDoublePair<DEPTree>> getParsedTrees(DEPTree tree, boolean uniqueOnly)
	{
		DEPState state = init(tree);
		processAux(state);
		
		List<ObjectsDoubleTriple<List<StringInstance>,StringIntPair[]>> branches = state.getBranches();
		List<ObjectDoublePair<DEPTree>> trees = Lists.newArrayList();
		Set<String> set = Sets.newHashSet();
		String s;
		
		UTCollection.sortReverseOrder(branches);
		
		for (ObjectsDoubleTriple<List<StringInstance>,StringIntPair[]> branch : branches)
		{
			tree.resetHeads(branch.o2);
			processHeadless(state);
			postProcess(state);
			s = Arrays.toString(tree.getHeads());
			
			if (!uniqueOnly || !set.contains(s))
			{
				set.add(s);
				trees.add(new ObjectDoublePair<DEPTree>(tree.clone(), branch.d));
			}
		}
		
		return trees;
	}
	
	@Override
	public void process(DEPTree tree)
	{
		DEPState state = init(tree);
		processAux(state);
		
		if (isDevelopOrDecode())
		{
			processHeadless(state);
			postProcess(state);
			
			if (isDevelop())
				e_eval.countAccuracy(state.getTree(), state.getGoldLabels());
		}
	}
	
	/** Called by {@link AbstractDEPParser#process(DEPTree)}. */
	protected DEPState init(DEPTree tree)
	{
		DEPState state = new DEPState(tree);
		
		if (!isDecode())
	 	{
			state.setGoldLabels(tree.getHeads());
	 		tree.clearHeads();	
	 	}
		
		return state;
	}
	
	/** Called by {@link AbstractDEPParser#process(DEPTree)}. */
	protected void processAux(DEPState state)
	{
		List<StringInstance> insts = parse(state);
		
		if (isTrainOrBootstrap())
			s_spaces[0].addInstances(insts);
		
		if (isDecode() && state.resetPOSTags())
		{
			state.reInit();
			processAux(state);
		}
	}
	
	/** Called by {@link AbstractDEPParser#processAux()}. */
	protected List<StringInstance> parse(DEPState state)
	{
		List<StringInstance> insts = parseOne(state);

		if (state.hasMoreState())
			insts.addAll(parseBranches(state));

		return insts;
	}
	
	protected List<StringInstance> parseOne(DEPState state)
	{
		List<StringInstance> insts = Lists.newArrayList();
		DEPNode  lambda, beta;
		DEPLabel label;
		
		while (state.isBetaValid())
		{
			if (!state.isLambdaValid())
			{
				state.shift();
				continue;
			}
			
			if (resetPre(state))
				continue;

			lambda = state.getLambda();
			beta   = state.getBeta();
			label  = getLabel(insts, state);
			
			parseAux(label, state);
			resetPost(lambda, beta, label, state);
		}
		
		state.trimStates(n_beams);
		state.addBranch(insts);
//		System.out.println(state.getScore());
//		System.out.println(state.getTree().toStringDEP()+"\n");
		return insts;
	}
	
	protected void parseAux(DEPLabel label, DEPState state)
	{
		DEPNode lambda = state.getLambda();
		DEPNode beta   = state.getBeta();
		
		state.increaseTransitionCount();
		state.addScore(label.score);
		
		if (label.isArc(LB_LEFT))
		{
			if (lambda.id == DEPLib.ROOT_ID)
				state.shift();
			else if (beta.isDescendentOf(lambda))
				state.pass();
			else
			{
				leftArc(lambda, beta, label.deprel);

				if (label.isList(LB_REDUCE))	state.reduce();
				else							state.pass();
			}
		}
		else if (label.isArc(LB_RIGHT))
		{
			if (lambda.isDescendentOf(beta))
				state.pass();
			else
			{
				rightArc(lambda, beta, label.deprel);
				
				if (label.isList(LB_SHIFT))	state.shift();
				else						state.pass();
			}
		}
		else
		{
			if (label.isList(LB_SHIFT))
				state.shift();
			else if (label.isList(LB_REDUCE) && lambda.hasHead())
				state.reduce();
			else
				state.pass();
		}
	}
	
	/** Called by {@link #parse()}. */
	protected DEPLabel getLabel(List<StringInstance> insts, DEPState state)
	{
		StringFeatureVector vector = getFeatureVector(f_xmls[0], state);
		DEPLabel label = null;
		
		if (isTrain())
		{
			label = state.getGoldLabel();
			insts.add(new StringInstance(label.toString(), vector));
		}
		else if (isDevelopOrDecode())
		{
			label = getAutoLabel(vector, state);
		}
		else if (isBootstrap())
		{
			label = getAutoLabel(vector, state);
			insts.add(new StringInstance(state.getGoldLabel().toString(), vector));
		}
		
		return label;
	}
	
	/** Called by {@link #getLabel()}. */
	private DEPLabel getAutoLabel(StringFeatureVector vector, DEPState state)
	{
		List<StringPrediction> ps = getPredictions(vector, state);

		DEPLabel fst = new DEPLabel(ps.get(0).label, ps.get(0).score);
		DEPLabel snd = new DEPLabel(ps.get(1).label, ps.get(1).score);
		
		if (fst.score - snd.score < d_margin)
		{
			if (fst.isArc(LB_NO))
				state.add2ndHead(snd);
			
			state.addState(snd);
		}
		
		return fst;
	}
	
	private List<StringPrediction> getPredictions(StringFeatureVector vector, DEPState state)
	{
		List<StringPrediction> ps = s_models[0].predictAll(vector);
		AbstractAlgorithm.normalize(ps);
		rerankPredictions(ps, state);
		
		return ps;
	}
	
	public void leftArc(DEPNode lambda, DEPNode beta, String deprel)
	{
		lambda.setHead(beta, deprel);
	}
	
	public void rightArc(DEPNode lambda, DEPNode beta, String deprel)
	{
		beta.setHead(lambda, deprel);
	}
	
//	====================================== PROCESS HEADLESS ======================================
	
	protected void processHeadless(DEPState state)
	{
		Triple<DEPNode,String,Double> max = new Triple<DEPNode,String,Double>(null, null, -1d);
		DEPNode root = state.getNode(DEPLib.ROOT_ID);
		int i, size = state.getTreeSize();
		List<DEPHead> list;
		DEPNode node, head;
		
		for (i=1; i<size; i++)
		{
			node = state.getNode(i);
			
			if (!node.hasHead())
			{
				if (!(list = state.get2ndHeads(node.id)).isEmpty())
				{
					for (DEPHead p : list)
					{
						head = state.getNode(p.headId);
						
						if (!isNotHead(head) && !head.isDescendentOf(node))
						{
							node.setHead(head, p.deprel);
							break;
						}
					}
				}
				
				if (!node.hasHead())
				{
					max.set(root, DEPLibEn.DEP_ROOT, -1d);
					
					processHeadlessAux(node, -1, max, state);
					processHeadlessAux(node, +1, max, state);
					
					node.setHead(max.o1, max.o2);
				}
			}
		}
	}
	
	protected void processHeadlessAux(DEPNode node, int dir, Triple<DEPNode,String,Double> max, DEPState state)
	{
		int i, size = state.getTreeSize();
		List<StringPrediction> ps;
		DEPLabel label;
		DEPNode  head;
		
		if (dir < 0)	state.setBeta(node.id);
		else			state.setLambda(node.id);
		
		for (i=node.id+dir; 0<=i && i<size; i+=dir)
		{
			head = state.getNode(i);			
			if (head.isDescendentOf(node))	continue;
			
			if (dir < 0)	state.setLambda(i);
			else			state.setBeta(i);
			
			ps = getPredictions(getFeatureVector(f_xmls[0], state), state);
			
			for (StringPrediction p : ps)
			{
				if (p.score <= max.o3)
					break;
				
				label = new DEPLabel(p.label);
				
				if ((dir < 0 && label.isArc(LB_RIGHT)) || (dir > 0 && label.isArc(LB_LEFT)))
				{
					max.set(head, label.deprel, p.score);
					break;
				}
			}
		}
	}
	
//	====================================== SELECTIONAL BRANCHING ======================================

	public List<StringInstance> parseBranches(DEPState state)
	{
		ObjectsDoubleTriple<List<StringInstance>,StringIntPair[]> tm;
		branch(state);
		
		if (isDevelopOrDecode())
		{
			tm = state.getBestBranch();
			state.resetHeads(tm.o2);
		}
		else
		{
			state.setGoldScoresToBranches();
			tm = state.getBestBranch();
		}
		
		return tm.o1;
	}
	
	private void branch(DEPState state)
	{
		state.disableBranching();
		DEPLabel label;
		
		while ((label = state.setToNextState()) != null)
		{
			parseAux(label, state);
			parseOne(state);
		}
	}
	
//	================================ FEATURE EXTRACTION ================================

	@Override
	protected String getField(FtrToken token, DEPState state)
	{
		DEPNode node = state.getNode(token);
		if (node == null)	return null;
		Matcher m;
		
		if (token.isField(JointFtrXml.F_FORM))
		{
			return node.form;
		}
		else if (token.isField(JointFtrXml.F_SIMPLIFIED_FORM))
		{
			return node.simplifiedForm;
		}
		else if (token.isField(JointFtrXml.F_LEMMA))
		{
			return node.lemma;
		}
		else if (token.isField(JointFtrXml.F_POS))
		{
			return node.pos;
		}
		else if (token.isField(JointFtrXml.F_DEPREL))
		{
			return node.getLabel();
		}
		else if (token.isField(JointFtrXml.F_DISTANCE))
		{
			int dist = state.getDistance();
			return (dist > 6) ? "6" : Integer.toString(dist);
		}
		else if (token.isField(JointFtrXml.F_LEFT_VALENCY))
		{
			return state.getLeftValency(node.id);
		}
		else if (token.isField(JointFtrXml.F_RIGHT_VALENCY))
		{
			return state.getRightValency(node.id);
		}
		else if ((m = JointFtrXml.P_BOOLEAN.matcher(token.field)).find())
		{
			int field = Integer.parseInt(m.group(1));
			
			switch (field)
			{
			case  0: return state.isLambdaFirst() ? token.field : null;
			case  1: return state.isBetaLast() ? token.field : null;
			case  2: return state.isLambdaBetaAdjacent() ? token.field : null;
			default: throw new IllegalArgumentException("Unsupported feature: "+field);
			}
		}
		else if ((m = JointFtrXml.P_FEAT.matcher(token.field)).find())
		{
			return node.getFeat(m.group(1));
		}
		
		return null;
	}
	
	@Override
	protected String[] getFields(FtrToken token, DEPState state)
	{
		return null;
	}
}
