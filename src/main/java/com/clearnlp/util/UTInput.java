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
package com.clearnlp.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import com.clearnlp.constant.universal.UNPunct;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Input utilities.
 * @author Jinho D. Choi (jdchoi77@gmail.com)
 */
public class UTInput
{
	public static Map<String,byte[]> toByteMap(ZipInputStream stream) throws IOException
	{
		Map<String,byte[]> map = Maps.newHashMap();
		ZipEntry zEntry;
		
		while ((zEntry = stream.getNextEntry()) != null)
			map.put(zEntry.getName(), UTInput.toByteArray(stream));

		stream.close();
		return map;
	}
	
	public static byte[] toByteArray(ZipInputStream stream) throws IOException
	{
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int count;
		
		while ((count = stream.read(buffer)) != -1)
			bout.write(buffer, 0, count);
         
		return bout.toByteArray();
	}
	
	public static InputStream getInputStreamsFromClasspath(String filename)
	{
		return UTInput.class.getResourceAsStream(UNPunct.FORWARD_SLASH+filename);
	}
	
	static public Set<String> getStringSet(InputStream in) throws IOException
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		Set<String> set = Sets.newHashSet();
		String line;
		
		while ((line = reader.readLine()) != null)
			set.add(line.trim());
		
		return set;
	}
	
	static public Map<String,String> getStringMap(InputStream in, Pattern delim) throws IOException
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		Map<String,String> map = Maps.newHashMap();
		String[] tmp;
		String line;
		
		while ((line = reader.readLine()) != null)
		{
			tmp = delim.split(line.trim());
			if (tmp.length == 2) map.put(tmp[0], tmp[1]);
		}
		
		return map;
	}
	
	/** @return new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8")) */
	static public BufferedReader createBufferedFileReader(String filename)
	{
		BufferedReader reader = null;
		
		try
		{
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"));
		}
		catch (Exception e) {e.printStackTrace();}
		
		return reader;
	}
	
	static public BufferedReader createBufferedReader(InputStream stream)
	{
		BufferedReader reader = null;
		
		try
		{
			reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
		}
		catch (Exception e) {e.printStackTrace();}
		
		return reader;
	}
	
	/** @return new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(filename)))) */
	static public BufferedReader createBufferedGZipFileReader(String filename)
	{
		BufferedReader reader = null;
		
		try
		{
			reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(filename))));
		}
		catch (Exception e) {e.printStackTrace();}
		
		return reader;
	}
	
	/** @return new ZipInputStream(new FileInputStream(filename)) */
	static public ZipInputStream createZipFileInputStream(String filename)
	{
		ZipInputStream stream = null;
		
		try
		{
			stream = new ZipInputStream(new FileInputStream(filename));
		}
		catch (Exception e) {e.printStackTrace();}
		
		return stream;
	}

	static public Set<String> getStringSet(BufferedReader fin) throws Exception
	{
		Set<String> set = new HashSet<String>();
		int i, size = Integer.parseInt(fin.readLine());
		
		for (i=0; i<size; i++)
			set.add(fin.readLine());
		
		return set;
	}
	
	static public Map<String,String> getStringMap(BufferedReader fin, String delim) throws Exception
	{
		Map<String,String> map = new HashMap<String, String>();
		int i, size = Integer.parseInt(fin.readLine());
		String[] tmp;
		
		for (i=0; i<size; i++)
		{
			tmp = fin.readLine().split(delim);
			map.put(tmp[0], tmp[1]);
		}
		
		return map;
	}
	
	static public ObjectIntOpenHashMap<String> getStringIntOpenHashMap(BufferedReader fin, String delim) throws Exception
	{
		ObjectIntOpenHashMap<String> map = new ObjectIntOpenHashMap<String>();
		int i, size = Integer.parseInt(fin.readLine());
		String[] tmp;
		
		for (i=0; i<size; i++)
		{
			tmp = fin.readLine().split(delim);
			map.put(tmp[0], Integer.parseInt(tmp[1]));
		}
		
		return map;
	}
	
	static public ByteArrayInputStream toInputStream(String s)
	{
		return new ByteArrayInputStream(s.getBytes());
	}
}
