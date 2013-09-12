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
package com.clearnlp.experiment;

import java.io.PrintStream;

import org.kohsuke.args4j.Option;

import com.clearnlp.constituent.CTReader;
import com.clearnlp.constituent.CTTree;
import com.clearnlp.run.AbstractRun;
import com.clearnlp.util.UTInput;
import com.clearnlp.util.UTOutput;


/**
 * Prints each tree in one line.
 * @see CTTree#toStringLine()
 * @since v0.1
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class PrintTreeLine extends AbstractRun
{
	@Option(name="-i", usage="the input file (required)", required=true, metaVar="<filename>")
	private String s_inputDir;
	@Option(name="-o", usage="the output file (required)", required=true, metaVar="<filename>")
	private String s_outputDir;
	
	public PrintTreeLine() {}
	
	public PrintTreeLine(String[] args)
	{
		initArgs(args);
		print(s_inputDir, s_outputDir);
	}
	
	/**
	 * Prints each tree in one line.
	 * @param inputFile the name of the file containing input trees.
	 * @param outputFile the name of the file to contain output trees.
	 */
	public void print(String inputFile, String outputFile)
	{
		CTReader    reader = new CTReader(UTInput.createBufferedFileReader(inputFile));
		PrintStream fout   = UTOutput.createPrintBufferedFileStream(outputFile);
		CTTree      tree;

		while ((tree = reader.nextTree()) != null)
			fout.println(tree.toStringLine());
		
		fout.close();
	}
	
	static public void main(String[] args)
	{
		new PrintTreeLine(args);
	}
}
