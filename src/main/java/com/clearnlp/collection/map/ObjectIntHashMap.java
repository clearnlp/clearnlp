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
package com.clearnlp.collection.map;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;

import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import com.carrotsearch.hppc.cursors.ObjectCursor;
import com.clearnlp.util.pair.ObjectIntPair;
import com.google.common.collect.Lists;

/**
 * @since 1.4.2
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class ObjectIntHashMap<T> extends ObjectIntOpenHashMap<T> implements Serializable
{
	private static final long serialVersionUID = 7069282868294267610L;
	
	@SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		put((List<ObjectIntPair<T>>)in.readObject());
	}

	private void writeObject(ObjectOutputStream o) throws IOException
	{
		o.writeObject(toList());
	}
	
	/** @return a list of (key, value) pairs. */
	public List<ObjectIntPair<T>> toList()
	{
		List<ObjectIntPair<T>> list = Lists.newArrayList();
		
		for (ObjectCursor<T> cur : keys())
			list.add(new ObjectIntPair<T>(cur.value, get(cur.value)));

		return list;
	}
	
	/** Puts a the list of (key, value) pairs to this map. */
	@SuppressWarnings("unchecked")
	public void put(List<ObjectIntPair<T>> list)
	{
		for (ObjectIntPair<T> p : list)
			put((T)p.o, p.i);
	}
}
