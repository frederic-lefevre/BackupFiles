package org.fl.backupFiles.directoryPermanence;

import java.nio.file.Path;
import java.util.Comparator;

public class PermanencePathComparator implements Comparator<Path> {

	@Override
	public int compare(Path path1, Path path2) {
		
		if (path1.equals(path2)) {
			return 0 ;
		} else if (path1.startsWith(path2)) {
			return -1 ;
		} else if (path2.startsWith(path1)) {
			return 1 ;
		} else {
			return path1.compareTo(path2) ;
		}
	}

}
