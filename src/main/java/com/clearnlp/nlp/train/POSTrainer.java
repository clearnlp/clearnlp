/**
* Copyright 2013 IPSoft Inc.
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*   http://www.apache.org/licenses/LICENSE-2.0
*   
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.clearnlp.nlp.train;

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
import com.clearnlp.nlp.NLPLib;
import com.clearnlp.nlp.NLPProcess;
import com.clearnlp.reader.AbstractReader;
import com.clearnlp.reader.JointReader;
import com.clearnlp.util.UTInput;
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
		AbstractPOSTagger collector = getCollector(reader, getLanguage(eConfig), xmls, trainFiles, devId);
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
		return NLPLib.MODE_POS;
	}
	
//	====================================== COLLECT ======================================
	
	protected AbstractPOSTagger getCollector(JointReader reader, String language, JointFtrXml[] xmls, String[] trainFiles, int devId)
	{
		if (language.equals(AbstractReader.LANG_EN))
			return new EnglishPOSTagger(xmls, getLowerSimplifiedForms(reader, xmls[0], trainFiles, devId));
		else
			return new DefaultPOSTagger(xmls, getLowerSimplifiedForms(reader, xmls[0], trainFiles, devId));
	}
	
	/** Called by {@link #getCollector(JointReader, String, JointFtrXml[], String[], int)}. */
	private Set<String> getLowerSimplifiedForms(JointReader reader, JointFtrXml xml, String[] trainFiles, int devId)
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
}
