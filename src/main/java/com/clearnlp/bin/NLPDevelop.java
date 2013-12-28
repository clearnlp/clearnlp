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
package com.clearnlp.bin;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.kohsuke.args4j.Option;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.clearnlp.classification.algorithm.AbstractAlgorithm;
import com.clearnlp.classification.feature.JointFtrXml;
import com.clearnlp.classification.model.StringModelAD;
import com.clearnlp.collection.list.FloatArrayList;
import com.clearnlp.component.evaluation.AbstractEval;
import com.clearnlp.component.online.AbstractOnlineStatisticalComponent;
import com.clearnlp.component.online.OnlinePOSTagger;
import com.clearnlp.component.state.AbstractState;
import com.clearnlp.dependency.DEPTree;
import com.clearnlp.morphology.MPLib;
import com.clearnlp.nlp.NLPMode;
import com.clearnlp.reader.JointReader;
import com.clearnlp.util.UTArgs4j;
import com.clearnlp.util.UTFile;
import com.clearnlp.util.UTInput;
import com.clearnlp.util.UTOutput;
import com.clearnlp.util.UTXml;
import com.clearnlp.util.map.Prob1DMap;
import com.clearnlp.util.pair.ObjectDoublePair;
import com.google.common.collect.Sets;

/**
 * @since 2.0.1
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class NLPDevelop extends AbstractNLP implements NLPMode
{
	protected final String DELIM_FILENAME = ":";
	protected final int    MAX_TREES = 5000;
	
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
	@Option(name="-m", usage="model file (required)", required=true, metaVar="<filename>")
	protected String s_modelFile;
	@Option(name="-t", usage="type (required)", required=true, metaVar="<0|1|2|3>")
	protected int i_type;
	
//	@Option(name="-r", usage="random seed", required=false, metaVar="<integer>")
//	protected int i_randomSeed = 11;
	
	public NLPDevelop(String[] args)
	{
		UTArgs4j.initArgs(this, args);
		
		String[] featureFiles = s_featureFiles.split(DELIM_FILENAME);
		String[] trainFiles   = UTFile.getSortedFileListBySize(s_trainPath, ".*", true);
		String[] developFiles = UTFile.getSortedFileListBySize(s_developPath, ".*", true);
		
		try
		{
			Element    eConfig = UTXml.getDocumentElement(new FileInputStream(s_configFile));
			JointFtrXml[] xmls = getFeatureTemplates(featureFiles);
			
			switch (i_type)
			{
			case 0: develop(xmls, trainFiles, developFiles, eConfig, s_mode, -1); break;
			case 1: train(xmls, trainFiles, s_modelFile, eConfig, s_mode); break;
			case 2: decode(developFiles, s_modelFile, eConfig);
			}
		}
		catch (Exception e) {e.printStackTrace();}
	}
	
	public void decode(String[] inputFiles, String modelFile, Element eConfig) throws Exception
	{
		JointReader reader = getJointReader(UTXml.getFirstElementByTagName(eConfig, TAG_READER));
		AbstractOnlineStatisticalComponent<? extends AbstractState> component = getDecoder(new ObjectInputStream(new BufferedInputStream(new GZIPInputStream(new FileInputStream(modelFile)))));
		process(inputFiles, reader, component, "Decoding:", FLAG_DECODE, -1);
	}
	
	public void train(JointFtrXml[] xmls, String[] trainFiles, String modelFile, Element eConfig, String mode) throws Exception
	{
		JointReader reader = getJointReader(UTXml.getFirstElementByTagName(eConfig, TAG_READER));
		AbstractOnlineStatisticalComponent<? extends AbstractState> component = preBootstrap(xmls, trainFiles, reader, eConfig, mode, -1);
		
		Element eMode = UTXml.getFirstElementByTagName(eConfig, mode);
		NodeList eTrains = eMode.getElementsByTagName(TAG_TRAIN);
		int boot = 0, nBootstraps = getNumberOfBootstraps(eMode);
		
		while (true)
		{
			train(component, eTrains, boot);
			if (boot >= nBootstraps) break;
			
			LOG.info(String.format("===== Bootstrap: %d =====\n", ++boot));
			process(trainFiles, reader, component, "Generating instances:", FLAG_BOOTSTRAP, -1);
		}
		
		for (StringModelAD model : component.getModels())
			model.trimFeatures(LOG, 0f);
		
		component.save(new ObjectOutputStream(new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(modelFile)))));
	}
	
	public void develop(JointFtrXml[] xmls, String[] trainFiles, String[] developFiles, Element eConfig, String mode, int devId) throws Exception
	{
		JointReader reader = getJointReader(UTXml.getFirstElementByTagName(eConfig, TAG_READER));
		AbstractOnlineStatisticalComponent<? extends AbstractState> component = preBootstrap(xmls, trainFiles, reader, eConfig, mode, devId);
		
		Element eMode = UTXml.getFirstElementByTagName(eConfig, mode);
		NodeList eTrains = eMode.getElementsByTagName(TAG_TRAIN);
		double currScore, bestScore = 0;
		int boot = 0;
		
		while (true)
		{
			if (boot == 0)
			{
				develop(trainFiles, reader, component, eTrains, getBootstrapScore(eMode), boot, FLAG_EVALUATE);
			}
			else
			{
				currScore = develop(developFiles, reader, component, eTrains, 0d, boot, FLAG_EVALUATE);
				if (currScore <= bestScore) break;
				bestScore = currScore;
			}
			
			LOG.info(String.format("===== Bootstrap: %d =====\n", ++boot));
			process(trainFiles, reader, component, "Generating instances:", FLAG_BOOTSTRAP, devId);
		}
	}
	
	private AbstractOnlineStatisticalComponent<? extends AbstractState> preBootstrap(JointFtrXml[] xmls, String[] trainFiles, JointReader reader, Element eConfig, String mode, int devId) throws Exception
	{
		AbstractOnlineStatisticalComponent<? extends AbstractState> component;
		Object[] lexica = null;
		
		// collect
		component = getCollector(xmls, trainFiles, reader, eConfig, devId);
		
		if (component != null)
		{
			process(trainFiles, reader, component, "Collecting lexica:", FLAG_COLLECT, devId);
			lexica = component.getLexica();
		}
		
		// train
		component = getTrainer(xmls, lexica);
		process(trainFiles, reader, component, "Generating instances:", FLAG_TRAIN, devId);
		
		return component;
	}
	
// ================================== PROCESS ==================================
	
	protected List<String> process(String[] filenames, JointReader reader, AbstractOnlineStatisticalComponent<? extends AbstractState> component, String message, byte flag, int devId) throws Exception
	{
		List<String> outputs = (flag == FLAG_GENERATE) ? new ArrayList<String>() : null;
		if (message != null) LOG.info(message+"\n");
		int i, total = 0, size = filenames.length;
		StringBuilder build = null;
		PrintStream out = null;
		DEPTree tree;
		String s;
		
		for (i=0; i<size; i++)
		{
			reader.open(UTInput.createBufferedFileReader(filenames[i]));
			
			switch (flag)
			{
			case FLAG_DECODE  : out   = new PrintStream(new BufferedOutputStream(new FileOutputStream(filenames[i]+".cnlp"))); break;
			case FLAG_GENERATE: build = new StringBuilder();
			}
			
			while ((tree = reader.next()) != null)
			{
				component.process(tree, flag);
				s = toString(tree)+"\n\n";
				
				switch (flag)
				{
				case FLAG_DECODE  : out.print(s); break;
				case FLAG_GENERATE: build.append(s);
				}
				
				if (message != null && ++total%MAX_TREES == 0) LOG.info(".");
			}
			
			reader.close();
			
			switch (flag)
			{
			case FLAG_DECODE  : out.close(); break;
			case FLAG_GENERATE: outputs.add(build.toString());
			}
		}
		
		if (message != null) LOG.info("\n");
		return outputs;
	}
	
	protected void train(AbstractOnlineStatisticalComponent<? extends AbstractState> component, NodeList eTrains, int boot)
	{
		StringModelAD[] models = component.getModels();
		int modelSize = models.length;
		AbstractAlgorithm algorithm;
		StringModelAD model;
		int i, nIterations;
		Element eTrain;
		
		for (i=0; i<modelSize; i++)
		{
			eTrain = (Element)eTrains.item(i);
			model = models[i];
			model.build(getLabelCutoff(eTrain), getFeatureCutoff(eTrain), getRandomSeed(eTrain), true);
			model.printInfo(LOG);
			
			nIterations = getNumberOfIterations(eTrain, boot);
			algorithm = getAlgorithm(eTrain);
			trainOnline(model, algorithm, nIterations);
		}
	}
	
	private void trainOnline(StringModelAD model, AbstractAlgorithm algorithm, int nIterations)
	{
		int i;
		
		for (i=0; i<nIterations; i++)
		{
			algorithm.train(model);
			LOG.info(".");
		}
		
		LOG.info("\n");
	}
	
	@SuppressWarnings("unchecked")
	protected double develop(String[] developFiles, JointReader reader, AbstractOnlineStatisticalComponent<? extends AbstractState> component, NodeList eTrains, double bootstrapScore, int boot, byte flag) throws Exception
	{
		StringModelAD[] models = component.getModels();
		ObjectDoublePair<List<String>> output = null;
		int modelSize = models.length;
		AbstractAlgorithm algorithm;
		StringModelAD model;
		Element eTrain;
		int i;
		
		for (i=0; i<modelSize; i++)
		{
			eTrain = (Element)eTrains.item(i);
			model = models[i];
			model.build(getLabelCutoff(eTrain), getFeatureCutoff(eTrain), getRandomSeed(eTrain), true);
			model.printInfo(LOG);
			
			algorithm = getAlgorithm(eTrain);
			output = developOnline(developFiles, reader, component, model, algorithm, bootstrapScore, flag);
		}
		
		if (flag == FLAG_GENERATE)
			printOutput(developFiles, (List<String>)output.o, boot);
		
		return output.d;
	}
	
	protected ObjectDoublePair<List<String>> developOnline(String[] developFiles, JointReader reader, AbstractOnlineStatisticalComponent<? extends AbstractState> component, StringModelAD model, AbstractAlgorithm algorithm, double bootstrapScore, byte flag) throws Exception
	{
		boolean prepareBootstrap = bootstrapScore > 0;
		List<String> currOutput, bestOutput = null;
		FloatArrayList bestWeights = null; 
		double currScore, bestScore = 0;
		AbstractEval eval;
		int iter;
		
		for (iter=1; true; iter++)
		{
			algorithm.train(model);
			currOutput = process(developFiles, reader, component, null, flag, -1);
			
			eval = component.getEval();
			currScore = eval.getAccuracies()[0];
			LOG.info(String.format("%2d: %s\n", iter, eval.toString()));
			eval.clear();
			
			if (bestScore < currScore)
			{
				bestWeights = model.cloneWeights();
				bestScore   = currScore; 
				bestOutput  = currOutput;
			}
			else break;
			
			if (prepareBootstrap && bootstrapScore <= currScore) break;
		}
		
		model.setWeights(bestWeights);
		return new ObjectDoublePair<List<String>>(bestOutput, bestScore);
	}
	
	protected ObjectDoublePair<List<String>> developBatch(String[] developFiles, JointReader reader, AbstractOnlineStatisticalComponent<? extends AbstractState> component, StringModelAD model, AbstractAlgorithm algorithm, byte flag) throws Exception
	{
		algorithm.train(model);
		List<String> output = process(developFiles, reader, component, null, flag, -1);
		
		AbstractEval eval = component.getEval();
		double score = eval.getAccuracies()[0];
		LOG.info(String.format("%s\n", eval.toString()));
		eval.clear();
		
		return new ObjectDoublePair<List<String>>(output, score);
	}
	
	protected void printOutput(String[] developFiles, List<String> output, int boot)
	{
		int i, size = developFiles.length;
		PrintStream fout;
		
		for (i=0; i<size; i++)
		{
			fout = UTOutput.createPrintBufferedFileStream(developFiles[i]+"."+boot);
			fout.print(output.get(i));
			fout.close();
		}
	}
	
	// ================================== SUBCLASS ==================================
	
	protected AbstractOnlineStatisticalComponent<? extends AbstractState> getCollector(JointFtrXml[] xmls, String[] trainFiles, JointReader reader, Element eConfig, int devId)
	{
		Element eMode = UTXml.getFirstElementByTagName(eConfig, MODE_POS);
		int dfc = getDocumentFrequencyCutoff(eMode);
		int dtc = getDocumentMaxTokenCount(eMode);
		Set<String> sLsfs;
		
		if (dtc <= 0)	sLsfs = getLowerSimplifiedFormsByDocumentFrequencies(reader, trainFiles, devId, dfc);
		else			sLsfs = getLowerSimplifiedFormsByDocumentFrequencies(reader, trainFiles, devId, dfc, dtc);
		
		return new OnlinePOSTagger(xmls, sLsfs);
	}
	
	protected AbstractOnlineStatisticalComponent<? extends AbstractState> getTrainer(JointFtrXml[] xmls, Object[] lexica)
	{
		return new OnlinePOSTagger(xmls, lexica);
	}
	
	protected AbstractOnlineStatisticalComponent<? extends AbstractState> getDecoder(ObjectInputStream in)
	{
		return new OnlinePOSTagger(in);
	}
	
	protected String toString(DEPTree tree)
	{
		return tree.toStringPOS();
	}
	
	private Set<String> getLowerSimplifiedFormsByDocumentFrequencies(JointReader reader, String[] filenames, int devId, int cutoff)
	{
		int i, j, len, size = filenames.length;
		Set<String> set = Sets.newHashSet();
		Prob1DMap map = new Prob1DMap();
		DEPTree tree;
		
		LOG.info(String.format("Collecting simplified-forms: cutoff = %d\n", cutoff));
		
		for (i=0; i<size; i++)
		{
			if (i == devId)	continue;
			reader.open(UTInput.createBufferedFileReader(filenames[i]));
			set.clear();
			
			while ((tree = reader.next()) != null)
			{
				len = tree.size();
				
				for (j=1; j<len; j++)
					set.add(MPLib.getSimplifiedLowercaseWordForm(tree.get(j).form));
			}
			
			map.addAll(set);
			reader.close();
			LOG.info(".");
		}	LOG.info("\n");
		
		return map.toSet(cutoff);
	}
	
	private Set<String> getLowerSimplifiedFormsByDocumentFrequencies(JointReader reader, String[] filenames, int devId, int cutoff, int maxCount)
	{
		int i, j, len, count = 0, size = filenames.length;
		Set<String> set = Sets.newHashSet();
		Prob1DMap map = new Prob1DMap();
		DEPTree tree;
		
		LOG.info(String.format("Collecting simplified-forms: cutoff = %d, max = %d\n", cutoff, maxCount));
		
		for (i=0; i<size; i++)
		{
			if (i == devId)	continue;
			reader.open(UTInput.createBufferedFileReader(filenames[i]));
			
			while ((tree = reader.next()) != null)
			{
				len = tree.size();
				
				for (j=1; j<len; j++)
					set.add(MPLib.getSimplifiedLowercaseWordForm(tree.get(j).form));
				
				if ((count += len) >= maxCount)
				{
					map.addAll(set);
					LOG.info(".");
					set.clear();
					count = 0;
				}
			}
			
			reader.close();
		}	LOG.info("\n");
		
		if (!set.isEmpty()) map.addAll(set);
		return map.toSet(cutoff);
	}
	
	static public void main(String[] args)
	{
		new NLPDevelop(args);
	}
}
