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
package com.clearnlp.nlp;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.clearnlp.component.AbstractComponent;
import com.clearnlp.component.dep.AbstractDEPParser;
import com.clearnlp.component.dep.DefaultDEPParser;
import com.clearnlp.component.dep.EnglishDEPParser;
import com.clearnlp.component.morph.AbstractMPAnalyzer;
import com.clearnlp.component.morph.DefaultMPAnalyzer;
import com.clearnlp.component.morph.EnglishMPAnalyzer;
import com.clearnlp.component.pos.AbstractPOSTagger;
import com.clearnlp.component.pos.DefaultPOSTagger;
import com.clearnlp.component.pos.EnglishPOSTagger;
import com.clearnlp.component.pred.AbstractPredicateIdentifier;
import com.clearnlp.component.pred.DefaultPredicateIdentifier;
import com.clearnlp.component.pred.EnglishPredicateIdentifier;
import com.clearnlp.component.role.AbstractRolesetClassifier;
import com.clearnlp.component.role.EnglishRolesetClassifier;
import com.clearnlp.component.srl.AbstractSRLabeler;
import com.clearnlp.component.srl.DefaultSRLabeler;
import com.clearnlp.component.srl.EnglishSRLabeler;
import com.clearnlp.conversion.AbstractC2DConverter;
import com.clearnlp.conversion.EnglishC2DConverter;
import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dependency.DEPTree;
import com.clearnlp.headrule.HeadRuleMap;
import com.clearnlp.reader.AbstractReader;
import com.clearnlp.segmentation.AbstractSegmenter;
import com.clearnlp.segmentation.EnglishSegmenter;
import com.clearnlp.tokenization.AbstractTokenizer;
import com.clearnlp.tokenization.EnglishTokenizer;
import com.clearnlp.util.UTInput;

/**
 * @since 1.1.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class NLPGetter
{
	// ============================= getter: constituent-to-dependency converter =============================
	
	static public AbstractC2DConverter getC2DConverter(String language, String headruleFile, String mergeLabels)
	{
		HeadRuleMap headrules = new HeadRuleMap(UTInput.createBufferedFileReader(headruleFile));
		
		if (language.equals(AbstractReader.LANG_EN))
			return new EnglishC2DConverter(headrules, mergeLabels);
		
		throw new IllegalArgumentException("The requested language '"+language+"' is not currently supported.");
	}
	
	// ============================= getter: word tokenizer =============================
	
	/** Initializes a tokenizer from from the dictionary file in classpath. */
	static public AbstractTokenizer getTokenizer(String language)
	{
		if (language.equals(AbstractReader.LANG_EN))
			return new EnglishTokenizer();
		
		throw new IllegalArgumentException("The requested language '"+language+"' is not currently supported.");
	}
	
	// ============================= getter: sentence segmenter =============================
	
	static public AbstractSegmenter getSegmenter(String language, AbstractTokenizer tokenizer)
	{
		if (language.equals(AbstractReader.LANG_EN))
			return new EnglishSegmenter(tokenizer);
		
		throw new IllegalArgumentException("The requested language '"+language+"' is not currently supported.");
	}
	
	// ============================= getter: component =============================
	
	static public AbstractComponent[] getComponents(String path, String language, List<String> modes) throws IOException
	{
		int i, size = modes.size();
		AbstractComponent[] components = new AbstractComponent[size];
		
		for (i=0; i<size; i++)
			components[i] = getComponent(path, language, modes.get(i));
		
		return components;
	}
	
	static public AbstractComponent[] getComponents(ZipFile file, String language, List<String> modes) throws IOException
	{
		int i, size = modes.size();
		AbstractComponent[] components = new AbstractComponent[size];
		
		for (i=0; i<size; i++)
			components[i] = getComponent(file, language, modes.get(i));
		
		return components;
	}
	
	static public AbstractComponent getComponent(String path, String language, String mode) throws IOException
	{
		return getComponent(getObjectInputStream(path, mode), language, mode);
	}
	
	static public AbstractComponent getComponent(ZipFile file, String language, String mode) throws IOException
	{
		return getComponent(getObjectInputStream(file, mode), language, mode);
	}
	
	static public AbstractComponent getComponent(ObjectInputStream in, String language, String mode) throws IOException
	{
		switch (mode)
		{
		case NLPLib.MODE_POS  : return getPOSTagger(in, language);
		case NLPLib.MODE_MORPH: return getMPAnalyzer(language);
		case NLPLib.MODE_DEP  : return getDEPParser(in, language);
		case NLPLib.MODE_PRED : return getPredicateIdentifier(in, language);
		case NLPLib.MODE_ROLE : return getRolesetClassifier(in, language);
		case NLPLib.MODE_SRL  : return getSRLabeler(in, language);
		}
		
		throw new IllegalArgumentException("The requested mode '"+mode+"' is not supported.");
	}
	
	static private ObjectInputStream getObjectInputStream(String path, String mode) throws IOException
	{
		if (mode.equals(NLPLib.MODE_MORPH))
			return null;
			
		InputStream stream = UTInput.getInputStreamsFromClasspath(path+"/"+mode);
		return new ObjectInputStream(new BufferedInputStream(new GZIPInputStream(stream)));
	}
	
	static private ObjectInputStream getObjectInputStream(ZipFile file, String mode) throws IOException
	{
		if (mode.equals(NLPLib.MODE_MORPH))
			return null;
			
		InputStream stream = file.getInputStream(new ZipEntry(mode));
		return new ObjectInputStream(new BufferedInputStream(new GZIPInputStream(stream)));
	}
	
	static public AbstractPOSTagger getPOSTagger(ObjectInputStream in, String language)
	{
		if (language.equals(AbstractReader.LANG_EN))
			return new EnglishPOSTagger(in);
		
		return new DefaultPOSTagger(in);
	}
	
	static public AbstractDEPParser getDEPParser(ObjectInputStream in, String language)
	{
		if (language.equals(AbstractReader.LANG_EN))
			return new EnglishDEPParser(in);
		
		return new DefaultDEPParser(in);
	}
	
	static public AbstractMPAnalyzer getMPAnalyzer(String language)
	{
		if (language.equals(AbstractReader.LANG_EN))
			return new EnglishMPAnalyzer();
		
		return new DefaultMPAnalyzer();
	}
	
	static public AbstractSRLabeler getSRLabeler(ObjectInputStream in, String language)
	{
		if (language.equals(AbstractReader.LANG_EN))
			return new EnglishSRLabeler(in);
		
		return new DefaultSRLabeler(in);
	}
	
	static public AbstractPredicateIdentifier getPredicateIdentifier(ObjectInputStream in, String language)
	{
		if (language.equals(AbstractReader.LANG_EN))
			return new EnglishPredicateIdentifier(in);
		
		return new DefaultPredicateIdentifier(in);
	}
	
	static public AbstractRolesetClassifier getRolesetClassifier(ObjectInputStream in, String language)
	{
		return new EnglishRolesetClassifier(in);
	}

	static public DEPTree toDEPTree(List<String> tokens)
	{
		DEPTree tree = new DEPTree();
		int i, size = tokens.size();
		
		for (i=0; i<size; i++)
			tree.add(new DEPNode(i+1, tokens.get(i)));
		
		return tree;
	}
}
