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
package com.clearnlp.dependency;

public class DEPCountArc implements Comparable<DEPCountArc>
{
	public int count, order, depId, headId;
	public String deprel;
	
	public DEPCountArc(int count, int order, int depId, int headId, String deprel)
	{
		this.count  = count;
		this.order  = order;
		this.depId  = depId;
		this.headId = headId;
		this.deprel = deprel;
	}
	
	@Override
	public int compareTo(DEPCountArc p)
	{
		int n = p.count - count;
		return (n == 0) ? order - p.order : n;
	}
}