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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import com.clearnlp.constituent.CTNode;
import com.clearnlp.constituent.CTReader;
import com.clearnlp.constituent.CTTree;
import com.clearnlp.io.FileExtFilter;
import com.clearnlp.util.UTArray;
import com.clearnlp.util.UTInput;
import com.clearnlp.util.UTOutput;
import com.clearnlp.util.pair.IntIntPair;

public class GenerateOntoNotesCoref
{
	final Pattern P_SPACE = Pattern.compile(" ");
	final Pattern P_COLON = Pattern.compile(":");
	final Pattern P_UNDER = Pattern.compile("_");
	
	public GenerateOntoNotesCoref(String ontoDir) throws IOException
	{
		generateTokenBasedCorefs(ontoDir);
	}
	
	void generateTokenBasedCorefs(String ontoDir) throws IOException
	{
		List<String> corefFiles = getFilenames(ontoDir, "coref");
		String parseFile;
		
		for (String corefFile : corefFiles)
		{
		//	System.out.println(corefFile);
			parseFile = corefFile.substring(0, corefFile.length()-5) + "parse";
			generateTokenBasedCorefs(parseFile, corefFile, corefFile+".tok");
		}
	}
	
	List<String> getFilenames(String rootDir, String ext)
	{
		List<String> filenames = new ArrayList<String>();
		File root = new File(rootDir);
		File fGenre, fSource, fSection;

		for (String genre : root.list())
		{
			fGenre = new File(root.getAbsolutePath() + File.separator + genre);
			
			if (fGenre.isDirectory())
			{
				for (String source : fGenre.list())
				{
					fSource = new File(fGenre.getAbsolutePath() + File.separator + source);
							
					if (fSource.isDirectory())
					{
						for (String section : fSource.list())
						{
							fSection = new File(fSource.getAbsolutePath() + File.separator + section);
							
							if (fSection.isDirectory())
							{
								for (String corpus : fSection.list(new FileExtFilter(ext)))
									filenames.add(fSection.getAbsolutePath() + File.separator + corpus);
							}
						}
					}
				}
			}
		}
		
		Collections.sort(filenames);
		return filenames;
	}
	
	private void generateTokenBasedCorefs(String parseFile, String corefFile, String outputFile) throws IOException
	{
		List<CTTree> trees = getTrees(parseFile);
		BufferedReader reader = UTInput.createBufferedFileReader(corefFile);
		PrintStream fout = UTOutput.createPrintBufferedFileStream(outputFile);
		String line;
		
		while ((line = reader.readLine()) != null)
			fout.println(getTokenBasedLine(trees, line));
		
		reader.close();
		fout.close();
	}
	
	private List<CTTree> getTrees(String parseFile)
	{
		CTReader reader = new CTReader(UTInput.createBufferedFileReader(parseFile));
		List<CTTree> trees = new ArrayList<CTTree>();
		CTTree tree;
		
		while ((tree = reader.nextTree()) != null)
			trees.add(tree);
		
		return trees;
	}
	
	private String getTokenBasedLine(List<CTTree> trees, String line)
	{
		List<String> mentions = new ArrayList<String>();
		String[] tmp = P_SPACE.split(line);
		int i, size = tmp.length;
		String mention;
		
		mentions.add(tmp[0]);
		
		for (i=1; i<size; i++)
		{
			mention = getTokenBasedMention(trees, tmp[i]);
			if (mention != null)	mentions.add(mention);
		}
		
		return UTArray.join(mentions, " ");
	}
	
	private String getTokenBasedMention(List<CTTree> trees, String mention)
	{
		StringBuilder build = new StringBuilder();
		int idx = mention.indexOf("-");
		
		String   span = mention.substring(0,idx);
		String   type = mention.substring(idx+1);
		String[] locs = P_COLON.split(span);
		
		IntIntPair fst = getTokenBasedMentionAux(trees, locs[0], true);
		IntIntPair snd = getTokenBasedMentionAux(trees, locs[1], false);
		
		if (fst == null || snd == null || fst.i2 > snd.i2)
		{
			System.err.println("WRONG");
			return null;
		}

		build.append(fst.i1);
		build.append("_");
		build.append(fst.i2);
		build.append(":");
		build.append(snd.i2);
		build.append("-");
		build.append(type);
		
		return build.toString();
	}
	
	private IntIntPair getTokenBasedMentionAux(List<CTTree> trees, String loc, boolean isFirst)
	{
		String[] tmp = P_UNDER.split(loc);
		int treeId = Integer.parseInt(tmp[0]);
		int terminalId = Integer.parseInt(tmp[1]);
		CTTree tree = trees.get(treeId);
		List<CTNode> terminals = tree.getTerminals();
		CTNode node = tree.getTerminal(terminalId);
		
		if (isFirst)
		{
			int size = terminals.size();
			
			while (node.isEmptyCategory())
			{
				if (terminalId+1 < size)
					node = terminals.get(++terminalId);
				else
					return null;
			}	
		}
		else
		{
			while (node.isEmptyCategory())
			{
				if (terminalId-1 >= 0)
					node = terminals.get(--terminalId);
				else
					return null;
			}
		}
		
		return new IntIntPair(treeId, node.getTokenId());
	}

	public static void main(String[] args)
	{
		try {
			new GenerateOntoNotesCoref(args[0]);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
