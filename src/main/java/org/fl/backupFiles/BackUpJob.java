package org.fl.backupFiles;

import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class BackUpJob {

	private String title ;
	private final Logger bLog ;
	
	public enum JobTaskType { 
		SOURCE_TO_BUFFER("Source vers buffer"), BUFFER_TO_TARGET("Buffer vers target") ;
		private String jobName ;
		private JobTaskType(String name) { jobName = name ; }
		@Override
        public String toString() { return jobName ; } 
	} ;
	
	private Map<JobTaskType, List<BackUpTask>> backUpTasks ;
	
	private final static String TITLE  = "titre"  ;
	private final static String ITEMS  = "items"  ;
	private final static String SOURCE = "source" ;
	private final static String TARGET = "target" ;
	private final static String BUFFER = "buffer" ;
	private final static String PARALLEL_SCAN = "parallelScan";
	
	// A back up jobs is defined by a Json object (passed in parameter of this constructor)
	// It is basically 2 lists of back up tasks :
	//  - a list of back up task from source directories to buffer directories
	//  - a list of back up task from buffer directories to target directories
	// A back up task is a source directory to back up and a destination diectory to back up
	public BackUpJob(String jsonConfig, Logger l) {
		
		bLog = l ;
		backUpTasks = new HashMap<JobTaskType, List<BackUpTask>>() ;
		if (jsonConfig != null) {
			
			try {
				JsonElement jElemConf = JsonParser.parseString(jsonConfig) ;
				
				if ((jElemConf != null) && (jElemConf.isJsonObject())) {
					
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

		for (JobTaskType jtt : JobTaskType.values()) {
			ArrayList<BackUpTask> tasksForJtt = new ArrayList<BackUpTask>() ;
			backUpTasks.put(jtt, tasksForJtt) ;
		}

		for (JsonElement jItem : jItems) {

			JsonObject jObjItem = jItem.getAsJsonObject() ;

			Path srcPath = getPathElement(jObjItem, SOURCE) ;
			Path tgtPath = getPathElement(jObjItem, TARGET) ;
			Path bufPath = getPathElement(jObjItem, BUFFER) ;

			boolean scanInParallel = getParallelScanElement(jObjItem, PARALLEL_SCAN) ;

			if (scanInParallel) {
				addParallelBackUpTasks(srcPath, bufPath, tgtPath) ;
			} else {
				addBackUpTask(srcPath, bufPath, tgtPath) ;
			}
		}
	}
	
	private void addParallelBackUpTasks(Path srcPath, Path bufPath, Path tgtPath) {
		
		 try (DirectoryStream<Path> sourceFileStream = Files.newDirectoryStream(srcPath)) {
			 
			 for (Path sourceSubPath : sourceFileStream) {	
				 
				 Path bufSubPath = bufPath.resolve(srcPath.relativize(sourceSubPath)) ;
				 Path tgtSubPath = tgtPath.resolve(srcPath.relativize(sourceSubPath)) ;
				 addBackUpTask(sourceSubPath, bufSubPath, tgtSubPath) ;
				 
			 }
		 } catch (Exception e) {
			bLog.log(Level.SEVERE, "Exception when scanning directory to create parallel scan " + srcPath, e);
		}
	}
	
	private void addBackUpTask(Path srcPath, Path bufPath, Path tgtPath) {
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
	
	public String toString() {
		return title ;
	}

	public List<BackUpTask> getTasks(JobTaskType jobTaskType) {
		
		if (backUpTasks.get(jobTaskType) == null) {
			return null ;
		} else {
			return Collections.unmodifiableList(backUpTasks.get(jobTaskType)) ;
		}
	}
	
	private Path getPathElement(JsonObject jObjItem, String prop) {
		
		Path returnPath = null ;
		JsonElement elem = jObjItem.get(prop) ;
		if (elem != null) {
			returnPath = Paths.get(URI.create(elem.getAsString())) ;
		}
		return returnPath ;
	}
	
	private boolean getParallelScanElement(JsonObject jObjItem, String prop) {
		
		boolean scanInParallel = false ;
		JsonElement elem = jObjItem.get(prop) ;
		if (elem != null) {
			scanInParallel = elem.getAsBoolean();
		}
		return scanInParallel ;
	}
}
