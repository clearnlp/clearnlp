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
package com.clearnlp.component.pred;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import com.clearnlp.classification.feature.FtrToken;
import com.clearnlp.classification.feature.JointFtrXml;
import com.clearnlp.classification.instance.StringInstance;
import com.clearnlp.classification.model.AbstractModel;
import com.clearnlp.classification.model.StringModel;
import com.clearnlp.classification.prediction.StringPrediction;
import com.clearnlp.classification.train.StringTrainSpace;
import com.clearnlp.classification.vector.StringFeatureVector;
import com.clearnlp.component.AbstractStatisticalComponent;
import com.clearnlp.component.evaluation.PredEval;
import com.clearnlp.component.state.POSState;
import com.clearnlp.dependency.DEPLib;
import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dependency.DEPTree;
import com.google.common.collect.Lists;

/**
 * PropBank predicate identifier.
 * @since 1.3.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
abstract public class AbstractPredicateIdentifier extends AbstractStatisticalComponent<POSState>
{
//	====================================== CONSTRUCTORS ======================================

	/** Constructs a predicate identifier for collecting lexica. */
	public AbstractPredicateIdentifier(JointFtrXml[] xmls)
	{
		super(xmls);
	}
		
	/** Constructs a predicate identifier for training. */
	public AbstractPredicateIdentifier(JointFtrXml[] xmls, StringTrainSpace[] spaces, Object[] lexica)
	{
		super(xmls, spaces, lexica);
	}
	
	/** Constructs a predicate identifier for developing. */
	public AbstractPredicateIdentifier(JointFtrXml[] xmls, StringModel[] models, Object[] lexica)
	{
		super(xmls, models, lexica, new PredEval());
	}
	
	/** Constructs a predicate identifier for decoding. */
	public AbstractPredicateIdentifier(ObjectInputStream in)
	{
		super(in);
	}
	
	@Override
	protected void initLexia(Object[] lexica) {}
	
//	====================================== ABSTRACT METHODS ======================================

	abstract protected void resetNode(DEPNode node);
	
//	====================================== LOAD/SAVE MODELS ======================================
	
	@Override
	public void load(ObjectInputStream in)
	{
		try
		{
			loadDefault(in);
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
			out.close();
		}
		catch (Exception e) {e.printStackTrace();}
	}

//	====================================== GETTERS AND SETTERS ======================================

	@Override
	public Object[] getLexica() {return null;}
	
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
	
	/** Called by {@link AbstractPredicateIdentifier#process(DEPTree)}. */
	protected POSState init(DEPTree tree)
	{
		POSState state = new POSState(tree);
		tree.setDependents();
		
		if (!isDecode())
		{
			state.setGoldLabels(tree.getRolesetIDs());
			tree.clearPredicates();
		}
	 	
	 	return state;
	}
	
	/** Called by {@link AbstractPredicateIdentifier#process(DEPTree)}. */
	protected void processAux(POSState state)
	{
		List<StringInstance> insts = identify(state);
		
		if (isTrain())
			s_spaces[0].addInstances(insts);
	}
	
	protected List<StringInstance> identify(POSState state)
	{
		List<StringInstance> insts = Lists.newArrayList();
		String label;
		DEPNode node;
		
		while ((node = state.shift()) != null)
		{
			if (f_xmls[0].isPredicate(node))
			{
				label = getLabel(insts, state);
				
				if (AbstractModel.toBoolean(label))
				{
					resetNode(node);
					node.addFeat(DEPLib.FEAT_PB, node.lemma+".XX");
				}
			}
		}
		
		return insts;
	}
	
	/** Called by {@link AbstractPredicateIdentifier#identify()}. */
	protected String getLabel(List<StringInstance> insts, POSState state)
 	 {
		StringFeatureVector vector = getFeatureVector(f_xmls[0], state);
		String label = null;
		
		if (isTrain())
		{
			label = AbstractModel.getBooleanLabel(state.getGoldLabel() != null);
			insts.add(new StringInstance(label, vector));
		}
		else if (isDevelopOrDecode())
		{
			label = getAutoLabel(vector);
		}
		
		return label;
	}
	
	/** Called by {@link AbstractPredicateIdentifier#getLabel()}. */
	private String getAutoLabel(StringFeatureVector vector)
	{
		StringPrediction p = s_models[0].predictBest(vector);
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
