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

import org.junit.Test;

import com.clearnlp.propbank.PBLoc;


/** @author Jinho D. Choi ({@code jdchoi77@gmail.com}) */
public class PBLocTest
{
	@Test
	public void testPBLoc()
	{
		PBLoc loc1 = new PBLoc(0, 1);
		PBLoc loc2 = new PBLoc(0, 1, "*");
		
		assertEquals( "0:1", loc1.toString());
		assertEquals("*0:1", loc2.toString());
		assertEquals(true , loc1.equals(loc2.terminalId, loc2.height));
		assertEquals(false, loc1.equals(loc2));
		
		loc1.set(0, 2);
		assertEquals(false, loc1.equals(loc2.terminalId, loc2.height));
		
		loc2 = new PBLoc("0:3", ",");
		assertEquals(",0:3", loc2.toString());
	}
}
