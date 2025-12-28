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

package org.fl.backupFiles;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.fl.backupFiles.directoryGroup.DirectoryGroupConfiguration;
import org.fl.backupFiles.directoryGroup.DirectoryGroupMap;
import org.fl.util.file.FilesUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class BackUpJob {

	private static final Logger bLog = Logger.getLogger(BackUpJob.class.getName());
	
	private static final ObjectMapper mapper = new ObjectMapper();
	
	private static long defaultWarningSizeLimit = 10_000_000L;
	
	private String title;
	
	private final List<FullBackUpTask> fullBackUpTaskList;
	private final DirectoryGroupConfiguration directoryGroupConfiguration;
	
	public enum JobTaskType { 
		SOURCE_TO_BUFFER("Source vers tampon"), 
		BUFFER_TO_TARGET("Tampon vers cible"),
		SOURCE_TO_TARGET("Source vers cible");
		
		private final String jobName;
		
		private JobTaskType(String name) { 
			jobName = name; 
		}
		
		@Override
        public String toString() { 
			return jobName; 
		} 
	} ;
	
	private Map<JobTaskType, List<BackUpTask>> backUpTasks;

	private static final String TITLE = "titre";
	private static final String ITEMS = "items";
	private static final String SOURCE = "source";
	private static final String TARGET = "target";
	private static final String BUFFER = "buffer";
	private static final String PARALLEL_SCAN = "parallelScan";
	private static final String SIZE_WARNING_LIMIT = "sizeWarningLimit";
	
	// A back up jobs is defined by a JSON object (passed in parameter of this constructor)
	// It is basically either 2 lists of back up tasks :
	//  - a list of back up task from source directories to buffer directories
	//  - a list of back up task from buffer directories to target directories
	// or a single list of back up task from source directories to target directories
	// A back up task is a source directory to back up and a destination directory to back up
	public BackUpJob(String jsonConfig, DirectoryGroupConfiguration directoryGroupConfiguration) {

		fullBackUpTaskList = new ArrayList<FullBackUpTask>();
		this.directoryGroupConfiguration = directoryGroupConfiguration;
		
		initBackUpTasksMap();

		if ((jsonConfig != null) && !jsonConfig.isEmpty()) {

			try {
				JsonNode jsonObjectConf = mapper.readTree(jsonConfig);

				JsonNode jTitle = jsonObjectConf.get(TITLE);
				if (jTitle != null) {
					title = jTitle.asText();
				} else {
					bLog.severe("No title found in JSON configuration: " + jsonConfig);
				}

				JsonNode jBackupItems = jsonObjectConf.get(ITEMS);
				if (jBackupItems == null) {
					bLog.severe("No items found in JSON configuration: " + jsonConfig);					
				} else if (jBackupItems.isArray()){
					getBackUpTasks((ArrayNode) jBackupItems);
				} else {
					bLog.severe("The items property in JSON configuration should be an array: " + jsonConfig);					
				}

			} catch (JsonProcessingException e) {
				bLog.log(Level.SEVERE, "Invalid JSON configuration: " + jsonConfig, e);
			} catch (Exception e) {
				bLog.log(Level.SEVERE, "Exception when creating JSON configuration: " + jsonConfig, e);
			}
		} else {
			bLog.severe("JSON configuration null or empty");
		}
	}

	private void initBackUpTasksMap() {
		
		backUpTasks = new HashMap<JobTaskType, List<BackUpTask>>();
		for (JobTaskType jtt : JobTaskType.values()) {
			backUpTasks.put(jtt, new ArrayList<BackUpTask>());
		}
	}
	
	private class FullBackUpTask {
		
		private final Path srcPath;
		private final Path bufPath;
		private final Path tgtPath;
		private final boolean scanInParallel;
		private final long sizeWarningLimit;

		public FullBackUpTask(Path srcPath, Path bufPath, Path tgtPath, boolean scanInParallel, long sizeWarningLimit) {
			super();
			this.srcPath = srcPath;
			this.bufPath = bufPath;
			this.tgtPath = tgtPath;
			this.scanInParallel = scanInParallel;
			this.sizeWarningLimit = sizeWarningLimit;
		}
		
		public Path getSrcPath() {
			return srcPath;
		}

		public Path getBufPath() {
			return bufPath;
		}

		public Path getTgtPath() {
			return tgtPath;
		}
		
		public boolean isScanInParallel() {
			return scanInParallel;
		}

		public long getSizeWarningLimit() {
			return sizeWarningLimit;
		}
	}
	
	private void getBackUpTasks(ArrayNode jItems) throws URISyntaxException {

		for (JsonNode jObjItem : jItems) {

			Path srcPath = getPathElement(jObjItem, SOURCE);
			Path tgtPath = getPathElement(jObjItem, TARGET);
			Path bufPath = getPathElement(jObjItem, BUFFER);

			boolean scanInParallel = getParallelScanElement(jObjItem, PARALLEL_SCAN);
			long sizeWarningLimit = getSizeWarningLimit(jObjItem, SIZE_WARNING_LIMIT);

			fullBackUpTaskList.add(new FullBackUpTask(srcPath, bufPath, tgtPath, scanInParallel, sizeWarningLimit));
		}
	}
	
	private void addParallelBackUpTasks(Path srcPath, Path bufPath, Path tgtPath, long sizeWarningLimit) {

		Set<Path> srcFilenameSet = new HashSet<Path>();
		Set<Path> bufFilenameSet = new HashSet<Path>();

		try (DirectoryStream<Path> sourceFileStream = Files.newDirectoryStream(srcPath)) {

			for (Path sourceSubPath : sourceFileStream) {

				Path bufSubPath = bufPath.resolve(srcPath.relativize(sourceSubPath));
				Path tgtSubPath = tgtPath.resolve(srcPath.relativize(sourceSubPath));
				addBackUpTask(sourceSubPath, bufSubPath, tgtSubPath, sizeWarningLimit);
				srcFilenameSet.add(sourceSubPath.getFileName());
			}
		} catch (Exception e) {
			bLog.log(Level.SEVERE, "Exception when scanning directory to create parallel scan " + srcPath, e);
		}

		if (Files.exists(bufPath)) {
			try (DirectoryStream<Path> bufferFileStream = Files.newDirectoryStream(bufPath)) {

				for (Path bufferSubPath : bufferFileStream) {

					if (!srcFilenameSet.contains(bufferSubPath.getFileName())) {
						Path srcSubPath = srcPath.resolve(bufPath.relativize(bufferSubPath));
						Path tgtSubPath = tgtPath.resolve(bufPath.relativize(bufferSubPath));
						addBackUpTask(srcSubPath, bufferSubPath, tgtSubPath, sizeWarningLimit);
						bufFilenameSet.add(bufferSubPath.getFileName());
					}
				}
			} catch (Exception e) {
				bLog.log(Level.SEVERE, "Exception when scanning directory to create parallel scan " + bufPath, e);
			}
		}

		if (Files.exists(tgtPath)) {
			try (DirectoryStream<Path> targetFileStream = Files.newDirectoryStream(tgtPath)) {

				for (Path targetSubPath : targetFileStream) {
					if ((!srcFilenameSet.contains(targetSubPath.getFileName()))
							&& (!bufFilenameSet.contains(targetSubPath.getFileName()))) {
						Path srcSubPath = srcPath.resolve(tgtPath.relativize(targetSubPath));
						Path bufSubPath = bufPath.resolve(tgtPath.relativize(targetSubPath));
						addBackUpTask(srcSubPath, bufSubPath, targetSubPath, sizeWarningLimit);
					}
				}

			} catch (Exception e) {
				bLog.log(Level.SEVERE, "Exception when scanning directory to create parallel scan " + tgtPath, e);
			}
		}
	}
	
	private void addBackUpTask(Path srcPath, Path bufPath, Path tgtPath, long sizeWarningLimit) throws IOException {
		if ((srcPath != null)) {
			if (bufPath != null) {
				if (Files.isRegularFile(srcPath)) {
					Path bufFile = bufPath.resolve(srcPath.getFileName());
					DirectoryGroupMap directoryGroupMapForThisBackUpTask = new DirectoryGroupMap(srcPath, srcPath, directoryGroupConfiguration);
					backUpTasks.get(JobTaskType.SOURCE_TO_BUFFER).add(new BackUpTask(srcPath, bufFile, directoryGroupMapForThisBackUpTask, sizeWarningLimit));
				} else {
					DirectoryGroupMap directoryGroupMapForThisBackUpTask = new DirectoryGroupMap(srcPath, srcPath, directoryGroupConfiguration);
					backUpTasks.get(JobTaskType.SOURCE_TO_BUFFER).add(new BackUpTask(srcPath, bufPath, directoryGroupMapForThisBackUpTask, sizeWarningLimit));
				}
				if (tgtPath != null) {
					DirectoryGroupMap directoryGroupMapForThisBackUpTask = new DirectoryGroupMap(srcPath, bufPath, directoryGroupConfiguration);
					backUpTasks.get(JobTaskType.BUFFER_TO_TARGET).add(new BackUpTask(bufPath, tgtPath, directoryGroupMapForThisBackUpTask, sizeWarningLimit));
				}
			} else if (tgtPath != null) {
				DirectoryGroupMap directoryGroupMapForThisBackUpTask = new DirectoryGroupMap(srcPath, srcPath, directoryGroupConfiguration);
				backUpTasks.get(JobTaskType.SOURCE_TO_TARGET).add(new BackUpTask(srcPath, tgtPath, directoryGroupMapForThisBackUpTask, sizeWarningLimit));
			} else {
				bLog.severe("No buffer and target element definition for back up job " + title);
			}
		} else {
			bLog.severe("No source element definition for back up job " + title);
		}

	}
	
	public static void setDefaultWarningSizeLimit(long defaultWarningSizeLimit) {
		BackUpJob.defaultWarningSizeLimit = defaultWarningSizeLimit;
	}

	public String toString() {
		return title;
	}

	public List<BackUpTask> getTasks(JobTaskType jobTaskType) {
		
		initBackUpTasksMap();
		
		fullBackUpTaskList.forEach(fullBackUpTask -> {
						
			if (fullBackUpTask.isScanInParallel()) {
				addParallelBackUpTasks(fullBackUpTask.getSrcPath(), fullBackUpTask.getBufPath(), fullBackUpTask.getTgtPath(), fullBackUpTask.getSizeWarningLimit());
			} else {
				try {
					addBackUpTask(fullBackUpTask.getSrcPath(), fullBackUpTask.getBufPath(), fullBackUpTask.getTgtPath(), fullBackUpTask.getSizeWarningLimit());
				} catch (IOException e) {
					bLog.log(Level.SEVERE, "Exception when creating backup task with target path" + fullBackUpTask.getTgtPath(), e);
				}
			}
			
		});
		
		if (backUpTasks.get(jobTaskType) == null) {
			return null;
		} else {
			return Collections.unmodifiableList(backUpTasks.get(jobTaskType));
		}
	}
	
	public Set<JobTaskType> getAllJobTaskType() {
		
		return Stream.of(JobTaskType.values())
			.filter(jtt -> !getTasks(jtt).isEmpty())
			.collect(Collectors.toSet());
	}
	
	private Path getPathElement(JsonNode jObjItem, String prop) throws URISyntaxException {

		Path returnPath = null;
		JsonNode elem = jObjItem.get(prop);
		if (elem != null) {
			returnPath = FilesUtils.uriStringToAbsolutePath(elem.asText());
		}
		return returnPath;
	}

	private boolean getParallelScanElement(JsonNode jObjItem, String prop) {

		boolean scanInParallel = false;
		JsonNode elem = jObjItem.get(prop);
		if (elem != null) {
			scanInParallel = elem.asBoolean();
		}
		return scanInParallel;
	}
	
	private long getSizeWarningLimit(JsonNode jObjItem, String prop) {
		
		JsonNode elem = jObjItem.get(prop);
		if (elem != null) {
			return elem.asLong();
		} else {
			return defaultWarningSizeLimit;
		}
	}
}
