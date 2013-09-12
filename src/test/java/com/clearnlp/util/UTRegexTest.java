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
package com.clearnlp.util;

import static org.junit.Assert.assertEquals;

import java.util.regex.Pattern;

import org.junit.Test;

import com.clearnlp.dependency.DEPLibEn;
import com.clearnlp.util.UTRegex;


/** @author Jinho D. Choi ({@code jdchoi77@gmail.com}) */
public class UTRegexTest
{
	@Test
	public void testUTString()
	{
		Pattern p = UTRegex.getORPattern(DEPLibEn.DEP_COMPLM, DEPLibEn.DEP_MARK);
		assertEquals(true, p.matcher(DEPLibEn.DEP_COMPLM).find());
		assertEquals(true, p.matcher(DEPLibEn.DEP_MARK).find());
		assertEquals(true, p.matcher("a"+DEPLibEn.DEP_COMPLM).find());
		assertEquals(true, p.matcher(DEPLibEn.DEP_COMPLM+"b").find());
		
		p = UTRegex.getORPatternExact(DEPLibEn.DEP_COMPLM, DEPLibEn.DEP_MARK);
		assertEquals(true , p.matcher(DEPLibEn.DEP_COMPLM).find());
		assertEquals(true , p.matcher(DEPLibEn.DEP_MARK).find());
		assertEquals(false, p.matcher("a"+DEPLibEn.DEP_COMPLM).find());
		assertEquals(false, p.matcher(DEPLibEn.DEP_COMPLM+"b").find());
	}
}
