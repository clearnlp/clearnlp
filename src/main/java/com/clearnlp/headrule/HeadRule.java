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
package com.clearnlp.headrule;

/**
 * Headrule.
 * @see HeadTagSet
 * @since 1.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class HeadRule
{
	/** The delimiter between head tagsets ({@code ";"}). */
	static final public String DELIM_TAGSETS = ";";
	/** The left-to-right search direction ({@code "l"}). */
	static final public String DIR_LEFT_TO_RIGHT = "l";
	/** The right-to-left search direction ({@code "r"}). */
	static final public String DIR_RIGHT_TO_LEFT = "r";
	
	protected boolean      b_r2l;
	protected HeadTagSet[] a_tagSets;
	
	/**
	 * Constructs a new headrule by decoding the specific head tagsets.
	 * @param dir {@link HeadRule#DIR_LEFT_TO_RIGHT} or {@link HeadRule#DIR_RIGHT_TO_LEFT}.
	 * If {@link HeadRule#DIR_LEFT_TO_RIGHT}, searches the head from left to right.
	 * If {@link HeadRule#DIR_RIGHT_TO_LEFT}, searches the head from right to left. 
	 * @param headTagSets a 2-dimensional array of the specific head tags (e.g., {@code {{"NN.*","NP"}, {"VB.*","VP"}}}).
	 * Each row represents an array of head tags (see the {@code tags} parameter in {@link HeadTagSet#HeadTag(String)}).
	 */
	public HeadRule(String dir, String[][] headTagSets)
	{
		b_r2l     = dir.equals(DIR_RIGHT_TO_LEFT);
		a_tagSets = new HeadTagSet[headTagSets.length];
		
		for (int i=0; i<a_tagSets.length; i++)
			a_tagSets[i] = new HeadTagSet(headTagSets[i]);
	}
	
	/**
	 * Returns {@code true} if the search direction is right-to-left.
	 * @return {@code true} if the search direction is right-to-left.
	 */
	public boolean isRightToLeft()
	{
		return b_r2l;
	}
	
	/**
	 * Returns the array of head tags.
	 * @return the array of head tags.
	 */
	public HeadTagSet[] getHeadTags() 
	{
		return a_tagSets;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		StringBuilder build = new StringBuilder();
		
		for (HeadTagSet tag : a_tagSets)
		{
			build.append(DELIM_TAGSETS);
			build.append(tag.toString());
		}

		return build.substring(DELIM_TAGSETS.length());
	}
}
