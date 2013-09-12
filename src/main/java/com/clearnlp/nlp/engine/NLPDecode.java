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
package com.clearnlp.nlp.engine;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.kohsuke.args4j.Option;
import org.w3c.dom.Element;

import com.clearnlp.io.FileExtFilter;
import com.clearnlp.nlp.NLPLib;
import com.clearnlp.nlp.decode.DEPDecoder;
import com.clearnlp.nlp.decode.MorphDecoder;
import com.clearnlp.nlp.decode.NLPDecoder;
import com.clearnlp.nlp.decode.POSDecoder;
import com.clearnlp.nlp.decode.SRLDecoder;
import com.clearnlp.util.UTArgs4j;
import com.clearnlp.util.UTXml;

/**
 * @since 1.3.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class NLPDecode
{
	@Option(name="-c", usage="configuration file (required)", required=true, metaVar="<filename>")
	private String s_configXml;
	@Option(name="-i", usage="input path (required)", required=true, metaVar="<filepath>")
	private String s_inputPath;
	@Option(name="-ie", usage="input file extension (default: .*)", required=false, metaVar="<regex>")
	private String s_inputExt = ".*";
	@Option(name="-oe", usage="output file extension (default: cnlp)", required=false, metaVar="<string>")
	private String s_outputExt = "cnlp";
	@Option(name="-z", usage="mode (pos|morph|dep|srl)", required=true, metaVar="<string>")
	protected String s_mode;
	
	public NLPDecode() {}
	
	public NLPDecode(String[] args)
	{
		UTArgs4j.initArgs(this, args);
		
		try
		{
			decode(s_configXml, s_inputPath, s_inputExt, s_outputExt, s_mode);
		}
		catch (Exception e) {e.printStackTrace();}
	}
	
	public void decode(String configXml, String inputPath, String inputExt, String outputExt, String mode)
	{
		try
		{
			Element eConfig = UTXml.getDocumentElement(new FileInputStream(configXml));
			List<String[]> filenames = getFilenames(inputPath, inputExt, outputExt);
			NLPDecoder decoder = getDecoder(mode);
			
			decoder.decode(eConfig, filenames);
		}
		catch (Exception e) {e.printStackTrace();}
	}
	
	public NLPDecoder getDecoder(String mode)
	{
		switch (mode)
		{
		case NLPLib.MODE_POS  : return new POSDecoder();
		case NLPLib.MODE_MORPH: return new MorphDecoder();
		case NLPLib.MODE_DEP  : return new DEPDecoder();
		case NLPLib.MODE_SRL  : return new SRLDecoder();
		}
		
		throw new IllegalArgumentException("The requested mode '"+mode+"' is not supported.");
	}
	
	/** String[0]: input filename, String[1]: output filename. */
	private List<String[]> getFilenames(String inputPath, String inputExt, String outputExt)
	{
		List<String[]> filenames = new ArrayList<String[]>();
		File f = new File(inputPath);
		String[] inputFiles;
		String outputFile;
		
		if (f.isDirectory())
		{
			inputFiles = f.list(new FileExtFilter(inputExt));
			Arrays.sort(inputFiles);
			
			for (String inputFile : inputFiles)
			{
				inputFile  = inputPath + File.separator + inputFile;
				outputFile = inputFile + "." + outputExt;
				filenames.add(new String[]{inputFile, outputFile});
			}
		}
		else
			filenames.add(new String[]{inputPath, inputPath+"."+outputExt});
		
		return filenames;
	}
	
	static public void main(String[] args)
	{
		new NLPDecode(args);
	}
}
