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
package com.clearnlp.run;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.PrintStream;

import org.kohsuke.args4j.Option;

import com.clearnlp.classification.instance.StringInstance;
import com.clearnlp.classification.model.SparseModel;
import com.clearnlp.classification.model.StringModel;
import com.clearnlp.classification.prediction.StringPrediction;
import com.clearnlp.classification.train.AbstractTrainSpace;
import com.clearnlp.classification.train.SparseTrainSpace;
import com.clearnlp.classification.train.StringTrainSpace;
import com.clearnlp.classification.vector.SparseFeatureVector;
import com.clearnlp.util.UTInput;
import com.clearnlp.util.UTOutput;
import com.clearnlp.util.pair.Pair;


/**
 * Predicts using a Liblinear model.
 * @since 0.1.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class LiblinearPredict extends AbstractRun
{
	@Option(name="-i", usage="the input file (input; required)", required=true, metaVar="<filename>")
	private String s_testFile;
	
	@Option(name="-o", usage="the output file (output; required)", required=true, metaVar="<filename>")
	private String s_outputFile;
	
	@Option(name="-m", usage="the model file (input; required)", required=true, metaVar="<filename>")
	private String s_modelFile;
	
	@Option(name="-v", usage="the type of vector space (default: "+AbstractTrainSpace.VECTOR_STRING+")\n"+
							AbstractTrainSpace.VECTOR_SPARSE+": sparse vector space\n"+
							AbstractTrainSpace.VECTOR_STRING+": string vector space\n",
			required=false, metaVar="<byte>")
	private byte i_vectorType = AbstractTrainSpace.VECTOR_STRING;
	
	public LiblinearPredict() {}
	
	public LiblinearPredict(String[] args)
	{
		initArgs(args);
		
		try
		{
			predict(s_testFile, s_outputFile, s_modelFile, i_vectorType);
		}
		catch (Exception e) {e.printStackTrace();}
	}
	
	public void predict(String testFile, String outputFile, String modelFile, byte vectorType) throws Exception
	{
		BufferedReader    fin = UTInput.createBufferedFileReader(testFile);
		PrintStream      fout = UTOutput.createPrintBufferedFileStream(outputFile);
		ObjectInputStream  in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(modelFile)));
		SparseModel    pModel = null;
		StringModel    sModel = null;
		
		switch (vectorType)
		{
		case AbstractTrainSpace.VECTOR_SPARSE:
			pModel = (SparseModel)in.readObject(); break;
		case AbstractTrainSpace.VECTOR_STRING:
			sModel = (StringModel)in.readObject(); break;
		}
		
		in.close();
		
		boolean hasWeight = AbstractTrainSpace.hasWeight(vectorType, testFile);
		int correct = 0, total = 0;
		StringPrediction r = null;
		String line, label = null;
		
		System.out.print("Predicting");
		
		while ((line = fin.readLine()) != null)
		{
			if (vectorType == AbstractTrainSpace.VECTOR_SPARSE)
			{
				Pair<String,SparseFeatureVector> sp = SparseTrainSpace.toInstance(line, hasWeight);
				r = pModel.predictBest(sp.o2);
				label = sp.o1;
			}
			else
			{
				StringInstance ss = StringTrainSpace.toInstance(line, hasWeight);
				r = sModel.predictBest(ss.getFeatureVector());
				label = ss.getLabel();
			}
			
			fout.println(r.label+" "+r.score);
			if (r.label.equals(label))	correct++;
			total++;
			
			if (total%10000 == 0)	System.out.print(".");
		}
		
		fin.close();
		fout.close();
		System.out.println();
		System.out.printf("Accuracy = %7.4f (%d/%d)\n", 100d*correct/total, correct, total);
	}
	
	static public void main(String[] args)
	{
		new LiblinearPredict(args);
	}
}
