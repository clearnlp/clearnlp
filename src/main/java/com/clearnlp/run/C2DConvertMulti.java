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
package com.clearnlp.run;

import java.io.BufferedReader;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.kohsuke.args4j.Option;

import com.carrotsearch.hppc.IntObjectOpenHashMap;
import com.carrotsearch.hppc.IntOpenHashSet;
import com.clearnlp.component.AbstractComponent;
import com.clearnlp.constituent.CTLibEn;
import com.clearnlp.constituent.CTNode;
import com.clearnlp.constituent.CTReader;
import com.clearnlp.constituent.CTTree;
import com.clearnlp.conversion.AbstractC2DConverter;
import com.clearnlp.dependency.DEPArc;
import com.clearnlp.dependency.DEPFeat;
import com.clearnlp.dependency.DEPLib;
import com.clearnlp.dependency.DEPLibEn;
import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dependency.DEPTree;
import com.clearnlp.dependency.srl.SRLArc;
import com.clearnlp.dependency.srl.SRLLib;
import com.clearnlp.morphology.MPLibEn;
import com.clearnlp.nlp.NLPGetter;
import com.clearnlp.propbank.PBArg;
import com.clearnlp.propbank.PBInstance;
import com.clearnlp.propbank.PBLib;
import com.clearnlp.propbank.PBLoc;
import com.clearnlp.reader.AbstractReader;
import com.clearnlp.util.UTFile;
import com.clearnlp.util.UTInput;
import com.clearnlp.util.UTOutput;
import com.clearnlp.util.pair.StringIntPair;
import com.google.common.collect.Lists;


public class C2DConvertMulti extends AbstractRun
{
	@Option(name="-i", usage="input path (required)", required=true, metaVar="<filepath>")
	private String s_inputPath;
	@Option(name="-h", usage="name of a headrule file (required)", required=true, metaVar="<filename>")
	private String s_headruleFile;
	@Option(name="-et", usage="parse-file extension (default: parse)", required=false, metaVar="<extension>")
	private String s_parseExt = "parse";
	@Option(name="-ep", usage="prop-file extension (default: prop)", required=false, metaVar="<extension>")
	private String s_propExt = "prop";
	@Option(name="-es", usage="sense-file extension (default: sense)", required=false, metaVar="<extension>")
	private String s_senseExt = "sense";
	@Option(name="-ev", usage="vclass-file extension (default: sl)", required=false, metaVar="<extension>")
	private String s_vclassExt = "sl";
	@Option(name="-en", usage="name-file extension (default: name)", required=false, metaVar="<extension>")
	private String s_nameExt = "name";
	@Option(name="-ed", usage="output-file extension (default: dep)", required=false, metaVar="<extension>")
	private String s_outputExt = "dep";
	@Option(name="-l", usage="language (default: "+AbstractReader.LANG_EN+")", required=false, metaVar="<language>")
	private String s_language = AbstractReader.LANG_EN;
	@Option(name="-m", usage="merge specified labels", required=false, metaVar="<string>")
	private String s_mergeLabels = null;
	@Option(name="-v", usage="if set, add only verb predicates in PropBank", required=false, metaVar="<boolean>")
	private boolean b_verbs_only = false;
	
	final Pattern P_SPACE  = Pattern.compile(" ");
	final Pattern P_HYPHEN = Pattern.compile("-");
	final Pattern P_COLON  = Pattern.compile(":");
	
	public C2DConvertMulti(String[] args) throws Exception
	{
		initArgs(args);
		convert(s_headruleFile, s_language, s_mergeLabels, s_inputPath, s_parseExt, s_propExt, s_senseExt, s_vclassExt, s_nameExt, s_outputExt);
	}
	
	public void convert(String headruleFile, String language, String mergeLabels, String inputPath, String parseExt, String propExt, String senseExt, String vclassExt, String nameExt, String outputExt) throws Exception
	{
		AbstractComponent morph = NLPGetter.getMPAnalyzer(s_language);
		AbstractC2DConverter c2d = NLPGetter.getC2DConverter(s_language, s_headruleFile, s_mergeLabels);
		
		convertRec(c2d, morph, language, inputPath, parseExt, propExt, senseExt, vclassExt, nameExt, outputExt);
	}
	
	private void convertRec(AbstractC2DConverter c2d, AbstractComponent morph, String language, String inputPath, String parseExt, String propExt, String senseExt, String vclassExt, String nameExt, String outputExt)
	{
		File file = new File(inputPath);
		
		if (file.isDirectory())
		{
			for (String filePath : file.list())
				convertRec(c2d, morph, language, inputPath+File.separator+filePath, parseExt, propExt, senseExt, vclassExt, nameExt, outputExt);
		}
		else if (inputPath.endsWith(parseExt))
		{
			System.out.println(inputPath);
			IntObjectOpenHashMap<List<PBInstance>>    mProp   = null;
			IntObjectOpenHashMap<List<StringIntPair>> mSense  = null;
			IntObjectOpenHashMap<List<StringIntPair>> mVclass = null;
			IntObjectOpenHashMap<List<String>>        mName   = null;
			
			try
			{
				mProp   = getPBInstances(UTFile.replaceExtension(inputPath, propExt));
				mSense  = getWordSenses (UTFile.replaceExtension(inputPath, senseExt));
				mVclass = getVerbClasses(UTFile.replaceExtension(inputPath, vclassExt));
				mName   = getNames      (UTFile.replaceExtension(inputPath, nameExt));
			}
			catch (Exception e) {e.printStackTrace();}
			
			PrintStream fout = UTOutput.createPrintBufferedFileStream(UTFile.replaceExtension(inputPath, outputExt));
			CTReader reader = new CTReader(UTInput.createBufferedFileReader(inputPath));
			CTTree cTree; DEPTree dTree; int n;
			List<PBInstance> instances = null;
			
			for (n=0; (cTree = reader.nextTree()) != null; n++)
			{
				if (language.equals(AbstractReader.LANG_EN))
					CTLibEn.preprocessTree(cTree);
				
				if (mProp != null)
				{
					instances = mProp.get(n);
					addPBInstances(cTree, instances);
				}
				
				dTree = c2d.toDEPTree(cTree);
				
				if (dTree == null)
				{
				//	fout.println(getNullTree()+"\n");
				}
				else
				{
					if (morph   != null)	morph.process(dTree);
					if (mSense  != null)	addWordSenses(cTree, dTree, mSense.get(n), DEPLibEn.FEAT_WS);
					if (mVclass != null)	addWordSenses(cTree, dTree, mVclass.get(n), DEPLibEn.FEAT_VN);
					if (mName   != null)	addNames(cTree, dTree, mName.get(n));
					
					if (mProp != null)
					{
						addRolesets(cTree, dTree, instances);
						if (b_verbs_only) relabelLightVerb(dTree);
						DEPLibEn.postLabel(dTree);
					}
				
					dTree = getDEPTreeWithoutEdited(cTree, dTree);
					fout.println(dTree+"\n");					
				}
			}
			
			fout.close();
			reader.close();
		}
	}
	
	public DEPTree getDEPTreeWithoutEdited(CTTree cTree, DEPTree dTree)
	{
		IntOpenHashSet set = new IntOpenHashSet();
		addEditedTokensAux(cTree.getRoot(), set);
		int i, j, size = dTree.size();
		DEPTree tree = new DEPTree();
		DEPNode node;
		
		for (i=1,j=1; i<size; i++)
		{
			if (!set.contains(i))
			{
				node = dTree.get(i);
				node.id = j++;
				removeEditedHeads(node.getXHeads(), set);
				removeEditedHeads(node.getSHeads(), set);
				tree.add(node);
			}
		}
		
		return (tree.size() == 1) ? null : tree;
	}
	
	private void addEditedTokensAux(CTNode curr, IntOpenHashSet set)
	{
		for (CTNode child : curr.getChildren())
		{
			if (child.isPTag(CTLibEn.PTAG_EDITED) || (child.getChildrenSize() == 1 && child.getChild(0).isPTag(CTLibEn.PTAG_EDITED)))
			{
				for (CTNode sub : child.getSubTokens())
					set.add(sub.getTokenId()+1);
			}
			else if (child.isPhrase())
			{
				addEditedTokensAux(child, set);
			}
		}
	}
	
	private <T extends DEPArc>void removeEditedHeads(List<T> heads, IntOpenHashSet set)
	{
		List<T> remove = Lists.newArrayList();
		
		for (T arc : heads)
		{
			if (set.contains(arc.getNode().id))
				remove.add(arc);
		}
		
		heads.removeAll(remove);
	}
	
	private IntObjectOpenHashMap<List<PBInstance>> getPBInstances(String propFile)
	{
		if (!new File(propFile).isFile())	return null;
		IntObjectOpenHashMap<List<PBInstance>> map = new IntObjectOpenHashMap<List<PBInstance>>();
		List<PBInstance> list;
		
		for (PBInstance inst : PBLib.getPBInstanceList(propFile))
		{
			if (map.containsKey(inst.treeId))
				list = map.get(inst.treeId);
			else
			{
				list = new ArrayList<PBInstance>();
				map.put(inst.treeId, list);
			}
			
			list.add(inst);
		}
		
		return map;
	}
	
	private void addPBInstances(CTTree cTree, List<PBInstance> instances)
	{
		if (instances == null)	return;
		initPBArgs(cTree.getRoot());
		int    predTokenId;
		String label;
		CTNode cNode;
		
		for (PBInstance instance : instances)
		{
			if (isPBSkip(instance, cTree))	continue;
			predTokenId = cTree.getTerminal(instance.predId).getTokenId() + 1;
			
			for (PBArg arg : instance.getArgs())
			{
				if (arg.label.startsWith(PBLib.PB_LINK))
					continue;
				
				if (arg.label.endsWith("UNDEF"))
					continue;
				
				label = arg.isLabel(PBLib.PB_REL) ? PBLib.PB_C_V : "A"+arg.label.substring(3);
				
				for (PBLoc loc : arg.getLocs())
				{
					if (arg.isLabel(PBLib.PB_REL) && loc.terminalId == instance.predId)
						continue;
					
					cNode = cTree.getNode(loc);
					
					if (!cNode.isEmptyCategoryRec())
						cNode.pbArgs.add(new StringIntPair(label, predTokenId));
				}
			}
		}
	}
	
	private void relabelLightVerb(DEPTree tree)
	{
		int i, j, size = tree.size();
		DEPNode noun, head, arg;
		Set<DEPNode> verbs;
		SRLArc arc;
		
		for (i=1; i<size; i++)
		{
			noun = tree.get(i);
			
			if (MPLibEn.isNoun(noun.pos) && noun.getFeat(DEPLib.FEAT_PB) != null)
			{
				verbs = new HashSet<DEPNode>();
				
				for (DEPArc verb : noun.getSHeadsByLabel(SRLLib.ARGM_PRR))
					verbs.add(verb.getNode());
				
				for (j=1; j<size; j++)
				{
					if (i == j)	continue;
					arg = tree.get(j);
					
					if ((arc = arg.getSHead(noun)) != null)
					{
						head = arg.getHead();
					
						if (verbs.contains(head))
							arc.setNode(head);
						else
							arg.removeSHead(arc);
					}
				}
				
				noun.removeFeat(DEPLib.FEAT_PB);
			}
		}
	}
	
	private boolean isPBSkip(PBInstance instance, CTTree cTree)
	{
		if (PBLib.ILLEGAL_ROLESET.matcher(instance.roleset).find())
			return true;
		
		if (b_verbs_only)
		{
			if (!instance.isVerbPredicate() && !instance.isLVNounPredicate(cTree))
				return true;
		}
		
		return false;
	}
	
	private void initPBArgs(CTNode node)
	{
		node.pbArgs = new ArrayList<StringIntPair>();
		
		for (CTNode child : node.getChildren())
			initPBArgs(child);
	}
	
	private IntObjectOpenHashMap<List<StringIntPair>> getWordSenses(String senseFile) throws Exception
	{
		if (!new File(senseFile).isFile())	return null;
		IntObjectOpenHashMap<List<StringIntPair>> map = new IntObjectOpenHashMap<List<StringIntPair>>();
		BufferedReader fin = UTInput.createBufferedFileReader(senseFile);
		List<StringIntPair> list;
		String line, sense;
		int treeId, wordId;
		String[] tmp;
		
		while ((line = fin.readLine()) != null)
		{
			tmp    = P_SPACE.split(line);
			treeId = Integer.parseInt(tmp[1]);
			wordId = Integer.parseInt(tmp[2]);
			sense  = tmp[3].substring(0, tmp[3].length()-2)+"."+tmp[4];
			
			if (map.containsKey(treeId))
				list = map.get(treeId);
			else
			{
				list = new ArrayList<StringIntPair>();
				map.put(treeId, list);
			}
			
			list.add(new StringIntPair(sense, wordId));
		}
		
		fin.close();
		return map;
	}
	
	private IntObjectOpenHashMap<List<StringIntPair>> getVerbClasses(String vclassFile) throws Exception
	{
		if (!new File(vclassFile).isFile())	return null;
		IntObjectOpenHashMap<List<StringIntPair>> map = new IntObjectOpenHashMap<List<StringIntPair>>();
		BufferedReader fin = UTInput.createBufferedFileReader(vclassFile);
		List<StringIntPair> list;
		String line, vclass;
		int treeId, wordId;
		String[] tmp;
		
		while ((line = fin.readLine()) != null)
		{
			tmp    = P_SPACE.split(line);
			treeId = Integer.parseInt(tmp[1]);
			wordId = Integer.parseInt(tmp[2]);
			vclass = tmp[5];
			
			if (map.containsKey(treeId))
				list = map.get(treeId);
			else
			{
				list = new ArrayList<StringIntPair>();
				map.put(treeId, list);
			}
			
			list.add(new StringIntPair(vclass, wordId));
		}
		
		fin.close();
		return map;
	}
	
	private IntObjectOpenHashMap<List<String>> getNames(String nameFile) throws Exception
	{
		if (!new File(nameFile).isFile())	return null;
		IntObjectOpenHashMap<List<String>> map = new IntObjectOpenHashMap<List<String>>();
		BufferedReader fin = UTInput.createBufferedFileReader(nameFile);
		List<String> list;
		int treeId, i;
		String[] tmp;
		String line;
		
		while ((line = fin.readLine()) != null)
		{
			tmp    = P_SPACE.split(line);
			treeId = Integer.parseInt(tmp[1]);
			list   = new ArrayList<String>();
			
			for (i=2; i<tmp.length; i++)
				list.add(tmp[i]);

			map.put(treeId, list);
		}
		
		fin.close();
		return map;
	}
	
	private void addRolesets(CTTree cTree, DEPTree dTree, List<PBInstance> instances)
	{
		if (instances == null)	return;
		DEPNode pred;
		
		for (PBInstance inst : instances)
		{
			if (isPBSkip(inst, cTree))	continue;
			pred = dTree.get(cTree.getTerminal(inst.predId).getTokenId()+1);
			pred.addFeat(DEPLib.FEAT_PB, inst.roleset);
			
			if (s_language.equals(AbstractReader.LANG_EN))
				pred.lemma = inst.roleset.substring(0, inst.roleset.lastIndexOf("."));
		}
	}
	
	private void addWordSenses(CTTree cTree, DEPTree dTree, List<StringIntPair> p, String key)
	{
		if (p == null)	return;
		DEPNode node;
		
		for (StringIntPair sense : p)
		{
			node = dTree.get(cTree.getTerminal(sense.i).getTokenId()+1);
			node.addFeat(key, sense.s);
		}
	}
	
	private void addNames(CTTree cTree, DEPTree dTree, List<String> names)
	{
		if (names == null)	return;
		String[] t0, t1;
		int bIdx, eIdx, i, size = dTree.size();
		String ent;
		DEPNode node;
		
		for (i=1; i<size; i++)
			dTree.get(i).nament = "O";
		
		for (String name : names)
		{
			t0   = P_HYPHEN.split(name);
			t1   = P_COLON.split(t0[0]);
			ent  = t0[1];
			bIdx = Integer.parseInt(t1[0]);
			eIdx = Integer.parseInt(t1[1]);
			
			if (bIdx == eIdx)
			{
				node = dTree.get(cTree.getTerminal(bIdx).getTokenId()+1);
				node.nament = "U-"+ent;
			}
			else
			{
				node = dTree.get(cTree.getTerminal(bIdx).getTokenId()+1);
				node.nament = "B-"+ent;
				
				for (i=bIdx+1; i<eIdx; i++)
				{
					node = dTree.get(cTree.getTerminal(i).getTokenId()+1);
					node.nament = "I-"+ent;
				}
				
				node = dTree.get(cTree.getTerminal(eIdx).getTokenId()+1);
				node.nament = "L-"+ent;
			}
		}
	}
	
	public DEPTree getNullTree()
	{
		DEPTree tree = new DEPTree();
		
		DEPNode dummy = new DEPNode(1, "NULL", "NULL", "NULL", new DEPFeat());
		dummy.setHead(tree.get(0), "NULL");
		
		tree.add(dummy);
		tree.initXHeads();
		tree.initSHeads();
		
		return tree;
	}

	public static void main(String[] args)
	{
		try
		{
			new C2DConvertMulti(args);
		}
		catch (Exception e) {e.printStackTrace();}
	}
}
