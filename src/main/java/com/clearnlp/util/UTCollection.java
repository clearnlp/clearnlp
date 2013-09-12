/**
* Copyright 2012-2013 University of Massachusetts Amherst
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
package com.clearnlp.util;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class UTCollection
{
	static public <T extends Comparable<? extends T>>void sortReverseOrder(List<T> list)
	{
		Collections.sort(list, Collections.reverseOrder());
	}
	
	static public String[] toArray(Collection<String> col)
	{
		String[] array = new String[col.size()];
		
		col.toArray(array);
		return array;
	}
	
	static public String toString(Collection<String> col, String delim)
	{
		StringBuilder build = new StringBuilder();
		
		for (String item : col)
		{
			build.append(delim);
			build.append(item);
		}
		
		return build.substring(delim.length());
	}
}
