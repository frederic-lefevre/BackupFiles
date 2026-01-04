/*
 * MIT License

Copyright (c) 2017, 2026 Frederic Lefevre

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

package org.fl.backupFiles.gui.workers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.fl.backupFiles.directoryGroup.DirectoryPermanenceLevel;
import org.fl.backupFiles.directoryGroup.GroupPolicy;
import org.fl.util.file.FilesUtils;
import org.fl.util.json.JsonUtils;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class TestDataManager {

	private final Logger bLog;
	private final Path configFilesDir;
	private final Path directoryGroupFileDir;
	private long nbHighPermanenceGenerated;
	private long nbMediumPermanenceGenerated;
	private long nbLowPermanenceGenerated;

	// Json property name for config
	private static final String TITLE = "titre";
	private static final String ITEMS = "items";
	private static final String SOURCE = "source";
	private static final String TARGET = "target";
	private static final String BUFFER = "buffer";

	// Json property names for directoryGroup
	private static final String PATH = "path";
	private static final String PERMANENCE = "permanence";
	private static final String GROUP_POLICY = "groupPolicy";
		
	private static final String SOURCE_BASE_DIR = "/ForTests/BackUpFiles/FP_Test_Source2/";
	private static final String SUB_DIRECTORY_FOR_GROUP = "/Concert/";
	
	private static final String SOURCE_BASE_URI = "file:///ForTests/BackUpFiles/FP_Test_Source2/";
	private static final String TARGET_BASE_URI = "file:///ForTests/BackUpFiles/FP_Test_Target2/";
	private static final String BUFFER_BASE_URI = "file:///ForTests/BackUpFiles/FP_Test_Buffer2/";

	private static final String TESTDATA_URI = "file:///ForTests/BackUpFiles/TestDataForMultiThread";

	protected static final String CONFIG_FILE_FOLDER_URI = "file:///ForTests/BackUpFiles/configTest2/";
	protected static final String DIRECTORY_GROUP_FOLDER_URI = "file:///ForTests/BackUpFiles/directoryGroup2/";
	private static final String CONFIG_FILE_NAME = "config.json";
	private static final String DIRECTORY_GROUP_FILE_NAME = "directoryGroup.json";
	
	public TestDataManager(Logger l) throws URISyntaxException {

		bLog = l;
		configFilesDir = FilesUtils.uriStringToAbsolutePath(CONFIG_FILE_FOLDER_URI);
		directoryGroupFileDir = FilesUtils.uriStringToAbsolutePath(DIRECTORY_GROUP_FOLDER_URI);
	}

	public boolean generateTestData(int nbDirToGenerate) {

		nbHighPermanenceGenerated = 0;
		nbMediumPermanenceGenerated = 0;
		nbLowPermanenceGenerated = 0;
		try {
			Path testDataDir = FilesUtils.uriStringToAbsolutePath(TESTDATA_URI);

			ObjectNode confJson = JsonNodeFactory.instance.objectNode();
			ArrayNode groupJson = JsonNodeFactory.instance.arrayNode();

			confJson.put(TITLE, "Test multi thread");

			ArrayNode items = JsonNodeFactory.instance.arrayNode();

			for (int i = 0; i < nbDirToGenerate; i++) {

				String dirName = "dir" + i;
				String srcUri = SOURCE_BASE_URI + dirName;
				String tgtUri = TARGET_BASE_URI + dirName;
				String bufUri = BUFFER_BASE_URI + dirName;

				// update config
				ObjectNode backUpTask = JsonNodeFactory.instance.objectNode();
				backUpTask.put(SOURCE, srcUri);
				backUpTask.put(TARGET, tgtUri);
				backUpTask.put(BUFFER, bufUri);

				items.add(backUpTask);

				// update group policy
				String permanence;
				String groupPolicy;
				if (i % 3 == 0) {
					permanence = DirectoryPermanenceLevel.HIGH.name();
					groupPolicy = GroupPolicy.DO_NOT_GROUP.name();
					nbHighPermanenceGenerated++;
				} else if (i % 3 == 2) {
					permanence = DirectoryPermanenceLevel.MEDIUM.name();
					groupPolicy = GroupPolicy.GROUP_SUB_ITEMS.name();
					nbMediumPermanenceGenerated++;
				} else {
					permanence = DirectoryPermanenceLevel.LOW.name();
					groupPolicy = GroupPolicy.GROUP_ALL.name();
					nbLowPermanenceGenerated++;
				}
				ObjectNode oneDirectoryGroup = JsonNodeFactory.instance.objectNode();
				oneDirectoryGroup.put(PATH, SOURCE_BASE_DIR + dirName + SUB_DIRECTORY_FOR_GROUP);
				oneDirectoryGroup.put(PERMANENCE, permanence);
				oneDirectoryGroup.put(GROUP_POLICY, groupPolicy);
				
				groupJson.add(oneDirectoryGroup);
				
				// Copy test data to source and buffer
				Path srcPath = FilesUtils.uriStringToAbsolutePath(srcUri);
				Path bufPath = FilesUtils.uriStringToAbsolutePath(bufUri);
				boolean b1 = FilesUtils.copyDirectoryTree(testDataDir, srcPath, bLog);
				boolean b2 = FilesUtils.copyDirectoryTree(testDataDir, bufPath, bLog);
				if (!(b1 && b2)) {
					return false;
				}
				Path tgtPath = FilesUtils.uriStringToAbsolutePath(tgtUri + SUB_DIRECTORY_FOR_GROUP);
				Files.createDirectories(tgtPath);
			}

			confJson.set(ITEMS, items);

			// write config and directory group files
			Path cfFilePath = configFilesDir.resolve(CONFIG_FILE_NAME);
			Path directoryGroupPath = directoryGroupFileDir.resolve(DIRECTORY_GROUP_FILE_NAME);
			String confToWrite = JsonUtils.jsonPrettyPrint(confJson);
			String directoryGroupToWrite = JsonUtils.jsonPrettyPrint(groupJson);

			Files.write(cfFilePath, confToWrite.getBytes(StandardCharsets.UTF_8));
			Files.write(directoryGroupPath, directoryGroupToWrite.getBytes(StandardCharsets.UTF_8));
			
			return true;
		} catch (URISyntaxException e) {
			bLog.log(Level.SEVERE, "URI exception for " + TESTDATA_URI, e);
			return false;
		} catch (Exception e) {
			bLog.log(Level.SEVERE, "Exception writing config and testdata files", e);
			return false;
		}				
	}
	
	
	public boolean deleteTestData() {

		try {

			Path cfFilePath = configFilesDir.resolve(CONFIG_FILE_NAME);
			Path directoryGroupPath = directoryGroupFileDir.resolve(DIRECTORY_GROUP_FILE_NAME);
			Files.delete(cfFilePath);
			Files.delete(directoryGroupPath);

			return (deteleOneDirContent(SOURCE_BASE_URI) && deteleOneDirContent(BUFFER_BASE_URI)
					&& deteleOneDirContent(TARGET_BASE_URI));

		} catch (Exception e) {
			bLog.log(Level.SEVERE, "Exception deleting config and testdata files", e);
			return false;
		}

	}
	
	public long getNbHighPermanenceGenerated() {
		return nbHighPermanenceGenerated;
	}

	public long getNbMediumPermanenceGenerated() {
		return nbMediumPermanenceGenerated;
	}

	public long getNbLowPermanenceGenerated() {
		return nbLowPermanenceGenerated;
	}

	private boolean deteleOneDirContent(String uri) throws IOException, URISyntaxException {
		return Files.list(FilesUtils.uriStringToAbsolutePath(uri))
				 .map(path -> {
					try {
						return FilesUtils.deleteDirectoryTree(path, true, bLog);
					} catch (IOException e) {
						bLog.log(Level.SEVERE, "Exception deleting config and testdata files", e) ;
						return false ;
					}
				})
				.allMatch(res -> res); 
	}
}
