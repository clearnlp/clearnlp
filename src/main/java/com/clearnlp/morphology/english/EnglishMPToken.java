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
package com.clearnlp.morphology.english;

import com.clearnlp.morphology.DefaultMPToken;
import com.clearnlp.morphology.Morpheme;


/**
 * @since 2.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class EnglishMPToken extends DefaultMPToken
{
	private Morpheme base_morpheme;
	private Morpheme inflection_suffix;
	private Morpheme derivation_suffix;
	
	public EnglishMPToken()
	{
		super();
	}
	
	public EnglishMPToken(Morpheme baseMorpheme)
	{
		super();
		
		base_morpheme = baseMorpheme;
		addLast(baseMorpheme);
	}
	
	public EnglishMPToken(Morpheme baseMorpheme, Morpheme inflectionSuffix)
	{
		super();
		
		base_morpheme     = baseMorpheme;
		inflection_suffix = inflectionSuffix;
		
		addLast(baseMorpheme);
		addLast(inflectionSuffix);
	}
	
	public Morpheme getBaseMorpheme()
	{
		return base_morpheme;
	}
	
	public Morpheme getInflectionMorpheme()
	{
		return inflection_suffix;
	}
	
	public Morpheme getDerivationMorpheme()
	{
		return derivation_suffix;
	}
}
