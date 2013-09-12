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
package com.clearnlp.reader;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 1.1.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class TOKReader extends AbstractColumnReader<List<String>>
{
	private int i_form;
	
	/**
	 * Constructs a token reader.
	 * @param iForm the column index of the word-form field.
	 */
	public TOKReader(int iForm)
	{
		init(iForm);
	}
	
	/**
	 * Initializes column indexes of fields.
	 * @param iForm the column index of the form field.
	 */
	public void init(int iForm)
	{
		i_form = iForm;
	}
	
	@Override
	public List<String> next()
	{
		List<String> tokens = null;
		
		try
		{
			List<String[]> lines = readLines();
			if (lines == null)	return null;
			
			int i, size = lines.size();
			String[] tmp;
			
			tokens = new ArrayList<String>(size);
			
			for (i=0; i<size; i++)
			{
				tmp = lines.get(i);
				tokens.add(tmp[i_form]);
			}
		}
		catch (Exception e) {e.printStackTrace();}
		
		return tokens;
	}
	
	public String getType()
	{
		return TYPE_TOK;
	}
}
