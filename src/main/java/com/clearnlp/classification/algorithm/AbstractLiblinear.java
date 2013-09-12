/**
* Copyright 2012-2013 University of Massachusetts Amherst
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
package com.clearnlp.classification.algorithm;


/**
 * @since 1.3.2
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
abstract public class AbstractLiblinear extends AbstractOneVsAll
{
	protected final int MAX_ITER = 1000;
	
	protected double d_cost;
	protected double d_eps;
	protected double d_bias;
	
	public AbstractLiblinear(double cost, double eps, double bias)
	{
		d_cost = cost;
		d_eps  = eps;
		d_bias = (bias > 0) ? bias : 0;
	}
	
	/** @return y-value for diagonal matrix. */
	protected int GETI(byte[] y, int i)
	{
		return y[i] + 1;
	}
}
