package org.fl.backupFiles.directoryPermanence;

public enum DirectoryPermanenceLevel {
	HIGH ("Haut"), MEDIUM("Moyen"), LOW("Faible") ;
	private String name ;
	private DirectoryPermanenceLevel(String n) {
		name  = n ;
	}
	@Override
    public String toString()  { return name ; }
}
