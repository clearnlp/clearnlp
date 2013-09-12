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
package com.clearnlp.component.pred;

import java.io.ObjectInputStream;

import com.clearnlp.classification.feature.JointFtrXml;
import com.clearnlp.classification.model.StringModel;
import com.clearnlp.classification.train.StringTrainSpace;
import com.clearnlp.constant.english.ENAux;
import com.clearnlp.dependency.DEPNode;

/**
 * PropBank predicate identifier.
 * @since 1.3.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class EnglishPredicateIdentifier extends AbstractPredicateIdentifier
{
//	====================================== CONSTRUCTORS ======================================

	/** Constructs a predicate identifier for collecting lexica. */
	public EnglishPredicateIdentifier(JointFtrXml[] xmls)
	{
		super(xmls);
	}
		
	/** Constructs a predicate identifier for training. */
	public EnglishPredicateIdentifier(JointFtrXml[] xmls, StringTrainSpace[] spaces, Object[] lexica)
	{
		super(xmls, spaces, lexica);
	}
	
	/** Constructs a predicate identifier for developing. */
	public EnglishPredicateIdentifier(JointFtrXml[] xmls, StringModel[] models, Object[] lexica)
	{
		super(xmls, models, lexica);
	}
	
	/** Constructs a predicate identifier for decoding. */
	public EnglishPredicateIdentifier(ObjectInputStream in)
	{
		super(in);
	}
	
//	====================================== ABSTRACT METHODS ======================================

	protected void resetNode(DEPNode node)
	{
		if (node.isLemma(ENAux.APOSTROPHE_S))
			node.lemma = ENAux.BE;
	}
}
