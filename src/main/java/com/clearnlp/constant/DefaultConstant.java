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
package com.clearnlp.constant;

import java.lang.reflect.Field;
import java.util.Set;

import com.google.common.collect.Sets;

/**
 * @since 1.4.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class DefaultConstant
{
	private Set<String> VALUE_SET;
	
	public DefaultConstant()
	{
		VALUE_SET = initValueSet();
	}
	
	public Set<String> getValueSet()
	{
		return VALUE_SET;
	}
	
	/**
	 * @param lemma a lower-case string.
	 * @return {@code true} if this class contains the specific lemma; otherwise, {@code false}.
	 */
	public boolean contains(String lemma)
	{
		return VALUE_SET.contains(lemma);
	}
	
	/** @return a set containing all field values of this class. */
	private Set<String> initValueSet()
	{
		Set<String> set = Sets.newHashSet();
		Class<?> cs = getThisClass();
		
		try
		{
			for (Field f : cs.getFields())
				set.add(f.get(cs).toString());
		}
		catch (IllegalArgumentException e) {e.printStackTrace();}
		catch (IllegalAccessException e)   {e.printStackTrace();}
		
		return set;
	}
	
	final protected Class<?> getThisClass()
	{
		return this.getClass();
	}
}
