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
package com.clearnlp.classification.algorithm;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clearnlp.classification.prediction.StringPrediction;
import com.clearnlp.util.UTMath;

/**
 * Abstract algorithm.
 * @since 1.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
abstract public class AbstractAlgorithm
{
	protected final Logger LOG = LoggerFactory.getLogger(this.getClass());
	
	/** The flag to indicate L2-regularized L1-loss support vector classification (dual). */
	static public final byte SOLVER_LIBLINEAR_LR2_L1_SVC = 0;
	/** The flag to indicate L2-regularized L2-loss support vector classification (dual). */
	static public final byte SOLVER_LIBLINEAR_LR2_L2_SVC = 1;
	/** The flag to indicate L2-regularized logistic regression (dual). */
	static public final byte SOLVER_LIBLINEAR_LR2_LR = 2;
	/** The flag to indicate adaptive gradient method using hinge loss. */
	static public final byte SOLVER_ADAGRAD_HINGE = 3;
	/** The flag to indicate adaptive gradient method using logistic regression. */
	static public final byte SOLVER_ADAGRAD_LR = 4;

	protected double[] getQD(List<int[]> xs, List<double[]> vs, double init, double bias)
	{
		int i, size = xs.size();
		double[] qd = new double[size];
		
		for (i=0; i<size; i++)
		{
			qd[i]  = init + UTMath.sq(bias);
			qd[i] += (vs != null) ? UTMath.squareSum(vs.get(i)) : xs.get(i).length;
		}
		
		return qd;
	}
	
	protected void normalize(double[] scores)
	{
		int i, size = scores.length;
		double d, sum = 0;
		
		for (i=0; i<size; i++)
		{
			d = Math.exp(scores[i]);
			scores[i] = d;
			sum += d;
		}
		
		for (i=0; i<size; i++)
			scores[i] /= sum;
	}
	
	static public void normalize(List<StringPrediction> ps)
	{
		int i, size = ps.size();
		StringPrediction p;
		double d, sum = 0;
		
		for (i=0; i<size; i++)
		{
			p = ps.get(i);
			d = Math.exp(p.score);
			p.score = d;
			sum += d;
		}
		
		for (i=0; i<size; i++)
			ps.get(i).score /= sum; 
	}
}
