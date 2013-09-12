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
package com.clearnlp.experiment;

import java.io.File;
import java.io.PrintStream;

import com.clearnlp.constituent.CTNode;
import com.clearnlp.constituent.CTReader;
import com.clearnlp.constituent.CTTree;
import com.clearnlp.util.UTFile;
import com.clearnlp.util.UTInput;
import com.clearnlp.util.UTOutput;

public class GeneratePOS
{
	public GeneratePOS(String inputDir, String inputExt, String outputDir)
	{
		File file = new File(inputDir);
		String outputFile;
		PrintStream fout;
		
		for (File section : file.listFiles())
		{
			if (section.isDirectory())
			{
				outputFile = outputDir+File.separator+section.getName()+".pos";
				fout = UTOutput.createPrintBufferedFileStream(outputFile);
				System.out.println(outputFile);
				
				for (String inputFile : UTFile.getSortedFileList(section.getAbsolutePath()))
					printTree(fout, inputFile);
				
				fout.close();
			}
		}
	}
	
	private void printTree(PrintStream fout, String inputFile)
	{
		CTReader reader = new CTReader();
		StringBuilder build;
		CTTree tree;
		
		reader.open(UTInput.createBufferedFileReader(inputFile));
		
		while ((tree = reader.nextTree()) != null)
		{
			build = new StringBuilder();
			
			for (CTNode node : tree.getTerminals())
			{
				build.append(node.form);
				build.append("\t");
				build.append(node.pTag);
				build.append("\n");
			}
			
			fout.println(build.toString());
		}
		
		reader.close();
	}
	
	static public void main(String[] args)
	{
		new GeneratePOS(args[0], args[1], args[2]);
	}
}
