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
	
	private boolean hasNotBeenProcessed ;
	
	public ScannerThreadResponse(BackUpTask but, BackUpItemList bil, BackUpCounters buc, ArrayList<Path> fvf) {
		
		backUpTask 		 = but ;
		backUpItemList 	 = bil ;
		backUpCounters 	 = buc ;
		filesVisitFailed = fvf ;
		
		hasNotBeenProcessed = true ;
	}

	public BackUpTask 	   getBackUpTask() 		 { return backUpTask ; 			}
	public ArrayList<Path> getFilesVisitFailed() { return filesVisitFailed ; 	}
	public BackUpItemList  getBackUpItemList() 	 { return backUpItemList ; 	 	}
	public BackUpCounters  getBackUpCounters() 	 { return backUpCounters ;	    }
	public boolean 		   hasNotBeenProcessed() { return hasNotBeenProcessed ; }
	
	public void setHasNotBeenProcessed(boolean hasNotBeenProcessed) {	
		this.hasNotBeenProcessed = hasNotBeenProcessed ;	
	}
}
