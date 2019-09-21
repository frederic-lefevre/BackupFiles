package org.fl.backupFiles.gui;

import javax.swing.table.AbstractTableModel;

import org.fl.backupFiles.BackUpItem;
import org.fl.backupFiles.BackUpItemList;

public class BackUpTableModel extends AbstractTableModel {


	private static final long serialVersionUID = 1L;
	
	public final static String[] entetes = {"Chemin source", "Action", "Status", "Chemin cible"};
	
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
        case 0:
            return backUpItems.get(rowIndex).getSourcePath();
        case 1:
            return backUpItems.get(rowIndex).getBackupAction();
        case 2:
            return backUpItems.get(rowIndex).getBackupStatus();
        case 3:
            return backUpItems.get(rowIndex).getTargetPath();
        default:
            return null;
		}
	}

	public BackUpItem getBackUpItemAt(int rowIndex) {
		return backUpItems.get(rowIndex) ;
	}
}
