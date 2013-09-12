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
package com.clearnlp.constant.english;


/**
 * @since 1.4.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class ENPronoun
{
	static public final String YOU		= "you";
	static public final String YOUR		= "your";
	static public final String YOURS	= "yours";
	static public final String YOURSELF	= "yourself";
	
	static public final String I		= "I";
	static public final String ME		= "me";
	static public final String MY		= "my";
	static public final String MINE		= "mine";
	static public final String MYSELF	= "myself";
	
	/** @param lemma a lower-case string. */
	static public boolean is1stSingular(String lemma)
	{
		return I.equals(lemma) || ME.equals(lemma) || MY.equals(lemma) || MINE.equals(lemma) || MYSELF.equals(lemma);
	}
}
