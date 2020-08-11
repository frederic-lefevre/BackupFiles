package org.fl.backupFiles.scanner;

import java.nio.file.Path;
import java.util.List;

import org.fl.backupFiles.BackUpCounters;
import org.fl.backupFiles.BackUpItemList;
import org.fl.backupFiles.BackUpTask;

public class ScannerThreadResponse {

	private final BackUpTask backUpTask ;
	private final List<Path> filesVisitFailed ;	

	private final BackUpItemList backUpItemList ;	
	private final BackUpCounters backUpCounters ;
	private final String 		 status ;
	
	private boolean hasNotBeenProcessed ;
	
	public ScannerThreadResponse(BackUpTask but, BackUpItemList bil, BackUpCounters buc, List<Path> fvf, String st) {
		
		backUpTask 		 = but ;
		backUpItemList 	 = bil ;
		backUpCounters 	 = buc ;
		filesVisitFailed = fvf ;
		status			 = st  ;
		
		hasNotBeenProcessed = true ;
	}

	public BackUpTask 	   getBackUpTask() 		 { return backUpTask ; 			}
	public List<Path> 	   getFilesVisitFailed() { return filesVisitFailed ; 	}
	public BackUpItemList  getBackUpItemList() 	 { return backUpItemList ; 	 	}
	public BackUpCounters  getBackUpCounters() 	 { return backUpCounters ;	    }
	public String 		   getStatus() 			 { return status ;				}

	public boolean 		   hasNotBeenProcessed() { return hasNotBeenProcessed ; }
	
	public void setHasNotBeenProcessed(boolean hasNotBeenProcessed) {	
		this.hasNotBeenProcessed = hasNotBeenProcessed ;	
	}
}
