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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;

import com.clearnlp.constituent.CTLibEn;
import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dictionary.DTEnglish;
import com.clearnlp.morphology.AbstractAffixMatcher;
import com.clearnlp.morphology.MPLib;
import com.clearnlp.morphology.MPLibEn;
import com.clearnlp.morphology.MPTag;
import com.clearnlp.morphology.Morpheme;
import com.clearnlp.morphology.english.EnglishAffixMatcherFactory;
import com.clearnlp.morphology.english.EnglishInflection;
import com.clearnlp.morphology.english.EnglishMPToken;
import com.clearnlp.morphology.english.EnglishSuffixMatcher;
import com.clearnlp.pattern.PTLib;
import com.clearnlp.util.UTInput;
import com.clearnlp.util.UTOutput;
import com.clearnlp.util.UTXml;
import com.clearnlp.util.map.Prob2DMap;
import com.clearnlp.util.pair.Pair;
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
	final String FIELD_DELIM = "_";

	private EnglishInflection inf_verb;
	private EnglishInflection inf_noun;
	private EnglishInflection inf_adjective;
	private EnglishInflection inf_adverb;
	private EnglishInflection inf_cardinal;
	
	/** Abbreviation replacement rules */
	private Map<String,String> rule_abbreviation;
	/** Ordinal base-forms */
	private Set<String> base_ordinal;
	
//	====================================== CONSTRUCTORS ======================================
	
	/** Constructs an English morphological analyzer from the dictionary in a classpath. */
	public EnglishMPAnalyzer()
	{
		Element inflection = UTXml.getDocumentElement(UTInput.getInputStreamsFromClasspath(DTEnglish.INFLECTION_SUFFIX));
		
		try
		{
			base_ordinal      = UTInput.getStringSet(UTInput.getInputStreamsFromClasspath(DTEnglish.ORDINAL_BASE));
			rule_abbreviation = getAbbreviationMap(UTInput.getInputStreamsFromClasspath(DTEnglish.ABBREVIATOIN_RULE));
			
			inf_verb      = getInflectionRules(inflection, DTEnglish.VERB     , CTLibEn.POS_VB, MPTag.IVX);
			inf_noun      = getInflectionRules(inflection, DTEnglish.NOUN     , CTLibEn.POS_NN, MPTag.INX);
			inf_adjective = getInflectionRules(inflection, DTEnglish.ADJECTIVE, CTLibEn.POS_JJ, MPTag.IJX);
			inf_adverb    = getInflectionRules(inflection, DTEnglish.ADVERB   , CTLibEn.POS_RB, MPTag.IRX);
			inf_cardinal  = getInflectionRules(inflection, DTEnglish.CARDINAL , CTLibEn.POS_CD, MPTag.ICX);
		}
		catch (IOException e) {e.printStackTrace();}
	}
	
	private EnglishInflection getInflectionRules(Element eInflection, String type, String basePOS, String irregularPOS) throws IOException
	{
		Element     eAffixes        = UTXml.getFirstElementByTagName(eInflection, type);
		InputStream baseStream      = UTInput.getInputStreamsFromClasspath(DTEnglish.PATH + type + DTEnglish.EXT_BASE);
		InputStream exceptionStream = UTInput.getInputStreamsFromClasspath(DTEnglish.PATH + type + DTEnglish.EXT_EXCEPTION);
		
		Map<String,String> exceptionMap = (exceptionStream != null) ? UTInput.getStringMap(exceptionStream, PTLib.SPACE) : null;
		List<AbstractAffixMatcher> affixMatchers = new EnglishAffixMatcherFactory().createAffixMatchers(eAffixes);
		Set<String> baseSet = UTInput.getStringSet(baseStream);
		
		return new EnglishInflection(basePOS, baseSet, irregularPOS, exceptionMap, affixMatchers);
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
	
	/** @param form the lower simplified word-form. */
	public List<EnglishMPToken> getInflections(String form)
	{
		List<EnglishMPToken> tokens = Lists.newArrayList();
		
		tokens.addAll(inf_verb     .getInflections(form));
		tokens.addAll(inf_noun     .getInflections(form));
		tokens.addAll(inf_adjective.getInflections(form));
		tokens.addAll(inf_adverb   .getInflections(form));
		tokens.addAll(inf_cardinal .getInflections(form));

		return tokens;
	}
	
	/** @param form the lower simplified word-form. */
	public EnglishMPToken getInflection(String form, String pos)
	{
		EnglishMPToken token;
		
		if ((token = inf_cardinal.getInflection(form, pos)) != null)
			return token;
		
		if (MPLibEn.isVerb(pos))
			return inf_verb.getInflection(form, pos);
			
		if (MPLibEn.isNoun(pos))
			return inf_noun.getInflection(form, pos);
		
		if (MPLibEn.isAdjective(pos))
			return inf_adjective.getInflection(form, pos);
		
		if (MPLibEn.isAdverb(pos))
			return inf_adverb.getInflection(form, pos);
			
		return null;
	}
	
	@Override
	public String getLemma(String form, String pos)
	{
		Pair<String,EnglishMPToken> p = getLemmaAndMPToken(MPLib.getSimplifiedLowercaseWordForm(form), pos);
		return p.o1;
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
		
		Pair<String,EnglishMPToken> p = getLemmaAndMPToken(node.lowerSimplifiedForm, node.pos);
		if (p.o2 == null) p.o2 = new EnglishMPToken();
		
		node.setLemma  (p.o1);
		node.setMPToken(p.o2);
	}
	
	private Pair<String,EnglishMPToken> getLemmaAndMPToken(String form, String pos)
	{
		Pair<String,EnglishMPToken> p = new Pair<String,EnglishMPToken>(null, null);
		p.o1 = getAbbreviation(form, pos);
		
		if (p.o1 == null)
			p.o1 = getOrdinal(form);
		
		if (p.o1 == null && inf_cardinal.isBase(form))
			p.o1 = MPTag.LEMMA_CARDINAL;
		
		if (p.o1 == null)
		{
			p.o2 = getInflection(form, pos);
			
			if (p.o2 != null)
			{
				Morpheme base = p.o2.getBaseMorpheme();
				p.o1 = base.isPOS(CTLibEn.POS_CD) ? MPTag.LEMMA_CARDINAL : base.getForm();
			}
		}
		
		if (p.o1 == null)
			p.o1 = form;
		
		return p;
	}
	
	/** Called by {@link #analyze(DEPNode)}. */
	private String getAbbreviation(String form, String pos)
	{
		String key = form + FIELD_DELIM + pos;
		return rule_abbreviation.get(key);
	}
	
	/** Called by {@link #analyze(DEPNode)}. */
	private String getOrdinal(String form)
	{
		if (form.equals("0st") || form.equals("0nd") || form.equals("0rd") || form.equals("0th") || base_ordinal.contains(form))
			return MPTag.LEMMA_ORDINAL;

		return null;
	}
	
//	------------------------------------ EVALUATION ------------------------------------ 
	
	public void check(String outputDir)
	{
		try
		{
			check(outputDir, DTEnglish.VERB);
			check(outputDir, DTEnglish.NOUN);
			check(outputDir, DTEnglish.ADJECTIVE);
			check(outputDir, DTEnglish.ADVERB);
		}
		catch (IOException e) {e.printStackTrace();}
	}
	
	private void check(String outputDir, String pos) throws IOException
	{
		BufferedReader fin = UTInput.createBufferedFileReader(outputDir+"/"+pos+".exc.removed");
		PrintStream fout = UTOutput.createPrintBufferedFileStream(outputDir+"/"+pos+".exc.kept");
		String line, f, m, p;
		String[] tmp;

		while ((line = fin.readLine()) != null)
		{
			tmp = PTLib.splitSpace(line);
			f   = tmp[0];
			m   = tmp[1];
			p   = tmp[2];
			
			if (!m.equals(getLemma(f, p)))
				fout.println(f+" "+m);
		}
		
		fin.close();
		fout.close();
	}
	
	public void trim(String outputDir)
	{
		try
		{
			trim(outputDir, DTEnglish.VERB     , inf_verb);
			trim(outputDir, DTEnglish.NOUN     , inf_noun);
			trim(outputDir, DTEnglish.ADJECTIVE, inf_adjective);
			trim(outputDir, DTEnglish.ADVERB   , inf_adverb);
		}
		catch (Exception e) {e.printStackTrace();}
	}
	
	private void trim(String outputDir, String pos, EnglishInflection inflection) throws Exception
	{
		PrintStream fBaseRemoved = UTOutput.createPrintBufferedFileStream(outputDir+"/"+pos+".base.removed");
		PrintStream fExcRemoved  = UTOutput.createPrintBufferedFileStream(outputDir+"/"+pos+".exc.removed");
		PrintStream fBase = UTOutput.createPrintBufferedFileStream(outputDir+"/"+pos+".base");
		PrintStream fExc  = UTOutput.createPrintBufferedFileStream(outputDir+"/"+pos+".exc");
		Set<String> sAccept = UTInput.getStringSet(UTInput.getInputStreamsFromClasspath(DTEnglish.PATH + pos + ".accept"));
		Set<String> baseSet = inflection.getBaseSet();
		Map<String,String> excMap = inflection.getExceptionMap();
		Morpheme baseMorphem;
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
			
			for (EnglishMPToken token : inflection.getInflectionsFromSuffixes(form))
			{
				baseMorphem = token.getBaseMorpheme();
				
				if (baseMorphem.isForm(base))
				{
					excMap.remove(form);
					fExcRemoved.println(form+" "+base+" "+getPOS(pos, token.getInflectionMorpheme().getPOS()));
					break;
				}
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
	
	private String getPOS(String basePOS, String iPOS)
	{
		switch (iPOS)
		{
		case MPTag.ISD: return CTLibEn.POS_VBD;
		case MPTag.ISG: return CTLibEn.POS_VBG;
		case MPTag.ISN: return CTLibEn.POS_VBN;
		case MPTag.ISZ: return CTLibEn.POS_VBZ;
		case MPTag.ISP: return CTLibEn.POS_NNS;
		case MPTag.ISR: return basePOS.equals("adverb") ? CTLibEn.POS_RBR : CTLibEn.POS_JJR;
		case MPTag.IST: return basePOS.equals("adverb") ? CTLibEn.POS_RBS : CTLibEn.POS_JJS;
		}
		
		throw new IllegalArgumentException(iPOS);
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
