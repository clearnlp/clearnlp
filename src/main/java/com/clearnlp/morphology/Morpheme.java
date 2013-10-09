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
package com.clearnlp.morphology;

import com.clearnlp.constant.universal.UNPunct;

/**
 * @since 2.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class Morpheme
{
	public String form;
	public String pos;
	
	public Morpheme(String form, String pos)
	{
		this.form = form;
		this.pos  = pos;
	}
	
	public String getForm()
	{
		return form;
	}
	
	public String getPOS()
	{
		return pos;
	}
	
	public boolean isForm(String form)
	{
		return this.form.equals(form);
	}
	
	public boolean isPOS(String pos)
	{
		return this.pos.equals(pos);
	}
	
	public String toString()
	{
		StringBuilder build = new StringBuilder();
		
		build.append(form);
		build.append(UNPunct.FORWARD_SLASH);
		build.append(pos);
		
		return build.toString();
	}
}
