package org.fl.backupFiles.gui.workers;

import org.fl.backupFiles.scanner.ScannerThreadResponse;

public class BackupScannerInformation {

	// Stocke le status du process
	// status , information , et résultat si il est terminé
		
	private final String status ;
	private final String information ;	
	private final ScannerThreadResponse scannerThreadResponse;
	
	public BackupScannerInformation(String st, String info, ScannerThreadResponse str) {
		status 			 = st ;
		information 	 = info ;
		scannerThreadResponse = str ;
	}
	
	public String 				 getStatus() 	 	    	{ return status ;	        	}
	public String 				 getInformation() 	    	{ return information ; 	    	}
	public ScannerThreadResponse getScannerThreadResponse() { return scannerThreadResponse; }
}
