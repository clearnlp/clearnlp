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
package com.clearnlp.propbank;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

import com.clearnlp.constituent.CTReader;
import com.clearnlp.constituent.CTTree;
import com.clearnlp.propbank.PBArg;
import com.clearnlp.propbank.PBLoc;
import com.clearnlp.util.UTInput;


/** @author Jinho D. Choi ({@code jdchoi77@gmail.com}) */
public class PBArgTest
{
	@Test
	public void testPBArg()
	{
		PBArg arg = new PBArg();
		arg.label = "ARG";
		
		arg.addLoc(new PBLoc(2, 1));
		arg.addLoc(new PBLoc(2, 1, ";"));
		arg.addLoc(new PBLoc(0, 1, "*"));
		arg.addLoc(new PBLoc(0, 1, "&"));
		arg.addLoc(new PBLoc(0, 0, ","));
		
		assertEquals("2:1;2:1*0:1&0:1,0:0-ARG", arg.toString());
		arg.sortLocs();
		assertEquals("0:0*0:1&0:1,2:1;2:1-ARG", arg.toString());
		
		arg = new PBArg("0:0*0:1&0:2,0:3;0:4-ARGM-TMP");
		assertEquals(arg.toString(), new PBArg(arg.toString()).toString());
		
		assertEquals(true , arg.isLabel("ARGM-TMP"));
		assertEquals(false, arg.isLabel("ARGM"));
		
		assertEquals( "0:0", arg.getLoc(0).toString());
		assertEquals("*0:1", arg.getLoc(1).toString());
		assertEquals(null  , arg.getLoc(-1));
		
		assertEquals(arg.getLoc(4), arg.getLoc(0,4));
		
		String filename = "src/test/resources/constituent/CTReaderTest.parse"; 
		CTReader reader = new CTReader(UTInput.createBufferedFileReader(filename));
		CTTree   tree   = reader.nextTree();
		
		arg = new PBArg("3:1*7:2-ARG1");
		assertEquals("[3, 7, 8, 9, 10, 11]", Arrays.toString(arg.getSortedTerminalIdList(tree)));
	}
}
