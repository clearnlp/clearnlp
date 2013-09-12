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

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import com.carrotsearch.hppc.cursors.ObjectCursor;

/**
 * Output utilities.
 * @author Jinho D. Choi (jdchoi77@gmail.com)
 */
public class UTOutput
{
	/** @return new PrintStream(new BufferedOutputStream(new FileOutputStream(filename), 65536), false, "UTF-8") */
	static public PrintStream createPrintBufferedFileStream(String filename)
	{
		PrintStream fout = null;
		
		try
		{
			fout = new PrintStream(new BufferedOutputStream(new FileOutputStream(filename), 65536), false, "UTF-8");
		}
		catch (Exception e) {e.printStackTrace();}
		
		return fout;
	}
	
	static public PrintStream createPrintBufferedStream(OutputStream stream)
	{
		PrintStream fout = null;
		
		try
		{
			fout = new PrintStream(new BufferedOutputStream(stream), false, "UTF-8");
		}
		catch (Exception e) {e.printStackTrace();}
		
		return fout;
	}
	
	static public ObjectOutputStream createObjectBufferedStream(OutputStream stream)
	{
		ObjectOutputStream fout = null;
		
		try
		{
			fout = new ObjectOutputStream(new BufferedOutputStream(stream));
		}
		catch (Exception e) {e.printStackTrace();}
		
		return fout;
	}
	
	/** @return new PrintStream(new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(filename)))) */
	static public PrintStream createPrintBufferedGZipFileStream(String filename)
	{
		PrintStream fout = null;
		
		try
		{
			fout = new PrintStream(new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(filename))));
		}
		catch (Exception e) {e.printStackTrace();}
		
		return fout;
	}
	
	static public void printSet(PrintStream fout, Set<String> set)
	{
		fout.println(set.size());
		for (String key : set)	fout.println(key);
	}
	
	static public void printMap(PrintStream fout, Map<String,String> map, String delim)
	{
		StringBuilder build;
		fout.println(map.size());
		
		for (String key : map.keySet())
		{
			build = new StringBuilder();
			
			build.append(key);
			build.append(delim);
			build.append(map.get(key));

			fout.println(build.toString());
		}
	}

	static public void printMap(PrintStream fout, ObjectIntOpenHashMap<String> map, String delim)
	{
		StringBuilder build;
		fout.println(map.size());
		
		for (ObjectCursor<String> cur : map.keys())
		{
			build = new StringBuilder();
			
			build.append(cur.value);
			build.append(delim);
			build.append(map.get(cur.value));

			fout.println(build.toString());
		}
	}
}
