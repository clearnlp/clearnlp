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

import org.w3c.dom.Element;

import com.clearnlp.classification.feature.JointFtrXml;
import com.clearnlp.classification.model.StringModel;
import com.clearnlp.classification.train.StringTrainSpace;
import com.clearnlp.component.AbstractStatisticalComponent;
import com.clearnlp.component.srl.AbstractSRLabeler;
import com.clearnlp.component.srl.DefaultSRLabeler;
import com.clearnlp.component.srl.EnglishSRLabeler;
import com.clearnlp.nlp.NLPLib;
import com.clearnlp.propbank.frameset.AbstractFrames;
import com.clearnlp.propbank.frameset.MultiFrames;
import com.clearnlp.reader.AbstractReader;
import com.clearnlp.reader.JointReader;

/**
 * @since 2.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class SRLTrainer extends AbstractNLPTrainer
{
	@Override
	protected AbstractStatisticalComponent<?> getComponent(Element eConfig, JointReader reader, JointFtrXml[] xmls, String[] trainFiles, int devId)
	{
		AbstractSRLabeler collector = getCollector(xmls, getLanguage(eConfig), getFrameDirectory(eConfig));
		return getTrainedComponentBoot(eConfig, reader, collector, xmls, trainFiles, devId);
	}
	
	@Override
	protected AbstractStatisticalComponent<?> getComponent(Element eTrain, String language, JointFtrXml[] xmls, StringModel[] models, Object[] lexica)
	{
		if (language.equals(AbstractReader.LANG_EN))
			return new EnglishSRLabeler(xmls, models, lexica);
		
		return new DefaultSRLabeler(xmls, models, lexica);
	}

	@Override
	protected AbstractStatisticalComponent<?> getComponent(Element eTrain, String language, JointFtrXml[] xmls, StringTrainSpace[] spaces, StringModel[] models, Object[] lexica)
	{
		if (language.equals(AbstractReader.LANG_EN))
			return (models == null) ? new EnglishSRLabeler(xmls, spaces, lexica) : new EnglishSRLabeler(xmls, spaces, models, lexica);
		
		return (models == null) ? new DefaultSRLabeler(xmls, spaces, lexica) : new DefaultSRLabeler(xmls, spaces, models, lexica);
	}

	@Override
	protected StringTrainSpace[] getStringTrainSpaces(JointFtrXml[] xmls, Object[] lexica, int boot)
	{
		return getStringTrainSpaces(xmls[0], 2);
	}
	
	@Override
	public String getMode()
	{
		return NLPLib.MODE_SRL;
	}
	
//	====================================== COLLECT ======================================
	
	protected AbstractSRLabeler getCollector(JointFtrXml[] xmls, String language, String frameDir)
	{
		if (language.equals(AbstractReader.LANG_EN))
			return new EnglishSRLabeler(xmls, getFrames(frameDir));
		else
			return new DefaultSRLabeler(xmls, getFrames(frameDir));
	}

	protected AbstractFrames getFrames(String frameDir)
	{
		return (frameDir != null) ? new MultiFrames(frameDir) : null;
	}
}
