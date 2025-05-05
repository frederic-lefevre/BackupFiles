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
import java.util.logging.Logger;

import javax.swing.SwingConstants;

import org.fl.backupFiles.BackUpItem;
import org.fl.backupFiles.BackUpItemGroup;
import org.fl.util.swing.CustomTableCellRenderer;

public class GroupCellRenderer extends CustomTableCellRenderer  {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(GroupCellRenderer.class.getName());
	
	private static final Font font = new Font("Dialog", Font.PLAIN, 12);
	
	public GroupCellRenderer() {
		super(font, SwingConstants.RIGHT);
	}

	private static final Color BACKUP_ITEM_GROUP_COLOR = new Color(180, 220, 255);
	private static final Color BACKUP_ITEM_COLOR = Color.WHITE;

	@Override
	public void valueProcessor(Object value) {
		
		if (value instanceof BackUpItem backUpItem) {
			setValue(backUpItem.getBackUpItemNumber()); // should always be 1
			setBackground(BACKUP_ITEM_COLOR);
		} else if (value instanceof BackUpItemGroup backUpItemGroup) {
			setValue(backUpItemGroup.getBackUpItemNumber()); // may be 1 if there is a single element in the group
			setBackground(BACKUP_ITEM_GROUP_COLOR);
		} else {
			logger.severe("Invalid value type in Groupé cell. Should be a AbstractBackUpItem subclass but is " + value.getClass().getName());
		}
		
	}

}
