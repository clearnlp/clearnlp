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
import java.io.PrintStream;

import org.kohsuke.args4j.Option;

import com.clearnlp.constituent.CTLib;
import com.clearnlp.constituent.CTReader;
import com.clearnlp.constituent.CTTree;
import com.clearnlp.io.FileExtFilter;
import com.clearnlp.util.UTInput;
import com.clearnlp.util.UTOutput;


/**
 * Normalizes indices of empty categories in constituent trees.
 * @see CTReader#normalizeIndices(CTTree)
 * @since v0.1
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class ECNormalize extends AbstractRun
{
	@Option(name="-i", usage="the input path (input; required)", required=true, metaVar="<filepath>")
	private String s_inputPath;
	@Option(name="-o", usage="the output path (output; required)", required=true, metaVar="<filepath>")
	private String s_outputPath;
	@Option(name="-ie", usage="the input file extension (default: .*)", required=false, metaVar="<regex>")
	private String s_inputExt = ".*";
	
	public ECNormalize() {}
	
	public ECNormalize(String[] args)
	{
		initArgs(args);
		
		if (new File(s_inputPath).isFile())
			normalize(s_inputPath, s_outputPath);
		else
			normalize(s_inputPath, s_outputPath, s_inputExt);
	}
	
	/**
	 * Normalizes indices of constituent trees.
	 * @param inputPath the directory containing unnormalized tree files.
	 * @param outputPath the directory to save normalized tree files.
	 * @param inputExt the tree file extension (e.g., {@code parse}).
	 */
	public void normalize(String inputPath, String outputPath, String inputExt)
	{
		File dir = new File(outputPath);
		if (!dir.exists())	dir.mkdirs();
		
		inputPath  += File.separator;
		outputPath += File.separator;
		
		String inputFile, outputFile;
		
		for (String filename : new File(inputPath).list(new FileExtFilter(inputExt)))
		{
			inputFile  = inputPath  + filename;
			outputFile = outputPath + filename;
			
			System.out.println(filename);
			normalize(inputFile, outputFile);
		}
	}
	
	public void normalize(String inputFile, String outputFile)
	{
		CTReader reader = new CTReader(UTInput.createBufferedFileReader(inputFile));
		PrintStream fout = UTOutput.createPrintBufferedFileStream(outputFile);
		CTTree tree;
		
		while ((tree = reader.nextTree()) != null)
		{
			CTLib.normalizeIndices(tree);
			fout.println(tree.toString()+"\n");
		}
		
		reader.close();
		fout.close();
	}
	
	static public void main(String[] args)
	{
		new ECNormalize(args);
	//	new CTNormalizeIndices().normalize(inputDir, outputDir, extension);
	}
}
