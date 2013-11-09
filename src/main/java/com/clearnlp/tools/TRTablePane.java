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
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dependency.DEPTree;
import com.clearnlp.util.UTInput;
import com.clearnlp.util.pair.StringIntPair;

public class TRTablePane extends JPanel
{
	private static final long serialVersionUID = 3390510006593843456L;

	private final String[] t_columns = {"ID", "Form", "POS", "Head ID", "Deprel"};
	private JTable j_table;
	private DEPTree d_tree;
	
	private int t_height;
	
	public TRTablePane(TableModelListener listener, StringIntPair[] tags)
	{
		setLayout(new BorderLayout());
		
		j_table = new JTable();
		j_table.setModel(new DefaultTableModel(new Object[][]{{0,"","",0,""}}, t_columns));
		j_table.getModel().addTableModelListener(listener);
		
		TableColumnModel columnModel = j_table.getColumnModel();
		
		for (StringIntPair tag : tags)
			setColumnEditor(columnModel, tag.s, tag.i);
		
		add(j_table.getTableHeader(), BorderLayout.NORTH);
		add(j_table, BorderLayout.CENTER);
		
		t_height = j_table.getFontMetrics(j_table.getFont()).getHeight();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void setColumnEditor(TableColumnModel model, String tagFile, int columnIndex)
	{
		BufferedReader fin = UTInput.createBufferedFileReader(tagFile);
		List<String> list = new ArrayList<String>();
		String line;
		
		try
		{
			while ((line = fin.readLine()) != null)
				list.add(line.trim());
		
			Collections.sort(list);
			Object[] tags = list.toArray();
			
			model.getColumn(columnIndex).setCellEditor(new DefaultCellEditor(new JComboBox(tags)));
			fin.close();			
		}
		catch (IOException e) {e.printStackTrace();}
	}
	
	public void init(DEPTree tree)
	{
		d_tree = tree;
		removeAll();
		add(tree);
		
		int rowCount = j_table.getModel().getRowCount();
		if (rowCount < 20)	rowCount = 20;
		
		j_table.setPreferredSize(new Dimension(50, (rowCount+3) * t_height));
		revalidate();
	}

	public void removeAll()
	{
		DefaultTableModel model = (DefaultTableModel)j_table.getModel();
		int i, size = model.getRowCount();
		
		for (i=size-1; i>=0; i--)
			model.removeRow(i);
	}
	
	public void add(DEPTree tree)
	{
		int i, size = tree.size();
		
		for (i=1; i<size; i++)
			add(tree.get(i));
	}
	
	public void add(DEPNode node)
	{
		Object[] data = new Object[]{node.id, node.form, node.pos, node.getHead().id, node.getLabel()};
		DefaultTableModel model = (DefaultTableModel)j_table.getModel();
		model.addRow(data);
	}
	
	public void updateTree(int row, int column)
	{
		DEPNode node = d_tree.get(row+1);
		Object  value = j_table.getValueAt(row, column);
		
		if (column == 2)
		{
			node.pos = (String)value;
		}
		else if (column == 3)
		{
			int headId = Integer.parseInt((String)value);
			DEPNode head = d_tree.get(headId);
			if (head != null)	node.setHead(head);
		}
		else if (column == 4)
		{
			node.setLabel((String)value);
		}
	}
}
