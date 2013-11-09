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
package com.clearnlp.run;

import java.io.BufferedReader;
import java.io.IOException;

import org.kohsuke.args4j.Option;

import com.clearnlp.component.evaluation.SRLEvalFull;
import com.clearnlp.dependency.DEPLib;
import com.clearnlp.reader.AbstractColumnReader;
import com.clearnlp.util.UTInput;
import com.clearnlp.util.pair.StringIntPair;


public class SRLEvaluate extends AbstractRun
{
	@Option(name="-g", usage="the gold-standard file (input; required)", required=true, metaVar="<filename>")
	private String s_goldFile;
	@Option(name="-s", usage="the system file (input; required)", required=true, metaVar="<filename>")
	private String s_autoFile;
	@Option(name="-gi", usage="the column index of semantic arguments in the gold-standard file (input; required)", required=true, metaVar="<integer>")
	private int    i_goldIndex;
	@Option(name="-si", usage="the column index of semantic arguments in the sytem file (input; required)", required=true, metaVar="<integer>")
	private int    i_autoIndex;
	
	public SRLEvaluate() {}
	
	public SRLEvaluate(String[] args)
	{
		initArgs(args);
		run(s_goldFile, s_autoFile, i_goldIndex-1, i_autoIndex-1);
	}
	
	public void run(String goldFile, String autoFile, int goldIndex, int autoIndex)
	{
		BufferedReader fGold = UTInput.createBufferedFileReader(goldFile);
		BufferedReader fAuto = UTInput.createBufferedFileReader(autoFile);
		StringIntPair[] gHeads, aHeads;
		SRLEvalFull eval = new SRLEvalFull();
		String[] gold, auto;
		String line;
		
		try
		{
			while ((line = fGold.readLine()) != null)
			{
				gold = line.split(AbstractColumnReader.DELIM_COLUMN);
				auto = fAuto.readLine().split(AbstractColumnReader.DELIM_COLUMN);
				
				line = line.trim();
				if (line.isEmpty())	 continue;
				
				gHeads = toSHeads(gold[goldIndex]);
				aHeads = toSHeads(auto[autoIndex]);
				eval.evaluate(gHeads, aHeads);
			}
		}
		catch (IOException e) {e.printStackTrace();}
		
		eval.print();
	}
	
	private StringIntPair[] toSHeads(String sHeads)
	{
		if (sHeads.equals(AbstractColumnReader.BLANK_COLUMN))
			return new StringIntPair[0];
			
		String[] heads = sHeads.split(DEPLib.DELIM_HEADS), tmp;
		int i, size = heads.length, headId;
		
		StringIntPair[] p = new StringIntPair[size];
		String label;
		
		for (i=0; i<size; i++)
		{
			tmp    = heads[i].split(DEPLib.DELIM_HEADS_KEY);
			headId = Integer.parseInt(tmp[0]);
			label  = tmp[1];
			p[i]   = new StringIntPair(label, headId); 
		}
		
		return p;
	}

	static public void main(String[] args)
	{
		new SRLEvaluate(args);
	}
}
