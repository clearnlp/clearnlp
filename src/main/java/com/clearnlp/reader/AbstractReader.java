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

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Abstract reader.
 * @since 1.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
abstract public class AbstractReader<T>
{
	/** The flag for Arabic. */
	static final public String LANG_AR = "ar";
	/** The flag for Chinese. */
	static final public String LANG_CH = "ch";
	/** The flag for English. */
	static final public String LANG_EN = "en";
	/** The flag for Hindi. */
	static final public String LANG_HI = "hi";
	/** The flag for Korean. */
	static final public String LANG_KR = "ko";
	
	/** The flag for raw-text reader. */
	static final public String TYPE_RAW = "raw";
	/** The flag for sentence reader. */
	static final public String TYPE_LINE = "line";
	/** The flag for token reader. */
	static final public String TYPE_TOK = "tok";
	/** The flag for part-of-speech reader. */
	static final public String TYPE_POS = "pos";
	/** The flag for morphological analyzer. */
	static final public String TYPE_MORPH = "morph";
	/** The flag for dependency reader. */
	static final public String TYPE_DEP = "dep";
	/** The flag for semantic role label reader. */
	static final public String TYPE_SRL = "srl";
	/** The flag for directed acyclic graph reader. */
	static final public String TYPE_DAG = "dag";

	/** The dummy tag for any field. */
	static public final String DUMMY_TAG = "_N_";
	
	protected BufferedReader f_in;
	
	/**
	 * Initializes this reader with the specific reader.
	 * @param reader the reader to be initialized.
	 */
	public void open(BufferedReader reader)
	{
		f_in = reader;
	}
	
	public BufferedReader getBufferedReader()
	{
		return f_in;
	}

	/** Closes this reader. */
	public void close()
	{
		try
		{
			f_in.close();
		}
		catch (IOException e) {e.printStackTrace();}
	}
	
	/** @return the next object of this reader. */
	abstract public T next();
	
	/** @return the type of this reader. */
	abstract public String getType();
}
