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
package com.clearnlp.tools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import com.clearnlp.dependency.DEPTree;
import com.clearnlp.reader.JointReader;
import com.clearnlp.util.UTInput;
import com.clearnlp.util.UTOutput;
import com.clearnlp.util.pair.StringIntPair;

public class Trinity extends JFrame
{
	private static final long serialVersionUID = -5371383601723946523L;

	private TRMenuBar		mn_bar;
	@SuppressWarnings("rawtypes")
	private JComboBox		cb_trees;
	private TRTreePane		pn_trees;
	private TRTablePane		pn_table;
	
	private List<DEPTree>	l_trees;
	private int				i_currTree;
	private boolean			b_update;
	
	private String 			s_outputFile;
	
	public Trinity(StringIntPair[] tags)
	{
		mn_bar = new TRMenuBar(new TRMenuListener());
		setJMenuBar(mn_bar);
		
		setLayout(new BorderLayout());
		
		cb_trees = getCBTrees();
		add(cb_trees, BorderLayout.NORTH);
		
		pn_trees = new TRTreePane();
		add(new JScrollPane(pn_trees), BorderLayout.CENTER);
		
		pn_table = new TRTablePane(new JTableModelListener(), tags);
		add(new JScrollPane(pn_table), BorderLayout.SOUTH);
		
		setVisible(true);
		setBounds(0, 0, 800, 500);
		setBackground(Color.WHITE);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public void open(String inputFile, String outputFile)
	{
		l_trees = getTrees(inputFile);
		resetCBTrees(l_trees);
		s_outputFile = outputFile;
	}
	
	private List<DEPTree> getTrees(String inputFile)
	{
		JointReader reader = new JointReader(0, 1, 2, 3, 4, 5, 6);
		reader.open(UTInput.createBufferedFileReader(inputFile));
		List<DEPTree> trees = new ArrayList<DEPTree>();
		DEPTree tree;
	
		while ((tree = reader.next()) != null)
			trees.add(tree);
		
		reader.close();
		return trees;
	}
	
	@SuppressWarnings("rawtypes")
	private JComboBox getCBTrees()
	{
		JComboBox cb = new JComboBox();
		cb.addActionListener(new TRComboBoxListener());
		
		return cb;
	}
	
	@SuppressWarnings("unchecked")
	private void resetCBTrees(List<DEPTree> trees)
	{ 
		int i, size = trees.size();
		cb_trees.removeAllItems();
		
		for (i=0; i<size; i++)
			cb_trees.addItem(Integer.toString(i)+" - "+trees.get(i).toStringRaw());
		
		cb_trees.revalidate();
	}
	
	protected void selectTree(int index)
	{
		DEPTree tree = l_trees.get(index);
		i_currTree = index;
		
		b_update = false;
		pn_trees.init(tree);
		pn_table.init(tree);
		b_update = true;
	}
	
	public void saveTrees(String outputFile)
	{
		PrintStream fout = UTOutput.createPrintBufferedFileStream(outputFile);
		
		for (DEPTree tree : l_trees)
			fout.println(tree.toStringDEP()+"\n");
		
		fout.close();
	}
	
	private class TRMenuListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			if (e.getSource() == mn_bar.nvPrev)
				menuPrev();
			else if (e.getSource() == mn_bar.nvNext)
				menuNext();
			else if (e.getSource() == mn_bar.fileSave)
				menuSave();
		}
	}
	
	private void menuSave()
	{
		PrintStream fout = UTOutput.createPrintBufferedFileStream(s_outputFile);
		
		for (DEPTree tree : l_trees)
			fout.println(tree.toStringDEP()+"\n");
		
		fout.close();
	}
	
	private void menuPrev()
	{
		if (i_currTree > 0)
			i_currTree--;
		
		cb_trees.setSelectedIndex(i_currTree);
	}
	
	private void menuNext()
	{
		if (i_currTree < l_trees.size() - 1)
			i_currTree++;
		
		cb_trees.setSelectedIndex(i_currTree);
	}
	
	private class TRComboBoxListener implements ActionListener
	{
		@SuppressWarnings("rawtypes")
		@Override
		public void actionPerformed(ActionEvent e)
		{
			JComboBox cb = (JComboBox)e.getSource();
			selectTree(cb.getSelectedIndex());
		}
	}
	
	private class JTableModelListener implements TableModelListener
	{
		@Override
		public void tableChanged(TableModelEvent e)
		{
			if (b_update)
			{
				int column = e.getColumn(), row = e.getLastRow();
				pn_table.updateTree(row, column);
				pn_trees.updateTree();
				
			}
		}
	}
	
	static public void main(String[] args)
	{
		String posFile    = args[0];
		String depFile    = args[1];
		String inputFile  = args[2];
		String outputFile = args[3];
		
		StringIntPair[] ps = {new StringIntPair(posFile, 2), new StringIntPair(depFile, 4)};
		Trinity tr = new Trinity(ps);
		tr.open(inputFile, outputFile);
	}
}
