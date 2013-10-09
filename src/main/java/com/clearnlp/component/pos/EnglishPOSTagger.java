/**
* Copyright 2013 IPSoft Inc.
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
package com.clearnlp.component.pos;

import java.io.ObjectInputStream;
import java.util.Set;

import com.clearnlp.classification.feature.JointFtrXml;
import com.clearnlp.classification.model.StringModel;
import com.clearnlp.classification.train.StringTrainSpace;
import com.clearnlp.component.morph.EnglishMPAnalyzer;
import com.clearnlp.component.state.POSState;
import com.clearnlp.constant.english.ENAux;
import com.clearnlp.constituent.CTLibEn;
import com.clearnlp.dependency.DEPNode;

/**
 * Part-of-speech tagger using document frequency cutoffs.
 * @since 1.3.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class EnglishPOSTagger extends AbstractPOSTagger
{
	private EnglishMPAnalyzer m_analyzer;
	
//	====================================== CONSTRUCTORS ======================================
	
	/** Constructs a part-of-speech tagger for collecting lexica. */
	public EnglishPOSTagger(JointFtrXml[] xmls, Set<String> sLsfs)
	{
		super(xmls, sLsfs);
	}
	
	/** Constructs a part-of-speech tagger for training. */
	public EnglishPOSTagger(JointFtrXml[] xmls, StringTrainSpace[] spaces, Object[] lexica)
	{
		super(xmls, spaces, lexica);
		init();
	}
	
	/** Constructs a part-of-speech tagger for developing. */
	public EnglishPOSTagger(JointFtrXml[] xmls, StringModel[] models, Object[] lexica)
	{
		super(xmls, models, lexica);
		init();
	}
	
	/** Constructs a part-of-speech tagger for bootsrapping. */
	public EnglishPOSTagger(JointFtrXml[] xmls, StringTrainSpace[] spaces, StringModel[] models, Object[] lexica)
	{
		super(xmls, spaces, models, lexica);
		init();
	}
	
	/** Constructs a part-of-speech tagger for decoding. */
	public EnglishPOSTagger(ObjectInputStream in)
	{
		super(in);
		init();
	}
	
	private void init()
	{
		m_analyzer = new EnglishMPAnalyzer();
	}
	
//	====================================== ABSTRACT METHODS ======================================
	
	@Override
	protected void morphologicalAnalyze(DEPNode node)
	{
		m_analyzer.analyze(node);
	}
	
	@Override
	protected boolean applyRules(POSState state)
	{
		DEPNode node = state.getInput();
		
		if (containsLowerSimplifiedForm(node)) return false;
		if (applyNNP(state)) return true;
		
		return false;
	}
	
	private boolean applyNNP(POSState state)
	{
		DEPNode node = state.getInput();
		DEPNode p2 = state.getNode(node.id-2);
		
		if (p2 != null)
		{
			DEPNode p1 = state.getNode(node.id-1);
			
			if (p2.lowerSimplifiedForm.endsWith("name") && p1.lowerSimplifiedForm.equals(ENAux.IS))
			{
				node.pos = CTLibEn.POS_NNP;
				return true;
			}
		}
		
		return false;
	}
}
