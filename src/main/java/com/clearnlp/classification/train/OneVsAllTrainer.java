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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.clearnlp.classification.algorithm.old.AbstractOneVsAll;
import com.clearnlp.classification.model.AbstractModel;


/**
 * Trainer.
 * @since 1.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class OneVsAllTrainer
{
	private AbstractTrainSpace     t_space;
	private AbstractOneVsAll       a_algorithm;
	volatile private AbstractModel m_model;
	
	public OneVsAllTrainer(AbstractTrainSpace space, AbstractOneVsAll algorithm, int numThreads)
	{
		t_space     = space;
		a_algorithm = algorithm;
		m_model     = space.getModel();
		
		m_model.initWeightVector();
		
		if (space.isBinaryLabel())
			trainBinary();
		else
			trainMulti(numThreads);
	}
	
	private void trainBinary()
	{
		float[] weights = a_algorithm.getWeight(t_space, 0);
		m_model.copyWeights(weights);
	}
	
	private void trainMulti(int numThreads)
	{
		ExecutorService executor = Executors.newFixedThreadPool(numThreads);
		
		int currLabel, size = t_space.getLabelSize();
		
		for (currLabel=0; currLabel<size; currLabel++)
			executor.execute(new TrainTask(currLabel));
		
		executor.shutdown();
		
		try
		{
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		}
		catch (InterruptedException e) {e.printStackTrace();}
	}
	
	class TrainTask implements Runnable
	{
		/** The current label to train */
		int curr_label;
		
		/**
		 * Trains one-vs-all model.
		 * @param currLabel the current label to train.
		 */
		public TrainTask(int currLabel)
		{
			curr_label = currLabel;
		}
		
		public void run()
		{
			float[] weights = a_algorithm.getWeight(t_space, curr_label);
			m_model.copyWeights(weights, curr_label);
		}
    }
}
