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
package com.clearnlp.pos;

import com.clearnlp.reader.AbstractColumnReader;
import com.clearnlp.reader.AbstractReader;

/**
 * Part-of-speech node.
 * @since v0.1
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class POSNode
{
	/** The word-form of this node. */
	public String form;
	/** The simplified form of the word-form (used for POS tagging). */
	public String simplifiedForm;
	/** The lowercase simplified form of the word-form (used for POS tagging). */
	public String lowerSimplifiedForm;
	/** The part-of-speech tag of the word-form. */
	public String pos;
	/** The lemma of the word-form. */
	public String lemma;
	
	/** Constructs a POS node with dummy values ({@link AbstractReader#DUMMY_TAG}). */
	public POSNode()
	{
		init(AbstractReader.DUMMY_TAG, AbstractReader.DUMMY_TAG, AbstractReader.DUMMY_TAG);
	}
	
	/**
	 * Constructs a POS node with the specific word-form.
	 * @param form the word-form.
	 */
	public POSNode(String form)
	{
		init(form, AbstractReader.DUMMY_TAG, AbstractReader.DUMMY_TAG);
	}
	
	/**
	 * Constructs a POS node with the specific word-form and part-of-speech tag.
	 * @param form the word-form.
	 * @param pos the part-of-speech tag.
	 */
	public POSNode(String form, String pos)
	{
		init(form, pos, AbstractReader.DUMMY_TAG);
	}
		
	/**
	 * Constructs a POS node with the specific word-form, part-of-speech tag, and lemma.
	 * @param form the word-form.
	 * @param pos the part-of-speech tag.
	 * @param lemma the lemma.
	 */
	public POSNode(String form, String pos, String lemma)
	{
		init(form, pos, lemma);
	}
	
	/**
	 * Initializes the word-form, part-of-speech tag, and lemma of this node.
	 * @param form the word-form.
	 * @param pos the part-of-speech tag.
	 * @param lemma the lemma.
	 */
	public void init(String form, String pos, String lemma)
	{
		this.form  = form;
		this.pos   = pos;
		this.lemma = lemma;
		
		simplifiedForm      = AbstractReader.DUMMY_TAG;
		lowerSimplifiedForm = AbstractReader.DUMMY_TAG;
	}
	
	/**
	 * Returns {@code true} if this node's word-form equals to the specific form.
	 * @param form the word-form to be compared.
	 * @return {@code true} if this node's word-form equals to the specific form.
	 */
	public boolean isForm(String form)
	{
		return this.form.equals(form);
	}
	
	/**
	 * Returns {@code true} if this node's simplified form equals to the specific form.
	 * @param form the form to be compared.
	 * @return {@code true} if this node's simplified form equals to the specific form.
	 */
	public boolean isSimplifiedForm(String form)
	{
		return this.simplifiedForm.equals(form);
	}
	
	/**
	 * Returns {@code true} if this node's POS tag equals to the specific tag.
	 * @param pos the POS tag to be compared.
	 * @return {@code true} if this node's POS tag equals to the specific tag.
	 */
	public boolean isPos(String pos)
	{
		return this.pos.equals(pos);
	}
	
	public boolean isPosAny(String... posTags)
	{
		for (String pos : posTags)
		{
			if (this.pos.equals(pos))
				return true;
		}
		
		return false;
	}
	
	/**
	 * Returns {@code true} if this node's word-lemma equals to the specific lemma.
	 * @param lemma the word-lemma to be compared.
	 * @return {@code true} if this node's word-lemma equals to the specific lemma.
	 */
	public boolean isLemma(String lemma)
	{
		return this.lemma.equals(lemma);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		StringBuilder build = new StringBuilder();
		
		build.append(form);	build.append(AbstractColumnReader.DELIM_COLUMN);
		build.append(pos);
		
		return build.toString();
	}
	
	public String toString(boolean includeLemma)
	{
		StringBuilder build = new StringBuilder();
		
		build.append(form);
		
		build.append(AbstractColumnReader.DELIM_COLUMN);
		build.append(pos);
		
		if (includeLemma)
		{
			build.append(AbstractColumnReader.DELIM_COLUMN);
			build.append(lemma);
		}
		
		return build.toString();		
	}
}
