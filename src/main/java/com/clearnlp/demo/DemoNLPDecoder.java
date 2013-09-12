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
package com.clearnlp.demo;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.List;
import java.util.zip.ZipFile;

import com.clearnlp.component.AbstractComponent;
import com.clearnlp.dependency.DEPTree;
import com.clearnlp.nlp.NLPGetter;
import com.clearnlp.nlp.NLPLib;
import com.clearnlp.reader.AbstractReader;
import com.clearnlp.segmentation.AbstractSegmenter;
import com.clearnlp.tokenization.AbstractTokenizer;
import com.clearnlp.util.UTInput;
import com.clearnlp.util.UTOutput;

/**
 * @since 1.1.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class DemoNLPDecoder
{
	final String language = AbstractReader.LANG_EN;
	
	public DemoNLPDecoder(String modelFile, String inputFile, String outputFile) throws Exception
	{
		ZipFile file = new ZipFile(modelFile);
		
		AbstractTokenizer tokenizer  = NLPGetter.getTokenizer(language);
		AbstractComponent tagger     = NLPGetter.getComponent(file, language, NLPLib.MODE_POS);
		AbstractComponent analyzer   = NLPGetter.getComponent(file, language, NLPLib.MODE_MORPH);
		AbstractComponent parser     = NLPGetter.getComponent(file, language, NLPLib.MODE_DEP);
		AbstractComponent identifier = NLPGetter.getComponent(file, language, NLPLib.MODE_PRED);
		AbstractComponent classifier = NLPGetter.getComponent(file, language, NLPLib.MODE_ROLE);
		AbstractComponent labeler    = NLPGetter.getComponent(file, language, NLPLib.MODE_SRL);
		
		AbstractComponent[] components = {tagger, analyzer, parser, identifier, classifier, labeler};
		
		String sentence = "I'd like to meet Dr. Choi.";
		process(tokenizer, components, sentence);
		process(tokenizer, components, UTInput.createBufferedFileReader(inputFile), UTOutput.createPrintBufferedFileStream(outputFile));
	}
	
	public void process(AbstractTokenizer tokenizer, AbstractComponent[] components, String sentence)
	{
		DEPTree tree = NLPGetter.toDEPTree(tokenizer.getTokens(sentence));
		
		for (AbstractComponent component : components)
			component.process(tree);

		System.out.println(tree.toStringSRL()+"\n");
	}
	
	public void process(AbstractTokenizer tokenizer, AbstractComponent[] components, BufferedReader reader, PrintStream fout)
	{
		AbstractSegmenter segmenter = NLPGetter.getSegmenter(language, tokenizer);
		DEPTree tree;
		
		for (List<String> tokens : segmenter.getSentences(reader))
		{
			tree = NLPGetter.toDEPTree(tokens);
			
			for (AbstractComponent component : components)
				component.process(tree);
			
			fout.println(tree.toStringSRL()+"\n");
		}
		
		fout.close();
	}

	public static void main(String[] args)
	{
		String modelFile  = args[0];
		String inputFile  = args[1];
		String outputFile = args[2];
		
		try
		{
			new DemoNLPDecoder(modelFile, inputFile, outputFile);
		}
		catch (Exception e) {e.printStackTrace();}
	}
}
