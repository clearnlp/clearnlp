/**
* Copyright 2013 IPSoft Inc.
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
package com.clearnlp.classification.feature;

import static org.junit.Assert.assertEquals;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Test;

import com.clearnlp.classification.feature.AbstractFtrXml;
import com.clearnlp.classification.feature.JointFtrXml;

/** @author Jinho D. Choi ({@code jdchoi77@gmail.com}) */
public class AbstractFtrXmlTest
{
	@Test
	public void testJointFtrXml() throws Exception
	{
		AbstractFtrXml xml = new JointFtrXml(new BufferedInputStream(new FileInputStream("src/main/resources/feature/feature_en_pos.xml")));
		String objFile = "src/test/resources/generation/tmp.txt";
		String s = xml.toString();
		
		ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(objFile))); 
		out.writeObject(xml);
		out.close();
		
		ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(objFile)));
		xml = (AbstractFtrXml)in.readObject();
		in.close();
		
		assertEquals(s, xml.toString());
	}
}
