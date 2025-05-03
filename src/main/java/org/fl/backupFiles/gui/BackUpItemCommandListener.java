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

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.logging.Logger;

import org.fl.backupFiles.AbstractBackUpItem;
import org.fl.backupFiles.BackUpItem;
import org.fl.util.os.OScommand;

public class BackUpItemCommandListener implements java.awt.event.ActionListener{

	private static final Logger bLog = Logger.getLogger(BackUpItemCommandListener.class.getName());
	
	private final BackUpJTable backUpJTable;
	private final String command;
	private final FileElement fileElement;
	
	public BackUpItemCommandListener(BackUpJTable bkt, String cmd, FileElement elem) {
		backUpJTable = bkt;
		fileElement = elem;
		command	= cmd;
	}
	
	@Override
	public void actionPerformed(ActionEvent evt) {

		AbstractBackUpItem selectedEntry = backUpJTable.getSelectedBackUpItem();

		if ((selectedEntry != null) && (selectedEntry instanceof BackUpItem backUpItem))  {
			// Command defined in the property file, passed for execution to the OS (maybe an external binary editor for instance)

			List<String> filePaths;
			if (fileElement.equals(FileElement.Source)) {
				filePaths = List.of(backUpItem.getSourcePath().toAbsolutePath().toString());
			} else if (fileElement.equals(FileElement.Cible)) {
				filePaths = List.of(backUpItem.getTargetPath().toAbsolutePath().toString());
			} else if (fileElement.equals(FileElement.Both)) {
				filePaths = List.of(backUpItem.getSourcePath().toAbsolutePath().toString(), backUpItem.getTargetPath().toAbsolutePath().toString());
			} else {
				filePaths = null;
			}

			OScommand osCommand = new OScommand(command, filePaths, null, false, bLog) ;
			osCommand.run();
		}
	}
	
}
