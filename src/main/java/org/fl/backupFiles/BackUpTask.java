package org.fl.backupFiles;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

public class BackUpTask {

	private final Path source ;
	private final Path target ;
	
	private boolean compareContent ;
	
	private Logger bLog ;
	
	private final static String warning1  = "  Attention : les chemins source et cible n'existent pas" ;
	private final static String warning2  = "  Attention : le chemin source n'existe pas" ;
	private final static String warning3  = "  Attention : le chemin cible n'existe pas" ;
	private final static String noWarning = "" ;
	
	// A back up task is a source directory to back up to a destination directory
	public BackUpTask(Path src, Path tgt, Logger l) {
		
		source = src ;
		target = tgt ;
		bLog   = l ;
		
		if (source == null) {
			bLog.severe("source path null when creating back up task") ;
		}		
		compareContent  = false ;
	}

	public Path getSource() {
		return source;
	}

	public Path getTarget() {
		return target;
	}

	public String toString() {
		return source.toString() + " ==> " + target.toString() ;  
	}
	
	public String eventualWarning() {
		
		String warning ;
		boolean sourceExists = Files.exists(source) ;
		boolean targetExists = Files.exists(target) ;
		if (!sourceExists && !targetExists) {
			warning = warning1 ;
		} else if (!sourceExists) {
			warning = warning2 ;
		} else if (!targetExists) {
			warning = warning3 ;
		} else {
			warning = noWarning ;
		}
		return warning ;
	}

	public boolean compareContent() {
		return compareContent;
	}

	public void setCompareContent(boolean compareContent) {
		this.compareContent = compareContent;
	}

	// Hashcode with lazy init
	private int hashcode = 0 ;
	
	@Override
	public int hashCode() {
		
		int result = hashcode ;
		
		if (result == 0) {
			final int prime = 31 ;
			result = 1 ;
			result = prime * result + ((source == null) ? 0 : source.hashCode()) ;
			result = prime * result + ((target == null) ? 0 : target.hashCode()) ;
		}
		return result ;
	}

	@Override
	public boolean equals(Object obj) {
		
		if (this == obj) 				  	return true  ;
		if (obj == null) 				  	return false ;		
		if (!(obj instanceof BackUpTask)) 	return false ;
		
		BackUpTask other = (BackUpTask) obj;
		if (source == null) {
			if (other.source != null) 		return false ;
		} else if (!source.equals(other.source)) {
			return false;
		}
		if (target == null) {
			if (other.target != null)		return false ;
		} else if (!target.equals(other.target)) {
			return false;
		}
		return true;
	}

}
