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
package com.clearnlp.generation;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.clearnlp.generation.LGVerbEn;


/** @author Jinho D. Choi ({@code jdchoi77@gmail.com}) */
public class LGVerbEnTest
{
	@Test
	public void testGet3rdSingularForm()
	{
		String s = "work";
		assertEquals(LGVerbEn.get3rdSingularForm(s), "works");
		
		s = "hope";
		assertEquals(LGVerbEn.get3rdSingularForm(s), "hopes");
		
		s = "teach";
		assertEquals(LGVerbEn.get3rdSingularForm(s), "teaches");
		
		s = "wish";
		assertEquals(LGVerbEn.get3rdSingularForm(s), "wishes");
		
		s = "miss";
		assertEquals(LGVerbEn.get3rdSingularForm(s), "misses");
		
		s = "buzz";
		assertEquals(LGVerbEn.get3rdSingularForm(s), "buzzes");
		
		s = "fix";
		assertEquals(LGVerbEn.get3rdSingularForm(s), "fixes");
		
		s = "go";
		assertEquals(LGVerbEn.get3rdSingularForm(s), "goes");
		
		s = "stay";
		assertEquals(LGVerbEn.get3rdSingularForm(s), "stays");
		
		s = "enjoy";
		assertEquals(LGVerbEn.get3rdSingularForm(s), "enjoys");
		
		s = "fly";
		assertEquals(LGVerbEn.get3rdSingularForm(s), "flies");
	}
	
	@Test
	public void testGetPastRegularForm()
	{
		String s = "work";
		assertEquals(LGVerbEn.getPastRegularForm(s), "worked");
		
		s = "hope";
		assertEquals(LGVerbEn.getPastRegularForm(s), "hoped");
		
		s = "stay";
		assertEquals(LGVerbEn.getPastRegularForm(s), "stayed");
		
		s = "enjoy";
		assertEquals(LGVerbEn.getPastRegularForm(s), "enjoyed");
		
		s = "fly";
		assertEquals(LGVerbEn.getPastRegularForm(s), "flied");
		
		s = "ski";
		assertEquals(LGVerbEn.getPastRegularForm(s), "skied");
	}
	
	@Test
	public void testGetPastForm() throws Exception
	{
		LGVerbEn verb = new LGVerbEn();
		
		assertEquals("foreshowed", verb.getPastForm("foreshow"));
		assertEquals("foreshown", verb.getPastParticipleForm("foreshow"));
		
		assertEquals("hoped", verb.getPastForm("hope"));
		assertEquals("hoped", verb.getPastParticipleForm("hope"));
		
		assertEquals("went", verb.getPastForm("go"));
		assertEquals("gone", verb.getPastParticipleForm("go"));
	}
}
