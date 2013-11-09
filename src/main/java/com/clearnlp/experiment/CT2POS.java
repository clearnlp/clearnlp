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
package com.clearnlp.experiment;

import java.io.File;
import java.io.PrintStream;

import org.kohsuke.args4j.Option;

import com.clearnlp.constituent.CTNode;
import com.clearnlp.constituent.CTReader;
import com.clearnlp.constituent.CTTree;
import com.clearnlp.io.FileExtFilter;
import com.clearnlp.reader.AbstractColumnReader;
import com.clearnlp.run.AbstractRun;
import com.clearnlp.util.UTInput;
import com.clearnlp.util.UTOutput;


/**
 * Normalizes indices of constituent trees.
 * @see CTReader#normalizeIndices(CTTree)
 * @since v0.1
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class CT2POS extends AbstractRun
{
	@Option(name="-i", usage="the input directory path (required)", required=true, metaVar="<dirpath>")
	private String s_inputDir;
	@Option(name="-o", usage="the output directory path (required)", required=true, metaVar="<dirpath>")
	private String s_outputDir;
	@Option(name="-e", usage="the treefile extension (required)", required=true, metaVar="<extension>")
	private String s_extension;
	
	public CT2POS() {}
	
	public CT2POS(String[] args)
	{
		initArgs(args);
		run(s_inputDir, s_outputDir, s_extension);
	}
	
	/**
	 * Normalizes indices of constituent trees.
	 * @param inputDir the directory containing unnormalized tree files.
	 * @param outputDir the directory to save normalized tree files.
	 * @param extension the tree file extension (e.g., {@code parse}).
	 */
	public void run(String inputDir, String outputDir, String extension)
	{
		CTReader      reader;
		CTTree        tree;
		PrintStream   fout;
		StringBuilder build;
		
		File dir = new File(outputDir);
		if (!dir.exists())	dir.mkdirs();
		
		inputDir  += File.separator;
		outputDir += File.separator;
		
		for (String filename : new File(inputDir).list(new FileExtFilter(extension)))
		{
			reader = new CTReader(UTInput.createBufferedFileReader(inputDir + filename));
			fout   = UTOutput.createPrintBufferedFileStream(outputDir + filename);
			
			while ((tree = reader.nextTree()) != null)
			{
			/*	build = new StringBuilder();
				
				for (CTNode node : tree.getTokens())
				{
					build.append(" ");
					build.append(node.form);
					build.append("/");
					build.append(node.pTag);
				}
				
				fout.println(build.substring(1));*/
				
				for (CTNode node : tree.getTokens())
				{
					build = new StringBuilder();
					
					build.append(node.form);
					build.append(AbstractColumnReader.DELIM_COLUMN);
					build.append(node.pTag);
					
					fout.println(build.toString());
				}
				
				fout.println();
			}
			
			reader.close();
			fout.close();
		}
	}
	
	static public void main(String[] args)
	{
		new CT2POS(args);
	//	new CTNormalizeIndices().normalize(inputDir, outputDir, extension);
	}
}
