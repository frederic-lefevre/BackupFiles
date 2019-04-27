package org.fl.backupFiles;


public class BackupFilesInformation {

	// Stocke le status du process
	// status , information , nombre de fichiers traités
	
	private String status ;
	private String information ;
	private long   nbFilesProcessed ;
	
	public BackupFilesInformation(String st, String info, long nb) {
		status 			 = nullToEmpty(st) ;
		information 	 = nullToEmpty(info) ;
		nbFilesProcessed = nb ;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = nullToEmpty(status) ;
	}

	public String getInformation() {
		return information;
	}

	public void setInformation(String information) {
		this.information = nullToEmpty(information) ;
	}

	public long getNbFilesProcessed() {
		return nbFilesProcessed;
	}

	public void setNbFilesProcessed(long nbFilesProcessed) {
		this.nbFilesProcessed = nbFilesProcessed;
	}

	private String nullToEmpty(String s) {
		
		if (s == null) {
			return "" ;
		} else {
			return s ;
		}
	}
}
