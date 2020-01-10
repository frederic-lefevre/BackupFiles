package org.fl.backupFiles.gui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.fl.backupFiles.directoryPermanence.DirectoryPermanenceLevel;

public class PermanenceCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 1L;

	private static final Color HIGH_COLOR = Color.RED ;
	private static final Color MEDIUM_COLOR = Color.PINK ;
	
	@Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
 
        DirectoryPermanenceLevel permanenceLevel = (DirectoryPermanenceLevel) value ;
        
        if (permanenceLevel.equals(DirectoryPermanenceLevel.HIGH)) {
        	setBackground(HIGH_COLOR) ;
        } else if (permanenceLevel.equals(DirectoryPermanenceLevel.MEDIUM)) {
        	setBackground(MEDIUM_COLOR) ;
        } else {
        	setBackground(Color.WHITE) ;
        }
        return this ;
	}
}
