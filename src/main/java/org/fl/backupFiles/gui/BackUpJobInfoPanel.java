/*
 * MIT License

Copyright (c) 2017, 2025 Frederic Lefevre

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package org.fl.backupFiles.gui;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class BackUpJobInfoPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	public BackUpJobInfoPanel(BackUpJobInfoTableModel tabModel) {
		
		super();
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createLineBorder(Color.BLACK,5,true));
		
		JTable jobInfosTable = new JTable(tabModel);

		jobInfosTable.setRowHeight(300);
		
		// Set column width
		for (int colNum=0; colNum < tabModel.getColumnCount() ; colNum++) {
			jobInfosTable.getColumnModel().getColumn(colNum).setPreferredWidth(tabModel.getColumnPreferredWith(colNum));
		}
		
		// Table headers
		add(jobInfosTable.getTableHeader());
		
		// Scroll pane to contain the table
		JScrollPane infoScrollTable = new JScrollPane(jobInfosTable);
		
		add(infoScrollTable);
	}

}
