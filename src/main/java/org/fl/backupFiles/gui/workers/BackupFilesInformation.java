package org.fl.backupFiles.gui.workers;

public class BackupFilesInformation {

	// Stocke le status du backup processor
	// nombre de fichiers traités
	private long   nbFilesProcessed ;
	
	public BackupFilesInformation(long nb) {
		nbFilesProcessed = nb ;
	}

	public long   getNbFilesProcessed() { return nbFilesProcessed ; }
}