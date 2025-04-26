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

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.fl.backupFiles.BackUpJobInformation;

public class BackUpJobInfoTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;
	
	private static final String[] entetes = {"Configuration", "Date", "Résultat", "Opération", "Type"};

	private final List<BackUpJobInformation> jobInfosList;

	public BackUpJobInfoTableModel() {
		super();
		jobInfosList = new ArrayList<BackUpJobInformation>();
	}

	public void add(BackUpJobInformation jobInfo) {
		jobInfosList.add(jobInfo);
	}

	@Override
	public int getColumnCount() {
		return entetes.length;
	}

	@Override
	public int getRowCount() {
		return jobInfosList.size();
	}
	
	@Override
	public String getColumnName(int col) {
	    return entetes[col];
	}
	
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {

		return switch (columnIndex) {
			case 0 -> jobInfosList.get(rowIndex).getJobTitle();
			case 1 -> jobInfosList.get(rowIndex).getJobEnd();
			case 2 -> jobInfosList.get(rowIndex).getJobResult();
			case 3 -> jobInfosList.get(rowIndex).getJobOperation();
			case 4 -> jobInfosList.get(rowIndex).getJobDirection();
			default -> null;
		};
	}

	public int getColumnPreferredWith(int columnIndex) {
		
		return switch (columnIndex) {
			case 0 -> 300;
			case 1 -> 200;
			case 2 -> 980;
			case 3 -> 240;
			case 4 -> 120;
			default -> 0;
		};
	}
}
