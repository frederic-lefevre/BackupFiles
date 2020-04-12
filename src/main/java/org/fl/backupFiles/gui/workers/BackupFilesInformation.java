package org.fl.backupFiles.gui.workers;

public class BackupFilesInformation {

	// Stocke le status du backup processor
	// nombre de fichiers traités
	private int nbFilesProcessed ;
	
	public BackupFilesInformation(int nb) {
		nbFilesProcessed = nb ;
	}

	public int getNbFilesProcessed() { return nbFilesProcessed ; }
}