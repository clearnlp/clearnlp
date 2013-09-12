/**
* Copyright 2013 IPSoft Inc.
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
package com.clearnlp.component;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.clearnlp.classification.feature.JointFtrXml;
import com.clearnlp.classification.model.StringModel;
import com.clearnlp.classification.train.StringTrainSpace;
import com.clearnlp.component.evaluation.AbstractEval;
import com.clearnlp.component.state.DefaultState;

/**
 * @since 1.4.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
abstract public class AbstractStatisticalComponentSB<T extends DefaultState> extends AbstractStatisticalComponent<T>
{
	protected int    n_beams;	// beam size
	protected double d_margin;	// margin threshold
	
//	====================================== CONSTRUCTORS ======================================
	
	public AbstractStatisticalComponentSB() {}
	
	/** Constructs a component for collecting lexica. */
	public AbstractStatisticalComponentSB(JointFtrXml[] xmls)
	{
		super(xmls);
	}
	
	/** Constructs a component for training. */
	public AbstractStatisticalComponentSB(JointFtrXml[] xmls, StringTrainSpace[] spaces, Object[] lexica, double margin, int beams)
	{
		super(xmls, spaces, lexica);
		init(margin, beams);
	}
	
	/** Constructs a component for developing. */
	public AbstractStatisticalComponentSB(JointFtrXml[] xmls, StringModel[] models, Object[] lexica, AbstractEval eval, double margin, int beams)
	{
		super(xmls, models, lexica, eval);
		init(margin, beams);
	}
	
	/** Constructs a component for bootstrapping. */
	public AbstractStatisticalComponentSB(JointFtrXml[] xmls, StringTrainSpace[] spaces, StringModel[] models, Object[] lexica, double margin, int beams)
	{
		super(xmls, spaces, models, lexica);
		init(margin, beams);
	}
	
	/** Constructs a component for decoding. */
	public AbstractStatisticalComponentSB(ObjectInputStream in)
	{
		super(in);
	}
	
	private void init(double margin, int beams)
	{
		d_margin = margin;
		n_beams  = beams;
	}
	
//	====================================== LOAD/SAVE MODELS ======================================

	protected void loadSB(ObjectInputStream in) throws Exception
	{
		LOG.info("Loading configuration.\n");
		
		n_beams  = in.readInt();
		d_margin = in.readDouble();
	}
	
	protected void saveSB(ObjectOutputStream out) throws Exception
	{
		LOG.info("Saving configuration.\n");
		
		out.writeInt(n_beams);
		out.writeDouble(d_margin);
	}
	
//	====================================== GETTERS/SETTERS ======================================
	
	public void setMargin(double margin)
	{
		d_margin = margin;
	}
	
	public void setBeams(int beams)
	{
		n_beams = beams;
	}
}
