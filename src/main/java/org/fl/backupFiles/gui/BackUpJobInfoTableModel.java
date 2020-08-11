package org.fl.backupFiles.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.fl.backupFiles.BackUpJobInformation;

public class BackUpJobInfoTableModel extends AbstractTableModel {


	private static final long serialVersionUID = 1L;
	
	private final static String[] entetes = {"Configuration", "Date", "Résultat", "Opération", "Type"};

	private List<BackUpJobInformation> jobInfosList ;
	
	public BackUpJobInfoTableModel() {
		super() ;
		jobInfosList = new ArrayList<BackUpJobInformation>() ;
	}

	public void add(BackUpJobInformation jobInfo) {
		jobInfosList.add(jobInfo) ;
	}
	
	@Override
	public int getColumnCount() {
		return entetes.length ;
	}

	@Override
	public int getRowCount() {
		return jobInfosList.size() ;
	}
	
	@Override
	public String getColumnName(int col) {
	    return entetes[col];
	}
	
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		
		switch(columnIndex){
        case 0:
        	return jobInfosList.get(rowIndex).getJobTitle() ;
        case 1:
        	return jobInfosList.get(rowIndex).getJobEnd() ;
        case 2:
        	return jobInfosList.get(rowIndex).getJobResult() ;
        case 3:
        	return jobInfosList.get(rowIndex).getJobOperation() ;
        case 4:
        	return jobInfosList.get(rowIndex).getJobDirection() ;
        default:
        	return null;
		}
	}
	
	public int getColumnPreferredWith(int columnIndex) {
		switch(columnIndex){
        case 0:
        	return 460 ;
        case 1:
        	return 320 ;
        case 2:
        	return 700 ;
        case 3:
        	return 180 ;
        case 4:
        	return 180 ;
        default: 
        	return 0;
		}
	}
}
