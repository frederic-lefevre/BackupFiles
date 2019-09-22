package org.fl.backupFiles.gui.workers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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
	
	private final static String TESTDATA_DIR = "file:///C:/ForTests/BackUpFiles/TestDataForMultiThread/Concert" ;
	
	private final static int NB_DIR_TO_GENERATE = 3 ;
	
	private final static String CONFIG_FILE_NAME = "config.json" ;
	
	public TestDataManager(Path config, Logger l) {

		bLog = l ;
		configFilesDir = config ;
	}

	public boolean generateTestData() {

//		Path testDataDir = Paths.get(new URI(TESTDATA_DIR)) ;
		
		JsonObject confJson = new JsonObject() ;
		
		confJson.addProperty(TITLE, "Test multi thread");
		
		JsonArray items = new JsonArray() ;
		
		for (int i=0; i < NB_DIR_TO_GENERATE; i++) {
			
			String dirName = "dir" + i ;
			
			// Copy test data to source and buffer
						
			// update config
			JsonObject backUpTask = new JsonObject() ;
			backUpTask.addProperty(SOURCE, SOURCE_BASE_DIR + dirName) ;
			backUpTask.addProperty(TARGET, TARGET_BASE_DIR + dirName) ;
			backUpTask.addProperty(BUFFER, BUFFER_BASE_DIR + dirName) ;
			
			items.add(backUpTask) ;
		}
		
		confJson.add(ITEMS, items);
		
		// write config file
		Path cfFilePath = configFilesDir.resolve(CONFIG_FILE_NAME) ;
		String confToWrite = JsonUtils.jsonPrettyPrint(confJson) ;
		try {
			Files.write(cfFilePath, confToWrite.getBytes(StandardCharsets.UTF_8)) ;
			return true ;
		} catch (IOException e) {
			bLog.log(Level.SEVERE, "Exception writing config file", e) ;
			return false ;
		}
				
	}
}
