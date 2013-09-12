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
package com.clearnlp.component.role;

import java.io.ObjectInputStream;

import com.clearnlp.classification.feature.JointFtrXml;
import com.clearnlp.classification.model.StringModel;
import com.clearnlp.classification.train.StringTrainSpace;
import com.clearnlp.dependency.DEPNode;

/**
 * @since 1.3.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class EnglishRolesetClassifier extends AbstractRolesetClassifier
{
//	====================================== CONSTRUCTORS ======================================

	/** Constructs a roleset classifier for collecting lexica. */
	public EnglishRolesetClassifier(JointFtrXml[] xmls)
	{
		super(xmls);
	}
		
	/** Constructs a roleset classifier for training. */
	public EnglishRolesetClassifier(JointFtrXml[] xmls, StringTrainSpace[] spaces, Object[] lexica)
	{
		super(xmls, spaces, lexica);
	}
	
	/** Constructs a roleset classifier for developing. */
	public EnglishRolesetClassifier(JointFtrXml[] xmls, StringModel[] models, Object[] lexica)
	{
		super(xmls, models, lexica);
	}
	
	/** Constructs a roleset classifier for decoding. */
	public EnglishRolesetClassifier(ObjectInputStream in)
	{
		super(in);
	}

//	====================================== ABSTRACT METHODS ======================================

	protected String getDefaultLabel(DEPNode node)
	{
		return node.lemma+".01";
	}
}