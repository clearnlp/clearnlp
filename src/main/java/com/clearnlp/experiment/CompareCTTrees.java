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

import com.clearnlp.constituent.CTReader;
import com.clearnlp.constituent.CTTree;
import com.clearnlp.io.FileExtFilter;
import com.clearnlp.util.UTFile;
import com.clearnlp.util.UTInput;


public class CompareCTTrees
{
	public CompareCTTrees(String treeDir, String ext1, String ext2)
	{
		CTReader reader1, reader2;
		CTTree tree1, tree2;
		String filename2;
	//	int n;
		
		for (String filename1 : new File(treeDir).list(new FileExtFilter(ext1)))
		{
		//	System.out.print(filename1+": ");
			filename1 = treeDir + File.separator + filename1;
			filename2 = UTFile.replaceExtension(filename1, ext1, ext2);
			reader1   = new CTReader(UTInput.createBufferedFileReader(filename1));
			reader2   = new CTReader(UTInput.createBufferedFileReader(filename2));
			
		//	for (n=0; (tree1 = reader1.nextTree()) != null; n++)
			while ((tree1 = reader1.nextTree()) != null)
			{
				tree2 = reader2.nextTree();
				
				if (tree1.getTerminals().size() != tree2.getTerminals().size())
				{
					System.out.println(filename2);
					System.out.println(tree1+"\n"+tree2);
					return;
				}
			}
			
		//	System.out.println(n);
		}
	}
	
	static public void main(String[] args)
	{
		new CompareCTTrees(args[0], args[1], args[2]);
	}
}
