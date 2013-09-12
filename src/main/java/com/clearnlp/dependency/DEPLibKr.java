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
package com.clearnlp.dependency;

/**
 * Dependency library for English.
 * @since 1.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class DEPLibKr extends DEPLib
{
	// syntactic roles
	static public final String DEP_COMP		= "comp";
	static public final String DEP_COMIT	= "comit";
	static public final String DEP_OBJ 		= "obj";
	static public final String DEP_QUOT		= "quot";
	static public final String DEP_SBJ		= "sbj";
	static public final String DEP_INTJ		= "intj";
	static public final String DEP_TPC		= "tpc";
	
	static public final String DEP_SUB		= "sub";
	
	static public final String DEP_AUX		= "aux";
	static public final String DEP_ADN		= "adn";
	static public final String DEP_ADV		= "adv";
	static public final String DEP_CC		= "cc";
	static public final String DEP_CONJ		= "conj";
	static public final String DEP_DEP  	= "dep";
	static public final String DEP_EJX		= "ejx";
	static public final String DEP_PUNCT	= "p";
	static public final String DEP_PRN		= "prn";
	static public final String DEP_ROOT 	= "root";

	static public final String DEP_AMOD		= "amod";
	static public final String DEP_NMOD		= "nmod";
	static public final String DEP_VMOD		= "vmod";
}
