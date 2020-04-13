package org.fl.backupFiles.gui;

import javax.swing.table.AbstractTableModel;

import org.fl.backupFiles.BackUpItem;
import org.fl.backupFiles.BackUpItemList;

public class BackUpTableModel extends AbstractTableModel {

	public static final int SOURCE_PATH_COL_IDX = 0 ;
	public static final int SIZE_DIFF_COL_IDX 	= 1 ;
	public static final int PERMANENCE_COL_IDX 	= 2 ;
	public static final int ACTION_COL_IDX 		= 3 ;
	public static final int STATUS_COL_IDX 		= 4 ;
	public static final int TARGET_PATH_COL_IDX = 5 ;
	
	private static final long serialVersionUID = 1L;
	
	public final static String[] entetes = {"Chemin source", "Taille", "Permanence", "Action", "Status", "Chemin cible"};
	
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
	
	// AbstractTableModel.getColumnClass is overridden because it interprets numbers as string
	// So they are not sorted in number natural order, in case of negative numbers
	@Override
	public Class<?> getColumnClass(int columnIndex) {

		if ((backUpItems == null) || (backUpItems.isEmpty())) {
			return super.getColumnClass(columnIndex);
		} else {
			Object val = getValueAt(0, columnIndex);
			if (val == null) {
				// Source or target path maybe null
				return super.getColumnClass(columnIndex);
			} else {
				return getValueAt(0, columnIndex).getClass();
			}
		}
	}
	 
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch(columnIndex){
        case SOURCE_PATH_COL_IDX:
            return backUpItems.get(rowIndex).getSourcePath();
        case SIZE_DIFF_COL_IDX:
        	return backUpItems.get(rowIndex).getSizeDifference() ;
        case PERMANENCE_COL_IDX:
        	return backUpItems.get(rowIndex).getPermanenceLevel() ;
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
