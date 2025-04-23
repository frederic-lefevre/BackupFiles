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

import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;

public class CaptionPane extends JPanel{

	private static final long serialVersionUID = 1L;
	private static final int TOP_CAPTION_SPACE = 25;
	
	private static final Font font = new Font("Verdana", Font.BOLD, 18);
	
	public CaptionPane() {
		super();
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		showCaption("Actions", new CaptionForActionTable());
		showCaption("Etat", new CaptionForStatusTable());
	}
	
	private void showCaption(String title, JTable captionTable) {
		
		JLabel captionTitle = new JLabel(title);
		captionTitle.setFont(font);
		captionTitle.setBorder(BorderFactory.createEmptyBorder(TOP_CAPTION_SPACE,0,0,0));
		
		add(captionTitle);
		add(captionTable.getTableHeader());
		add(captionTable);
	}
}
