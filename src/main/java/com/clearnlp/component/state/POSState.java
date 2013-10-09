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
package com.clearnlp.component.state;

import com.clearnlp.classification.feature.FtrToken;
import com.clearnlp.classification.feature.JointFtrXml;
import com.clearnlp.dependency.DEPLib;
import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dependency.DEPTree;

/**
 * @since 2.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class POSState extends DefaultState
{
	String[] g_labels;
	int      i_input;
 	
 	public POSState(DEPTree tree)
	{
		super(tree);
		init (tree);
	}
 	
//	====================================== INITIALIZATION ======================================
	
	private void init(DEPTree tree)
	{
		i_input = 0;
	}
	
//	====================================== GETTERS ======================================
	
	public String[] getGoldLabels()
	{
		return g_labels;
	}

	public String getGoldLabel()
	{
		return g_labels[i_input];
	}
	
	public DEPNode getInput()
	{
		return getNode(i_input);
	}
	
//	====================================== SETTERS ======================================
	
	public void setGoldLabels(String[] labels)
	{
		g_labels = labels;
	}
	
	public void setInput(int id)
	{
		i_input = id;
	}
	
	public void add2ndLabel(String pos)
	{
		getInput().addFeat(DEPLib.FEAT_POS2, pos);
	}
	
//	====================================== BOOLEANS ======================================
	
	/** @return {@code true} if the current node is the first node in the tree. */
	public boolean isInputFirstNode()
	{
		return i_input == 1;
	}
	
	/** @return {@code true} if the current node is the last node in the tree. */
	public boolean isInputLastNode()
	{
		return i_input + 1 == t_size;
	}
	
//	====================================== MOVES ======================================

	/**
	 * Moves the current point to the next node to process.
	 * @return the next node to process if exists; otherwise, {@code null}.
	 */
	public DEPNode shift()
	{
		return getNode(++i_input);
	}
	
//	====================================== NODES ======================================
	
	public DEPNode getNode(FtrToken token)
	{
		DEPNode node = getNode(token, i_input, 0, t_size);
		if (node == null)	return null;
		
		if (token.relation != null)
		{
			     if (token.isRelation(JointFtrXml.R_H))		node = node.getHead();
			else if (token.isRelation(JointFtrXml.R_LMD))	node = node.getLeftMostDependent();
			else if (token.isRelation(JointFtrXml.R_RMD))	node = node.getRightMostDependent();
			else if (token.isRelation(JointFtrXml.R_LND))	node = node.getLeftNearestDependent();
			else if (token.isRelation(JointFtrXml.R_RND))	node = node.getRightNearestDependent();
		}
		
		return node;
	}
}

