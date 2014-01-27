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
 * Copyright 2012/09-2013/04, University of Massachusetts Amherst
 * Copyright 2013/05-Present, IPSoft Inc.
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
package com.clearnlp.pattern;

import java.util.List;
import java.util.regex.Pattern;

import com.clearnlp.util.pair.Pair;
import com.google.common.collect.Lists;

/**
 * @since 2.0.2
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class PTHtml
{
	private List<Pair<Pattern,String>> l_replaces;
	
	public PTHtml()
	{
		l_replaces = Lists.newArrayList();
		
		addSymbols();
		addASCII();
	}
	
	private void addSymbols()
	{
		addPattern("&quot;", "\"");
		addPattern("&amp;" , "&");
		addPattern("&lt;"  , "<");
		addPattern("&gt;"  , ">");
		addPattern("&nbsp;", " ");
	}
	
	private void addASCII()
	{
		addPattern("&#32;", " ");
		addPattern("&#33;", "!");
		addPattern("&#34;", "\"");
		addPattern("&#35;", "#");
		addPattern("&#36;", "$");
		addPattern("&#37;", "%");
		addPattern("&#38;", "&");
		addPattern("&#39;", "'");
		addPattern("&#40;", "(");
		addPattern("&#41;", ")");
		addPattern("&#42;", "*");
		addPattern("&#43;", "+");
		addPattern("&#44;", ",");
		addPattern("&#45;", "-");
		addPattern("&#46;", ".");
		addPattern("&#47;", "/");
		addPattern("&#48;", "0");
		addPattern("&#49;", "1");
		addPattern("&#50;", "2");
		addPattern("&#51;", "3");
		addPattern("&#52;", "4");
		addPattern("&#53;", "5");
		addPattern("&#54;", "6");
		addPattern("&#55;", "7");
		addPattern("&#56;", "8");
		addPattern("&#57;", "9");
		addPattern("&#58;", ":");
		addPattern("&#59;", ";");
		addPattern("&#60;", "<");
		addPattern("&#61;", "=");
		addPattern("&#62;", ">");
		addPattern("&#63;", "?");
		addPattern("&#64;", "@");
		addPattern("&#65;", "A");
		addPattern("&#66;", "B");
		addPattern("&#67;", "C");
		addPattern("&#68;", "D");
		addPattern("&#69;", "E");
		addPattern("&#70;", "F");
		addPattern("&#71;", "G");
		addPattern("&#72;", "H");
		addPattern("&#73;", "I");
		addPattern("&#74;", "J");
		addPattern("&#75;", "K");
		addPattern("&#76;", "L");
		addPattern("&#77;", "M");
		addPattern("&#78;", "N");
		addPattern("&#79;", "O");
		addPattern("&#80;", "P");
		addPattern("&#81;", "Q");
		addPattern("&#82;", "R");
		addPattern("&#83;", "S");
		addPattern("&#84;", "T");
		addPattern("&#85;", "U");
		addPattern("&#86;", "V");
		addPattern("&#87;", "W");
		addPattern("&#88;", "X");
		addPattern("&#89;", "Y");
		addPattern("&#90;", "Z");
		addPattern("&#91;", "[");
		addPattern("&#92;", "\\");
		addPattern("&#93;", "]");
		addPattern("&#94;", "^");
		addPattern("&#95;", "_");
		addPattern("&#96;", "`");
		addPattern("&#97;", "a");
		addPattern("&#98;", "b");
		addPattern("&#99;", "c");
		addPattern("&#100;", "d");
		addPattern("&#101;", "e");
		addPattern("&#102;", "f");
		addPattern("&#103;", "g");
		addPattern("&#104;", "h");
		addPattern("&#105;", "i");
		addPattern("&#106;", "j");
		addPattern("&#107;", "k");
		addPattern("&#108;", "l");
		addPattern("&#109;", "m");
		addPattern("&#110;", "n");
		addPattern("&#111;", "o");
		addPattern("&#112;", "p");
		addPattern("&#113;", "q");
		addPattern("&#114;", "r");
		addPattern("&#115;", "s");
		addPattern("&#116;", "t");
		addPattern("&#117;", "u");
		addPattern("&#118;", "v");
		addPattern("&#119;", "w");
		addPattern("&#120;", "x");
		addPattern("&#121;", "y");
		addPattern("&#122;", "z");
		addPattern("&#123;", "{");
		addPattern("&#124;", "|");
		addPattern("&#125;", "}");
		addPattern("&#126;", "~");
		addPattern("&#039;", "'");
	}
	
	public void addPattern(String p, String s)
	{
		l_replaces.add(new Pair<Pattern,String>(Pattern.compile(p), s));
	}
	
	public String toText(String code)
	{
		for (Pair<Pattern,String> t : l_replaces)
			code = t.o1.matcher(code).replaceAll(t.o2);
		
		return code;
	}
}
