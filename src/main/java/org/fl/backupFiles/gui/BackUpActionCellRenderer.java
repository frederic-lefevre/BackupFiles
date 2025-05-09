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
import java.awt.Font;

import javax.swing.SwingConstants;

import org.fl.backupFiles.BackupAction;
import org.fl.backupFiles.IllegalBackupActionException;
import org.fl.util.swing.CustomTableCellRenderer;

public class BackUpActionCellRenderer extends CustomTableCellRenderer {

	private static final long serialVersionUID = 1L;
	private static final Font font = new Font("Dialog", Font.BOLD, 12);
	
	public BackUpActionCellRenderer() {
		
		super(font, SwingConstants.CENTER);	
	}
	
	@Override
	public void valueProcessor(Object value) {
		
		if (value instanceof BackupAction backupAction) {
			switch (backupAction) {
				case COPY_REPLACE -> setBackground(Color.MAGENTA);
				case COPY_NEW, COPY_TREE -> setBackground(Color.GREEN);
				case DELETE, DELETE_DIR -> setBackground(Color.ORANGE);
				case COPY_TARGET, ADJUST_TIME -> setBackground(Color.PINK);
				case AMBIGUOUS -> setBackground(Color.RED);
			}
			setValue(backupAction.getActionName());
		} else {
			throw new IllegalBackupActionException(value);
		}	
	}
}
