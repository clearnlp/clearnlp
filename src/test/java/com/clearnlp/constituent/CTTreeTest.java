/**
* Copyright (c) 2009-2012, Regents of the University of Colorado
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
*
* Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
* Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
* Neither the name of the University of Colorado at Boulder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
* AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
* IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
* ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
* LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
* CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
* ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
* POSSIBILITY OF SUCH DAMAGE.
*/
package com.clearnlp.constituent;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.clearnlp.constituent.CTNode;
import com.clearnlp.constituent.CTReader;
import com.clearnlp.constituent.CTTree;
import com.clearnlp.util.UTInput;


/** @author Jinho D. Choi ({@code jdchoi77@gmail.com}) */
public class CTTreeTest
{
	@Test
	public void testCTTree()
	{
		String filename = "src/test/resources/constituent/CTReaderTest.parse";
		CTReader reader = new CTReader(UTInput.createBufferedFileReader(filename));
		
		CTTree tree = reader.nextTree();
		assertEquals("I pray that I will be allowed *-1 to come to you .", tree.toForms());
		assertEquals("I_pray_that_I_will_be_allowed_to_come_to_you_.", tree.toForms(false,"_"));
		
		CTNode root = tree.getRoot();
		assertEquals(tree.toString(), root.toString());
		
		CTNode node = tree.getNode(0, 1);
		assertEquals(true, node.isTag("NP", "-SBJ"));
		node = tree.getTerminal(7);
		assertEquals("*-1", node.form);
		node = tree.getToken(7);
		assertEquals("to", node.form);
		
		node = node.getParent();
		assertEquals("[8, 9, 10, 11]", node.getSubTerminalIdList().toString());
		
		tree.setPBLocs();
		assertEquals("3:1", tree.getCoIndexedAntecedent(1).getPBLoc().toString());
		assertEquals("[(-NONE- *-1)]", tree.getCoIndexedEmptyCategories(1).toString());
		
		assertEquals(true , tree.isRange(0, 3));
		assertEquals(false, tree.isRange(0, 4));
	}
}
