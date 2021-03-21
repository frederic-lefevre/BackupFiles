package org.fl.backupFiles;

import org.fl.backupFiles.BackUpItem.BackupAction;

public class IllegalBackupActionException extends IllegalArgumentException {

	private static final long serialVersionUID = 1L;
	
	public IllegalBackupActionException(String s, BackupAction backupAction) {
		super(s + " Action=" + backupAction);
	}
}
