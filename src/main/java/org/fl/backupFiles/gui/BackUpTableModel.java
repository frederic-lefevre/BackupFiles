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

import java.nio.file.Path;

import javax.swing.table.AbstractTableModel;

import org.fl.backupFiles.AbstractBackUpItem;
import org.fl.backupFiles.BackUpItemList;
import org.fl.backupFiles.BackupAction;
import org.fl.backupFiles.BackupStatus;
import org.fl.backupFiles.directoryGroup.DirectoryPermanenceLevel;

public class BackUpTableModel extends AbstractTableModel {

	public static final int SOURCE_PATH_COL_IDX = 0;
	public static final int GROUP_COL_IDX = 1;
	public static final int SIZE_DIFF_COL_IDX = 2;
	public static final int PERMANENCE_COL_IDX = 3;
	public static final int ACTION_COL_IDX = 4;
	public static final int STATUS_COL_IDX = 5;
	public static final int TARGET_PATH_COL_IDX = 6;
	
	private static final long serialVersionUID = 1L;
	
	private final static String[] entetes = {"Chemin origine", "Groupé", "Taille", "Permanence", "Action", "Etat", "Chemin destination"};
	
	// Underlying data
	private BackUpItemList backUpItems;
	
	public BackUpTableModel(BackUpItemList bt) {
		super();
		backUpItems = bt;		
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
		return backUpItems.size();
	}

	@Override
	public String getColumnName(int col) {
	    return entetes[col];
	}
	
	// AbstractTableModel.getColumnClass is overridden because it interprets numbers as string
	// So they are not sorted in number natural order, in case of negative numbers
	@Override
	public Class<?> getColumnClass(int columnIndex) {

		return switch(columnIndex){
    		case SOURCE_PATH_COL_IDX -> Path.class;
    		case GROUP_COL_IDX -> AbstractBackUpItem.class;
    		case SIZE_DIFF_COL_IDX -> AbstractBackUpItem.class;
    		case PERMANENCE_COL_IDX -> DirectoryPermanenceLevel.class;
    		case ACTION_COL_IDX -> BackupAction.class;
    		case STATUS_COL_IDX -> BackupStatus.class;
    		case TARGET_PATH_COL_IDX -> Path.class;
    		default -> super.getColumnClass(columnIndex);
		};
	}
	 
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return switch(columnIndex){
        	case SOURCE_PATH_COL_IDX -> backUpItems.get(rowIndex).getSourcePath();
        	case GROUP_COL_IDX -> backUpItems.get(rowIndex);
        	case SIZE_DIFF_COL_IDX -> backUpItems.get(rowIndex);
        	case PERMANENCE_COL_IDX -> backUpItems.get(rowIndex).getPermanenceLevel();
        	case ACTION_COL_IDX -> backUpItems.get(rowIndex).getBackupAction();
        	case STATUS_COL_IDX -> backUpItems.get(rowIndex).getBackupStatus();
        	case TARGET_PATH_COL_IDX -> backUpItems.get(rowIndex).getTargetPath();
        	default -> null;
		};
	}

	public AbstractBackUpItem getBackUpItemAt(int rowIndex) {
		return backUpItems.get(rowIndex);
	}
}
