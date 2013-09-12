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
package com.clearnlp.morphology;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

import com.clearnlp.pattern.PTLib;
import com.clearnlp.pattern.PTNumber;
import com.clearnlp.pattern.PTPunct;


/** @author Jinho D. Choi ({@code jdchoi77@gmail.com}) */
public class MPLibTest
{
	@Test
	public void normalizePunctuationTest()
	{
		String form = "...!!!???---***===~~~,,,";
		assertEquals("..!!??--**==~~,,", PTPunct.collapsePunctuation(form));
		
		form = ". .. ... !!! ??? ---- ***** === ~~~ , ,, ,,,";
		assertEquals(". .. .. !! ?? -- ** == ~~ , ,, ,,", PTPunct.collapsePunctuation(form));
	}
	
	@Test
	public void containsPunctuationTest()
	{
		String form = "a@b.com";
		assertEquals(true , PTPunct.containsAnyPunctuation(form));
		assertEquals(false, PTPunct.containsOnlyPunctuation(form));
		assertEquals(true , PTPunct.containsAnySpecificPunctuation(form, '@', '?'));	
		assertEquals(false, PTPunct.containsAnySpecificPunctuation(form, '!', '?'));
		
		form = "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~";
		assertEquals(true, PTPunct.containsAnyPunctuation(form));
		assertEquals(true, PTPunct.containsOnlyPunctuation(form));
		assertEquals(true, PTPunct.containsAnySpecificPunctuation(form, '@', '?'));
		
		form = "abcde";
		assertEquals(false, PTPunct.containsAnyPunctuation(form));
	}
	
	@Test
	public void normalizeDigitsTest()
	{
		String form = "10%";
		assertEquals("0", PTNumber.collapseDigits(form));
		form = "$10";
		assertEquals("0", PTNumber.collapseDigits(form));
		form = "A.01";
		assertEquals("A.0", PTNumber.collapseDigits(form));
		form = "A:01";
		assertEquals("A:0", PTNumber.collapseDigits(form));
		form = "A/01";
		assertEquals("A/0", PTNumber.collapseDigits(form));
		form = ".01";
		assertEquals("0", PTNumber.collapseDigits(form));
		form = "12.34";
		assertEquals("0", PTNumber.collapseDigits(form));
		form = "12,34,56";
		assertEquals("0", PTNumber.collapseDigits(form));
		form = "12:34:56";
		assertEquals("0", PTNumber.collapseDigits(form));
		form = "12-34-56";
		assertEquals("0", PTNumber.collapseDigits(form));
		form = "12/34/56";
		assertEquals("0", PTNumber.collapseDigits(form));
		form = "$10.23,45:67-89/10%";
		assertEquals("0", PTNumber.collapseDigits(form));
	}
	
	@Test
	public void revertBracketTest()
	{
		assertEquals("(", PTPunct.revertBracket("-LRB-"));
		assertEquals(")", PTPunct.revertBracket("-RRB-"));
		assertEquals("[", PTPunct.revertBracket("-LSB-"));
		assertEquals("]", PTPunct.revertBracket("-RSB-"));
		assertEquals("{", PTPunct.revertBracket("-LCB-"));
		assertEquals("}", PTPunct.revertBracket("-RCB-"));
		
		assertEquals("(0)", PTPunct.revertBracket("-LRB-0-RRB-"));
		assertEquals(":-)", PTPunct.revertBracket(":--RRB-"));
	}

	@Test
	public void isPeriodLikeTest()
	{
		String form = ".";
		assertEquals(true, PTPunct.isPeriodLike(form));
		form = "?";
		assertEquals(true, PTPunct.isPeriodLike(form));
		form = "!";
		assertEquals(true, PTPunct.isPeriodLike(form));
		form = ".?!";
		assertEquals(true, PTPunct.isPeriodLike(form));
		form = ".?!.?!";
		assertEquals(true, PTPunct.isPeriodLike(form));
		form = "/.?!.?!";
		assertEquals(true, PTPunct.isPeriodLike(form));
		form = "?-";
		assertEquals(false, PTPunct.isPeriodLike(form));
		form = "@?";
		assertEquals(false, PTPunct.isPeriodLike(form));
	}
	
	public void splitWhiteSpaces()
	{
		String s = "A B\nC\tD\rE\fF  G\n";
		assertEquals("[A, B, C, D, E, F, G]", Arrays.toString(PTLib.splitWhiteSpaces(s)));
	}
}
