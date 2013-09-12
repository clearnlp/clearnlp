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
package com.clearnlp.experiment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.carrotsearch.hppc.IntArrayList;
import com.clearnlp.constituent.CTLib;
import com.clearnlp.constituent.CTReader;
import com.clearnlp.constituent.CTTree;
import com.clearnlp.propbank.PBInstance;
import com.clearnlp.propbank.PBLib;
import com.clearnlp.propbank.verbnet.PVMap;
import com.clearnlp.propbank.verbnet.PVRoles;
import com.clearnlp.propbank.verbnet.PVRoleset;
import com.clearnlp.util.UTFile;
import com.clearnlp.util.UTInput;


public class SemlinkToOntoNotes
{
	public SemlinkToOntoNotes(String ontoDir, String ontoPBFile, String semDir, String semVNFile, String pvMapFile, String outputFile) throws Exception
	{
		List<PBInstance> pbList = PBLib.getPBInstanceList(ontoPBFile, ontoDir, false);
		Map<String,String> semMap = getSemlinkMap(ontoDir, semDir, semVNFile);
		PVMap pvMap = new PVMap(new FileInputStream(pvMapFile));
		int mono = 0, poly = 0, subcls = 0, supcls = 0, none = 0, skip = 0;
		PVRoleset pvRoleset;
		PVRoles   pvRoles;
		String    vncls;
		
		for (PBInstance inst : pbList)
		{
			pvRoleset = pvMap.getRoleset(inst.roleset);
			     
			if (pvRoleset != null)
			{
				pvRoles = null;
				
				if (pvRoleset.size() == 1)
				{
					pvRoles = pvRoleset.getSubVNRoles("");
					inst.annotator = "mono";
					mono++;
				}
				else
				{
					vncls = semMap.get(getKey(inst.treePath, inst.treeId, inst.predId));

					if (vncls != null)
					{
						if ((pvRoles = pvRoleset.get(vncls)) != null)
						{
							inst.annotator = "poly";
							poly++;
						}
						else if ((pvRoles = pvRoleset.getSubVNRoles(vncls)) != null)
						{
							inst.annotator = "subcls";
							subcls++;
						}
						else if ((pvRoles = pvRoleset.getSuperVNRoles(vncls)) != null)
						{
							inst.annotator = "supcls";
							supcls++;
						}
					}
				}
				
				if (pvRoles == null)
				{
					inst.annotator = "skip";
					skip++;					
				}
				else
				{
					pvRoles.addVBRoles(inst);
				}
			}
			else
				none++;
		}
		
		PBLib.printPBInstances(pbList, outputFile);
		
		System.out.println("Total     : "+pbList.size());
		System.out.println("Mononymous: "+mono);
		System.out.println("Polysemous: "+poly);
		System.out.println("Subclass  : "+subcls);
		System.out.println("Superclass: "+supcls);
		System.out.println("Skip      : "+skip);
		System.out.println("None      : "+none);
	}
	
	Map<String,String> getSemlinkMap(String ontoDir, String semDir, String semVNFile) throws Exception
	{
		BufferedReader fin = UTInput.createBufferedFileReader(semVNFile);
		CTReader reader1 = new CTReader(), reader2 = new CTReader();
		Map<String,String> map = new HashMap<String,String>();
		Set<String> skipPaths = new HashSet<String>();
		String line, ontoPath = "", semPath = "";
		int i, treeId, predId, prevId = -1;
		CTTree tree1 = null, tree2 = null;
		IntArrayList[] lists;
		IntArrayList list;
		String[] tmp;
		
		while ((line = fin.readLine()) != null)
		{
			tmp = line.split(" ");
			if (skipPaths.contains(tmp[0]))	continue;
			
			if (!semPath.equals(tmp[0]))
			{
				ontoPath = UTFile.replaceExtension("nw"+File.separator+tmp[0], "parse");
				
				if (!new File(ontoDir+File.separator+ontoPath).exists())
				{
					skipPaths.add(tmp[0]);
					continue;
				}
				
				semPath = tmp[0];
				prevId  = -1;
				
				reader1.close();
				reader2.close();
				
				reader1.open(UTInput.createBufferedFileReader(ontoDir+File.separator+ontoPath));
				reader2.open(UTInput.createBufferedFileReader(semDir +File.separator+semPath));
			}
			
			treeId = Integer.parseInt(tmp[1]);
			predId = Integer.parseInt(tmp[2]);
			
			for (i=0; i<treeId-prevId; i++)
			{
				tree1 = reader1.nextTree();
				tree2 = reader2.nextTree();
			}
			
			prevId = treeId;
			lists  = CTLib.getTokenMapList(tree2, tree1);
			
			if (lists == null)
			{
				skipPaths.add(tmp[0]);
				continue;
			}
			
			list = lists[tree2.getTerminal(predId).getTokenId()];
			
			if (list.size() > 1)
			{
				skipPaths.add(tmp[0]);
				continue;
			}
			
			predId = tree1.getToken(list.get(0)).getTerminalId();
			map.put(getKey(ontoPath, treeId, predId), tmp[4]);
		}
		
		return map;
	}
	
	String getKey(String treePath, int treeId, int predId)
	{
		StringBuilder build = new StringBuilder();
		
		build.append(treePath);	build.append("_");
		build.append(treeId);	build.append("_");
		build.append(predId);
		
		return build.toString();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		String ontoDir    = args[0];
		String ontoPBFile = args[1];
		String semDir     = args[2];
		String semVNFile  = args[3];
		String pvMapFile  = args[4];
		String outputFile = args[5];
		
		try
		{
			new SemlinkToOntoNotes(ontoDir, ontoPBFile, semDir, semVNFile, pvMapFile, outputFile);
		}
		catch (Exception e) {e.printStackTrace();}
	}
}
