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
package com.clearnlp.nlp;

import java.io.File;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import com.clearnlp.dependency.DEPTree;
import com.clearnlp.reader.AbstractColumnReader;
import com.clearnlp.reader.AbstractReader;
import com.clearnlp.reader.JointReader;
import com.clearnlp.reader.LineReader;
import com.clearnlp.reader.RawReader;
import com.clearnlp.util.UTFile;
import com.clearnlp.util.UTXml;

/**
 * @since 1.3.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
abstract public class AbstractNLP
{
	protected final Logger LOG = Logger.getLogger(this.getClass());
	
	final public String TAG_READER	= "reader";
	final public String TAG_TYPE	= "type";
	final public String TAG_COLUMN	= "column";
	final public String TAG_INDEX	= "index";
	final public String TAG_FIELD	= "field";
	
	final public String TAG_TRAIN		= "train";
	final public String TAG_ALGORITHM	= "algorithm";
	final public String TAG_NAME		= "name";
	final public String TAG_THREADS		= "threads";
	final public String TAG_BOOTSTRAPS	= "bootstraps";
	final public String TAG_BEAMS		= "beams";
	final public String TAG_MARGIN		= "margin";
	final public String TAG_DFC			= "documentFrequencyCutoff";
	final public String TAG_DTC			= "documentTokenCount";
	
	final public String TAG_LANGUAGE	= "language";
	final public String TAG_TWIT		= "twit";
	final public String TAG_DICTIONARY 	= "dictionary";
	final public String TAG_MODEL		= "model";
	final public String TAG_FRAMES		= "frames";
	final public String TAG_MODE		= "mode";
	final public String TAG_PATH		= "path";
	
	// ============================= getter: mode =============================
	
	protected String[] getFilenames(String filePath)
	{
		if (new File(filePath).isDirectory())
			return UTFile.getSortedFileListBySize(filePath, ".*", true);
		
		return new String[]{filePath};
	}
	
	// ============================= getter: mode =============================
	
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
	
	// ============================= getter: readers =============================
	
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
	
	// ============================= XML: train =============================
	
	protected int getNumerOfThreads(Element eTrain)
	{
		return Integer.parseInt(getTextContent(eTrain, TAG_THREADS));
	}
	
	protected int getNumerOfBootstraps(Element eTrain)
	{
		return Integer.parseInt(getTextContent(eTrain, TAG_BOOTSTRAPS));
	}
	
	protected double getMargin(Element eTrain)
	{
		return Double.parseDouble(getTextContent(eTrain, TAG_MARGIN));
	}
	
	protected int getBeamSize(Element eTrain)
	{
		return Integer.parseInt(getTextContent(eTrain, TAG_BEAMS));
	}
	
	// ============================= XML: config =============================
	
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
	
	protected int getDocumentFrequencyCutoff(Element eMode)
	{
		return Integer.parseInt(getTextContent(eMode, TAG_DFC));
	}
	
	protected int getDocumentTokenCount(Element eMode)
	{
		return Integer.parseInt(getTextContent(eMode, TAG_DTC));
	}
	
	private String getTextContent(Element element, String key)
	{
		Element e = UTXml.getFirstElementByTagName(element, key);
		return (e != null) ? UTXml.getTrimmedTextContent(e) : null;
	}
}