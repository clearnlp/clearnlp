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
import java.util.ArrayList;
import java.util.List;

import com.clearnlp.constituent.CTLibEn;
import com.clearnlp.constituent.CTNode;
import com.clearnlp.constituent.CTReader;
import com.clearnlp.constituent.CTTree;
import com.clearnlp.util.UTFile;
import com.clearnlp.util.UTInput;
import com.clearnlp.util.UTOutput;

public class RemoveEdited
{
	public RemoveEdited(String inputPath, String parseExt, String outputExt)
	{
		remove(inputPath, parseExt, outputExt);
	}
	
	public void remove(String inputPath, String parseExt, String outputExt)
	{
		File file = new File(inputPath);
		
		if (file.isDirectory())
		{
			for (String filePath : file.list())
				remove(inputPath+File.separator+filePath, parseExt, outputExt);
		}
		else if (inputPath.endsWith(parseExt))
		{
			PrintStream fout = UTOutput.createPrintBufferedFileStream(UTFile.replaceExtension(inputPath, outputExt));
			CTReader reader = new CTReader(UTInput.createBufferedFileReader(inputPath));
			CTTree tree;
			
			while ((tree = reader.nextTree()) != null)
			{
				remove(inputPath, tree.getRoot());
				fout.println(tree.toString()+"\n");
			}
			
			fout.close();
			reader.close();
		} 
	}
	
	public void remove(String inputPath, CTNode curr)
	{
		List<CTNode> remove = new ArrayList<CTNode>();
		List<CTNode> children = curr.getChildren();
		
		for (CTNode child : children)
		{
			if (child.isPTag(CTLibEn.PTAG_EDITED) || (child.getChildrenSize() == 1 &&  child.getChild(0).isPTag(CTLibEn.PTAG_EDITED)))
				remove.add(child);
			else if (child.isPhrase())
				remove(inputPath, child);
		}
		
		if (remove.size() == children.size())
			System.out.println(inputPath+"\n"+curr.toString());
		
		for (CTNode child : remove)
			curr.removeChild(child);
	}

	static public void main(String[] args)
	{
		new RemoveEdited(args[0], args[1], args[2]);
	}
}
