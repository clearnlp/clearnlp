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
package com.clearnlp.classification.train;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.carrotsearch.hppc.IntArrayList;
import com.clearnlp.classification.model.AbstractModel;
import com.clearnlp.classification.vector.AbstractFeatureVector;
import com.clearnlp.util.UTInput;


/**
 * Abstract train space.
 * @since 1.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
abstract public class AbstractTrainSpace
{
	protected final Logger LOG = Logger.getLogger(this.getClass());
	
	/** The flag to indicate sparse vector space. */
	static public final byte VECTOR_SPARSE = 0;
	/** The flag to indicate string vector space. */
	static public final byte VECTOR_STRING = 1;

	/** The delimiter between columns ({@code " "}). */
	static public final String DELIM_COL = " ";
	
	/** The abstract model to be saved. */
	protected AbstractModel       m_model;
	/** {@code true} if features are assigned with different weights. */
	protected boolean             b_weight;
	/** The list of training labels. */
	protected IntArrayList        a_ys;
	/** The list of training feature indices. */
	protected ArrayList<int[]>    a_xs;
	/** The list of training feature weights. */
	protected ArrayList<double[]> a_vs;
	
	/**
	 * Constructs an abstract train space.
	 * @param model the model to be trained.
	 * @param hasWeight {@code true} if features are assigned with different weights.
	 */
	public AbstractTrainSpace(AbstractModel model, boolean hasWeight)
	{
		m_model  = model;
		b_weight = hasWeight;
		a_ys     = new IntArrayList();
		a_xs     = new ArrayList<int[]>();
		if (hasWeight)	a_vs = new ArrayList<double[]>();
	}
	
	/**
	 * Reads training instances from the specific reader.
	 * The reader is closed after this method is called.
	 * @param reader the reader to read training instances from.
	 */
	public void readInstances(BufferedReader reader)
	{
		LineNumberReader fin = new LineNumberReader(reader);
		String line;
	
		LOG.info("Reading: ");
		
		try
		{
			while ((line = fin.readLine()) != null)
			{
				addInstance(line);
				if (fin.getLineNumber()%10000 == 0)	LOG.debug(".");
			}
			
			fin.close();
			LOG.info("\rReading: "+fin.getLineNumber()+"\n");
		}
		catch (IOException e) {e.printStackTrace();}
	}
	
	/**
	 * Adds a training instance to this space.
	 * @param see the description in each sub-class.
	 */
	abstract public void addInstance(String line);
	
	/** Generates vector space given training instances. */
	abstract public void build(boolean clearInstances);
	
	/** Generates vector space given training instances. */
	abstract public void build();
	
	/**
	 * Returns the list of training labels.
	 * @return the list of training labels.
	 */
	public IntArrayList getYs()
	{
		return a_ys;
	}
	
	/**
	 * Returns the list of training feature indices.
	 * @return the list of training feature indices.
	 */
	public ArrayList<int[]>getXs()
	{
		return a_xs;
	}
	
	/**
	 * Returns the list of training feature weights.
	 * @return the list of training feature weights.
	 */
	public ArrayList<double[]> getVs()
	{
		return a_vs;
	}
	
	/**
	 * Returns {@code true} if features are assigned with different weights. 
	 * @return {@code true} if features are assigned with different weights.
	 */
	public boolean hasWeight()
	{
		return b_weight;
	}
	
	/**
	 * Returns the total number of training instances.
	 * @return the total number of training instances.
	 */
	public int getInstanceSize()
	{
		return a_ys.size();
	}
	
	/**
	 * Returns the total number of labels.
	 * @return the total number of labels.
	 */
	public int getLabelSize()
	{
		return m_model.getLabelSize();
	}
	
	/**
	 * Returns the total number of features.
	 * @return the total number of features.
	 */
	public int getFeatureSize()
	{
		return m_model.getFeatureSize();
	}
	
	/**
	 * Returns {@code true} if there are only 2 labels.
	 * @return {@code true} if there are only 2 labels.
	 */
	public boolean isBinaryLabel()
	{
		return m_model.isBinaryLabel();
	}
	
	/**
	 * Returns the trained model.
	 * @return the trained model.
	 */
	public AbstractModel getModel()
	{
		return m_model;
	}
	
	/**
	 * Returns {@code true} if features are assigned with different weights. 
	 * @param vectorType the type of vector space.
	 * @param filename the name of the file containing training instances.
	 * @see AbstractTrainSpace#VECTOR_SPARSE
	 * @see AbstractTrainSpace#VECTOR_STRING
	 * @return {@code true} if features are assigned with different weights.
	 * @throws IOException
	 */
	static public boolean hasWeight(byte vectorType, String filename) throws IOException
	{
		BufferedReader fin = UTInput.createBufferedFileReader(filename);
		String[] tmp = fin.readLine().split(AbstractTrainSpace.DELIM_COL);
		int i, idx0, idx1, size = tmp.length;
		String str;
		
		fin.close();
		
		for (i=1; i<size; i++)
		{
			str  = tmp[i];
			idx0 = str.indexOf(AbstractFeatureVector.DELIM);
			if (idx0 == -1)	return false;
			
			if (vectorType == AbstractTrainSpace.VECTOR_STRING)
			{
				idx1 = str.lastIndexOf(AbstractFeatureVector.DELIM);
				if (idx1 == -1 || idx0 == idx1)	return false;
			}
		}
		
		return true;
	}
	
	public void printInstances(PrintStream fout)
	{
		int i, j, len, size = a_ys.size();
		int[] xs; double[] vs;
		StringBuilder build;
		
		for (i=0; i<size; i++)
		{
			build = new StringBuilder();
			build.append(a_ys.get(i));
			
			xs  = a_xs.get(i);
			vs  = (b_weight) ? a_vs.get(i) : null;
			len = xs.length;
			
			for (j=0; j<len; j++)
			{
				build.append(DELIM_COL);
				build.append(xs[j]);
				
				if (b_weight)
				{
					build.append(AbstractFeatureVector.DELIM);
					build.append(vs[j]);
				}
			}
			
			fout.println(build.toString());
		}
	}
}
