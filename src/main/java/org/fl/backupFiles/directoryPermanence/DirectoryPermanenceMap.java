/*
 * MIT License

Copyright (c) 2017, 2025 Frederic Lefevre

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package org.fl.backupFiles.directoryPermanence;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


public class DirectoryPermanenceMap implements DirectoryPermanence {

	private static final Logger bLog = Logger.getLogger(DirectoryPermanenceMap.class.getName());
	
	private static final ObjectMapper mapper = new ObjectMapper();
	
	private static final String PATH = "path";
	private static final String PERMANENCE = "permanence";

	private final Map<Path, DirectoryPermanenceLevel> permanenceMap;
	private final Set<Path> pathKeys;

	public DirectoryPermanenceMap(String jsonConfig) {
		super();
		permanenceMap = new TreeMap<Path, DirectoryPermanenceLevel>(new DeeperPathComparator());

		if (jsonConfig != null) {

			try {
				JsonNode jPathsNode = mapper.readTree(jsonConfig);
				if ((jPathsNode != null) && (jPathsNode.isArray())) {

					for (JsonNode jPathPermanence : jPathsNode) {

						Path sPath = Paths.get(jPathPermanence.get(PATH).asText());
						DirectoryPermanenceLevel sPermanence = DirectoryPermanenceLevel.valueOf(jPathPermanence.get(PERMANENCE).asText());
						permanenceMap.put(sPath, sPermanence);
					}
				} else {
					bLog.severe("Json null or not an array:\n" + jsonConfig);
				}

			} catch (JsonProcessingException e) {
				bLog.log(Level.SEVERE, "Invalid JSON configuration: " + jsonConfig, e);
			} catch (Exception e) {
				bLog.log(Level.SEVERE, "Exception when creating JSON configuration: " + jsonConfig, e);
			}
		}
		pathKeys = permanenceMap.keySet();
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
