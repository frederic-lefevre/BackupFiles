package org.fl.backupFiles;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestUtils {

	public static Path getPathFromUriString(String uriString) {
		return Paths.get(URI.create(uriString)) ;
	}
	


}
