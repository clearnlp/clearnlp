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
import com.clearnlp.component.state.POSState;
import com.clearnlp.dependency.DEPNode;

/**
 * Part-of-speech tagger using document frequency cutoffs.
 * @since 1.3.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class DefaultPOSTagger extends AbstractPOSTagger
{
//	====================================== CONSTRUCTORS ======================================

	/** Constructs a part-of-speech tagger for collecting lexica. */
	public DefaultPOSTagger(JointFtrXml[] xmls, Set<String> sLsfs)
	{
		super(xmls, sLsfs);
	}
	
	/** Constructs a part-of-speech tagger for training. */
	public DefaultPOSTagger(JointFtrXml[] xmls, StringTrainSpace[] spaces, Object[] lexica)
	{
		super(xmls, spaces, lexica);
	}
	
	/** Constructs a part-of-speech tagger for developing. */
	public DefaultPOSTagger(JointFtrXml[] xmls, StringModel[] models, Object[] lexica)
	{
		super(xmls, models, lexica);
	}
	
	/** Constructs a part-of-speech tagger for bootsrapping. */
	public DefaultPOSTagger(JointFtrXml[] xmls, StringTrainSpace[] spaces, StringModel[] models, Object[] lexica)
	{
		super(xmls, spaces, models, lexica);
	}
	
	/** Constructs a part-of-speech tagger for decoding. */
	public DefaultPOSTagger(ObjectInputStream in)
	{
		super(in);
	}
	
//	====================================== ABSTRACT METHODS ======================================

	@Override
	protected void morphologicalAnalyze(DEPNode node)
	{
		node.lemma = node.lowerSimplifiedForm;
	}
	
	@Override
	protected boolean applyRules(POSState state) {return false;}
}
