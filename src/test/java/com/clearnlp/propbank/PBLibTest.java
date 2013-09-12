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

import com.clearnlp.propbank.PBLib;


/** @author Jinho D. Choi ({@code jdchoi77@gmail.com}) */
public class PBLibTest
{
	@Test
	public void testMatcher()
	{
		assertEquals("0", PBLib.getNumber("A0"));
		assertEquals("A", PBLib.getNumber("AA"));
		assertEquals("0", PBLib.getNumber("C-A0"));
		assertEquals("0", PBLib.getNumber("R-A0"));
		assertEquals("1", PBLib.getNumber("A1-DSP"));
		
		assertEquals("0", PBLib.getNumber("ARG0"));
		assertEquals("A", PBLib.getNumber("ARGA"));
		assertEquals("1", PBLib.getNumber("ARG1-DSP"));
	}
	
	@Test
	public void testIsNumberedArgument()
	{
		String label = "ARG0";
		assertEquals(true, PBLib.isNumberedArgument(label));
		
		label = "ARGA";
		assertEquals(true, PBLib.isNumberedArgument(label));

		label = "ARG1-DSP";
		assertEquals(true, PBLib.isNumberedArgument(label));
		
		label = "ARG";
		assertEquals(false, PBLib.isNumberedArgument(label));
		
		label = "ARGM-LOC";
		assertEquals(false, PBLib.isNumberedArgument(label));

		label = "A0";
		assertEquals(true, PBLib.isNumberedArgument(label));
		
		label = "C-A0";
		assertEquals(true, PBLib.isNumberedArgument(label));
		
		label = "R-A0";
		assertEquals(true, PBLib.isNumberedArgument(label));
		
		label = "AA";
		assertEquals(true, PBLib.isNumberedArgument(label));

		label = "A1-DSP";
		assertEquals(true, PBLib.isNumberedArgument(label));
		
		label = "AM-LOC";
		assertEquals(false, PBLib.isNumberedArgument(label));
	}
	
	@Test
	public void testIsCoreNumberedArgument()
	{
		String label = "ARG0";
		assertEquals(true, PBLib.isCoreNumberedArgument(label));
		
		label = "ARGA";
		assertEquals(true, PBLib.isCoreNumberedArgument(label));

		label = "ARG1-DSP";
		assertEquals(true, PBLib.isCoreNumberedArgument(label));
		
		label = "ARG";
		assertEquals(false, PBLib.isCoreNumberedArgument(label));
		
		label = "ARGM-LOC";
		assertEquals(false, PBLib.isCoreNumberedArgument(label));

		label = "A0";
		assertEquals(true, PBLib.isCoreNumberedArgument(label));
		
		label = "AA";
		assertEquals(true, PBLib.isCoreNumberedArgument(label));

		label = "A1-DSP";
		assertEquals(true, PBLib.isCoreNumberedArgument(label));
		
		label = "C-A0";
		assertEquals(false, PBLib.isCoreNumberedArgument(label));
		
		label = "R-A0";
		assertEquals(false, PBLib.isCoreNumberedArgument(label));
		
		label = "AM-LOC";
		assertEquals(false, PBLib.isCoreNumberedArgument(label));
	}
	
	@Test
	public void testIsModifier()
	{
		String label = "ARG0";
		assertEquals(false, PBLib.isModifier(label));
		
		label = "ARGA";
		assertEquals(false, PBLib.isModifier(label));

		label = "ARG1-DSP";
		assertEquals(false, PBLib.isModifier(label));
		
		label = "ARGM-LOC";
		assertEquals(true, PBLib.isModifier(label));
	}
}
