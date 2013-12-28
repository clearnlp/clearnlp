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
package com.clearnlp.util;

public class UTMath
{
	static public double cosineSimilarity(double[] vector1, double[] vector2)
	{
		double d1, d2, prod = 0, d1s = 0, d2s = 0;
		int i, size = vector1.length;
		
		for (i=0; i<size; i++)
		{
			d1 = vector1[i];
			d2 = vector2[i];
			
			prod += d1 * d2;
			d1s  += d1 * d1;
			d2s  += d2 * d2;
		}
		
		return prod / (Math.sqrt(d1s) * Math.sqrt(d2s));
	}
	
	static public int[][] getCombinations(int n)
	{
		int i, j, k, v = 1, t = (int)Math.pow(2, n);
		int[][] b = new int[t][n];
		
		for (i=0; i<n; i++)
		{
			k = (int)Math.pow(2, i);
			
			for (j=0; j<t; j++)
			{
				if (j%k == 0)
					v = (v + 1) % 2;
				
				b[j][i] = v;
			}
		}
		
		return b;
	}
	
	static public double sq(double d)
	{
		return d * d;
	}

	static public double squareSum(double[] v)
	{
		double sum = 0;
		
		for (double d : v)
			sum += sq(d);
		
		return sum;
	}
	
	static public double mean(double... ds)
	{
		double sum = 0;
		
		for (double d : ds)
			sum += d;
		
		return sum / ds.length;
	}
	
	static public double variance(double... ds)
	{
		if (ds.length < 2)
			throw new IllegalArgumentException("The argument length must be greater than 1");
		
		double avg = mean(ds), sum = 0;
		
		for (double d : ds)
			sum += sq(d - avg);
		
		return sum / (ds.length - 1);
	}
	
	static public double stdev(double... ds)
	{
		return Math.sqrt(variance(ds));
	}

	static public double getF1(double precision, double recall)
	{
		return (precision + recall == 0) ? 0 : 2 * (precision * recall) / (precision + recall);
	}
	
	static public int signum(double d)
	{
		return (int)Math.signum(d);
	}
	
	static public int signnum(double l)
	{
		if      (l > 0)	return  1;
		else if (l < 0)	return -1;
		else			return  0;
	}
}
