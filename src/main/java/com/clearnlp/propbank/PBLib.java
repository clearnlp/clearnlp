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
package com.clearnlp.propbank;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.clearnlp.constituent.CTLib;
import com.clearnlp.constituent.CTReader;
import com.clearnlp.constituent.CTTree;
import com.clearnlp.util.UTInput;
import com.clearnlp.util.UTOutput;


/**
 * PropBank library.
 * @since 1.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class PBLib
{
	static final public String PB_REL		= "rel";
	static final public String PB_DSP		= "DSP";
	static final public String PB_DIS	 	= "DIS";
	static final public String PB_ARG0		= "ARG0";
	static final public String PB_ARGM		= "ARGM";
	static final public String PB_ARGM_ADJ	= "ARGM-ADJ";
	static final public String PB_ARGM_MOD	= "ARGM-MOD";
	static final public String PB_ARGM_PRR	= "ARGM-PRR";
	static final public String PB_LINK		= "LINK";
	static final public String PB_LINK_SLC	= "LINK-SLC";
	static final public String PB_LINK_PRO	= "LINK-PRO";
	static final public String PB_LINK_PSV	= "LINK-PSV";
	static final public String PB_C_V 		= "C-V";
	static final public String LIGHT_VERB	= "LV";
	
	/** The delimiter between terminal ID and height ({@code ":"}). */
	static final public String DELIM_LOC  = ":";
	/** The delimiter between locations and label ({@code "-"}). */
	static final public String DELIM_LABEL  = "-";
	/** The delimiter between different fields ({@code " "}). */
	static final public String DELIM_INST = " ";
	/** The location operators ({@code "*&,;"}). */
	static final public String LOC_TYPES  = "*&,;";
	/** The pattern of illegal rolesets. */
	static final public Pattern ILLEGAL_ROLESET = Pattern.compile(".*\\.(ER|NN|IE|YY)");
	
	static final public Pattern P_ARGN      = Pattern.compile("^(A|C-A|R-A)(RG)?(\\d|A)");
	static public final Pattern P_ARGM      = Pattern.compile("^A(RG)?M");
	static final public Pattern P_ARGN_CORE = Pattern.compile("^A(RG)?(\\d|A)");
	
	/**
	 * Returns the sorted list of PropBank instances from the specific file. 
	 * @param propFile the PropBank file to retrieve instances from.
	 * @return the sorted list of PropBank instances from the specific file.
	 */
	static public List<PBInstance> getPBInstanceList(String propFile)
	{
		List<PBInstance> list = new ArrayList<PBInstance>();
		PBReader   reader = new PBReader(UTInput.createBufferedFileReader(propFile));
		PBInstance instance;
		
		while ((instance = reader.nextInstance()) != null)
			list.add(instance);

		reader.close();
		Collections.sort(list);
		return list;
	}
	
	/**
	 * Returns the sorted list of PropBank instances from the specific file.
	 * Each instance takes the constituent tree associated with it.
	 * @param propFile the PropBank file to retrieve instances from.
	 * @param treeDir the Treebank directory path.
	 * @param norm if {@code true}, normalize indices of constituent trees.
	 * @return the sorted list of PropBank instances from the specific file.
	 */
	static public List<PBInstance> getPBInstanceList(String propFile, String treeDir, boolean norm)
	{
		List<PBInstance> list = PBLib.getPBInstanceList(propFile);
		CTReader reader = new CTReader();
		CTTree   tree   = null;
		String treeFile = "";
		int    treeId   = -1;
		
		for (PBInstance instance : list)
		{
			if (!treeFile.equals(instance.treePath))
			{
				treeFile = instance.treePath;
				treeId   = -1;
				reader.close();
				reader.open(UTInput.createBufferedFileReader(treeDir+File.separator+treeFile));
			}
			
			for (; treeId < instance.treeId; treeId++)
				tree = reader.nextTree();
			
			if (norm)	CTLib.normalizeIndices(tree);
			tree.setPBLocs();
			instance.setTree(tree);
		}
		
		return list;
	}
	
	/**
	 * Returns a map using "treePath TreeId" as a key and a list of associated instances as a value.  
	 * Each instance takes the constituent tree associated with it.
	 * @param propFile the PropBank file to retrieve instances from.
	 * @param treeDir the Treebank directory path.
	 * @param norm if {@code true}, normalize indices of constituent trees.
	 * @return a map using "treePath TreeId" as a key and a list of associated instances as a value.
	 */
	static public Map<String,List<PBInstance>> getPBInstanceMap(String propFile, String treeDir, boolean norm)
	{
		Map<String,List<PBInstance>> map = new HashMap<String,List<PBInstance>>();
		List<PBInstance> list = null;
		String currkey, prevkey = "";
		
		for (PBInstance inst : PBLib.getPBInstanceList(propFile, treeDir, norm))
		{
			currkey = getTreePathId(inst);
			
			if (!currkey.equals(prevkey))
			{
				list = new ArrayList<PBInstance>();
				prevkey = currkey;
				map.put(currkey, list);
			}
			
			list.add(inst);
		}
		
		return map;
	}
	
	/** Called by {@link PBLib#getPBInstanceMap(String, String, boolean)}. */
	static private String getTreePathId(PBInstance inst)
	{
		StringBuilder build = new StringBuilder();
		
		build.append(inst.treePath);
		build.append(PBLib.DELIM_INST);
		build.append(inst.treeId);
		
		return build.toString();
	}
	
	/**
	 * Prints the list of PropBank instances to the specific file.
	 * @param instances the list of PropBank instances to print.
	 * @param outputFile the name of the file to print.
	 */
	static public void printPBInstances(List<PBInstance> instances, String outputFile)
	{
		PrintStream fout = UTOutput.createPrintBufferedFileStream(outputFile);
		
		for (PBInstance instance : instances)
			fout.println(instance.toString());
				
		fout.close();
	}
	
	/**
	 * Returns {@code true} if the specific argument is a numbered argument.
	 * In other words, returns {@code true} if the label of the argument is "ARG#". 
	 * @param arg the argument to be compared.
	 * @return {@code true} if the specific argument is a numbered argument.
	 */
	static public boolean isNumberedArgument(PBArg arg)
	{
		return arg.label.length() >= 4 && Character.isDigit(arg.label.charAt(3));
	}

	static public boolean isNumberedArgument(String label)
	{
		return P_ARGN.matcher(label).find();
	}

	static public boolean isCoreNumberedArgument(String label)
	{
		return P_ARGN_CORE.matcher(label).find();
	}
	
	static public boolean isModifier(String label)
	{
		return P_ARGM.matcher(label).find();
	}

	/**
	 * @return the number of an numbered argument (e.g., "0", "A").
	 * If the label is not a numbered argument, returns {@code null}. 
	 */
	static public String getNumber(String label)
	{
		Matcher m = P_ARGN.matcher(label);
		return m.find() ? m.group(3) : null;
	}
}
