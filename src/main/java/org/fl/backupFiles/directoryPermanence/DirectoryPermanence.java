package org.fl.backupFiles.directoryPermanence;

import java.nio.file.Path;

public interface DirectoryPermanence {

	public DirectoryPermanenceLevel getPermanenceLevel(Path dir) ;
	
}
