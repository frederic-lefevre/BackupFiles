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

import java.awt.Desktop;

import java.awt.event.ActionEvent;
import java.io.File;

import org.fl.backupFiles.BackUpItem;
import org.fl.util.swing.FileActions;

public class BackUpItemActionListener implements java.awt.event.ActionListener {
	
	private BackUpJTable backUpJTable;
	private Desktop.Action action;
	private FileElement fileElement;
	
	public BackUpItemActionListener(BackUpJTable bkt, Desktop.Action act, FileElement elem) {
		backUpJTable = bkt;
		action = act;
		fileElement = elem;
	}
	
	@Override
	public void actionPerformed(ActionEvent evt) {

		BackUpItem selectedEntry = backUpJTable.getSelectedBackUpItem() ;

		if (selectedEntry != null) {
			// OS command (Java Desktop class OS action on File object: Edit, Open, Print)

			File file = null;
			if (fileElement.equals(FileElement.Source)) {
				file = selectedEntry.getSourceFile();
			} else if (fileElement.equals(FileElement.Cible)) {
				file = selectedEntry.getTargetFile();
			}
			if (file != null) {
				FileActions.launchAction(file, action);
			}
		} 
	}

}
