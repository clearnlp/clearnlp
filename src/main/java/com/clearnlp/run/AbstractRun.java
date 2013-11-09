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
package com.clearnlp.run;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import com.clearnlp.classification.model.AbstractModel;
import com.clearnlp.classification.train.AbstractTrainSpace;
import com.clearnlp.classification.train.StringTrainSpace;
import com.clearnlp.io.FileExtFilter;
import com.clearnlp.nlp.NLPGetter;
import com.clearnlp.reader.AbstractColumnReader;
import com.clearnlp.reader.AbstractReader;
import com.clearnlp.reader.DAGReader;
import com.clearnlp.reader.DEPReader;
import com.clearnlp.reader.LineReader;
import com.clearnlp.reader.POSReader;
import com.clearnlp.reader.RawReader;
import com.clearnlp.reader.SRLReader;
import com.clearnlp.reader.TOKReader;
import com.clearnlp.segmentation.AbstractSegmenter;
import com.clearnlp.tokenization.AbstractTokenizer;
import com.clearnlp.util.UTXml;
import com.clearnlp.util.pair.Pair;

/**
 * @since 1.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
abstract public class AbstractRun
{
	final public String TAG_READER					= "reader";
	final public String TAG_READER_TYPE				= "type";
	final public String TAG_READER_COLUMN			= "column";
	final public String TAG_READER_COLUMN_INDEX		= "index";
	final public String TAG_READER_COLUMN_FIELD		= "field";
	
	final public String TAG_LEXICA					= "lexica";
	final public String TAG_LEXICA_LEXICON			= "lexicon";
	final public String TAG_LEXICA_LEXICON_TYPE		= "type";
	final public String TAG_LEXICA_LEXICON_LABEL	= "label";
	final public String TAG_LEXICA_LEXICON_CUTOFF	= "cutoff";
	
	final public String TAG_TRAIN					= "train";
	final public String TAG_TRAIN_ALGORITHM			= "algorithm";
	final public String TAG_TRAIN_ALGORITHM_NAME	= "name";
	final public String TAG_TRAIN_THREADS			= "threads";
	
	final public String TAG_LANGUAGE	= "language";
	final public String TAG_DICTIONARY	= "dictionary";
	final public String TAG_POS_MODEL	= "pos_model";
	final public String TAG_DEP_MODEL	= "dep_model";
	final public String TAG_PRED_MODEL	= "pred_model";
	final public String TAG_SRL_MODEL	= "srl_model";
	
	/** Initializes arguments using args4j. */
	protected void initArgs(String[] args)
	{
		CmdLineParser cmd = new CmdLineParser(this);
		
		try
		{
			cmd.parseArgument(args);
		}
		catch (CmdLineException e)
		{
			System.err.println(e.getMessage());
			cmd.printUsage(System.err);
			System.exit(1);
		}
		catch (Exception e) {e.printStackTrace();}
	}
	
	// ============================= getter: language =============================
	
	protected String getLanguage(Element eConfig)
	{
		Element eLanguage = UTXml.getFirstElementByTagName(eConfig, TAG_LANGUAGE);
		return UTXml.getTrimmedTextContent(eLanguage);
	}
	
	// ============================= getter: components =============================
	
	protected AbstractSegmenter getSegmenter(Element eConfig)
	{
		String language = getLanguage(eConfig);
		return NLPGetter.getSegmenter(language, NLPGetter.getTokenizer(language));
	}
	
	protected AbstractTokenizer getTokenizer(Element eConfig)
	{
		String language = getLanguage(eConfig);
		return NLPGetter.getTokenizer(language);
	}
	
	// ============================= getter: readers =============================
	
	protected Pair<AbstractReader<?>,String> getReader(Element element)
	{
		Element eReader = UTXml.getFirstElementByTagName(element, TAG_READER);
		String  type    = UTXml.getTrimmedAttribute(eReader, TAG_READER_TYPE);
		
		if      (type.equals(AbstractReader.TYPE_RAW))
			return new Pair<AbstractReader<?>,String>(new RawReader(), type);
		else if (type.equals(AbstractReader.TYPE_LINE))
			return new Pair<AbstractReader<?>,String>(new LineReader(), type);
		else if (type.equals(AbstractReader.TYPE_TOK))
			return new Pair<AbstractReader<?>,String>(getTOKReader(eReader), type);
		else if (type.equals(AbstractReader.TYPE_POS))
			return new Pair<AbstractReader<?>,String>(getPOSReader(eReader), type);
		else if (type.equals(AbstractReader.TYPE_DEP))
			return new Pair<AbstractReader<?>,String>(getDEPReader(eReader), type);
		else if (type.equals(AbstractReader.TYPE_DAG))
			return new Pair<AbstractReader<?>,String>(getDAGReader(eReader), type);
		else if (type.equals(AbstractReader.TYPE_SRL))
			return new Pair<AbstractReader<?>,String>(getSRLReader(eReader), type);
		
		throw new IllegalArgumentException("Unsupported type: "+type);
	}
	
	/** Called by {@link AbstractRun#getReader(Element)}. */
	private ObjectIntOpenHashMap<String> getFieldMap(Element eReader)
	{
		NodeList list = eReader.getElementsByTagName(TAG_READER_COLUMN);
		int i, index, size = list.getLength();
		Element element;
		String field;
		
		ObjectIntOpenHashMap<String> map = new ObjectIntOpenHashMap<String>();
		
		for (i=0; i<size; i++)
		{
			element = (Element)list.item(i);
			field   = UTXml.getTrimmedAttribute(element, TAG_READER_COLUMN_FIELD);
			index   = Integer.parseInt(element.getAttribute(TAG_READER_COLUMN_INDEX));
			
			map.put(field, index);
		}
		
		return map;
	}
	
	/** Called by {@link AbstractRun#getReader(Element)}. */
	private TOKReader getTOKReader(Element eReader)
	{
		ObjectIntOpenHashMap<String> map = getFieldMap(eReader);
		int iForm = map.get(AbstractColumnReader.FIELD_FORM) - 1;
		
		if (iForm < 0)
		{
			System.err.printf("The '%s' field must be specified in the configuration file.\n", AbstractColumnReader.FIELD_FORM);
			System.exit(1);
		}
		
		return new TOKReader(iForm);
	}
	
	/** Called by {@link AbstractRun#getReader(Element)}. */
	private POSReader getPOSReader(Element eReader)
	{
		ObjectIntOpenHashMap<String> map = getFieldMap(eReader);
		
		int iForm = map.get(AbstractColumnReader.FIELD_FORM) - 1;
		int iPos  = map.get(AbstractColumnReader.FIELD_POS)  - 1;
		
		if (iForm < 0)
		{
			System.err.printf("The '%s' field must be specified in the configuration file.\n", AbstractColumnReader.FIELD_FORM);
			System.exit(1);
		}
		
		return new POSReader(iForm, iPos);
	}
	
	/** Called by {@link AbstractRun#getReader(Element)}. */
	private DEPReader getDEPReader(Element eReader)
	{
		ObjectIntOpenHashMap<String> map = getFieldMap(eReader);
		
		int iId		= map.get(AbstractColumnReader.FIELD_ID)	 - 1;
		int iForm	= map.get(AbstractColumnReader.FIELD_FORM)	 - 1;
		int iLemma	= map.get(AbstractColumnReader.FIELD_LEMMA)	 - 1;
		int iPos	= map.get(AbstractColumnReader.FIELD_POS)	 - 1;
		int iFeats	= map.get(AbstractColumnReader.FIELD_FEATS)	 - 1;
		int iHeadId	= map.get(AbstractColumnReader.FIELD_HEADID) - 1;
		int iDeprel	= map.get(AbstractColumnReader.FIELD_DEPREL) - 1;
		
		if (iId < 0)
		{
			System.err.printf("The '%s' field must be specified in the configuration file.\n", AbstractColumnReader.FIELD_ID);
			System.exit(1);
		}
		else if (iForm < 0)
		{
			System.err.printf("The '%s' field must be specified in the configuration file.\n", AbstractColumnReader.FIELD_FORM);
			System.exit(1);
		}
		else if (iLemma < 0)
		{
			System.err.printf("The '%s' field must be specified in the configuration file.\n", AbstractColumnReader.FIELD_LEMMA);
			System.exit(1);
		}
		else if (iPos < 0)
		{
			System.err.printf("The '%s' field must be specified in the configuration file.\n", AbstractColumnReader.FIELD_POS);
			System.exit(1);
		}
		else if (iFeats < 0)
		{
			System.err.printf("The '%s' field must be specified in the configuration file.\n", AbstractColumnReader.FIELD_FEATS);
			System.exit(1);
		}
		
		return new DEPReader(iId, iForm, iLemma, iPos, iFeats, iHeadId, iDeprel);
	}
	
	/** Called by {@link AbstractRun#getReader(Element)}. */
	private SRLReader getSRLReader(Element eReader)
	{
		ObjectIntOpenHashMap<String> map = getFieldMap(eReader);
		
		int iId		= map.get(AbstractColumnReader.FIELD_ID)	 - 1;
		int iForm	= map.get(AbstractColumnReader.FIELD_FORM)	 - 1;
		int iLemma	= map.get(AbstractColumnReader.FIELD_LEMMA)	 - 1;
		int iPos	= map.get(AbstractColumnReader.FIELD_POS)	 - 1;
		int iFeats	= map.get(AbstractColumnReader.FIELD_FEATS)	 - 1;
		int iHeadId	= map.get(AbstractColumnReader.FIELD_HEADID) - 1;
		int iDeprel	= map.get(AbstractColumnReader.FIELD_DEPREL) - 1;
		int iSheads	= map.get(AbstractColumnReader.FIELD_SHEADS) - 1;
		
		if (iId < 0)
		{
			System.err.printf("The '%s' field must be specified in the configuration file.\n", AbstractColumnReader.FIELD_ID);
			System.exit(1);
		}
		else if (iForm < 0)
		{
			System.err.printf("The '%s' field must be specified in the configuration file.\n", AbstractColumnReader.FIELD_FORM);
			System.exit(1);
		}
		else if (iLemma < 0)
		{
			System.err.printf("The '%s' field must be specified in the configuration file.\n", AbstractColumnReader.FIELD_LEMMA);
			System.exit(1);
		}
		else if (iPos < 0)
		{
			System.err.printf("The '%s' field must be specified in the configuration file.\n", AbstractColumnReader.FIELD_POS);
			System.exit(1);
		}
		else if (iFeats < 0)
		{
			System.err.printf("The '%s' field must be specified in the configuration file.\n", AbstractColumnReader.FIELD_FEATS);
			System.exit(1);
		}
		else if (iHeadId < 0)
		{
			System.err.printf("The '%s' field must be specified in the configuration file.\n", AbstractColumnReader.FIELD_HEADID);
			System.exit(1);
		}
		else if (iDeprel < 0)
		{
			System.err.printf("The '%s' field must be specified in the configuration file.\n", AbstractColumnReader.FIELD_DEPREL);
			System.exit(1);
		}
		
		return new SRLReader(iId, iForm, iLemma, iPos, iFeats, iHeadId, iDeprel, iSheads);
	}
	
	/** Called by {@link AbstractRun#getReader(Element)}. */
	private DAGReader getDAGReader(Element eReader)
	{
		ObjectIntOpenHashMap<String> map = getFieldMap(eReader);
		
		int iId		= map.get(AbstractColumnReader.FIELD_ID)	 - 1;
		int iForm	= map.get(AbstractColumnReader.FIELD_FORM)	 - 1;
		int iLemma	= map.get(AbstractColumnReader.FIELD_LEMMA)	 - 1;
		int iPos	= map.get(AbstractColumnReader.FIELD_POS)	 - 1;
		int iFeats	= map.get(AbstractColumnReader.FIELD_FEATS)	 - 1;
		int iXheads	= map.get(AbstractColumnReader.FIELD_XHEADS) - 1;
		
		if (iId < 0)
		{
			System.err.printf("The '%s' field must be specified in the configuration file.\n", AbstractColumnReader.FIELD_ID);
			System.exit(1);
		}
		else if (iForm < 0)
		{
			System.err.printf("The '%s' field must be specified in the configuration file.\n", AbstractColumnReader.FIELD_FORM);
			System.exit(1);
		}
		else if (iLemma < 0)
		{
			System.err.printf("The '%s' field must be specified in the configuration file.\n", AbstractColumnReader.FIELD_LEMMA);
			System.exit(1);
		}
		else if (iPos < 0)
		{
			System.err.printf("The '%s' field must be specified in the configuration file.\n", AbstractColumnReader.FIELD_POS);
			System.exit(1);
		}
		else if (iFeats < 0)
		{
			System.err.printf("The '%s' field must be specified in the configuration file.\n", AbstractColumnReader.FIELD_FEATS);
			System.exit(1);
		}
		
		return new DAGReader(iId, iForm, iLemma, iPos, iFeats, iXheads);
	}
	
	// ============================= classification =============================
	
	protected StringTrainSpace mergeTrainSpaces(List<StringTrainSpace> spaces, int labelCutoff, int featureCutoff)
	{
		StringTrainSpace space;
		
		if (spaces.size() == 1)
		{
			space = spaces.get(0);
		}
		else
		{
			System.out.println("Merging training instances:");
			space = new StringTrainSpace(false, labelCutoff, featureCutoff);
			
			for (StringTrainSpace s : spaces)
			{
				space.appendSpace(s);
				System.out.print(".");
				s.clear();
			}
			
			System.out.println();			
		}
		
		return space;
	}
	
	protected AbstractModel getModel(Element eTrain, AbstractTrainSpace space, int index)
	{
		NodeList list = eTrain.getElementsByTagName(TAG_TRAIN_ALGORITHM);
		int numThreads = getNumOfThreads(eTrain);
		Element  eAlgorithm;
		String   name;
		
		eAlgorithm = (Element)list.item(index);
		name       = UTXml.getTrimmedAttribute(eAlgorithm, TAG_TRAIN_ALGORITHM_NAME);
		
		if (name.equals("liblinear"))
		{
			byte solver = Byte  .parseByte  (UTXml.getTrimmedAttribute(eAlgorithm, "solver"));
			double cost = Double.parseDouble(UTXml.getTrimmedAttribute(eAlgorithm, "cost"));
			double eps  = Double.parseDouble(UTXml.getTrimmedAttribute(eAlgorithm, "eps"));
			double bias = Double.parseDouble(UTXml.getTrimmedAttribute(eAlgorithm, "bias"));

			return getLiblinearModel(space, numThreads, solver, cost, eps, bias);
		}
		
		return null;
	}
	
	/** Called by {@link AbstractRun#getModel(Element, AbstractTrainSpace, int)}. */
	protected AbstractModel getLiblinearModel(AbstractTrainSpace space, int numThreads, byte solver, double cost, double eps, double bias)
	{
		space.build();
		System.out.println("Liblinear:");
		System.out.printf("- solver=%d, cost=%f, eps=%f, bias=%f\n", solver, cost, eps, bias);
		return LiblinearTrain.getModel(space, numThreads, solver, cost, eps, bias);
	}
	
	protected int getNumOfThreads(Element eTrain)
	{
		Element eThreads = UTXml.getFirstElementByTagName(eTrain, TAG_TRAIN_THREADS); 
		return Integer.parseInt(UTXml.getTrimmedTextContent(eThreads));
	}
	
	protected void printTime(String message, long st, long et)
	{
		long millis = et - st;
		long mins   = TimeUnit.MILLISECONDS.toMinutes(millis);
		long secs   = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(mins);

		System.out.println(message);
		System.out.println(String.format("- %d mins, %d secs", mins, secs));
	}
	
	// ============================= utilities =============================
	
	/** String[0]: input filename, String[1]: output filename. */
	protected List<String[]> getFilenames(String inputPath, String inputExt, String outputExt)
	{
		List<String[]> filenames = new ArrayList<String[]>();
		File f = new File(inputPath);
		String outputFile;
		
		if (f.isDirectory())
		{
			for (String inputFile : f.list(new FileExtFilter(inputExt)))
			{
				inputFile  = inputPath + File.separator + inputFile;
				outputFile = inputFile + "." + outputExt;
				filenames.add(new String[]{inputFile, outputFile});
			}
		}
		else
			filenames.add(new String[]{inputPath, inputPath+"."+outputExt});
		
		return filenames;
	}
}