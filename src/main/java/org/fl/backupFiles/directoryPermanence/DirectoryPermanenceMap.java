package org.fl.backupFiles.directoryPermanence;

import java.nio.file.Path;
import java.util.Set;
import java.util.TreeMap;

public class DirectoryPermanenceMap implements DirectoryPermanence {

	private final TreeMap<Path,DirectoryPermanenceLevel> permanenceMap ;
	private final Set<Path> pathKeys ;
	
	public DirectoryPermanenceMap(String config) {
		super();
		permanenceMap = new TreeMap<Path,DirectoryPermanenceLevel>(new PermanencePathComparator()) ;
		pathKeys = permanenceMap.keySet() ;
	}

	@Override
	public DirectoryPermanenceLevel getPermanenceLevel(Path dir) {

		for (Path pathKey : pathKeys) {
			if (dir.startsWith(pathKey)) {
				return permanenceMap.get(pathKey) ;
			}
		}
		return DEFAULT_PERMANENCE_LEVEL;
	}

}
