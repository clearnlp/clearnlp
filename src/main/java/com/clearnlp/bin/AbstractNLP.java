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
package com.clearnlp.bin;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import com.clearnlp.classification.algorithm.AbstractAlgorithm;
import com.clearnlp.classification.algorithm.AdaGradOnlineHingeLoss;
import com.clearnlp.classification.algorithm.AdaGradOnlineLogisticRegression;
import com.clearnlp.classification.algorithm.LiblinearHingeLoss;
import com.clearnlp.classification.feature.JointFtrXml;
import com.clearnlp.component.online.IFlag;
import com.clearnlp.dependency.DEPTree;
import com.clearnlp.nlp.NLPMode;
import com.clearnlp.pattern.PTLib;
import com.clearnlp.pattern.PTNumber;
import com.clearnlp.reader.AbstractColumnReader;
import com.clearnlp.reader.AbstractReader;
import com.clearnlp.reader.JointReader;
import com.clearnlp.reader.LineReader;
import com.clearnlp.reader.RawReader;
import com.clearnlp.util.UTFile;
import com.clearnlp.util.UTInput;
import com.clearnlp.util.UTXml;
import com.google.common.collect.Lists;

/**
 * @since 2.0.2
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
abstract public class AbstractNLP implements IFlag
{
	protected final Logger LOG = Logger.getLogger(this.getClass());
	
	// reader
	final public String TAG_READER	= "reader";
	final public String TAG_COLUMN	= "column";
	final public String TAG_TYPE	= "type";
	final public String TAG_INDEX	= "index";
	final public String TAG_FIELD	= "field";
	
	// train
	final public String TAG_FEATURE_CUTOFF	= "featureCutoff";
	final public String TAG_LABEL_CUTOFF 	= "labelCutoff";
	final public String TAG_RANDOM_SEED		= "randomSeed";
	final public String TAG_BOOTSTRAPS		= "bootstraps";
	final public String TAG_ITERATIONS		= "iterations";
	final public String TAG_BOOTSTRAP_SCORE	= "bootstrapScore";
	final public String TAG_MARGIN			= "margin";
	final public String TAG_BEAMS			= "beams";
	final public String TAG_TRAIN			= "train";
	
	final public String TAG_DOCUMENT_FREQUENCY_CUTOFF = "documentFrequencyCutoff";
	final public String TAG_DOCUMENT_TOKEN_COUNT = "documentTokenCount";
	
	// algorithm
	final public String TAG_ALGORITHM	= "algorithm";
	final public String TAG_NAME		= "name";
	
	final public String TAG_LANGUAGE	= "language";
	final public String TAG_TWIT		= "twit";
	final public String TAG_DICTIONARY 	= "dictionary";
	final public String TAG_MODEL		= "model";
	final public String TAG_FRAMES		= "frames";
	final public String TAG_PATH		= "path";
	
// ================================== INPUT ==================================
	
	protected String[] getFilenames(String filePath)
	{
		if (new File(filePath).isDirectory())
			return UTFile.getSortedFileListBySize(filePath, ".*", true);
		
		return new String[]{filePath};
	}
	
	protected JointFtrXml[] getFeatureTemplates(String[] featureFiles) throws Exception
	{
		int i, size = featureFiles.length;
		JointFtrXml[] xmls = new JointFtrXml[size];
		
		for (i=0; i<size; i++)
			xmls[i] = new JointFtrXml(new FileInputStream(featureFiles[i]));
		
		return xmls;
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
	
// ================================== MODE ==================================
	
	protected String toString(DEPTree tree, String mode)
	{
		switch (mode)
		{
		case NLPMode.MODE_POS  : return tree.toStringMorph();
		case NLPMode.MODE_MORPH: return tree.toStringMorph();
		case NLPMode.MODE_DEP  : return tree.toStringDEP();
		case NLPMode.MODE_PRED : return tree.toStringDEP();
		case NLPMode.MODE_ROLE : return tree.toStringDEP();
		case NLPMode.MODE_SRL  : return tree.toStringSRL();
		}
		
		throw new IllegalArgumentException("The requested mode '"+mode+"' is not supported.");
	}
	
// ================================== READER ==================================
	
	protected AbstractReader<?> getReader(Element eReader)
	{
		String type = UTXml.getTrimmedAttribute(eReader, TAG_TYPE);
		
		if      (type.equals(AbstractReader.TYPE_RAW))
			return new RawReader();
		else if (type.equals(AbstractReader.TYPE_LINE))
			return new LineReader();
		else
			return getJointReader(eReader);
	}
	
	protected JointReader getJointReader(Element eReader)
	{
		ObjectIntOpenHashMap<String> map = getFieldMap(eReader);
		
		int iId		= map.get(AbstractColumnReader.FIELD_ID)	 - 1;
		int iForm	= map.get(AbstractColumnReader.FIELD_FORM)	 - 1;
		int iLemma	= map.get(AbstractColumnReader.FIELD_LEMMA)	 - 1;
		int iPos	= map.get(AbstractColumnReader.FIELD_POS)	 - 1;
		int iFeats	= map.get(AbstractColumnReader.FIELD_FEATS)	 - 1;
		int iHeadId	= map.get(AbstractColumnReader.FIELD_HEADID) - 1;
		int iDeprel	= map.get(AbstractColumnReader.FIELD_DEPREL) - 1;
		int iXHeads = map.get(AbstractColumnReader.FIELD_XHEADS) - 1;
		int iSHeads = map.get(AbstractColumnReader.FIELD_SHEADS) - 1;
		int iNament = map.get(AbstractColumnReader.FIELD_NAMENT) - 1;
		int iCoref  = map.get(AbstractColumnReader.FIELD_COREF)  - 1;
		
		JointReader reader = new JointReader(iId, iForm, iLemma, iPos, iFeats, iHeadId, iDeprel, iXHeads, iSHeads, iNament, iCoref);
		reader.initGoldPOSTag(map.get(AbstractColumnReader.FIELD_GPOS) - 1);
		
		return reader;
	}
	
	/** Called by {@link AbstractNLP#getCDEPReader(Element, String)}. */
	private ObjectIntOpenHashMap<String> getFieldMap(Element eReader)
	{
		NodeList list = eReader.getElementsByTagName(TAG_COLUMN);
		int i, index, size = list.getLength();
		Element element;
		String field;
		
		ObjectIntOpenHashMap<String> map = new ObjectIntOpenHashMap<String>();
		
		for (i=0; i<size; i++)
		{
			element = (Element)list.item(i);
			field   = UTXml.getTrimmedAttribute(element, TAG_FIELD);
			index   = Integer.parseInt(element.getAttribute(TAG_INDEX));
			
			map.put(field, index);
		}
		
		return map;
	}
	
// ================================== ALGORITHM ==================================
	
	protected AbstractAlgorithm getAlgorithm(Element eTrain)
	{
		Element eAlgorithm = UTXml.getFirstElementByTagName(eTrain, TAG_ALGORITHM);
		String  name       = UTXml.getTrimmedAttribute(eAlgorithm, TAG_NAME);
		
		if (name.equals("adagrad"))
		{
			String  type    = UTXml.getTrimmedAttribute(eAlgorithm, "type");
			double  alpha   = Double.parseDouble(UTXml.getTrimmedAttribute(eAlgorithm, "alpha"));
			double  rho     = Double.parseDouble(UTXml.getTrimmedAttribute(eAlgorithm, "rho"));
			boolean average = UTXml.getTrimmedAttribute(eAlgorithm, "average").equalsIgnoreCase("true");
			
			LOG.info(String.format("AdaGrad: type=%s, alpha=%5.2f, rho=%5.2f, average=%b\n", type, alpha, rho, average));
			
			switch (type)
			{
			case "hinge"     : return new AdaGradOnlineHingeLoss(alpha, rho, average);
			case "regression": return new AdaGradOnlineLogisticRegression(alpha, rho, average);
			default          : throw new IllegalArgumentException("Unknown solver type: "+type);
			}
		}
		else if (name.equals("liblinear"))
		{
			String  type = UTXml.getTrimmedAttribute(eAlgorithm, "type");
			double  cost = Double.parseDouble(UTXml.getTrimmedAttribute(eAlgorithm, "cost"));
			double  eps  = Double.parseDouble(UTXml.getTrimmedAttribute(eAlgorithm, "eps"));
			
			switch (type)
			{
			case "hinge"   : return new LiblinearHingeLoss(cost, eps);
//			case "logistic": return ;
			default        : throw new IllegalArgumentException("Unknown solver type: "+type);
			}
		}

		return null;
	}
	
// ================================== XML ==================================

	protected int getLabelCutoff(Element eTrain)
	{
		return getIntegerContent(eTrain, TAG_LABEL_CUTOFF);
	}
	
	protected int getFeatureCutoff(Element eTrain)
	{
		return Integer.parseInt(getTextContent(eTrain, TAG_FEATURE_CUTOFF));
	}
	
	protected int getRandomSeed(Element eTrain)
	{
		return getIntegerContent(eTrain, TAG_RANDOM_SEED);
	}
	
	protected int getDocumentFrequencyCutoff(Element eTrain)
	{
		return getIntegerContent(eTrain, TAG_DOCUMENT_FREQUENCY_CUTOFF);
	}
	
	protected int getDocumentMaxTokenCount(Element eTrain)
	{
		return getIntegerContent(eTrain, TAG_DOCUMENT_TOKEN_COUNT);
	}
	
	protected int getNumberOfIterations(Element eTrain, int boot)
	{
		String[] tmp = PTLib.splitCommas(getTextContent(eTrain, TAG_ITERATIONS));
		return Integer.parseInt(tmp[boot]);
	}
	
	protected double getBootstrapScore(Element eMode)
	{
		return Double.parseDouble(getTextContent(eMode, TAG_BOOTSTRAP_SCORE)); 
	}
	
	protected int getNumberOfBootstraps(Element eMode)
	{
		return getIntegerContent(eMode, TAG_BOOTSTRAPS);
	}
	
	protected double getMargin(Element eTrain)
	{
		return Double.parseDouble(getTextContent(eTrain, TAG_MARGIN));
	}
	
	protected int getBeamSize(Element eMode)
	{
		return getIntegerContent(eMode, TAG_BEAMS);
	}
	
	protected String getLanguage(Element eConfig)
	{
		return getTextContent(eConfig, TAG_LANGUAGE);
	}
	
	protected String getFrameDirectory(Element eConfig)
	{
		return getTextContent(eConfig, TAG_FRAMES);
	}
	
	protected String getModelFilename(Element eConfig)
	{
		return getTextContent(eConfig, TAG_MODEL);
	}
	
	protected boolean isTwit(Element eConfig)
	{
		return Boolean.parseBoolean(getTextContent(eConfig, TAG_TWIT));
	}
	
	private String getTextContent(Element element, String key)
	{
		Element e = UTXml.getFirstElementByTagName(element, key);
		return (e != null) ? UTXml.getTrimmedTextContent(e) : null;
	}
	
	private int getIntegerContent(Element element, String key)
	{
		String text = getTextContent(element, key);
		return (text != null && PTNumber.containsOnlyDigits(text)) ? Integer.parseInt(text) : 0;
	}
}