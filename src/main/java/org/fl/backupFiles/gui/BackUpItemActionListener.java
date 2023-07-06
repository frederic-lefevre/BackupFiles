/*
 * MIT License

Copyright (c) 2017, 2023 Frederic Lefevre

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
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.fl.backupFiles.BackUpItem;
import org.fl.util.os.OScommand;
import org.fl.util.swing.FileActions;

public class BackUpItemActionListener implements java.awt.event.ActionListener {

	public enum FileElement { Source, Cible, Both } ;
	
	public enum CustomAction { Compare, ShowParentDir } ;
	
	private static Map<CustomAction, String> customActionCommands ;
	
	private BackUpJTable   backUpJTable ;
	private Desktop.Action action ;
	private FileElement    fileElement ;
	private String 		   command ;
	private CustomAction   customAction ;
	private Logger		   bLog ;
	
	public BackUpItemActionListener(BackUpJTable bkt, Desktop.Action act, FileElement elem, Logger l) {
		backUpJTable = bkt ;
		action 		 = act ;
		fileElement  = elem ;
		command		 = null ;
		customAction = null ;
		bLog		 = l ;
	}

	public BackUpItemActionListener(BackUpJTable bkt, String cmd, FileElement elem, Logger l) {
		backUpJTable = bkt ;
		action 		 = null ;
		customAction = null ;
		fileElement  = elem ;
		command		 = cmd ;
		bLog		 = l ;
	}
	
	public BackUpItemActionListener(BackUpJTable bkt, CustomAction ca, FileElement elem, Logger l) {
		backUpJTable = bkt ;
		action 		 = null ;
		command		 = null ;
		fileElement  = elem ;
		customAction = ca ;
		bLog		 = l ;
	}
	
	@Override
	public void actionPerformed(ActionEvent evt) {
		
		BackUpItem selectedEntry = backUpJTable.getSelectedBackUpItem() ;
		
		if (action != null) {
			// OS command (Java Desktop class OS action on File object: Edit, Open, Print)

				File file = null ;
				if (fileElement.equals(FileElement.Source)) {
					file = selectedEntry.getSourceFile() ;
				} else if (fileElement.equals(FileElement.Cible)) {
					file = selectedEntry.getTargetFile();
				}
				if (file != null) {
					FileActions.launchAction(file, action);
				}
		
		} else if (command != null) {
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
			
			OScommand osCommand = new OScommand(fullCommand.toString(), false, bLog) ;
			osCommand.run();
		} else if (customAction.equals(CustomAction.Compare)) {
			// Display all possible informations on the source and target files 
			// including the result of a binary compare 
			
			StringBuilder compareInfos = new StringBuilder() ;

				selectedEntry.getInformation(compareInfos) ;
				compareInfos.append("- - - - - - - - - - - - - - -\n") ;
			
			
			// Show informations in popup message
			JTextArea infoFiles = new JTextArea(40, 200);	
			infoFiles.setText(compareInfos.toString());
			infoFiles.setFont(new Font("monospaced", Font.BOLD, 14));
			JScrollPane infoFilesScroll = new JScrollPane(infoFiles) ;
			JOptionPane.showMessageDialog(null, infoFilesScroll, "Informations", JOptionPane.INFORMATION_MESSAGE);
			
		}  else if (customAction.equals(CustomAction.ShowParentDir)) {
			// Launch a file explorer on the parent directory
			
			String showParentDirCmd = customActionCommands.get(CustomAction.ShowParentDir) ;
			
			if ((showParentDirCmd != null) && (! showParentDirCmd.isEmpty())) {

					
					Path filePath ;
					if (fileElement.equals(FileElement.Source)) {
						filePath = selectedEntry.getSourcePath() ;
					} else {
						filePath = selectedEntry.getTargetPath() ;
					}
				
					if (filePath != null) {
						Path parentDir = filePath.getParent();
						if (Files.exists(parentDir)) {
							OScommand osCommand = new OScommand(showParentDirCmd + " " + parentDir.toAbsolutePath(), false, bLog) ;
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

	public static void setCustomActionCommands(HashMap<CustomAction, String> cac) {
		customActionCommands = cac ;
	}
}
