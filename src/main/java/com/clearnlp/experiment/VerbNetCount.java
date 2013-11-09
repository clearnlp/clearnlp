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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.clearnlp.dependency.DEPFeat;
import com.clearnlp.dependency.DEPLib;
import com.clearnlp.propbank.verbnet.PVMap;
import com.clearnlp.propbank.verbnet.PVRoleset;
import com.clearnlp.propbank.verbnet.PVVerb;
import com.clearnlp.util.UTFile;
import com.clearnlp.util.UTInput;
import com.clearnlp.util.UTOutput;

public class VerbNetCount
{
	public VerbNetCount(String pbvnFile) throws Exception
	{
		PVMap pvMap = new PVMap(new BufferedInputStream(new FileInputStream(pbvnFile)));
		Set<String> prev, curr = new HashSet<String>();
		
		do
		{
			prev = curr;
			curr = getRSet(pvMap, prev);
			System.out.println("---------------------");
		}
		while (prev.size() < curr.size());
	}
	
	public VerbNetCount(String pbvnFile, String inputDir) throws Exception
	{
		PVMap pvMap = new PVMap(new BufferedInputStream(new FileInputStream(pbvnFile)));
		Map<String,Set<String>> mDif = new HashMap<String,Set<String>>();
		Map<String,Set<String>> mSub = new HashMap<String,Set<String>>();
		Pattern delim = Pattern.compile("\t");
		String line, roleset, vncls;
		BufferedReader fin;
		Set<String> sVN;
		DEPFeat feat;
		String[] l;
		
		for (String filename : UTFile.getInputFileList(inputDir, ".pmd"))
		{
			fin = UTInput.createBufferedFileReader(filename);
			
			while ((line = fin.readLine()) != null)
			{
				line = line.trim();
				
				if (line.isEmpty())
				{
					
				}
				else
				{
					l = delim.split(line);
					feat = new DEPFeat(l[6]);
					roleset = feat.get(DEPLib.FEAT_PB);
					vncls = feat.get(DEPLib.FEAT_VN);
					
					if (roleset != null && vncls != null)
					{
						sVN = pvMap.getVNSet(roleset);
						
						if (!sVN.contains(vncls))
						{
							if (checkSub(sVN, vncls))	add(mSub, roleset, vncls);
							else						add(mDif, roleset, vncls);
						}
					}
				}
			}
			
			fin.close();
		}
		
		printMap(mDif, "dif.txt");
		printMap(mSub, "sub.txt");
	}
	
	private boolean checkSub(Set<String> set, String vncls)
	{
		for (String vn : set)
		{
			if (vncls.startsWith(vn) || vn.startsWith(vncls))
				return true;
		}
		
		return false;
	}
	
	private void add(Map<String,Set<String>> map, String roleset, String vncls)
	{
		Set<String> set = map.get(roleset);
		
		if (set == null)
		{
			set = new HashSet<String>();
			map.put(roleset, set);
		}
		
		set.add(vncls);
	}
	
	private void printMap(Map<String,Set<String>> map, String outFile)
	{
		PrintStream fout = UTOutput.createPrintBufferedFileStream(outFile);
		List<String> rolesets = new ArrayList<String>(map.keySet());
		Collections.sort(rolesets);
		
		for (String roleset : rolesets)
			fout.printf("%20s: %s\n", roleset, map.get(roleset).toString());
		
		fout.close();
	}
	
	public Set<String> getRSet(PVMap pvMap, Set<String> prev)
	{
		Set<String> sVncls, sVerb, curr = new HashSet<String>();
		PVRoleset   pvRoleset;
		PVVerb      pvVerb;
		
		int[] count = new int[5];
		int total = 0, c;
		
		for (String verb : pvMap.keySet())
		{
			pvVerb = pvMap.get(verb);
			 sVerb = pvVerb.keySet();
			total += sVerb.size();
			
			for (String roleset : sVerb)
			{
				pvRoleset = pvVerb.get(roleset);
				sVncls = new HashSet<String>(pvRoleset.keySet());
				c = sVncls.size();
				sVncls.removeAll(prev);
				
				if (sVncls.isEmpty())	c = 1;
				else if (c > 5)			c = 5;
				
				count[c-1]++;
				if (c == 1)	curr.addAll(sVncls);
			}
		}
		
		printCount(count, total);
		return curr;
	}
	
	public Set<String> getVSet(PVMap pvMap, Set<String> prev)
	{
		Set<String> sVncls, sVerb, curr = new HashSet<String>();
		PVRoleset   pvRoleset;
		PVVerb      pvVerb;
		
		int[] count = new int[5];
		int total = 0, c;
		
		for (String verb : pvMap.keySet())
		{
			pvVerb = pvMap.get(verb);
			 sVerb = pvVerb.keySet();
			sVncls = new HashSet<String>();
			total++;
			
			for (String roleset : sVerb)
			{
				pvRoleset = pvVerb.get(roleset);
				sVncls.addAll(pvRoleset.keySet());
			}
			
			c = sVncls.size();
			sVncls.removeAll(prev);
			
			if (sVncls.isEmpty())	c = 1;
			else if (c > 5)			c = 5;
			
			count[c-1]++;
			if (c == 1)	curr.addAll(sVncls);
		}
		
		printCount(count, total);
		return curr;
	}
	
	private void printCount(int[] count, int total)
	{
		int i, c, size = count.length;
		
		for (i=0; i<size; i++)
		{
			c = count[i];
			System.out.printf("%2d: %5.2f (%d/%d)\n", i+1, 100d*c/total, c, total);
		}
	}
	
	static public void main(String[] args) throws Exception
	{
		new VerbNetCount(args[0], args[1]);
	}
}
