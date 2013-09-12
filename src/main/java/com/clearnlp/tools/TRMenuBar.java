/**
* Copyright 2013 University of Massachusetts Amherst
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
package com.clearnlp.tools;

import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

/**
 * @since 1.3.1
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
@SuppressWarnings("serial")
public class TRMenuBar extends JMenuBar
{
	protected ActionListener a_listener;
	
	JMenuItem fileOpen, fileSave, fileSaveAs, fileQuit;
	JMenuItem nvPrev, nvNext, nvJump;
	
	public TRMenuBar(ActionListener listener)
	{
		a_listener = listener;
		
		initMenuFile();
		initMenuNavigate();
	}
	
	private void initMenuFile()
	{
		JMenu mFile = new JMenu("File");
		mFile.setMnemonic(KeyEvent.VK_F);
		
		fileOpen = getJMenuItem("Open", KeyEvent.VK_O, KeyEvent.VK_O, KeyEvent.CTRL_MASK);
		mFile.add(fileOpen);
		mFile.addSeparator();
		
		fileSave = getJMenuItem("Save", KeyEvent.VK_S, KeyEvent.VK_S, KeyEvent.CTRL_MASK);
		mFile.add(fileSave);
		fileSaveAs = getJMenuItem("Save As", KeyEvent.VK_A);
		mFile.add(fileSaveAs);
		mFile.addSeparator();
	
		fileQuit = getJMenuItem("Quit", KeyEvent.VK_Q, KeyEvent.VK_Q, KeyEvent.CTRL_MASK);
		mFile.add(fileQuit);
		
		add(mFile);
	}
	
	private void initMenuNavigate()
	{
		JMenu mNaviagate = new JMenu("Nagivate");
		mNaviagate.setMnemonic(KeyEvent.VK_N);
		
		nvPrev = getJMenuItem("Previous Tree", KeyEvent.VK_P, KeyEvent.VK_COMMA, 0);
		mNaviagate.add(nvPrev);
		nvNext = getJMenuItem("Next Tree", KeyEvent.VK_N, KeyEvent.VK_PERIOD, 0);
		mNaviagate.add(nvNext);
		nvJump = getJMenuItem("Jump To", KeyEvent.VK_J, KeyEvent.VK_J, KeyEvent.CTRL_MASK);
		mNaviagate.add(nvJump);
		
		add(mNaviagate);
	}
	
	// set menu-item with name, short-key
	protected JMenuItem getJMenuItem(String text, int mnemonic)
	{
		JMenuItem mi = new JMenuItem(text, mnemonic);		
		mi.addActionListener(a_listener);
		
		return mi;
    }
	
	// set menu-item with name, short-key, accelerator
	protected JMenuItem getJMenuItem(String text, int mnemonic, int keyCode, int modifiers)
	{
		JMenuItem mi = new JMenuItem(text, mnemonic);
		
		mi.setAccelerator(KeyStroke.getKeyStroke(keyCode, modifiers));
		mi.addActionListener(a_listener);
		
		return mi;
    }
}