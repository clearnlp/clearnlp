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

import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import com.clearnlp.classification.feature.JointFtrXml;
import com.clearnlp.classification.model.StringModel;
import com.clearnlp.classification.train.StringTrainSpace;
import com.clearnlp.component.AbstractStatisticalComponent;
import com.clearnlp.component.role.AbstractRolesetClassifier;
import com.clearnlp.component.role.EnglishRolesetClassifier;
import com.clearnlp.nlp.NLPLib;
import com.clearnlp.reader.JointReader;

/**
 * @since 1.5.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class RoleTrainer extends AbstractNLPTrainer
{
	@Override
	protected AbstractStatisticalComponent<?> getComponent(Element eConfig, JointReader reader, JointFtrXml[] xmls, String[] trainFiles, int devId)
	{
		AbstractRolesetClassifier collector = getCollector(xmls);
		return getTrainedComponent(eConfig, reader, collector, xmls, trainFiles, devId);
	}
	
	@Override
	protected AbstractStatisticalComponent<?> getComponent(Element eTrain, String language, JointFtrXml[] xmls, StringModel[] models, Object[] lexica)
	{
		return new EnglishRolesetClassifier(xmls, models, lexica); 
	}

	@Override
	protected AbstractStatisticalComponent<?> getComponent(Element eTrain, String language, JointFtrXml[] xmls, StringTrainSpace[] spaces, StringModel[] models, Object[] lexica)
	{
		return new EnglishRolesetClassifier(xmls, spaces, lexica);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected StringTrainSpace[] getStringTrainSpaces(JointFtrXml[] xmls, Object[] lexica, int boot)
	{
		return getStringTrainSpaces(xmls[0], ((ObjectIntOpenHashMap<String>)lexica[1]).size());
	}
	
	@Override
	public String getMode()
	{
		return NLPLib.MODE_ROLE;
	}
	
//	====================================== COLLECT ======================================
	
	protected AbstractRolesetClassifier getCollector(JointFtrXml[] xmls)
	{
		return new EnglishRolesetClassifier(xmls);
	}
}
