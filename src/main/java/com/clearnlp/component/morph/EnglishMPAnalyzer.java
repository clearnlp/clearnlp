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
package com.clearnlp.component.morph;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.clearnlp.dictionary.DTEnglish;
import com.clearnlp.morphology.MPAffix;
import com.clearnlp.morphology.MPLib;
import com.clearnlp.morphology.MPLibEn;
import com.clearnlp.morphology.MPTag;
import com.clearnlp.morphology.Morpheme;
import com.clearnlp.pattern.PTLib;
import com.clearnlp.util.UTInput;
import com.clearnlp.util.UTOutput;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * English morphological analyzer.
 * @since 1.3.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class EnglishMPAnalyzer extends AbstractMPAnalyzer
{
	final String FIELD_DELIM   = "_";
	final String POS_NOUN      = "N";
	final String POS_VERB      = "V";
	final String POS_ADJECTIVE = "J";
	final String POS_ADVERB    = "R";
	
	/** Noun inflection rules. */
	private List<MPAffix> inf_noun;
	/** Verb inflection rules. */
	private List<MPAffix> inf_verb;
	/** Adjective inflection rules. */
	private List<MPAffix> inf_adjective;
	/** Adverb inflection rules. */
	private List<MPAffix> inf_adverb;
	/** Cardinal inflection rules. */
	private List<MPAffix> inf_cardinal;
	
	/** Noun derivation rules. */
	private List<MPAffix> dev_noun;
	/** Verb derivation rules. */
	private List<MPAffix> dev_verb;
	/** Adjective derivation rules. */
//	private List<MPAffix> dev_adjective;
	/** Adverb derivation rules. */
//	private List<MPAffix> dev_adverb;
	
	/** Noun exceptions */
	private Map<String,String> m_noun_exc;
	/** Verb exceptions */
	private Map<String,String> m_verb_exc;
	/** Adjective exceptions */
	private Map<String,String> m_adj_exc;
	/** Adverb exceptions */
	private Map<String,String> m_adv_exc;
	
	/** Noun base-forms */
	private Set<String> s_noun_base;
	/** Verb base-forms */
	private Set<String> s_verb_base;
	/** Adjective base-forms */
	private Set<String> s_adj_base;
	/** Adverb base-forms */
	private Set<String> s_adv_base;
	
	/** Cardinal base-forms */
	private Set<String> s_cardinal;
	/** Ordinal base-forms */
	private Set<String> s_ordinal;

	/** Abbreviation replacement rules */
	Map<String,String> m_abbr_rule;
	
//	====================================== CONSTRUCTORS ======================================
	
	/** Constructs an English morphological analyzer from the dictionary in a classpath. */
	public EnglishMPAnalyzer()
	{
		initInflections();
		initDerivations();
		initDictionaries();
	}
	
	private void initInflections()
	{
		inf_noun = Lists.newArrayList();
		
		inf_noun.add(new MPAffix(new Morpheme("s", MPTag.ISS), "ices", new String[]{"ex"}));
		inf_noun.add(new MPAffix(new Morpheme("s", MPTag.ISS), "ces" , new String[]{"x"}));
		inf_noun.add(new MPAffix(new Morpheme("s", MPTag.ISS), "ies" , new String[]{"y"}));
		inf_noun.add(new MPAffix(new Morpheme("s", MPTag.ISS), "ves" , new String[]{"f","fe"}));
		inf_noun.add(new MPAffix(new Morpheme("s", MPTag.ISS), "es"  , new String[]{""}));
		inf_noun.add(new MPAffix(new Morpheme("s", MPTag.ISS), "es"  , new String[]{""}, true));
		inf_noun.add(new MPAffix(new Morpheme("s", MPTag.ISS), "s"   , new String[]{""}));
		
		inf_noun.add(new MPAffix(new Morpheme("e", MPTag.ISS), "men" , new String[]{"man"}));
		inf_noun.add(new MPAffix(new Morpheme("e", MPTag.ISS), "ouse", new String[]{"ice"}));
		inf_noun.add(new MPAffix(new Morpheme("e", MPTag.ISS), "eese", new String[]{"oose"}));
		inf_noun.add(new MPAffix(new Morpheme("e", MPTag.ISS), "eeth", new String[]{"ooth"}));
		inf_noun.add(new MPAffix(new Morpheme("e", MPTag.ISS), "eet" , new String[]{"oot"}));
		inf_noun.add(new MPAffix(new Morpheme("e", MPTag.ISS), "es"  , new String[]{"is"}));
		
		inf_noun.add(new MPAffix(new Morpheme(MPTag.LATIN, MPTag.ISS), "ae", new String[]{"e","a"}));
		inf_noun.add(new MPAffix(new Morpheme(MPTag.LATIN, MPTag.ISS), "a" , new String[]{"um","on"}));
		inf_noun.add(new MPAffix(new Morpheme(MPTag.LATIN, MPTag.ISS), "i" , new String[]{"us"}));
		
		inf_verb = Lists.newArrayList();
		
		inf_verb.add(new MPAffix(new Morpheme("s", MPTag.ISZ), "ies", new String[]{"y"}));
		inf_verb.add(new MPAffix(new Morpheme("s", MPTag.ISZ), "es" , new String[]{""}));
		inf_verb.add(new MPAffix(new Morpheme("s", MPTag.ISZ), "es" , new String[]{""}, true));
		inf_verb.add(new MPAffix(new Morpheme("s", MPTag.ISZ), "s"  , new String[]{""}));
		
		inf_verb.add(new MPAffix(new Morpheme("g", MPTag.ISG), "ying", new String[]{"ie"}));
		inf_verb.add(new MPAffix(new Morpheme("g", MPTag.ISG), "ing" , new String[]{"","e"}));
		inf_verb.add(new MPAffix(new Morpheme("g", MPTag.ISG), "ing" , new String[]{""}, true));
		
		inf_verb.add(new MPAffix(new Morpheme("d", MPTag.ISD), "ept", new String[]{"eep"}));
		inf_verb.add(new MPAffix(new Morpheme("d", MPTag.ISD), "ied", new String[]{"y"}));
		inf_verb.add(new MPAffix(new Morpheme("d", MPTag.ISD), "ed" , new String[]{"","eed","ead"}));
		inf_verb.add(new MPAffix(new Morpheme("d", MPTag.ISD), "ed" , new String[]{""}, true));
		inf_verb.add(new MPAffix(new Morpheme("d", MPTag.ISD), "t"  , new String[]{"","d"}));
		inf_verb.add(new MPAffix(new Morpheme("d", MPTag.ISD), "d"  , new String[]{""}));
		
		inf_verb.add(new MPAffix(new Morpheme("n", MPTag.ISD), "en", new String[]{""}));
		inf_verb.add(new MPAffix(new Morpheme("n", MPTag.ISD), "en", new String[]{"","e"}, true));
		inf_verb.add(new MPAffix(new Morpheme("n", MPTag.ISD), "ne", new String[]{""}));
		inf_verb.add(new MPAffix(new Morpheme("n", MPTag.ISD), "n" , new String[]{""}));
		inf_verb.add(new MPAffix(new Morpheme("n", MPTag.ISD), "n" , new String[]{""}, true));
		
		inf_verb.add(new MPAffix(new Morpheme("n", MPTag.ISD), "oken", new String[]{"ake","eak"}));
		inf_verb.add(new MPAffix(new Morpheme("n", MPTag.ISD), "oven", new String[]{"eave"}));
		inf_verb.add(new MPAffix(new Morpheme("o", MPTag.ISD), "ode" , new String[]{"ide"}));
		inf_verb.add(new MPAffix(new Morpheme("o", MPTag.ISD), "oke" , new String[]{"ake","eak"}));
		inf_verb.add(new MPAffix(new Morpheme("o", MPTag.ISD), "ote" , new String[]{"ite"}));
		inf_verb.add(new MPAffix(new Morpheme("o", MPTag.ISD), "ore" , new String[]{"ear"}));
		inf_verb.add(new MPAffix(new Morpheme("o", MPTag.ISD), "ove" , new String[]{"ave","ive","eave"}));
		inf_verb.add(new MPAffix(new Morpheme("o", MPTag.ISD), "ung" , new String[]{"ing"}));
		
		inf_adjective = Lists.newArrayList();
		
		inf_adjective.add(new MPAffix(new Morpheme("er", MPTag.ISR), "ier", new String[]{"y"}));
		inf_adjective.add(new MPAffix(new Morpheme("er", MPTag.ISR), "er" , new String[]{"","e"}));
		inf_adjective.add(new MPAffix(new Morpheme("er", MPTag.ISR), "er" , new String[]{"","e"}, true));
		
		inf_adjective.add(new MPAffix(new Morpheme("est", MPTag.ISR), "iest", new String[]{"y"}));
		inf_adjective.add(new MPAffix(new Morpheme("est", MPTag.ISR), "est" , new String[]{"","e"}));
		inf_adjective.add(new MPAffix(new Morpheme("est", MPTag.ISR), "est" , new String[]{"","e"}, true));
		
		inf_adverb = Lists.newArrayList();
		
		inf_adverb.add(new MPAffix(new Morpheme("er", MPTag.ISR), "ier", new String[]{"y","ily"}));
		inf_adverb.add(new MPAffix(new Morpheme("er", MPTag.ISR), "er" , new String[]{"","e","ly"}));
		inf_adverb.add(new MPAffix(new Morpheme("er", MPTag.ISR), "er" , new String[]{"","e","ly"}, true));
		
		inf_adverb.add(new MPAffix(new Morpheme("est", MPTag.ISR), "iest", new String[]{"y","ily"}));
		inf_adverb.add(new MPAffix(new Morpheme("est", MPTag.ISR), "est" , new String[]{"","e","ly"}));
		inf_adverb.add(new MPAffix(new Morpheme("est", MPTag.ISR), "est" , new String[]{"","e","ly"}, true));
		
		inf_cardinal = Lists.newArrayList();
		
		inf_cardinal.add(new MPAffix(new Morpheme("s", MPTag.ISS), "ies", new String[]{"y"}));
		inf_cardinal.add(new MPAffix(new Morpheme("s", MPTag.ISS), "s"  , new String[]{""}));
		
//		inf_verb.add(new MPAffix(new Morpheme("d", MPTag.ISD), "ee"   , new String[]{"eed"}));			// +1
//		inf_verb.add(new MPAffix(new Morpheme("d", MPTag.ISD), "elt"  , new String[]{"eel"}));			// +2
//		inf_verb.add(new MPAffix(new Morpheme("d", MPTag.ISD), "ought", new String[]{"eech","eek"}));	// +2
//		inf_verb.add(new MPAffix(new Morpheme("r", MPTag.ISD), "aw"   , new String[]{"ee"}));			// +4
//		inf_verb.add(new MPAffix(new Morpheme("r", MPTag.ISD), "et"   , new String[]{"eet"}));			// +1
//		inf_verb.add(new MPAffix(new Morpheme("r", MPTag.ISD), "ilgee", new String[]{"eegee"}));		// +1
//		inf_verb.add(new MPAffix(new Morpheme("o", MPTag.ISD), "ang"  , new String[]{"ing"}));			// +4
//		inf_verb.add(new MPAffix(new Morpheme("o", MPTag.ISD), "one"  , new String[]{"ine"}));			// +2
//		inf_verb.add(new MPAffix(new Morpheme("o", MPTag.ISD), "ose"  , new String[]{"ise"}));			// +3
//		inf_verb.add(new MPAffix(new Morpheme("o", MPTag.ISD), "ove"  , new String[]{"eeve"}));			// +1
//		inf_verb.add(new MPAffix(new Morpheme("o", MPTag.ISD), "oze"  , new String[]{"eeze"}));			// +3
//		inf_verb.add(new MPAffix(new Morpheme("o", MPTag.ISD), "ozen" , new String[]{"eeze"}));			// +3
//		inf_verb.add(new MPAffix(new Morpheme("o", MPTag.ISD), "on"   , new String[]{"in"}));			// +1
//		inf_verb.add(new MPAffix(new Morpheme("o", MPTag.ISD), "ot"   , new String[]{"et"}));			// +3
//		inf_verb.add(new MPAffix(new Morpheme("o", MPTag.ISD), "aught", new String[]{"each"}));			// +2
//		inf_verb.add(new MPAffix(new Morpheme("o", MPTag.ISD), "eft"  , new String[]{"eave"}));			// +3
//		inf_verb.add(new MPAffix(new Morpheme("o", MPTag.ISD), "od"   , new String[]{"ead"}));			// +3
//		inf_verb.add(new MPAffix(new Morpheme("o", MPTag.ISD), "odden", new String[]{"ead"}));			// +3
//		inf_verb.add(new MPAffix(new Morpheme("o", MPTag.ISD), "ole"  , new String[]{"eal"}));			// +1
//		inf_verb.add(new MPAffix(new Morpheme("o", MPTag.ISD), "olen" , new String[]{"eal"}));			// +1
//		inf_verb.add(new MPAffix(new Morpheme("o", MPTag.ISD), "orne" , new String[]{"ear"}));			// +3
	}
	
	private void initDerivations()
	{
		dev_noun = Lists.newArrayList();
		
		dev_noun.add(new MPAffix(POS_ADJECTIVE, new Morpheme("ness", MPTag.DSN), "iness", new String[]{"y"}));	// happy+ness
		dev_noun.add(new MPAffix(POS_ADJECTIVE, new Morpheme("ness", MPTag.DSN), "ness" , new String[]{""}));	// use+ful+ness

		
		
		dev_verb = Lists.newArrayList();
		
		dev_verb.add(new MPAffix(POS_ADJECTIVE, new Morpheme("ize", MPTag.DSV), "ize", new String[]{""}));		// normalize
		
//		
//		
//		
//		
//		
//		
//		
//		
//		
//		
//		
//		
//		
//		
//		
//		List<MPAffix> dev_suffixes;
//		
//		dev_suffixes = Lists.newArrayList();
//		
//		
//		
//		dev_suffixes.add(new MPAffix(POS_VERB, "able", "able", new String[]{"","e","ate"}));				// wash+able, move+able, irritate+able
//		dev_suffixes.add(new MPAffix(POS_NOUN, "able", "able", new String[]{""}));						// fashion+able
//		
//		dev_suffixes.add(new MPAffix(POS_NOUN, "ous", "ous" , new String[]{"on","y"}));					// religion+ous, analogy+ous
//		dev_suffixes.add(new MPAffix(POS_NOUN, "ous", "ious", new String[]{"y","ity"}));					// glory+ous, vivace+ity+ous
//		dev_suffixes.add(new MPAffix(POS_NOUN, "ous", "rous", new String[]{"er"}));						// disater+ous
//		
//		dev_suffixes.add(new MPAffix(POS_NOUN, "ant", "ant", new String[]{"ance","ation"}));				// defy+ance+ant, fumigate+tion+ant
//		dev_suffixes.add(new MPAffix(POS_VERB, "ant", "ant", new String[]{"","e","ate"}));				// assist+ant, serve+ant, immigrate+ant
//		
//		dev_suffixes.add(new MPAffix(POS_ADJECTIVE, "ity", "ability", new String[]{"able"}));			// read+able+ity
//		dev_suffixes.add(new MPAffix(POS_ADJECTIVE, "ity", "ity"    , new String[]{"","e"}));			// normal+ity, adverse+ity
//		
//		dev_suffixes.add(new MPAffix(POS_VERB, "tion", "unciation", new String[]{"ounce"}));				// pronounce+tion
//		dev_suffixes.add(new MPAffix(POS_VERB, "tion", "ation"    , new String[]{"","e"}));				// flirt+ation, admire+ation
//		dev_suffixes.add(new MPAffix(POS_VERB, "tion", "tion"     , new String[]{"e","t","te"}));		// introduce+tion, resurrect+tion, alienate+tion
//		dev_suffixes.add(new MPAffix(POS_VERB, "tion", "ication"  , new String[]{"y"}));					// verify+tion
//		dev_suffixes.add(new MPAffix(POS_VERB, "sion", "sion"     , new String[]{"de","se","t","s"}));	// decide+sion, illuse+sion, divert+sion, obsess+sion
//		
//		dev_suffixes.add(new MPAffix(POS_VERB     , "ance", "iance", new String[]{"y"}));				// defy+ance
//		dev_suffixes.add(new MPAffix(POS_VERB     , "ance", "ance" , new String[]{"","e"}));				// annoy+ance, insure+ance
//		dev_suffixes.add(new MPAffix(POS_ADJECTIVE, "ance", "ence" , new String[]{"ent","ential"}));		// difference+ent, difference+ential
//		dev_suffixes.add(new MPAffix(POS_ADJECTIVE, "ance", "ency" , new String[]{"ent"}));				// fluency+ent
//		
//		dev_suffixes.add(new MPAffix(POS_VERB, "sis", "sis", new String[]{"se"}));						// diagnose+sis
//		
//		dev_suffixes.add(new MPAffix(POS_NOUN, "tic", "tic", new String[]{"ia","sis","m"}));				// fantasia+tic, diagnose+sis+tic, atavism+tic
//		
//		dev_suffixes.add(new MPAffix(POS_NOUN, "ic", "ic", new String[]{"","ia","ism","y"}));			// alcohol+ic, academia+ic, barbarism+ic, demagogy + ic 
//		
//		
//		dev_suffixes.add(new MPAffix(POS_ADJECTIVE, "ment", "ment" , new String[]{""}));					// develop+ment
//		
//		dev_suffixes.add(new MPAffix(POS_ADJECTIVE, "ly", "ly"  , new String[]{"","le"}));				// active+ly, invariable+ly
//		dev_suffixes.add(new MPAffix(POS_ADJECTIVE, "ly", "ily" , new String[]{"y"}));					// easy+ly
//		dev_suffixes.add(new MPAffix(POS_ADJECTIVE, "ly", "ally", new String[]{""}));					// electron+ic+ly
//		dev_suffixes.add(new MPAffix(POS_NOUN     , "ly", "ly"  , new String[]{""}));					// month+ly
//		
//		dev_suffixes.add(new MPAffix(POS_NOUN, "less", "less", new String[]{""}));						// use+less
//		
//		dev_suffixes.add(new MPAffix(POS_NOUN, "ful", "iful", new String[]{"y"}));						// beauty+ful
//		dev_suffixes.add(new MPAffix(POS_NOUN, "ful", "ful" , new String[]{""}));						// use+ful
//		
//		dev_suffixes.add(new MPAffix(POS_NOUN, "cal", "ical", new String[]{"y"}));						// academy+cal
//		dev_suffixes.add(new MPAffix(POS_NOUN, "cal", "cal",  new String[]{"c"}));						// critic+cal
//		
//		dev_suffixes.add(new MPAffix(POS_VERB, "er", "er", new String[]{"","e"}));						// read+er, write+er
//		dev_suffixes.add(new MPAffix(POS_VERB, "er", "or", new String[]{"","e"}));						// act+er, sense+er
//		
//		
//		
//		dev_suffixes.add(new MPAffix(POS_VERB, "age", "age" , new String[]{"e"}));	// assemble+age
//		dev_suffixes.add(new MPAffix(POS_VERB, "age", "iage", new String[]{"y"}));	// marry+age
//
//		dev_suffixes.add(new MPAffix(POS_NOUN, "al", "al", new String[]{"","e"}));				// profession+al, universe+al
//		dev_suffixes.add(new MPAffix(POS_VERB, "al", "al", new String[]{"e"}));		// arrive+al
//		
//
//		dev_suffixes.add(new MPAffix(POS_NOUN     , "tial"  , "tial", new String[]{"ce"}));			// influential -> influence
//		dev_suffixes.add(new MPAffix(POS_NOUN     , "t"     , "t"   , new String[]{"ce","cy"}));	// violent -> violence, deficient -> deficiency
//		dev_suffixes.add(new MPAffix(POS_NOUN     , "eal"   , "eal" , new String[]{"is"}));			// diaphyseal -> diaphysis
//		
//		
//		
//		
//		
//		dev_suffixes.add(new MPAffix(POS_ADJECTIVE, "y"  , "ly", new String[]{"","le","l"}));		// sadly -> sad, incredibly -> incredible, fully -> full 
		
	}
	
	private void initDictionaries()
	{
		try
		{
			m_noun_exc  = UTInput.getStringMap(UTInput.getInputStreamsFromClasspath(DTEnglish.NOUN_EXC), PTLib.SPACE);
			m_verb_exc  = UTInput.getStringMap(UTInput.getInputStreamsFromClasspath(DTEnglish.VERB_EXC), PTLib.SPACE);
			m_adj_exc   = UTInput.getStringMap(UTInput.getInputStreamsFromClasspath(DTEnglish.ADJ_EXC) , PTLib.SPACE);
			m_adv_exc   = UTInput.getStringMap(UTInput.getInputStreamsFromClasspath(DTEnglish.ADV_EXC) , PTLib.SPACE);
			s_noun_base = UTInput.getStringSet(UTInput.getInputStreamsFromClasspath(DTEnglish.NOUN_BASE));
			s_verb_base = UTInput.getStringSet(UTInput.getInputStreamsFromClasspath(DTEnglish.VERB_BASE));
			s_adj_base  = UTInput.getStringSet(UTInput.getInputStreamsFromClasspath(DTEnglish.ADJ_BASE));
			s_adv_base  = UTInput.getStringSet(UTInput.getInputStreamsFromClasspath(DTEnglish.ADV_BASE));
			s_cardinal  = UTInput.getStringSet(UTInput.getInputStreamsFromClasspath(DTEnglish.CARDINAL));
			s_ordinal   = UTInput.getStringSet(UTInput.getInputStreamsFromClasspath(DTEnglish.ORDINAL));
			m_abbr_rule = getAbbreviationMap(UTInput.getInputStreamsFromClasspath(DTEnglish.ABBR_RULE));
		}
		catch (IOException e) {e.printStackTrace();}
	}
	
/*	private void initDictionaries(ZipInputStream in)
	{
		String filename;
		ZipEntry entry;

		try
		{
			while ((entry = in.getNextEntry()) != null)
			{
				filename = entry.getName();
				
				     if (filename.equals(DTEnglish.NOUN_EXC))
					m_noun_exc  = UTInput.getStringMap(in, PTLib.SPACE);
				else if (filename.equals(DTEnglish.VERB_EXC))
					m_verb_exc  = UTInput.getStringMap(in, PTLib.SPACE);
				else if (filename.equals(DTEnglish.ADJ_EXC))
					m_adj_exc   = UTInput.getStringMap(in, PTLib.SPACE);
				else if (filename.equals(DTEnglish.ADV_EXC))
					m_adv_exc   = UTInput.getStringMap(in, PTLib.SPACE);
				else if (filename.equals(DTEnglish.NOUN_BASE))
					s_noun_base = UTInput.getStringSet(in);
				else if (filename.equals(DTEnglish.VERB_BASE))
					s_verb_base = UTInput.getStringSet(in);
				else if (filename.equals(DTEnglish.ADJ_BASE))
					s_adj_base  = UTInput.getStringSet(in);
				else if (filename.equals(DTEnglish.ADV_BASE))
					s_adv_base  = UTInput.getStringSet(in);
				else if (filename.equals(DTEnglish.CARDINAL))
					s_cardinal  = UTInput.getStringSet(in);
				else if (filename.equals(DTEnglish.ORDINAL))
					s_ordinal   = UTInput.getStringSet(in);
				else if (filename.equals(DTEnglish.ABBR_RULE))
					m_abbr_rule = getAbbreviationMap(in);
			}
			
			in.close();
		}
		catch (IOException e) {e.printStackTrace();}
	}*/
	
	/** Called by {@link #initDictionaries()}. */
	private Map<String,String> getAbbreviationMap(InputStream stream) throws IOException
	{
		BufferedReader fin = new BufferedReader(new InputStreamReader(stream));
		Map<String,String> map = Maps.newHashMap();
		String line, abbr, pos, key, base;
		String[] tmp;
		
		while ((line = fin.readLine()) != null)
		{
			tmp  = PTLib.splitSpace(line.trim());
			abbr = tmp[0];
			pos  = tmp[1];
			base = tmp[2];
			key  = abbr + FIELD_DELIM + pos;
			
			map.put(key, base);
		}
			
		return map;
	}

	@Override
	public String getLemma(String form, String pos)
	{
		return getLemmaFromLowerSimplifiedForm(MPLib.simplifyBasic(form).toLowerCase(), pos);
	}
	
	public String getLemmaFromLowerSimplifiedForm(String lowerSimplifiedForm, String pos)
	{
		// numbers
		String morphem = getNumber(lowerSimplifiedForm);
		if (morphem != null)	return morphem;
		
		// abbreviations
		morphem = getAbbreviation(lowerSimplifiedForm, pos);
		if (morphem != null)	return morphem;
		
		// exceptions
		morphem = getException(lowerSimplifiedForm, pos);
		if (morphem != null)	return morphem;
				
		// base-forms
		morphem = getBase(lowerSimplifiedForm, pos);
		if (morphem != null)	return morphem;
				
		return lowerSimplifiedForm;
	}
	
	/** Called by {@link #getLemma(String, String)}. */
	private String getAbbreviation(String form, String pos)
	{
		String key = form + FIELD_DELIM + pos;
		return m_abbr_rule.get(key);
	}
	
	/** Called by {@link #getLemma(String, String)}. */
	private String getNumber(String form)
	{
		if (s_cardinal.contains(form) || getBaseFromSuffix(form, s_cardinal, inf_cardinal) != null)
			return "#crd#";
		
		if (form.equals("0st") || form.equals("0nd") || form.equals("0rd") || form.equals("0th") || s_ordinal.contains(form))
			return "#ord#";
		
		return null;
	}
	
	/** Called by {@link #getLemma(String, String)}. */
	private String getException(String form, String pos)
	{
		if (MPLibEn.isNoun     (pos))	return m_noun_exc.get(form);
		if (MPLibEn.isVerb     (pos))	return m_verb_exc.get(form);
		if (MPLibEn.isAdjective(pos))	return m_adj_exc .get(form);
		if (MPLibEn.isAdverb   (pos))	return m_adv_exc .get(form);
		
		return null;
	}
	
	/** Called by {@link #getLemma(String, String)}. */
	private String getBase(String form, String pos)
	{
		if (MPLibEn.isNoun(pos))
			return getBaseFromSuffix(form, s_noun_base, inf_noun);
		
		if (MPLibEn.isVerb(pos))
			return getBaseFromSuffix(form, s_verb_base, inf_verb);
		
		if (MPLibEn.isAdjective(pos))
			return getBaseFromSuffix(form, s_adj_base , inf_adjective);
		
		if (MPLibEn.isAdverb(pos))
			return getBaseFromSuffix(form, s_adv_base , inf_adjective);
		
		return null;
	}
	
	/** Called by {@link #getBase(String, String)}. */
	private String getBaseFromSuffix(String form, Set<String> baseSet, List<MPAffix> affixes)
	{
		return getBaseFromSuffix(form, baseSet, affixes, true);
	}
	
	private String getBaseFromSuffix(String form, Set<String> baseSet, List<MPAffix> affixes, boolean checkFirst)
	{
		if (checkFirst && baseSet.contains(form)) return form;
		String stem, base;
		
		for (MPAffix affix : affixes)
		{
			if ((stem = affix.getStemFromSuffix(form)) != null)
			{
				for (String rep : affix.getReplacements())
				{
					base = stem + rep;
					if (baseSet.contains(base))	return base;			
				}
			}
		}
		
		return null;
	}
	
	public void trim(String outputDir)
	{
		try
		{
//			trim(outputDir, "noun", s_noun_base, m_noun_exc, inf_noun);
			trim(outputDir, "verb", s_verb_base, m_verb_exc, inf_verb);
//			trim(outputDir, "adj" , s_adj_base , m_adj_exc , inf_adjective);
//			trim(outputDir, "adv" , s_adj_base , m_adj_exc , inf_adjective);
		}
		catch (Exception e) {e.printStackTrace();}
	}
	
	private void trim(String outputDir, String pos, Set<String> baseSet, Map<String,String> excMap, List<MPAffix> suffixes) throws Exception
	{
		PrintStream fBaseRemoved = UTOutput.createPrintBufferedFileStream(outputDir+"/"+pos+".txt.removed");
		PrintStream fExcRemoved = UTOutput.createPrintBufferedFileStream(outputDir+"/"+pos+".exc.removed");
		PrintStream fBase = UTOutput.createPrintBufferedFileStream(outputDir+"/"+pos+".txt.new");
		PrintStream fExc = UTOutput.createPrintBufferedFileStream(outputDir+"/"+pos+".exc.new");
		Set<String> sKeep, sExcBase = Sets.newHashSet(excMap.values());
		List<String> list;
		InputStream fin;
		String stem;
		
		System.out.println(pos+":");
		System.out.println("  original      : "+baseSet.size()+" "+excMap.size());
		
		// add base forms in the new file to the base set
		fin = new BufferedInputStream(new FileInputStream(outputDir+"/"+pos+".txt"));
		baseSet.addAll(UTInput.getStringSet(fin));
		fin.close();
		System.out.println("+ from a new set: "+baseSet.size()+" "+excMap.size());
		
		// add base forms in the exception map to the base set 
		baseSet.addAll(sExcBase);
		System.out.println("+ from exception: "+baseSet.size()+" "+excMap.size());
		
		// remove base forms in the base set that are inflections of one another
		fin = new BufferedInputStream(new FileInputStream(outputDir+"/"+pos+".txt.accept"));
		sKeep = UTInput.getStringSet(fin);
		sKeep.addAll(sExcBase);
		fin.close();
		
		for (String form : Sets.newHashSet(baseSet))
		{
			stem = getBaseFromSuffix(form, baseSet, suffixes, false);
			
			if (!sKeep.contains(form) && (excMap.containsKey(form) || baseSet.contains(stem)))
			{
				fBaseRemoved.println(form+" "+stem);
				baseSet.remove(form);
			}
		}
		
		fBaseRemoved.close();
		System.out.println("- inflected base: "+baseSet.size()+" "+excMap.size());
		
		// remove exception forms in the exception map that are inflections of one another
		for (String form : Sets.newHashSet(excMap.keySet()))
		{
			stem = getBaseFromSuffix(form, baseSet, suffixes, false);
			
//			if (form.equals("best"))
//				System.out.println(form+" "+stem+" "+excMap.get(form));
			
			if (excMap.get(form).equals(stem))
			{
				excMap.remove(form);
				fExcRemoved.println(form+" "+stem);
			}
		}
		
		fExcRemoved.close();
		System.out.println("- inflected excs: "+baseSet.size()+" "+excMap.size());
		
		// print a new base set
		list = Lists.newArrayList(baseSet);
		Collections.sort(list);
		
		for (String key : list)
			fBase.println(key);
		
		fBase.close();
		
		// print a new exception map
		list = Lists.newArrayList(excMap.keySet());
		Collections.sort(list);
		
		for (String key : list)
			fExc.println(key+" "+excMap.get(key));
		
		fExc.close();
	}
	
	static public void main(String[] args)
	{
		EnglishMPAnalyzer morph = new EnglishMPAnalyzer();
		morph.trim(args[0]);
	}
}
