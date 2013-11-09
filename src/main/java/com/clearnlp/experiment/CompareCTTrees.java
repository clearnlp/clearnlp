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
