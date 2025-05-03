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

package org.fl.backupFiles.directoryGroup;

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

public class DirectoryGroupMap {

	private static final Logger bLog = Logger.getLogger(DirectoryGroupMap.class.getName());
	
	private static final ObjectMapper mapper = new ObjectMapper();
	
	public final static DirectoryPermanenceLevel DEFAULT_PERMANENCE_LEVEL = DirectoryPermanenceLevel.HIGH;
	public static final GroupPolicy DEFAULT_GROUP_POLICY = GroupPolicy.DO_NOT_GROUP;
	
	private static final String PATH = "path";
	private static final String PERMANENCE = "permanence";
	private static final String GROUP_POLICY = "groupPolicy";

	private final Map<Path, DirectoryGroup> directoryGroupMap;
	private final Set<Path> pathKeys;
	private final DirectoryGroup defaultDirectoryGroup;

	public DirectoryGroupMap(String jsonConfig) {
		super();
		defaultDirectoryGroup = new DirectoryGroup(Path.of("/"), DEFAULT_PERMANENCE_LEVEL, DEFAULT_GROUP_POLICY);
		directoryGroupMap = new TreeMap<Path, DirectoryGroup>(new DeeperPathComparator());

		if (jsonConfig != null) {

			try {
				JsonNode jPathsNode = mapper.readTree(jsonConfig);
				if ((jPathsNode != null) && (jPathsNode.isArray())) {

					for (JsonNode jPathPermanence : jPathsNode) {

						Path sPath = Paths.get(jPathPermanence.get(PATH).asText());
						DirectoryPermanenceLevel permanenceLevel = DirectoryPermanenceLevel.valueOf(jPathPermanence.get(PERMANENCE).asText());
						GroupPolicy groupPolicy = GroupPolicy.valueOf(jPathPermanence.get(GROUP_POLICY).asText());
						directoryGroupMap.put(sPath, new DirectoryGroup(sPath, permanenceLevel, groupPolicy));
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
		pathKeys = directoryGroupMap.keySet();
	}

	public DirectoryGroup getDirectoryGroup(Path dir) {

		for (Path pathKey : pathKeys) {
			if (intersectWith(dir, pathKey)) {
				return directoryGroupMap.get(pathKey) ;
			}
		}
		return defaultDirectoryGroup;
	}

	// nativePath maybe a windows path. 
	// stdPath has "/" as separator
	private boolean intersectWith(Path nativePath, Path stdPath) {
		return nativePath.toString().contains(stdPath.toString()) ;
	}
}
