package org.fl.backupFiles.directoryPermanence;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class DirectoryPermanenceMap implements DirectoryPermanence {

	private static final String PATH 	   = "path" ;
	private static final String PERMANENCE = "permanence" ;
	
	private final TreeMap<Path,DirectoryPermanenceLevel> permanenceMap ;
	private final Set<Path> pathKeys ;
	
	public DirectoryPermanenceMap(String jsonConfig, Logger bLog) {
		super();
		permanenceMap = new TreeMap<Path,DirectoryPermanenceLevel>(new PermanencePathComparator()) ;
		
		if (jsonConfig != null) {
			
			try {
				JsonArray jPaths = new JsonParser().parse(jsonConfig).getAsJsonArray() ;
				for (JsonElement jElem : jPaths) {
					JsonObject jPathPermanence = jElem.getAsJsonObject() ;
					
					Path sPath = Paths.get(jPathPermanence.get(PATH).getAsString()) ;
					DirectoryPermanenceLevel sPermanence = DirectoryPermanenceLevel.valueOf(jPathPermanence.get(PERMANENCE).getAsString()) ;
					permanenceMap.put(sPath, sPermanence) ;
				}
		
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
			if (intersectWith(dir, pathKey)) {
				return permanenceMap.get(pathKey) ;
			}
		}
		return DEFAULT_PERMANENCE_LEVEL;
	}

	// nativePath maybe a windows path. 
	// stdPath has "/" as separator
	private boolean intersectWith(Path nativePath, Path stdPath) {
		return nativePath.toString().contains(stdPath.toString()) ;
	}
}
