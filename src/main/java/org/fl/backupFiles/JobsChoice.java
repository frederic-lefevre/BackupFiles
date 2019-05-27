package org.fl.backupFiles;

import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.fl.backupFiles.BackUpJob.JobTaskType;

public class JobsChoice {

	private final List<BackUpJob> backUpJobs ;
	private final String 		  jobsTitleString ;
	private final String 		  jobsTitleHtml ;
	private final String 		  jobsDetail ;
	private final StringBuilder   details ;
	
	private boolean			compareContent ;
	
	private Logger jLog ;
	
	private final static String jobSeparator  	 = "\n__________________________\n" ;
	private final static String taskJobSeparator = "\n\n" ;
	private final static String taskSeparator 	 = "\n" ;
	
	private final HashMap<JobTaskType, ArrayList<BackUpTask>> backUpTasks ;
	
	private HashMap<FileStore,TargetFileStore> targetFileStores ;
	
	public JobsChoice(List<BackUpJob> bj, Logger l) {
		
		backUpJobs = bj ;
		jLog	   = l ;
		
		compareContent = false ;
		
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
	
	public ArrayList<BackUpTask> getTasks(JobTaskType jobTaskType) {
				
		return backUpTasks.get(jobTaskType) ;
	}
	
	public void setCompareContent(JobTaskType jobTaskType, boolean cc) {
		
		compareContent = cc ;
		ArrayList<BackUpTask> bupTasks = getTasks(jobTaskType) ;
		for (BackUpTask backUpTask : bupTasks) {
			backUpTask.setCompareContent(cc);
		}
	}
	
	private void addAllTasks(ArrayList<BackUpTask> tasks, ArrayList<BackUpTask> tasksToAdd) {
		for (BackUpTask taskToAdd : tasksToAdd) {
			if (! tasks.contains(taskToAdd)) {
				tasks.add(taskToAdd) ;
				details.append(taskToAdd.toString()).append(taskToAdd.eventualWarning()).append(taskSeparator) ;
			}
		}
	}
	
	public void initTargetFileStores(JobTaskType jobTaskType) {
		ArrayList<BackUpTask> bupTasks = getTasks(jobTaskType) ;
		for (BackUpTask backUpTask : bupTasks) {
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
						targetFileStores.get(fs).setInitialRemainingSpace() ;
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
}
