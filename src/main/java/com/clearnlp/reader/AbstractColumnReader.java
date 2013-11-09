/**
 * Copyright (c) 2009/09-2012/08, Regents of the University of Colorado
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/**
 * Copyright 2012/09-2013/04, 2013/11-Present, University of Massachusetts Amherst
 * Copyright 2013/05-2013/10, IPSoft Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import java.util.regex.Pattern;

/**
 * Abstract column reader.
 * @since 1.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
abstract public class AbstractColumnReader<T> extends AbstractReader<T>
{
	/** The indicator of a blank column ({@code "_"}). */
	static public final String BLANK_COLUMN		= "_";
	/** The delimiter between columns ({@code "\t"}). */
	static public final String DELIM_COLUMN		= "\t";
	/** The delimiter between sentences ({@code "\n"}). */
	static public final String DELIM_SENTENCE	= "\n";
	
	static public final String FIELD_ID		= "id";
	static public final String FIELD_FORM	= "form";
	static public final String FIELD_LEMMA	= "lemma";
	static public final String FIELD_POS 	= "pos";
	static public final String FIELD_FEATS 	= "feats";
	static public final String FIELD_HEADID	= "headId";
	static public final String FIELD_DEPREL	= "deprel";
	static public final String FIELD_SHEADS	= "sheads";
	static public final String FIELD_XHEADS	= "xheads";
	static public final String FIELD_NAMENT	= "nament";
	static public final String FIELD_COREF	= "coref";
	static public final String FIELD_GPOS	= "gpos";
	
	private final Pattern P_COLUMN = Pattern.compile(DELIM_COLUMN);
	
	/** Returns the next batch of lines. */
	protected List<String[]> readLines() throws Exception
	{
		// skip empty lines
		String line;
		
		while ((line = f_in.readLine()) != null)
			if (!isSkip(line))	break;

		// the end of the line
		if (line == null)
		{	close();	return null;	}
		
		// add lines
		List<String[]> list = new ArrayList<String[]>();
		list.add(P_COLUMN.split(line));
		
		while ((line = f_in.readLine()) != null)
		{
			if (isSkip(line))
				return list;
			else
				list.add(line.split(DELIM_COLUMN));
		}

		return list;
	}
	
	/** Called by {@link AbstractColumnReader#readLines()}. */
	protected boolean isSkip(String line)
	{
		return line.trim().isEmpty();
	}
}
