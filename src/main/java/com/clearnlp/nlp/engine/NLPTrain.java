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
package com.clearnlp.nlp.engine;

import java.io.FileInputStream;

import org.kohsuke.args4j.Option;
import org.w3c.dom.Element;

import com.clearnlp.classification.feature.JointFtrXml;
import com.clearnlp.nlp.NLPLib;
import com.clearnlp.nlp.train.AbstractNLPTrainer;
import com.clearnlp.nlp.train.DEPTrainer;
import com.clearnlp.nlp.train.POSTrainer;
import com.clearnlp.nlp.train.PredTrainer;
import com.clearnlp.nlp.train.RoleTrainer;
import com.clearnlp.nlp.train.SRLTrainer;
import com.clearnlp.util.UTFile;
import com.clearnlp.util.UTXml;

/**
 * @since 2.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class NLPTrain extends AbstractNLPTrain
{
	@Option(name="-m", usage="model directory (output; required)", required=true, metaVar="<dirpath>")
	protected String s_modelDir;
	
	public NLPTrain()
	{
		super();
	}
	
	public NLPTrain(String[] args)
	{
		super(args);
		train(s_configFile, s_featureFiles.split(DELIM_FILENAME), s_trainDir, s_modelDir);
	}
	
	public void train(String configFile, String[] featureFiles, String trainDir, String modelDir)
	{
		AbstractNLPTrainer trainer = getTrainer(s_mode);
		String[] trainFiles = UTFile.getSortedFileListBySize(trainDir, ".*", true);
		
		try
		{
			Element eConfig = UTXml.getDocumentElement(new FileInputStream(configFile));
			JointFtrXml[] xmls = getFeatureTemplates(featureFiles);
			trainer.train(eConfig, xmls, trainFiles, modelDir);
		}
		catch (Exception e) {e.printStackTrace();}		
	}

	public AbstractNLPTrainer getTrainer(String mode)
	{
		switch (mode)
		{
		case NLPLib.MODE_POS : return new POSTrainer();
		case NLPLib.MODE_DEP : return new DEPTrainer();
		case NLPLib.MODE_PRED: return new PredTrainer();
		case NLPLib.MODE_ROLE: return new RoleTrainer();
		case NLPLib.MODE_SRL : return new SRLTrainer();
		}

		throw new IllegalArgumentException("The requested mode '"+mode+"' is not supported.");
	}
	
	static public void main(String[] args)
	{
		new NLPTrain(args);
	}
}
