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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import org.fl.backupFiles.BackUpItem.BackupStatus;

public class BackUpStatusCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 1L;
	private static final Color DIFFERENT_COLOR 		 = new Color(255, 220, 220);
	private static final Color DIFF_BY_CONTENT_COLOR = new Color(255, 120, 120);
	private static final Color SAME_CONTENT_COLOR = new Color(128, 191, 255);

	private static final Font font = new Font("Dialog", Font.BOLD, 12);
	
	public BackUpStatusCellRenderer() {
		super();
		setHorizontalAlignment(SwingConstants.CENTER);
	}
	
	@Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
 
        // DefaultTableCellRenderer.getTableCellRendererComponent sets the font at each call
        // So it is ineffective to change the font in the constructor
        setFont(font);
        
        BackupStatus backupStatus = (BackupStatus) value;
 
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
			setBackground(DIFF_BY_CONTENT_COLOR);
			setForeground(Color.BLACK);
		} else if (backupStatus.equals(BackupStatus.SAME_CONTENT)) {
			setBackground(SAME_CONTENT_COLOR);
			setForeground(Color.BLACK);
		}
        return this;
    }
}
