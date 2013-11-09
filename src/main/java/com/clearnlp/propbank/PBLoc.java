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

/**
 * PropBank location.
 * @since 1.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class PBLoc implements Comparable<PBLoc>
{
	/** Terminal ID (starting from 0). */
	public int terminalId;
	/** Height from the terminal (starting from 0). */
	public int height;
	/** {@code ""} (default) | {@code "*"} (linking) | {@code "&"} (passive linking) | {@code ","} (concatenation) | {@code ";"} (ICH concatenation) */
	public String type;
	
	/**
	 * Constructs a PropBank location.
	 * The type gets a default value of {@code ""}.
	 * @param terminalId {@link PBLoc#terminalId}.
	 * @param height {@link PBLoc#height}.
	 */
	public PBLoc(int terminalId, int height)
	{
		set(terminalId, height, "");
	}
	
	/**
	 * Constructs a PropBank location.
	 * @param terminalId {@link PBLoc#terminalId}.
	 * @param height {@link PBLoc#height}.
	 * @param type {@link PBLoc#type}.
	 */
	public PBLoc(int terminalId, int height, String type)
	{
		set(terminalId, height, type);
	}
	
	/**
	 * Constructs a PropBank location from the specific string and type.
	 * @param str {@code terminalId:height}.
	 * @param type {@link PBLoc#type}.
	 */
	public PBLoc(String str, String type)
	{
		String[] loc = str.split(PBLib.DELIM_LOC);
		
		try
		{
			terminalId = Integer.parseInt(loc[0]);
			height     = Integer.parseInt(loc[1]);
			this.type  = type;
		}
		catch (NumberFormatException e)
		{
			System.err.println("Error: illegal format - "+str);
			System.exit(1);
		}
	}
	
	/**
	 * Constructs a PropBank location using the terminal ID and height of the specific location and the specific type.
	 * @param loc the terminal ID and height to be assigned.
	 * @param type the type to be assigned.
	 */
	public PBLoc(PBLoc loc, String type)
	{
		set(loc, type);
	}

	/**
	 * Sets the terminal ID and height of this location.
	 * @param terminalId {@link PBLoc#terminalId}.
	 * @param height {@link PBLoc#height}.
	 */
	public void set(int terminalId, int height)
	{
		this.terminalId = terminalId;
		this.height     = height;
	}
	
	/**
	 * Sets the terminal ID, height and type of this location.
	 * @param terminalId {@link PBLoc#terminalId}.
	 * @param height {@link PBLoc#height}.
	 * @param type {@link PBLoc#type}.
	 */
	public void set(int terminalId, int height, String type)
	{
		this.terminalId = terminalId;
		this.height     = height;
		this.type       = type;
	}
	
	/**
	 * Sets the terminal ID, height and type of this location.
	 * @param loc the terminal ID and height to be assigned.
	 * @param type the type to be assigned.
	 */
	public void set(PBLoc loc, String type)
	{
		this.terminalId = loc.terminalId;
		this.height     = loc.height;
		this.type       = type;
	}
	
	/**
	 * Returns {@code true} if this location type equals to the specific type.
	 * @param type the type to be compared.
	 * @return {@code true} if this location type equals to the specific type.
	 */
	public boolean isType(String type)
	{
		return this.type.equals(type);
	}
	
	/**
	 * Returns {@code true} if this location matches the specific terminal ID and height.
	 * @param terminalId the terminal ID to be compared.
	 * @param height the height to be cmpared.
	 * @return {@code true} if this location matches the specific terminal ID and height.
	 */
	public boolean equals(int terminalId, int height)
	{
		return this.terminalId == terminalId && this.height == height;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		StringBuilder build = new StringBuilder();
		
		build.append(type);
		build.append(terminalId);
		build.append(PBLib.DELIM_LOC);
		build.append(height);
		
		return build.toString();
	}
	
	@Override
	public int compareTo(PBLoc loc)
	{
		if (terminalId == loc.terminalId)
			return height - loc.height;
		else
			return terminalId - loc.terminalId;
	}
}
