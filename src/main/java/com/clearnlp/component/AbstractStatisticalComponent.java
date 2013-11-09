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
package com.clearnlp.component;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import com.clearnlp.classification.feature.FtrTemplate;
import com.clearnlp.classification.feature.FtrToken;
import com.clearnlp.classification.feature.JointFtrXml;
import com.clearnlp.classification.model.StringModel;
import com.clearnlp.classification.train.StringTrainSpace;
import com.clearnlp.classification.vector.StringFeatureVector;
import com.clearnlp.component.evaluation.AbstractEval;
import com.clearnlp.component.state.DefaultState;
import com.clearnlp.dependency.DEPArc;
import com.clearnlp.dependency.DEPNode;
import com.clearnlp.reader.AbstractColumnReader;
import com.google.common.collect.Sets;

/**
 * @since 1.3.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
abstract public class AbstractStatisticalComponent<T extends DefaultState> extends AbstractComponent
{
	private final byte FLAG_LEXICA    = 0;
	private final byte FLAG_TRAIN     = 1;
	private final byte FLAG_DECODE    = 2;
	private final byte FLAG_BOOTSTRAP = 3;
	private final byte FLAG_DEVELOP   = 4;
	
	protected StringTrainSpace[]	s_spaces;
	protected StringModel[]			s_models;
	protected JointFtrXml[]			f_xmls;
	protected AbstractEval			e_eval;
	private   byte					i_flag;
	
//	====================================== CONSTRUCTORS ======================================
	
	public AbstractStatisticalComponent() {}
	
	/** Constructs a component for collecting lexica. */
	public AbstractStatisticalComponent(JointFtrXml[] xmls)
	{
		i_flag = FLAG_LEXICA;
		f_xmls = xmls;
	}
	
	/** Constructs a component for training. */
	public AbstractStatisticalComponent(JointFtrXml[] xmls, StringTrainSpace[] spaces, Object[] lexica)
	{
		i_flag   = FLAG_TRAIN;
		f_xmls   = xmls;
		s_spaces = spaces;
		
		initLexia(lexica);
	}
	
	/** Constructs a component for developing. */
	public AbstractStatisticalComponent(JointFtrXml[] xmls, StringModel[] models, Object[] lexica, AbstractEval eval)
	{
		i_flag   = FLAG_DEVELOP;
		f_xmls   = xmls;
		s_models = models;
		e_eval   = eval;

		initLexia(lexica);
	}
	
	/** Constructs a component for decoding. */
	public AbstractStatisticalComponent(ObjectInputStream in)
	{
		i_flag = FLAG_DECODE;
	
		try
		{
			load(in);
		}
		catch (Exception e) {e.printStackTrace();}
	}
	
	/** Constructs a component for bootstrapping. */
	public AbstractStatisticalComponent(JointFtrXml[] xmls, StringTrainSpace[] spaces, StringModel[] models, Object[] lexica)
	{
		i_flag   = FLAG_BOOTSTRAP;
		f_xmls   = xmls;
		s_spaces = spaces;
		s_models = models;
		
		initLexia(lexica);
	}
	
//	====================================== FLAGS ======================================
	
	protected boolean isLexica()
	{
		return i_flag == FLAG_LEXICA;
	}
	
	protected boolean isTrain()
	{
		return i_flag == FLAG_TRAIN;
	}
	
	protected boolean isDecode()
	{
		return i_flag == FLAG_DECODE;
	}
	
	protected boolean isBootstrap()
	{
		return i_flag == FLAG_BOOTSTRAP;
	}
	
	protected boolean isDevelop()
	{
		return i_flag == FLAG_DEVELOP;
	}
	
	protected boolean isTrainOrBootstrap()
	{
		return i_flag == FLAG_TRAIN || i_flag == FLAG_BOOTSTRAP;
	}
	
	protected boolean isDevelopOrDecode()
	{
		return i_flag == FLAG_DEVELOP || i_flag == FLAG_DECODE;
	}
	
//	====================================== ABSTRACT METHODS ======================================

	/** Initializes lexica used for this component. */
	abstract protected void initLexia(Object[] lexica);
	/** @return the set of labels used for this statistical model. */
	abstract public Set<String> getLabels();
	
//	====================================== LOAD/SAVE MODELS ======================================

	/**
	 * Loads all models of this joint-component. 
	 * @throws Exception
	 */
	abstract public void load(ObjectInputStream in) throws Exception;

	/**
	 * Saves all models of this joint-component. 
	 * @throws Exception
	 */
	abstract public void save(ObjectOutputStream out) throws Exception;

	protected void loadDefault(ObjectInputStream in) throws Exception
	{
		LOG.info("Loading feature templates.\n");
		f_xmls = (JointFtrXml[])in.readObject();
		
		LOG.info("Loading models.\n");
		s_models = (StringModel[])in.readObject();
	}
	
	protected void saveDefault(ObjectOutputStream out) throws Exception
	{
		LOG.info("Saving feature templates.\n");
		out.writeObject(f_xmls);
		
		LOG.info("Saving models.\n");
		out.writeObject(s_models);
	}
	
//	====================================== GETTERS/SETTERS ======================================
	
	/** @return all training spaces of this joint-components. */
	public StringTrainSpace[] getTrainSpaces()
	{
		return s_spaces;
	}
	
	/** @return all models of this joint-components. */
	public StringModel[] getModels()
	{
		return s_models;
	}
	
	public void printAccuracies()
	{
		LOG.info(e_eval.toString()+"\n");
	}
	
	public double[] getAccuracies()
	{
		return e_eval.getAccuracies();
	}
	
	protected Set<String> getDefaultLabels()
	{
		Set<String> set = Sets.newHashSet();
		
		for (StringModel model : s_models)
		{
			for (String label : model.getLabels())
				set.add(label);
		}
		
		return set;
	}
	
	/** @return all objects containing lexica. */
	abstract public Object[] getLexica();
	
//	====================================== FEATURE EXTRACTION ======================================

	/** @return a field of the specific feature token (e.g., lemma, pos-tag). */
	abstract protected String getField(FtrToken token, T state);
	
	/** @return multiple fields of the specific feature token (e.g., lemma, pos-tag). */
	abstract protected String[] getFields(FtrToken token, T state);
	
	/** @param the dependency node that is not {@code null}. */
	protected String getDefaultField(FtrToken token, DEPNode node)
	{
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
		else if ((m = JointFtrXml.P_FEAT.matcher(token.field)).find())
		{
			return node.getFeat(m.group(1));
		}
		
		return null;
	}
	
	protected String[] getDefaultFields(FtrToken token, DEPNode node)
	{
		if (token.isField(JointFtrXml.F_DEPREL_SET))
		{
			return getDeprelSet(node.getDependents());
		}
		
		return null;
	}
	
	protected String[] getDeprelSet(List<DEPArc> deps)
	{
		if (deps.isEmpty())	return null;
		
		Set<String> set = new HashSet<String>();
		for (DEPArc arc : deps)	set.add(arc.getLabel());
		
		String[] fields = new String[set.size()];
		set.toArray(fields);
		
		return fields;		
	}
	
//	====================================== FEATURE VECTOR ======================================
	
	/** @return a feature vector using the specific feature template. */
	protected StringFeatureVector getFeatureVector(JointFtrXml xml, T state)
	{
		StringFeatureVector vector = new StringFeatureVector();
		
		for (FtrTemplate template : xml.getFtrTemplates())
			addFeatures(vector, template, state);
		
		return vector;
	}

	/** Called by {@link AbstractStatisticalComponent#getFeatureVector(JointFtrXml)}. */
	private void addFeatures(StringFeatureVector vector, FtrTemplate template, T state)
	{
		FtrToken[] tokens = template.tokens;
		int i, size = tokens.length;
		
		if (template.isSetFeature())
		{
			String[][] fields = new String[size][];
			String[]   tmp;
			
			for (i=0; i<size; i++)
			{
				tmp = getFields(tokens[i], state);
				if (tmp == null)	return;
				fields[i] = tmp;
			}
			
			addFeatures(vector, template.type, fields, 0, "");
		}
		else
		{
			StringBuilder build = new StringBuilder();
			String field;
			
			for (i=0; i<size; i++)
			{
				field = getField(tokens[i], state);
				if (field == null)	return;
				
				if (i > 0)	build.append(AbstractColumnReader.BLANK_COLUMN);
				build.append(field);
			}
			
			vector.addFeature(template.type, build.toString());			
		}
    }
	
	/** Called by {@link #getFeatureVector(JointFtrXml)}. */
	private void addFeatures(StringFeatureVector vector, String type, String[][] fields, int index, String prev)
	{
		if (index < fields.length)
		{
			for (String field : fields[index])
			{
				if (prev.isEmpty())
					addFeatures(vector, type, fields, index+1, field);
				else
					addFeatures(vector, type, fields, index+1, prev + AbstractColumnReader.BLANK_COLUMN + field);
			}
		}
		else
			vector.addFeature(type, prev);
	}
}
