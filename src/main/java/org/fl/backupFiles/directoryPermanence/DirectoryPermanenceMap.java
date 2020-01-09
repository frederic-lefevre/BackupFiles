package org.fl.backupFiles.directoryPermanence;

import java.nio.file.Path;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class DirectoryPermanenceMap implements DirectoryPermanence {

	private final TreeMap<Path,DirectoryPermanenceLevel> permanenceMap ;
	private final Set<Path> pathKeys ;
	
	public DirectoryPermanenceMap(String jsonConfig, Logger bLog) {
		super();
		permanenceMap = new TreeMap<Path,DirectoryPermanenceLevel>(new PermanencePathComparator()) ;
		
		if (jsonConfig != null) {
			
			try {
				JsonElement jElemConf = new JsonParser().parse(jsonConfig) ;
		
			} catch (JsonSyntaxException e) {
				bLog.log(Level.SEVERE, "Invalid JSON configuration: " + jsonConfig, e) ;
			} catch (Exception e) {
				bLog.log(Level.SEVERE, "Exception when creating JSON configuration: " + jsonConfig, e) ;
			}
		}
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
