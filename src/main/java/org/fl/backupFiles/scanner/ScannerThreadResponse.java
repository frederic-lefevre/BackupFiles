package org.fl.backupFiles.scanner;

import java.nio.file.Path;
import java.util.ArrayList;

import org.fl.backupFiles.BackUpCounters;
import org.fl.backupFiles.BackUpItemList;
import org.fl.backupFiles.BackUpTask;

public class ScannerThreadResponse {

	private final BackUpTask 	  backUpTask ;
	private final ArrayList<Path> filesVisitFailed ;	

	private final BackUpItemList backUpItemList ;	
	private final BackUpCounters backUpCounters ;
	
	public ScannerThreadResponse(BackUpTask but, BackUpItemList bil, BackUpCounters buc, ArrayList<Path> fvf) {
		
		backUpTask 		 = but ;
		backUpItemList 	 = bil ;
		backUpCounters 	 = buc ;
		filesVisitFailed = fvf ;
	}

	public BackUpTask getBackUpTask() {
		return backUpTask;
	}

	public ArrayList<Path> getFilesVisitFailed() {
		return filesVisitFailed;
	}

	public BackUpItemList getBackUpItemList() {
		return backUpItemList;
	}

	public BackUpCounters getBackUpCounters() {
		return backUpCounters;
	}
}
