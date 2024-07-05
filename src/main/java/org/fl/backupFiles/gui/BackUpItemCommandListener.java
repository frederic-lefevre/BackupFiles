/*
 * MIT License

Copyright (c) 2017, 2024 Frederic Lefevre

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
import java.util.logging.Logger;

import org.fl.backupFiles.BackUpItem;
import org.fl.backupFiles.Config;
import org.fl.util.os.OScommand;

public class BackUpItemCommandListener implements java.awt.event.ActionListener{

	private static final Logger bLog = Config.getLogger();
	
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

		BackUpItem selectedEntry = backUpJTable.getSelectedBackUpItem();

		if (selectedEntry != null) {
			// Command defined in the property file, passed for execution to the OS (maybe an external binary editor for instance)
			StringBuilder fullCommand = new StringBuilder(command) ;

			String filePaths = null ;

			if (fileElement.equals(FileElement.Source)) {
				filePaths = selectedEntry.getSourcePath().toAbsolutePath().toString() ;
			} else if (fileElement.equals(FileElement.Cible)) {
				filePaths = selectedEntry.getTargetPath().toAbsolutePath().toString() ;
			} else if (fileElement.equals(FileElement.Both)) {
				filePaths = selectedEntry.getSourcePath().toAbsolutePath().toString() + " " + selectedEntry.getTargetPath().toAbsolutePath().toString() ;
			}
			if (filePaths != null) {
				fullCommand.append(" ").append(filePaths) ;
			}

			OScommand osCommand = new OScommand(fullCommand.toString(), null, null, false, bLog) ;
			osCommand.run();
		}
	}
	
}
