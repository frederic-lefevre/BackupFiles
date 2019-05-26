package org.fl.backupFiles.scanner;

import java.nio.file.Path;
import java.util.ArrayList;

import org.fl.backupFiles.BackUpCounters;
import org.fl.backupFiles.BackUpItemList;
import org.fl.backupFiles.BackUpTask;

public class ScannerThreadResponse {

	private BackUpTask backUpTask ;
	private ArrayList<Path> filesVisitFailed ;	

	private BackUpItemList 	backUpItemList ;	
	private BackUpCounters backUpCounters ;
	
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
