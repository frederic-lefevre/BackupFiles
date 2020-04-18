package org.fl.backupFiles.gui;

import java.util.ArrayList;
import java.util.logging.Logger;

import javax.swing.JTable;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import org.fl.backupFiles.BackUpItem ;
import org.fl.backupFiles.Config;

public class BackUpJTable extends JTable {

	private static final long serialVersionUID = 1L;
	private Logger tLog ;

	public BackUpJTable(TableModel arg0, Logger l) {
		super(arg0);
		
		init(l) ;	
	}

	public BackUpJTable(TableModel arg0, TableColumnModel arg1, Logger l) {
		super(arg0, arg1);
		init(l) ;
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
		setAutoResizeMode(JTable.AUTO_RESIZE_OFF) ;
		
		addMouseListener(new BackupItemMouseAdapter(this, Config.getOsActions(), tLog));
		setAutoCreateRowSorter(true);
	}
	
	// Get the list of selected BackUpItem
	public ArrayList<BackUpItem> getSelectedBackUpItems() {
		
		int[] rowIdxs = getSelectedRows() ;
		ArrayList<BackUpItem> res = new ArrayList<BackUpItem>() ;
		for (int idx : rowIdxs) {
			res.add(((BackUpTableModel)getModel()).getBackUpItemAt(convertRowIndexToModel(idx))) ;
		}
		return res ;
	}

}
