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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ibm.lge.fl.util.file.FilesUtils;
import com.ibm.lge.fl.util.json.JsonUtils;

public class TestDataManager {

	private final Logger bLog ;
	private final Path   configFilesDir ;
	
	private final static String TITLE  = "titre"  ;
	private final static String ITEMS  = "items"  ;
	private final static String SOURCE = "source" ;
	private final static String TARGET = "target" ;
	private final static String BUFFER = "buffer" ;
	
	private final static String SOURCE_BASE_DIR = "file:///C:/ForTests/BackUpFiles/FP_Test_Source2/" ;
	private final static String TARGET_BASE_DIR = "file:///C:/ForTests/BackUpFiles/FP_Test_Target2/" ;
	private final static String BUFFER_BASE_DIR = "file:///C:/ForTests/BackUpFiles/FP_Test_Buffer2/" ;
	
	private final static String TESTDATA_DIR = "file:///C:/ForTests/BackUpFiles/TestDataForMultiThread" ;
	
	private final static String CONFIG_FILE_NAME = "config.json" ;
	
	public TestDataManager(Path config, Logger l) {

		bLog = l ;
		configFilesDir = config ;
	}

	public boolean generateTestData(int nbDirToGenerate) {

		try {
			Path testDataDir = Paths.get(new URI(TESTDATA_DIR)) ;

			JsonObject confJson = new JsonObject() ;

			confJson.addProperty(TITLE, "Test multi thread");

			JsonArray items = new JsonArray() ;


			for (int i=0; i < nbDirToGenerate; i++) {

				String dirName = "dir" + i ;
				String srcUri = SOURCE_BASE_DIR + dirName ;
				String tgtUri = TARGET_BASE_DIR + dirName ;
				String bufUri = BUFFER_BASE_DIR + dirName ;

				// update config
				JsonObject backUpTask = new JsonObject() ;
				backUpTask.addProperty(SOURCE, srcUri) ;
				backUpTask.addProperty(TARGET, tgtUri) ;
				backUpTask.addProperty(BUFFER, bufUri) ;

				items.add(backUpTask) ;

				// Copy test data to source and buffer
				Path srcPath = Paths.get(new URI(srcUri)) ;
				Path bufPath = Paths.get(new URI(bufUri)) ;
				boolean b1 = FilesUtils.copyDirectoryTree(testDataDir, srcPath, bLog) ;
				boolean b2 = FilesUtils.copyDirectoryTree(testDataDir, bufPath, bLog) ;
				if (! (b1 && b2)) {
					return false ;
				}
			}

			confJson.add(ITEMS, items);

			// write config file
			Path cfFilePath = configFilesDir.resolve(CONFIG_FILE_NAME) ;
			String confToWrite = JsonUtils.jsonPrettyPrint(confJson) ;

			Files.write(cfFilePath, confToWrite.getBytes(StandardCharsets.UTF_8)) ;
			return true ;
		}  catch (URISyntaxException e) {
			bLog.log(Level.SEVERE, "URI exception for " + TESTDATA_DIR, e) ;
			return false ;
		} catch (Exception e) {
			bLog.log(Level.SEVERE, "Exception writing config file", e) ;
			return false ;
		}				
	}
	
	public boolean deleteTestData() {
		return true ;
	}
}
