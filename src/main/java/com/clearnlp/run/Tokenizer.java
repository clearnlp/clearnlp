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
