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
package com.clearnlp.util;

public class UTMath
{
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
