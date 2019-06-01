package org.fl.backupFiles;

import java.util.LinkedList;

import org.fl.backupFiles.BackUpItem.BackupStatus;

public class BackUpItemList extends LinkedList<BackUpItem> {

	private static final long serialVersionUID = 1L;
	
	public BackUpItemList() {
		super() ;
	}
	
	public void removeItemsDone() {
		
		int idx = 0 ;
		while (size() > idx) {
			if (get(idx).getBackupStatus().equals(BackupStatus.DONE)) {
				remove(idx) ;
			} else {
				idx++ ;
			}			
		}
	}
	
}
