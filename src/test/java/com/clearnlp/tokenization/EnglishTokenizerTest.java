/**
 * Copyright (c) 2009/09-2012/08, Regents of the University of Colorado
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/**
 * Copyright 2012/09-2013/04, 2013/11-Present, University of Massachusetts Amherst
 * Copyright 2013/05-2013/10, IPSoft Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package com.clearnlp.tokenization;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.zip.ZipFile;

import org.junit.Test;

import com.clearnlp.dictionary.DTLib;

/**
 * @since 1.1.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class EnglishTokenizerTest
{
	@Test
	public void testTokenize() throws Exception
	{
		EnglishTokenizer tok = new EnglishTokenizer(new ZipFile(new File(DTLib.DICTIONARY_JAR)));
//		EnglishTokenizer tok = new EnglishTokenizer();
		String src, trg;
		
		// spaces
		src = "a b  c\n d \t\n\r\fe";
		trg = "[a, b, c, d, e]";
		assertEquals(tok.getTokens(src).toString(), trg);
		
		// emoticons
		src = ":-))))))) :------------) (____) :( :-) :--)";
		trg = "[:-))))))), :------------), (____), :(, :-), :, --, )]";
		assertEquals(tok.getTokens(src).toString(), trg);
		
		// URLs
		src = "|http://www.google.com|www.google.com|mailto:somebody@google.com|some-body@google+.com|";
		trg = "[|, http://www.google.com, |, www.google.com, |, mailto:somebody@google.com, |, some-body@google+.com, |]";
		assertEquals(tok.getTokens(src).toString(), trg);
	
		src = "google.com index.html a.b.htm ab-cd.shtml";
		trg = "[google.com, index.html, a.b.htm, ab-cd.shtml]";
		assertEquals(tok.getTokens(src).toString(), trg);
		
		// abbreviations
		src = "prof. ph.d. a. a.b. a.b a.b.c.";
		trg = "[prof., ph.d., a., a.b., a.b, a.b.c.]";
		assertEquals(tok.getTokens(src).toString(), trg);
		
		// user IDs
		src = "ab.cd. 12.34. @a.!";
		trg = "[ab.cd, ., 12.34, ., @, a, .!]";
		assertEquals(tok.getTokens(src).toString(), trg);
				
		// consecutive punctuation
		src = "A..B!!C??D.!?E.!?.!?F..!!??";
		trg = "[A, .., B, !!, C, ??, D, .!?, E, .!?.!?, F, ..!!??]";
		assertEquals(tok.getTokens(src).toString(), trg);
		
		src = ",,A---C*D**E~~~~F==";
		trg = "[,,, A, ---, C*D, **, E, ~~~~, F, ==]";
		assertEquals(tok.getTokens(src).toString(), trg);
		
		// dots in numbers
		src = ".1 a.1 2.3 4,5 6:7 8-9 0/1 '2 3's 3'4 5'b a'6 a'b";
		trg = "[.1, a.1, 2.3, 4,5, 6:7, 8-9, 0/1, '2, 3's, 3'4, 5'b, a'6, a'b]";
		assertEquals(tok.getTokens(src).toString(), trg);
		
		src = ".a a.3 4,a a:a a8-9 0/1a";
		trg = "[., a, a.3, 4, ,, a, a, :, a, a8-9, 0/1a]";
		assertEquals(tok.getTokens(src).toString(), trg);
		
		// hyphens
		src = "dis-able cross-validation o-kay art-o-torium s-e-e art-work";
		trg = "[dis-able, cross-validation, o-kay, art-o-torium, s-e-e, art, -, work]";
		assertEquals(tok.getTokens(src).toString(), trg);
		
		// apostrophies
		src = "he's we'd I'm you'll they're I've didn't did'nt";
		trg = "[he, 's, we, 'd, I, 'm, you, 'll, they, 're, I, 've, did, n't, did, 'nt]";
		assertEquals(tok.getTokens(src).toString(), trg);
		
		src = "he'S DON'T gue'ss";
		trg = "[he, 'S, DO, N'T, gue'ss]";
		assertEquals(tok.getTokens(src).toString(), trg);
		
		src = "aint cannot don'cha d'ye i'mma dunno";
		trg = "[ai, nt, can, not, do, n', cha, d', ye, i, 'm, ma, du, n, no]";
		assertEquals(tok.getTokens(src).toString(), trg);
		
		src = "$1 E2 L3 USD1 2KPW ||$1 USD1..";
		trg = "[$, 1, E2, L3, USD, 1, 2, KPW, |, |, $, 1, USD, 1, ..]";
		assertEquals(tok.getTokens(src).toString(), trg);
		
		src = "1m 2mm 3kg 4oz";
		trg = "[1, m, 2, mm, 3, kg, 4, oz]";
		assertEquals(tok.getTokens(src).toString(), trg);
		
		src = "1D 2nM 3CM 4LB";
		trg = "[1, D, 2, nM, 3, CM, 4, LB]";
		assertEquals(tok.getTokens(src).toString(), trg);
		
		src = "(1){2}[3]<4>";
		trg = "[(, 1, ), {, 2, }, [, 3, ], <, 4, >]";
		assertEquals(tok.getTokens(src).toString(), trg);
		
		src = "`a'b,c:d;e-f/g\"h'";
		trg = "[`, a'b, ,, c, :, d, ;, e, -, f, /, g, \", h, ']";
		assertEquals(tok.getTokens(src).toString(), trg);
		
		src = "`a'b,c:d;e-f/g\"h'";
		trg = "[`, a'b, ,, c, :, d, ;, e, -, f, /, g, \", h, ']";
		assertEquals(tok.getTokens(src).toString(), trg);
		
		src = "a@b #c$d%e&f|g";
		trg = "[a@b, #, c, $, d, %, e, &, f, |, g]";
		assertEquals(tok.getTokens(src).toString(), trg);
		
		src = "e.g., i.e, (e.g.,";
		trg = "[e.g., ,, i.e, ,, (, e.g., ,]";
		assertEquals(tok.getTokens(src).toString(), trg);
		
		src = " \n \t";
		trg = "[]";
		assertEquals(tok.getTokens(src).toString(), trg);
		
		src = "\"John & Mary's dog,\" Jane thought (to herself).\n" + "\"What a #$%!\n" + "a- ``I like AT&T''.\"";
		trg = "[\", John, &, Mary, 's, dog, ,, \", Jane, thought, (, to, herself, ), ., \", What, a, #, $, %, !, a, -, ``, I, like, AT&T, '', ., \"]";
		assertEquals(tok.getTokens(src).toString(), trg);
		
		src = "I said at 4:45pm.";
		trg = "[I, said, at, 4:45, pm, .]";
		assertEquals(tok.getTokens(src).toString(), trg);
		
		src = "I can't believe they wanna keep 40% of that.\"``Whatcha think?''\"I don't --- think so...,\"";
		trg = "[I, ca, n't, believe, they, wan, na, keep, 40, %, of, that, ., \", ``, What, cha, think, ?, '', \", I, do, n't, ---, think, so, ..., ,, \"]";
		assertEquals(tok.getTokens(src).toString(), trg);
		
		src = "You `paid' US$170,000?!\nYou should've paid only$16.75.";
		trg = "[You, `, paid, ', US$, 170,000, ?!, You, should, 've, paid, only, $, 16.75, .]";
		assertEquals(tok.getTokens(src).toString(), trg);
		
		src = " 1. Buy a new Chevrolet (37%-owned in the U.S..) . 15%";
		trg = "[1, ., Buy, a, new, Chevrolet, (, 37, %, -, owned, in, the, U.S., ., ), ., 15, %]";
		assertEquals(tok.getTokens(src).toString(), trg);
	}
}
