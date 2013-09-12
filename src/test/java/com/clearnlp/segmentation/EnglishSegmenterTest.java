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
package com.clearnlp.segmentation;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.StringReader;
import java.util.List;

import org.junit.Test;

import com.clearnlp.segmentation.EnglishSegmenter;
import com.clearnlp.tokenization.EnglishTokenizer;
import com.clearnlp.util.UTArray;

/**
 * @since 1.1.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class EnglishSegmenterTest
{
	@Test
	public void testEnglishSegmenter() throws FileNotFoundException
	{
		String src = "He said, \"I'd like to know Mr. Choi.\" He's the owner of ClearNLP";
		EnglishSegmenter tok = new EnglishSegmenter(new EnglishTokenizer());
		BufferedReader reader = new BufferedReader(new StringReader(src));
		
		for (List<String> sentence : tok.getSentences(reader))
			System.out.println(UTArray.join(sentence, " "));
	}
}
