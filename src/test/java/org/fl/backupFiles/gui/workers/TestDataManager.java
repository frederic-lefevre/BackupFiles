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

package org.fl.backupFiles.gui.workers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.fl.util.file.FilesUtils;
import org.fl.util.json.JsonUtils;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class TestDataManager {

	private final Logger bLog;
	private final Path configFilesDir;

	private final static String TITLE = "titre";
	private final static String ITEMS = "items";
	private final static String SOURCE = "source";
	private final static String TARGET = "target";
	private final static String BUFFER = "buffer";

	private final static String SOURCE_BASE_DIR = "file:///C:/ForTests/BackUpFiles/FP_Test_Source2/";
	private final static String TARGET_BASE_DIR = "file:///C:/ForTests/BackUpFiles/FP_Test_Target2/";
	private final static String BUFFER_BASE_DIR = "file:///C:/ForTests/BackUpFiles/FP_Test_Buffer2/";

	private final static String TESTDATA_DIR = "file:///C:/ForTests/BackUpFiles/TestDataForMultiThread";

	private final static String CONFIG_FILE_NAME = "config.json";
	
	public TestDataManager(Path config, Logger l) {

		bLog = l;
		configFilesDir = config;
	}

	public boolean generateTestData(int nbDirToGenerate) {

		try {
			Path testDataDir = Paths.get(new URI(TESTDATA_DIR));

			ObjectNode confJson = JsonNodeFactory.instance.objectNode();

			confJson.put(TITLE, "Test multi thread");

			ArrayNode items = JsonNodeFactory.instance.arrayNode();

			for (int i = 0; i < nbDirToGenerate; i++) {

				String dirName = "dir" + i;
				String srcUri = SOURCE_BASE_DIR + dirName;
				String tgtUri = TARGET_BASE_DIR + dirName;
				String bufUri = BUFFER_BASE_DIR + dirName;

				// update config
				ObjectNode backUpTask = JsonNodeFactory.instance.objectNode();
				backUpTask.put(SOURCE, srcUri);
				backUpTask.put(TARGET, tgtUri);
				backUpTask.put(BUFFER, bufUri);

				items.add(backUpTask);

				// Copy test data to source and buffer
				Path srcPath = Paths.get(new URI(srcUri));
				Path bufPath = Paths.get(new URI(bufUri));
				boolean b1 = FilesUtils.copyDirectoryTree(testDataDir, srcPath, bLog);
				boolean b2 = FilesUtils.copyDirectoryTree(testDataDir, bufPath, bLog);
				if (!(b1 && b2)) {
					return false;
				}
			}

			confJson.set(ITEMS, items);

			// write config file
			Path cfFilePath = configFilesDir.resolve(CONFIG_FILE_NAME);
			String confToWrite = JsonUtils.jsonPrettyPrint(confJson);

			Files.write(cfFilePath, confToWrite.getBytes(StandardCharsets.UTF_8));
			return true;
		} catch (URISyntaxException e) {
			bLog.log(Level.SEVERE, "URI exception for " + TESTDATA_DIR, e);
			return false;
		} catch (Exception e) {
			bLog.log(Level.SEVERE, "Exception writing config and testdata files", e);
			return false;
		}				
	}
	
	public boolean deleteTestData() {

		try {

			Path cfFilePath = configFilesDir.resolve(CONFIG_FILE_NAME);
			Files.delete(cfFilePath);

			return (deteleOneDirContent(SOURCE_BASE_DIR) && deteleOneDirContent(BUFFER_BASE_DIR)
					&& deteleOneDirContent(TARGET_BASE_DIR));

		} catch (Exception e) {
			bLog.log(Level.SEVERE, "Exception deleting config and testdata files", e);
			return false;
		}

	}
	
	private boolean deteleOneDirContent(String uri) throws IOException, URISyntaxException {
		return Files.list(Paths.get(new URI(uri)))
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
