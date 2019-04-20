package org.fl.backupFiles.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import org.fl.backupFiles.BackUpItem.BackupAction;

public class BackUpActionCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 1L;

	@Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
 
        BackupAction backupAction = (BackupAction) value;
 
        setFont(getFont().deriveFont(Font.BOLD));
		if (backupAction.equals(BackupAction.COPY_REPLACE)) {
			setBackground(Color.MAGENTA);
		} else if (backupAction.equals(BackupAction.COPY_NEW)) {
			setBackground(Color.GREEN);
		} else if (backupAction.equals(BackupAction.DELETE)) {
			setBackground(Color.ORANGE);
		} else if (backupAction.equals(BackupAction.COPY_TREE)) {
			setBackground(Color.GREEN);
		} else if (backupAction.equals(BackupAction.DELETE_DIR)) {				
			setBackground(Color.ORANGE);
		} else if (backupAction.equals(BackupAction.AMBIGUOUS)) {
			setBackground(Color.RED);
		}
		setHorizontalAlignment(SwingConstants.CENTER) ;
        return this;
    }

}
