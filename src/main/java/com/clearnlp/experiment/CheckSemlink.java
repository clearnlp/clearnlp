/**
* Copyright 2012-2013 University of Massachusetts Amherst
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*   http://www.apache.org/licenses/LICENSE-2.0
*   
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.clearnlp.experiment;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.clearnlp.propbank.verbnet.PVMap;
import com.clearnlp.util.UTInput;
import com.clearnlp.util.UTOutput;
import com.clearnlp.util.map.Prob2DMap;
import com.clearnlp.util.pair.Pair;
import com.clearnlp.util.pair.StringDoublePair;

public class CheckSemlink
{
	final Pattern DELIM = Pattern.compile(" ");
	
	public CheckSemlink(String pbvnFile, String semlinkFile, String propFile) throws Exception
	{
	//	check(pbvnFile, semlinkFile, propFile);
	//	PVMap pvMap = new PVMap(new BufferedInputStream(new FileInputStream(pbvnFile)));
	//	Set<String> semKeys = getSemlinkKeys(semlinkFile);
	//	countMoreAnnotations(pvMap, semKeys, propFile);
	}
	
	public CheckSemlink(String pbvnFile, String semlinkFile, String propFile, String outFile) throws Exception
	{
		PVMap pvMap = new PVMap(new BufferedInputStream(new FileInputStream(pbvnFile)));
		Pair<Set<String>,Map<String,String>> o = getSemlinkKeys(pvMap, semlinkFile, outFile);
		getAnnotations(o.o1, o.o2, propFile, outFile);
	}
	
	Set<String> getSemlinkKeys(String semlinkFile) throws Exception
	{
		BufferedReader fin = UTInput.createBufferedFileReader(semlinkFile);
		Set<String> set = new HashSet<String>();
		String line;
		String[] t;

		while ((line = fin.readLine()) != null)
		{
			t = DELIM.split(line);
			set.add(getKey(t));
		}
		
		return set;
	}
	
	void countMoreAnnotations(PVMap pvMap, Set<String> semKeys, String propFile) throws Exception
	{
		BufferedReader fin = UTInput.createBufferedFileReader(propFile);
		int c0 = 0, c1 = 0, c2 = 0, tc = 0, vc = 0, sc = 0, z;
		String line, v;
		String[] t;
		
		while ((line = fin.readLine()) != null)
		{
			t = DELIM.split(line);
			v = t[4];
			v = v.substring(0, v.length()-2);
			
			tc++;
			
			if (pvMap.containsKey(v))
			{
				vc++;
			
				if (!semKeys.contains(getKey(t)))
				{
					sc++;
					z = pvMap.getVNSet(t[5]).size();
					
					if      (z == 0)	c0++;
					else if (z == 1)	c1++;
					else 				c2++;
					
				}
			}
		}
		
		System.out.println(semKeys.size());
		System.out.println(c0+" "+c1+" "+c2+" "+tc+" "+vc+" "+sc);
	}
	
	void getAnnotations(Set<String> semKeys, Map<String,String> rolesets, String propFile, String outFile) throws Exception
	{
		BufferedReader fin = UTInput.createBufferedFileReader(propFile);
		PrintStream fout = UTOutput.createPrintBufferedFileStream(outFile+".poly");
		String line, roleset, vncls;
		String[] t;
		
		while ((line = fin.readLine()) != null)
		{
			t = DELIM.split(line);
			
			if (!semKeys.contains(getKey(t)))
			{
				roleset = t[5];
				vncls = rolesets.get(roleset);
				
				if (vncls != null)
					fout.println(toString(t, vncls));				
			}
		}
		
		fout.close();
	}
	
	String toString(String[] t, String vncls)
	{
		StringBuilder build = new StringBuilder();
		
		build.append(t[0]);		build.append(" ");
		build.append(t[1]);		build.append(" ");
		build.append(t[2]);		build.append(" ");
		build.append(t[3]);		build.append(" ");
		build.append(t[4]);		build.append(" ");
		build.append(vncls);	build.append(" ");
		build.append("null");	build.append(" ");
		build.append(t[5]);		build.append(" ");
		build.append("null");
		
		int i, size = t.length;
		
		for (i=6; i<size; i++)
		{
			build.append(" ");
			build.append(t[i]);		
		}
		
		return build.toString();
	}
	
	
	Pair<Set<String>,Map<String,String>> getSemlinkKeys(PVMap pvMap, String semlinkFile, String outFile) throws Exception
	{
		double threshold = 0.95;
		
		BufferedReader fin = UTInput.createBufferedFileReader(semlinkFile);
		PrintStream fout = UTOutput.createPrintBufferedFileStream(outFile+"."+threshold);
		Set<String> set = new HashSet<String>();
		Prob2DMap poly = new Prob2DMap();
		String line, roleset, verbnet;
		Set<String> vnset;
		String[] t;

		while ((line = fin.readLine()) != null)
		{
			t = DELIM.split(line);
			set.add(getKey(t));
			
			verbnet = t[5];
			roleset = t[7];
			vnset   = pvMap.getVNSet(roleset);
			
			if (vnset.size() > 1)
				poly.add(roleset, verbnet);
		}
		
		List<String> keys = new ArrayList<String>(poly.keySet());
		Map<String,String> map = new HashMap<String,String>();
		Collections.sort(keys);
		StringDoublePair[] ps;
		
		for (String key : keys)
		{
			ps = poly.getProb1D(key);
			Arrays.sort(ps);
			
			if (ps[0].d < threshold)
			{
				fout.println(toString(key, ps));
				map.put(key, toString(ps));
			}
		}
		
		fout.close();
		return new Pair<Set<String>,Map<String,String>>(set, map);
	}
	
	String toString(String key, StringDoublePair[] ps)
	{
		StringBuilder build = new StringBuilder();
		
		build.append(key);
		
		for (StringDoublePair p : ps)
		{
			build.append(" ");
			build.append(p.s);
			build.append(":");
			build.append(String.format("%5.4f", p.d));
		}
		
		return build.toString();
	}
	
	String toString(StringDoublePair[] ps)
	{
		StringBuilder build = new StringBuilder();
		
		for (StringDoublePair p : ps)
		{
			build.append("|");
			build.append(p.s);
		}
		
		return build.substring(1);
	}
	
	String getKey(String[] t)
	{
		return t[0]+" "+t[1]+" "+t[2];
	}
	
	public void check(String pbvnFile, String semlinkFile, String errorFile) throws Exception
	{
		PVMap pvMap = new PVMap(new BufferedInputStream(new FileInputStream(pbvnFile)));
		BufferedReader fin = UTInput.createBufferedFileReader(semlinkFile);
		String line, verbnet, roleset;
		Set<String> vnset;
		String[] t;
		
	//	Map<String,ObjectIntOpenHashMap<String>> miss = new HashMap<String,ObjectIntOpenHashMap<String>>();
	//	ObjectIntOpenHashMap<String> m;
		
		Map<String,Set<String>> miss = new HashMap<String,Set<String>>();
		Set<String> m;
		
		while ((line = fin.readLine()) != null)
		{
			t = DELIM.split(line);
			verbnet = t[5];
			roleset = t[7];
			vnset = pvMap.getVNSet(roleset);
			
			if (vnset.isEmpty() || !vnset.contains(verbnet))
			{
				m = miss.get(roleset);
				
				if (m == null)
				{
				//	m = new ObjectIntOpenHashMap<String>();
					m = new HashSet<String>();
					miss.put(roleset, m);
				}
				
			//	m.put(verbnet, m.get(verbnet)+1);
				m.add(verbnet);
			}
		}
		
		printErrors(miss, errorFile);
	}
	
	void printErrors(Map<String,Set<String>> map, String outputFile)
	{
		PrintStream fout = UTOutput.createPrintBufferedFileStream(outputFile);
		List<String> keys = new ArrayList<String>(map.keySet());
		Collections.sort(keys);
		
		for (String key: keys)
		{
			fout.println(key+" "+map.get(key).toString());
		}
		
		fout.close();
	}
	
	static public void main(String[] args)
	{
		try
		{
			new CheckSemlink(args[0], args[1], args[2], args[3]);
		}
		catch (Exception e) {e.printStackTrace();}
	}
}

