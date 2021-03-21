package org.fl.backupFiles;

import java.nio.file.Path;

public class IllegalBackUpItemException extends IllegalArgumentException {

	private static final long serialVersionUID = 1L;

	public IllegalBackUpItemException(String s, Path p) {
		super(s + "Path=" + p);
	}
	
}
