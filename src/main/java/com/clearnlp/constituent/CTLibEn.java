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

import java.util.List;
import java.util.regex.Pattern;

import com.clearnlp.morphology.MPLibEn;


/**
 * Constituent library for English.
 * @since 1.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class CTLibEn extends CTLib
{
	/** The phrase tag of declarative clauses. */
	static final public String PTAG_S		= "S";
	/** The phrase tag of subordinating clauses. */
	static final public String PTAG_SBAR	= "SBAR";
	/** The phrase tag of wh-question clauses. */
	static final public String PTAG_SBARQ	= "SBARQ";
	/** The phrase tag of inverted declarative clauses. */
	static final public String PTAG_SINV	= "SINV";
	/** The phrase tag of yes-no question clauses. */
	static final public String PTAG_SQ		= "SQ";
	
	/** The phrase tag of adjective phrases. */
	static final public String PTAG_ADJP	= "ADJP";
	/** The phrase tag of adverb phrases. */
	static final public String PTAG_ADVP	= "ADVP";
	/** The phrase tag of caption phrases. */
	static final public String PTAG_CAPTION	= "CAPTION";
	/** The phrase tag of citation phrases. */
	static final public String PTAG_CIT		= "CIT";
	/** The phrase tag of conjunction phrases. */
	static final public String PTAG_CONJP	= "CONJP";
	/** The phrase tag of edited phrases. */
	static final public String PTAG_EDITED	= "EDITED";
	/** The phrase tag of embedded phrases. */
	static final public String PTAG_EMBED	= "EMBED";
	/** The phrase tag of fragments. */
	static final public String PTAG_FRAG	= "FRAG";
	/** The phrase tag of headings. */
	static final public String PTAG_HEADING	= "HEADING";
	/** The phrase tag of interjections. */
	static final public String PTAG_INTJ	= "INTJ";
	/** The phrase tag of list markers. */
	static final public String PTAG_LST		= "LST";
	/** The phrase tag of meta phrases. */
	static final public String PTAG_META	= "META";
	/** The phrase tag of "not a constituent". */
	static final public String PTAG_NAC		= "NAC";
	/** The phrase tag of nominal phrases. */
	static final public String PTAG_NML		= "NML";
	/** The phrase tag of noun phrases. */
	static final public String PTAG_NP		= "NP";
	/** The phrase tag of complex noun phrases. */
	static final public String PTAG_NX		= "NX";
	/** The phrase tag of prepositional phrases. */
	static final public String PTAG_PP		= "PP";
	/** The phrase tag of parenthetical phrases. */
	static final public String PTAG_PRN		= "PRN";
	/** The phrase tag of particles. */
	static final public String PTAG_PRT		= "PRT";
	/** The phrase tag of quantifier phrases. */
	static final public String PTAG_QP		= "QP";
	/** The phrase tag of reduced relative clauses. */
	static final public String PTAG_RRC		= "RRC";
	/** The phrase tag of titles. */
	static final public String PTAG_TITLE	= "TITLE";
	/** The phrase tag of types. */
	static final public String PTAG_TYPO	= "TYPO";
	/** The phrase tag of unlike coordinated phrase. */
	static final public String PTAG_UCP		= "UCP";
	/** The phrase tag of verb phrases. */
	static final public String PTAG_VP		= "VP"; 
	/** The phrase tag of wh-adjective phrases. */
	static final public String PTAG_WHADJP	= "WHADJP";
	/** The phrase tag of wh-adverb phrases. */
	static final public String PTAG_WHADVP	= "WHADVP";
	/** The phrase tag of wh-noun phrases. */
	static final public String PTAG_WHNP	= "WHNP";
	/** The phrase tag of wh-prepositional phrases. */
	static final public String PTAG_WHPP	= "WHPP";

	/** The pos tag of emails. */
	static final public String POS_ADD		= "ADD";
	/** The pos tag of affixes. */
	static final public String POS_AFX		= "AFX";
	/** The pos tag of coordinating conjunctions. */
	static final public String POS_CC		= "CC";
	/** The pos tag of cardinal numbers. */
	static final public String POS_CD		= "CD";
	/** The pos tag of codes. */
	static final public String POS_CODE		= "CODE";
	/** The pos tag of determiners. */
	static final public String POS_DT		= "DT";
	/** The pos tag of existentials. */
	static final public String POS_EX		= "EX";
	/** The pos tag of foreign words. */
	static final public String POS_FW		= "FW";
	/** The pos tag of prepositions or subordinating conjunctions. */
	static final public String POS_IN		= "IN";
	/** The pos tag of adjectives. */
	static final public String POS_JJ		= "JJ";
	/** The pos tag of comparative adjectives. */
	static final public String POS_JJR		= "JJR";
	/** The pos tag of superlative adjectives. */
	static final public String POS_JJS		= "JJS";
	/** The pos tag of list item markers. */
	static final public String POS_LS		= "LS";
	/** The pos tag of modals. */
	static final public String POS_MD		= "MD";
	/** The pos tag of singular or mass nouns. */
	static final public String POS_NN		= "NN";
	/** The pos tag of plural nouns. */
	static final public String POS_NNS		= "NNS";
	/** The pos tag of singular proper nouns. */
	static final public String POS_NNP		= "NNP"; 
	/** The pos tag of plural proper nouns. */
	static final public String POS_NNPS		= "NNPS";
	/** The pos tag of predeterminers. */
	static final public String POS_PDT		= "PDT";
	/** The pos tag of possessive endings. */
	static final public String POS_POS		= "POS";
	/** The pos tag of personal pronouns. */
	static final public String POS_PRP		= "PRP";
	/** The pos tag of possessive pronouns. */
	static final public String POS_PRPS		= "PRP$";
	/** The pos tag of adverbs. */
	static final public String POS_RB		= "RB";
	/** The pos tag of comparative adverbs. */
	static final public String POS_RBR		= "RBR";
	/** The pos tag of superlative adverbs. */
	static final public String POS_RBS		= "RBS";
	/** The pos tag of particles. */
	static final public String POS_RP		= "RP"; 
	/** The pos tag of "to". */
	static final public String POS_TO		= "TO";
	/** The pos tag of interjections. */
	static final public String POS_UH		= "UH";
	/** The pos tag of base form verbs. */
	static final public String POS_VB		= "VB";
	/** The pos tag of past tense verbs. */
	static final public String POS_VBD		= "VBD";
	/** The pos tag of gerunds. */
	static final public String POS_VBG		= "VBG";
	/** The pos tag of past participles. */
	static final public String POS_VBN		= "VBN";
	/** The pos tag of non-3rd person singular present verbs. */
	static final public String POS_VBP		= "VBP";
	/** The pos tag of 3rd person singular present verbs. */
	static final public String POS_VBZ		= "VBZ";
	/** The pos tag of wh-determiners. */
	static final public String POS_WDT		= "WDT";
	/** The pos tag of wh-pronouns. */
	static final public String POS_WP		= "WP";
	/** The pos tag of possessive wh-pronouns. */
	static final public String POS_WPS		= "WP$";
	/** The pos tag of wh-adverbs. */
	static final public String POS_WRB		= "WRB"; 
	
	/** The pos tag of dollar signs. */
	static final public String POS_DOLLAR	= "$";
	/** The pos tag of colons. */	
	static final public String POS_COLON	= ":";
	/** The pos tag of commas. */
	static final public String POS_COMMA	= ",";
	/** The pos tag of periods. */
	static final public String POS_PERIOD	= ".";
	/** The pos tag of left quotes. */
	static final public String POS_LQ		= "``";
	/** The pos tag of right quotes. */
	static final public String POS_RQ		= "''";
	/** The pos tag of left round brackets. */
	static final public String POS_LRB		= "-LRB-";
	/** The pos tag of right round brackets. */
	static final public String POS_RRB		= "-RRB-";
	/** The pos tag of hyphens. */
	static final public String POS_HYPH		= "HYPH";
	/** The pos tag of superfluous punctuation. */
	static final public String POS_NFP		= "NFP";
	/** The pos tag of symbols. */
	static final public String POS_SYM		= "SYM";
	/** Punctuation. */
	static final public String POS_PUNC		= "PUNC";
	
	/** The function tag of adverbials. */
	static final public String FTAG_ADV = "ADV";
	/** The function tag of benefactives. */
	static final public String FTAG_BNF = "BNF";
	/** The function tag of clefts. */
	static final public String FTAG_CLF = "CLF";
	/** The function tag of closely related constituents. */
	static final public String FTAG_CLR = "CLR";
	/** The function tag of directions. */
	static final public String FTAG_DIR = "DIR";
	/** The function tag of datives. */
	static final public String FTAG_DTV = "DTV";
	/** The function tag of et cetera. */
	static final public String FTAG_ETC = "ETC";
	/** The function tag of extents. */
	static final public String FTAG_EXT = "EXT";
	/** The function tag of headlines. */
	static final public String FTAG_HLN = "HLN";
	/** The function tag of imperatives. */
	static final public String FTAG_IMP = "IMP";
	/** The function tag of interrogative. */
	static final public String FTAG_INT = "INT";
	/** The function tag of logical subjects. */
	static final public String FTAG_LGS = "LGS";
	/** The function tag of locatives. */
	static final public String FTAG_LOC = "LOC";
	/** The function tag of manners. */
	static final public String FTAG_MNR = "MNR";
	/** The function tag of nominalizations. */
	static final public String FTAG_NOM = "NOM";
	/** The function tag of predicates. */
	static final public String FTAG_PRD = "PRD";
	/** The function tag of purposes. */
	static final public String FTAG_PRP = "PRP";
	/** The function tag of the locative complement of "put". */
	static final public String FTAG_PUT = "PUT";
	/** The function tag of surface subjects. */
	static final public String FTAG_SBJ = "SBJ";
	/** The function tag of direct speeches. */
	static final public String FTAG_SEZ = "SEZ";
	/** The function tag of temporals. */
	static final public String FTAG_TMP = "TMP";
	/** The function tag of topicalizations. */
	static final public String FTAG_TPC = "TPC";
	/** The function tag of titles. */
	static final public String FTAG_TTL = "TTL";
	/** The function tag of unfinished constituents. */
	static final public String FTAG_UNF = "UNF";
	/** The function tag of vocatives. */
	static final public String FTAG_VOC = "VOC";

	/** The empty category representing expletives ({@code *EXP*}). */
	static final public String EC_EXP	= "*EXP*";
	/** The empty category representing ellipsed materials ({@code *?*}). */
	static final public String EC_ESM	= "*?*";
	/** The empty category representing "interpret constituent here" ({@code *ICH*}). */
	static final public String EC_ICH	= "*ICH*";
	/** The empty category representing anti-placeholders of gappings ({@code *NOT*}). */
	static final public String EC_NOT	= "*NOT*";
	/** The empty category representing null complementizers ({@code 0}). */
	static final public String EC_ZERO	= "0";
	/** The empty category representing "permanet predictable ambiguity" ({@code *PPA*}). */
	static final public String EC_PPA	= "*PPA*";
	/** The empty category representing subject/object controls ({@code *PRO*}). */
	static final public String EC_PRO	= "*PRO*";
	/** The empty category representing "right node raising" ({@code *RNR*}). */
	static final public String EC_RNR	= "*RNR*";
	/** The empty category representing passive nulls ({@code *}). */
	static final public String EC_NULL	= "*";
	/** The empty category representing traces ({@code *T*}). */
	static final public String EC_TRACE	= "*T*";
	/** The empty category representing null units ({@code *U*}). */
	static final public String EC_UNIT	= "*U*";
	
	/** POS tags of complemenizers. */
	static final public Pattern RE_COMP_POS  = Pattern.compile("^(WDT|WP.*|WRB)$");
	/** Lower-cased word-forms of complementizers. */
	static final public Pattern RE_COMP_FORM = Pattern.compile("^(how|however|that|what|whatever|whatsoever|when|whenever|where|whereby|wherein|whereupon|wherever|which|whichever|whither|who|whoever|whom|whose|why)$");
	/** Passive empty categories ({@code *|*-\d}). */
	static final public Pattern RE_NULL = Pattern.compile("^(\\*|\\*-.+)$");
	/** WH phrases with antecedents. */
	static final public Pattern RE_COMP_LINK = Pattern.compile("^(WHNP|WHPP|WHADVP)$");
	/** Complementizers with antecedents. */
	static final public Pattern RE_COMP_LINK_FORM = Pattern.compile("^(0|that|when|where|whereby|wherein|whereupon|which|who|whom|whose)$");
	/** A regular expression of identifying ICH|PPA|RNR empty categories. */
	static final public Pattern RE_ICH_PPA_RNR = Pattern.compile("\\*(ICH|PPA|RNR)\\*.*");

	/**
	 * Fixes inconsistent function tags.
	 * Links antecedents of reduced passive nulls ({@code *}) and complementizers.
	 * @see CTLibEn#fixFunctionTags(CTTree)
	 * @see CTLibEn#linkReducedPassiveNulls(CTTree)
	 * @see CTLibEn#linkComplementizers(CTTree)	 
	 */
	static public void preprocessTree(CTTree tree)
	{
		fixFunctionTags(tree);
		linkReducedPassiveNulls(tree);
		linkComplementizers(tree);
	}
	
	//	======================== Linking ========================
	
	/**
	 * Returns {@code true} if the specific node represents a passive null ({@code *|*-\d}).
	 * @param node the node to be compared.
	 * @return {@code true} if the specific node represents a passive null ({@code *|*-\d}).
	 */
	static public boolean isPassiveNull(CTNode node)
	{
		if (node.isEmptyCategory() && RE_NULL.matcher(node.form).find() && node.parent != null)
		{
			node = node.parent;
			
			if (node.isPTag(PTAG_NP)  && node.s_fTags.isEmpty() &&
				node.parent != null   && node.parent.isPTag(PTAG_VP) &&
				node.i_siblingId > 0  && node.parent.getChild(node.i_siblingId-1).isPTagAny(POS_VBN, POS_VBD))
				return true;
		}
		
		return false;
	}
	
	/**
	 * Returns {@code true} if the specific node is a passive verb.
	 * @param node the node to be compared.
	 * @return {@code true} if the specific node is a passive verb.
	 */
	static public boolean isPassiveVerb(CTNode node)
	{
		if (!node.isPTag(POS_VBN))
			return false;
		
		CTNode tmp = node.getParent();
		
		if (tmp == null || !tmp.isPTag(PTAG_VP))
			return false;
		
		tmp = tmp.getParent();
		
		if (tmp == null || !tmp.isPTag(PTAG_VP))
			return true;
		
		if ((tmp = tmp.getFirstChild("+VB.*")) != null)
			return !MPLibEn.isHave(tmp.form);
		
		return true;
	}
	
	/**
	 * Returns {@code true} if the specific node is a complementizer.
	 * @param node the node to be compared.
	 * @return {@code true} if the specific node is a complementizer.
	 */
	static public boolean isComplementizer(CTNode node)
	{
		if (node.isPhrase())	return false;
		return RE_COMP_POS.matcher(node.pTag).find() || (node.isPTag(POS_NONE) && node.isForm("0"));
	}
	
	/**
	 * Returns the first complementizer under the specific node, or {@code null} if there is no such node.
	 * @param node the node to be compared.
	 * @return the first complementizer under the specific node, or {@code null} if there is no such node.
	 */
	static public CTNode getComplementizer(CTNode node)
	{
		if (!node.pTag.startsWith("WH"))
			return null;
		
		List<CTNode> terminals = node.getSubTerminals();
		
		if (node.isEmptyCategoryRec())
			return terminals.get(0);
		
		for (CTNode term : terminals)
		{
			if (RE_COMP_POS.matcher(term.pTag).find())
				return term;
		}
		
		for (CTNode term : terminals)
		{
			if (RE_COMP_FORM.matcher(term.form.toLowerCase()).find())
				return term;
		}
			
		return null;
	}
	
	/**
	 * Finds reduced passive nulls ({@code *}) and links them to their antecedents in the specific tree.
	 * This method links most but not all antecedents; especially ones related to parenthetical phrases and topicaliszation.
	 * @see CTLibEn#isPassiveNull(CTNode)
	 * @param tree the tree to be operated.
	 */
	static public void linkReducedPassiveNulls(CTTree tree)
	{
		linkReducedPassiveNullsAux(tree, tree.getRoot());
	}
	
	/** Called by {@link CTLibEn#linkComplementizers(CTTree)}. */
	static private void linkReducedPassiveNullsAux(CTTree tree, CTNode curr)
	{
		String npRegex = PTAG_NP+"|"+PTAG_NML;
		String vpRegex = PTAG_VP+"|"+PTAG_RRC+"|"+PTAG_UCP;
		
		if (isPassiveNull(curr) && curr.form.equals("*"))
		{
			CTNode parent = curr.parent;		// NP
			
			if (parent.parent.coIndex != -1)	// VP
			{
				List<CTNode> list = tree.getCoIndexedEmptyCategories(parent.parent.coIndex);
				if (list != null) parent = list.get(0);
			}
			
			CTNode vp = parent.getHighestChainedAncestor("+"+vpRegex);

			if (vp.parent.matchesPTag(npRegex) || vp.parent.hasFTag(FTAG_NOM))
			{
				curr.antecedent = vp.getPrevSibling("+"+npRegex);
				
				if (curr.antecedent == null)
					curr.antecedent = vp.getPrevSibling("+NN.*");
				
				if (curr.antecedent == null)
					curr.antecedent = vp.getPrevSibling(PTAG_QP);
				
				if (curr.antecedent == null)
					curr.antecedent = vp.getPrevSibling("-"+FTAG_NOM);
			}
			else if (vp.parent.matchesPTag("S.*"))
			{
				curr.antecedent = vp.getPrevSibling(PTAG_NP,"-"+FTAG_SBJ);
				
				if (curr.antecedent == null)	// VP-TPC
					curr.antecedent = vp.getNextSibling(PTAG_NP,"-"+FTAG_SBJ);
			}
		}
		
		for (CTNode child : curr.ls_children)
			linkReducedPassiveNullsAux(tree, child);
	}
	
	/**
	 * Finds complementizers and links them to their antecedents in the specific tree.
	 * This method links most but not all antecedents; especially when the complementizers are under {@code *-PRD} phrases.
	 * @see CTLibEn#getComplementizer(CTNode)
	 * @param tree the tree to be operated.
	 */
	static public void linkComplementizers(CTTree tree)
	{
		linkComlementizersAux(tree, tree.getRoot());
	}
	
	/** Called by {@link CTLibEn#linkComplementizers(CTTree)}. */
	static private void linkComlementizersAux(CTTree tree, CTNode curr)
	{
		if (RE_COMP_LINK.matcher(curr.pTag).find())
		{
			CTNode comp = getComplementizer(curr);
			CTNode sbar = curr.getHighestChainedAncestor(PTAG_SBAR);
			
			if (comp != null && sbar != null && !sbar.hasFTag(FTAG_NOM) && RE_COMP_LINK_FORM.matcher(comp.form.toLowerCase()).find())
			{
				if (sbar.coIndex != -1)
				{
					List<CTNode> ecs = tree.getCoIndexedEmptyCategories(sbar.coIndex);
					
					if (ecs != null)
					{
						for (CTNode ec : ecs)
						{
							if (ec.form.startsWith(EC_ICH) && ec.parent.isPTag(PTAG_SBAR))
							{
								sbar = ec.parent;
								break;
							}
						}						
					}
				}
				else if (sbar.parent != null && sbar.parent.isPTag(PTAG_UCP))
					sbar = sbar.getParent();
				
				CTNode p = sbar.parent, ante;
				if (p == null)	return;
				
				if (p.isPTag(PTAG_NP))
				{
					if ((ante = sbar.getPrevSibling(PTAG_NP)) != null)
						comp.antecedent = ante;
				}
				else if (p.isPTag(PTAG_ADVP))
				{
					if ((ante = sbar.getPrevSibling(PTAG_ADVP)) != null)
						comp.antecedent = ante;
				}
				else if (p.isPTag(PTAG_VP))
				{
					if ((ante = sbar.getPrevSibling("-"+FTAG_PRD)) != null)
					{
						if (sbar.hasFTag(FTAG_CLF) ||
						   (curr.isPTag(PTAG_WHNP)   && ante.isPTag(PTAG_NP)) ||
						   (curr.isPTag(PTAG_WHPP)   && ante.isPTag(PTAG_PP)) ||
						   (curr.isPTag(PTAG_WHADVP) && ante.isPTag(PTAG_ADVP)))
							comp.antecedent = ante;
					}
				}
			/*	else if (p.isPTag(PTAG_ADJP) && p.hasFTag(FTAG_PRD))
				{
					if ((ante = p.getTopChainedAncestor(PTAG_VP)) != null)
						p = ante;
					
					if ((ante = p.getPrevSibling("-"+FTAG_SBJ)) != null)	// SQ
						comp.antecedent = ante;
				}*/
				
				ante = comp.antecedent;
				
				while (ante != null && ante.isEmptyCategoryRec())
					ante = ante.getSubTerminals().get(0).getAntecedent();
				
				comp.antecedent = ante;
			}
		}
		else
		{
			for (CTNode child : curr.ls_children)
				linkComlementizersAux(tree, child);
		}
	}
	
	//	======================== Booleans ========================

	/**
	 * Returns {@code true} if the specific node contains coordination.
	 * @param node the constituent node.
	 * @return {@code true} if the specific node contains coordination.
	 */
	static public boolean containsCoordination(CTNode node)
	{
		return containsCoordination(node, node.getChildren());
	}
	
	/**
	 * Returns {@code true} if the specific list of siblings contains coordination.
	 * @param parent the parent of all siblings.
	 * @param siblings the list of siblings.
	 * @return {@code true} if the specific list of siblings contains coordination.
	 */
	static public boolean containsCoordination(CTNode parent, List<CTNode> siblings)
	{
		if (parent.isPTag(PTAG_UCP))
			return true;
		
		if (parent.isPTagAny(PTAG_NML, PTAG_NP) && containsEtc(siblings))
			return true;
		
		for (CTNode child : siblings)
		{
			if (isConjunction(child))
				return true;
		}

		return false;
	}
	
	/** Called by {@link CTLibEn#containsCoordination(CTNode, List)}. */
	static private boolean containsEtc(List<CTNode> children)
	{
		int i, size = children.size();
		CTNode child;
		
		for (i=size-1; i>0; i--)
		{
			child = children.get(i);
			
			if (isPunctuation(child))	continue;
			if (isEtc(child))			return true;
			break;
		}
		
		return false;
	}
	
	/**
	 * Returns {@code true} if the specific node is et cetera (e.g., etc).
	 * @param node the node to be compared.
	 * @return {@code true} if the specific node is et cetera (e.g., etc).
	 */
	static public boolean isEtc(CTNode node)
	{
		if (node.hasFTag(FTAG_ETC))
			return true;
		
		return node.getSubTerminals().get(0).form.equalsIgnoreCase("etc.");
	}
	
	/**
	 * Returns {@code true} if this node is either a conjunction or a seperator.
	 * @see CTLibEn#isConjunction(CTNode)
	 * @see CTLibEn#isSeparator(CTNode)
	 * @param node the node to be compared.
	 * @return {@code true} if this node is a conjunction.
	 */
	static public boolean isCoordinator(CTNode node)
	{
		return isConjunction(node) || isSeparator(node);
	}
	
	/**
	 * Returns {@code true} if this node is a conjunction.
	 * @param node the node to be compared.
	 * @return {@code true} if this node is a conjunction.
	 */
	static public boolean isConjunction(CTNode node)
	{
		return node.isPTag(POS_CC) || node.isPTag(PTAG_CONJP);
	}
	
	/**
	 * Returns {@code true} if this node is a separator.
	 * @param node the node to be compared.
	 * @return {@code true} if this node is a separator.
	 */
	static public boolean isSeparator(CTNode node)
	{
		return node.isPTag(POS_COMMA) || node.isPTag(POS_COLON);
	}
	
	/**
	 * Returns {@code true} if this node is a correlative conjunction.
	 * @param node the node to be compared.
	 * @return {@code true} if this node is a correlative conjunction.
	 */
	static public boolean isCorrelativeConjunction(CTNode node)
	{
		if (node.isPTag(POS_CC))
		{
			String form = node.form.toLowerCase();
			return form.equals("either") || form.equals("neither") || form.equals("whether") || form.equals("both");
		}
		else if (node.isPTag(PTAG_CONJP))
		{
			String form = node.toForms(false, " ").toLowerCase();
			return form.equals("not only");
		}
		
		return false;
	}
	
	/**
	 * Returns {@code true} if the specific node is punctuation.
	 * @param node the node to be compared.
	 * @return {@code true} if the specific node is punctuation.
	 */
	static public boolean isPunctuation(CTNode node)
	{
		return node.isPTagAny(POS_COLON, POS_COMMA, POS_PERIOD, POS_LQ, POS_RQ, POS_LRB, POS_RRB, POS_HYPH, POS_NFP, POS_SYM, POS_PUNC);
	}
	
	/**
	 * Returns {@code true} if the specific node is a clause.
	 * @param node the node to be compared.
	 * @return {@code true} if the specific node is a clause.
	 */
	static public boolean isClause(CTNode node)
	{
		return node.isPTagAny(PTAG_S, PTAG_SQ, PTAG_SINV, PTAG_SBAR, PTAG_SBARQ);
	}
	
	/**
	 * Returns {@code true} if the specific node is an adjective.
	 * @param node the node to be compared.
	 * @return {@code true} if the specific node is an adjective.
	 */
	static public boolean isAdjective(CTNode node)
	{
		return MPLibEn.isAdjective(node.pTag);
	}
	
	/**
	 * Returns {@code true} if the specific node is an adverb.
	 * @param node the node to be compared.
	 * @return {@code true} if the specific node is an adverb.
	 */
	static public boolean isAdverb(CTNode node)
	{
		return MPLibEn.isAdverb(node.pTag);
	}
	
	/**
	 * Returns {@code true} if the specific node is a noun.
	 * @param node the node to be compared.
	 * @return {@code true} if the specific node is a noun.
	 */
	static public boolean isNoun(CTNode node)
	{
		return MPLibEn.isNoun(node.pTag);
	}
	
	/**
	 * Returns {@code true} if the specific node is a verb.
	 * @param node the node to be compared.
	 * @return {@code true} if the specific node is a verb.
	 */
	static public boolean isVerb(CTNode node)
	{
		return MPLibEn.isVerb(node.pTag);
	}
	
	/**
	 * Returns {@code true} if the specific node is a noun phrase.
	 * @param node the node to be compared.
	 * @return {@code true} if the specific node is a noun phrase.
	 */
	static public boolean isNounPhrase(CTNode node)
	{
		return node.isPTagAny(PTAG_NP, PTAG_NML, PTAG_NX, PTAG_NAC);
	}
	
	/**
	 * Returns {@code true} if the specific node is a kind of left bracket.
	 * In other words, checks if the word-form of the node matches {@code "^-L(R|S|C)B-$"}. 
	 * @param node the node to be compared.
	 * @return {@code true} if the specific node is a kind of left bracket.
	 */
	static public boolean isLeftBracket(CTNode node)
	{
		return !node.isPhrase() && node.form.matches("^-L(R|S|C)B-$");
	}
	
	/**
	 * Returns {@code true} if the specific node is a kind of right bracket.
	 * In other words, checks if the word-form of the node matches {@code "^-R(R|S|C)B-$"}. 
	 * @param node the node to be compared.
	 * @return {@code true} if the specific node is a kind of right bracket.
	 */
	static public boolean isRightBracket(CTNode node)
	{
		return !node.isPhrase() && node.form.matches("^-R(R|S|C)B-$");	
	}
	
	//	======================== function tag manipulation ========================

	/**
	 * Fixes inconsistent function tags in the specific tree.
	 * @see CTLibEn#fixSBJ(CTNode)
	 * @see CTLibEn#fixLGS(CTNode)
	 * @see CTLibEn#fixCLF(CTNode)
	 * @param tree the constituent tree.
	 */
	static public void fixFunctionTags(CTTree tree)
	{
		fixFunctionTagsAux(tree.getRoot());
	}
	
	/** Called by {@link CTLibEn#fixFunctionTags(CTTree)}. */
	static private void fixFunctionTagsAux(CTNode node)
	{
		fixSBJ(node);
		fixLGS(node);
		fixCLF(node);
		
		for (CTNode child : node.getChildren())
			fixFunctionTagsAux(child);
	}
	
	/**
	 * If the specific node contains the function tag {@link CTLibEn#FTAG_SBJ} and it is the only child of its parent, moves the tag to its parent.
	 * @param node the node to be processed.
	 */
	static private boolean fixSBJ(CTNode node)
	{
		if (node.hasFTag(FTAG_SBJ))
		{
			CTNode parent = node.getParent();
			
			if (parent.getChildrenSize() == 1 && !parent.isPTagAny(PTAG_EDITED, PTAG_EMBED) && parent.getFTags().isEmpty())
			{
				node.removeFTag(FTAG_SBJ);
				parent.addFTag(FTAG_SBJ);
				parent.pTag = node.pTag;
				
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * If the specific node contains the function tag {@link CTLibEn#FTAG_LGS} and it is not a prepositional phrase, moves the tag to its parent.
	 * @param node the node to be processed.
	 */
	static private boolean fixLGS(CTNode node)
	{
		if (node.hasFTag(FTAG_LGS) && !node.isPTag(PTAG_PP))
		{
			CTNode parent = node.getParent();
			
			if (parent.isPTagAny(PTAG_PP, PTAG_SBAR))
			{
				node.removeFTag(FTAG_LGS);
				parent.addFTag(FTAG_LGS);
				
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * If the specific node contains the function tag {@link CTLibEn#FTAG_CLF} and it is not a subordinate clause, moves the tag to the subordinate clause.
	 * @param node the node to be processed.
	 */
	static private boolean fixCLF(CTNode node)
	{
		if (node.hasFTag(FTAG_CLF) && node.matchesPTag("S|SQ|SINV"))
		{
			CTNode desc = node.getFirstDescendant("+SBAR.*");
			node.removeFTag(FTAG_CLF);
			
			if (desc != null)
			{
				desc.addFTag(FTAG_CLF);
				return true;
			}
		}
		
		return false;
	}
	
	static public boolean isRelPhrase(CTNode node)
	{
		return node.pTag.startsWith("WH");
	}
}
