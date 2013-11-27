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
package com.clearnlp.component.state;

import com.clearnlp.classification.feature.FtrToken;
import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dependency.DEPTree;

/**
 * @since 2.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class TagState extends AbstractState
{
	private String[] g_labels;
	private int      i_input;
 	
 	public TagState(DEPTree tree)
	{
		super(tree);
		init (tree);
	}
 	
//	====================================== INITIALIZATION ======================================
	
	private void init(DEPTree tree)
	{
		i_input = 1;
	}
	
//	====================================== INPUT ======================================
	
	public DEPNode getInput()
	{
		return getNode(i_input);
	}
	
	public void setInput(int id)
	{
		i_input = id;
	}
	
//	====================================== LABELS ======================================

	public String getGoldLabel()
	{
		return g_labels[i_input];
	}
	
	@Override
	public Object[] getGoldLabels()
	{
		return g_labels;
	}
	
	public void setGoldLabels(String[] labels)
	{
		g_labels = labels;
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
	
	/** @return {@code true} if the tagging should be terminated. */
	public boolean isTerminate()
	{
		return i_input >= t_size;
	}
	
//	====================================== MOVES ======================================

	/** Moves the current point to the next node to process. */
	public void moveForward()
	{
		i_input++;
	}
	
//	====================================== NODES ======================================
	
	public DEPNode getNode(FtrToken token)
	{
		return getNode(token, i_input, 0, t_size);
	}
}

