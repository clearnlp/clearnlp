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
package com.clearnlp.util.triple;

import com.clearnlp.util.UTMath;

/**
 * @since 2.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class ObjectsDoubleTriple<T1,T2> implements Comparable<ObjectsDoubleTriple<T1,T2>>
{
	public T1     o1;
	public T2     o2;
	public double d;
	
	public ObjectsDoubleTriple(T1 o1, T2 o2, double d)
	{
		set(o1, o2, d);
	}
	
	public void set(T1 o1, T2 o2, double d)
	{
		this.o1 = o1;
		this.o2 = o2;
		this.d  = d;
	}

	@Override
	public int compareTo(ObjectsDoubleTriple<T1, T2> o)
	{
		return UTMath.signnum(d - o.d);
	}
}
