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

/**
 * @since 1.5.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class PTNumber
{
	static final public Pattern DIGIT_SPAN = Pattern.compile("\\d+");
	static final public Pattern DIGIT_ONLY = Pattern.compile("^\\d+$");
	static final public Pattern DIGIT_LIKE = Pattern.compile("\\d%|\\$\\d|(^|\\d)\\.\\d|\\d,\\d|\\d:\\d|\\d-\\d|\\d\\/\\d");

	/**
	 * Collapses all digit-like characters in the specific word-form to {@code "0"}.
	 * @return the collapsed form.
	 */
	static public String collapseDigits(String form)
	{
		form = DIGIT_LIKE.matcher(form).replaceAll("0");
		return DIGIT_SPAN.matcher(form).replaceAll("0");
	}

	/**
	 * Returns {@code true} if the specific word-form contains only digits.
	 * @param form the word-form to be compared.
	 * @return {@code true} if the specific word-form contains only digits.
	 */
	static public boolean containsOnlyDigits(String form)
	{
		return DIGIT_ONLY.matcher(form).find();
	}
}
