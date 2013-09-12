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
package com.clearnlp.run;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import org.kohsuke.args4j.Option;

import com.clearnlp.nlp.NLPGetter;
import com.clearnlp.reader.AbstractReader;
import com.clearnlp.segmentation.AbstractSegmenter;
import com.clearnlp.tokenization.AbstractTokenizer;
import com.clearnlp.util.UTArray;
import com.clearnlp.util.UTInput;
import com.clearnlp.util.UTOutput;

/**
 * @since 1.1.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class Tokenizer extends AbstractRun
{
	@Option(name="-i", usage="input path (required)", required=true, metaVar="<filepath>")
	private String s_inputPath;
	@Option(name="-ie", usage="input file extension (default: .*)", required=false, metaVar="<regex>")
	private String s_inputExt = ".*";
	@Option(name="-oe", usage="output file extension (default: tok)", required=false, metaVar="<string>")
	private String s_outputExt = "tok";
	@Option(name="-l", usage="language (default: "+AbstractReader.LANG_EN+")", required=false, metaVar="<language>")
	private String s_language = AbstractReader.LANG_EN;
	@Option(name="-if", usage="input format (default: "+AbstractReader.TYPE_RAW+")", required=false, metaVar="<string>")
	private String i_format = AbstractReader.TYPE_RAW;
	@Option(name="-of", usage="output format (default: "+AbstractReader.TYPE_LINE+")", required=false, metaVar="<string>")
	private String o_format = AbstractReader.TYPE_LINE;
	@Option(name="-twit", usage="if set, do not tokenize special punctuation used in twitter", required=false, metaVar="<boolean>")
	protected boolean b_twit;
	
	public Tokenizer() {}
	
	public Tokenizer(String[] args)
	{
		initArgs(args);
		
		try
		{
			AbstractTokenizer tokenizer = NLPGetter.getTokenizer(s_language);
			AbstractSegmenter segmenter = i_format.equals(AbstractReader.TYPE_RAW) ? NLPGetter.getSegmenter(s_language, tokenizer) : null;
			List<String[]>    filenames = getFilenames(s_inputPath, s_inputExt, s_outputExt);
			boolean outLine = o_format.equals(AbstractReader.TYPE_LINE);
			tokenizer.setTwit(b_twit);
			
			for (String[] io : filenames)
			{
				System.out.println(io[0]);
				tokenize(tokenizer, segmenter, io[0], io[1], outLine);
			}
		}
		catch (IOException e) {e.printStackTrace();}
	}
	
	public void tokenize(AbstractTokenizer tokenizer, AbstractSegmenter segmenter, String inputFile, String outputFile, boolean outLine) throws IOException
	{
		BufferedReader fin = UTInput.createBufferedFileReader(inputFile);
		PrintStream   fout = UTOutput.createPrintBufferedFileStream(outputFile);
		
		if (segmenter == null)
		{
			String line;
		
			while ((line = fin.readLine()) != null)
				print(fout, tokenizer.getTokens(line), outLine);
		}
		else
		{
			for (List<String> tokens : segmenter.getSentences(fin))
				print(fout, tokens, outLine);
		}
		
		fin.close();
		fout.close();
	}
	
	private void print(PrintStream fout, List<String> tokens, boolean outLine)
	{
		if (outLine)
			fout.println(UTArray.join(tokens, " "));
		else
			fout.println(UTArray.join(tokens, "\n")+"\n");
	}
	
	static public void main(String[] args)
	{
		new Tokenizer(args);
	}
}
