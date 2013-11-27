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
package com.clearnlp.component.srl;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import com.carrotsearch.hppc.IntOpenHashSet;
import com.clearnlp.classification.feature.FtrToken;
import com.clearnlp.classification.feature.JointFtrXml;
import com.clearnlp.classification.instance.StringInstance;
import com.clearnlp.classification.model.StringModel;
import com.clearnlp.classification.prediction.StringPrediction;
import com.clearnlp.classification.train.StringTrainSpace;
import com.clearnlp.classification.vector.StringFeatureVector;
import com.clearnlp.component.AbstractStatisticalComponent;
import com.clearnlp.component.evaluation.SRLEval;
import com.clearnlp.component.state.SRLState;
import com.clearnlp.constant.universal.UNConstant;
import com.clearnlp.constant.universal.UNPunct;
import com.clearnlp.dependency.DEPArc;
import com.clearnlp.dependency.DEPLibEn;
import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dependency.DEPTree;
import com.clearnlp.dependency.srl.SRLLib;
import com.clearnlp.propbank.PBLib;
import com.clearnlp.propbank.frameset.AbstractFrames;
import com.clearnlp.propbank.frameset.PBRoleset;
import com.clearnlp.propbank.frameset.PBType;
import com.clearnlp.util.UTCollection;
import com.clearnlp.util.map.Prob1DMap;
import com.clearnlp.util.pair.ObjectDoublePair;
import com.clearnlp.util.pair.StringIntPair;

/**
 * @since 1.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
abstract public class AbstractSRLabeler extends AbstractStatisticalComponent<SRLState>
{
	protected final int LEXICA_PATH_UP	 = 0;
	protected final int LEXICA_PATH_DOWN = 1;
	protected final int LEXICA_FRAMES	 = 2;
	
	protected final int PATH_ALL		 = 0;
	protected final int PATH_UP			 = 1;
	protected final int PATH_DOWN		 = 2;
	protected final int SUBCAT_ALL		 = 0;
	protected final int SUBCAT_LEFT		 = 1;
	protected final int SUBCAT_RIGHT	 = 2;
	
	protected final String LB_NO_ARG = "N";
	
	protected Prob1DMap		 m_down, m_up;	// only for collecting
	protected Set<String>	 s_down, s_up;
	protected AbstractFrames m_frames;
	
//	====================================== CONSTRUCTORS ======================================
	
	/** Constructs a semantic role labeler for collecting lexica. */
	public AbstractSRLabeler(JointFtrXml[] xmls, AbstractFrames frames)
	{
		super(xmls);
		m_down   = new Prob1DMap();
		m_up     = new Prob1DMap();
		m_frames = frames;
	}
	
	/** Constructs a semantic role labeler for training. */
	public AbstractSRLabeler(JointFtrXml[] xmls, StringTrainSpace[] spaces, Object[] lexica)
	{
		super(xmls, spaces, lexica);
	}
	
	/** Constructs a semantic role labeler for developing. */
	public AbstractSRLabeler(JointFtrXml[] xmls, StringModel[] models, Object[] lexica)
	{
		super(xmls, models, lexica, new SRLEval());
	}
	
	/** Constructs a semantic role labeler for decoding. */
	public AbstractSRLabeler(ObjectInputStream in)
	{
		super(in);
	}
	
	/** Constructs a semantic role labeler for bootstrapping. */
	public AbstractSRLabeler(JointFtrXml[] xmls, StringTrainSpace[] spaces, StringModel[] models, Object[] lexica)
	{
		super(xmls, spaces, models, lexica);
	}
	
	@Override @SuppressWarnings("unchecked")
	protected void initLexia(Object[] lexica)
	{
		s_down   = (Set<String>)   lexica[LEXICA_PATH_DOWN];
		s_up     = (Set<String>)   lexica[LEXICA_PATH_UP];
		m_frames = (AbstractFrames)lexica[LEXICA_FRAMES];
	}
	
//	====================================== ABSTRACT METHODS ======================================
	
	abstract protected String getHardLabel(SRLState state, String label);
	abstract protected PBType getPBType(DEPNode pred);
	abstract protected void postLabel(SRLState state);
	abstract protected DEPNode getPossibleDescendent(DEPNode pred, DEPNode arg);
	abstract protected boolean rerankFromArgument(StringPrediction prediction, DEPNode arg);
	
//	====================================== LOAD/SAVE MODELS ======================================
	
	@Override
	public void load(ObjectInputStream in)
	{
		try
		{
			loadDefault(in);
			loadLexica(in);
			in.close();
		}
		catch (Exception e) {e.printStackTrace();}
	}

	@Override
	public void save(ObjectOutputStream out)
	{
		try
		{
			saveDefault(out);
			saveLexica(out);
			out.close();
		}
		catch (Exception e) {e.printStackTrace();}
	}
	
	private void loadLexica(ObjectInputStream in) throws Exception
	{
		LOG.info("Loading lexica.\n");
		
		Object[] lexica = {in.readObject(), in.readObject(), in.readObject()};
		initLexia(lexica);
	}
	
	private void saveLexica(ObjectOutputStream out) throws Exception
	{
		LOG.info("Saving lexica.\n");
		
		out.writeObject(s_down);
		out.writeObject(s_up);
		out.writeObject(m_frames);
	}
	
//	====================================== GETTERS/SETTERS ======================================
	
	@Override
	public Object[] getLexica()
	{
		Object[] lexica = new Object[3];
		
		lexica[LEXICA_PATH_DOWN] = (isLexica()) ? m_down.toSet(f_xmls[0].getPathDownCutoff()) : s_down; 
		lexica[LEXICA_PATH_UP]   = (isLexica()) ? m_up  .toSet(f_xmls[0].getPathUpCutoff())   : s_up;
		lexica[LEXICA_FRAMES]    = m_frames;
		
		return lexica;
	}
	
	public Set<String> getDownSet(int cutoff)
	{
		return m_down.toSet(cutoff);
	}
	
	public Set<String> getUpSet(int cutoff)
	{
		return m_up.toSet(cutoff);
	}
	
	@Override
	public Set<String> getLabels()
	{
		return getDefaultLabels();
	}

//	================================ PROCESS ================================
	
	@Override
	public void process(DEPTree tree)
	{
		SRLState state = init(tree);
		processAux(state);
		
		if (isDevelop())
			e_eval.countAccuracy(state.getTree(), state.getGoldLabels());
	}
	
	/** Called by {@link AbstractSRLabeler#process(DEPTree)}. */
	protected SRLState init(DEPTree tree)
	{
		SRLState state = new SRLState(tree);
		
		if (!isDecode())
		{
			state.setGoldLabels(tree.getSHeads());
			tree.clearSHeads();
		}
		else
			tree.initSHeads();

		return state;
	}
	
	protected void processAux(SRLState state)
	{
		if (isLexica())	addLexica(state);
		else			label(state);
	}
	
	private void addLexica(SRLState state)
	{
		DEPNode pred, head;
		
		while ((pred = state.moveToNextPredicate()) != null)
		{
			for (DEPArc arc : pred.getGrandDependents())
				collectDown(pred, arc.getNode());
		
			head = pred.getHead();
			if (head != null) collectUp(pred, head.getHead());
		}
	}
	
	private void collectDown(DEPNode pred, DEPNode arg)
	{
		if (arg.isArgumentOf(pred))
		{
			for (String path : getDUPathList(pred, arg.getHead()))
				m_down.add(path);
		}
		
		for (DEPArc arc : arg.getDependents())
			collectDown(pred, arc.getNode());
	}
	
	private void collectUp(DEPNode pred, DEPNode head)
	{
		if (head == null)	return;
		
		for (DEPArc arc : head.getDependents())
		{
			if (arc.getNode().isArgumentOf(pred))
			{
				for (String path : getDUPathList(head, pred))
					m_up.add(path);
				
				break;
			}
		}	
		
		collectUp(pred, head.getHead());
	}
	
	private String getDUPath(DEPNode top, DEPNode bottom)
	{
		return getPathAux(top, bottom, JointFtrXml.F_DEPREL, SRLLib.DELIM_PATH_DOWN, true);
	}
	
	private List<String> getDUPathList(DEPNode top, DEPNode bottom)
	{
		List<String> paths = new ArrayList<String>();
		
		while (bottom != top)
		{
			paths.add(getDUPath(top, bottom));
			bottom = bottom.getHead();
		}
		
		return paths;
	}
	
	private void label(SRLState state)
	{
		DEPNode pred;
		
		while ((pred = state.moveToNextPredicate()) != null)
		{
			setRoleset(pred, state);

			do
			{
				labelAux(state);
			}
			while (state.moveToNextLowestCommonAncestor());// && (pred.isDependentOf(d_lca) || s_up.contains(getDUPath(d_lca, pred))));
		}
		
		postLabel(state);
	}
	
	private void setRoleset(DEPNode pred, SRLState state)
	{
		if (m_frames != null)
		{
			PBType type = getPBType(pred);
			
			if (type != null)
				state.setRoleset(m_frames.getRoleset(type, pred.lemma, pred.getFeat(DEPLibEn.FEAT_PB)));
		}
	}
	
	/** Called by {@link AbstractSRLabeler#label(DEPTree)}. */
	private void labelAux(SRLState state)
	{
		DEPNode head = state.getLowestCommonAncestor();
		
		if (!state.isSkip(head))
		{
			state.setArgument(head);
			addArgument(getLabel(state), state);	
		}
		
		labelDown(head.getDependents(), state);
	}
	
	/** Called by {@link AbstractSRLabeler#labelAux(DEPNode, IntOpenHashSet)}. */
	private void labelDown(List<DEPArc> arcs, SRLState state)
	{
		DEPNode pred = state.getCurrentPredicate();
		DEPNode arg;
		
		for (DEPArc arc : arcs)
		{
			arg = arc.getNode();
			
			if (!state.isSkip(arg))
			{
				state.setArgument(arg);
				addArgument(getLabel(state), state);
				
				if (state.isLowestCommonAncestor(pred))
				{
					if (s_down.contains(getDUPath(pred, arg)))
						labelDown(arg.getDependents(), state);
					else if ((arg = getPossibleDescendent(pred, arg)) != null)
						labelDown(arg.getDependents(), state);
				}
				
//				if (state.isLowestCommonAncestor(pred) && s_down.contains(getDUPath(pred, arg)))
//					labelDown(arg.getDependents(), state);
			}
		}
	}
	
	private StringPrediction getLabel(SRLState state)
	{
		StringFeatureVector vector = getFeatureVector(f_xmls[0], state);
		int idx = state.getDirection();
		StringPrediction p = null;
		
		if (isTrain())
		{
			p = new StringPrediction(getGoldLabel(state), 1d);
			s_spaces[idx].addInstance(new StringInstance(p.label, vector));
		}
		else if (isDevelopOrDecode())
		{
			p = getAutoLabel(idx, vector, state);
		}
		else if (isBootstrap())
		{
			p = getAutoLabel(idx, vector, state);
			s_spaces[idx].addInstance(new StringInstance(getGoldLabel(state), vector));
		}

		return p;
	}
	
	/** Called by {@link AbstractSRLabeler#getGoldLabel(byte)}. */
	private String getGoldLabel(SRLState state)
	{
		for (StringIntPair head : state.getGoldLabel())
		{
			if (head.i == state.getCurrPredicateID())
				return head.s;
		}
		
		return LB_NO_ARG;
	}

	/** Called by {@link AbstractSRLabeler#getLabel(byte)}. */
	private StringPrediction getAutoLabel(int idx, StringFeatureVector vector, SRLState state)
	{
		StringPrediction p = getBestPrediction(s_models[idx], vector, state);
		
		if (isDecode() && !p.label.equals(LB_NO_ARG))
		{
			String label = getHardLabel(state, p.label);
			if (label != null)	p.label = label;
		}
		
		return p;
	}

	private void addArgument(StringPrediction p, SRLState state)
	{
		DEPNode arg = state.getCurrentArgument();
		ObjectDoublePair<DEPNode> prev;
		DEPNode node;

		state.addArgumentToSkipList();
		
		if (!p.label.equals(LB_NO_ARG))
		{
			if (PBLib.isNumberedArgument(p.label))
			{
				state.addNumberedArgument(p.label);
				
				if (PBLib.isCoreNumberedArgument(p.label))
				{
					if ((prev = state.getCoreNumberedArgument(p.label)) != null)
					{
						node = (DEPNode)prev.o;
						node.removeSHeadsByLabel(p.label);
					}
					
					state.putCoreNumberedArgument(p.label, new ObjectDoublePair<DEPNode>(arg, p.score));
				}
			}
			
			String fTag = UNConstant.EMPTY;
			PBRoleset roleset = state.getRoleset();
			
			if (!p.label.contains(UNPunct.HYPHEN) && roleset != null)
			{
				String n = PBLib.getNumber(p.label);
				fTag = roleset.getFunctionTag(n);
			}
			
			arg.addSHead(state.getCurrentPredicate(), p.label, fTag);
		}
	}
	
//	================================ FEATURE EXTRACTION ================================

	@Override
	protected String getField(FtrToken token, SRLState state)
	{
		DEPNode node = state.getNode(token);
		if (node == null)	return null;
		Matcher m;
		
		if (token.isField(JointFtrXml.F_FORM))
		{
			return node.form;
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
			return getDistance(node, state);
		}
		else if ((m = JointFtrXml.P_ARGN.matcher(token.field)).find())
		{
			return state.getNumberedArgument(Integer.parseInt(m.group(1)));
		}
		else if ((m = JointFtrXml.P_PATH.matcher(token.field)).find())
		{
			String type = m.group(1);
			int    dir  = Integer.parseInt(m.group(2));
			
			return getPath(type, dir, state);
		}
		else if ((m = JointFtrXml.P_SUBCAT.matcher(token.field)).find())
		{
			String type = m.group(1);
			int    dir  = Integer.parseInt(m.group(2));
			
			return getSubcat(node, type, dir);
		}
		else if ((m = JointFtrXml.P_FEAT.matcher(token.field)).find())
		{
			return node.getFeat(m.group(1));
		}
		else if ((m = JointFtrXml.P_BOOLEAN.matcher(token.field)).find())
		{
			DEPNode pred = state.getCurrentPredicate();
			int    field = Integer.parseInt(m.group(1));
			
			switch (field)
			{
			case 0: return (node.isDependentOf(pred)) ? token.field : null;
			case 1: return (pred.isDependentOf(node)) ? token.field : null;
			case 2: return (pred.isDependentOf(state.getLowestCommonAncestor())) ? token.field : null;
			case 3: return (state.isLowestCommonAncestor(pred)) ? token.field : null;
			case 4: return (state.isLowestCommonAncestor(node)) ? token.field : null;
			}
		}
		
		return null;
	}
	
	@Override
	protected String[] getFields(FtrToken token, SRLState state)
	{
		DEPNode node = state.getNode(token);
		if (node == null)	return null;
		
		if (token.isField(JointFtrXml.F_DEPREL_SET))
		{
			return getDeprelSet(node.getDependents());
		}
		else if (token.isField(JointFtrXml.F_GRAND_DEPREL_SET))
		{
			return getDeprelSet(node.getGrandDependents());
		}
		
		return null;
	}
	
	private String getDistance(DEPNode node, SRLState state)
	{
		int dist = Math.abs(state.getCurrPredicateID() - node.id);
		
		if      (dist <=  5)	return "0";
		else if (dist <= 10)	return "1";
		else if (dist <= 15)	return "2";
		else					return "3";
	}
	
	private String getPath(String type, int dir, SRLState state)
	{
		DEPNode pred = state.getCurrentPredicate();
		DEPNode arg  = state.getCurrentArgument();
		DEPNode lca  = state.getLowestCommonAncestor();
		
		if (dir == PATH_UP)
		{
			if (lca != pred)
				return getPathAux(lca, pred, type, SRLLib.DELIM_PATH_UP, true);
		}
		else if (dir == PATH_DOWN)
		{
			if (lca != arg)
				return getPathAux(lca, arg, type, SRLLib.DELIM_PATH_DOWN, true);
		}
		else
		{
			if (pred == lca)
				return getPathAux(pred, arg, type, SRLLib.DELIM_PATH_DOWN, true);
			else if (pred.isDescendentOf(arg))
				return getPathAux(arg, pred, type, SRLLib.DELIM_PATH_UP, true);
			else
			{
				String path = getPathAux(lca, pred, type, SRLLib.DELIM_PATH_UP, true);
				path += getPathAux(lca, arg, type, SRLLib.DELIM_PATH_DOWN, false);
				
				return path;
			}			
		}
		
		return null;
	}
	
	private String getPathAux(DEPNode top, DEPNode bottom, String type, String delim, boolean includeTop)
	{
		StringBuilder build = new StringBuilder();
		DEPNode head = bottom;
		int dist = 0;
		
		do
		{
			if (type.equals(JointFtrXml.F_POS))
			{
				build.append(delim);
				build.append(head.pos);
			}
			else if (type.equals(JointFtrXml.F_DEPREL))
			{
				build.append(delim);
				build.append(head.getLabel());
			}
			else if (type.equals(JointFtrXml.F_DISTANCE))
			{
				dist++;
			}
		
			head = head.getHead();
		}
		while (head != top && head != null);
		
		if (type.equals(JointFtrXml.F_POS))
		{
			if (includeTop)
			{
				build.append(delim);
				build.append(top.pos);	
			}
		}
		else if (type.equals(JointFtrXml.F_DISTANCE))
		{
			build.append(delim);
			build.append(dist);
		}
		
		return build.length() == 0 ? null : build.toString();
	}
	
	private String getSubcat(DEPNode node, String type, int dir)
	{
		List<DEPArc>  deps  = node.getDependents();
		StringBuilder build = new StringBuilder();
		int i, size = deps.size();
		DEPNode dep;
		
		if (dir == SUBCAT_LEFT)
		{
			for (i=0; i<size; i++)
			{
				dep = deps.get(i).getNode();
				if (dep.id > node.id)	break;
				getSubcatAux(build, dep, type);
			}
		}
		else if (dir == SUBCAT_RIGHT)
		{
			for (i=size-1; i>=0; i--)
			{
				dep = deps.get(i).getNode();
				if (dep.id < node.id)	break;
				getSubcatAux(build, dep, type);
			}
		}
		else
		{
			for (i=0; i<size; i++)
			{
				dep = deps.get(i).getNode();
				getSubcatAux(build, dep, type);
			}
		}
		
		return build.length() == 0 ? null : build.substring(SRLLib.DELIM_SUBCAT.length());
	}
	
	private void getSubcatAux(StringBuilder build, DEPNode node, String type)
	{
		build.append(SRLLib.DELIM_SUBCAT);
		
		if (type.equals(JointFtrXml.F_POS))
			build.append(node.pos);
		else if (type.equals(JointFtrXml.F_DEPREL))
			build.append(node.getLabel());
	}
	
//	================================ RERANK ================================
	
	private StringPrediction getBestPrediction(StringModel model, StringFeatureVector vector, SRLState state)
	{
		List<StringPrediction> ps = model.predictAll(vector);
		rerankPredictions(ps, state);

		return ps.get(0);
	}
	
	protected void rerankPredictions(List<StringPrediction> ps, SRLState state)
	{
		DEPNode arg = state.getCurrentArgument();
		boolean change = false;
		
		for (StringPrediction p : ps)
		{
			if (rerankFrameMismatch(p, state) || rerankRedundantNumberedArgument(p, state) || rerankFromArgument(p, arg))
			{
				p.score = -1;
				change = true;
			}
		}
		
		if (change)	UTCollection.sortReverseOrder(ps);
	}
	
	protected boolean rerankFrameMismatch(StringPrediction prediction, SRLState state)
	{
		PBRoleset roleset = state.getRoleset();
		
		if (roleset != null && !roleset.isValidArgument(prediction.label))
			return true;
		
		return false;
	}
	
	protected boolean rerankRedundantNumberedArgument(StringPrediction prediction, SRLState state)
	{
		ObjectDoublePair<DEPNode> prev = state.getCoreNumberedArgument(prediction.label);
		
		if (prev != null && prev.d >= prediction.score)
			return true;
		
		return false;
	}
}
