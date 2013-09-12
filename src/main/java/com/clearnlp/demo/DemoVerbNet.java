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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.PrintStream;

import com.clearnlp.dependency.DEPTree;
import com.clearnlp.nlp.NLPProcess;
import com.clearnlp.propbank.verbnet.PVMap;
import com.clearnlp.reader.SRLReader;
import com.clearnlp.util.UTInput;
import com.clearnlp.util.UTOutput;

/**
 * @since 1.1.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class DemoVerbNet
{
	public DemoVerbNet(String mapFile, String inputFile, String outputFile) throws Exception
	{
		PVMap map = new PVMap(new BufferedInputStream(new FileInputStream(mapFile)));
		BufferedReader fin = UTInput.createBufferedFileReader(inputFile);
		PrintStream fout = UTOutput.createPrintBufferedFileStream(outputFile);
	
		addVerbNet(map, fin, fout);
	}
	
	public void addVerbNet(PVMap map, BufferedReader fin, PrintStream fout)
	{
		SRLReader reader = new SRLReader(0, 1, 2, 3, 4, 5, 6, 7);
		
		reader.open(fin);
		DEPTree tree;
		
		while ((tree = reader.next()) != null)
		{
			NLPProcess.addVerbNet(map, tree);
			fout.println(tree.toStringSRL()+"\n");
		}
		
		reader.close();
		fout.close();
	}

	public static void main(String[] args)
	{
		String mapFile    = args[0];
		String inputFile  = args[1];
		String outputFile = args[2];

		try
		{
			new DemoVerbNet(mapFile, inputFile, outputFile);
		}
		catch (Exception e) {e.printStackTrace();}
	}
}
