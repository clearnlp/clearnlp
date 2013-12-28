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
package com.clearnlp.nlp.train;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.clearnlp.classification.algorithm.old.AbstractAdaGrad;
import com.clearnlp.classification.algorithm.old.AbstractAlgorithm;
import com.clearnlp.classification.feature.JointFtrXml;
import com.clearnlp.classification.model.AbstractModel;
import com.clearnlp.classification.model.StringModel;
import com.clearnlp.classification.train.AbstractTrainSpace;
import com.clearnlp.classification.train.StringTrainSpace;
import com.clearnlp.component.AbstractStatisticalComponent;
import com.clearnlp.dependency.DEPTree;
import com.clearnlp.nlp.AbstractNLP;
import com.clearnlp.reader.JointReader;
import com.clearnlp.run.AdaGradTrain;
import com.clearnlp.run.LiblinearTrain;
import com.clearnlp.util.UTInput;
import com.clearnlp.util.UTOutput;
import com.clearnlp.util.UTXml;
import com.clearnlp.util.pair.ObjectDoublePair;

/**
 * @since 1.3.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
abstract public class AbstractNLPTrainer extends AbstractNLP
{
	public void train(Element eConfig, JointFtrXml[] xmls, String[] trainFiles, String modelDir) throws Exception
	{
		JointReader reader = getJointReader(UTXml.getFirstElementByTagName(eConfig, TAG_READER));
		AbstractStatisticalComponent<?> component = getComponent(eConfig, reader, xmls, trainFiles, -1);
		component.save(new ObjectOutputStream(new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(modelDir+"/"+getMode())))));
	}
	
	/** @return a component trained on the specific training data. */
	abstract protected AbstractStatisticalComponent<?> getComponent(Element eConfig, JointReader reader, JointFtrXml[] xmls, String[] trainFiles, int devId);
	
	/** @return a component for developing. */
	abstract protected AbstractStatisticalComponent<?> getComponent(Element eTrain, String language, JointFtrXml[] xmls, StringModel[] models, Object[] lexica);
	
	/** @return a component for training. */
	abstract protected AbstractStatisticalComponent<?> getComponent(Element eTrain, String language, JointFtrXml[] xmls, StringTrainSpace[] spaces, StringModel[] models, Object[] lexica);
	
	/** @return string training spaces. */
	abstract protected StringTrainSpace[] getStringTrainSpaces(JointFtrXml[] xmls, Object[] lexica, int boot);
	
	abstract public String getMode();
	
//	====================================== TRAIN COMPONENTS ======================================
	
	/** @return a trained statistical component without using bootstrapping. */
	protected AbstractStatisticalComponent<?> getTrainedComponent(Element eConfig, JointReader reader, AbstractStatisticalComponent<?> collector, JointFtrXml[] xmls, String[] trainFiles, int devId)
	{
		Object[] lexica = getLexica(reader, collector, xmls, trainFiles, devId);
		StringTrainSpace[] spaces = getStringTrainSpaces(eConfig, xmls, trainFiles, null, lexica, 0, devId);
		Element eTrain = UTXml.getFirstElementByTagName(eConfig, getMode());
		int i, mSize = spaces.length;

		StringModel[] models = new StringModel[mSize];
		
		for (i=0; i<mSize; i++)
		{
			models[i] = (StringModel)getModel(eTrain, spaces[i], i);
			spaces[i].clear();
		}
		
		return getComponent(eTrain, getLanguage(eConfig), xmls, models, lexica);
	}
	
	/** @return a trained statistical component using bootstrapping. */
	protected AbstractStatisticalComponent<?> getTrainedComponentBoot(Element eConfig, JointReader reader, AbstractStatisticalComponent<?> collector, JointFtrXml[] xmls, String[] trainFiles, int devId) 
	{
		Object[] lexica = getLexica(reader, collector, xmls, trainFiles, devId);
		Element eTrain = UTXml.getFirstElementByTagName(eConfig, getMode());
		AbstractStatisticalComponent<?> processor = null;
		int nBoot = getNumerOfBootstraps(eTrain);
		StringModel[] models = null;
		int boot;
		
		for (boot=0; boot<=nBoot; boot++)
		{
			LOG.info(String.format("=== Bootstrap: %d ===\n", boot));
			processor = getTrainedComponent(eConfig, xmls, trainFiles, models, lexica, boot, devId);
			models = processor.getModels();
		}
		
		return processor;
	}
	
	protected AbstractStatisticalComponent<?> getTrainedComponent(Element eConfig, JointFtrXml[] xmls, String[] trainFiles, StringModel[] models, Object[] lexica, int boot, int devId)
	{
		StringTrainSpace[] spaces = getStringTrainSpaces(eConfig, xmls, trainFiles, models, lexica, boot, devId);
		Element eTrain = UTXml.getFirstElementByTagName(eConfig, getMode());
		int i, mSize = spaces.length;

		models = new StringModel[mSize];
		
		for (i=0; i<mSize; i++)
		{
			models[i] = (StringModel)getModel(eTrain, spaces[i], i);
			spaces[i].clear();
		}
		
		return getComponent(eTrain, getLanguage(eConfig), xmls, models, lexica);
	}

//	====================================== DEVELOP COMPONENTS ======================================
	
	/** Develops a component without bootsrapping. */
	protected void developComponent(Element eConfig, JointReader reader, JointFtrXml[] xmls, String[] trainFiles, String[] devFiles, AbstractStatisticalComponent<?> component, boolean generate, int devId) throws Exception
	{
		component = getTrainedComponent(eConfig, reader, component, xmls, trainFiles, devId);
		decode(reader, component, devFiles, "", generate);
	}
	
	/** Develops a component with bootsrapping. */
	protected void developComponentBoot(Element eConfig, JointReader reader, JointFtrXml[] xmls, String[] trainFiles, String[] devFiles, AbstractStatisticalComponent<?> component, boolean generate, int devId) throws Exception
	{
		Object[] lexica = getLexica(reader, component, xmls, trainFiles, devId);
		ObjectDoublePair<StringModel[]> p;
		double prevScore, currScore = 0;
		StringModel[] models = null;
		int boot = 0;
		
		do
		{
			LOG.info(String.format("=== Bootstrap: %d ===\n", boot));
			prevScore = currScore;
			p = developComponent(eConfig, reader, xmls, trainFiles, devFiles, models, lexica, boot, generate, devId);
			models = (StringModel[])p.o;
			currScore = p.d;
			boot++;
		}
		while (prevScore < currScore);
	}
	
	/** Called by {@link #developComponentBoot(Element, JointReader, JointFtrXml[], String[], String[], AbstractStatisticalComponent, String, int)}. */
	private ObjectDoublePair<StringModel[]> developComponent(Element eConfig, JointReader reader, JointFtrXml[] xmls, String[] trainFiles, String[] devFiles, StringModel[] models, Object[] lexica, int boot, boolean generate, int devId) throws Exception
	{
		AbstractStatisticalComponent<?> component = getTrainedComponent(eConfig, xmls, trainFiles, models, lexica, boot, devId);
		double score = decode(reader, component, devFiles, "."+boot, generate);
		
		return new ObjectDoublePair<StringModel[]>(component.getModels(), score);
	}
	
	protected double decode(JointReader reader, AbstractStatisticalComponent<?> component, String[] devFiles, String ext, boolean generate) throws Exception
	{
		PrintStream fout = null;
		DEPTree tree;
		
		for (String devFile : devFiles)
		{
			if (generate) fout = UTOutput.createPrintBufferedFileStream(devFile+ext);
			reader.open(UTInput.createBufferedFileReader(devFile));
			
			while ((tree = reader.next()) != null)
			{
				component.process(tree);
				if (generate) fout.println(toString(tree, getMode())+"\n");
			}
			
			reader.close();
			if (generate) fout.close();
		}
		
		component.printAccuracies();
		return component.getAccuracies()[0];
	}
	
//	====================================== HELPER METHODS ======================================
	
	protected Object[] getLexica(JointReader reader, AbstractStatisticalComponent<?> collector, JointFtrXml[] xmls, String[] trainFiles, int devId)
	{
		if (collector == null) return null;
		int i, size = trainFiles.length;
		DEPTree tree;
		
		LOG.info("Collecting lexica:\n");
		
		for (i=0; i<size; i++)
		{
			if (devId == i)	continue;
			reader.open(UTInput.createBufferedFileReader(trainFiles[i]));
			
			while ((tree = reader.next()) != null)
				collector.process(tree);
			
			reader.close();
			LOG.debug(".");
		}	LOG.debug("\n");
		
		return collector.getLexica();
	}
	
	protected JointFtrXml[] getFeatureTemplates(String[] featureFiles) throws Exception
	{
		int i, size = featureFiles.length;
		JointFtrXml[] xmls = new JointFtrXml[size];
		
		for (i=0; i<size; i++)
			xmls[i] = new JointFtrXml(new FileInputStream(featureFiles[i]));
		
		return xmls;
	}
	
//	====================================== TRAIN ======================================
	
	protected StringTrainSpace[] getStringTrainSpaces(Element eConfig, JointFtrXml[] xmls, String[] trainFiles, StringModel[] models, Object[] lexica, int boot, int devId)
	{
		Element eTrain = UTXml.getFirstElementByTagName(eConfig, getMode());
		int i, j, mSize = 1, size = trainFiles.length;
		int numThreads = getNumerOfThreads(eTrain);
		String language = getLanguage(eConfig);
		
		List<StringTrainSpace[]> lSpaces = new ArrayList<StringTrainSpace[]>();
		ExecutorService executor = Executors.newFixedThreadPool(numThreads);
		StringTrainSpace[] spaces;
		
		LOG.info("Collecting training instances:\n");
		
		for (i=0; i<size; i++)
		{
			if (devId != i)
			{
				lSpaces.add(spaces = getStringTrainSpaces(xmls, lexica, boot));
				executor.execute(new TrainTask(eConfig, trainFiles[i], getComponent(eTrain, language, xmls, spaces, models, lexica)));
			}
		}
		
		executor.shutdown();
		
		try
		{
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		}
		catch (InterruptedException e) {e.printStackTrace();}
		
		LOG.debug("\n");
		
		mSize = lSpaces.get(0).length;
		spaces = new StringTrainSpace[mSize];
		StringTrainSpace sp;

		for (i=0; i<mSize; i++)
		{
			spaces[i] = lSpaces.get(0)[i];
			
			if ((size = lSpaces.size()) > 1)
			{
				LOG.info("Merging training instances:\n");
				
				for (j=1; j<size; j++)
				{
					spaces[i].appendSpace(sp = lSpaces.get(j)[i]);
					sp.clear();
					LOG.debug(".");
				}	LOG.debug("\n");
			}
		}
		
		return spaces;
	}
	
	/** @return string training spaces using the same cutoff values. */
	protected StringTrainSpace[] getStringTrainSpaces(JointFtrXml[] xmls)
	{
		return getStringTrainSpaces(xmls, 0);
	}
	
	/**
	 * @param the index of the specific cutoff values.
	 * @return string training spaces using the specific cutoff values.
	 */
	protected StringTrainSpace[] getStringTrainSpaces(JointFtrXml[] xmls, int cIndex)
	{
		int i, size = xmls.length;
		StringTrainSpace[] spaces = new StringTrainSpace[size];
		
		for (i=0; i<size; i++)
			spaces[i] = new StringTrainSpace(false, xmls[i].getLabelCutoff(cIndex), xmls[i].getFeatureCutoff(cIndex));
		
		return spaces;
	}
	
	/** @return string training spaces using the same feature template. */
	protected StringTrainSpace[] getStringTrainSpaces(JointFtrXml xml, int size)
	{
		StringTrainSpace[] spaces = new StringTrainSpace[size];
		int i;
		
		for (i=0; i<size; i++)
			spaces[i] = new StringTrainSpace(false, xml.getLabelCutoff(0), xml.getFeatureCutoff(0));
		
		return spaces;
	}
	
	/** Called by {@link #getStringTrainSpaces(Element, JointFtrXml[], String[], StringModel[], Object[], String, int)}. */
	private class TrainTask implements Runnable
	{
		AbstractStatisticalComponent<?> j_component;
		JointReader j_reader;
		
		public TrainTask(Element eConfig, String trainFile, AbstractStatisticalComponent<?> component)
		{
			j_reader = getJointReader(UTXml.getFirstElementByTagName(eConfig, TAG_READER));
			j_reader.open(UTInput.createBufferedFileReader(trainFile));
			j_component = component;
		}
		
		public void run()
		{
			DEPTree tree;
			
			while ((tree = j_reader.next()) != null)
				j_component.process(tree);
			
			j_reader.close();
			LOG.debug(".");
		}
	}
	
//	====================================== MODEL ======================================
	
	protected AbstractModel getModel(Element eTrain, AbstractTrainSpace space, int index)
	{
		NodeList  list = eTrain.getElementsByTagName(TAG_ALGORITHM);
		int numThreads = getNumerOfThreads(eTrain);
		Element eAlgorithm;
		String  name;
		
		if (index >= list.getLength())
			index = 0;
		
		eAlgorithm = (Element)list.item(index);
		name       = UTXml.getTrimmedAttribute(eAlgorithm, TAG_NAME);
		
		if (name.equals("liblinear"))
		{
			byte solver = Byte  .parseByte  (UTXml.getTrimmedAttribute(eAlgorithm, "solver"));
			double cost = Double.parseDouble(UTXml.getTrimmedAttribute(eAlgorithm, "cost"));
			double eps  = Double.parseDouble(UTXml.getTrimmedAttribute(eAlgorithm, "eps"));
			double bias = Double.parseDouble(UTXml.getTrimmedAttribute(eAlgorithm, "bias"));

			return getLiblinearModel(space, numThreads, solver, cost, eps, bias);
		}
		else if (name.equals("adagrad"))
		{
			String  type    = UTXml.getTrimmedAttribute(eAlgorithm, "type");
			double  alpha   = Double .parseDouble(UTXml.getTrimmedAttribute(eAlgorithm, "alpha"));
			double  rho     = Double .parseDouble(UTXml.getTrimmedAttribute(eAlgorithm, "rho"));
			double  eps     = Double .parseDouble(UTXml.getTrimmedAttribute(eAlgorithm, "eps"));
			byte    solver  = type.equals("hinge") ? AbstractAlgorithm.SOLVER_ADAGRAD_HINGE : AbstractAlgorithm.SOLVER_ADAGRAD_LR;
			boolean average = UTXml.getTrimmedAttribute(eAlgorithm, "average").equalsIgnoreCase("true");
			
			return getAdaGradModel(space, solver, alpha, rho, eps, average);
		}
		
		return null;
	}
	
	/** Called by {@link #getModel(Element, AbstractTrainSpace, int, int)}. */
	protected AbstractModel getLiblinearModel(AbstractTrainSpace space, int numThreads, byte solver, double cost, double eps, double bias)
	{
		space.build();
		LOG.info(String.format("Liblinear: solver=%d, cost=%5.3f, eps=%5.3f, bias=%5.3f\n", solver, cost, eps, bias));
		return LiblinearTrain.getModel(space, numThreads, solver, cost, eps, bias);
	}
	
	/** Called by {@link #getModel(Element, AbstractTrainSpace, int, int)}. */
	protected AbstractModel getAdaGradModel(AbstractTrainSpace space, byte solver, double alpha, double rho, double eps, boolean average)
	{
		space.build();
		LOG.info(String.format("AdaGrad: solver=%d, alpha=%5.3f, rho=%5.3f, eps=%5.3f, average=%b\n", solver, alpha, rho, eps, average));
		return AdaGradTrain.getModel(space, solver, alpha, rho, eps, average);
	}
	
	protected AbstractModel updateModel(Element eTrain, AbstractTrainSpace space, int index, int boot)
	{
		NodeList  list = eTrain.getElementsByTagName(TAG_ALGORITHM);
		Element eAlgorithm;
		String  name;
		
		if (index >= list.getLength())
			index = 0;
		
		eAlgorithm = (Element)list.item(index);
		name       = UTXml.getTrimmedAttribute(eAlgorithm, TAG_NAME);
		
		if (name.equals("adagrad"))
		{
			String  type    = UTXml.getTrimmedAttribute(eAlgorithm, "type");
			double  alpha   = Double .parseDouble(UTXml.getTrimmedAttribute(eAlgorithm, "alpha"));
			double  rho     = Double .parseDouble(UTXml.getTrimmedAttribute(eAlgorithm, "rho"));
			double  eps     = Double .parseDouble(UTXml.getTrimmedAttribute(eAlgorithm, "eps"));
			byte    solver  = type.equals("hinge") ? AbstractAlgorithm.SOLVER_ADAGRAD_HINGE : AbstractAlgorithm.SOLVER_ADAGRAD_LR;
			boolean average = UTXml.getTrimmedAttribute(eAlgorithm, "average").equalsIgnoreCase("true");
			
			return getAdaGradModel(space, solver, alpha, rho, eps, average);
		}
		
		return null;
	}
	
	protected AbstractModel updateAdaGradModel(AbstractTrainSpace space, byte solver, double alpha, double rho, double eps, boolean average)
	{
		space.build();
		LOG.info(String.format("AdaGrad: solver=%d, alpha=%5.3f, rho=%5.3f, eps=%5.3f, average=%b\n", solver, alpha, rho, eps, average));
		
		AbstractAdaGrad algorithm = AdaGradTrain.getAlgorithm(solver, alpha, rho, eps);
		algorithm.updateWeights(space, average);
		
		return AdaGradTrain.getModel(space, solver, alpha, rho, eps, average);
	}
}
