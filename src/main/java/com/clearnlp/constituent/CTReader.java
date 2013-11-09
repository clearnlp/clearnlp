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
package com.clearnlp.constituent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.StringTokenizer;

/**
 * Constituent tree reader.
 * @see CTTree 
 * @since 1.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class CTReader
{
	private LineNumberReader f_in;
	private Deque<String>    d_tokens;
	
	public CTReader() {}
	
	/**
	 * Creates a constituent tree reader from the specific reader.
	 * @param in an input reader, which gets internally wrapped with {@code new LineNumberReader(in)}.
	 */
	public CTReader(BufferedReader in)
	{
		open(in);
	}
	
	public void open(BufferedReader in)
	{
		f_in     = new LineNumberReader(in);
		d_tokens = new ArrayDeque<String>();
	}
	
	/** Closes the current reader. */
	public void close()
	{
		if (f_in != null)
		{
			try
			{
				f_in.close();
			}
			catch (IOException e) {e.printStackTrace();}			
		}
	}
	
	/**
	 * Returns the next tree, or {@code null} if there is no more tree.
	 * Returns {@code null} if the next tree is incomplete or erroneous.
	 * Automatically links antecedents of all co-indexed empty categories (see {@link CTNode#getAntecedent()}).
	 * Calls the protected method {@link CTReader#processLanguageSpecifics(CTTree)}.
	 * @return the next tree, or {@code null} if there is no more tree.
	 */
	public CTTree nextTree()
	{
		String token = nextToken(), tags;
		
		if (token == null)
			return null;
		
		if (!token.equals("("))
		{
			System.err.println("Error: \""+token+"\" found, \"(\" expected - line "+f_in.getLineNumber());
			return null;
		}
		
		CTNode root   = new CTNode(CTLib.PTAG_TOP, null);
		CTNode curr   = root, node;
		int nBrackets = 1, startLine = f_in.getLineNumber();
		
		while ((token = nextToken()) != null)
		{
			if (nBrackets == 1 && token.equals(CTLib.PTAG_TOP))
				continue;
			
			if (token.equals("("))
			{
				tags = nextToken();
				node = new CTNode(tags);
				curr.addChild(node);
				curr = node;
				nBrackets++;
			}
			else if (token.equals(")"))
			{
				curr = curr.parent;
				nBrackets--;
			}
			else
			{
				curr.form = token;
			}
			
			if (nBrackets == 0)
			{
				CTTree tree = new CTTree(root);
				return tree;
			}
		}
		
		System.err.println("Error: brackets mismatch - starting line "+startLine);
		return null;
	}
	
	public CTTree nextTree(int treeId)
	{
		CTTree tree = null;
		
		for (int i=0; i<=treeId; i++)
			tree = nextTree();
		
		return tree;
	}

	/** @return if exists ? next token : null. */
	private String nextToken()
	{
		if (d_tokens.isEmpty())
		{
			String line = null;
			
			try
			{
				line = f_in.readLine();
			}
			catch (IOException e) {e.printStackTrace();}

			if (line == null)
				return null;
			
			line = line.trim();
			if (line.isEmpty())
				return nextToken();
			
			StringTokenizer tok = new StringTokenizer(line, "() \t\n\r\f", true);
			String str;
			
			while (tok.hasMoreTokens())
			{
				str = tok.nextToken().trim();
				if (!str.isEmpty()) d_tokens.add(str);
			}
		}
		
		return d_tokens.pop();
	}
}
