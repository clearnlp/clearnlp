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

import org.junit.Test;

import com.clearnlp.component.morph.EnglishMPAnalyzer;


/** @author Jinho D. Choi ({@code jdchoi77@gmail.com}) */
public class EnglishMPAnalyzerTest
{
	@Test
	public void testEnglishMPAnalyzer() throws Exception
	{
		String[][] tokens = {
				{"crosses"  , "NNS", "cross"},
				{"oxes"     , "NNS", "ox"},
				{"buzzes"   , "NNS", "buzz"},
				{"knives"   , "NNS", "knife"},
				{"wolves"   , "NNS", "wolf"},
				{"indices"  , "NNS", "index"},
				{"cashes"   , "NNS", "cash"},
				{"churches" , "NNS", "church"},
				{"studies"  , "NNS", "study"},
				{"potatoes" , "NNS", "potato"},
				{"areas"    , "NNS", "area"},
				{"analyses" , "NNS", "analysis"},
				{"gentlemen", "NNS", "gentleman"},
				{"geese"    , "NNS", "goose"},
				{"feet"     , "NNS", "foot"},
				{"teeth"    , "NNS", "tooth"},
				{"foci"     , "NNS", "focus"},
				{"optima"   , "NNS", "optimum"},
				{"lexica"   , "NNS", "lexicon"},
				{"vertebrae", "NNS", "vertebra"},
				{"studying" , "NN" , "studying"},
				{"meet"     , "NN" , "meet"},
				{"ratitae"  , "NN" , "ratitae"},
				{"mamma"    , "NN" , "mamma"},
				
				{"drabber" , "JJR", "drab"},
				{"after"   , "JJ" , "after"},
				{"larger"  , "JJR", "large"},
				{"largest" , "JJS", "large"},
				{"earlier" , "JJR", "early"},
				{"earliest", "JJS", "early"},
				
				{"actively", "RB" , "actively"},
				{"best"    , "RBS", "well"},
				{"larger"  , "RBR", "large"},
				{"largest" , "RBS", "large"},
				{"earlier" , "RBR", "early"},
				{"earliest", "RBS", "early"},
				
				{"took"    , "VBD", "take"},
				{"takes"   , "VBZ", "take"},
				{"pushes"  , "VBZ", "push"},
				{"abuses"  , "VBZ", "abuse"},
				{"abused"  , "VBD", "abuse"},
				{"studies" , "VBD", "study"},
				{"studied" , "VBD", "study"},
				{"studying", "VBG", "study"},
				
				{"dominated", "JJ", "dominated"},
				{"dominated", "VBD", "dominate"},
				
				{"1st"   , "XX", "#ord#"},
				{"12nd"  , "XX", "#ord#"},
				{"23rd"  , "XX", "#ord#"},
				{"34th"  , "XX", "#ord#"},
				{"first" , "XX", "#ord#"},
				{"third" , "XX", "#ord#"},
				{"fourth", "XX", "#ord#"},
				
				{"zero"    , "XX", "#crd#"},
				{"ten"     , "XX", "#crd#"},
				{"tens"    , "XX", "#crd#"},
				{"eleven"  , "XX", "#crd#"},
				{"fourteen", "XX", "#crd#"},
				{"thirties", "XX", "#crd#"},
				
				{"http://www.google.com"     , "XX", "#url#"},
				{"www.google.com"            , "XX", "#url#"},
				{"mailto:somebody@google.com", "XX", "#url#"},
				{"some-body@google+.com"     , "XX", "#url#"},
				
				{"10%", "XX", "0"},
				{"$10", "XX", "0"},
				{".01", "XX", "0"},
				{"12.34", "XX", "0"},
				{"12,34,56", "XX", "0"},
				{"12-34-56", "XX", "0"},
				{"12/34/46", "XX", "0"},
				{"A.01", "XX", "a.0"},
				{"A:01", "XX", "a:0"},
				{"A/01", "XX", "a/0"},
				{"$10.23,45:67-89/10%", "XX", "0"},
				
				{".!?-*=~,", "XX", ".!?-*=~,"},
				{"..!!??--**==~~,,", "XX", "..!!??--**==~~,,"},
				{"...!!!???---***===~~~,,,", "XX", "..!!??--**==~~,,"},
				{"....!!!!????----****====~~~~,,,,", "XX", "..!!??--**==~~,,"}};
		
		EnglishMPAnalyzer analyzer = new EnglishMPAnalyzer();
		
		for (String[] token : tokens)
			assertEquals(token[2], analyzer.getLemma(token[0], token[1]));
	}
}
