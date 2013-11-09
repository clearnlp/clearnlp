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
package com.clearnlp.clustering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.carrotsearch.hppc.IntOpenHashSet;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import com.carrotsearch.hppc.cursors.IntCursor;
import com.clearnlp.dependency.DEPTree;
import com.clearnlp.pos.POSNode;
import com.clearnlp.util.pair.IntDoublePair;


/**
 * K-means clustering.
 * @since 1.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class Kmeans
{
	private final int RAND_SEED = 0;
	private int K, N, D;
	
	private ObjectIntOpenHashMap<String> m_lexica;
	private List<int[]> v_units;
	private double[] d_centroid;
	private double[] d_scala;
	
	public Kmeans()
	{
		m_lexica = new ObjectIntOpenHashMap<String>();
		v_units  = new ArrayList<int[]>();
	}
	
	public void addUnit(Set<String> lexica)
	{
		int index, i = 0, size = lexica.size();
		int[] unit = new int[size];
		
		for (String lexicon : lexica)
		{
			if (m_lexica.containsKey(lexicon))
			{
				index = m_lexica.get(lexicon);
			}
			else
			{
				index = m_lexica.size();
				m_lexica.put(lexicon, index);
			}
			
			unit[i++] = index;
		}

		Arrays.sort(unit);
		v_units.add(unit);
	}
	
	public void addUnit(POSNode[] nodes)
	{
		Set<String> lexica = new HashSet<String>();
		
		for (POSNode node : nodes)
			lexica.add(node.lemma);
				
		addUnit(lexica);
	}
	
	public void addUnit(DEPTree tree)
	{
		Set<String> lexica = new HashSet<String>();
		int i, size = tree.size();
		
		for (i=1; i<size; i++)
			lexica.add(tree.get(i).lemma);
				
		addUnit(lexica);
	}

	/**
	 * K-means clustering.
	 * @param threshold minimum RSS.
	 * @return each row represents a cluster, and
	 *         each column represents a pair of (index of a unit vector, similarity to the centroid).
	 */
	public List<List<IntDoublePair>> cluster(int k, double threshold)
	{
		List<List<IntDoublePair>> currCluster = null;
		List<List<IntDoublePair>> prevCluster = null;
		double prevRss = -1, currRss;
		
		K = k;
		N = v_units.size();
		D = m_lexica.size();
		
		initCentroids();
		int iter, max = N / K;
		
		for (iter=0; iter<max; iter++) 
		{
			System.out.printf("===== Iteration: %d =====\n", iter);
			
			currCluster = getClusters();
			updateCentroids(currCluster);
			currRss = getRSS(currCluster);
			
			if (prevRss >= currRss)		return prevCluster;
			if (currRss >= threshold)	break;
			
			prevRss     = currRss;
			prevCluster = currCluster;
		}

		return currCluster;
	}
	
	/** Initializes random centroids. */
	private void initCentroids()
	{
		IntOpenHashSet set = new IntOpenHashSet();
		Random rand = new Random(RAND_SEED);
		d_centroid  = new double[K*D];
		d_scala     = new double[K];
		
		while (set.size() < K)
			set.add(rand.nextInt(N));

		int[] unit;
		int k = 0;
		
		for (IntCursor cur : set)
		{
			unit = v_units.get(cur.value);
			
			for (int index : unit)
				d_centroid[getCentroidIndex(k, index)] = 1;
			
			d_scala[k++] = Math.sqrt(unit.length);
		}
	}
	
	/** @return centroid of each cluster. */
	private void updateCentroids(List<List<IntDoublePair>> cluster)
	{
		List<IntDoublePair> ck;
		int i, k, size;
		double scala;
		
		Arrays.fill(d_centroid, 0);
		Arrays.fill(d_scala   , 0);
		
		System.out.print("Updating centroids: ");
		
		for (k=0; k<K; k++)
		{
			ck = cluster.get(k);
			
			for (IntDoublePair p : ck)
			{
				for (int index : v_units.get(p.i))
					d_centroid[getCentroidIndex(k, index)] += 1;
			}
			
			size  = ck.size();
			scala = 0;
			
			for (i=k*D; i<(k+1)*D; i++)
			{
				if (d_centroid[i] > 0)
				{
					d_centroid[i] /= size;
					scala += d_centroid[i] * d_centroid[i];	
				}
			}
			
			d_scala[k] = Math.sqrt(scala);
			System.out.print(".");
		}
		
		System.out.println();
	}
	
	/** Each cluster contains indices of {@link Kmeans#v_units}. */
	private List<List<IntDoublePair>> getClusters()
	{
		List<List<IntDoublePair>> cluster = new ArrayList<List<IntDoublePair>>(K);
		IntDoublePair max = new IntDoublePair(-1, -1);
		int[] unit;
		int i, k;	double sim;
		
		for (k=0; k<K; k++)
			cluster.add(new ArrayList<IntDoublePair>());
		
		System.out.print("Clustering: ");
		
		for (i=0; i<N; i++)
		{
			unit = v_units.get(i);
			max.set(-1, -1);
			
			for (k=0; k<K; k++)
			{
				if ((sim = cosine(unit, k)) > max.d)
					max.set(k, sim);
			}
			
			cluster.get(max.i).add(new IntDoublePair(i, max.d));
			if (i%10000 == 0)	System.out.print(".");
		}
		
		System.out.println();
		
		for (k=0; k<K; k++)
			System.out.printf("- %4d: %d\n", k, cluster.get(k).size());
		
		return cluster;
	}
	
	/**
	 * @param k     [0, K-1].
	 * @param index [0, D-1].
	 */
	private int getCentroidIndex(int k, int index)
	{
		return k * D + index;
	}
	
	private double getRSS(List<List<IntDoublePair>> cluster)
	{
		double sim = 0;
		System.out.print("Calulating RSS: ");
		
		for (int k=0; k<K; k++)
		{
			for (IntDoublePair tup : cluster.get(k))
				sim += cosine(v_units.get(tup.i), k);
			
			System.out.print(".");
		}
		
		System.out.println();
		sim /= N;
		
		System.out.println("RSS = "+sim);
		return sim / N;
	}
	
	private double cosine(int[] unit, int k)
	{
		double dot = 0;
		
		for (int index : unit)
			dot += d_centroid[getCentroidIndex(k, index)];
		
		return dot / (Math.sqrt(unit.length) * d_scala[k]);
	}
}
