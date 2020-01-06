package org.fl.backupFiles.directoryPermanence;

public enum DirectoryPermanenceLevel {
	HIGH ("Haut", 100), MEDIUM("Moyen", 50), LOW("Faible", 10) ;
	private int level ;
	private String name ;
	private DirectoryPermanenceLevel(String n, int l) {
		name  = n ;
		level = l ;
	}
	public String getName() { return name ; }
}
