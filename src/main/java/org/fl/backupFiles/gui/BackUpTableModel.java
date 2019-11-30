package org.fl.backupFiles.gui;

import javax.swing.table.AbstractTableModel;

import org.fl.backupFiles.BackUpItem;
import org.fl.backupFiles.BackUpItemList;

public class BackUpTableModel extends AbstractTableModel {

	public static final int SOURCE_PATH_COL_IDX = 0 ;
	public static final int SIZE_LIMT_COL_IDX 	= 1 ;
	public static final int ACTION_COL_IDX 		= 2 ;
	public static final int STATUS_COL_IDX 		= 3 ;
	public static final int TARGET_PATH_COL_IDX = 4 ;
	
	private static final long serialVersionUID = 1L;
	
	public final static String[] entetes = {"Chemin source", "Size limit", "Action", "Status", "Chemin cible"};
	
	// Underlying data
	private BackUpItemList backUpItems ;
	
	public BackUpTableModel(BackUpItemList bt) {
		super();
		backUpItems = bt ;		
	}

	public BackUpItemList getBackUpItems() {
		return backUpItems;
	}

	@Override
	public int getColumnCount() {
		return entetes.length;
	}

	@Override
	public int getRowCount() {
		return backUpItems.size() ;
	}

	@Override
	public String getColumnName(int col) {
	    return entetes[col];
	}
	
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch(columnIndex){
        case SOURCE_PATH_COL_IDX:
            return backUpItems.get(rowIndex).getSourcePath();
        case SIZE_LIMT_COL_IDX:
        	return backUpItems.get(rowIndex).isAboveSizeThreshold() ;
        case ACTION_COL_IDX:
            return backUpItems.get(rowIndex).getBackupAction();
        case STATUS_COL_IDX:
            return backUpItems.get(rowIndex).getBackupStatus();
        case TARGET_PATH_COL_IDX:
            return backUpItems.get(rowIndex).getTargetPath();
        default:
            return null;
		}
	}

	public BackUpItem getBackUpItemAt(int rowIndex) {
		return backUpItems.get(rowIndex) ;
	}
}
