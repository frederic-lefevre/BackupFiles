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
		getColumnModel().getColumn(2).setCellRenderer(new BackUpActionCellRenderer()) ;
		getColumnModel().getColumn(3).setCellRenderer(new BackUpStatusCellRenderer()) ;
		getColumnModel().getColumn(0).setPreferredWidth(810);
		getColumnModel().getColumn(1).setPreferredWidth(120);
		getColumnModel().getColumn(2).setPreferredWidth(120);
		getColumnModel().getColumn(3).setPreferredWidth(100);
		getColumnModel().getColumn(4).setPreferredWidth(810);
		setAutoResizeMode(JTable.AUTO_RESIZE_OFF) ;
		
		addMouseListener(new BackupItemMouseAdapter(this, Config.getOsActions(), tLog));
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
