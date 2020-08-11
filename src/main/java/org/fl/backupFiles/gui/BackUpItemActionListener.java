package org.fl.backupFiles.gui;

import java.awt.Desktop;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.fl.backupFiles.BackUpItem;

import com.ibm.lge.fl.util.os.OScommand;
import com.ibm.lge.fl.util.swing.FileActions;

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
		
		List<BackUpItem> selectedEntries = backUpJTable.getSelectedBackUpItems() ;
		
		if (action != null) {
			// OS command (Java Desktop class OS action on File object: Edit, Open, Print)
			for (BackUpItem localEntry : selectedEntries) {
				File file = null ;
				if (fileElement.equals(FileElement.Source)) {
					file = localEntry.getSourceFile() ;
				} else if (fileElement.equals(FileElement.Cible)) {
					file = localEntry.getTargetFile();
				}
				if (file != null) {
					FileActions.launchAction(file, action);
				}
			}
		} else if (command != null) {
			// Command defined in the property file, passed for execution to the OS (maybe an external binary editor for instance)
			StringBuilder fullCommand = new StringBuilder(command) ;
			for (BackUpItem localEntry : selectedEntries) {
				
				String filePaths = null ;
				
				if (fileElement.equals(FileElement.Source)) {
					filePaths = localEntry.getSourcePath().toAbsolutePath().toString() ;
				} else if (fileElement.equals(FileElement.Cible)) {
					filePaths = localEntry.getTargetPath().toAbsolutePath().toString() ;
				} else if (fileElement.equals(FileElement.Both)) {
					filePaths = localEntry.getSourcePath().toAbsolutePath().toString() + " " + localEntry.getTargetPath().toAbsolutePath().toString() ;
				}
				if (filePaths != null) {
					fullCommand.append(" ").append(filePaths) ;
				}
			}
			OScommand osCommand = new OScommand(fullCommand.toString(), false, bLog) ;
			osCommand.run();
		} else if (customAction.equals(CustomAction.Compare)) {
			// Display all possible informations on the source and target files 
			// including the result of a binary compare 
			
			StringBuilder compareInfos = new StringBuilder() ;
			for (BackUpItem localEntry : selectedEntries) {
				localEntry.getInformation(compareInfos) ;
				compareInfos.append("- - - - - - - - - - - - - - -\n") ;
			}
			
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
				for (BackUpItem localEntry : selectedEntries) {
					
					Path filePath ;
					if (fileElement.equals(FileElement.Source)) {
						filePath = localEntry.getSourcePath() ;
					} else {
						filePath = localEntry.getTargetPath() ;
					}
				
					if ((filePath != null) && Files.exists(filePath )) {
						OScommand osCommand = new OScommand(showParentDirCmd + " " + filePath.getParent().toAbsolutePath(), false, bLog) ;
						osCommand.run();
					}
				}
			}
		}
	}

	public static void setCustomActionCommands(HashMap<CustomAction, String> cac) {
		customActionCommands = cac ;
	}
}
