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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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
import com.google.common.collect.Lists;

/**
 * @since 1.1.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class DemoMultiThread
{
	final String language = AbstractReader.LANG_EN;
	AbstractComponent[] c_components;
	
	public DemoMultiThread(String modelFile, String inputFile, String outputFile, int numThreads) throws Exception
	{
		ZipFile file = new ZipFile(modelFile);
		
		AbstractTokenizer tokenizer  = NLPGetter.getTokenizer(language);
		AbstractComponent tagger     = NLPGetter.getComponent(file, language, NLPLib.MODE_POS);
		AbstractComponent analyzer   = NLPGetter.getComponent(file, language, NLPLib.MODE_MORPH);
		AbstractComponent parser     = NLPGetter.getComponent(file, language, NLPLib.MODE_DEP);
		AbstractComponent identifier = NLPGetter.getComponent(file, language, NLPLib.MODE_PRED);
		AbstractComponent classifier = NLPGetter.getComponent(file, language, NLPLib.MODE_ROLE);
		AbstractComponent labeler    = NLPGetter.getComponent(file, language, NLPLib.MODE_SRL);
		
		c_components = new AbstractComponent[]{tagger, analyzer, parser, identifier, classifier, labeler};
		process(tokenizer, UTInput.createBufferedFileReader(inputFile), UTOutput.createPrintBufferedFileStream(outputFile), numThreads);
	}
	
	public void process(AbstractTokenizer tokenizer, BufferedReader reader, PrintStream fout, int numThreads)
	{
		AbstractSegmenter segmenter = NLPGetter.getSegmenter(language, tokenizer);
		ExecutorService executor = Executors.newFixedThreadPool(numThreads);
		List<DEPTree> trees = Lists.newArrayList();
		DEPTree tree;
		
		for (List<String> tokens : segmenter.getSentences(reader))
		{
			tree = NLPGetter.toDEPTree(tokens);
			trees.add(tree);
			executor.execute(new DecodeTask(tree));
		}
		
		executor.shutdown();
		
		try
		{
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		}
		catch (InterruptedException e) {e.printStackTrace();}
		
		for (DEPTree t : trees)
			fout.println(t.toStringSRL()+"\n");			
		
		fout.close();
	}
	
	class DecodeTask implements Runnable
	{
		DEPTree d_tree;
		
		public DecodeTask(DEPTree tree)
		{
			d_tree = tree;
		}
		
		public void run()
		{
			for (AbstractComponent component : c_components)
				component.process(d_tree);			
		}
    }

	public static void main(String[] args)
	{
		String modelFile  = args[0];
		String inputFile  = args[1];
		String outputFile = args[2];
		int    numThreads = Integer.parseInt(args[3]);
		
		try
		{
			new DemoMultiThread(modelFile, inputFile, outputFile, numThreads);
		}
		catch (Exception e) {e.printStackTrace();}
	}
}
