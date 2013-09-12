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

import com.clearnlp.propbank.PBInstance;


/** @author Jinho D. Choi ({@code jdchoi77@gmail.com}) */
public class PBInstanceTest
{
	@Test
	public void testPBArg()
	{
		PBInstance instance = new PBInstance("wsj_2100.parse 8 20 gold get-v get.04 ----- 21:2-ARG1 20:0-rel 18:0-ARG0 17:1-ARGM-MNR 18:0*11:1-LINK-PCR 17:1*15:1-LINK-SLC");
		assertEquals("20:0-rel", instance.getArg(1).toString());
		
		instance.sortArgs();
		assertEquals("wsj_2100.parse 8 20 gold get-v get.04 ----- 11:1*18:0-LINK-PCR 15:1*17:1-LINK-SLC 17:1-ARGM-MNR 18:0-ARG0 20:0-rel 21:2-ARG1", instance.toString());
		assertEquals(instance.getArg(3), instance.getFirstArg("ARG0"));
		
		instance.removeArgs("ARG0");
		assertEquals("wsj_2100.parse 8 20 gold get-v get.04 ----- 11:1*18:0-LINK-PCR 15:1*17:1-LINK-SLC 17:1-ARGM-MNR 20:0-rel 21:2-ARG1", instance.toString());
		instance.removeArgs("ARG0");
		assertEquals("wsj_2100.parse 8 20 gold get-v get.04 ----- 11:1*18:0-LINK-PCR 15:1*17:1-LINK-SLC 17:1-ARGM-MNR 20:0-rel 21:2-ARG1", instance.toString());
	}
}
