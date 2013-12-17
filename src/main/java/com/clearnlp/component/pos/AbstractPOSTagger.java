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
package com.clearnlp.component.pos;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import com.clearnlp.classification.feature.FtrToken;
import com.clearnlp.classification.feature.JointFtrXml;
import com.clearnlp.classification.instance.StringInstance;
import com.clearnlp.classification.model.StringModel;
import com.clearnlp.classification.prediction.StringPrediction;
import com.clearnlp.classification.train.StringTrainSpace;
import com.clearnlp.classification.vector.StringFeatureVector;
import com.clearnlp.component.AbstractStatisticalComponent;
import com.clearnlp.component.evaluation.POSEval;
import com.clearnlp.component.morph.AbstractMPAnalyzer;
import com.clearnlp.component.state.POSState;
import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dependency.DEPTree;
import com.clearnlp.nlp.NLPProcess;
import com.clearnlp.reader.AbstractColumnReader;
import com.clearnlp.util.UTArray;
import com.clearnlp.util.UTString;
import com.clearnlp.util.map.Prob2DMap;
import com.clearnlp.util.pair.Pair;
import com.clearnlp.util.pair.StringDoublePair;
import com.google.common.collect.Lists;

/**
 * Part-of-speech tagger using document frequency cutoffs.
 * @since 1.3.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
abstract public class AbstractPOSTagger extends AbstractStatisticalComponent<POSState>
{
	protected final int LEXICA_LOWER_SIMPLIFIED_FORMS = 0;
	protected final int LEXICA_AMBIGUITY_CLASSES      = 1;
	
	protected Set<String>		 s_lsfs;	// lower simplified forms
	protected Prob2DMap			 p_ambi;	// ambiguity classes (for collection)
	protected Map<String,String> m_ambi;	// ambiguity classes
	
	protected AbstractMPAnalyzer mp_analyzer;
	
//	====================================== CONSTRUCTORS ======================================

	/** Constructs a part-of-speech tagger for collecting lexica. */
	public AbstractPOSTagger(JointFtrXml[] xmls, Set<String> sLsfs)
	{
		super(xmls);

		s_lsfs = sLsfs;
		p_ambi = new Prob2DMap();
	}
	
	/** Constructs a part-of-speech tagger for training. */
	public AbstractPOSTagger(JointFtrXml[] xmls, StringTrainSpace[] spaces, Object[] lexica)
	{
		super(xmls, spaces, lexica);
		initMorphologicalAnalyzer();
	}
	
	/** Constructs a part-of-speech tagger for developing. */
	public AbstractPOSTagger(JointFtrXml[] xmls, StringModel[] models, Object[] lexica)
	{
		super(xmls, models, lexica, new POSEval());
		initMorphologicalAnalyzer();
	}
	
	/** Constructs a part-of-speech tagger for bootsrapping. */
	public AbstractPOSTagger(JointFtrXml[] xmls, StringTrainSpace[] spaces, StringModel[] models, Object[] lexica)
	{
		super(xmls, spaces, models, lexica);
		initMorphologicalAnalyzer();
	}
	
	/** Constructs a part-of-speech tagger for decoding. */
	public AbstractPOSTagger(ObjectInputStream in)
	{
		super(in);
		initMorphologicalAnalyzer();
	}
	
//	====================================== ABSTRACT METHODS ======================================
	
	abstract protected void initMorphologicalAnalyzer();
	abstract protected boolean applyRules(POSState state);
	
//	====================================== LOAD/SAVE MODELS ======================================
	
	@Override
	public void load(ObjectInputStream in) throws Exception
	{
		loadDefault(in);
		loadLexica(in);
		in.close();
	}
	
	@Override
	public void save(ObjectOutputStream out) throws Exception
	{
		saveDefault(out);
		saveLexica(out);
		out.close();
	}
	
	@Override @SuppressWarnings("unchecked")
	protected void initLexia(Object[] lexica)
	{
		s_lsfs = (Set<String>)lexica[LEXICA_LOWER_SIMPLIFIED_FORMS];
		m_ambi = (Map<String,String>)lexica[LEXICA_AMBIGUITY_CLASSES];
	}
	
	protected void loadLexica(ObjectInputStream in) throws Exception
	{
		LOG.info("Loading lexica.\n");
		
		Object[] lexica = {in.readObject(), in.readObject()};
		initLexia(lexica);
	}
	
	protected void saveLexica(ObjectOutputStream out) throws Exception
	{
		LOG.info("Saving lexica.\n");
		
		out.writeObject(s_lsfs);
		out.writeObject(m_ambi);
	}
	
//	====================================== GETTERS/SETTERS ======================================

	@Override
	public Object[] getLexica()
	{
		Object[] lexica = new Object[2];
		
		lexica[LEXICA_LOWER_SIMPLIFIED_FORMS] = s_lsfs;
		lexica[LEXICA_AMBIGUITY_CLASSES] = isLexica() ? getAmbiguityClasses() : m_ambi;
		
		return lexica;
	}
	
	/** {@link AbstractStatisticalComponent#FLAG_LEXICA}. */
	public Set<String> getLowerSimplifiedForms()
	{
		return s_lsfs;
	}
	
	/** {@link AbstractStatisticalComponent#FLAG_LEXICA}. */
	public void clearLowerSimplifiedForms()
	{
		s_lsfs.clear();
	}
	
	/** Called by {@link AbstractPOSTagger#getLexica()}. */
	private Map<String,String> getAmbiguityClasses()
	{
		double threshold = f_xmls[0].getAmbiguityClassThreshold();
		Map<String,String> mAmbi = new HashMap<String,String>();
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
	
	@Override
	public Set<String> getLabels()
	{
		return getDefaultLabels();
	}
	
//	====================================== PROCESS ======================================
	
	@Override
	public void process(DEPTree tree)
	{
		POSState state = init(tree);
		processAux(state);
		
		if (isDevelop())
			e_eval.countAccuracy(state.getTree(), state.getGoldLabels());
	}
	
	/** Called by {@link AbstractPOSTagger#process(DEPTree)}. */
	protected POSState init(DEPTree tree)
	{
		POSState state = new POSState(tree);
		NLPProcess.simplifyForms(tree);
		
		if (!isDecode())
	 	{
			state.setGoldLabels(tree.getPOSTags());
	 		tree.clearPOSTags();
	 	}
		
	 	return state;
	}
	
	/** Called by {@link AbstractPOSTagger#process(DEPTree)}. */
	protected void processAux(POSState state)
	{
		if (isLexica())
			addLexica(state);
		else
		{
			List<StringInstance> insts = tag(state);
			
			if (isTrainOrBootstrap())
				s_spaces[0].addInstances(insts);
		}
	}
	
	/** Called by {@link AbstractPOSTagger#processAux()}. */
	protected void addLexica(POSState state)
	{
		DEPNode node;

		while ((node = state.shift()) != null)
		{
			if (s_lsfs.contains(node.lowerSimplifiedForm))
				p_ambi.add(node.simplifiedForm, state.getGoldLabel());
		}
	}
	
	/** Called by {@link AbstractPOSTagger#processAux()}. */
	protected List<StringInstance> tag(POSState state)
	{
		List<StringInstance> insts = Lists.newArrayList();
		DEPNode node;
		
		while ((node = state.shift()) != null)
		{
			if (!applyRules(state))
				node.pos = getLabel(insts, state);
			
			mp_analyzer.analyze(node);
		}
		
		return insts;
	}
	
	/** Called by {@link AbstractPOSTagger#tag()}. */
	private String getLabel(List<StringInstance> insts, POSState state)
	{
		StringFeatureVector vector = getFeatureVector(f_xmls[0], state);
		String label = null;
		
		if (isTrain())
		{
			label = state.getGoldLabel();
			if (vector.size() > 0) insts.add(new StringInstance(label, vector));
		}
		else if (isDevelopOrDecode())
		{
			label = getAutoLabel(vector, state);
		}
		else if (isBootstrap())
		{
			label = getAutoLabel(vector, state);
			if (vector.size() > 0) insts.add(new StringInstance(state.getGoldLabel(), vector));
		}
		
		return label;
	}
	
	/** Called by {@link AbstractPOSTagger#getLabel()}. */
	private String getAutoLabel(StringFeatureVector vector, POSState state)
	{
		Pair<StringPrediction,StringPrediction> ps = s_models[0].predictTwo(vector);
		StringPrediction fst = ps.o1;
		StringPrediction snd = ps.o2;
		
		if (fst.score - snd.score < 1)
			state.add2ndLabel(snd.label);
		
		return fst.label;
	}
	
//	private void addEmbedding(StringFeatureVector vector, POSState state)
//	{
//		vector.populateWeights();
//		if (m_embed == null) return;
//		
//		double[] embed = m_embed.getEmbedding(state.getInput().form);
//		
//		if (embed != null)
//		{
//			int i, size = embed.length;
//			
//			for (i=0; i<size; i++)
//				vector.addFeature("embed", Integer.toString(i), embed[i]);
//		}
//	}

//	====================================== FEATURE EXTRACTION ======================================

	@Override
	protected String getField(FtrToken token, POSState state)
	{
		DEPNode node = state.getNode(token);
		if (node == null) return null;
		Matcher m;
		
		if (token.isField(JointFtrXml.F_SIMPLIFIED_FORM))
		{
			return containsLowerSimplifiedForm(node) ? node.simplifiedForm : null;
		}
		else if (token.isField(JointFtrXml.F_LOWER_SIMPLIFIED_FORM))
		{
			return containsLowerSimplifiedForm(node) ? node.lowerSimplifiedForm : null;
		}
		else if (token.isField(JointFtrXml.F_LEMMA))
		{
			return containsLowerSimplifiedForm(node) ? node.lemma : null;
		}
		else if (token.isField(JointFtrXml.F_POS))
		{
			return node.pos;
		}
		else if (token.isField(JointFtrXml.F_AMBIGUITY_CLASS))
		{
			return m_ambi.get(node.simplifiedForm);
		}
		else if ((m = JointFtrXml.P_BOOLEAN.matcher(token.field)).find())
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
			default: throw new IllegalArgumentException("Unsupported feature: "+field);
			}
		}
		else if ((m = JointFtrXml.P_FEAT.matcher(token.field)).find())
		{
			return node.getFeat(m.group(1));
		}
		else if ((m = JointFtrXml.P_PREFIX.matcher(token.field)).find())
		{
			int n = Integer.parseInt(m.group(1)), len = node.lowerSimplifiedForm.length();
			return (n <= len) ? node.lowerSimplifiedForm.substring(0, n) : null;
		}
		else if ((m = JointFtrXml.P_SUFFIX.matcher(token.field)).find())
		{
			int n = Integer.parseInt(m.group(1)), len = node.lowerSimplifiedForm.length();
			return (n <= len) ? node.lowerSimplifiedForm.substring(len-n, len) : null;
		}
		
		return null;
	}
	
	@Override
	protected String[] getFields(FtrToken token, POSState state)
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
	
	protected boolean containsLowerSimplifiedForm(DEPNode node)
	{
		return s_lsfs.contains(node.lowerSimplifiedForm);
	}
}
