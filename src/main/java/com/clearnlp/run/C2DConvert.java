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

import java.io.PrintStream;
import java.util.List;

import org.kohsuke.args4j.Option;

import com.clearnlp.component.AbstractComponent;
import com.clearnlp.constituent.CTLib;
import com.clearnlp.constituent.CTLibEn;
import com.clearnlp.constituent.CTReader;
import com.clearnlp.constituent.CTTree;
import com.clearnlp.conversion.AbstractC2DConverter;
import com.clearnlp.dependency.DEPFeat;
import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dependency.DEPTree;
import com.clearnlp.nlp.NLPGetter;
import com.clearnlp.reader.AbstractReader;
import com.clearnlp.util.UTInput;
import com.clearnlp.util.UTOutput;


public class C2DConvert extends AbstractRun
{
	@Option(name="-i", usage="input path (required)", required=true, metaVar="<filepath>")
	private String s_inputPath;
	@Option(name="-ie", usage="input file extension (default: .*)", required=false, metaVar="<regex>")
	private String s_inputExt = ".*";
	@Option(name="-oe", usage="output file extension (default: dep)", required=false, metaVar="<string>")
	private String s_outputExt = "dep";
	@Option(name="-l", usage="language (default: "+AbstractReader.LANG_EN+")", required=false, metaVar="<language>")
	private String s_language = AbstractReader.LANG_EN;
	@Option(name="-h", usage="name of a headrule file (required)", required=true, metaVar="<filename>")
	private String s_headruleFile;
	@Option(name="-m", usage="merge labels (default: null)", required=false, metaVar="<string>")
	private String s_mergeLabels = null;
	@Option(name="-n", usage="if set, normalize empty category indices", required=false, metaVar="<boolean>")
	private boolean b_normalize = false;

	public C2DConvert() {}
	
	public C2DConvert(String[] args) throws Exception
	{
		initArgs(args);
		
		AbstractC2DConverter c2d = NLPGetter.getC2DConverter(s_language, s_headruleFile, s_mergeLabels);
		AbstractComponent  morph = NLPGetter.getMPAnalyzer(s_language);
		List<String[]> filenames = getFilenames(s_inputPath, s_inputExt, s_outputExt);
		int n;
		
		for (String[] io : filenames)
		{
			n = convert(c2d, morph, s_language, io[0], io[1], b_normalize);
			System.out.printf("%s: %d trees\n", io[0], n);
		}
	}
	
	protected int convert(AbstractC2DConverter c2d, AbstractComponent morph, String language, String inputFile, String outputFile, boolean normalize)
	{
		CTReader  reader = new CTReader(UTInput.createBufferedFileReader(inputFile));
		PrintStream fout = UTOutput.createPrintBufferedFileStream(outputFile);
		CTTree  cTree;
		DEPTree dTree;
		int n;
		
		for (n=0; (cTree = reader.nextTree()) != null; n++)
		{
			if (normalize)	CTLib.normalizeIndices(cTree);
			
			if (language.equals(AbstractReader.LANG_EN))
				CTLibEn.preprocessTree(cTree);
			
			dTree = c2d.toDEPTree(cTree);
			
			if (dTree == null)
			{
				fout.println(getNullTree()+"\n");
			}
			else
			{
				morph.process(dTree);
				fout.println(dTree.toStringDAG()+"\n");
			}
		}
		
		reader.close();
		fout.close();
		
		return n;
	}
	
	private DEPTree getNullTree()
	{
		DEPTree tree = new DEPTree();
		
		DEPNode dummy = new DEPNode(1, "NULL", "NULL", "NULL", new DEPFeat());
		dummy.setHead(tree.get(0), "NULL");
		
		tree.add(dummy);
		tree.initXHeads();
		tree.initSHeads();
		
		return tree;
	}

	public static void main(String[] args)
	{
		try
		{
			new C2DConvert(args);
		}
		catch (Exception e) {e.printStackTrace();}
	}
}
