/**
* Copyright (c) 2009-2012, Regents of the University of Colorado
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
*
* Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
* Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
* Neither the name of the University of Colorado at Boulder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
* AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
* IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
* ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
* LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
* CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
* ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
* POSSIBILITY OF SUCH DAMAGE.
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
