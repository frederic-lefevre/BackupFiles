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

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import org.fl.backupFiles.directoryPermanence.DirectoryPermanenceLevel;

public class PermanenceCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 1L;

	private static final Color HIGH_COLOR = Color.RED;
	private static final Color MEDIUM_COLOR = Color.PINK;
	
	public PermanenceCellRenderer() {
		super();
		setHorizontalAlignment(SwingConstants.CENTER);
	}
	
	@Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
 
        DirectoryPermanenceLevel permanenceLevel = (DirectoryPermanenceLevel) value;
        
        if (permanenceLevel.equals(DirectoryPermanenceLevel.HIGH)) {
        	setBackground(HIGH_COLOR);
        } else if (permanenceLevel.equals(DirectoryPermanenceLevel.MEDIUM)) {
        	setBackground(MEDIUM_COLOR);
        } else {
        	setBackground(Color.WHITE);
        }
    
        return this;
	}
}
