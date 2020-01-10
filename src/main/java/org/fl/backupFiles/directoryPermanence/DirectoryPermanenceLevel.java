package org.fl.backupFiles.directoryPermanence;

public enum DirectoryPermanenceLevel {
	HIGH ("1: Haut"), MEDIUM("2: Moyen"), LOW("3: Faible") ;
	private String name ;
	private DirectoryPermanenceLevel(String n) {
		name  = n ;
	}
	@Override
    public String toString()  { return name ; }
}
