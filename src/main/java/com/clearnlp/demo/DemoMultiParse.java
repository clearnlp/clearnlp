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
import com.clearnlp.component.dep.AbstractDEPParser;
import com.clearnlp.dependency.DEPTree;
import com.clearnlp.nlp.NLPGetter;
import com.clearnlp.nlp.NLPLib;
import com.clearnlp.reader.AbstractReader;
import com.clearnlp.segmentation.AbstractSegmenter;
import com.clearnlp.tokenization.AbstractTokenizer;
import com.clearnlp.util.UTInput;
import com.clearnlp.util.UTOutput;
import com.clearnlp.util.pair.ObjectDoublePair;

/**
 * @since 1.1.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class DemoMultiParse
{
	final String language = AbstractReader.LANG_EN;
	
	public DemoMultiParse(String modelFile, String inputFile, String outputFile) throws Exception
	{
		ZipFile file = new ZipFile(modelFile);
		
		AbstractTokenizer tokenizer  = NLPGetter.getTokenizer(language);
		AbstractComponent tagger     = NLPGetter.getComponent(file, language, NLPLib.MODE_POS);
		AbstractComponent analyzer   = NLPGetter.getComponent(file, language, NLPLib.MODE_MORPH);
		AbstractComponent parser     = NLPGetter.getComponent(file, language, NLPLib.MODE_DEP);
		AbstractComponent identifier = NLPGetter.getComponent(file, language, NLPLib.MODE_PRED);
		AbstractComponent classifier = NLPGetter.getComponent(file, language, NLPLib.MODE_ROLE);
		AbstractComponent labeler    = NLPGetter.getComponent(file, language, NLPLib.MODE_SRL);
		
		AbstractComponent[] preComponents  = {tagger, analyzer};	// components used before parsing
		AbstractComponent[] postComponents = {identifier, classifier, labeler};	// components used after parsing
		
		String sentence = "I know you know who I know.";
		process(tokenizer, (AbstractDEPParser)parser, preComponents, postComponents, sentence);
		process(tokenizer, (AbstractDEPParser)parser, preComponents, postComponents, UTInput.createBufferedFileReader(inputFile), UTOutput.createPrintBufferedFileStream(outputFile));
	}
	
	public void process(AbstractTokenizer tokenizer, AbstractDEPParser parser, AbstractComponent[] preComponents, AbstractComponent[] postComponents, String sentence)
	{
		DEPTree tree = NLPGetter.toDEPTree(tokenizer.getTokens(sentence));
		List<ObjectDoublePair<DEPTree>> trees = getParses(parser, preComponents, postComponents, tree);
		
		for (ObjectDoublePair<DEPTree> p : trees)
		{
			tree = (DEPTree)p.o;
			System.out.println("Score: "+p.d);
			System.out.println(tree.toStringSRL()+"\n");
		}
	}
	
	public void process(AbstractTokenizer tokenizer, AbstractDEPParser parser, AbstractComponent[] preComponents, AbstractComponent[] postComponents, BufferedReader reader, PrintStream fout)
	{
		AbstractSegmenter segmenter = NLPGetter.getSegmenter(language, tokenizer);
		List<ObjectDoublePair<DEPTree>> trees;
		DEPTree tree;
		
		for (List<String> tokens : segmenter.getSentences(reader))
		{
			tree  = NLPGetter.toDEPTree(tokens);
			trees = getParses(parser, preComponents, postComponents, tree);
			
			for (ObjectDoublePair<DEPTree> p : trees)
			{
				tree = (DEPTree)p.o;
				fout.println("Score: "+p.d);
				fout.println(tree.toStringSRL()+"\n");
			}
		}
		
		fout.close();
	}
	
	private List<ObjectDoublePair<DEPTree>> getParses(AbstractDEPParser parser, AbstractComponent[] preComponents, AbstractComponent[] postComponents, DEPTree tree)
	{
		List<ObjectDoublePair<DEPTree>> trees;
		boolean uniqueOnly = true;	// return only unique trees given a sentence 
		
		for (AbstractComponent component : preComponents)
			component.process(tree);
		
		trees = parser.getParsedTrees(tree, uniqueOnly);
		
		// parses are already sorted by their scores in descending order
		for (ObjectDoublePair<DEPTree> p : trees)
		{
			tree = (DEPTree)p.o;
			
			for (AbstractComponent component : postComponents)
				component.process(tree);
		}
		
		return trees;
	}

	public static void main(String[] args)
	{
		String modelFile  = args[0];	// e.g., dictionary.zip
		String inputFile  = args[1];
		String outputFile = args[2];

		try
		{
			new DemoMultiParse(modelFile, inputFile, outputFile);
		}
		catch (Exception e) {e.printStackTrace();}
	}
}
