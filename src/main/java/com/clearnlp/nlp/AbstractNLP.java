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
package com.clearnlp.nlp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import com.clearnlp.dependency.DEPTree;
import com.clearnlp.reader.AbstractColumnReader;
import com.clearnlp.reader.AbstractReader;
import com.clearnlp.reader.JointReader;
import com.clearnlp.reader.LineReader;
import com.clearnlp.reader.RawReader;
import com.clearnlp.util.UTXml;

/**
 * @since 1.3.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
abstract public class AbstractNLP
{
	protected final Logger LOG = LoggerFactory.getLogger(this.getClass());
	
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
	
	final public String TAG_LANGUAGE	= "language";
	final public String TAG_TWIT		= "twit";
	final public String TAG_DICTIONARY 	= "dictionary";
	final public String TAG_MODEL		= "model";
	final public String TAG_FRAMES		= "frames";
	final public String TAG_MODE		= "mode";
	final public String TAG_PATH		= "path";
	
	// ============================= getter: mode =============================
	
	protected String toString(DEPTree tree, String mode)
	{
		switch (mode)
		{
		case NLPLib.MODE_POS  : return tree.toStringMorph();
		case NLPLib.MODE_MORPH: return tree.toStringMorph();
		case NLPLib.MODE_DEP  : return tree.toStringDEP();
		case NLPLib.MODE_PRED : return tree.toStringDEP();
		case NLPLib.MODE_ROLE : return tree.toStringDEP();
		case NLPLib.MODE_SRL  : return tree.toStringSRL();
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
	
	private String getTextContent(Element element, String key)
	{
		Element e = UTXml.getFirstElementByTagName(element, key);
		return (e != null) ? UTXml.getTrimmedTextContent(e) : null;
	}
}