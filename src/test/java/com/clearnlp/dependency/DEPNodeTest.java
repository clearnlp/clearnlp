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
package com.clearnlp.dependency;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.clearnlp.dependency.DEPFeat;
import com.clearnlp.dependency.DEPLib;
import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dependency.DEPTree;
import com.clearnlp.reader.AbstractReader;
import com.clearnlp.reader.DEPReader;


public class DEPNodeTest
{
	@Test
	public void testDEPNode()
	{
		// initialization
		DEPNode root = new DEPNode();

	//	assertEquals(DEPLib.NULL_ID , root.id);
		assertEquals(AbstractReader.DUMMY_TAG, root.form);
		assertEquals(AbstractReader.DUMMY_TAG, root.lemma);
		assertEquals(AbstractReader.DUMMY_TAG, root.pos);

		root.initRoot();
		
		assertEquals(DEPLib.ROOT_ID , root.id);
		assertEquals(DEPLib.ROOT_TAG, root.form);
		assertEquals(DEPLib.ROOT_TAG, root.lemma);
		assertEquals(DEPLib.ROOT_TAG, root.pos);
		assertEquals(null, root.getLabel());
		assertEquals(null, root.getHead());
		
		DEPNode sbj  = new DEPNode(1, "Jinho", "jinho", "NNP", new DEPFeat("fst=jinho|lst=choi"));
		DEPNode verb = new DEPNode(2, "is", "be", "VBZ", new DEPFeat(DEPReader.BLANK_COLUMN));
		DEPNode obj  = new DEPNode(3, "awesome", "awesome", "JJ", new DEPFeat("_"));
		
		assertEquals(1      , sbj.id);
		assertEquals("Jinho", sbj.form);
		assertEquals("jinho", sbj.lemma);
		assertEquals("NNP"  , sbj.pos);
		assertEquals("choi" , sbj.getFeat("lst"));
		assertEquals(null   , sbj.getFeat("mid"));
		
		// getters and setters
		assertEquals(null, verb.getHead());
		
		verb.setHead(root, "ROOT");
		sbj .setHead(verb, "SBJ");
		obj .setHead(verb, "OBJ");
		
		assertEquals(root  , verb.getHead());
		assertEquals("ROOT", verb.getLabel());
		
		obj.setHead(verb, "PRD");
		
		assertEquals(verb , obj.getHead());
		assertEquals("PRD", obj.getLabel());
		
		obj.setHead(root, "OBJ");
		
		assertEquals(root , obj.getHead());
		assertEquals("OBJ", obj.getLabel());
		
		// booleans
		assertEquals(false, root.hasHead());
		assertEquals(false, root.isDependentOf(verb));
		assertEquals(true , verb.isDependentOf(root));
		assertEquals(false, sbj .isDependentOf(root));
		assertEquals(true , sbj .isDescendentOf(root));
		assertEquals(true , sbj .isDescendentOf(verb));
	}
	
	public void testSub()
	{
		DEPTree tree = new DEPTree();
		DEPNode n1 = new DEPNode(1, "A", "A", "A", null);
		DEPNode n2 = new DEPNode(2, "B", "B", "PRP$", null);
		DEPNode n3 = new DEPNode(3, "C", "C", "A", null);
		DEPNode n4 = new DEPNode(4, "D", "D", "A", null);
		
		tree.add(n1);
		tree.add(n2);
		tree.add(n3);
		tree.add(n4);
		
		n1.setHead(n4, "A");
		n2.setHead(n4, "A");
		n3.setHead(n4, "nn");
		
		tree.setDependents();
		assertEquals("B C D", n4.getSubLemmasEnNoun(" "));
		
		DEPNode n5 = new DEPNode(5, "F", "F", "PRP$", null);
		DEPNode n6 = new DEPNode(6, "G", "G", "A", null);
		DEPNode n7 = new DEPNode(7, "H", "H", "A", null);
		
		tree.add(n5);
		tree.add(n6);
		tree.add(n7);
		
		n5.setHead(n4, "A");
		n6.setHead(n4, "nn");
		n7.setHead(n4, "A");
		
		tree.resetDependents();
		assertEquals("B C D E F", n4.getSubLemmasEnNoun(" "));
	}
}
