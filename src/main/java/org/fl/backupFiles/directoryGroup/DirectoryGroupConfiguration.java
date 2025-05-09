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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.fl.backupFiles.directoryGroup.core.DirectoryGroup;
import org.fl.backupFiles.directoryGroup.core.DirectoryGroupBuilder;
import org.fl.util.file.FilesUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DirectoryGroupConfiguration {

	private static final Logger bLog = Logger.getLogger(DirectoryGroupConfiguration.class.getName());
	
	private static final ObjectMapper mapper = new ObjectMapper();
	
	private static final String PATH = "path";
	private static final String PERMANENCE = "permanence";
	private static final String GROUP_POLICY = "groupPolicy";
	private static final String URI_FILE_SCHEME = "file://";
	
	private final List<DirectoryGroup> directoryGroupList;
	
	public DirectoryGroupConfiguration(String jsonConfig) {
		
		List<DirectoryGroup> directoryGroupList = new ArrayList<>();
		
		if ((jsonConfig != null) && !jsonConfig.isEmpty()) {

			try {
				JsonNode jPathsNode = mapper.readTree(jsonConfig);
				if ((jPathsNode != null) && (jPathsNode.isArray())) {

					for (JsonNode jPathPermanence : jPathsNode) {

						Path sPath = FilesUtils.uriStringToAbsolutePath(URI_FILE_SCHEME + jPathPermanence.get(PATH).asText());
						DirectoryPermanenceLevel permanenceLevel = DirectoryPermanenceLevel.valueOf(jPathPermanence.get(PERMANENCE).asText());
						GroupPolicy groupPolicy = GroupPolicy.valueOf(jPathPermanence.get(GROUP_POLICY).asText());
						
						directoryGroupList.add(DirectoryGroupBuilder.build(sPath, permanenceLevel, groupPolicy));
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
		this.directoryGroupList = Collections.unmodifiableList(directoryGroupList);
	}

	public List<DirectoryGroup> getDirectoryGroupList() {
		return directoryGroupList;
	}
}
