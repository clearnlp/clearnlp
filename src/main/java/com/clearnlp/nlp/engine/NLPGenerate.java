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
import com.clearnlp.nlp.develop.IDeveloper;
import com.clearnlp.util.UTArgs4j;
import com.clearnlp.util.UTFile;
import com.clearnlp.util.UTXml;

/**
 * @since 1.3.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class NLPGenerate extends NLPDevelop
{
	@Option(name="-b", usage="the beginning index (required)", required=true, metaVar="<directory>")
	protected int b_idx = -1;
	@Option(name="-e", usage="the endding index (required)", required=true, metaVar="<directory>")
	protected int e_idx = -1;
	@Option(name="-ie", usage="input file extension (default: .*)", required=false, metaVar="<regex>")
	protected String s_inputExt = ".*";
	
	public NLPGenerate()
	{
		super();
	}
	
	public NLPGenerate(String[] args)
	{
		UTArgs4j.initArgs(this, args);
		
		try
		{
			generate(s_configFile, s_featureFiles.split(DELIM_FILENAME), s_trainDir, s_inputExt, s_mode, b_idx, e_idx);
		}
		catch (Exception e) {e.printStackTrace();}
	}
	
	public void generate(String configFile, String[] featureFiles, String trainDir, String inputExt, String mode, int bIdx, int eIdx) throws Exception
	{
		String[]  trainFiles = UTFile.getSortedFileListBySize(trainDir, inputExt, true), devFiles;
		Element      eConfig = UTXml.getDocumentElement(new FileInputStream(configFile));
		JointFtrXml[]   xmls = getFeatureTemplates(featureFiles);
		IDeveloper generator = getDeveloper(mode);
		int i;
		
		try
		{
			for (i=bIdx; i<eIdx; i++)
			{
				devFiles = new String[]{trainFiles[i]};
				generator.develop(eConfig, xmls, trainFiles, devFiles, mode, true, i);
			}
		}
		catch (Exception e) {e.printStackTrace();}
	}
		
	static public void main(String[] args)
	{
		new NLPGenerate(args);
	}
}
