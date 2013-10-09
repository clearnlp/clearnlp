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
package com.clearnlp.pattern;

import java.util.regex.Pattern;

import com.clearnlp.constant.universal.UNConstant;
import com.clearnlp.constant.universal.UNPunct;

/**
 * @since 1.4.2
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class PTLib
{
	static final public Pattern COMMA        = Pattern.compile(UNPunct.COMMA);
	static final public Pattern SPACE        = Pattern.compile(UNConstant.SPACE);
	static final public Pattern TAB          = Pattern.compile(UNConstant.TAB);
	static final public Pattern UNDERSCORE   = Pattern.compile(UNPunct.UNDERSCORE);
	static final public Pattern WHITE_SPACES = Pattern.compile("\\s+");
	
	static public String[] split(String s, Pattern p)
	{
		return p.split(s);
	}
	
	static public String[] splitSpace(String s)
	{
		return split(s, SPACE);
	}
	
	static public String[] splitUnderscore(String s)
	{
		return split(s, UNDERSCORE);
	}

	static public String[] splitWhiteSpaces(String s)
	{
		return split(s, WHITE_SPACES);
	}
	
	static public String[] splitTabs(String s)
	{
		return split(s, TAB);
	}
	
	static public String[] splitCommas(String s)
	{
		return split(s, COMMA);
	}
}
