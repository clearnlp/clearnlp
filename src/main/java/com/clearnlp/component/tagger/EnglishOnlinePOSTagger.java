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
package com.clearnlp.component.tagger;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;

import org.apache.log4j.Logger;

import com.clearnlp.classification.algorithm.online.IOnlineAlgorithm;
import com.clearnlp.classification.feature.FtrToken;
import com.clearnlp.classification.feature.JointFtrXml;
import com.clearnlp.classification.instance.StringInstance;
import com.clearnlp.classification.model.StringOnlineModel;
import com.clearnlp.classification.prediction.StringPrediction;
import com.clearnlp.classification.vector.StringFeatureVector;
import com.clearnlp.collection.list.FloatArrayList;
import com.clearnlp.component.AbstractOnlineComponent;
import com.clearnlp.component.evaluation.AbstractEval;
import com.clearnlp.component.evaluation.POSEval;
import com.clearnlp.component.state.AbstractState;
import com.clearnlp.component.state.TagState;
import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dependency.DEPTree;
import com.clearnlp.nlp.NLPProcess;
import com.clearnlp.reader.AbstractColumnReader;
import com.clearnlp.util.UTArray;
import com.clearnlp.util.UTString;
import com.clearnlp.util.map.Prob2DMap;
import com.clearnlp.util.pair.Pair;
import com.clearnlp.util.pair.StringDoublePair;

/**
 * @since 2.0.1
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class EnglishOnlinePOSTagger extends AbstractOnlineComponent<TagState>
{
	protected final int LEXICA_LOWER_SIMPLIFIED_FORMS = 0;
	protected final int LEXICA_AMBIGUITY_CLASSES      = 1;
	
	protected Set<String>        s_lsfs;	// lower simplified forms
	protected Prob2DMap          p_ambi;	// ambiguity classes (for collection)
	protected Map<String,String> m_ambi;	// ambiguity classes
	
	private StringOnlineModel s_model;
	private JointFtrXml       f_xml;
	
//	====================================== CONSTRUCTORS ======================================

	/** Constructs a part-of-speech tagger for collecting lexica. */
	public EnglishOnlinePOSTagger(JointFtrXml[] xmls, Set<String> sLsfs)
	{
		super(xmls);
		f_xml  = f_xmls[0];
		s_lsfs = sLsfs;
		p_ambi = new Prob2DMap();
	}
	
	/** Constructs a part-of-speech tagger for training, bootstrapping, and decoding. */
	public EnglishOnlinePOSTagger(JointFtrXml[] xmls, Object[] lexica)
	{
		super(xmls, lexica, 1);
		init();
	}
	
	/** Constructs a part-of-speech tagger from an existing object. */
	public EnglishOnlinePOSTagger(ObjectInputStream in)
	{
		super(in);
		init();
	}
	
	private void init()
	{
		s_model = s_models[0];
		f_xml   = f_xmls[0];
	}
	
//	====================================== LEXICA ======================================

	@Override
	public Object[] getLexica()
	{
		Object[] lexica = new Object[2];
		
		lexica[LEXICA_LOWER_SIMPLIFIED_FORMS] = s_lsfs;
		lexica[LEXICA_AMBIGUITY_CLASSES] = (m_ambi == null) ? getAmbiguityClasses() : m_ambi;
		
		return lexica;
	}
	
	@Override @SuppressWarnings("unchecked")
	public void setLexia(Object[] lexica)
	{
		s_lsfs = (Set<String>)lexica[LEXICA_LOWER_SIMPLIFIED_FORMS];
		m_ambi = (Map<String,String>)lexica[LEXICA_AMBIGUITY_CLASSES];
	}
	
	/** Called by {@link #getLexica()}. */
	private Map<String,String> getAmbiguityClasses()
	{
		Map<String,String> mAmbi = new HashMap<String,String>();
		double threshold = f_xml.getAmbiguityClassThreshold();
		StringDoublePair[] ps;
		StringBuilder build;
		
		for (String key : p_ambi.keySet())
		{
			build = new StringBuilder();
			ps = p_ambi.getProb1D(key);
			UTArray.sortReverseOrder(ps);
			
			for (StringDoublePair p : ps)
			{
				if (p.d <= threshold)	break;
				
				build.append(AbstractColumnReader.BLANK_COLUMN);
				build.append(p.s);
			}
			
			if (build.length() > 0)
				mAmbi.put(key, build.substring(1));				
		}
		
		return mAmbi;
	}
	
	public boolean containsLowerSimplifiedForm(DEPNode node)
	{
		return s_lsfs.contains(node.lowerSimplifiedForm);
	}
	
//	====================================== LOAD/SAVE MODELS ======================================
	
	@Override
	public void load(ObjectInputStream in) throws Exception
	{
		loadDefault(in);
		loadLexica (in);
		in.close();
	}
	
	@Override
	public void save(ObjectOutputStream out) throws Exception
	{
		saveDefault(out);
		saveLexica (out);
		out.close();
	}
	
	protected void loadLexica(ObjectInputStream in) throws Exception
	{
		Object[] lexica = {in.readObject(), in.readObject()};
		setLexia(lexica);
	}
	
	protected void saveLexica(ObjectOutputStream out) throws Exception
	{
		out.writeObject(s_lsfs);
		out.writeObject(m_ambi);
	}
	
//	====================================== PROCESS ======================================

	public void train(IOnlineAlgorithm algorithm, int randomSeed, int iterations)
	{
		s_model.build(f_xml.getLabelCutoff(0), f_xml.getFeatureCutoff(0));
		int[] indices = UTArray.range(s_model.getInstanceSize());
		Random rand = new Random(randomSeed);
		int i;
		
		for (i=0; i<iterations; i++)
		{
			UTArray.shuffle(rand, indices);
			algorithm.updateWeights(s_model, indices);	
		}
	}
	
	public void develop(Logger log, IOnlineAlgorithm algorithm, int randomSeed, List<DEPTree> devTrees)
	{
		s_model.build(f_xml.getLabelCutoff(0), f_xml.getFeatureCutoff(0));
		s_model.printInfo(log);
		
		int[] indices = UTArray.range(s_model.getInstanceSize());
		Random rand = new Random(randomSeed);
		AbstractEval eval = new POSEval();
		double prevScore, currScore = 0;
		FloatArrayList prevWeights;
		String[] goldLabels;
		int iter = 1;
		
		do
		{
			prevScore   = currScore;
			prevWeights = s_model.cloneWeights();
			
			UTArray.shuffle(rand, indices);
			algorithm.updateWeights(s_model, indices);
			
			for (DEPTree tree : devTrees)
			{
				AbstractState state = process(tree, FLAG_DEVELOP, null);
				goldLabels = (String[])state.getGoldLabels();
				eval.countAccuracy(tree, goldLabels);
				tree.setPOSTags((String[])goldLabels);
			}
			
			System.out.printf("%2d: %s\n", iter++, eval.toString());
			currScore = eval.getAccuracies()[0];
			eval.clear();
		}
		while (prevScore < currScore);
		
		s_model.setWeights(prevWeights);
	}
	
	protected AbstractState process(DEPTree tree, byte flag, List<StringInstance> insts)
	{
		TagState state = init(tree, flag);
		
		while (!state.isTerminate())
		{
			switch (flag)
			{
			case FLAG_COLLECT  : processCollect(state);				break;
			case FLAG_TRAIN    : processTrain(state, insts);		break;
			case FLAG_BOOTSTRAP: processBootstrap(state, insts);	break;
			default            : processDecode(state);
			}
				
			state.moveForward();
		}
		
		return state;
	}
	
	/** Called by {@link #process(DEPTree)}. */
	private TagState init(DEPTree tree, byte flag)
	{
		TagState state = new TagState(tree);
		NLPProcess.simplifyForms(tree);
		
		if (flag != FLAG_DECODE)
		{
			state.setGoldLabels(tree.getPOSTags());
			if (flag != FLAG_COLLECT) tree.clearPOSTags();
		}
		
		return state;
	}
	
	/** Called by {@link #process(DEPTree)}. */
	private void processCollect(TagState state)
	{
		DEPNode input = state.getInput();
		
		if (s_lsfs.contains(input.lowerSimplifiedForm))
			p_ambi.add(input.simplifiedForm, input.pos);
	}
	
	/** Called by {@link #process(DEPTree)}. */
	private void processTrain(TagState state, List<StringInstance> insts)
	{
		StringFeatureVector vector = getFeatureVector(f_xml, state);
		if (!vector.isEmpty()) insts.add(new StringInstance(state.getGoldLabel(), vector));
	}
	
	/** Called by {@link #process(DEPTree)}. */
	private void processBootstrap(TagState state, List<StringInstance> insts)
	{
		StringFeatureVector vector = getFeatureVector(f_xml, state);
		if (!vector.isEmpty()) insts.add(new StringInstance(state.getGoldLabel(), vector));
		setAutoLabels(vector, state);
	}
	
	/** Called by {@link #process(DEPTree)}. */
	private void processDecode(TagState state)
	{
		StringFeatureVector vector = getFeatureVector(f_xml, state);
		setAutoLabels(vector, state);
	}
	
	/** Called by {@link #processBootstrap(TagState, List)} and {@link #processDecode(TagState)}. */
	private void setAutoLabels(StringFeatureVector vector, TagState state)
	{
		Pair<StringPrediction,StringPrediction> ps = s_model.predictTop2(vector);
		StringPrediction fst = ps.o1;
		StringPrediction snd = ps.o2;
		
		if (fst.score - snd.score >= 1)
			snd = null;

		DEPNode input = state.getInput();
		input.setPOSTag(fst.label);
		input.setAutoPOSTags(fst, snd);
	}

//	====================================== FEATURE EXTRACTION ======================================

	@Override
	protected String getField(FtrToken token, TagState state)
	{
		DEPNode node = state.getNode(token);
		if (node == null) return null;
		
		switch (token.field)
		{
		case JointFtrXml.F_SIMPLIFIED_FORM:
			return containsLowerSimplifiedForm(node) ? node.simplifiedForm : null;
		case JointFtrXml.F_LOWER_SIMPLIFIED_FORM:
			return containsLowerSimplifiedForm(node) ? node.lowerSimplifiedForm : null;
		case JointFtrXml.F_POS:
			return node.pos;
		case JointFtrXml.F_AMBIGUITY_CLASS:
			return m_ambi.get(node.simplifiedForm);
		}
		
		Matcher m;
		
		if ((m = JointFtrXml.P_BOOLEAN.matcher(token.field)).find())
		{
			int field = Integer.parseInt(m.group(1));
			
			switch (field)
			{
			case  0: return UTString.isAllUpperCase(node.simplifiedForm) ? token.field : null;
			case  1: return UTString.isAllLowerCase(node.simplifiedForm) ? token.field : null;
			case  2: return UTString.beginsWithUpperCase(node.simplifiedForm) & !state.isInputFirstNode() ? token.field : null;
			case  3: return UTString.getNumOfCapitalsNotAtBeginning(node.simplifiedForm) == 1 ? token.field : null;
			case  4: return UTString.getNumOfCapitalsNotAtBeginning(node.simplifiedForm)  > 1 ? token.field : null;
			case  5: return node.simplifiedForm.contains(".") ? token.field : null;
			case  6: return UTString.containsDigit(node.simplifiedForm) ? token.field : null;
			case  7: return node.simplifiedForm.contains("-") ? token.field : null;
			case  8: return state.isInputLastNode() ? token.field : null;
			case  9: return state.isInputFirstNode() ? token.field : null;
			default: throw new IllegalArgumentException("Unsupported feature: "+token.field);
			}
		}
		else if ((m = JointFtrXml.P_FEAT.matcher(token.field)).find())
			return node.getFeat(m.group(1));
		else
			throw new IllegalArgumentException("Unsupported feature: "+token.field);
	}
	
	@Override
	protected String[] getFields(FtrToken token, TagState state)
	{
		DEPNode node = state.getNode(token);
		if (node == null) return null;
		String[] fields = null;
		Matcher m;
		
		if ((m = JointFtrXml.P_PREFIX.matcher(token.field)).find())
		{
			fields = UTString.getPrefixes(node.lowerSimplifiedForm, Integer.parseInt(m.group(1)));
		}
		else if ((m = JointFtrXml.P_SUFFIX.matcher(token.field)).find())
		{
			fields = UTString.getSuffixes(node.lowerSimplifiedForm, Integer.parseInt(m.group(1)));
		}
		
		return (fields == null) || (fields.length == 0) ? null : fields;
	}
		
//	private boolean isMeta(String lowerSimplifiedForm)
//	{
//		return lowerSimplifiedForm.equals(MPLib.META_URL) ||
//			   PTPunct.containsOnlyPunctuation(lowerSimplifiedForm) ||
//			   PTNumber.containsOnlyDigits(lowerSimplifiedForm);
//	}
}
