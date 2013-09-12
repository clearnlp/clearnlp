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
package com.clearnlp.util;

import java.util.Collection;
import java.util.List;

import com.google.common.base.Supplier;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;

/**
 * @since 1.4.2
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class UTGuava
{
	static public <K,V>ListMultimap<K,V> getArrayListMultiHashMap()
	{
		ListMultimap<K,V> map = Multimaps.newListMultimap(Maps.<K,Collection<V>>newHashMap(), new Supplier<List<V>>()
		{
			public List<V> get() {return Lists.newArrayList();}
		});
		
		return map;
	}
}
