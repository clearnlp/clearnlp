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
package com.clearnlp.nlp.decode;

import java.util.List;

import com.clearnlp.nlp.NLPLib;
import com.clearnlp.reader.AbstractReader;
import com.google.common.collect.Lists;

/**
 * @since 2.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class SRLDecoder extends NLPDecoder
{
	@Override
	protected List<String> getModes(String readerType)
	{
		List<String> modes = Lists.newArrayList();
		
		if (isRawLineTok(readerType))
		{
			modes.add(NLPLib.MODE_POS);
			modes.add(NLPLib.MODE_MORPH);
			modes.add(NLPLib.MODE_DEP);
		}
		else if (readerType.equals(AbstractReader.TYPE_POS))
		{
			modes.add(NLPLib.MODE_MORPH);
			modes.add(NLPLib.MODE_DEP);
		}
		else if (readerType.equals(AbstractReader.TYPE_MORPH))
		{
			modes.add(NLPLib.MODE_DEP);
		}
		
		modes.add(NLPLib.MODE_PRED);
		modes.add(NLPLib.MODE_ROLE);
		modes.add(NLPLib.MODE_SRL);
		
		return modes;
	}
	
	@Override
	public String getMode()
	{
		return NLPLib.MODE_SRL;
	}
}
