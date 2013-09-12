/**
* Copyright (c) 2009-2012, Regents of the University of Colorado
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
*
* Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
* Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
* Neither the name of the University of Colorado at Boulder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
* AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
* IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
* ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
* LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
* CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
* ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
* POSSIBILITY OF SUCH DAMAGE.
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
