/*
 * MIT License

Copyright (c) 2017, 2023 Frederic Lefevre

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
import java.util.logging.Logger;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import org.fl.backupFiles.BackUpItem ;
import org.fl.backupFiles.Config;

public class BackUpJTable extends JTable {

	private static final long serialVersionUID = 1L;
	private Logger tLog ;

	public BackUpJTable(TableModel arg0, Logger l) {
		super(arg0);	
		init(l);	
	}

	public BackUpJTable(TableModel arg0, TableColumnModel arg1, Logger l) {
		super(arg0, arg1);
		init(l);
	}

	
	public BackUpJTable(TableModel dm, TableColumnModel cm, ListSelectionModel sm, Logger l) {
		super(dm, cm, sm);
		init(l);
	}

	private void init(Logger l) {
		tLog = l ;
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
		
		addMouseListener(new BackupItemMouseAdapter(this, Config.getOsActions(), tLog));
		setAutoCreateRowSorter(true);
	}
	
	// Get the list of selected BackUpItem
	public List<BackUpItem> getSelectedBackUpItems() {
		
		int[] rowIdxs = getSelectedRows() ;
		List<BackUpItem> res = new ArrayList<BackUpItem>() ;
		for (int idx : rowIdxs) {
			res.add(((BackUpTableModel)getModel()).getBackUpItemAt(convertRowIndexToModel(idx))) ;
		}
		return res ;
	}

}
