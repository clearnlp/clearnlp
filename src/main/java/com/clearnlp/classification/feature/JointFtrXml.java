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
package com.clearnlp.classification.feature;

import java.io.InputStream;
import java.util.regex.Pattern;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.clearnlp.dependency.DEPNode;
import com.clearnlp.util.UTRegex;
import com.clearnlp.util.UTXml;

/**
 * @since 1.3.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class JointFtrXml extends AbstractFtrXml
{
	private static final long serialVersionUID = 5682913800083291488L;
	
	static public final char S_INPUT	= 'i';
	static public final char S_STACK	= 's';
	static public final char S_LAMBDA	= 'l';
	static public final char S_BETA		= 'b';
	static public final char S_PRED		= 'p';
	static public final char S_ARG		= 'a';
	
	static public final String R_H		= "h";		// head
	static public final String R_H2		= "h2";		// grand-head
	static public final String R_LMD	= "lmd";	// leftmost dependent
	static public final String R_RMD	= "rmd";	// rightmost dependent
	static public final String R_LMD2	= "lmd2";	// leftmost dependent 2
	static public final String R_RMD2	= "rmd2";	// rightmost dependent 2
	static public final String R_LND	= "lnd";	// left-nearest dependent
	static public final String R_RND	= "rnd";	// right-nearest dependent
	static public final String R_LNS	= "lns";	// left-nearest sibling
	static public final String R_RNS	= "rns";	// right-nearest sibling

	static public final String F_FORM					= "f";
	static public final String F_SIMPLIFIED_FORM		= "sf";
	static public final String F_LOWER_SIMPLIFIED_FORM	= "lsf";
	static public final String F_POS					= "p";
	static public final String F_POS2					= "p2";
	static public final String F_POS_SET				= "ps";
	static public final String F_AMBIGUITY_CLASS		= "a";
	static public final String F_LEMMA					= "m";
	static public final String F_NAMENT					= "nt";
	static public final String F_DEPREL					= "d";
	static public final String F_LEFT_DEP_POS			= "ldp";
	static public final String F_RIGHT_DEP_POS			= "rdp";
	static public final String F_LEFT_DEP_DEPREL		= "ldd";
	static public final String F_RIGHT_DEP_DEPREL		= "rdd";
	static public final String F_DIRECTION				= "dir";
	static public final String F_DISTANCE				= "n";
	static public final String F_DEPREL_SET				= "ds";
	static public final String F_GRAND_DEPREL_SET		= "gds";
	static public final String F_LEFT_VALENCY			= "lv";
	static public final String F_RIGHT_VALENCY			= "rv";
	
	static public final Pattern P_BOOLEAN  	= Pattern.compile("^b(\\d+)$");
	static public final Pattern P_PREFIX  	= Pattern.compile("^pf(\\d+)$");
	static public final Pattern P_SUFFIX  	= Pattern.compile("^sf(\\d+)$");
	static public final Pattern P_FEAT		= Pattern.compile("^ft=(.+)$");		
	static public final Pattern P_SUBCAT 	= Pattern.compile("^sc(["+F_POS+F_DEPREL+"])(\\d+)$");
	static public final Pattern P_PATH	 	= Pattern.compile("^pt(["+F_POS+F_DEPREL+F_DISTANCE+"])(\\d+)$");
	static public final Pattern P_ARGN 	 	= Pattern.compile("^argn(\\d+)$");

	static protected final Pattern P_REL	= UTRegex.getORPattern(R_H, R_H2, R_LMD, R_RMD, R_LMD2, R_RMD2, R_LND, R_RND, R_LNS, R_RNS); 
	static protected final Pattern P_FIELD	= UTRegex.getORPattern(F_FORM, F_SIMPLIFIED_FORM, F_LOWER_SIMPLIFIED_FORM, F_LEMMA, F_POS, F_POS2, F_POS_SET, F_AMBIGUITY_CLASS, F_NAMENT, F_DEPREL, F_DIRECTION, F_DISTANCE, F_DEPREL_SET, F_LEFT_VALENCY, F_RIGHT_VALENCY, F_LEFT_DEP_POS, F_RIGHT_DEP_POS, F_LEFT_DEP_DEPREL, F_RIGHT_DEP_DEPREL);
	
	final String CUTOFF_AMBIGUITY			= "ambiguity";	// part-of-speech tagging
	final String CUTOFF_DOCUMENT_FREQUENCY	= "df";			// part-of-speech tagging
	final String CUTOFF_PATH_DOWN			= "down";		// semantic role labeling
	final String CUTOFF_PATH_UP				= "up";			// semantic role labeling
	
	final String LEXICA_PUNCTUATION 	= "punctuation";	// dependency parsing
	final String LEXICA_PREDICATE		= "predicate";		// predicate identification
	
	double			cutoff_ambiguity;	// part-of-speech tagging
	int				cutoff_df;			// part-of-speech tagging
	int				cutoff_pathDown;	// semantic role labeling
	int				cutoff_pathUp;		// semantic role labeling
	Pattern			p_predicates;		// predicate identification
	
	public JointFtrXml(InputStream in)
	{
		super(in);
	}
	
	/** For part-of-speech tagging. */
	public double getAmbiguityClassThreshold()
	{
		return cutoff_ambiguity;
	}
	
	/** For part-of-speech tagging. */
	public int getDocumentFrequencyCutoff()
	{
		return cutoff_df; 
	}
	
	/** Semantic role labeling. */
	public int getPathDownCutoff()
	{
		return cutoff_pathDown;
	}
	
	/** Semantic role labeling. */
	public int getPathUpCutoff()
	{
		return cutoff_pathUp;
	}
	
	/** For predicate identification. */
	public boolean isPredicate(DEPNode node)
	{
		return p_predicates.matcher(node.pos).find();
	}
	
	@Override
	protected void initCutoffMore(NodeList eList)
	{
		Element eCutoff = (Element)eList.item(0);
		
		cutoff_ambiguity = eCutoff.hasAttribute(CUTOFF_AMBIGUITY) ? Double.parseDouble(eCutoff.getAttribute(CUTOFF_AMBIGUITY)) : 0d;
		cutoff_df = eCutoff.hasAttribute(CUTOFF_DOCUMENT_FREQUENCY) ? Integer.parseInt(eCutoff.getAttribute(CUTOFF_DOCUMENT_FREQUENCY)) : 0;
		cutoff_pathDown = eCutoff.hasAttribute(CUTOFF_PATH_DOWN) ? Integer.parseInt(eCutoff.getAttribute(CUTOFF_PATH_DOWN)) : 0;
		cutoff_pathUp = eCutoff.hasAttribute(CUTOFF_PATH_UP)   ? Integer.parseInt(eCutoff.getAttribute(CUTOFF_PATH_UP)) : 0;
	}
	
	@Override
	protected void initMore(Document doc) throws Exception
	{
		initMoreLexica(doc);
	}
	
	/** Called by {@link JointFtrXml#initMore(Document)}. */
	private void initMoreLexica(Document doc)
	{
		NodeList eList = doc.getElementsByTagName(XML_LEXICA);
		int i, size = eList.getLength();
		String type, label;
		Element eLexica;
		
		for (i=0; i<size; i++)
		{
			eLexica = (Element)eList.item(i);
			type    = UTXml.getTrimmedAttribute(eLexica, XML_TYPE);
			label   = UTXml.getTrimmedAttribute(eLexica, XML_LABEL);
			
			if (type.equals(LEXICA_PREDICATE))
				p_predicates = Pattern.compile("^"+label+"$");
		}
	}
	
	@Override
	protected boolean validSource(char source)
	{
		return source == S_INPUT || source == S_STACK || source == S_LAMBDA || source == S_BETA || source == S_PRED || source == S_ARG;
	}

	@Override
	protected boolean validRelation(String relation)
	{
		return P_REL.matcher(relation).matches();
	}
	
	protected boolean validField(String field)
	{
		return P_FIELD  .matcher(field).matches() ||
			   P_BOOLEAN.matcher(field).matches() ||
			   P_PREFIX .matcher(field).matches() ||
			   P_SUFFIX .matcher(field).matches() ||
			   P_FEAT   .matcher(field).matches() ||
			   P_SUBCAT .matcher(field).matches() ||
			   P_PATH   .matcher(field).matches() ||
			   P_ARGN   .matcher(field).matches();
	}
}
