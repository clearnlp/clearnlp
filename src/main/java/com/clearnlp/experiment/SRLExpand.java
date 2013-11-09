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

import java.io.PrintStream;
import java.util.Arrays;

import com.carrotsearch.hppc.IntOpenHashSet;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import com.clearnlp.dependency.DEPArc;
import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dependency.DEPTree;
import com.clearnlp.reader.AbstractColumnReader;
import com.clearnlp.reader.SRLReader;
import com.clearnlp.util.UTArray;
import com.clearnlp.util.UTInput;
import com.clearnlp.util.UTOutput;


public class SRLExpand
{
	public SRLExpand(String inputFile, String outputFile)
	{
		PrintStream fout = UTOutput.createPrintBufferedFileStream(outputFile);
		SRLReader reader = new SRLReader(0, 1, 2, 3, 4, 5, 6, 7);
		reader.open(UTInput.createBufferedFileReader(inputFile));
		DEPTree tree;
	
		while ((tree = reader.next()) != null)
		{
			tree.setDependents();
			printSpans(fout, tree);
		}
		
		reader.close();
		fout.close();
	}
	
	private void printSpans(PrintStream fout, DEPTree tree)
	{
		String[][] spans = expandSRL(tree);
		
		if (spans == null)
		{
			fout.println(tree+AbstractColumnReader.DELIM_SENTENCE);
			return;
		}
		
		StringBuilder build = new StringBuilder();
		int i, size = tree.size();
		DEPNode node;
		
		for (i=1; i<size; i++)
		{
			node = tree.get(i);
			
			build.append(node.toStringDEP());
			build.append(AbstractColumnReader.DELIM_COLUMN);
			build.append(UTArray.join(spans[i], AbstractColumnReader.DELIM_COLUMN));
			build.append(AbstractColumnReader.DELIM_SENTENCE);
		}
		
		fout.println(build.toString());
	}
	
	public String[][] expandSRL(DEPTree tree)
	{
		ObjectIntOpenHashMap<DEPNode> map = new ObjectIntOpenHashMap<DEPNode>();
		int i = 0, predId = 0, size = tree.size();
		DEPNode pred, arg;
		String label;
		
		while ((pred = tree.getNextPredicate(predId)) != null)
		{
			map.put(pred, i++);
			predId = pred.id;
		}
		
		if (map.isEmpty())	return null;

		String[][] spans = new String[size][];
		int len = map.size();
		
		for (i=1; i<size; i++)
		{
			spans[i] = new String[len];
			Arrays.fill(spans[i], AbstractColumnReader.BLANK_COLUMN);
		}
		
		for (i=1; i<size; i++)
		{
			arg = tree.get(i);
			
			for (DEPArc arc : arg.getSHeads())
			{
				pred = arc.getNode();
				if (!map.containsKey(pred))	continue;
				
				predId = map.get(pred);
				label  = arc.getLabel();
				
				for (int spanId : getSpan(pred, arg))
					spans[spanId][predId] = label;
			}
		}
		
		return spans;
	}
	
	private int[] getSpan(DEPNode pred, DEPNode arg)
	{
		IntOpenHashSet sArg = arg .getSubIdSet();
		
		if (pred.isDescendentOf(arg))
			sArg.removeAll(pred.getSubIdSet());			
		
		int[] span = sArg.toArray();
		return span;
	}
	
	static public void main(String[] args)
	{
		new SRLExpand(args[0], args[1]);
	}
}
