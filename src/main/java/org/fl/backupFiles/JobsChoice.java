/*
 * MIT License

Copyright (c) 2017, 2023 Frederic Lefevre

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

import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.fl.backupFiles.BackUpJob.JobTaskType;

public class JobsChoice {

	private static final Logger jLog = Config.getLogger();
	
	private final List<BackUpJob> backUpJobs ;
	private final String 		  jobsTitleString ;
	private final String 		  jobsTitleHtml ;
	private final String 		  jobsDetail ;
	private final StringBuilder   details ;
	
	private boolean	compareContent;
	private boolean	compareContentOnAmbiguous;
	
	private final static String jobSeparator  	 = "\n__________________________\n" ;
	private final static String taskJobSeparator = "\n\n" ;
	private final static String taskSeparator 	 = "\n" ;
	
	private final Map<JobTaskType, ArrayList<BackUpTask>> backUpTasks ;
	
	private Map<FileStore,TargetFileStore> targetFileStores ;
	
	public JobsChoice(List<BackUpJob> bj) {
		
		backUpJobs = bj;
		
		compareContent = false;
		compareContentOnAmbiguous = true;
		
		targetFileStores = new HashMap<FileStore,TargetFileStore>() ;
		
		StringBuilder titlesString = new StringBuilder() ;
		StringBuilder titlesHtml   = new StringBuilder("<html><body>") ;
		for (BackUpJob backUpJob : backUpJobs) {
			titlesString.append(backUpJob.toString()).append("\n") ;
			titlesHtml.append(backUpJob.toString()).append("<br/>") ;
		}
		titlesHtml.append("</body></html>") ;
		jobsTitleString = titlesString.toString() ;
		jobsTitleHtml   = titlesHtml.toString() ;
		
		details = new StringBuilder(1024) ;
		backUpTasks = new HashMap<JobTaskType, ArrayList<BackUpTask>>() ;
		for (JobTaskType jtt : JobTaskType.values()) {
			ArrayList<BackUpTask> tasksForJtt = new ArrayList<BackUpTask>() ;
			details.append(jobSeparator).append(jtt.toString()).append(taskJobSeparator) ;
			backUpTasks.put(jtt, tasksForJtt) ;
			for (BackUpJob backUpJob : backUpJobs) {
				addAllTasks(tasksForJtt, backUpJob.getTasks(jtt)) ;
			}
		}
		jobsDetail = details.toString() ;
		
	}

	public String getTitleAsString() {
		return jobsTitleString ;
	}
	
	public String getTitleAsHtml() {
		return jobsTitleHtml ;
	}
	
	public String printDetail() {
		return jobsDetail ;
	}
	
	public List<BackUpTask> getTasks(JobTaskType jobTaskType) {
				
		return backUpTasks.get(jobTaskType) ;
	}
	
	public void setCompareContent(JobTaskType jobTaskType, boolean cc) {
		
		compareContent = cc ;
		getTasks(jobTaskType)
			.forEach(backUpTask -> backUpTask.setCompareContent(cc));
	}
	
	public void setCompareContentOnAmbiguous(JobTaskType jobTaskType, boolean cc) {
		
		compareContentOnAmbiguous = cc ;
		getTasks(jobTaskType)
			.forEach(backUpTask -> backUpTask.setCompareContentOnAmbiguous(cc));
	}
	
	private void addAllTasks(List<BackUpTask> tasks, List<BackUpTask> tasksToAdd) {
		for (BackUpTask taskToAdd : tasksToAdd) {
			if (! tasks.contains(taskToAdd)) {
				tasks.add(taskToAdd) ;
				details.append(taskToAdd.toString()).append(taskToAdd.eventualWarning()).append(taskSeparator) ;
			}
		}
	}
	
	public void initTargetFileStores(JobTaskType jobTaskType) {
		for (BackUpTask backUpTask : getTasks(jobTaskType)) {
			Path targetPath = backUpTask.getTarget() ;
			if ((targetPath != null) && (Files.exists(targetPath))) {
				TargetFileStore targetFileStore = new TargetFileStore(targetPath, jLog) ;
				FileStore fs = targetFileStore.getFileStore() ;
				if (fs != null) { 
					if (! targetFileStores.containsKey(fs)) {
						// put the new target file store in the map
						targetFileStores.put(fs, targetFileStore) ;
					} else {
						// Target file store exists in the map
						// just update remaining space
						targetFileStores.get(fs).memorizeInitialRemainingSpace() ;
					}
				}
			}
			
			
		}
	}
	
	public String getTargetRemainigSpace(boolean inHtml) {
		
		StringBuilder spaceEvol = new StringBuilder() ;
		if (inHtml) {
			spaceEvol.append("<p>") ;
		}
		spaceEvol.append("Stockage de fichiers, espace restant utilisable:") ;
		if (inHtml) {
			spaceEvol.append("<ul>") ;
		} else {
			spaceEvol.append("\n") ;
		}
		for (TargetFileStore targetFileStore : targetFileStores.values()) {
			if (inHtml) {
				spaceEvol.append("<li>") ;
			} else {
				spaceEvol.append("- ") ;
			}
			targetFileStore.getSpaceEvolution(spaceEvol) ;
			if (inHtml) {
				spaceEvol.append("</li>") ;
			} else {
				spaceEvol.append("\n") ;
			}
		}
		return spaceEvol.toString() ;
	}

	public boolean compareContent() {
		return compareContent;
	}
	
	public boolean compareContentOnAmbiguous() {
		return compareContentOnAmbiguous;
	}
}
