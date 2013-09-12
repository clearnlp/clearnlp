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
package com.clearnlp.component.srl;

import java.io.ObjectInputStream;

import com.clearnlp.classification.feature.JointFtrXml;
import com.clearnlp.classification.model.StringModel;
import com.clearnlp.classification.train.StringTrainSpace;
import com.clearnlp.component.state.SRLState;
import com.clearnlp.dependency.DEPNode;
import com.clearnlp.propbank.frameset.AbstractFrames;
import com.clearnlp.propbank.frameset.PBType;

/**
 * @since 1.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class DefaultSRLabeler extends AbstractSRLabeler
{
	/** Constructs a semantic role labeler for collecting lexica. */
	public DefaultSRLabeler(JointFtrXml[] xmls, AbstractFrames frames)
	{
		super(xmls, frames);
	}
	
	/** Constructs a semantic role labeler for training. */
	public DefaultSRLabeler(JointFtrXml[] xmls, StringTrainSpace[] spaces, Object[] lexica)
	{
		super(xmls, spaces, lexica);
	}
	
	/** Constructs a semantic role labeler for developing. */
	public DefaultSRLabeler(JointFtrXml[] xmls, StringModel[] models, Object[] lexica)
	{
		super(xmls, models, lexica);
	}
	
	/** Constructs a semantic role labeler for decoding. */
	public DefaultSRLabeler(ObjectInputStream in)
	{
		super(in);
	}
	
	/** Constructs a semantic role labeler for bootstrapping. */
	public DefaultSRLabeler(JointFtrXml[] xmls, StringTrainSpace[] spaces, StringModel[] models, Object[] lexica)
	{
		super(xmls, spaces, models, lexica);
	}

	@Override
	protected String getHardLabel(SRLState state, String label) {return null;}
	
	@Override
	protected PBType getPBType(DEPNode pred) {return null;}
	
	@Override
	protected void postLabel(SRLState state) {}
}
