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
package com.clearnlp.nlp.train;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Element;

import com.clearnlp.classification.feature.JointFtrXml;
import com.clearnlp.classification.model.StringModel;
import com.clearnlp.classification.train.StringTrainSpace;
import com.clearnlp.component.AbstractStatisticalComponent;
import com.clearnlp.component.pos.AbstractPOSTagger;
import com.clearnlp.component.pos.DefaultPOSTagger;
import com.clearnlp.component.pos.EnglishPOSTagger;
import com.clearnlp.dependency.DEPTree;
import com.clearnlp.morphology.Embedding;
import com.clearnlp.nlp.NLPMode;
import com.clearnlp.nlp.NLPProcess;
import com.clearnlp.reader.AbstractReader;
import com.clearnlp.reader.JointReader;
import com.clearnlp.util.UTInput;
import com.clearnlp.util.UTXml;
import com.clearnlp.util.map.Prob1DMap;

/**
 * @since 2.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class POSTrainer extends AbstractNLPTrainer
{
	@Override
	protected AbstractStatisticalComponent<?> getComponent(Element eConfig, JointReader reader, JointFtrXml[] xmls, String[] trainFiles, int devId)
	{
		AbstractPOSTagger collector = getCollector(eConfig, reader, getLanguage(eConfig), xmls, trainFiles, devId);
		return getTrainedComponentBoot(eConfig, reader, collector, xmls, trainFiles, devId);
	}
	
	@Override
	protected AbstractStatisticalComponent<?> getComponent(Element eTrain, String language, JointFtrXml[] xmls, StringModel[] models, Object[] lexica)
	{
		if (language.equals(AbstractReader.LANG_EN))
			return new EnglishPOSTagger(xmls, models, lexica);
		
		return new DefaultPOSTagger(xmls, models, lexica);
	}
	
	@Override
	protected AbstractStatisticalComponent<?> getComponent(Element eTrain, String language, JointFtrXml[] xmls, StringTrainSpace[] spaces, StringModel[] models, Object[] lexica)
	{
		if (language.equals(AbstractReader.LANG_EN))
			return (models == null) ? new EnglishPOSTagger(xmls, spaces, lexica) : new EnglishPOSTagger(xmls, spaces, models, lexica);
		
		return (models == null) ? new DefaultPOSTagger(xmls, spaces, lexica) : new DefaultPOSTagger(xmls, spaces, models, lexica);
	}

	@Override
	protected StringTrainSpace[] getStringTrainSpaces(JointFtrXml[] xmls, Object[] lexica, int boot)
	{
		return getStringTrainSpaces(xmls);
	}
	
	@Override
	public String getMode()
	{
		return NLPMode.MODE_POS;
	}
	
//	====================================== COLLECT ======================================
	
	protected AbstractPOSTagger getCollector(Element eConfig, JointReader reader, String language, JointFtrXml[] xmls, String[] trainFiles, int devId)
	{
		Set<String> set = getLowerSimplifiedForms(reader, xmls[0], trainFiles, devId);
		
		if (language.equals(AbstractReader.LANG_EN))
			return new EnglishPOSTagger(xmls, set);
		else
			return new DefaultPOSTagger(xmls, set);
	}
	
	/** Called by {@link #getCollector(JointReader, String, JointFtrXml[], String[], int)}. */
	protected Set<String> getLowerSimplifiedForms(JointReader reader, JointFtrXml xml, String[] trainFiles, int devId)
	{
		Set<String> set = new HashSet<String>();
		int i, j, len, size = trainFiles.length;
		Prob1DMap map = new Prob1DMap();
		DEPTree tree;
		
		LOG.info("Collecting word-forms:\n");
		
		for (i=0; i<size; i++)
		{
			if (devId == i)	continue;
			
			reader.open(UTInput.createBufferedFileReader(trainFiles[i]));
			set.clear();
			
			while ((tree = reader.next()) != null)
			{
				NLPProcess.simplifyForms(tree);
				len = tree.size();
				
				for (j=1; j<len; j++)
					set.add(tree.get(j).lowerSimplifiedForm);
			}
			
			reader.close();
			map.addAll(set);
			LOG.debug(".");
		}	LOG.debug("\n");
		
		return map.toSet(xml.getDocumentFrequencyCutoff());
	}
	
	protected Embedding getEmbedding(Element eConfig)
	{
		Element eEmbed = UTXml.getFirstElementByTagName(eConfig, "embedding");
		if (eEmbed == null)	return null;
		
		String path = UTXml.getTrimmedTextContent(eEmbed);
		Embedding embed = null;
		
		try
		{
			ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(path)));
			embed = (Embedding)in.readObject();
			in.close();
		}
		catch (Exception e) {e.printStackTrace();}
		
		return embed; 
	}
}
