package org.fl.backupFiles;

public class BackupFilesInformation {

	// Stocke le status du process
	// status , information , nombre de fichiers traités
	
	private String status ;
	private String information ;
	private long   nbFilesProcessed ;
	
	public BackupFilesInformation(String st, String info, long nb) {
		status 			 = st ;
		information 	 = info ;
		nbFilesProcessed = nb ;
	}

	public String getStatus() 	 	    { return status ;	        }
	public String getInformation() 	    { return information ; 	    }
	public long   getNbFilesProcessed() { return nbFilesProcessed ; }

}
