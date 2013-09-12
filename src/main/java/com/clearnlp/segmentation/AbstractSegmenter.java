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
import java.util.List;

import com.clearnlp.tokenization.AbstractTokenizer;

/**
 * @since 1.1.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
abstract public class AbstractSegmenter
{
	protected AbstractTokenizer g_tokenizer;
	
	public AbstractSegmenter(AbstractTokenizer tokenizer)
	{
		g_tokenizer = tokenizer;
	}
	
	/**
	 * Returns a list of sentences, which are arrays of string tokens, from the specific reader.
	 * @param fin the reader to retrieve sentences from.
	 * @return a list of sentences, which are arrays of string tokens, from the specific reader.
	 */
	abstract public List<List<String>> getSentences(BufferedReader fin);
}
