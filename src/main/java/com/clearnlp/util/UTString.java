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

import java.util.regex.Pattern;


public class UTString
{
	static final private Pattern PUNCT_FRONT = Pattern.compile("^\\p{Punct}+");
	static final private Pattern PUNCT_BACK  = Pattern.compile("\\p{Punct}+$");
	
	static public String stripPunctuation(String str)
	{
		str = PUNCT_FRONT.matcher(str).replaceAll("");
		str = PUNCT_BACK .matcher(str).replaceAll("");
		return str.trim();
	}
	
	static public boolean isAllUpperCase(String str)
	{
		int i, size = str.length();
		
		for (i=0; i<size; i++)
		{
			if (!Character.isUpperCase(str.charAt(i)))
				return false;
		}
		
		return true;
	}
	
	static public boolean isAllLowerCase(String str)
	{
		int i, size = str.length();
		
		for (i=0; i<size; i++)
		{
			if (!Character.isLowerCase(str.charAt(i)))
				return false;
		}
		
		return true;
	}
	
	static public boolean beginsWithUpperCase(String str)
	{
		return Character.isUpperCase(str.charAt(0));
	}
	
	static public int getNumOfCapitalsNotAtBeginning(String str)
	{
		int i, size = str.length(), n = 0;
		
		for (i=1; i<size; i++)
		{
			if (Character.isUpperCase(str.charAt(i)))
				n++;
		}
		
		return n;
	}
	
	static public boolean containsDigit(String str)
	{
		int i, size = str.length();
		
		for (i=0; i<size; i++)
		{
			if (Character.isDigit(str.charAt(i)))
				return true;
		}
		
		return false;
	}
	
	static public String[] getPrefixes(String form, int n)
	{
		int i, length = form.length() - 1;
		if (length < n)	n = length;	
		String[] prefixes = new String[n];
		
		for (i=0; i<n; i++)
			prefixes[i] = form.substring(0, i+1);
		
		return prefixes;
	}
	
	static public String[] getSuffixes(String form, int n)
	{
		int i, length = form.length() - 1;
		if (length < n)	n = length;	
		String[] suffixes = new String[n];
		
		for (i=0; i<n; i++)
			suffixes[i] = form.substring(length-i);
		
		return suffixes;
	}
	
	static public String convertFirstCharToUpper(String s)
	{
		 return Character.toString(s.charAt(0)).toUpperCase() + s.substring(1);
	}
	
	static public String convertFirstCharToLower(String s)
	{
		 return Character.toString(s.charAt(0)).toLowerCase() + s.substring(1);
	}
}
