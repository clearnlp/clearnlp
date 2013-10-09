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
package com.clearnlp.nlp.develop;

import org.w3c.dom.Element;

import com.clearnlp.classification.feature.JointFtrXml;
import com.clearnlp.nlp.train.PredTrainer;
import com.clearnlp.reader.JointReader;
import com.clearnlp.util.UTXml;

/**
 * @since 2.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class PredDeveloper extends PredTrainer implements IDeveloper
{
	@Override
	public void develop(Element eConfig, JointFtrXml[] xmls, String[] trainFiles, String[] devFiles, String mode, boolean generate, int devId) throws Exception
	{
		JointReader reader = getJointReader(UTXml.getFirstElementByTagName(eConfig, TAG_READER));
		developComponent(eConfig, reader, xmls, trainFiles, devFiles, null, generate, devId);
	}
}
