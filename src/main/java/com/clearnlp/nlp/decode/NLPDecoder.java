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
package com.clearnlp.nlp.decode;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.zip.ZipFile;

import org.w3c.dom.Element;

import com.clearnlp.component.AbstractComponent;
import com.clearnlp.constant.universal.UNConstant;
import com.clearnlp.dependency.DEPTree;
import com.clearnlp.nlp.AbstractNLP;
import com.clearnlp.nlp.NLPGetter;
import com.clearnlp.reader.AbstractReader;
import com.clearnlp.reader.JointReader;
import com.clearnlp.reader.LineReader;
import com.clearnlp.segmentation.AbstractSegmenter;
import com.clearnlp.tokenization.AbstractTokenizer;
import com.clearnlp.util.UTInput;
import com.clearnlp.util.UTOutput;
import com.clearnlp.util.UTXml;

/**
 * @since 1.3.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
abstract public class NLPDecoder extends AbstractNLP
{
	public void decode(Element eConfig, List<String[]> filenames) throws Exception
	{
		AbstractReader<?> reader = getReader(UTXml.getFirstElementByTagName(eConfig, TAG_READER));
		String modelFile = getModelFilename(eConfig);
		String language = getLanguage(eConfig);
		String readerType = reader.getType();
		boolean bTwit = isTwit(eConfig);
		PrintStream fout;
		
		AbstractSegmenter segmenter = readerType.equals(AbstractReader.TYPE_RAW)  ? getSegmenter(eConfig, bTwit) : null;
		AbstractTokenizer tokenizer = readerType.equals(AbstractReader.TYPE_LINE) ? getTokenizer(eConfig, bTwit) : null;
		AbstractComponent[] components = null;
		
		if (modelFile != null && !modelFile.equals(UNConstant.EMPTY))
		{
			if (new File(modelFile).isFile())
				components = NLPGetter.getComponents(new ZipFile(modelFile), language, getModes(readerType));
			else
				components = NLPGetter.getComponents(modelFile, language, getModes(readerType));			
		}
		else
			new IllegalArgumentException("Model must be specified");
		
		LOG.info("Decoding:\n");
		
		for (String[] filename : filenames)
		{
			reader.open(UTInput.createBufferedFileReader(filename[0]));
			fout = UTOutput.createPrintBufferedFileStream(filename[1]);
			LOG.info(filename[0]+"\n");
			
			decode(reader, fout, segmenter, tokenizer, components);
			reader.close(); fout.close();
		}
	}
	
	abstract protected List<String> getModes(String readerType);
	abstract public String getMode();
	
//	===================================== DECODE ===================================== 
	
	public void decode(AbstractReader<?> reader, PrintStream fout, AbstractSegmenter segmenter, AbstractTokenizer tokenizer, AbstractComponent[] components) throws IOException
	{
		     if (segmenter != null)
			decode(reader.getBufferedReader(), fout, segmenter, components);
		else if (tokenizer != null)
			decode((LineReader)reader, fout, tokenizer, components);
		else
			decode((JointReader)reader, fout, components);
	}
	
	public void decode(BufferedReader reader, PrintStream fout, AbstractSegmenter segmenter, AbstractComponent[] components) throws IOException
	{
		String mode = getMode();
		DEPTree tree;
		
		for (List<String> tokens : segmenter.getSentences(reader))
		{
			tree = NLPGetter.toDEPTree(tokens);
			
			for (AbstractComponent component : components)
				component.process(tree);
			
			fout.println(toString(tree, mode)+"\n");
		}
	}
	
	public void decode(LineReader reader, PrintStream fout, AbstractTokenizer tokenizer, AbstractComponent[] components)
	{
		String sentence, mode = getMode();
		DEPTree tree;

		while ((sentence = reader.next()) != null)
		{
			if (sentence.trim().equals(UNConstant.EMPTY)) continue;
			tree = NLPGetter.toDEPTree(tokenizer.getTokens(sentence));
			
			for (AbstractComponent component : components)
				component.process(tree);
			
			fout.println(toString(tree, mode)+"\n");
		}
	}
	
	public void decode(JointReader reader, PrintStream fout, AbstractComponent[] components)
	{
		String mode = getMode();
		DEPTree tree;
		
		while ((tree = reader.next()) != null)
		{
			for (AbstractComponent component : components)
				component.process(tree);
			
			fout.println(toString(tree, mode)+"\n");
		}
	}
	
//	===================================== COMPONENT GETTERS =====================================

	protected AbstractSegmenter getSegmenter(Element eConfig, boolean twit) throws IOException
	{
		AbstractTokenizer tokenizer = getTokenizer(eConfig, twit);
		String language = getLanguage(eConfig);
		
		return NLPGetter.getSegmenter(language, tokenizer);
	}
	
	protected AbstractTokenizer getTokenizer(Element eConfig, boolean twit) throws IOException
	{
		AbstractTokenizer tokenizer = NLPGetter.getTokenizer(getLanguage(eConfig));
		tokenizer.setTwit(twit);
		
		return tokenizer;
	}
	
	protected boolean isRawLineTok(String readerType)
	{
		return readerType.equals(AbstractReader.TYPE_RAW) || readerType.equals(AbstractReader.TYPE_LINE) || readerType.equals(AbstractReader.TYPE_TOK);
	}
}
