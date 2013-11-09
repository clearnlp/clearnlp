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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.carrotsearch.hppc.IntArrayList;
import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dependency.DEPTree;
import com.clearnlp.util.list.SortedIntArrayList;

public class TRTreePane extends JPanel
{
	private static final long serialVersionUID = 8954140478832546803L;
	
	private final int ARROW_W = 4;
	private final int ARROW_H = 8;
	
	private final int GAP_EDGE_W = 8;
	private final int GAP_EDGE_H = 20;
	private final int GAP_FORM   = 25;
	
	private final Font        FORM_FONT   = new Font(Font.MONOSPACED, Font.PLAIN, 14);
	private final FontMetrics FORM_FM     = new JLabel().getFontMetrics(FORM_FONT);
	private final int         FORM_HEIGHT = FORM_FM.getHeight();
	
	private final Font        POS_FONT   = new Font(Font.MONOSPACED, Font.PLAIN, 14);
	private final FontMetrics POS_FM     = new JLabel().getFontMetrics(POS_FONT);
	private final int         POS_HEIGHT = POS_FM.getHeight();
		
	private final Font        DEPREL_FONT   = new Font(Font.MONOSPACED, Font.PLAIN, 12);
	private final FontMetrics DEPREL_FM     = new JLabel().getFontMetrics(DEPREL_FONT);
	private final int         DEPREL_HEIGHT = DEPREL_FM.getHeight();
	
	private DEPTree d_tree;
	
	private Rectangle[] r_forms;
	private int[][]     r_edges;
	
	public TRTreePane() {}
	
	public void init(DEPTree tree)
	{
		d_tree = tree;
		updateTree();
	}
	
	public void updateTree()
	{
		IntArrayList[] groups = getGeometriesGroups();
		r_forms = getGeometriesForms(groups);
		r_edges = getGeometriesEdges(groups);
		
		Rectangle rect = r_forms[r_forms.length-1];
		setPreferredSize(new Dimension((int)rect.getMaxX()+GAP_FORM, (int)rect.getMaxY()+FORM_HEIGHT+POS_HEIGHT+ARROW_H));
	
		repaint();
		revalidate();
	}
	
	/** Called by {@link TRTreePane#updateTree()}. */
	private IntArrayList[] getGeometriesGroups()
	{
		int i, size = d_tree.size(); 
		DEPNode curr, head;
		
		SortedIntArrayList[] lhs = new SortedIntArrayList[size];
		SortedIntArrayList[] rhs = new SortedIntArrayList[size];
		
		for (i=0; i<size; i++)
		{
			lhs[i] = new SortedIntArrayList(false);
			rhs[i] = new SortedIntArrayList(false);
		}
		
		for (i=1; i<size; i++)
		{
			curr = d_tree.get(i);
			head = curr.getHead();
			
			if (head != null)
			{
				if (curr.id < head.id)
				{
					lhs[head.id].add(curr.id);
					rhs[curr.id].add(head.id);
				}
				else
				{
					lhs[curr.id].add(head.id);
					rhs[head.id].add(curr.id);
				}
			}
		}
		
		IntArrayList[] groups = new IntArrayList[size];
		
		for (i=0; i<size; i++)
		{
			groups[i] = new IntArrayList();
			groups[i].addAll(lhs[i]);
			groups[i].addAll(rhs[i]);
		}
		
		return groups;
	}
	
	/** Called by {@link TRTreePane#updateTree()}. */
	private Rectangle[] getGeometriesForms(IntArrayList[] groups)
	{
		int i, j, w, m, x, pm = 0, size = d_tree.size();
		Rectangle[] rForms = new Rectangle[size];
		DEPNode node, head;
		
		for (i=0; i<size; i++)
		{
			w = FORM_FM.stringWidth(d_tree.get(i).form);
			m = (groups[i].size() - 1) * GAP_EDGE_W - w;
			m = (m > 0) ? Math.round((float)m/2) : 0;
			x = (i > 0) ? (int)rForms[i-1].getMaxX() + GAP_FORM : GAP_FORM;
			
			rForms[i] = new Rectangle(x+m+pm, 0, w, FORM_HEIGHT);
			pm = m;
		}
		
		for (i=1; i<size; i++)
		{
			node = d_tree.get(i);
			head = node.getHead();
			
			if (head != null)
			{
				w = DEPREL_FM.stringWidth(node.getLabel()) + 20;
				m = (int)Math.abs(rForms[node.id].x - rForms[head.id].x);
				
				if (w > m)
				{
					j = (node.id > head.id) ? node.id : head.id;
					w -= m;
					
					for (; j<size; j++)
						rForms[j].x += w;
				}
			}
		}

		return rForms;
	}
	
	/** Called by {@link TRTreePane#updateTree()}. */
	private int[][] getGeometriesEdges(IntArrayList[] groups)
	{
		int i, xh, xd, size = d_tree.size();
		int[] heights = getHeights(d_tree);
		int[][] rEdges = new int[size][];
		DEPNode curr, head;
		
		for (i=1; i<size; i++)
		{
			curr = d_tree.get(i);
			head = curr.getHead();
			
			if (head != null)
			{
				xd = getGeometriesEdgesAux(groups, curr.id, head.id);
				xh = getGeometriesEdgesAux(groups, head.id, curr.id);
				rEdges[i] = new int[]{xd, xh, heights[i] * GAP_EDGE_H};
			}
		}
		
		return rEdges;
	}
	
	private int[] getHeights(DEPTree tree)
	{
		int i, size = tree.size();
		int[] heights = new int[size];
		
		for (i=1; i<size; i++)
			getHeightsRec(tree, i, heights);

		return heights;
	}
	
	private void getHeightsRec(DEPTree tree, int id, int[] heights)
	{
		DEPNode curr = tree.get(id);
		DEPNode head = curr.getHead();
		int i, st, et, max = 0;
		DEPNode node;
		
		if (head == null) return;
		
		if (curr.id < head.id)
		{
			st = curr.id;
			et = head.id;
		}
		else
		{
			st = head.id;
			et = curr.id;
		}
		
		for (i=st; i<=et; i++)
		{
			if (i == id)	continue;
			node = tree.get(i).getHead();
			
			if (node != null && st <= node.id && node.id <= et)
			{
				if (heights[i] == 0)
					getHeightsRec(tree, i, heights);
					
				max = Math.max(max, heights[i]);
			}
		}		
	
		heights[id] = max + 1;
	}
	
	/** Called by {@link TRTreePane#getGeometriesEdges(IntArrayList[])}. */
	private int getGeometriesEdgesAux(IntArrayList[] groups, int id1, int id2)
	{
		return (int)(r_forms[id1].getCenterX() - ((double)(groups[id1].size() - 1) / 2 - groups[id1].indexOf(id2)) * GAP_EDGE_W);
	}
	
	@Override
    public void paint(Graphics g)
	{
		if (r_edges == null)	return;
		super.paint(g);
		
		int i, x, y, size = r_edges.length;
		String deprel;
		DEPNode node;
		int[] edge;
		String s;
		
		int maxY = 0;
		
		for (i=1; i<size; i++)
			maxY = Math.max(maxY, r_edges[i][2]);
		
		maxY += GAP_EDGE_H * 2;
		
		for (i=0; i<size; i++)
		{
			node = d_tree.get(i);
			
			g.setColor(Color.BLACK);
			g.setFont(FORM_FONT);
			y = maxY + FORM_HEIGHT;
			g.drawString(node.form, r_forms[i].x, y);
			
			g.setColor(Color.MAGENTA);
			g.setFont(POS_FONT);
			x = (int)Math.round(r_forms[i].getCenterX() - (double)POS_FM.stringWidth(node.pos)/2);
			y += POS_HEIGHT + 2;
			g.drawString(d_tree.get(i).pos, x, y);
			
			g.setColor(Color.DARK_GRAY);
			g.setFont(DEPREL_FONT);
			s = Integer.toString(i);
			x = (int)Math.round(r_forms[i].getCenterX() - (double)DEPREL_FM.stringWidth(s)/2);
			y += DEPREL_HEIGHT + 2;
			g.drawString(s, x, y);
			
			edge = r_edges[i];
			
			if (edge != null)
			{
				deprel = node.getLabel();
				
				if (edge[0] < edge[1])
					x = edge[0] + Math.round((float)(edge[1] - edge[0]) / 2);
				else
					x = edge[1] + Math.round((float)(edge[0] - edge[1]) / 2);
				
				x -= Math.round((float)DEPREL_FM.stringWidth(deprel) / 2);
				y  = maxY - edge[2];
				
				g.setColor(Color.BLUE);
				g.setFont(DEPREL_FONT);
				g.drawString(deprel, x, y-DEPREL_HEIGHT/2);
				
				g.setColor(Color.DARK_GRAY);
				drawArrow (g, edge[0], y, edge[0], maxY);
				g.drawLine(   edge[1], y, edge[1], maxY);
				g.drawLine(   edge[0], y, edge[1], y);				
			}
		}
	}
	
	/** Called by {@link TRTreePane#paintComponent(Graphics)}. */
	private void drawArrow(Graphics g1, int x1, int y1, int x2, int y2)
    {
    	Graphics2D g2 = (Graphics2D)g1.create();
    	double dx = x2 - x1, dy = y2 - y1;
    	double angle = Math.atan2(dy, dx);
    	int len = (int)Math.sqrt(dx*dx + dy*dy);
    	
    	AffineTransform at = AffineTransform.getTranslateInstance(x1, y1);
    	at.concatenate(AffineTransform.getRotateInstance(angle));
    	g2.transform(at);
    	
    	g2.drawLine(0, 0, len, 0);
    	g2.fillPolygon(new int[]{len, len-ARROW_H, len-ARROW_H, len}, new int[]{0, -ARROW_W, ARROW_W, 0}, 4);
    }

}
