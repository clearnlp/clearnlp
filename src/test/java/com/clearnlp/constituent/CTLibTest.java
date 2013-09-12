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

import com.carrotsearch.hppc.IntArrayList;
import com.clearnlp.constituent.CTLib;
import com.clearnlp.constituent.CTReader;
import com.clearnlp.constituent.CTTree;
import com.clearnlp.util.UTInput;


/** @author Jinho D. Choi ({@code jdchoi77@gmail.com}) */
public class CTLibTest
{
	@Test
	public void getTokenMapListTest()
	{
		String filename = "src/test/resources/constituent/CTLibTest.parse";
		CTReader reader = new CTReader(UTInput.createBufferedFileReader(filename));
		
		CTTree tree1 = reader.nextTree();
		CTTree tree2 = reader.nextTree();
		
		String[] m1 = {"[0]","[1]","[2]","[2]","[3]","[4]","[5]","[5]","[5]","[6]"};
		String[] m2 = {"[0]","[1]","[2, 3]","[4]","[5]","[6, 7, 8]","[9]"};
		int i;

		IntArrayList[] map = CTLib.getTokenMapList(tree1, tree2);
		for (i=0; i<map.length; i++)
			assertEquals(m1[i], map[i].toString());
		
		map = CTLib.getTokenMapList(tree2, tree1);
		for (i=0; i<map.length; i++)
			assertEquals(m2[i], map[i].toString());
		
		reader.close();
	}
}