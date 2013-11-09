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
package com.clearnlp.constituent;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.clearnlp.constituent.CTNode;


/** @author Jinho D. Choi ({@code jdchoi77@gmail.com}) */
public class CTNodeTest
{
	@Test
	public void gettersTest()
	{
		CTNode curr = new CTNode("NP-PRD-1-LOC=2");
		
		// getTags
		assertEquals("NP-LOC-PRD-1=2", curr.getTags());
		
		// addChild
		CTNode[] children = new CTNode[3];
		children[0] = new CTNode("NP-LOC");
		children[1] = new CTNode("CC", "and");
		children[2] = new CTNode("NP-TMP");
		
		for (CTNode child : children)
			curr.addChild(child);
		
		// siblingId
		for (CTNode child : curr.getChildren())
			assertEquals(child.pTag, children[child.getSiblingId()].pTag);
		
		// getChildren
		assertEquals("[(NP-LOC null), (CC and), (NP-TMP null)]", curr.getChildren().toString());
		assertEquals("[(CC and), (NP-TMP null)]", curr.getChildren(1).toString());
		assertEquals("[(NP-LOC null), (CC and)]", curr.getChildren(0, 2).toString());
		
		// get*Child
		children[0].addChild(new CTNode("-NONE-", "*"));
		children[2].addChild(new CTNode("NNP", "Jinho"));
		
		assertEquals("(NP-LOC (-NONE- *))", curr.getChild(0).toString());
		assertEquals("(NP-LOC (-NONE- *))", curr.getFirstChild("NP").toString());
		assertEquals("(NP-TMP (NNP Jinho))", curr.getLastChild("NP").toString());
		assertEquals("(CC and)", curr.getFirstChild("CC").toString());
		assertEquals("(CC and)", curr.getLastChild("CC").toString());
		
		// get*Sibling
		assertEquals(0, children[1].getPrevSibling("NP", "-LOC").getSiblingId());
		assertEquals(2, children[1].getNextSibling("NP", "-TMP").getSiblingId());
		assertEquals(null, children[1].getPrevSibling("NP", "-TMP"));
		assertEquals(null, children[1].getNextSibling("NP", "-LOC"));
		assertEquals("[(NP-LOC (-NONE- *)), (CC and)]", children[2].getPrevSiblings().toString());
		
		// get*Ancestor
		CTNode gChild = new CTNode("-NONE-", "*ICH*");
		children[2].addChild(gChild);
		assertEquals(children[2], gChild.getNearestAncestor("NP"));
		assertEquals(children[2], gChild.getNearestAncestor("NP","-TMP"));
		assertEquals(curr, gChild.getNearestAncestor("NP","-PRD"));
		assertEquals(null, gChild.getNearestAncestor("CC"));
		assertEquals(curr, gChild.getHighestChainedAncestor("NP"));
		assertEquals(null, gChild.getHighestChainedAncestor("VP"));
		
		// get*EmptyCategories
		assertEquals("[(-NONE- *ICH*)]", curr.getIncludedEmptyCategory("\\*ICH\\*").toString());

		// getSubTerminal*
		assertEquals("[(-NONE- *), (CC and), (NNP Jinho), (-NONE- *ICH*)]", curr.getSubTerminals().toString());
		assertEquals("* and Jinho *ICH*", curr.toForms(true, " "));
		assertEquals("and Jinho", curr.toForms(false, " "));
		
		// get*Descendant
		CTNode desc = curr.getFirstDescendant("NP");
		assertEquals(children[0], desc);
		
		desc = curr.getFirstDescendant("NP","-TMP");
		assertEquals(children[2], desc);
		
		desc = curr.getFirstDescendant("NNP");
		assertEquals("(NNP Jinho)", desc.toString());

		gChild = new CTNode("NP");
		children[0].addChild(gChild);
		assertEquals(gChild, curr.getFirstChainedDescendant("NP"));
		assertEquals("(-NONE- *)", curr.getFirstTerminal().toString());
		
		// getLowestCommonAncestor
		assertEquals(curr, children[1].getLowestCommonAncestor(gChild));
		assertEquals(curr, curr.getLowestCommonAncestor(gChild));
		assertEquals(curr, gChild.getLowestCommonAncestor(curr));
	}
	
	@Test
	public void settersTest()
	{
		CTNode curr = new CTNode("NP");
		
		// add child
		curr.addChild(new CTNode("A"));
		curr.addChild(new CTNode("B"));
		curr.addChild(0, new CTNode("C"));
		curr.addChild(3, new CTNode("D"));
		curr.addChild(2, new CTNode("E"));

		assertEquals("(NP (C null) (A null) (E null) (B null) (D null))", curr.toStringLine());
		
		for (int i=0; i<curr.getChildren().size(); i++)
			assertEquals(i, curr.getChild(i).getSiblingId());

		// set child
		curr.setChild(0, new CTNode("CC"));
		curr.setChild(1, new CTNode("AA"));
		curr.setChild(4, new CTNode("DD"));
		
		assertEquals("(NP (CC null) (AA null) (E null) (B null) (DD null))", curr.toStringLine());
		for (int i=0; i<curr.getChildren().size(); i++)
			assertEquals(i, curr.getChild(i).getSiblingId());
		
		// remove child
		curr.removeChild(0);
		assertEquals("(NP (AA null) (E null) (B null) (DD null))", curr.toStringLine());
		curr.removeChild(3);
		assertEquals("(NP (AA null) (E null) (B null))", curr.toStringLine());
		curr.removeChild(1);
		assertEquals("(NP (AA null) (B null))", curr.toStringLine());
		
		for (int i=0; i<curr.getChildren().size(); i++)
			assertEquals(i, curr.getChild(i).getSiblingId());
		
		List<CTNode> list = new ArrayList<CTNode>();
		list.add(new CTNode("F"));
		list.add(new CTNode("G"));
		list.add(new CTNode("H"));
		
		curr.resetChildren(list);
		assertEquals("(NP (F null) (G null) (H null))", curr.toStringLine());
		
		for (int i=0; i<curr.getChildren().size(); i++)
			assertEquals(i, curr.getChild(i).getSiblingId());
	}
	
	@Test
	public void testCTNodeBooleans()
	{
		CTNode node = new CTNode("NP-PRD-LOC");
		
		assertEquals(true , node.isPTag("NP"));
		assertEquals(false, node.isPTag("VP"));
		assertEquals(true , node.isPTagAny("NP", "VP"));
		assertEquals(true , node.isPTagAny("NP"));
		assertEquals(false, node.isPTagAny("VP"));
		assertEquals(true , node.matchesPTag("N.+"));
		assertEquals(false, node.matchesPTag("N"));
		assertEquals(true , node.hasFTag("PRD"));
		assertEquals(false, node.isFTag("PRD"));
		node.removeFTag("LOC");
		assertEquals(true, node.isFTag("PRD"));
		node.addFTag("LOC");
		
		Set<String> fTags = new HashSet<String>();

		fTags.add("PRD");	assertEquals(true , node.hasFTagAll(fTags));
		fTags.add("LOC");	assertEquals(true , node.hasFTagAll(fTags));
		fTags.add("TMP");	assertEquals(false, node.hasFTagAll(fTags));
		assertEquals(true , node.hasFTagAny(fTags));
		assertEquals(true , node.hasFTagAny("PRD", "LOC", "TMP"));
		assertEquals(false, node.hasFTagAny("TMP"));
		
		assertEquals(true , node.isTag("NP"));
		assertEquals(true , node.isTag("+N.*"));
		assertEquals(true , node.isTag("-PRD"));
		
		assertEquals(true , node.isTag("NP","-PRD"));
		assertEquals(false, node.isTag("VP","-PRD"));
		assertEquals(false, node.isTag("NP","-TMP"));
		
		assertEquals(true , node.isTag("+N.*","-LOC"));
		assertEquals(true , node.isTag("+N.*","-PRD","-LOC"));
		assertEquals(false, node.isTag("+N.*","-PRD","-TMP"));
		
		assertEquals(false, node.isPhrase());
		CTNode child = new CTNode("-NONE-", "*");
		node.addChild(child);
		assertEquals(true, node.isPhrase());

		assertEquals(true , child.isEmptyCategory());
		assertEquals(true , child.isEmptyCategoryRec());
		assertEquals(false, node.isEmptyCategory());
		assertEquals(true , node.isEmptyCategoryRec());
		
		child = new CTNode("NP");
		node.addChild(child);
		assertEquals(true, child.isDescendantOf(node));
		CTNode gchild = new CTNode("PP");
		child.addChild(gchild);
		assertEquals(true, gchild.isDescendantOf(node));
	}
}
