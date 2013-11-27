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
 * Copyright 2012/09-2013/04, University of Massachusetts Amherst
 * Copyright 2013/05-Present, IPSoft Inc.
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
package com.clearnlp.nlp;

import java.io.FileInputStream;
import java.util.List;
import java.util.Set;

import org.kohsuke.args4j.Option;
import org.w3c.dom.Element;

import com.clearnlp.classification.algorithm.online.IOnlineAlgorithm;
import com.clearnlp.classification.algorithm.online.OnlineAdaGradHinge;
import com.clearnlp.classification.feature.JointFtrXml;
import com.clearnlp.component.tagger.EnglishOnlinePOSTagger;
import com.clearnlp.dependency.DEPTree;
import com.clearnlp.morphology.MPLib;
import com.clearnlp.reader.JointReader;
import com.clearnlp.util.UTArgs4j;
import com.clearnlp.util.UTFile;
import com.clearnlp.util.UTInput;
import com.clearnlp.util.UTXml;
import com.clearnlp.util.map.Prob1DMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * @since 2.0.1
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class NLPDevelop extends AbstractNLP
{
	protected final String DELIM_FILENAME = ":";
	
	@Option(name="-c", usage="configuration file (required)", required=true, metaVar="<filename>")
	protected String s_configFile;
	@Option(name="-f", usage="feature template files delimited by '"+DELIM_FILENAME+"' (required)", required=true, metaVar="<filename>")
	protected String s_featureFiles;
	@Option(name="-i", usage="training file or directory containing training files (required)", required=true, metaVar="<directory>")
	protected String s_trainPath;
	@Option(name="-z", usage="mode (pos|dep|pred|role|srl)", required=true, metaVar="<string>")
	protected String s_mode;
	
	@Option(name="-d", usage="development file or directory containing development files (required)", required=true, metaVar="<directory>")
	protected String s_developPath;
	
	public NLPDevelop(String[] args)
	{
		UTArgs4j.initArgs(this, args);
		
		String[] featureFiles = s_featureFiles.split(DELIM_FILENAME);
		String[] trainFiles   = UTFile.getSortedFileListBySize(s_trainPath, ".*", true);
		String[] developFiles = UTFile.getSortedFileListBySize(s_developPath, ".*", true);
		
		develop(s_configFile, featureFiles, trainFiles, developFiles, s_mode);
	}
	
	public void develop(String configFile, String[] featureFiles, String[] trainFiles, String[] developFiles, String mode)
	{
		try
		{
			Element    eConfig = UTXml.getDocumentElement(new FileInputStream(configFile));
			JointFtrXml[] xmls = getFeatureTemplates(featureFiles);
			develop(eConfig, xmls, trainFiles, developFiles, mode, false, -1);
		}
		catch (Exception e) {e.printStackTrace();}
	}
	
	public void develop(Element eConfig, JointFtrXml[] xmls, String[] trainFiles, String[] devFiles, String mode, boolean generate, int devId) throws Exception
	{
		JointReader reader = getJointReader(UTXml.getFirstElementByTagName(eConfig, TAG_READER));
		Element eTrain = UTXml.getFirstElementByTagName(eConfig, mode);
		int dfc = getDocumentFrequencyCutoff(eTrain);
		int dtc = getDocumentTokenCount(eTrain);
		DEPTree tree;
		
		Set<String> sLsfs = getSimplifiedFormsByDocumentFrequencies(reader, trainFiles, dfc, dtc);
		EnglishOnlinePOSTagger component = new EnglishOnlinePOSTagger(xmls, sLsfs);
		
		LOG.info("Collecting lexica:");
		int total = 0;
		
		for (String trainFile : trainFiles)
		{
			reader.open(UTInput.createBufferedFileReader(trainFile));
			
			while ((tree = reader.next()) != null)
			{
				component.collect(tree);
				if (++total%5000 == 0) LOG.info(".");
			}
			
			reader.close();
		}	LOG.info("\n");

		LOG.info("Trainig:");
		
		Object[] lexica = component.getLexica();
		component = new EnglishOnlinePOSTagger(xmls, lexica);
		
		for (String trainFile : trainFiles)
		{
			reader.open(UTInput.createBufferedFileReader(trainFile));
			
			while ((tree = reader.next()) != null)
			{
				component.train(tree);
				if (++total%5000 == 0) LOG.info(".");
			}
			
			reader.close();
		}
		
		IOnlineAlgorithm algorithm = new OnlineAdaGradHinge(0.01, 0.1);
		List<DEPTree> devTrees = getTrees(reader, devFiles);
		
		component.develop(LOG, algorithm, 5, devTrees);
		
		LOG.info("Bootsrapping:");
		
		for (String trainFile : trainFiles)
		{
			reader.open(UTInput.createBufferedFileReader(trainFile));
			
			while ((tree = reader.next()) != null)
			{
				component.bootstrap(tree);
				if (++total%5000 == 0) LOG.info(".");
			}
			
			reader.close();
		}
		
		component.develop(LOG, algorithm, 5, devTrees);
	}

	protected JointFtrXml[] getFeatureTemplates(String[] featureFiles) throws Exception
	{
		int i, size = featureFiles.length;
		JointFtrXml[] xmls = new JointFtrXml[size];
		
		for (i=0; i<size; i++)
			xmls[i] = new JointFtrXml(new FileInputStream(featureFiles[i]));
		
		return xmls;
	}

	protected Set<String> getSimplifiedFormsByDocumentFrequencies(JointReader reader, String[] filenames, int cutoff, int maxCount)
	{
		Set<String> set = Sets.newHashSet();
		Prob1DMap map = new Prob1DMap();
		int j, len, count = 0;
		DEPTree tree; 
		
		for (String filename : filenames)
		{
			reader.open(UTInput.createBufferedFileReader(filename));
			
			while ((tree = reader.next()) != null)
			{
				len = tree.size();
				
				for (j=1; j<len; j++)
					set.add(MPLib.getSimplifiedLowercaseWordForm(tree.get(j).form));
				
				if ((count += len) >= maxCount)
				{
					map.addAll(set);
					count = 0;
					set = Sets.newHashSet();
				}
			}
			
			reader.close();
		}
		
		if (!set.isEmpty()) map.addAll(set);
		return map.toSet(cutoff);
	}
	
	protected List<DEPTree> getTrees(JointReader reader, String[] filenames)
	{
		List<DEPTree> trees = Lists.newArrayList();
		DEPTree tree;
		
		for (String filename : filenames)
		{
			reader.open(UTInput.createBufferedFileReader(filename));
			
			while ((tree = reader.next()) != null)
				trees.add(tree);
			
			reader.close();
		}
		
		return trees;
	}
	
	protected int getDocumentFrequencyCutoff(Element eTrain)
	{
		Element element = UTXml.getFirstElementByTagName(eTrain, TAG_DFC);
		
		try
		{
			return Integer.parseInt(UTXml.getTrimmedTextContent(element));
		}
		catch (NumberFormatException e)
		{
			return 0;
		}
	}
	
	protected int getDocumentTokenCount(Element eTrain)
	{
		Element element = UTXml.getFirstElementByTagName(eTrain, TAG_DTC);
		
		try
		{
			return Integer.parseInt(UTXml.getTrimmedTextContent(element));
		}
		catch (NumberFormatException e)
		{
			return 5000;
		}
	}
	
	static public void main(String[] args)
	{
		new NLPDevelop(args);
	}
}
