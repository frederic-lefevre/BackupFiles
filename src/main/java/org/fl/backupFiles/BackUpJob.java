package org.fl.backupFiles;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class BackUpJob {

	private String title ;
	private Logger bLog ;
	
	public enum JobTaskType { 
		SOURCE_TO_BUFFER("Source vers buffer"), BUFFER_TO_TARGET("Buffer vers target") ;
		private String jobName ;
		private JobTaskType(String name) { jobName = name ; }
		@Override
        public String toString() { return jobName ; } 
	} ;
	
	private HashMap<JobTaskType, ArrayList<BackUpTask>> backUpTasks ;
	
	private final static String TITLE  = "titre"  ;
	private final static String ITEMS  = "items"  ;
	private final static String SOURCE = "source" ;
	private final static String TARGET = "target" ;
	private final static String BUFFER = "buffer" ;
	
	// A back up jobs is defined by a Json object (passed in parameter of this constructor)
	// It is basically 2 lists of back up tasks :
	//  - a list of back up task from source diectories to buffer directories
	//  - a list of back up task from buffer diectories to target directories
	// A back up task is a source directory to back up and a destination diectory to back up
	public BackUpJob(String jsonConfig, Logger l) {
		
		bLog = l ;
		if (jsonConfig != null) {
			
			try {
				JsonElement jElemConf = new JsonParser().parse(jsonConfig) ;
				
				if (jElemConf != null) {
					
					JsonObject jsonObjectConf = jElemConf.getAsJsonObject() ;
					
					JsonElement jElem = jsonObjectConf.get(TITLE) ;
					if (jElem != null) {
						title = jElem.getAsString() ;
					} else {
						bLog.severe("No title found inJSON configuration: " + jsonConfig );
					}
					
					jElem = jsonObjectConf.get(ITEMS) ;
					if (jElem != null) {
						
						getBackUpTasks(jElem.getAsJsonArray());

					} else {
						bLog.severe("No items found inJSON configuration: " + jsonConfig );
					}
				}
			} catch (JsonSyntaxException e) {
				bLog.log(Level.SEVERE, "Invalid JSON configuration: " + jsonConfig, e) ;
			} catch (Exception e) {
				bLog.log(Level.SEVERE, "Exception when creating JSON configuration: " + jsonConfig, e) ;
			}
		}
	}

	private void getBackUpTasks(JsonArray jItems) {
		
		backUpTasks = new HashMap<JobTaskType, ArrayList<BackUpTask>>() ;
		for (JobTaskType jtt : JobTaskType.values()) {
			ArrayList<BackUpTask> tasksForJtt = new ArrayList<BackUpTask>() ;
			backUpTasks.put(jtt, tasksForJtt) ;
		}

		for (JsonElement jItem : jItems) {
			
			JsonObject jObjItem = jItem.getAsJsonObject() ;
										
			Path srcPath = getPathElement(jObjItem, SOURCE) ;
			Path tgtPath = getPathElement(jObjItem, TARGET) ;
			Path bufPath = getPathElement(jObjItem, BUFFER) ;

			if ((srcPath != null) && (bufPath != null)) {
				if (Files.isRegularFile(srcPath)) {
					Path bufFile = bufPath.resolve(srcPath.getFileName()) ;
					 backUpTasks.get(JobTaskType.SOURCE_TO_BUFFER).add(new BackUpTask(srcPath, bufFile, bLog)) ;
				} else {
					 backUpTasks.get(JobTaskType.SOURCE_TO_BUFFER).add(new BackUpTask(srcPath, bufPath, bLog)) ;
				}
			} else {
				bLog.warning("No source / buffer element definition for back up job " + title);
			}
			if ((tgtPath != null) && (bufPath != null)) {
				 backUpTasks.get(JobTaskType.BUFFER_TO_TARGET).add(new BackUpTask(bufPath, tgtPath, bLog)) ;
			}
		}
	}
	
	public String toString() {
		return title ;
	}

	public ArrayList<BackUpTask> getTasks(JobTaskType jobTaskType) {
		
		return backUpTasks.get(jobTaskType) ;
	}
	
	private Path getPathElement(JsonObject jObjItem, String prop) {
		
		Path returnPath = null ;
		JsonElement elem = jObjItem.get(prop) ;
		if (elem != null) {
			returnPath = Paths.get(URI.create(elem.getAsString())) ;
		}
		return returnPath ;
	}
}
