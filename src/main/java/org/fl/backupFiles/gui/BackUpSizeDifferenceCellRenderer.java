package org.fl.backupFiles.gui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

public class BackUpSizeDifferenceCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 1L;
	private static final Color ABOVE_LIMIT_COLOR = new Color(255, 100, 100) ;
	private static final Color BELOW_LIMIT_COLOR = new Color(255, 255, 255) ;

	private static long fileSizeWarningThreshold ;
	public static void setFileSizeWarningThreshold(long fileSizeWarningThreshold) {
		BackUpSizeDifferenceCellRenderer.fileSizeWarningThreshold = fileSizeWarningThreshold;
	}

	@Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
 
        Long sizeDifference = (Long)value ;
        
        if (sizeDifference > fileSizeWarningThreshold) {
        	setBackground(ABOVE_LIMIT_COLOR) ;
        } else {
        	setBackground(BELOW_LIMIT_COLOR) ;
        }
        setHorizontalAlignment(SwingConstants.CENTER) ;
        return this ;
	}

}
