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
package com.clearnlp.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.clearnlp.io.FileExtFilter;
import com.clearnlp.util.pair.ObjectLongPair;
import com.google.common.collect.Lists;


/**
 * File utilities.
 * @since 1.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class UTFile
{
	/**
	 * Replaces the extension of a filename with the specific extension.
	 * If the filename does not have an extension, appends the specific extension. 
	 * @param filename the filename.
	 * @param newExt the new extension.
	 * @return the filename with the new extension.
	 */
	static public String replaceExtension(String filename, String newExt)
	{
		int idx = filename.lastIndexOf(".");
		if (idx == -1)	return filename+"."+newExt;
		
		return filename.substring(0, idx+1) + newExt;
	}
	
	/**
	 * Replaces the old extension to the new extension.
	 * If the filename does not end with the old extension, returns the original filename.
	 * @param filename the filename.
	 * @param oldExt the old extension.
	 * @param newExt the new extension.
	 * @return the filename with the new extension.
	 */
	static public String replaceExtension(String filename, String oldExt, String newExt)
	{
		if (filename.endsWith(oldExt))
			return filename.substring(0, filename.length()-oldExt.length()) + newExt;
		else
			return filename;
	}
	
	/**
	 * Returns the basename of the specific filepath (e.g., wsj/00/wsj_0001.mrg -> wsj_0001.mrg). 
	 * @param filepath the filepath to get the basename from.
	 * @return the basename of the specific filepath (e.g., wsj/00/wsj_0001.mrg -> wsj_0001.mrg).
	 */
	static public String getBasename(String filepath)
	{
		int idx = filepath.lastIndexOf(File.separator);
		return filepath.substring(idx+1);
	}
	
	/**
	 * Returns the sorted list of all filenames in the specific directory.
	 * @see UTFile#getSortedFileList(String, String)
	 * @param fileDir the directory to retrieve filenames from.
	 * @return the sorted list of all filenames in the specific directory.
	 */
	static public String[] getSortedFileList(String fileDir)
	{
		return getSortedFileList(fileDir, ".*");
	}
	
	/**
	 * Returns the sorted list of filenames with the specific extension in the specific directory.
	 * @param fileDir the directory to retrieve filenames from.
	 * @param fileExt the extension of filenames.
	 * @return the sorted list of filenames with the specific extension in the specific directory.
	 */
	static public String[] getSortedFileList(String fileDir, String fileExt)
	{
		List<String> list = new ArrayList<String>();
		
		for (String filepath : new File(fileDir).list(new FileExtFilter(fileExt)))
		{
			filepath = fileDir + File.separator + filepath;
			
			if (new File(filepath).isFile())
				list.add(filepath);
		}
		
		String[] filelist = new String[list.size()];
		list.toArray(filelist);
		Arrays.sort(filelist);
		
		return filelist;
	}
	
	static public String[] getSortedFileListBySize(String fileDir, String fileExt, boolean reverse)
	{
		List<ObjectLongPair<String>> list = Lists.newArrayList();
		File file;
		
		for (String filepath : new File(fileDir).list(new FileExtFilter(fileExt)))
		{
			filepath = fileDir + File.separator + filepath;
			file = new File(filepath);
			
			if (file.isFile())
				list.add(new ObjectLongPair<String>(filepath, file.length()));
		}
		
		int i, size = list.size();
		if (reverse)	UTCollection.sortReverseOrder(list);
		else			Collections.sort(list);
		
		String[] filelist = new String[size];
		
		for (i=0; i<size; i++)
			filelist[i] = list.get(i).o;
		
		return filelist;
	}
	
	/**
	 * If the specific input path is a directory, returns the sorted list of filenames with the specific extension.
	 * If the input path is a file, returns a list containing only the input path.
	 * @see UTFile#getSortedFileList(String, String) 
	 * @param inputPath the input path.
	 * @param inputExt the input extension.
	 * @return the sorted list of filenames with the specific extension.
	 */
	static public String[] getInputFileList(String inputPath, String inputExt)
	{
		File input = new File(inputPath);
		
		if (input.isDirectory())
			return getSortedFileList(inputPath, inputExt);
		else
			return new String[]{inputPath};
	}
}
