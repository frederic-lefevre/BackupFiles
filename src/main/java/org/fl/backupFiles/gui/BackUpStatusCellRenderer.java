package org.fl.backupFiles.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import org.fl.backupFiles.BackUpItem.BackupStatus;

public class BackUpStatusCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 1L;
	private static final Color DIFFERENT_COLOR 		 = new Color(255, 220, 220) ;
	private static final Color DIFF_BY_CONTENT_COLOR = new Color(255, 120, 120) ;

	@Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
 
        BackupStatus backupStatus = (BackupStatus) value;
 
        setFont(getFont().deriveFont(Font.BOLD));
        if (backupStatus.equals(BackupStatus.DIFFERENT)) {
			setBackground(DIFFERENT_COLOR);
			setForeground(Color.BLACK);
        } else if (backupStatus.equals(BackupStatus.DONE)) {
			setBackground(Color.YELLOW);
			setForeground(Color.BLACK);
		} else if (backupStatus.equals(BackupStatus.FAILED)) {
			setBackground(Color.LIGHT_GRAY);
			setForeground(Color.RED);
		} else if (backupStatus.equals(BackupStatus.DIFF_BY_CONTENT)) {
			setBackground(DIFF_BY_CONTENT_COLOR) ;
			setForeground(Color.BLACK);
		}
		setHorizontalAlignment(SwingConstants.CENTER) ;
        return this;
    }
}
