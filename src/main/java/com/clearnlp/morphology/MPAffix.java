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
package com.clearnlp.morphology;

/**
 * @since 1.5.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class MPAffix
{
	private Morpheme m_affix;
	private String   s_affixForm;
	private String[] s_replacements;
	private boolean  b_doubleConsonant;
	
	public MPAffix(Morpheme affix, String form)
	{
		init(affix, form, null, false);
	}
	
	public MPAffix(Morpheme affix, String form, String[] replacements)
	{
		init(affix, form, replacements, false);
	}
	
	public MPAffix(Morpheme affix, String form, String[] replacements, boolean doubleConsonant)
	{
		init(affix, form, replacements, doubleConsonant);
	}
	
	public void init(Morpheme affix, String form, String[] replacements, boolean doubleConsonant)
	{
		this.m_affix = affix;
		this.s_affixForm  = form;
		this.s_replacements = replacements;
		this.b_doubleConsonant = doubleConsonant;
	}
	
	public String getStemFromSuffix(String form)
	{
		int aLen = s_affixForm.length(), subLen = form.length() - aLen;
		
		if (form.endsWith(s_affixForm))
		{
			if (b_doubleConsonant && subLen >= 3 && form.charAt(subLen-2) == form.charAt(subLen-1))
				return form.substring(0, subLen-1);
			
			return form.substring(0, subLen);
		}
		
		return null;
	}
	
	public boolean isSuffix(String str)
	{
		return str.endsWith(s_affixForm);
	}
	
	public Morpheme getAffixMorpheme()
	{
		return m_affix;
	}
	
	public String[] getReplacements()
	{
		return s_replacements;
	}
}
