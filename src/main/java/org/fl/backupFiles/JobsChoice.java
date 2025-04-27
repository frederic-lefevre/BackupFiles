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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fl.backupFiles.BackUpJob.JobTaskType;

public class JobsChoice {
	
	private final List<BackUpJob> backUpJobs;
	private final String jobsTitleString;
	private final String jobsTitleHtml;
	private final String compareOperationAsHtml;
	private final String jobsDetail;

	private boolean compareContent;
	private boolean compareContentOnAmbiguous;

	private static final String HTML_BEGIN = "<html><body>";
	private static final String HTML_END = "</body></html>";
	private static final String jobSeparator = "\n__________________________\n";
	private static final String taskJobSeparator = "\n\n";
	private static final String taskSeparator = "\n";

	private final Map<JobTaskType, ArrayList<BackUpTask>> backUpTasks;

	private final TargetFileStores targetFileStores;
	
	public JobsChoice(List<BackUpJob> bj) {

		backUpJobs = bj;

		compareContent = false;
		compareContentOnAmbiguous = true;

		targetFileStores = new TargetFileStores();

		StringBuilder titlesString = new StringBuilder();
		StringBuilder titlesHtml = new StringBuilder(HTML_BEGIN);
		for (BackUpJob backUpJob : backUpJobs) {
			titlesString.append(backUpJob.toString()).append("\n");
			titlesHtml.append(backUpJob.toString()).append("<br/>");
		}
		titlesHtml.append(HTML_END);
		jobsTitleString = titlesString.toString();
		jobsTitleHtml = titlesHtml.toString();
		compareOperationAsHtml = buildCompareOperationAsHtml();

		long fileStoreRemainingSpaceWarningThreshold = Config.getFileStoreRemainingSpaceWarningThreshold();
		StringBuilder details = new StringBuilder(1024);
		backUpTasks = new HashMap<JobTaskType, ArrayList<BackUpTask>>();
		for (JobTaskType jtt : JobTaskType.values()) {
			ArrayList<BackUpTask> tasksForJtt = new ArrayList<BackUpTask>();
			details.append(jobSeparator).append(jtt.toString()).append(taskJobSeparator);
			backUpTasks.put(jtt, tasksForJtt);
			for (BackUpJob backUpJob : backUpJobs) {
				addAllTasks(tasksForJtt, backUpJob.getTasks(jtt), details);
			}
			initTargetFileStores(jtt, fileStoreRemainingSpaceWarningThreshold);
		}
		jobsDetail = details.toString();

	}

	public TargetFileStores getTargetFileStores() {
		return targetFileStores;
	}

	public String getTitleAsString() {
		return jobsTitleString;
	}

	public String getTitleAsHtml() {
		return jobsTitleHtml;
	}

	public String getCompareOperationAsHtml() {
		return compareOperationAsHtml;
	}
	
	public String printDetail() {
		return jobsDetail;
	}

	public List<BackUpTask> getTasks(JobTaskType jobTaskType) {

		return backUpTasks.get(jobTaskType);
	}

	public void setCompareContent(JobTaskType jobTaskType, boolean cc) {
		
		compareContent = cc;
		getTasks(jobTaskType)
			.forEach(backUpTask -> backUpTask.setCompareContent(cc));
	}
	
	public void setCompareContentOnAmbiguous(JobTaskType jobTaskType, boolean cc) {
		
		compareContentOnAmbiguous = cc;
		getTasks(jobTaskType)
			.forEach(backUpTask -> backUpTask.setCompareContentOnAmbiguous(cc));
	}
	
	private String buildCompareOperationAsHtml() {
		
		StringBuilder compareType = new StringBuilder(HTML_BEGIN);
		compareType.append("Comparaison");
		if (compareContent()) {
			compareType.append(" avec comparaison du contenu");
		} else if (compareContentOnAmbiguous()) {
			compareType.append(" avec comparaison du contenu pour les fichiers ambigues");
		}
		return compareType.toString();
	}
	
	private void addAllTasks(List<BackUpTask> tasks, List<BackUpTask> tasksToAdd, StringBuilder details) {
		for (BackUpTask taskToAdd : tasksToAdd) {
			if (! tasks.contains(taskToAdd)) {
				tasks.add(taskToAdd) ;
				details.append(taskToAdd.toString()).append(taskToAdd.eventualWarning()).append(taskSeparator);
			}
		}
	}
	
	private void initTargetFileStores(JobTaskType jobTaskType, long warningThreshold) {
		getTasks(jobTaskType).forEach(backUpTask -> targetFileStores.addTargetFileStore(backUpTask.getTarget(), warningThreshold));
	}

	public boolean compareContent() {
		return compareContent;
	}
	
	public boolean compareContentOnAmbiguous() {
		return compareContentOnAmbiguous;
	}
}
