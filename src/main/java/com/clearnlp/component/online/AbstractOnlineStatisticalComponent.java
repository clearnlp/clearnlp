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
package com.clearnlp.component.online;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.clearnlp.classification.feature.FtrTemplate;
import com.clearnlp.classification.feature.FtrToken;
import com.clearnlp.classification.feature.JointFtrXml;
import com.clearnlp.classification.instance.StringInstance;
import com.clearnlp.classification.model.StringModelAD;
import com.clearnlp.classification.vector.StringFeatureVector;
import com.clearnlp.component.evaluation.AbstractEval;
import com.clearnlp.component.state.AbstractState;
import com.clearnlp.reader.AbstractColumnReader;

/**
 * @since 2.0.1
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
abstract public class AbstractOnlineStatisticalComponent<T extends AbstractState> extends AbstractOnlineComponent implements IFlag
{
	protected StringModelAD[] s_models;
	protected JointFtrXml[]       f_xmls;
	protected AbstractEval        e_eval;
	
//	====================================== CONSTRUCTORS ======================================
	
	/** Constructs a component for collecting lexica. */
	public AbstractOnlineStatisticalComponent(JointFtrXml[] xmls)
	{
		f_xmls = xmls;
	}
	
	/** Constructs a component in general. */
	public AbstractOnlineStatisticalComponent(JointFtrXml[] xmls, Object[] lexica, int modelSize)
	{
		f_xmls   = xmls;
		s_models = getEmptyModels(modelSize);
		setLexia(lexica);
	}
	
	/** Constructs a component in general from an existing object. */
	public AbstractOnlineStatisticalComponent(ObjectInputStream in)
	{
		try
		{
			load(in);
		}
		catch (Exception e) {e.printStackTrace();}
	}
	
	private StringModelAD[] getEmptyModels(int modelSize)
	{
		StringModelAD[] models = new StringModelAD[modelSize];
		
		int i; for (i=0; i<modelSize; i++)
			models[i] = new StringModelAD();
		
		return models;
	}
	
//	====================================== LEXICA ======================================

	/** @return all objects containing lexica. */
	abstract public Object[] getLexica();
	
	/** Sets lexica used for this component. */
	abstract public void setLexia(Object[] lexica);
	
//	====================================== LOAD/SAVE MODELS ======================================

	/**
	 * Loads all models and objects of this component. 
	 * @throws Exception
	 */
	abstract public void load(ObjectInputStream in) throws Exception;

	/**
	 * Saves all models and objects of this component. 
	 * @throws Exception
	 */
	abstract public void save(ObjectOutputStream out) throws Exception;

	protected void loadDefault(ObjectInputStream in) throws Exception
	{
		f_xmls   = (JointFtrXml[])in.readObject();
		s_models = (StringModelAD[])in.readObject();
	}
	
	protected void saveDefault(ObjectOutputStream out) throws Exception
	{
		out.writeObject(f_xmls);
		out.writeObject(s_models);
	}
	
//	====================================== GETTERS ======================================

	/** @return the set of labels used for all statistical models. */
	abstract public Set<String> getLabels();
	
	/** @return all models of this component. */
	public StringModelAD[] getModels()
	{
		return s_models;
	}
	
	public AbstractEval getEval()
	{
		return e_eval;
	}
	
	protected List<StringInstance> getEmptyInstanceList(byte flag)
	{
		return isTrainOrBootstrap(flag) ? new ArrayList<StringInstance>() : null;
	}
	
//	====================================== FLAG ======================================
	
	protected boolean isCollect(byte flag)
	{
		return flag == FLAG_COLLECT;
	}
	
	protected boolean isTrain(byte flag)
	{
		return flag == FLAG_TRAIN;
	}
	
	protected boolean isBootstrap(byte flag)
	{
		return flag == FLAG_BOOTSTRAP;
	}
	
	protected boolean isEvaluate(byte flag)
	{
		return flag == FLAG_EVALUATE;
	}
	
	protected boolean isDecode(byte flag)
	{
		return flag == FLAG_DECODE;
	}
	
	public boolean isTrainOrBootstrap(byte flag)
	{
		return flag == FLAG_TRAIN || flag == FLAG_BOOTSTRAP;
	}
	
//	====================================== FEATURE EXTRACTION ======================================
	
	/** @return a feature vector using the specific feature template. */
	protected StringFeatureVector getFeatureVector(JointFtrXml xml, T state)
	{
		StringFeatureVector vector = new StringFeatureVector();
		
		for (FtrTemplate template : xml.getFtrTemplates())
			addFeatures(vector, template, state);
		
		return vector;
	}

	/** Called by {@link #getFeatureVector(JointFtrXml)}. */
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

	/** @return a field of the specific feature token (e.g., lemma, pos-tag). */
	abstract protected String getField(FtrToken token, T state);
	
	/** @return multiple fields of the specific feature token (e.g., lemma, pos-tag). */
	abstract protected String[] getFields(FtrToken token, T state);
}
