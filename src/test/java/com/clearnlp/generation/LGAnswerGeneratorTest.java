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
package com.clearnlp.generation;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.clearnlp.dependency.DEPLibEn;
import com.clearnlp.dependency.DEPTree;
import com.clearnlp.generation.LGAnswerGenerator;
import com.clearnlp.reader.SRLReader;
import com.clearnlp.util.UTInput;


/** @author Jinho D. Choi ({@code jdchoi77@gmail.com}) */
public class LGAnswerGeneratorTest
{
	@Test
	public void testWhich()
	{
		int[] rVerbIDs = {2,2,2}, qVerbIDs = {5,5,7};
		String filename = "which.txt";
		String[] expected = {
				"You bought Shakespeare",
				"You bought Shakespeare for Mary",
				"You bought Shakespeare for Mary"};
		
		testAux(filename, expected, rVerbIDs, qVerbIDs);
	}
	
	@Test
	public void testWhom()
	{
		int[] rVerbIDs = {7,7,2,7,7,7}, qVerbIDs = {3,3,4,2,4,4};
		String filename = "whom.txt";
		String[] expected = {
				"You bought the book",
				"For Mary you bought the book",
				"I met Mary for whom you bought the book",
				"You bought the book",
				"You bought the book",
				"For Mary you bought the book"};
		
		testAux(filename, expected, rVerbIDs, qVerbIDs);
	}
	
	@Test
	public void testWhose()
	{
		int[] rVerbIDs = {6,2,6,6}, qVerbIDs = {5,4,2,3};
		String filename = "whose.txt";
		String[] expected = {
				"Mary's car used to be yours",
				"I met Mary whose car used to be yours",
				"Mary's car used to be yours",
				"Mary's car used to be yours"};
		
		testAux(filename, expected, rVerbIDs, qVerbIDs);
	}
	
	@Test
	public void testNoRelativizer()
	{
		int[] rVerbIDs = {6,6,6}, qVerbIDs = {2,4,4};
		String filename = "noRelativizer.txt";
		String[] expected = {
				"I bought the book",
				"I bought the book",
				"I bought the book at the bookstore"};
		
		testAux(filename, expected, rVerbIDs, qVerbIDs);
	}
	
	@Test
	public void testSmallClause1()
	{
		int[] rVerbIDs = {2,4,4}, qVerbIDs = {4,4,4};
		String filename = "smallClause1.txt";
		String[] expected = {
				"You saw me buying a book yesterday",
				"A book",
				"Yesterday"};
		
		testAux(filename, expected, rVerbIDs, qVerbIDs);
	}
	
	@Test
	public void testSmallClause2()
	{
		int[] rVerbIDs = {2,5,5}, qVerbIDs = {4,4,4};
		String filename = "smallClause2.txt";
		String[] expected = {
				"You expected me to buy a book at the store",
				"A book",
				"At the store"};
		
		testAux(filename, expected, rVerbIDs, qVerbIDs);
	}
	
	@Test
	public void testCoordination()
	{
		int[] rVerbIDs = {4,7,4,7,4,7,4,7,4,7}, qVerbIDs = {3,3,5,5,4,4,4,4,4,4};
		String filename = "coordination.txt";
		String[] expected = {
				"John could have come here",
				"John could have bought a book",
				"John could have come",
				"John could have bought a book",
				"John could have come",
				"John could have bought a book",
				"John could have come yesterday",
				"John could have bought a book yesterday",
				"John could have come here",
				"John could have bought a book here"};
		
		testAux(filename, expected, rVerbIDs, qVerbIDs);
	}
	
	@Test
	public void testWhose2()
	{
		int[] rVerbIDs = {6,2}, qVerbIDs = {1,3};
		String filename = "whose2.txt";
		String[] expected = {
				"John's car is red",
				"Bill knows John whose car is red"};
		
		testAux(filename, expected, rVerbIDs, qVerbIDs);
	}
	
	@Test
	public void testWhich2()
	{
		int[] rVerbIDs = {9,2}, qVerbIDs = {5,4};
		String filename = "which2.txt";
		String[] expected = {
				"Your sister attends the school",
				"You went to the school which your sister attends"};
		
		testAux(filename, expected, rVerbIDs, qVerbIDs);
	}
	
	@Test
	public void testPassive()
	{
		int[] rVerbIDs = {4,4,4}, qVerbIDs = {3,2,4};
		String filename = "passive.txt";
		String[] expected = {
				"The car is bought by John",
				"John",
				"The car"};
		
		testAux(filename, expected, rVerbIDs, qVerbIDs);
	}
	
	@Test
	public void testLocative()
	{
		int[] rVerbIDs = {3,3}, qVerbIDs = {5,5};
		String filename = "locative.txt";
		String[] expected = {
				"The train stop in Hoboken",
				"The train stop in Hoboken"};
		
		testAux(filename, expected, rVerbIDs, qVerbIDs);
	}
	
	@Test
	public void testManner()
	{
		int[] rVerbIDs = {2}, qVerbIDs = {6};
		String filename = "manner.txt";
		String[] expected = {
				"Mary looks so beautiful"};
		
		testAux(filename, expected, rVerbIDs, qVerbIDs);
	}
	
//	@Test
	@SuppressWarnings("deprecation")
	public void testPrint()
	{
		String filename = "src/test/resources/generation/manner.txt";
		SRLReader fin = new SRLReader(0, 1, 2, 3, 4, 5, 6, 7);
		fin.open(UTInput.createBufferedFileReader(filename));
		LGAnswerGenerator lg = new LGAnswerGenerator();
		DEPTree rTree, qTree;
		
		int[] rVerbIDs = {2}, qVerbIDs = {6};
		
		rTree = fin.next();
		rTree.setDependents();
		DEPLibEn.postLabel(rTree);
		
		System.out.println("D:"+rTree.toStringRaw());
		
		int i; for (i=0; (qTree = fin.next()) != null; i++)
		{
			qTree.setDependents();
			DEPLibEn.postLabel(qTree);
			System.out.println("--------");
			System.out.println("Q:"+qTree.toStringRaw());
			System.out.println("A:"+lg.getAnswer(qTree, rTree, qVerbIDs[i], rVerbIDs[i], " "));
		}
	}
	
	@SuppressWarnings("deprecation")
	private void testAux(String name, String[] expected, int[] rVerbIDs, int[] qVerbIDs)
	{
		String filename = "src/test/resources/generation/"+name;
		SRLReader fin = new SRLReader(0, 1, 2, 3, 4, 5, 6, 7);
		fin.open(UTInput.createBufferedFileReader(filename));
		LGAnswerGenerator lg = new LGAnswerGenerator();
		DEPTree rTree, qTree;
		
		rTree = fin.next();
		rTree.setDependents();
		DEPLibEn.postLabel(rTree);
		
		int i;for (i=0; (qTree = fin.next()) != null; i++)
			assertEquals(expected[i], lg.getAnswer(qTree, rTree, qVerbIDs[i], rVerbIDs[i], " "));
	}
}
