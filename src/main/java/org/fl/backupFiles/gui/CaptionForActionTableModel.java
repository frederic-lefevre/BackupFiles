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

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.table.AbstractTableModel;

import org.fl.backupFiles.BackupAction;

public class CaptionForActionTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;
	
	private static final String[] entetes = {"Action", "Détail"};
	
	public static final int ACTION_COL_IDX = 0;
	public static final int DETAIL_COL_IDX = 1;
	
	private static final List<BackupAction> actions = Stream.of(BackupAction.values()).collect(Collectors.toList());
	
	public CaptionForActionTableModel() {
		super();
	}

	@Override
	public int getRowCount() {
		return actions.size();
	}

	@Override
	public int getColumnCount() {
		return entetes.length;
	}

	@Override
	public String getColumnName(int col) {
	    return entetes[col];
	}
	
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		
		return switch (columnIndex) {
			case ACTION_COL_IDX -> actions.get(rowIndex);
			case DETAIL_COL_IDX -> actions.get(rowIndex).getActionDetails();
			default -> null;
		};
	}
}
