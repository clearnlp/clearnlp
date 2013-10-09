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
package com.clearnlp.morphology.english;

import static org.junit.Assert.assertEquals;

import java.util.Set;
import java.util.regex.Pattern;

import org.junit.Test;

import com.clearnlp.morphology.Morpheme;
import com.clearnlp.util.pair.Pair;
import com.google.common.collect.Sets;


/** @author Jinho D. Choi ({@code jdchoi77@gmail.com}) */
public class EnglishSuffixMatcherTest
{
	@Test
	public void testEnglishSuffixReplacer()
	{
		EnglishSuffixMatcher matcher = new EnglishSuffixMatcher("ed", "ISD", Pattern.compile("VBD"));
		Set<String> baseSet = Sets.newHashSet("enter", "zip", "feed", "lead", "burn", "bend");
		Pair<Morpheme,Morpheme> morphemes;
		
		matcher.addReplacer(new EnglishSuffixReplacer("VB", "ed", new String[]{"","eed","ead"}, true));
		matcher.addReplacer(new EnglishSuffixReplacer("VB", "t" , new String[]{"","d"}, false));
		
		morphemes = matcher.getMorphemes(baseSet, "entered");
		assertEquals("(enter/VB,ed/ISD)", morphemes.toString());
		morphemes = matcher.getMorphemes(baseSet, "zipped");
		assertEquals("(zip/VB,ed/ISD)", morphemes.toString());
		morphemes = matcher.getMorphemes(baseSet, "fed");
		assertEquals("(feed/VB,ed/ISD)", morphemes.toString());
		morphemes = matcher.getMorphemes(baseSet, "led");
		assertEquals("(lead/VB,ed/ISD)", morphemes.toString());
		morphemes = matcher.getMorphemes(baseSet, "burnt");
		assertEquals("(burn/VB,ed/ISD)", morphemes.toString());
		morphemes = matcher.getMorphemes(baseSet, "bent");
		assertEquals("(bend/VB,ed/ISD)", morphemes.toString());
		morphemes = matcher.getMorphemes(baseSet, "ended");
		assertEquals(null, morphemes);
	}
}
