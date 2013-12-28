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
package com.clearnlp.nlp.engine;

import java.io.FileInputStream;

import org.kohsuke.args4j.Option;
import org.w3c.dom.Element;

import com.clearnlp.classification.feature.JointFtrXml;
import com.clearnlp.nlp.NLPMode;
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
		case NLPMode.MODE_POS : return new POSDeveloper();
		case NLPMode.MODE_DEP : return new DEPDeveloper();
		case NLPMode.MODE_PRED: return new PredDeveloper();
		case NLPMode.MODE_ROLE: return new RoleDeveloper();
		case NLPMode.MODE_SRL : return new SRLDeveloper();
		}
		
		throw new IllegalArgumentException("The requested mode '"+mode+"' is not supported.");
	}
		
	static public void main(String[] args)
	{
		new NLPDevelop(args);
	}
}
