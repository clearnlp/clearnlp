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
import com.clearnlp.dictionary.IDictionary;


/** @author Jinho D. Choi ({@code jdchoi77@gmail.com}) */
public class EnglishMPAnalyzerTest
{
	@Test
	public void testEnglishMPAnalyzer() throws Exception
	{
		String[][] tokens = {
				{"indices"   , "NNS", "index"},
				{"appendices", "NNS", "appendix"},
				{"studies"   , "NNS", "study"},
				{"wolves"    , "NNS", "wolf"},
				{"knives"    , "NNS", "knife"},
				{"crosses"   , "NNS", "cross"},
				{"quizzes"   , "NNS", "quiz"},
				{"areas"     , "NNS", "area"},
				{"gentlemen" , "NNS", "gentleman"},
				{"mice"      , "NNS", "mouse"},
				{"geese"     , "NNS", "goose"},
				{"teeth"     , "NNS", "tooth"},
				{"feet"      , "NNS", "foot"},
				{"analyses"  , "NNS", "analysis"},
				{"vertebrae" , "NNS", "vertebra"},
				{"optima"    , "NNS", "optimum"},
				{"lexica"    , "NNS", "lexicon"},
				{"foci"      , "NNS", "focus"},
				{"corpora"   , "NNS", "corpus"},
				{"studying"  , "NN" , "studying"},
				{"meet"      , "NN" , "meet"},
				{"ratitae"   , "NN" , "ratitae"},
				{"mamma"     , "NN" , "mamma"},
				
				{"studies"  , "VBZ", "study"},
				{"pushes"   , "VBZ", "push"},
				{"abuses"   , "VBZ", "abuse"},
				{"chivvies" , "VBZ", "chivy"},
				{"takes"    , "VBZ", "take"},
				{"feeling"  , "VBG", "feel"},
				{"running"  , "VBG", "run"},
				{"lying"    , "VBG", "lie"},
				{"taken"    , "VBN", "take"},
				{"beaten"   , "VBN", "beat"},
				{"forbidden", "VBN", "forbid"},
				{"bitten"   , "VBN", "bite"},
				{"spoken"   , "VBN", "speak"},
				{"woven"    , "VBN", "weave"},
				{"woken"    , "VBN", "wake"},
				{"slept"    , "VBD", "sleep"},
				{"studied"  , "VBD", "study"},
				{"entered"  , "VBD", "enter"},
				{"fed"      , "VBD", "feed"},
				{"led"      , "VBD", "lead"},
				{"zipped"   , "VBD", "zip"},
				{"learnt"   , "VBD", "learn"},
				{"abused"   , "VBD", "abuse"},
				{"heard"    , "VBD", "hear"},
				{"rode"     , "VBD", "ride"},
				{"spoke"    , "VBD", "speak"},
				{"woke"     , "VBD", "wake"},
				{"wrote"    , "VBD", "write"},
				{"bore"     , "VBD", "bear"},
				{"stove"    , "VBD", "stave"},
				{"stove"    , "VBD", "stave"},
				{"drove"    , "VBD", "drive"},
				{"wove"     , "VBD", "weave"},
				{"took"     , "VBD", "take"},
				
				{"occurred", "VBD", "occur"},
				{"denied"  , "VBD", "deny"},
				{"studying", "VBG", "study"},
				
				{"drabber" , "JJR", "drab"},
				{"larger"  , "JJR", "large"},
				{"earlier" , "JJR", "early"},
				{"largest" , "JJS", "large"},
				{"earliest", "JJS", "early"},
				{"after"   , "JJ" , "after"},
				
				{"larger"  , "RBR", "large"},
				{"earlier" , "RBR", "early"},
				{"actively", "RB" , "actively"},
				{"best"    , "RBS", "good"},
				{"largest" , "RBS", "large"},
				{"earliest", "RBS", "early"},
				
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
		System.out.println(IDictionary.LAST_UPDATE);
		
		for (String[] token : tokens)
			assertEquals(token[2], analyzer.getLemma(token[0], token[1]));
	}
}
