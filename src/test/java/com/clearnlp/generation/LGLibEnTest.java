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
package com.clearnlp.generation;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.clearnlp.constituent.CTLibEn;
import com.clearnlp.dependency.DEPArc;
import com.clearnlp.dependency.DEPFeat;
import com.clearnlp.dependency.DEPLibEn;
import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dependency.DEPTree;
import com.clearnlp.generation.LGLibEn;


/** @author Jinho D. Choi ({@code jdchoi77@gmail.com}) */
public class LGLibEnTest
{
	@Test
	public void testGetForms()
	{
		DEPNode head = new DEPNode(2, "H", "H", CTLibEn.POS_NN, new DEPFeat());
		head.initDependents();
		DEPNode node;
		
		node = new DEPNode(1, "``", "``", CTLibEn.POS_LQ, new DEPFeat());
		head.addDependent(new DEPArc(node, "A"));
		node.initDependents();
		
		node = new DEPNode(1, "I", "I", CTLibEn.POS_PRP, new DEPFeat());
		head.addDependent(new DEPArc(node, "A"));
		node.initDependents();
		
		node = new DEPNode(1, "'ll", "'ll", CTLibEn.POS_MD, new DEPFeat());
		head.addDependent(new DEPArc(node, "A"));
		node.initDependents();
		
		node = new DEPNode(1, "did", "did", CTLibEn.POS_VBD, new DEPFeat());
		head.addDependent(new DEPArc(node, "A"));
		node.initDependents();
		
		node = new DEPNode(1, "n't", "not", CTLibEn.POS_RB, new DEPFeat());
		head.addDependent(new DEPArc(node, "A"));
		node.initDependents();
		
		node = new DEPNode(1, "(", "(", CTLibEn.POS_LRB, new DEPFeat());
		head.addDependent(new DEPArc(node, "A"));
		node.initDependents();
		
		node = new DEPNode(1, "A", "A", CTLibEn.POS_NN, new DEPFeat());
		head.addDependent(new DEPArc(node, "A"));
		node.initDependents();
		
		node = new DEPNode(1, "-", "-", CTLibEn.POS_HYPH, new DEPFeat());
		head.addDependent(new DEPArc(node, "A"));
		node.initDependents();
		
		node = new DEPNode(3, "-", "-", CTLibEn.POS_HYPH, new DEPFeat());
		head.addDependent(new DEPArc(node, "A"));
		node.initDependents();
		
		node = new DEPNode(3, "B", "B", CTLibEn.POS_NN, new DEPFeat());
		head.addDependent(new DEPArc(node, "A"));
		node.initDependents();
		
		node = new DEPNode(3, "C", "C", CTLibEn.POS_NN, new DEPFeat());
		head.addDependent(new DEPArc(node, "A"));
		node.initDependents();
		
		node = new DEPNode(3, "'s", "'s", CTLibEn.POS_POS, new DEPFeat());
		head.addDependent(new DEPArc(node, "A"));
		node.initDependents();
		
		node = new DEPNode(3, "D", "D", CTLibEn.POS_NN, new DEPFeat());
		head.addDependent(new DEPArc(node, "A"));
		node.initDependents();
		
		node = new DEPNode(3, ")", ")", CTLibEn.POS_RRB, new DEPFeat());
		head.addDependent(new DEPArc(node, "A"));
		node.initDependents();
		
		node = new DEPNode(3, "E", "E", CTLibEn.POS_NN, new DEPFeat());
		head.addDependent(new DEPArc(node, "A"));
		node.initDependents();
		
		node = new DEPNode(3, "''", "''", CTLibEn.POS_RQ, new DEPFeat());
		head.addDependent(new DEPArc(node, "A"));
		node.initDependents();
		
		node = new DEPNode(3, "F", "F", CTLibEn.POS_NN, new DEPFeat());
		head.addDependent(new DEPArc(node, "A"));
		node.initDependents();
		
		node = new DEPNode(3, "!", "!", CTLibEn.POS_PERIOD, new DEPFeat());
		head.addDependent(new DEPArc(node, "A"));
		node.initDependents();
		
		assertEquals("``I'll didn't (A-H-B C's D) E'' F!", LGLibEn.getForms(head, true, " "));
	}
	
	@Test
	public void testConvertUnI()
	{
		DEPTree tree = new DEPTree();
		DEPNode sbj1 = new DEPNode(1, "I"     , "I"     , CTLibEn.POS_PRP , new DEPFeat());
		DEPNode verb = new DEPNode(2, "am"    , "be"    , CTLibEn.POS_VBP , new DEPFeat());
		DEPNode attr = new DEPNode(3, "me"    , "me"    , CTLibEn.POS_PRP , new DEPFeat());
		DEPNode poss = new DEPNode(4, "my"    , "my"    , CTLibEn.POS_POS , new DEPFeat());
		DEPNode appo = new DEPNode(5, "mine"  , "mine"  , CTLibEn.POS_PRPS, new DEPFeat());
		DEPNode relf = new DEPNode(5, "myself", "myself", CTLibEn.POS_PRP , new DEPFeat());
		
		sbj1.setHead(verb, DEPLibEn.DEP_NSUBJ);
		verb.setHead(tree.get(0), DEPLibEn.DEP_ROOT);
		attr.setHead(verb, DEPLibEn.DEP_ATTR);
		poss.setHead(appo, DEPLibEn.DEP_POSS);
		appo.setHead(attr, DEPLibEn.DEP_APPOS);
		relf.setHead(attr, DEPLibEn.DEP_APPOS);
		
		tree.add(sbj1);
		tree.add(verb);
		tree.add(attr);
		tree.add(poss);
		tree.add(appo);
		tree.add(relf);
		tree.setDependents();
		
		String si = "I am me my mine myself";
		String su = "you are you your yours yourself";
		
		assertEquals(tree.toStringRaw(), si);
		LGLibEn.convertUnI(tree);
		assertEquals(tree.toStringRaw(), su);
		LGLibEn.convertUnI(tree);
		assertEquals(tree.toStringRaw(), si);
		
		si = "I was me my mine myself";
		su = "you were you your yours yourself";
		verb.form = "was";
		
		assertEquals(tree.toStringRaw(), si);
		LGLibEn.convertUnI(tree);
		assertEquals(tree.toStringRaw(), su);
		LGLibEn.convertUnI(tree);
		assertEquals(tree.toStringRaw(), si);
		
		DEPNode sbj2 = new DEPNode(1, "He", "he", CTLibEn.POS_PRP, new DEPFeat());
		sbj2.setHead(sbj1, DEPLibEn.DEP_CONJ);
		tree.add(2, sbj2);
		tree.resetIDs();
		tree.resetDependents();
		
		si = "I He are me my mine myself";
		su = "you He are you your yours yourself";
		verb.form = "are";
		
		assertEquals(tree.toStringRaw(), si);
		LGLibEn.convertUnI(tree);
		assertEquals(tree.toStringRaw(), su);
		LGLibEn.convertUnI(tree);
		assertEquals(tree.toStringRaw(), si);
		
		sbj2.setHead(verb, DEPLibEn.DEP_NSUBJ);
		sbj1.setHead(sbj2, DEPLibEn.DEP_CONJ);
		tree.remove(sbj2);
		tree.add(1, sbj2);
		
		si = "He I are me my mine myself";
		su = "He you are you your yours yourself";
		
		assertEquals(tree.toStringRaw(), si);
		LGLibEn.convertUnI(tree);
		assertEquals(tree.toStringRaw(), su);
		LGLibEn.convertUnI(tree);
		assertEquals(tree.toStringRaw(), si);
		
		tree = new DEPTree();
		DEPNode main = new DEPNode(3, "done"  , "do"    , CTLibEn.POS_VBN , new DEPFeat());
		sbj1.setHead(main, DEPLibEn.DEP_NSUBJPASS);
		verb.form = "am";
		verb.setHead(main, DEPLibEn.DEP_AUXPASS);
		main.setHead(tree.get(0), DEPLibEn.DEP_ROOT);
		
		tree.add(sbj1);
		tree.add(verb);
		tree.add(main);
		tree.setDependents();
		
		si = "I am done";
		su = "you are done";
		
		assertEquals(tree.toStringRaw(), si);
		LGLibEn.convertUnI(tree);
		assertEquals(tree.toStringRaw(), su);
		LGLibEn.convertUnI(tree);
		assertEquals(tree.toStringRaw(), si);
	}
}
