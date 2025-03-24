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
import java.text.NumberFormat;
import java.util.Locale;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

import org.fl.backupFiles.BackUpItem;

public class BackUpSizeDifferenceCellRenderer extends JLabel implements TableCellRenderer {

	private static final Logger logger = Logger.getLogger(BackUpSizeDifferenceCellRenderer.class.getName());
	
	private static final Locale localeForFormat = Locale.FRANCE;
	private static final NumberFormat numberFormat = NumberFormat.getInstance(localeForFormat);
	
	private static final long serialVersionUID = 1L;
	private static final Color ABOVE_LIMIT_COLOR = new Color(255, 100, 100);
	private static final Color BELOW_LIMIT_COLOR = new Color(255, 255, 255);

	public BackUpSizeDifferenceCellRenderer() {
		super();
		setHorizontalAlignment(SwingConstants.CENTER);
		setOpaque(true);
	}
	
	@Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
 
		if (value instanceof BackUpItem) {
			BackUpItem backUpItem = (BackUpItem)value;
			long sizeDifference = backUpItem.getSizeDifference();
			setText(Long.toString(sizeDifference));

			setToolTipText(numberFormat.format(sizeDifference));

			if (sizeDifference > backUpItem.getFileSizeWarningThreshold()) {
				setBackground(ABOVE_LIMIT_COLOR);
			} else {
				setBackground(BELOW_LIMIT_COLOR);
			}
		} else {
			logger.severe("Invalid value type in Size Difference cell. Should be BackUpItem but is " + value.getClass().getName());
		}
        return this ;
	}

}
