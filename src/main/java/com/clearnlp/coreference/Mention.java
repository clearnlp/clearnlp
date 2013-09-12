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
package com.clearnlp.coreference;

public class Mention
{
	public String id;
	public String type;
	public int    beginIndex;
	public int    endIndex;

	public Mention(String id, int beginIndex, int endIndex)
	{
		init(id, null, beginIndex, endIndex);
	}
	
	public Mention(String id, String type, int beginIndex, int endIndex)
	{
		init(id, type, beginIndex, endIndex);
	}
	
	public void init(String id, String type, int beginIndex, int endIndex)
	{
		this.id         = id;
		this.type       = type;
		this.beginIndex = beginIndex;
		this.endIndex   = endIndex;
	}
}
