package org.fl.backupFiles.gui;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class BackUpJobInfoPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	public BackUpJobInfoPanel(BackUpJobInfoTableModel tabModel) {
		
		super() ;
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)) ;
		setBorder(BorderFactory.createLineBorder(Color.BLACK,5,true)) ;
		
		JTable jobInfosTable = new JTable(tabModel);

		jobInfosTable.setRowHeight(250);
		
		// Set column width
		for (int colNum=0; colNum < tabModel.getColumnCount() ; colNum++) {
			jobInfosTable.getColumnModel().getColumn(colNum).setPreferredWidth(tabModel.getColumnPreferredWith(colNum));
		}
		
		// Table headers
		add(jobInfosTable.getTableHeader()) ;
		
		// Scroll pane to contain the table
		JScrollPane infoScrollTable = new JScrollPane(jobInfosTable) ;
		
		add(infoScrollTable) ;
	}

}
