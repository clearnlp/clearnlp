/**
* Copyright 2013 IPSoft Inc.
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
package com.clearnlp.generation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.clearnlp.constant.universal.UNConstant;
import com.clearnlp.constant.universal.UNPunct;
import com.clearnlp.dictionary.DTEnglish;
import com.clearnlp.morphology.MPLibEn;
import com.clearnlp.util.UTInput;

/**
 * @since 1.4.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class LGVerbEn
{
	final Pattern TENSE_MULTI   = Pattern.compile("\\"+UNPunct.PIPE);
	final Pattern TENSE_BETWEEN = Pattern.compile(UNConstant.TAB);
	
	private Map<String,String> m_vbd, m_vbn;
	
	public LGVerbEn()
	{
		try
		{
			initTenseMap(UTInput.getInputStreamsFromClasspath(DTEnglish.VERB_TENSE));
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
}
