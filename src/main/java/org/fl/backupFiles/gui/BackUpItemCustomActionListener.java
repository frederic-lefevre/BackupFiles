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
import java.awt.event.ActionEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.fl.backupFiles.AbstractBackUpItem;
import org.fl.backupFiles.BackUpItem;
import org.fl.util.os.OScommand;

public class BackUpItemCustomActionListener implements java.awt.event.ActionListener {
	
	private static final Logger bLog = Logger.getLogger(BackUpItemCustomActionListener.class.getName());
	
	private static final Font font = new Font("monospaced", Font.BOLD, 14);
	
	public enum CustomAction { Compare, ShowParentDir };
	
	private static Map<CustomAction, String> customActionCommands;
	
	private final BackUpJTable backUpJTable;
	private final CustomAction customAction;
	private final FileElement fileElement;
	
	public BackUpItemCustomActionListener(BackUpJTable bkt, CustomAction ca, FileElement elem) {
		
		backUpJTable = bkt;
		fileElement = elem;
		customAction = ca;
	}
	
	@Override
	public void actionPerformed(ActionEvent evt) {

		AbstractBackUpItem selectedEntry = backUpJTable.getSelectedBackUpItem();

		if ((selectedEntry != null) && (selectedEntry instanceof BackUpItem backUpItem)) {

			if (customAction.equals(CustomAction.Compare)) {
				// Display all possible informations on the source and target files 
				// including the result of a binary compare 

				StringBuilder compareInfos = new StringBuilder();

				backUpItem.getInformation(compareInfos);
				compareInfos.append("- - - - - - - - - - - - - - -\n");


				// Show informations in popup message
				JTextArea infoFiles = new JTextArea(40, 200);	
				infoFiles.setText(compareInfos.toString());
				infoFiles.setFont(font);
				JScrollPane infoFilesScroll = new JScrollPane(infoFiles);
				JOptionPane.showMessageDialog(null, infoFilesScroll, "Informations", JOptionPane.INFORMATION_MESSAGE);

			}  else if (customAction.equals(CustomAction.ShowParentDir)) {
				// Launch a file explorer on the parent directory

				String showParentDirCmd = customActionCommands.get(CustomAction.ShowParentDir);

				if ((showParentDirCmd != null) && (! showParentDirCmd.isEmpty())) {


					Path filePath ;
					if (fileElement.equals(FileElement.Source)) {
						filePath = backUpItem.getSourcePath();
					} else {
						filePath = backUpItem.getTargetPath();
					}

					if (filePath != null) {
						Path parentDir = filePath.getParent();
						if (Files.exists(parentDir)) {
							OScommand osCommand = new OScommand(showParentDirCmd, List.of(parentDir.toAbsolutePath().toString()), null, false, bLog);
							osCommand.run();
						} else {
							bLog.warning("Showing parent directory: parent directory of " + filePath + " does not exists");
						}
					} else {
						bLog.warning("Action to show parent directory with a null file path");
					}
				}
			}
		}
	}

	public static void setCustomActionCommands(HashMap<CustomAction, String> cac) {
		customActionCommands = cac;
	}
}
