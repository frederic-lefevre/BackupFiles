package org.fl.backupFiles.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.fl.backupFiles.BackUpTask;

public class JobConfigTableModel extends AbstractTableModel  {

	private static final long serialVersionUID = 1L;

	private final static String[] entetes = {"Chemin source", "Chemin cible", "Commentaires"};
	
	// underlying data
	private List<BackUpTask> backUpTasks ;
	
	public JobConfigTableModel() {
		backUpTasks = new ArrayList<BackUpTask>() ;
	}

	public void setBackUpTasks(List<BackUpTask> backUpTasks) {
		this.backUpTasks = backUpTasks;
	}

	@Override
	public int getColumnCount() {
		return entetes.length;
	}

	@Override
	public int getRowCount() {
		return backUpTasks.size();
	}

	@Override
	public String getColumnName(int col) {
	    return entetes[col];
	}
	
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch(columnIndex){
        case 0:
            return backUpTasks.get(rowIndex).getSource();
        case 1:
            return backUpTasks.get(rowIndex).getTarget();
        case 2:
            return backUpTasks.get(rowIndex).eventualWarning();
        default:
            return null;
		}
	}

}
