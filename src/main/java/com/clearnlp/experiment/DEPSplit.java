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

import java.io.File;
import java.io.PrintStream;

import com.clearnlp.dependency.DEPTree;
import com.clearnlp.reader.DEPReader;
import com.clearnlp.util.UTFile;
import com.clearnlp.util.UTInput;
import com.clearnlp.util.UTOutput;


public class DEPSplit
{
	static public void main(String[] args)
	{
		DEPReader reader = new DEPReader(0, 1, 3, 5, 6, 9, 10);
		String[] filelist = UTFile.getSortedFileList(args[0]);
		PrintStream[] fout = new PrintStream[10];
		DEPTree tree;
		int i;
		
		for (i=0; i<fout.length; i++)
			fout[i] = UTOutput.createPrintBufferedFileStream(args[1]+File.separator+i+".dep");
		
		for (String filename : filelist)
		{
			reader.open(UTInput.createBufferedFileReader(filename));
			
			while ((tree = reader.next()) != null)
			{
				i = (tree.size() > 101) ? 9 : (tree.size()-2) / 10;
				fout[i].println(tree.toStringCoNLL()+"\n");
			}	
			
			reader.close();
		}
		
		for (i=0; i<fout.length; i++)
			fout[i].close();
	}

}
