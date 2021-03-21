package org.fl.backupFiles;

import java.util.LinkedList;

import org.fl.backupFiles.BackUpItem.BackupStatus;

public class BackUpItemList extends LinkedList<BackUpItem> {

	private static final long serialVersionUID = 1L;
	
	public BackUpItemList() {
		super() ;
	}
	
	public void removeItemsDone() {
		removeIf(i -> i.getBackupStatus().equals(BackupStatus.DONE));
	}
	
}
