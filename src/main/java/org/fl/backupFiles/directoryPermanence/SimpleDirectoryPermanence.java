package org.fl.backupFiles.directoryPermanence;

import java.nio.file.Path;

public class SimpleDirectoryPermanence implements DirectoryPermanence {

	
	public SimpleDirectoryPermanence() {
		super();
	}

	@Override
	public DirectoryPermanenceLevel getPermanenceLevel(Path dir) {
		
		return DirectoryPermanenceLevel.MEDIUM;
	}

}
