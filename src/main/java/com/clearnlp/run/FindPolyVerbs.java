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
package com.clearnlp.run;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.clearnlp.propbank.frameset.MultiFrames;
import com.clearnlp.propbank.frameset.PBFrameset;
import com.clearnlp.propbank.frameset.PBRoleset;
import com.clearnlp.propbank.frameset.PBType;

/**
 * @since 2.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class FindPolyVerbs
{
	public FindPolyVerbs(String framesDir) throws IOException
	{
		MultiFrames frames = new MultiFrames(framesDir);
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		Collection<PBFrameset> fs = frames.getFramesetMap(PBType.VERB).values();
		List<PBRoleset> rolesets;
		String vncls;

		while ((vncls = in.readLine()) != null)
		{
			for (PBFrameset frameset : fs)
			{
				rolesets = frameset.getRolesetsFromVerbNet(vncls, true);
				Collections.sort(rolesets);
				
				for (PBRoleset roleset : rolesets)
					System.out.println(roleset.getID());
			}
		}
	}
	
	static public void main(String[] args)
	{
		try
		{
			new FindPolyVerbs(args[0]);
		}
		catch (IOException e) {e.printStackTrace();}
	}
}
