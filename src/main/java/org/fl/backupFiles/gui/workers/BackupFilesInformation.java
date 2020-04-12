package org.fl.backupFiles.gui.workers;

public class BackupFilesInformation {

	// Stocke le status du backup processor
	// information , nombre de fichiers traités
	private String information ;
	private long   nbFilesProcessed ;
	
	public BackupFilesInformation(String info, long nb) {
		information 	 = info ;
		nbFilesProcessed = nb ;
	}

	public String getInformation() 	    { return information ; 	    }
	public long   getNbFilesProcessed() { return nbFilesProcessed ; }

}
