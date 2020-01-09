package org.fl.backupFiles.directoryPermanence;

import java.nio.file.Path;

public class DummyDirectoryPermanence implements DirectoryPermanence {
	
	public DummyDirectoryPermanence() {
		super();
	}

	@Override
	public DirectoryPermanenceLevel getPermanenceLevel(Path dir) {		
		return DirectoryPermanenceLevel.MEDIUM;
	}
}
