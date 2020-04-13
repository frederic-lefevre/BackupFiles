package org.fl.backupFiles.directoryPermanence;

import java.nio.file.Path;

public interface DirectoryPermanence {

	public final static DirectoryPermanenceLevel DEFAULT_PERMANENCE_LEVEL = DirectoryPermanenceLevel.HIGH ;
	
	public DirectoryPermanenceLevel getPermanenceLevel(Path dir) ;
	
}
