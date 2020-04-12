package org.fl.backupFiles.gui.workers;

import org.fl.backupFiles.scanner.ScannerThreadResponse;

public class BackupScannerInformation {

	// Stocke une information sur la progression du scanner
	// et le résultat si il est terminé
		
	private final String information ;	
	private final ScannerThreadResponse scannerThreadResponse;
	
	public BackupScannerInformation(String info, ScannerThreadResponse str) {
		information 	 = info ;
		scannerThreadResponse = str ;
	}
	
	public String 				 getInformation() 	    	{ return information ; 	    	}
	public ScannerThreadResponse getScannerThreadResponse() { return scannerThreadResponse; }
}
