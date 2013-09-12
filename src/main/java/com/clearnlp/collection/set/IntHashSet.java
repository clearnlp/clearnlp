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
package com.clearnlp.collection.set;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.carrotsearch.hppc.IntOpenHashSet;

/**
 * @since 1.5.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class IntHashSet extends IntOpenHashSet implements Serializable
{
	private static final long serialVersionUID = 9191549668736463520L;

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		addAll((int[])in.readObject());
	}

	private void writeObject(ObjectOutputStream o) throws IOException
	{
		o.writeObject(toArray());
	}
	
	public void addAll(int[] array)
	{
		for (int item : array)
			add(item);
	}
}
