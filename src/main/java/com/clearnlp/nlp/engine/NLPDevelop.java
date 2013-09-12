/**
* Copyright 2012-2013 University of Massachusetts Amherst
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
import com.clearnlp.nlp.develop.DEPDeveloper;
import com.clearnlp.nlp.develop.IDeveloper;
import com.clearnlp.nlp.develop.POSDeveloper;
import com.clearnlp.nlp.develop.PredDeveloper;
import com.clearnlp.nlp.develop.RoleDeveloper;
import com.clearnlp.nlp.develop.SRLDeveloper;
import com.clearnlp.util.UTFile;
import com.clearnlp.util.UTXml;

/**
 * @since 1.3.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class NLPDevelop extends AbstractNLPTrain
{
	@Option(name="-d", usage="the directory containing development files (required)", required=true, metaVar="<directory>")
	protected String s_devDir;
	
	public NLPDevelop()
	{
		super();
	}
	
	public NLPDevelop(String[] args)
	{
		super(args);
		develop(s_configFile, s_featureFiles.split(DELIM_FILENAME), s_trainDir, s_devDir, s_mode);
	}
	
	public void develop(String configFile, String[] featureFiles, String trainDir, String devDir, String mode)
	{
		try
		{
			Element     eConfig = UTXml.getDocumentElement(new FileInputStream(configFile));
			JointFtrXml[]  xmls = getFeatureTemplates(featureFiles);
			String[] trainFiles = UTFile.getSortedFileListBySize(trainDir, ".*", true);
			String[]   devFiles = UTFile.getSortedFileListBySize(devDir, ".*", true);
			
			IDeveloper developer = getDeveloper(mode);
			developer.develop(eConfig, xmls, trainFiles, devFiles, mode, false, -1);
		}
		catch (Exception e) {e.printStackTrace();}
	}
	
	public IDeveloper getDeveloper(String mode)
	{
		switch (mode)
		{
		case NLPLib.MODE_POS : return new POSDeveloper();
		case NLPLib.MODE_DEP : return new DEPDeveloper();
		case NLPLib.MODE_PRED: return new PredDeveloper();
		case NLPLib.MODE_ROLE: return new RoleDeveloper();
		case NLPLib.MODE_SRL : return new SRLDeveloper();
		}
		
		throw new IllegalArgumentException("The requested mode '"+mode+"' is not supported.");
	}
		
	static public void main(String[] args)
	{
		new NLPDevelop(args);
	}
}
