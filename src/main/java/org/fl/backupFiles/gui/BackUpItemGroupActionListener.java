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

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import org.fl.backupFiles.AbstractBackUpItem;
import org.fl.backupFiles.BackUpItemGroup;
import org.fl.backupFiles.BackUpItemList;

public class BackUpItemGroupActionListener implements java.awt.event.ActionListener {

	private final BackUpJTable backUpJTable;
	
	public BackUpItemGroupActionListener(BackUpJTable backUpJTable) {
		super();
		this.backUpJTable = backUpJTable;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		AbstractBackUpItem selectedEntry = backUpJTable.getSelectedBackUpItem();

		if ((selectedEntry != null) && (selectedEntry instanceof BackUpItemGroup backUpItemGroup)) {
			
			BackUpItemList backUpItemList = backUpItemGroup.getBackUpItems();
			
			BackUpTableModel backUpTableModel = new BackUpTableModel(backUpItemList);
			
			BackUpJTable backUpItemTable = new BackUpJTable(backUpTableModel);
			
			JScrollPane backUpItemsScrollTable = new JScrollPane(backUpItemTable);
			backUpItemsScrollTable.setPreferredSize(new Dimension(1800,700));
			
			JOptionPane.showMessageDialog(null, backUpItemsScrollTable, "Eléments du groupe (" + backUpItemGroup.getBackUpItemNumber() + ")", JOptionPane.INFORMATION_MESSAGE);
		}
		
	}

}
