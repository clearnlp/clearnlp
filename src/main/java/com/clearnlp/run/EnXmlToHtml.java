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

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class EnXmlToHtml
{
	private final String TAB = "&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp ";
	private final String VN_URL = "http://verbs.colorado.edu/verb-index/vn/";
	private final String VN_MAP_FILE = "vn-map.txt";
	private DocumentBuilder g_builder;
	private Hashtable<String,String> h_vnmap;
	
	public EnXmlToHtml(String inXml, String outHtml)
	{
		DocumentBuilderFactory dFactory = DocumentBuilderFactory.newInstance();
		
		try
		{
			g_builder = dFactory.newDocumentBuilder();
			initMap();
		}
		catch (Exception e){e.printStackTrace();}
		
		convert(inXml, outHtml);
	}

	@SuppressWarnings("resource")
	private void initMap() throws Exception
	{
		Scanner scan = new Scanner(new File(VN_MAP_FILE));
		h_vnmap = new Hashtable<String,String>();
		
		while (scan.hasNextLine())
		{
			String[] map = scan.nextLine().split(" ");
			h_vnmap.put(map[0], map[1]);
		}
	}
	
	public void convert(String inXml, String outHtml)
	{
		try
		{
			Document    doc  = g_builder.parse(new File(inXml));
			PrintStream fout = new PrintStream(new FileOutputStream(outHtml));
			prePrint(outHtml, fout);
			
			NodeList list = doc.getElementsByTagName("predicate");
			
			for (int i=0; i<list.getLength(); i++)
				convertPredicate((Element)list.item(i), fout);

			postPrint(fout);
		}
		catch (Exception e){System.err.println(e);}
	}
	
	private void prePrint(String outHtml, PrintStream fout)
	{
		int beginIdx = (outHtml.contains("/")) ? outHtml.lastIndexOf('/')+1 : 0;
		String  pred = outHtml.substring(beginIdx, outHtml.lastIndexOf('.'));

		fout.println("<html>");
		fout.println("<head><title>Frameset - "+pred+"</title></head>");
		fout.println();
		fout.println("<body>");
	}
	
	private void postPrint(PrintStream fout)
	{
		fout.println("</body>");
		fout.println("</html>");
	}
	
	private void convertPredicate(Element ePredicate, PrintStream fout)
	{
		String lemma = ePredicate.getAttribute("lemma");
		fout.println("<center><h2>Predicate: <font color=red><i>"+lemma+"</i></font></h2></center>");
		convertNote(lemma, ePredicate, fout);
		
		NodeList list = ePredicate.getChildNodes();
		for (int i=0; i<list.getLength(); i++)
		{
			Node node = list.item(i);
			if (node.getNodeName().equals("roleset"))
				convertRoleset((Element)node, fout);
		}
		
		fout.println("<br>");
	}
	
	private void convertRoleset(Element eRoleset, PrintStream fout)
	{
		String id      = eRoleset.getAttribute("id");
		String name    = eRoleset.getAttribute("name");
		String[] vncls = eRoleset.getAttribute("vncls").split(" ");
		String framnet = eRoleset.getAttribute("framnet");
		String vn = "";
		for (int i=0; i<vncls.length; i++)
		{
			if (!vncls[i].equals("-"))
				vn += " <a href=\""+VN_URL+h_vnmap.get(vncls[i])+".php\">"+vncls[i]+"</a>";
		}   vn = vn.trim();

		fout.print("<h4>Roleset id: <font color=green>"+id+"</font> , <i>"+name+"</i>"+", vncls: "+vn+", framnet: "+framnet+"</h4>");
		convertNote(id, eRoleset, fout);
		
		NodeList list = eRoleset.getChildNodes();
		for (int i=0; i<list.getLength(); i++)
		{
			Node node = list.item(i);
			if (node.getNodeName().equals("roles"))
				convertRoles((Element)node, fout);
			else if (node.getNodeName().equals("example"))
				convertExample((Element)node, fout);
		}
		
		fout.println("<br>");
	}
	
	private void convertRoles(Element eRoles, PrintStream fout)
	{
		fout.println("<h4>Roles:</h4>");
		convertNote(null, eRoles, fout);
				
		NodeList list = eRoles.getElementsByTagName("role");
		for (int i=0; i<list.getLength(); i++)
			convertRole((Element)list.item(i), fout);
	}
	
	private void convertRole(Element eRole, PrintStream fout)
	{
		String n      = eRole.getAttribute("n");
		String descr  = eRole.getAttribute("descr");
		String vnrole = "";
		
		NodeList list = eRole.getElementsByTagName("vnrole");
		for (int i=0; i<list.getLength(); i++)
		{
			Element eVnrole = (Element)list.item(i);
			vnrole += ", "+getVnrole(eVnrole);
		}
		if (vnrole.length() > 0)	vnrole = " (vnrole:"+vnrole.substring(1)+")";
		
		fout.println(TAB+"<b>Arg"+n+"</b>: <i>"+descr+"</i> "+vnrole);
		fout.println("<br>");
	}
	
	private String getVnrole(Element eVnrole)
	{
		String vncls   = eVnrole.getAttribute("vncls").trim();
		String vntheta = eVnrole.getAttribute("vntheta").trim();
		
		return vncls+"-"+vntheta;
	}
	
	private void convertExample(Element eExample, PrintStream fout)
	{
		String name = eExample.getAttribute("name");
		fout.println("<h4>Example: "+name+"</h4>");
		convertNote(null, eExample, fout);
		
		NodeList list = eExample.getChildNodes();
		for (int i=0; i<list.getLength(); i++)
		{
			Node node = list.item(i);
			String attr = node.getNodeName();
			if (attr.equals("inflection"))
				convertInflection((Element)node, fout);
			else if (attr.equals("text"))
				convertText((Element)node, fout);
			else if (attr.equals("arg"))
				convertArg((Element)node, fout);
			else if (attr.equals("rel"))
				convertRel((Element)node, fout);
		}
	}
	
	private void convertInflection(Element eInflection, PrintStream fout)
	{
		String person = eInflection.getAttribute("person");
		String tense  = eInflection.getAttribute("tense");
		String aspect = eInflection.getAttribute("aspect");
		String voice  = eInflection.getAttribute("voice");
		String form   = eInflection.getAttribute("form");
		
		String str = TAB+"person: <i>"+person+"</i>,&nbsp tense: <i>"+tense+"</i>,&nbsp aspect: <i>"+aspect+
		             "</i>,&nbsp voice: <i>"+voice+"</i>,&nbsp form: <i>"+form+"</i>";
		fout.println(str+"<br><br>");
	}
	
	private void convertText(Element eText, PrintStream fout)
	{
		StringTokenizer tok = new StringTokenizer(eText.getTextContent(), "\n\r");

		while (tok.hasMoreTokens())
		{
			String str = tok.nextToken().trim();
			if (!str.equals(""))	fout.println(TAB+"<font size=2>"+str+"</font><br>");
		}
		
		fout.println("<br>");
	}
	
	private void convertArg(Element eArg, PrintStream fout)
	{
		String arg = eArg.getTextContent();
		String n   = eArg.getAttribute("n").trim();
		String f   = eArg.getAttribute("f").trim();
		n += (!f.equals("")) ? "-"+f : ""; 
		fout.println(TAB+"<b>Arg"+n+"</b>: "+arg+"<br>");
	}
	
	private void convertRel(Element eRel, PrintStream fout)
	{
		String rel = eRel.getTextContent().trim();
		fout.println(TAB+"<b>Rel</b>: "+rel+"<br>");
	}
	
	// tag = "Predicate", "Roleset", "Example"
	private void convertNote(String tag, Element element, PrintStream fout)
	{
		String note = "";
		NodeList list = element.getChildNodes();
		
		for (int i=0; i<list.getLength(); i++)
		{
			Node node = list.item(i);
			if (!node.getNodeName().equals("note"))	continue;
			
			note += " "+node.getTextContent().trim();
		}	note = note.trim();
		
		if (!note.equals(""))
		{
			if (tag != null)	fout.println("<font size=2 color=blue><b>"+tag+"</b>: "+note+"</font><br>");
			else				fout.println(TAB+"<font size=2 color=blue>"+note+"</font><br>");
		}
		
	/*	String note = "";
		NodeList list = element.getElementsByTagName("note");
		
		for (int i=0; i<list.getLength(); i++)
		{
			Node node = list.item(i);
			note += " "+node.getTextContent().trim();
		}	note = note.trim();
		
		if (!note.equals(""))
		{
			if (tag != null)	fout.println("<font size=2 color=blue><b>"+tag+"</b>: "+note+"</font><br>");
			else				fout.println(TAB+"<font size=2 color=blue>"+note+"</font><br>");
		}*/
	}

	static public void main(String[] args)
	{
		new EnXmlToHtml(args[0], args[1]);
	}
}
