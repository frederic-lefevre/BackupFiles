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

import java.util.logging.Logger;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import org.fl.backupFiles.BackUpItem ;
import org.fl.backupFiles.Config;

public class BackUpJTable extends JTable {

	private static final Logger tLog = Logger.getLogger(BackUpJTable.class.getName());
	
	private static final long serialVersionUID = 1L;

	public BackUpJTable(TableModel arg0) {
		super(arg0);	
		init();	
	}

	public BackUpJTable(TableModel arg0, TableColumnModel arg1) {
		super(arg0, arg1);
		init();
	}

	
	public BackUpJTable(TableModel dm, TableColumnModel cm, ListSelectionModel sm) {
		super(dm, cm, sm);
		init();
	}

	private void init() {
		
		setFillsViewportHeight(true) ;
		setAutoCreateRowSorter(true) ;
		getColumnModel().getColumn(BackUpTableModel.ACTION_COL_IDX).setCellRenderer(new BackUpActionCellRenderer()) ;
		getColumnModel().getColumn(BackUpTableModel.STATUS_COL_IDX).setCellRenderer(new BackUpStatusCellRenderer()) ;
		getColumnModel().getColumn(BackUpTableModel.SIZE_DIFF_COL_IDX).setCellRenderer(new BackUpSizeDifferenceCellRenderer());
		getColumnModel().getColumn(BackUpTableModel.PERMANENCE_COL_IDX).setCellRenderer(new PermanenceCellRenderer()) ;
		getColumnModel().getColumn(BackUpTableModel.SOURCE_PATH_COL_IDX).setPreferredWidth(805);
		getColumnModel().getColumn(BackUpTableModel.SIZE_DIFF_COL_IDX).setPreferredWidth(70);
		getColumnModel().getColumn(BackUpTableModel.PERMANENCE_COL_IDX).setPreferredWidth(85);
		getColumnModel().getColumn(BackUpTableModel.ACTION_COL_IDX).setPreferredWidth(120);
		getColumnModel().getColumn(BackUpTableModel.STATUS_COL_IDX).setPreferredWidth(100);
		getColumnModel().getColumn(BackUpTableModel.TARGET_PATH_COL_IDX).setPreferredWidth(745);

		// Allow single row selection only
		ListSelectionModel listSelectionModel = new DefaultListSelectionModel();
		listSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setSelectionModel(listSelectionModel);
		
		setAutoResizeMode(JTable.AUTO_RESIZE_OFF) ;
		
		addMouseListener(new BackupItemMouseAdapter(this, Config.getOsActions()));
		setAutoCreateRowSorter(true);
	}
	
	// Get the selected BackUpItem
	public BackUpItem getSelectedBackUpItem() {
		
		int[] rowIdxs = getSelectedRows();
		if (rowIdxs.length == 0) {
			return null;
		} else if (rowIdxs.length > 1) {
			tLog.severe("Found several selected rows for BackUpJTable. Number of selected rows: " + rowIdxs.length);
		}
		return ((BackUpTableModel)getModel()).getBackUpItemAt(convertRowIndexToModel(rowIdxs[0]));
	}

}
