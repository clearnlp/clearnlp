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
package com.clearnlp.generation;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.clearnlp.constant.universal.UNConstant;
import com.clearnlp.constant.universal.UNPunct;
import com.clearnlp.morphology.MPLibEn;
import com.clearnlp.pattern.PTLib;
import com.clearnlp.util.UTInput;
import com.google.common.collect.Lists;

/**
 * @since 1.4.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class LGVerbEn
{
	final String  VERB_TENSE    = "dictionary/english/verb.tense";
	final Pattern TENSE_MULTI   = Pattern.compile("\\"+UNPunct.PIPE);
	final Pattern TENSE_BETWEEN = Pattern.compile(UNConstant.TAB);
	
	private Map<String,String> m_vbd, m_vbn;
	
	public LGVerbEn()
	{
		try
		{
			initTenseMap(UTInput.getInputStreamsFromClasspath(VERB_TENSE));
		}
		catch (IOException e) {e.printStackTrace();}
	}
	
	public LGVerbEn(ZipFile file)
	{
		try
		{
			initTenseMap(file.getInputStream(new ZipEntry(VERB_TENSE)));
		}
		catch (IOException e) {e.printStackTrace();}
	}
	
	private void initTenseMap(InputStream inputStream) throws IOException
	{
		BufferedReader fin = new BufferedReader(new InputStreamReader(inputStream));
		String line, base, past, part;
		String[] t;
		
		m_vbd = new HashMap<String,String>();
		m_vbn = new HashMap<String,String>();
		
		while ((line = fin.readLine()) != null)
		{
			t = TENSE_BETWEEN.split(line);
			base = t[0];
			past = t[1];
			part = t[2];
			
			m_vbd.put(base, past);
			m_vbn.put(base, part);
		}
	}
	
	public String getPastForm(String baseForm)
	{
		return getPastFormAux(baseForm, m_vbd);
	}
	
	public String getPastParticipleForm(String baseForm)
	{
		return getPastFormAux(baseForm, m_vbn);
	}
	
	private String getPastFormAux(String baseForm, Map<String,String> map)
	{
		String past = map.get(baseForm);
		return (past != null) ? past : getPastRegularForm(baseForm);
	}
	
	static public String getPastRegularForm(String baseForm)
	{
		if (baseForm.endsWith("e"))
			return baseForm+"d";
		
		if (baseForm.endsWith("y"))
		{
			int len = baseForm.length();
			
			if (len-2 >= 0 && MPLibEn.isVowel(baseForm.charAt(len-2)))
				return baseForm+"ed";
			else
				return baseForm.substring(0, len-1)+"ied";
		}
		
		return baseForm+"ed";
	}
	
	static public String get3rdSingularForm(String baseForm)
	{
		if (baseForm.equals("be"))
			return "is";
		
		if (baseForm.equals("have"))
			return "has";
		
		if (baseForm.endsWith("y"))
		{
			int len = baseForm.length();
			
			if (len-2 >= 0 && MPLibEn.isVowel(baseForm.charAt(len-2)))
				return baseForm+"s";
			else
				return baseForm.substring(0, len-1)+"ies";
		}
		
		if (baseForm.endsWith("ch") || baseForm.endsWith("sh") || baseForm.endsWith("s") || baseForm.endsWith("z") || baseForm.endsWith("x") || baseForm.endsWith("o"))
			return baseForm+"es"; 
		
		return baseForm+"s";
	}
	
	public void addVerbs(InputStream in) throws IOException
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String line, vb, vbd, vbn;
		String[] tmp;
		
		while ((line = reader.readLine()) != null)
		{
			tmp = PTLib.splitTabs(line);
			vb  = tmp[0];
			
			if (m_vbd.containsKey(vb))
				continue;
			
			vbd = tmp[1];
			vbn = tmp[2];

			if (vbd.equals(getPastForm(vb)) && vbn.equals(getPastParticipleForm(vb)))
				continue;
			
			m_vbd.put(vb, vbd);
			m_vbn.put(vb, vbn);
		}
		
		reader.close();
	}
	
	public void printVerbs(OutputStream out)
	{
		PrintStream fout = new PrintStream(new BufferedOutputStream(out));
		List<String> verbs = Lists.newArrayList(m_vbd.keySet());
		Collections.sort(verbs);
		
		for (String vb : verbs)
			fout.printf("%s\t%s\t%s\n", vb, m_vbd.get(vb), m_vbn.get(vb));
		
		fout.close();
	}
	
	static public void main(String[] args) throws IOException
	{
		LGVerbEn lgv = new LGVerbEn();
		
		lgv.addVerbs(new FileInputStream(args[0]));
		lgv.printVerbs(new FileOutputStream(args[1]));
	}
}
