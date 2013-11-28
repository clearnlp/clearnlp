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
package com.clearnlp.component.morph;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.w3c.dom.Element;

import com.clearnlp.constituent.CTLibEn;
import com.clearnlp.dependency.DEPNode;
import com.clearnlp.morphology.AbstractAffixMatcher;
import com.clearnlp.morphology.MPLib;
import com.clearnlp.morphology.MPLibEn;
import com.clearnlp.morphology.MPTag;
import com.clearnlp.morphology.english.EnglishAffixMatcherFactory;
import com.clearnlp.morphology.english.EnglishInflection;
import com.clearnlp.morphology.english.EnglishSuffixMatcher;
import com.clearnlp.pattern.PTLib;
import com.clearnlp.util.UTInput;
import com.clearnlp.util.UTOutput;
import com.clearnlp.util.UTXml;
import com.clearnlp.util.map.Prob2DMap;
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
	final String PATH          = "dictionary/english/";
	final String VERB          = "verb";
	final String NOUN          = "noun";
	final String ADJECTIVE     = "adjective";
	final String ADVERB        = "adverb";
	final String EXT_BASE      = ".base";
	final String EXT_EXCEPTION = ".exc";

	final String INFLECTION_SUFFIX = PATH + "inflection_suffix.xml";
	final String DERIVATION_SUFFIX = PATH + "derivation_suffix.xml";
	final String ABBREVIATOIN_RULE = PATH + "abbreviation.rule";
	final String CARDINAL_BASE     = PATH + "cardinal.base";
	final String ORDINAL_BASE      = PATH + "ordinal.base";

	final String FIELD_DELIM = "_";

	private EnglishInflection inf_verb;
	private EnglishInflection inf_noun;
	private EnglishInflection inf_adjective;
	private EnglishInflection inf_adverb;
	
	/** Abbreviation replacement rules */
	private Map<String,String> rule_abbreviation;
	private Set<String> base_cardinal;
	/** Ordinal base-forms */
	private Set<String> base_ordinal;
	
//	====================================== CONSTRUCTORS ======================================
	
	/** Constructs an English morphological analyzer from the dictionary in a classpath. */
	public EnglishMPAnalyzer()
	{
		Element inflection = UTXml.getDocumentElement(UTInput.getInputStreamsFromClasspath(INFLECTION_SUFFIX));
		
		try
		{
			inf_verb      = getInflectionRules(inflection, VERB     , CTLibEn.POS_VB);
			inf_noun      = getInflectionRules(inflection, NOUN     , CTLibEn.POS_NN);
			inf_adjective = getInflectionRules(inflection, ADJECTIVE, CTLibEn.POS_JJ);
			inf_adverb    = getInflectionRules(inflection, ADVERB   , CTLibEn.POS_RB);

			base_cardinal     = UTInput.getStringSet(UTInput.getInputStreamsFromClasspath(CARDINAL_BASE));
			base_ordinal      = UTInput.getStringSet(UTInput.getInputStreamsFromClasspath(ORDINAL_BASE));
			rule_abbreviation = getAbbreviationMap  (UTInput.getInputStreamsFromClasspath(ABBREVIATOIN_RULE));
		}
		catch (IOException e) {e.printStackTrace();}
	}
	
	public EnglishMPAnalyzer(ZipFile file)
	{
		try
		{
			Element inflection = UTXml.getDocumentElement(file.getInputStream(new ZipEntry(INFLECTION_SUFFIX)));
			
			inf_verb      = getInflectionRules(file, inflection, VERB     , CTLibEn.POS_VB);
			inf_noun      = getInflectionRules(file, inflection, NOUN     , CTLibEn.POS_NN);
			inf_adjective = getInflectionRules(file, inflection, ADJECTIVE, CTLibEn.POS_JJ);
			inf_adverb    = getInflectionRules(file, inflection, ADVERB   , CTLibEn.POS_RB);

			base_cardinal     = UTInput.getStringSet(file.getInputStream(new ZipEntry(CARDINAL_BASE)));
			base_ordinal      = UTInput.getStringSet(file.getInputStream(new ZipEntry(ORDINAL_BASE)));
			rule_abbreviation = getAbbreviationMap  (file.getInputStream(new ZipEntry(ABBREVIATOIN_RULE)));
		}
		catch (IOException e) {e.printStackTrace();}
	}
	
	/** @param stream this input-stream becomes a parameter of {@link ZipInputStream}. */
	public EnglishMPAnalyzer(InputStream stream)
	{
		try
		{
			ZipInputStream zin = new ZipInputStream(stream);
			Map<String,byte[]> map = UTInput.toByteMap(zin);

			Element inflection = UTXml.getDocumentElement(new ByteArrayInputStream(map.get(INFLECTION_SUFFIX)));
			
			inf_verb      = getInflectionRules(map, inflection, VERB     , CTLibEn.POS_VB);
			inf_noun      = getInflectionRules(map, inflection, NOUN     , CTLibEn.POS_NN);
			inf_adjective = getInflectionRules(map, inflection, ADJECTIVE, CTLibEn.POS_JJ);
			inf_adverb    = getInflectionRules(map, inflection, ADVERB   , CTLibEn.POS_RB);

			base_cardinal     = UTInput.getStringSet(new ByteArrayInputStream(map.get(CARDINAL_BASE)));
			base_ordinal      = UTInput.getStringSet(new ByteArrayInputStream(map.get(ORDINAL_BASE)));
			rule_abbreviation = getAbbreviationMap  (new ByteArrayInputStream(map.get(ABBREVIATOIN_RULE)));
		}
		catch (IOException e) {e.printStackTrace();}
	}
	
	/** Called by {@link #EnglishMPAnalyzer()}. */
	private EnglishInflection getInflectionRules(Element eInflection, String type, String basePOS) throws IOException
	{
		Element     eAffixes        = UTXml.getFirstElementByTagName(eInflection, type);
		InputStream baseStream      = UTInput.getInputStreamsFromClasspath(PATH + type + EXT_BASE);
		InputStream exceptionStream = UTInput.getInputStreamsFromClasspath(PATH + type + EXT_EXCEPTION);
		
		return getInflection(baseStream, exceptionStream, eAffixes, basePOS);
	}
	
	/** Called by {@link #EnglishMPAnalyzer(ZipFile)}. */
	private EnglishInflection getInflectionRules(ZipFile file, Element eInflection, String type, String basePOS) throws IOException
	{
		Element     eAffixes        = UTXml.getFirstElementByTagName(eInflection, type);
		InputStream baseStream      = file.getInputStream(new ZipEntry(PATH + type + EXT_BASE));
		InputStream exceptionStream = file.getInputStream(new ZipEntry(PATH + type + EXT_EXCEPTION));
		
		return getInflection(baseStream, exceptionStream, eAffixes, basePOS);
	}
	
	/** Called by {@link #EnglishMPAnalyzer(InputStream)}. */
	private EnglishInflection getInflectionRules(Map<String,byte[]> map, Element eInflection, String type, String basePOS) throws IOException
	{
		Element     eAffixes        = UTXml.getFirstElementByTagName(eInflection, type);
		InputStream baseStream      = new ByteArrayInputStream(map.get(PATH+type+EXT_BASE));
		InputStream exceptionStream = new ByteArrayInputStream(map.get(PATH+type+EXT_EXCEPTION));
		
		return getInflection(baseStream, exceptionStream, eAffixes, basePOS);
	}
	
	private EnglishInflection getInflection(InputStream baseStream, InputStream exceptionStream, Element eAffixes, String basePOS) throws IOException
	{
		Map<String,String> exceptionMap = (exceptionStream != null) ? UTInput.getStringMap(exceptionStream, PTLib.SPACE) : null;
		List<AbstractAffixMatcher> affixMatchers = new EnglishAffixMatcherFactory().createAffixMatchers(eAffixes);
		Set<String> baseSet = UTInput.getStringSet(baseStream);
		
		return new EnglishInflection(basePOS, baseSet, exceptionMap, affixMatchers);
	}
	
//	private void initDerivationRules()
//	{
//		Element derivation = UTXml.getDocumentElement(UTInput.getInputStreamsFromClasspath(DTEnglish.DERIVATION_SUFFIX));
//		Map<String,Set<String>> baseMap = getBaseMap();
//		
//		der_verb      = getDerivationRules(derivation, DTEnglish.VERB     , baseMap);
//		der_noun      = getDerivationRules(derivation, DTEnglish.NOUN     , baseMap);
//		der_adjective = getDerivationRules(derivation, DTEnglish.ADJECTIVE, baseMap);
//		der_adverb    = getDerivationRules(derivation, DTEnglish.ADVERB   , baseMap);
//	}
//	
//	private EnglishDerivation getDerivationRules(Element eDerivation, String type, Map<String,Set<String>> baseMap)
//	{
//		Element eAffixes = UTXml.getFirstElementByTagName(eDerivation, type);
//		List<AbstractAffixMatcher> affixMatchers = new EnglishAffixMatcherFactory().createAffixMatchers(eAffixes);
//		
//		return new EnglishDerivation(baseMap, affixMatchers);
//	}
//	
//	private Map<String,Set<String>> getBaseMap()
//	{
//		Map<String,Set<String>> baseMap = Maps.newHashMap();
//		
//		baseMap.put(inf_verb     .getBasePOS(), inf_verb.getBaseSet());
//		baseMap.put(inf_noun     .getBasePOS(), inf_noun.getBaseSet());
//		baseMap.put(inf_adjective.getBasePOS(), inf_adjective.getBaseSet());
//		baseMap.put(inf_adverb   .getBasePOS(), inf_adverb.getBaseSet());
//		
//		return baseMap;
//	}

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
	/**
	 * Analyzes the lemma and morphemes of the word-form in the specific node.
	 * PRE: the word-form and the POS tag of the node. 
	 */
	public void analyze(DEPNode node)
	{
		if (node.lowerSimplifiedForm == null)
			node.lowerSimplifiedForm = MPLib.getSimplifiedLowercaseWordForm(node.form);
		
		if (node.pos.equals(CTLibEn.POS_NNP))
		{
			node.lemma = node.form.toLowerCase();
			return;
		}
		
		if ((node.lemma = getAbbreviation(node.lowerSimplifiedForm, node.pos)) != null)
			return;
		
		if ((node.lemma = getBaseFormFromInflection(node.lowerSimplifiedForm, node.pos)) == null)
			node.lemma = node.lowerSimplifiedForm;
		
		if (!node.isPos(CTLibEn.POS_NNPS))
		{
			if      (isCardinal(node.lemma))	node.setLemma(MPTag.LEMMA_CARDINAL);
			else if (isOrdinal(node.lemma))		node.setLemma(MPTag.LEMMA_ORDINAL);	
		}
	}
	
	/** Called by {@link #analyze(DEPNode)}. */
	private String getAbbreviation(String form, String pos)
	{
		String key = form + FIELD_DELIM + pos;
		return rule_abbreviation.get(key);
	}
	
	/** @param form the lower simplified word-form. */
	private String getBaseFormFromInflection(String form, String pos)
	{
		if (MPLibEn.isVerb(pos))
			return inf_verb.getBaseForm(form, pos);
			
		if (MPLibEn.isNoun(pos))
			return inf_noun.getBaseForm(form, pos);
		
		if (MPLibEn.isAdjective(pos))
			return inf_adjective.getBaseForm(form, pos);
		
		if (MPLibEn.isAdverb(pos))
			return inf_adverb.getBaseForm(form, pos);
			
		return null;
	}
	
	private boolean isCardinal(String form)
	{
		return base_cardinal.contains(form);
	}
	
	private boolean isOrdinal(String form)
	{
		return form.equals("0st") || form.equals("0nd") || form.equals("0rd") || form.equals("0th") || base_ordinal.contains(form);
	}
	
//	------------------------------------ EVALUATION ------------------------------------ 
	
	public void check(String outputDir)
	{
		try
		{
			check(outputDir, VERB);
			check(outputDir, NOUN);
			check(outputDir, ADJECTIVE);
			check(outputDir, ADVERB);
		}
		catch (IOException e) {e.printStackTrace();}
	}
	
	private void check(String outputDir, String pos) throws IOException
	{
//		BufferedReader fin = UTInput.createBufferedFileReader(outputDir+"/"+pos+".exc.removed");
//		PrintStream fout = UTOutput.createPrintBufferedFileStream(outputDir+"/"+pos+".exc.kept");
//		String f, m, p;
//		String[] tmp;
//		String line;
//
//		while ((line = fin.readLine()) != null)
//		{
//			tmp = PTLib.splitSpace(line);
//			f   = tmp[0];
//			m   = tmp[1];
//			p   = tmp[2];
//			
//			if (!m.equals(getLemma(f, p)))
//				fout.println(f+" "+m);
//		}
//		
//		fin.close();
//		fout.close();
	}
	
	public void trim(String outputDir)
	{
		try
		{
			trim(outputDir, VERB     , inf_verb);
			trim(outputDir, NOUN     , inf_noun);
			trim(outputDir, ADJECTIVE, inf_adjective);
			trim(outputDir, ADVERB   , inf_adverb);
		}
		catch (Exception e) {e.printStackTrace();}
	}
	
	private void trim(String outputDir, String pos, EnglishInflection inflection) throws Exception
	{
		PrintStream fBaseRemoved = UTOutput.createPrintBufferedFileStream(outputDir+"/"+pos+".base.removed");
		PrintStream fExcRemoved  = UTOutput.createPrintBufferedFileStream(outputDir+"/"+pos+".exc.removed");
		PrintStream fBase = UTOutput.createPrintBufferedFileStream(outputDir+"/"+pos+".base");
		PrintStream fExc  = UTOutput.createPrintBufferedFileStream(outputDir+"/"+pos+".exc");
		Set<String> sAccept = UTInput.getStringSet(UTInput.getInputStreamsFromClasspath(PATH + pos + ".accept"));
		Set<String> baseSet = inflection.getBaseSet();
		Map<String,String> excMap = inflection.getExceptionMap();
//		Morpheme baseMorphem;
		List<String> list;
		String base;
		
		System.out.println(pos+":");
		System.out.println("  original      : "+baseSet.size()+" "+excMap.size());
		
		// add base forms in the exception map to the base set 
		baseSet.addAll(excMap.values());
		System.out.println("+ from exception: "+baseSet.size()+" "+excMap.size());
		
		// remove base forms in the exception map from the base set
		for (String form : Sets.newHashSet(baseSet))
		{
			if (!sAccept.contains(form) && (base = excMap.get(form)) != null && !base.equals(form))
			{
				baseSet.remove(form);
				fBaseRemoved.println(form);
			}
		}
		
		fBaseRemoved.close();
		System.out.println("- from exception: "+baseSet.size()+" "+excMap.size());
		
		// remove exception forms in the exception map that can be inflected by rules
		for (String form : Sets.newHashSet(excMap.keySet()))
		{
			base = excMap.get(form);
			
//			for (EnglishMPToken token : inflection.getInflectionsFromSuffixes(form))
//			{
//				baseMorphem = token.getBaseMorpheme();
//				
//				if (baseMorphem.isForm(base))
//				{
//					excMap.remove(form);
//					fExcRemoved.println(form+" "+base+" "+getPOS(pos, token.getInflectionMorpheme().getPOS()));
//					break;
//				}
//			}
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
	
	public void evaluateInflection(InputStream in) throws Exception
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		Map<String,Map<String,Prob2DMap>> smap = Maps.newHashMap();
		EnglishInflection inflection;
		String line, f, m, p;
		String[] t;
		
		while ((line = reader.readLine()) != null)
		{
			t = PTLib.splitTabs(line);
			f = t[0];
			m = t[1];
			p = t[2];
			
			if      (MPLibEn.isVerb(p))			inflection = inf_verb;
			else if (MPLibEn.isNoun(p))			inflection = inf_noun;
			else if (MPLibEn.isAdjective(p))	inflection = inf_adjective;
			else if (MPLibEn.isAdverb(p))		inflection = inf_adverb;
			else								continue;
			
			for (AbstractAffixMatcher matcher : inflection.getSuffixMatchers())
				((EnglishSuffixMatcher)matcher).evaluateInflection(smap, inflection.getBaseSet(), m, f, p);
		}
		
		printEvaluation(smap);
	}
	
	private void printEvaluation(Map<String,Map<String,Prob2DMap>> smap)
	{
		Map<String,Prob2DMap> rmap;
		List<String> skeys, rkeys;
		Prob2DMap map;
		
		skeys = Lists.newArrayList(smap.keySet());
		Collections.sort(skeys);
		
		for (String skey : skeys)
		{
			System.out.println(skey);
			rmap = smap.get(skey);
			rkeys = Lists.newArrayList(rmap.keySet());
			Collections.sort(rkeys);
			
			for (String rkey : rkeys)
			{
				map = rmap.get(rkey);
				
				for (String key : map.keySet())
					System.out.printf("%s\t%s\t%s\t%d\n", rkey, key, Arrays.toString(map.getProb1D(key)), map.getTotal1D(key));
			}
		}
	}
	
//	static public void main(String[] args)
//	{
//		EnglishMPAnalyzer morph = new EnglishMPAnalyzer();
//		
//		try
//		{
//			morph.trim(args[0]);
//			morph.check(args[0]);
//			morph.evaluateInflection(new FileInputStream(args[0]));
//		}
//		catch (Exception e) {e.printStackTrace();}
//	}
}
