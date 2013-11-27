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
package com.clearnlp.component.role;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import com.clearnlp.classification.feature.FtrToken;
import com.clearnlp.classification.feature.JointFtrXml;
import com.clearnlp.classification.instance.StringInstance;
import com.clearnlp.classification.model.StringModel;
import com.clearnlp.classification.prediction.StringPrediction;
import com.clearnlp.classification.train.StringTrainSpace;
import com.clearnlp.classification.vector.StringFeatureVector;
import com.clearnlp.collection.map.ObjectIntHashMap;
import com.clearnlp.component.AbstractStatisticalComponent;
import com.clearnlp.component.evaluation.RoleEval;
import com.clearnlp.component.state.POSState;
import com.clearnlp.dependency.DEPLib;
import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dependency.DEPTree;
import com.google.common.collect.Maps;

/**
 * @since 1.3.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
abstract public class AbstractRolesetClassifier extends AbstractStatisticalComponent<POSState>
{
	protected final int LEXICA_ROLESETS  = 0;
	protected final int LEXICA_LEMMAS    = 1;
	
	protected Map<String,Set<String>>	m_collect;	// for collecting lexica
	protected Map<String,String>		m_rolesets;
	protected ObjectIntHashMap<String>	m_lemmas;
	protected String[]					g_rolesets;
	
//	====================================== CONSTRUCTORS ======================================

	/** Constructs a roleset classifier for collecting lexica. */
	public AbstractRolesetClassifier(JointFtrXml[] xmls)
	{
		super(xmls);
		m_collect = new HashMap<String,Set<String>>();
	}
		
	/** Constructs a roleset classifier for training. */
	public AbstractRolesetClassifier(JointFtrXml[] xmls, StringTrainSpace[] spaces, Object[] lexica)
	{
		super(xmls, spaces, lexica);
	}
	
	/** Constructs a roleset classifier for developing. */
	public AbstractRolesetClassifier(JointFtrXml[] xmls, StringModel[] models, Object[] lexica)
	{
		super(xmls, models, lexica, new RoleEval());
	}
	
	/** Constructs a roleset classifier for decoding. */
	public AbstractRolesetClassifier(ObjectInputStream in)
	{
		super(in);
	}
	
	@Override @SuppressWarnings("unchecked")
	protected void initLexia(Object[] lexica)
	{
		m_rolesets = (Map<String,String>)lexica[LEXICA_ROLESETS];
		m_lemmas   = (ObjectIntHashMap<String>)lexica[LEXICA_LEMMAS];
	}
	
//	====================================== ABSTRACT METHODS ======================================

	abstract protected String getDefaultLabel(DEPNode node);
	
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
		
		Object[] lexica = {in.readObject(), in.readObject()};
		initLexia(lexica);
	}
	
	private void saveLexica(ObjectOutputStream out) throws Exception
	{
		LOG.info("Saving lexica.\n");
		
		out.writeObject(m_rolesets);
		out.writeObject(m_lemmas);
	}
	
//	====================================== GETTERS AND SETTERS ======================================

	@Override
	public Object[] getLexica()
	{
		Object[] lexica = new Object[2];
		Map<String,String> mRolesets = getRolesetMap();
		
		lexica[LEXICA_ROLESETS] = mRolesets;
		lexica[LEXICA_LEMMAS]   = getLemmas(m_collect.keySet(), mRolesets);
		
		return lexica;
	}
	
	private Map<String,String> getRolesetMap()
	{
		Map<String,String> map = Maps.newHashMap();
		Set<String> set;
		
		for (String lemma : m_collect.keySet())
		{
			set = m_collect.get(lemma);
			
			if (set.size() == 1)
				map.put(lemma, new ArrayList<String>(set).get(0));
		}
		
		return map;
	}
	
	private ObjectIntHashMap<String> getLemmas(Set<String> sLemmas, Map<String,String> mRolesets)
	{
		ObjectIntHashMap<String> map = new ObjectIntHashMap<String>();
		int idx = 0;
		
		for (String lemma : sLemmas)
		{
			if (!mRolesets.containsKey(lemma))
				map.put(lemma, idx++);
		}
		
		return map;
	}
	
	@Override
	public Set<String> getLabels()
	{
		return getDefaultLabels();
	}
	
//	====================================== PROCESS ======================================
	
	@Override
	public void process(DEPTree tree)
	{
		POSState state = init(tree);
		processAux(state);
		
		if (isDevelop())
			e_eval.countAccuracy(state.getTree(), state.getGoldLabels());
	}
	
	/** Called by {@link AbstractRolesetClassifier#process(DEPTree)}. */
	protected POSState init(DEPTree tree)
	{
		POSState state = new POSState(tree);
		tree.setDependents();

	 	if (!isDecode())
	 		state.setGoldLabels(tree.getRolesetIDs());
	 	
	 	return state;
	}
	
	/** Called by {@link AbstractRolesetClassifier#process(DEPTree)}. */
	protected void processAux(POSState state)
	{
		if (isLexica()) addLexica(state);
		else			classify (state);
	}
	
	protected void addLexica(POSState state)
	{
		String roleset, lemma;
		Set<String> set;
		DEPNode pred;
		
		while ((pred = state.shift()) != null)
		{
			roleset = state.getGoldLabel();
			lemma   = pred.lemma;
			
			if (roleset != null)
			{
				set = m_collect.get(lemma);
				
				if (set == null)
				{
					set = new HashSet<String>();
					m_collect.put(lemma, set);
				}
				
				set.add(roleset);
			}
		}
	}
	
	/** Called by {@link AbstractRolesetClassifier#processAux()}. */
	protected void classify(POSState state)
	{
		String roleset;
		DEPNode node;
		
		while ((node = state.shift()) != null)
		{
			if (node.getFeat(DEPLib.FEAT_PB) != null)
			{
				if ((roleset = m_rolesets.get(node.lemma)) == null)
				{
					if (m_lemmas.containsKey(node.lemma))
						roleset = getLabel(m_lemmas.get(node.lemma), state);
					else
						roleset = getDefaultLabel(node);
				}
				
				node.addFeat(DEPLib.FEAT_PB, roleset);				
			}
		}
	}
	
	/** Called by {@link AbstractRolesetClassifier#classify()}. */
	protected String getLabel(int modelId, POSState state)
 	 {
		StringFeatureVector vector = getFeatureVector(f_xmls[0], state);
		String label = null;
		
		if (isTrain())
		{
			label = state.getGoldLabel();
			s_spaces[modelId].addInstance(new StringInstance(label, vector));
		}
		else if (isDevelopOrDecode())
		{
			label = getAutoLabel(vector, modelId);
		}
		
		return label;
	}
	
	/** Called by {@link AbstractRolesetClassifier#getLabel()}. */
	private String getAutoLabel(StringFeatureVector vector, int modelId)
	{
		StringPrediction p = s_models[modelId].predictBest(vector);
		return p.label;
	}

//	====================================== FEATURE EXTRACTION ======================================

	@Override
	protected String getField(FtrToken token, POSState state)
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
		else if ((m = JointFtrXml.P_FEAT.matcher(token.field)).find())
		{
			return node.getFeat(m.group(1));
		}
		
		return null;
	}
	
	@Override
	protected String[] getFields(FtrToken token, POSState state)
	{
		DEPNode node = state.getNode(token);
		if (node == null)	return null;
		
		if (token.isField(JointFtrXml.F_DEPREL_SET))
		{
			return getDeprelSet(node.getDependents());
		}
		
		return null;
	}
}