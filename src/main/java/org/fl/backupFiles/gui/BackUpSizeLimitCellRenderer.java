package org.fl.backupFiles.gui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

public class BackUpSizeLimitCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 1L;
	private static final Color ABOVE_LIMIT_COLOR = new Color(255, 100, 100) ;
	private static final Color BELOW_LIMIT_COLOR = new Color(255, 255, 255) ;

	@Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
 
        Boolean aboveSizeLimit = (Boolean) value ;
        
        if (aboveSizeLimit) {
        	setBackground(ABOVE_LIMIT_COLOR) ;
        	setText("Elevée") ;
        } else {
        	setBackground(BELOW_LIMIT_COLOR) ;
        	setText("Normale") ;
        }
        setHorizontalAlignment(SwingConstants.CENTER) ;
        return this ;
	}

}
