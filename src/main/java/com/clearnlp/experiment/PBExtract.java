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

import com.clearnlp.constituent.CTNode;
import com.clearnlp.constituent.CTTree;
import com.clearnlp.propbank.PBInstance;
import com.clearnlp.propbank.PBLib;
import com.clearnlp.util.UTOutput;


public class PBExtract
{
	public PBExtract(String propFile, String treeDir, String outFile)
	{
		PrintStream fout = UTOutput.createPrintBufferedFileStream(outFile);
		
		for (PBInstance instance : PBLib.getPBInstanceList(propFile, treeDir, false))
		{
			if (instance.getArgSize() < 2)	continue;
			
			StringBuilder build = new StringBuilder();
			CTTree tree = instance.getTree();
			
			build.append(instance.treePath);	build.append("\t");
			build.append(instance.treeId);		build.append("\t");
			build.append(instance.predId);		build.append("\t");
			build.append(instance.roleset);		build.append("\t");
			build.append(tree.getTerminal(instance.predId).getTokenId());
			build.append("\t");
			build.append(getRawLine(tree, instance.predId));
			
			fout.println(build.toString());
		}
		
		fout.close();
	}
	
	private String getRawLine(CTTree tree, int predId)
	{
		StringBuilder build = new StringBuilder();
		
		for (CTNode node : tree.getTokens())
		{
			build.append(" ");
			
			if (node.getTerminalId() == predId)
			{
				build.append("[");
				build.append(node.form);
				build.append("]");
			}
			else
				build.append(node.form);
		}
		
		return build.substring(1);
	}

	public static void main(String[] args)
	{
		String propFile = args[0];
		String treeDir  = args[1];
		String outFile  = args[2];
		
		new PBExtract(propFile, treeDir, outFile);
	}

}
