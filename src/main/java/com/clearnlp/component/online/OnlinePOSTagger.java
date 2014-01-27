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
package com.clearnlp.component.online;

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
import com.clearnlp.classification.model.StringModelAD;
import com.clearnlp.classification.prediction.StringPrediction;
import com.clearnlp.classification.vector.StringFeatureVector;
import com.clearnlp.component.evaluation.POSEval;
import com.clearnlp.component.state.TagState;
import com.clearnlp.dependency.DEPLib;
import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dependency.DEPTree;
import com.clearnlp.nlp.NLPProcess;
import com.clearnlp.pattern.PTPunct;
import com.clearnlp.reader.AbstractColumnReader;
import com.clearnlp.util.UTArray;
import com.clearnlp.util.UTString;
import com.clearnlp.util.map.Prob2DMap;
import com.clearnlp.util.pair.Pair;
import com.clearnlp.util.pair.StringDoublePair;
import com.google.common.collect.Sets;

/**
 * @since 2.0.1
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class OnlinePOSTagger extends AbstractOnlineStatisticalComponent<TagState>
{
	protected final int LEXICA_LOWER_SIMPLIFIED_FORMS = 0;
	protected final int LEXICA_AMBIGUITY_CLASSE_PROB  = 1;
	protected final int LEXICA_AMBIGUITY_CLASSE_MAP   = 2;
	
	protected Set<String>        s_lsfs;	// lower simplified forms
	protected Prob2DMap          p_ambi;	// ambiguity classes (for collection)
	protected Map<String,String> m_ambi;	// ambiguity classes
	
	private StringModelAD s_model;
	private JointFtrXml   f_xml;
	
//	====================================== CONSTRUCTORS ======================================

	/** Constructs a part-of-speech tagger for collecting lexica. */
	public OnlinePOSTagger(JointFtrXml[] xmls, Set<String> sLsfs)
	{
		super(xmls);
		f_xml  = f_xmls[0];
		s_lsfs = sLsfs;
		p_ambi = new Prob2DMap();
	}
	
	/** Constructs a part-of-speech tagger for training, bootstrapping, and decoding. */
	public OnlinePOSTagger(JointFtrXml[] xmls, Object[] lexica)
	{
		super(xmls, lexica, 1);
		init();
	}
	
	/** Constructs a part-of-speech tagger from an existing object. */
	public OnlinePOSTagger(ObjectInputStream in)
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
		Object[] lexica = new Object[3];
		
		lexica[LEXICA_LOWER_SIMPLIFIED_FORMS] = s_lsfs;
		lexica[LEXICA_AMBIGUITY_CLASSE_PROB] = p_ambi;
		lexica[LEXICA_AMBIGUITY_CLASSE_MAP] = (m_ambi == null) ? getAmbiguityClasses() : m_ambi;
		
		return lexica;
	}
	
	@Override @SuppressWarnings("unchecked")
	public void setLexia(Object[] lexica)
	{
		s_lsfs = (Set<String>)lexica[LEXICA_LOWER_SIMPLIFIED_FORMS];
		p_ambi = (Prob2DMap)lexica[LEXICA_AMBIGUITY_CLASSE_PROB];
		m_ambi = (Map<String,String>)lexica[LEXICA_AMBIGUITY_CLASSE_MAP];
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
	
	@SuppressWarnings("unchecked")
	protected void loadLexica(ObjectInputStream in) throws Exception
	{
		m_ambi = (Map<String,String>)in.readObject();
	}
	
	protected void saveLexica(ObjectOutputStream out) throws Exception
	{
		out.writeObject(m_ambi);
	}
	
//	====================================== GETTERS ======================================

	@Override
	public Set<String> getLabels()
	{
		return Sets.newHashSet(s_model.getLabels());
	}
	
//	====================================== PROCESS ======================================
	
	public void process(DEPTree tree, byte flag)
	{
		TagState state = initialize(tree, flag);
		List<StringInstance> insts = processAux(state, flag);
		finalize(state, insts, flag);
	}
	
	private List<StringInstance> processAux(TagState state, byte flag)
	{
		List<StringInstance> insts = getEmptyInstanceList(flag);
		String label = null;
		
		while (!state.isTerminate())
		{
			switch (flag)
			{
			case FLAG_COLLECT  : processCollect(state);						break;
			case FLAG_TRAIN    : label = processTrain(state, insts);		break;
			case FLAG_BOOTSTRAP: label = processBootstrap(state, insts);	break;
			default            : label = processDecode(state);
			}
			
			setLabel(state.getInput(), label);
			state.moveForward();
		}
		
		return insts;
	}
	
	/** Called by {@link #process(DEPTree)}. */
	private TagState initialize(DEPTree tree, byte flag)
	{
		TagState state = new TagState(tree);
		simplifyForms(tree, flag);
		
		if (flag != FLAG_DECODE)
		{
			state.setGoldLabels(tree.getPOSTags());
			
			if (flag != FLAG_COLLECT)
				tree.clearPOSTags();
		}
		
		return state;
	}
	
	private void simplifyForms(DEPTree tree, byte flag)
	{
		NLPProcess.simplifyForms(tree);
	}
	
	private void finalize(TagState state, List<StringInstance> insts, byte flag)
	{
		if (isTrainOrBootstrap(flag))
		{
			s_model.addInstances(insts);
		}
		else if (isEvaluate(flag))
		{
			if (e_eval == null) e_eval = new POSEval();
			Object[] labels = state.getGoldLabels();
			DEPTree tree = state.getTree();
			
			e_eval.countAccuracy(tree, labels);
			tree.setPOSTags((String[])labels);
		}
	}
	
	/** Called by {@link #process(DEPTree)}. */
	private void processCollect(TagState state)
	{
		DEPNode input = state.getInput();
		
		if (s_lsfs.contains(input.lowerSimplifiedForm))
			p_ambi.add(input.simplifiedForm, input.pos);
	}
	
	/** Called by {@link #process(DEPTree)}. */
	private String processTrain(TagState state, List<StringInstance> insts)
	{
		StringFeatureVector vector = getFeatureVector(f_xml, state);
		String label = getGoldLabel(state);
		addInstance(state, insts, label, vector);
		return label;
	}
	
	/** Called by {@link #process(DEPTree)}. */
	private String processBootstrap(TagState state, List<StringInstance> insts)
	{
		StringFeatureVector vector = getFeatureVector(f_xml, state);
		String label = getAutoLabel(state, vector);
		addInstance(state, insts, getGoldLabel(state), vector);
		return label;
	}
	
	/** Called by {@link #process(DEPTree)}. */
	private String processDecode(TagState state)
	{
		StringFeatureVector vector = getFeatureVector(f_xml, state);
		String label = getAutoLabel(state, vector);
		return label;
	}
	
	private String getGoldLabel(TagState state)
	{
		return state.getGoldLabel();
	}
	
	/** Called by {@link #processBootstrap(TagState, List)} and {@link #processDecode(TagState)}. */
	private String getAutoLabel(TagState state, StringFeatureVector vector)
	{
		Pair<StringPrediction,StringPrediction> ps = s_model.predictTop2(vector);
		StringPrediction fst = ps.o1;
		StringPrediction snd = ps.o2;
		
		if (fst.score - snd.score < 1)
			state.getInput().addFeat(DEPLib.FEAT_POS2, snd.label);
		
		return fst.label;
	}
	
	private void addInstance(TagState state, List<StringInstance> insts, String goldLabel, StringFeatureVector vector)
	{
		if (!vector.isEmpty())
		{
			StringInstance instance = new StringInstance(goldLabel, vector);
			insts.add(instance);
		}
	}
	
//	/** Called by {@link #processBootstrap(TagState, List)} and {@link #processDecode(TagState)}. */
//	private StringPrediction setAutoLabel(StringFeatureVector vector, TagState state)
//	{
//		Pair<StringPrediction,StringPrediction> ps = s_model.predictTop2(vector);
//		DEPNode input = state.getInput();
//		StringPrediction fst = ps.o1;
//		StringPrediction snd = ps.o2;
//		
//		if (fst.isLabel(LABEL_DECAP) && input.simplifiedForm.equals(input.lowerSimplifiedForm))
//			return snd;
//		
//		return fst;
//		
//		
//		if (ps.o1.score - ps.o2.score >= 1) ps.o2 = null;
//		
//		DEPNode input = state.getInput();
//		setLabel(input, ps.o1.label);
//		
//		if (ps.o2 != null)
//			input.addFeat(DEPLib.FEAT_POS2, ps.o2.label);
		
//		DEPNode prev1 = state.getNode(input.id-1);
//		DEPNode prev2 = state.getNode(input.id-2);
//		boolean b = false;
//		String s;
//		
//		if (prev1 != null)
//		{
//			if ((input.isForm("Corp.") || input.isForm("Corp") || input.isForm("Inc.") || input.isForm("Inc")) && prev1.isPos("NNPS"))
//			{
//				prev1.pos = "NNP";
//				b = true;
//			}
//			else if (input.isPos("NNP") && prev1.isPos("NNPS"))
//			{
//				prev1.pos = "NNP";
//				b = true;
//			}
//			else if (input.isPos("NNPS") && prev1.isPos("NNP"))
//			{
//				ps.o1.label = input.pos = "NNP";
//				b = true;
//			}
//		}
//		
//		if (!b && prev2 != null)
//		{
//			if (input.isPos("NNPS") && prev1.isPos("CC") && prev2.isPos("NNP"))
//			{
//				ps.o1.label = input.pos = "NNP";
//				b = true;
//			}
//			else if (input.isPos("NNP") && prev1.isPos("CC") && prev2.isPos("NNPS"))
//			{
//				prev2.pos = "NNP";
//				b = true;
//			}
//		}
//	}
	
	private void setLabel(DEPNode input, String label)
	{
		input.setPOSTag(label);
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
		case JointFtrXml.F_POS2:
			return node.getFeat(DEPLib.FEAT_POS2);
		case JointFtrXml.F_AMBIGUITY_CLASS:
			return m_ambi.get(node.simplifiedForm);
		}
		
		Matcher m;
		
		if ((m = JointFtrXml.P_BOOLEAN.matcher(token.field)).find())
		{
			int field = Integer.parseInt(m.group(1));
			String value = token.field+token.offset;
			
			switch (field)
			{
			case  0: return UTString.isAllUpperCase(node.simplifiedForm) ? value : null;
			case  1: return UTString.isAllLowerCase(node.simplifiedForm) ? value : null;
			case  2: return UTString.beginsWithUpperCase(node.simplifiedForm) & !state.isInputFirstNode() ? value : null;
			case  3: return UTString.getNumOfCapitalsNotAtBeginning(node.simplifiedForm) == 1 ? value : null;
			case  4: return UTString.getNumOfCapitalsNotAtBeginning(node.simplifiedForm)  > 1 ? value : null;
			case  5: return node.simplifiedForm.contains(".") ? value : null;
			case  6: return UTString.containsDigit(node.simplifiedForm) ? value : null;
			case  7: return node.simplifiedForm.contains("-") ? value : null;
			case  8: return state.isInputLastNode() ? value : null;
			case  9: return state.isInputFirstNode() ? value : null;
			case 10: return PTPunct.containsOnlyPunctuation(node.lowerSimplifiedForm) ? value : null;
			default: throw new IllegalArgumentException("Unsupported feature: "+token.field);
			}
		}
		else if ((m = JointFtrXml.P_FEAT.matcher(token.field)).find())
			return node.getFeat(m.group(1));
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
	
	private boolean containsLowerSimplifiedForm(DEPNode node)
	{
		return s_lsfs == null || s_lsfs.contains(node.lowerSimplifiedForm);
	}
		
//	private boolean isMeta(String lowerSimplifiedForm)
//	{
//		return lowerSimplifiedForm.equals(MPLib.META_URL) ||
//			   PTPunct.containsOnlyPunctuation(lowerSimplifiedForm) ||
//			   PTNumber.containsOnlyDigits(lowerSimplifiedForm);
//	}
}
